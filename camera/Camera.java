package camera;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;

import util.ModuleRegistry;
import util.Updater;
import canon.cdsdk.CDSDK;
import canon.cdsdk.CDSDKException;
import canon.cdsdk.Constants;
import canon.cdsdk.Enum;
import canon.cdsdk.ProgressMonitor;
import canon.cdsdk.ReleaseEventHandler;
import canon.cdsdk.ReleaseImageInfo;
import canon.cdsdk.Source;
import canon.cdsdk.SourceInfo;
import digital_table.controller.DigitalTableModule;

// TODO move the webserver update stuff out of here - updater can listen to captures instead
// TODO currently we converted the corrected image to JPG in the ImageTransfer thread. Probably better to defer this until it is required (client can thread the call if the delay is a problem) (no point with updater running here)
// TODO more sophisticated error handling. figure out what errors can be recovered from. revise the primitive protection (transfering flag) in capture()

public class Camera implements CameraModule {
	private CDSDK sdk;
	Source source;

	// TODO figure out a better way to handle the camera being busy than this flag
	private boolean transferring;	// will be true while an image is being transfered. access to the camera will fail during this time

	private BufferedImage rawImage;
	private LensCorrection lensCorrect;
	private Homography H;
	private int remappedWidth;
	private int remappedHeight;
	private byte[] lastCorrectedImage;

	private EventListenerList moduleListenerList = new EventListenerList();

	public Camera() {
		sdk = new CDSDK();
		sdk.open();
		ModuleRegistry.register(CameraModule.class, this);
	}

	@Override
	public void moduleExit() {
		disconnect();
		sdk.close();
	}

	@Override
	public void addCameraModuleListener(CameraModuleListener l) {
		moduleListenerList.add(CameraModuleListener.class, l);
	}

	@Override
	public void removeCameraModuleListener(CameraModuleListener l) {
		moduleListenerList.remove(CameraModuleListener.class, l);
	}

	@Override
	public byte[] getLatestCorrectedJPEG() {
		return lastCorrectedImage;
	}

	private void fireImageCaptured(BufferedImage image, long size, Exception exception, String msg) {
		CameraModuleListener[] listeners = moduleListenerList.getListeners(CameraModuleListener.class);
		for (CameraModuleListener l : listeners) {
			l.imageCaptured(image, size, exception, msg);
		}
	}

	private void fireConnectEvent(String name) {
		CameraModuleListener[] listeners = moduleListenerList.getListeners(CameraModuleListener.class);
		for (CameraModuleListener l : listeners) {
			l.cameraConnected(name);
		}
	}

	private void fireDisconnectEvent() {
		CameraModuleListener[] listeners = moduleListenerList.getListeners(CameraModuleListener.class);
		for (CameraModuleListener l : listeners) {
			l.cameraDisconnected();
		}
	}

	private void fireErrorEvent(int error) {
		CameraModuleListener[] listeners = moduleListenerList.getListeners(CameraModuleListener.class);
		for (CameraModuleListener l : listeners) {
			l.cameraError(error);
		}
	}

	private void fireHomographyChanged(Exception e) {
		CameraModuleListener[] listeners = moduleListenerList.getListeners(CameraModuleListener.class);
		for (CameraModuleListener l : listeners) {
			l.homographyChanged(e);
		}
	}

	// attempts to connect to the first camera found.
	void connect() throws CDSDKException, CameraNotFoundException {
		int count = 0;
		try {
			Enum<SourceInfo> handle = sdk.getDeviceEnum();
			count = handle.getCount();
			//System.out.println("Device Count = "+count);

			if (count > 0) {
				SourceInfo info = handle.next();
				source = Source.open(info);
				//System.out.println("Handle = "+source);

				source.enterReleaseControl(releaseListener);

				fireConnectEvent(info.name);
				handle.release();
			}
		} catch (CDSDKException e) {
			disconnect();
			throw e;
		}
		if (count == 0) {
			throw new CameraNotFoundException();
		}
	}

	// returns true if we think we're connected. note that when a camera disconnects we
	// are not notified until we try to access the camera in some way. so a true return
	// does not guarentee any future operation will succeed
	boolean isConnected() {
		return source != null;
	}

	// ReleaseEventHandler methods
	private final ReleaseEventHandler releaseListener = new ReleaseEventHandler() {
		@Override
		public void errorOccured(int error) {
			disconnect();
			fireErrorEvent(error);
		}

		@Override
		public void parametersChanged() {
			//System.out.println("parametersChanged()");
		}

		@Override
		public void releaseButtonPressed() {
			//System.out.println("releaseButtonPressed()");
		}

		@Override
		public void releaseComplete(int numData) {
			//System.out.println("releaseComplete("+numData+")");
		}

		@Override
		public void releaseStart() {
			//System.out.println("releaseStart()");
		}

		@Override
		public void unknownEvent(int eventID) {
			//System.out.println("unknownEvent("+eventID+")");
		}

		@Override
		public void viewFinderDisabled() {
			//System.out.println("viewFinderDisabled()");
		}

		@Override
		public void viewFinderEnabled() {
			//System.out.println("viewFinderEnabled()");
		}

		@Override
		public void rotationAngleChanged(int angle) {
			//System.out.println("rotationAngleChanged("+angle+")");
		}
	};

	void disconnect() {
		if (source != null) {
			try {
				source.exitReleaseControl();
			} catch (Exception e) {
				// don't really have anything to do
			}
			source.close();
			source = null;
		}
		fireDisconnectEvent();
	}

	void capture(boolean applyLensCorrect, boolean applyHomography, PropertyChangeListener progressListener) throws CameraNotConnectedException {
		if (source == null) {
			throw new CameraNotConnectedException();
		}
		if (transferring) return;	// TODO throw exception?
		try {
			source.setReleaseDataKind(Constants.REL_KIND_PICT_TO_PC);
			//Rational mag = source.getDigitalMagnification();
			//System.out.println("Digital magnification = "+mag.numerator+"/"+mag.denominator);
			int numData = source.releaseShutter(false, null, 0);
			//System.out.println("Release resulted in "+numData+" lots of data to pickup");

			for (int i = 0; i < numData; i++) {
				//System.out.println("Data #"+i);
				ReleaseImageInfo relinfo = source.getReleasedDataInfo(null);
				if (relinfo.type == Constants.DATA_TYPE_PICTURE) {
					relinfo = new ReleaseImageInfo();
					ImageTransfer transfer = new ImageTransfer(relinfo, applyLensCorrect, applyHomography);
					if (progressListener != null) transfer.addPropertyChangeListener(progressListener);
					transfer.execute();
				} else {
					//System.out.println("Not picking up data of type "+relinfo.type);
				}
			}
		} catch (CDSDKException e) {
			disconnect();
			throw e;
		}
	}

	BufferedImage getLastImage(boolean applyLensCorrection, boolean applyHomography) {
		if (rawImage == null) return null;

		int width = rawImage.getWidth();
		int height = rawImage.getHeight();
		List<PointRemapper> corrections = new ArrayList<>();
		if (applyLensCorrection) corrections.add(lensCorrect);
		if (applyHomography) {
			corrections.add(H);
			width = remappedWidth;
			height = remappedHeight;
		}

		if (corrections.size() == 0) {
			return rawImage;
		} else {
			return PixelInterpolator.getImage(corrections, rawImage, width, height);
		}
	}

	void autoCalibrate() {
		DigitalTableModule dt = ModuleRegistry.getModule(DigitalTableModule.class);
		if (dt == null) throw new IllegalStateException("Autocalibrate called with no digital table available");

		dt.setCalibrateDisplay(true);

		try {
			source.setReleaseDataKind(Constants.REL_KIND_PICT_TO_PC);
			int numData = source.releaseShutter(false, null, 0);

			for (int i = 0; i < numData; i++) {
				ReleaseImageInfo relinfo = source.getReleasedDataInfo(null);
				if (relinfo.type == Constants.DATA_TYPE_PICTURE) {
					relinfo = new ReleaseImageInfo();
					CalibrateImageTransfer transfer = new CalibrateImageTransfer(relinfo);
					transfer.execute();
				}
			}
		} catch (CDSDKException e) {
			disconnect();
			throw e;
		}

		dt.setCalibrateDisplay(false);
	}

	void setHomography(Point[] regPoints) {
		// calculate world-coordinates for the control points based on the distance from point 0 to point 1 (width)
		// and from point 0 to point 3 (height):
		double w = regPoints[0].distance(regPoints[1]);
		double h = regPoints[0].distance(regPoints[3]);
		double x = w * 2 / 36;
		double y = h / 30;
		final Point2D[] worldPoint = new Point2D.Double[4];
		worldPoint[0] = new Point2D.Double(x, y);
		worldPoint[1] = new Point2D.Double(x + w, y);
		worldPoint[2] = new Point2D.Double(x + w, y + h);
		worldPoint[3] = new Point2D.Double(x, y + h);
//		for (int i = 0; i < 4; i++) {
//			System.out.println("world point " + i + ": " + worldPoint[i]);
//		}

		H = Homography.createHomographySVD(regPoints, worldPoint);
		remappedWidth = (int) (w * 39 / 36);
		remappedHeight = (int) (h * 32 / 30);
		fireHomographyChanged(null);
	}

	private class CalibrateImageTransfer extends SwingWorker<BufferedImage, BufferedImage> implements ProgressMonitor {
		ReleaseImageInfo relinfo;
		BufferedImage rawImage;
		BufferedImage image = null;
		Exception exception;

		public CalibrateImageTransfer(ReleaseImageInfo ri) {
			relinfo = ri;
		}

		@Override
		protected BufferedImage doInBackground() {
			try {
				byte[] buffer = source.getReleasedDataToBuffer(relinfo, this, 3);
				ByteArrayInputStream is = new ByteArrayInputStream(buffer);
				rawImage = ImageIO.read(is);

				int width = rawImage.getWidth();
				int height = rawImage.getHeight();
				List<PointRemapper> corrections = new ArrayList<>();

				// TODO need to set these parameters based on the camera zoom
				lensCorrect = new LensCorrection(-0.007715, 0.026731, 0.000000, rawImage.getWidth(), rawImage.getHeight());
				corrections.add(lensCorrect);

				image = PixelInterpolator.getImage(corrections, rawImage, width, height);

				return image;
			} catch (Exception e) {
				exception = e;
				return null;
			}
		}

		@Override
		protected void done() {
			Point[] points = AutoCalibrate.calibrate(image);
			if (points == null) {
				fireHomographyChanged(exception);
			} else {
				setHomography(points);
			}
		}

//	 ProgressMonitor methods
		@Override
		public boolean setProgress(int progress, int status) {
			return true;
		}
	}

	private class ImageTransfer extends SwingWorker<BufferedImage, BufferedImage> implements ProgressMonitor {
		ReleaseImageInfo relinfo;
		BufferedImage rawImage;
		BufferedImage image = null;
		boolean applyLensCorrection, applyHomography;
		Exception exception;
		byte[] buffer;

		public ImageTransfer(ReleaseImageInfo ri, boolean lensCorrect, boolean homography) {
			transferring = true;
			relinfo = ri;
			applyLensCorrection = lensCorrect;
			applyHomography = homography;
		}

		@Override
		protected BufferedImage doInBackground() {
			try {
				buffer = source.getReleasedDataToBuffer(relinfo, this, 3);
				transferring = false;
				//System.out.println(" Sequence = "+relinfo.sequence);
				//System.out.println(" Type = "+relinfo.type);
				//System.out.println(" Format = "+relinfo.format);
				//System.out.println(" Size = "+relinfo.size);
				//		System.out.println("Buffer size = "+buffer.length);
				ByteArrayInputStream is = new ByteArrayInputStream(buffer);
				rawImage = ImageIO.read(is);
				lensCorrect = new LensCorrection(-0.007715, 0.026731, 0.000000, rawImage.getWidth(), rawImage.getHeight());

				if (applyLensCorrection || applyHomography) {
					int width = rawImage.getWidth();
					int height = rawImage.getHeight();
					List<PointRemapper> corrections = new ArrayList<>();

					if (applyLensCorrection) {
						// TODO need to set these parameters based on the camera zoom
						corrections.add(lensCorrect);
					}
					if (applyHomography) {
						width = remappedWidth;
						height = remappedHeight;
						corrections.add(H);
					}

					image = PixelInterpolator.getImage(corrections, rawImage, width, height);

					// rewrite to buffer
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(image, "JPG", os);
					buffer = os.toByteArray();
					lastCorrectedImage = applyHomography ? buffer : null;

				} else {
					image = rawImage;
				}

				return image;
			} catch (Exception e) {
				exception = e;
				return null;
			}
		}

		@Override
		protected void done() {
			String msg = null;
			if (buffer != null) {
				msg = Updater.updateURL(Updater.MAP_IMAGE, buffer);
			}

			if (image != null) {
				//System.out.println("Got image size "+image.getWidth()+"x"+image.getHeight());
				Camera.this.rawImage = rawImage;
			}
			fireImageCaptured(image, relinfo.size, exception, msg);
		}

//	 ProgressMonitor methods
		@Override
		public boolean setProgress(int progress, int status) {
			//System.out.println("Progress = "+progress+", status = "+status);
			setProgress(progress);
			return true;
		}
	}
}

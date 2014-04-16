package camera;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import swing.ImagePanel;
import util.Updater;
import canon.cdsdk.CDSDK;
import canon.cdsdk.Constants;
import canon.cdsdk.Enum;
import canon.cdsdk.ImageFormat;
import canon.cdsdk.MaximumZoom;
import canon.cdsdk.ProgressMonitor;
import canon.cdsdk.ReleaseEventHandler;
import canon.cdsdk.ReleaseImageInfo;
import canon.cdsdk.Source;
import canon.cdsdk.SourceInfo;
import digital_table.controller.ControllerFrame;

// TODO calibrate buttons should be disabled when the camera has not taken a shot
// TODO may be fixed: controls stop responding if you hit stop during a transfer
@SuppressWarnings("serial")
public class CameraPanel extends JPanel implements ActionListener {
	private long defaultDelay = 60000;
	private int defaultImageSize = Constants.IMAGE_SIZE_LARGE;
	private int defaultImageQuality = Constants.COMP_QUALITY_NORMAL;
	private int defaultWhiteBalance = 0;

	private ImagePanel imagePanel;
	private JProgressBar progressBar;
	private JButton capture, startStop;
	private JComboBox<ImageFormatOption> imageFormatCombo;
	private JSlider zoomSlider;
	private JCheckBox focusLockCheck;
	private JFormattedTextField frequencyField;
	private JPanel controls;
	private JButton connectButton;
	private JComboBox<String> whiteBalanceCombo;
	private JTextArea logArea;
	private JCheckBox lensCorrection;
	private JCheckBox applyRemap;
	private JButton calibrateButton;
	private JButton manualButton;

	private static final String[] WHITE_BALANCE_NAMES = { "Auto", "Daylight", "Cloudy", "Tungsten", "Fluorescent", "Flash" };

	private CaptureThread timer;
	private long lastShot = System.currentTimeMillis();

	private CDSDK sdk;
	private Source source;

	private BufferedImage rawImage = null;
	private LensCorrection lensCorrect;
	private Homography H = null;
	private int remappedWidth;
	private int remappedHeight;
	private byte[] lastCorrectedImage = null;

	private ControllerFrame overlayGenerator = null;

	public CameraPanel() {
		sdk = new CDSDK();
		sdk.open();
		timer = new CaptureThread(defaultDelay);
		createAndShowGUI();
		connect(true);
	}

	public void setOverlayGenerator(ControllerFrame generator) {
		overlayGenerator = generator;
	}

	public byte[] getLatestCorrectedImage() {
		return lastCorrectedImage;
	}

	private void createAndShowGUI() {
		setLayout(new BorderLayout());

		imagePanel = new ImagePanel(null);
		imagePanel.setAllowEnargements(false);
//	imagePanel.setBackground(Color.RED);
		add(imagePanel,BorderLayout.CENTER);

		controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));

		JPanel buttons = new JPanel();
		connectButton = new JButton("Connect");
		connectButton.addActionListener(e -> connect());
		calibrateButton = new JButton("Autocalibrate");
		calibrateButton.addActionListener(e -> autoCalibrate());
//		calibrateButton.setEnabled(false);
		manualButton = new JButton("Manually Calibrate");
		manualButton.addActionListener(e -> manualCalibrate());
//		manualButton.setEnabled(false);
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
		buttons.add(connectButton);
		buttons.add(Box.createRigidArea(new Dimension(5, 0)));
		buttons.add(calibrateButton);
		buttons.add(Box.createRigidArea(new Dimension(5, 0)));
		buttons.add(manualButton);
		controls.add(buttons);

		capture = new JButton("Capture");
		capture.addActionListener(e -> {
			capture();
			if (timer.isRunning()) {
				timer.reschedule();
			}
		});
		startStop = new JButton("Start");
		startStop.addActionListener(e -> {
			if (timer.isRunning()) {
				timer.stopCapture();
				startStop.setText("Start");
			} else {
				timer.startCapture();
				startStop.setText("Stop");
			}
		});
		buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
		buttons.add(startStop);
		buttons.add(Box.createRigidArea(new Dimension(5,0)));
		buttons.add(capture);
		controls.add(buttons);

		frequencyField = new JFormattedTextField();
		frequencyField.setValue(new Integer((int) (defaultDelay/1000)));
		frequencyField.setColumns(5);
		frequencyField.addPropertyChangeListener("value", e -> {
			Object source = e.getSource();
			if (source == frequencyField) {
				int freq = ((Number) frequencyField.getValue()).intValue();
				freq *= 1000;
				if (timer.getDelay() != freq) {
					timer.setDelay(freq);
				}
			}
		});
		Dimension maxSize = frequencyField.getMaximumSize();
		maxSize.height = frequencyField.getPreferredSize().height;
		frequencyField.setMaximumSize(maxSize);
		JPanel freqPanel = new JPanel();
		freqPanel.setLayout(new BoxLayout(freqPanel, BoxLayout.LINE_AXIS));
		freqPanel.add(new JLabel("Take shot every"));
		freqPanel.add(Box.createRigidArea(new Dimension(5,0)));
		freqPanel.add(frequencyField);
		freqPanel.add(Box.createRigidArea(new Dimension(5,0)));
		freqPanel.add(new JLabel("seconds"));
		controls.add(freqPanel);

		controls.add(new JLabel("Zoom"));
		zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 0, 0);
		zoomSlider.setPaintTicks(true);
		zoomSlider.addChangeListener(e -> {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				//System.out.println("Setting zoom to "+slider.getValue());
				source.setZoomPosition(slider.getValue());
				// set AF lock if it should be set (changing the zoom will unset it)
				if (focusLockCheck.isSelected()) source.setAFLock(true);
			}
		});
		controls.add(zoomSlider);

		controls.add(new JLabel("Size & Quality"));
		imageFormatCombo = new JComboBox<>();
		Dimension prefSize = imageFormatCombo.getPreferredSize();
		prefSize.width = 300;
		imageFormatCombo.setMaximumSize(prefSize);
		imageFormatCombo.addActionListener(this);
		controls.add(imageFormatCombo);

		focusLockCheck = new JCheckBox("AF Lock");
		focusLockCheck.addItemListener(e -> {
			if (e.getSource() == focusLockCheck) {
				//System.out.println("Autofocus locked? "+focusLockCheck.isSelected());
				source.setAFLock(focusLockCheck.isSelected());
			}
		});
		controls.add(focusLockCheck);

		controls.add(new JLabel("White Balance"));
		whiteBalanceCombo = new JComboBox<>();
		prefSize = whiteBalanceCombo.getPreferredSize();
		prefSize.width = 300;
		whiteBalanceCombo.setMaximumSize(prefSize);
		whiteBalanceCombo.addActionListener(this);
		controls.add(whiteBalanceCombo);

		// TODO toggling should immediately affect the image
		lensCorrection = new JCheckBox("Apply Lens Correction");
		lensCorrection.setSelected(true);
		controls.add(lensCorrection);

		// TODO toggling should immediately affect the image
		applyRemap = new JCheckBox("Remap Image");
		applyRemap.setEnabled(false);
		controls.add(applyRemap);

		logArea = new JTextArea(20,40);
		JScrollPane scrollPane = new JScrollPane(logArea);
		logArea.setEditable(false);
		controls.add(scrollPane);

		setControls(false);

		add(controls,BorderLayout.LINE_START);

		progressBar = new JProgressBar();
		add(progressBar,BorderLayout.PAGE_END);

		timer.start();
	}

	private void logMessage(final String logMsg) {
		SwingUtilities.invokeLater(() -> logArea.append(logMsg + "\n"));
	}

	private void setControls(boolean active) {
		capture.setEnabled(active);
		startStop.setEnabled(active);
		imageFormatCombo.setEnabled(active);
		zoomSlider.setEnabled(active);
		focusLockCheck.setEnabled(active);
		frequencyField.setEnabled(active);
		whiteBalanceCombo.setEnabled(active);

		connectButton.setEnabled(!active);
	}

	private void connect() {
		connect(false);
	}

	private void connect(boolean noWarning) {
		try {
			Enum<SourceInfo> handle = sdk.getDeviceEnum();
			int count = handle.getCount();
			//System.out.println("Device Count = "+count);

			if (count > 0) {
				SourceInfo info = handle.next();
				logMessage("Found "+info.name);
				//System.out.println("  Type = "+info.type+", Name = "+info.name+", OS Name = "+info.osName);

				source = Source.open(info);
				//System.out.println("Handle = "+source);

				source.enterReleaseControl(releaseListener);
				getCameraOptions();
				setControls(true);

				handle.release();

			} else {
				logMessage("No camera was found");
				if (!noWarning){
					JOptionPane.showOptionDialog(null, "No camera was found", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				}
			}
		} catch (Exception e) {
			logMessage("Exception connecting to camera: "+e.getMessage());
			//e.printStackTrace();
			disconnect();
		}
	}

	private void getCameraOptions() {
		// Zooms
		MaximumZoom max = source.getMaximumZoom();
		zoomSlider.setMaximum(max.maximumZoom);
		zoomSlider.setMinorTickSpacing(1);
		zoomSlider.setMajorTickSpacing(max.maximumOpticalZoom);
		zoomSlider.setValue(source.getZoomPosition());

		// Image formats
		// removing all existing items will trigger an ActionEvent which would cause the camera option to be updated (to an invalid
		// option) so we temporarily suspend ActionEvent processing while we rebuild the list
		imageFormatCombo.removeActionListener(this);
		imageFormatCombo.removeAllItems();
		Enum<ImageFormat> formats = source.getImageFormatEnum();
		boolean foundDefault = false;
		for (int i=0; i<formats.getCount(); i++) {
			ImageFormat f = formats.next();
			if (f.quality == defaultImageQuality && f.size == defaultImageSize) foundDefault = true;
			if (f.quality != Constants.COMP_QUALITY_RAW)
				imageFormatCombo.addItem(new ImageFormatOption(f));
		}
		formats.release();
		imageFormatCombo.addActionListener(this);
		if (foundDefault) source.setImageFormat(defaultImageQuality, defaultImageSize);
		imageFormatCombo.setSelectedItem(new ImageFormatOption(source.getImageFormat()));

		// White balance
		// removing all existing items will trigger an ActionEvent which would cause the camera option to be updated (to an invalid
		// option) so we temporarily suspend ActionEvent processing while we rebuild the list
		whiteBalanceCombo.removeActionListener(this);
		whiteBalanceCombo.removeAllItems();
		Enum<Integer> modes = source.getWhiteBalanceEnum();
		foundDefault = false;
		for (int i=0; i<modes.getCount(); i++) {
			int wb = modes.next();
			if (wb < WHITE_BALANCE_NAMES.length) {
				if (wb == defaultWhiteBalance) foundDefault = true;
				whiteBalanceCombo.addItem(WHITE_BALANCE_NAMES[i]);
			}
		}
		modes.release();
		whiteBalanceCombo.addActionListener(this);
		if (foundDefault) source.setWhiteBalance(defaultWhiteBalance);
		whiteBalanceCombo.setSelectedIndex(source.getWhiteBalance());
	}

	public void disconnect() {
		setControls(false);
		timer.stopCapture();	// TODO if this does wait for capture to finish, should we?
		startStop.setText("Start");
		if (source != null) {
			try {
				source.exitReleaseControl();
			} catch (Exception e) {
				// don't really have anything to do
			}
			source.close();
			source = null;
		}
	}

	private void capture() {
		if (source == null) {
			logMessage("Camera not connected");
			return;
		}
		double secs = (System.currentTimeMillis() - lastShot)/1000.0;
		lastShot = System.currentTimeMillis();
		logMessage("" + secs + "s since last shot");
		try {
			source.setReleaseDataKind(Constants.REL_KIND_PICT_TO_PC);
			progressBar.setMinimum(0);
			progressBar.setMaximum(100);
			progressBar.setValue(0);
			//Rational mag = source.getDigitalMagnification();
			//System.out.println("Digital magnification = "+mag.numerator+"/"+mag.denominator);
			int numData = source.releaseShutter(false, null, 0);
			//System.out.println("Release resulted in "+numData+" lots of data to pickup");

			for (int i=0; i<numData; i++) {
				//System.out.println("Data #"+i);
				ReleaseImageInfo relinfo = source.getReleasedDataInfo(null);
				if (relinfo.type == Constants.DATA_TYPE_PICTURE) {
					relinfo = new ReleaseImageInfo();
					ImageTransfer transfer = new ImageTransfer(relinfo, lastShot);
					transfer.addPropertyChangeListener(evt -> {
						if ("progress".equals(evt.getPropertyName())) {
							progressBar.setValue((Integer)evt.getNewValue());
						}
					});
					transfer.execute();
				} else {
					//System.out.println("Not picking up data of type "+relinfo.type);
				}
			}
		} catch(Exception e) {
			logMessage("Exception taking shot: "+e.getMessage());
			//e.printStackTrace();
			disconnect();
		}
	}

	private void autoCalibrate() {
		if (rawImage == null) {
			JOptionPane.showMessageDialog(this,
					"Need to take a photo before calibration can occur",
					"Calibration error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		List<PointRemapper> corrections = new ArrayList<>();
		// TODO need to set these parameters based on the camera zoom
		corrections.add(new LensCorrection(-0.007715, 0.026731, 0.000000, rawImage.getWidth(), rawImage.getHeight()));
		final BufferedImage lensCorrected = PixelInterpolator.getImage(corrections, rawImage, rawImage.getWidth(), rawImage.getHeight());

		Point[] points = AutoCalibrate.calibrate(lensCorrected);
		if (points != null) setHomography(points);
	}

	private void setHomography(Point[] regPoints) {
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

		applyRemap.setEnabled(true);
		applyRemap.setSelected(true);

		List<PointRemapper> corrections = new ArrayList<>();
		corrections.add(lensCorrect);
		corrections.add(H);
		imagePanel.setImage(PixelInterpolator.getImage(corrections, rawImage, remappedWidth, remappedHeight));
	}

	private void manualCalibrate() {
		if (rawImage == null) {
			JOptionPane.showMessageDialog(this,
					"Need to take a photo before calibration can occur",
					"Calibration error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		final Point[] regPoints = new Point[4];

		List<PointRemapper> corrections = new ArrayList<>();
		// TODO need to set these parameters based on the camera zoom
		corrections.add(new LensCorrection(-0.007715, 0.026731, 0.000000, rawImage.getWidth(), rawImage.getHeight()));
		final BufferedImage lensCorrected = PixelInterpolator.getImage(corrections, rawImage, rawImage.getWidth(), rawImage.getHeight());

		final JPanel image = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(lensCorrected, 0, 0, null);
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(lensCorrected.getWidth(), lensCorrected.getHeight());
			}
		};

		final JLabel countLabel = new JLabel("Chosen: 0");

		image.addMouseListener(new MouseAdapter() {
			int count = 0;

			@Override
			public void mouseClicked(MouseEvent e) {
				regPoints[count++] = e.getPoint();
				countLabel.setText("Chosen: " + count);
			}
		});

		final JFrame frame = new JFrame("Select registration points in order");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final JScrollPane scroller = new JScrollPane(image);
		frame.add(scroller);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent arg0) {
				setHomography(regPoints);
			}
		});

		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(e -> frame.dispose());

		JPanel controls = new JPanel();
		controls.add(countLabel);
		controls.add(nextButton);
		frame.add(controls, BorderLayout.SOUTH);

		frame.pack();
		frame.setSize(1024, 800);
		frame.setVisible(true);
	}

// ReleaseEventHandler methods
	private final ReleaseEventHandler releaseListener = new ReleaseEventHandler() {
		@Override
		public void errorOccured(int error) {
			logMessage("errorOccured(" + error + ")");
			disconnect();
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

// ActionListener methods
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == imageFormatCombo) {
			ImageFormat f =(ImageFormat)imageFormatCombo.getSelectedItem();
			source.setImageFormat(f.quality, f.size);

		} else if (e.getSource() == whiteBalanceCombo) {
			source.setWhiteBalance(whiteBalanceCombo.getSelectedIndex());
		}
	}

	private class ImageTransfer extends SwingWorker<BufferedImage, BufferedImage> implements ProgressMonitor {
		ReleaseImageInfo relinfo;
		BufferedImage rawImage;
		BufferedImage image = null;
		long shotTime;

		public ImageTransfer(ReleaseImageInfo ri, long millis) {
			relinfo = ri;
			shotTime = millis;
		}

		@Override
		protected BufferedImage doInBackground() {
			try {
				byte[] buffer = source.getReleasedDataToBuffer(relinfo, this, 3);
				//System.out.println(" Sequence = "+relinfo.sequence);
				//System.out.println(" Type = "+relinfo.type);
				//System.out.println(" Format = "+relinfo.format);
				//System.out.println(" Size = "+relinfo.size);
				//		System.out.println("Buffer size = "+buffer.length);
				ByteArrayInputStream is = new ByteArrayInputStream(buffer);
				rawImage = ImageIO.read(is);
				lensCorrect = new LensCorrection(-0.007715, 0.026731, 0.000000, rawImage.getWidth(), rawImage.getHeight());

				if (lensCorrection.isSelected() || applyRemap.isSelected()) {
					int width = rawImage.getWidth();
					int height = rawImage.getHeight();
					List<PointRemapper> corrections = new ArrayList<>();

					if (lensCorrection.isSelected()) {
						// TODO need to set these parameters based on the camera zoom
						corrections.add(lensCorrect);
					}
					if (applyRemap.isSelected()) {
						width = remappedWidth;
						height = remappedHeight;
						corrections.add(H);
					}

					image = PixelInterpolator.getImage(corrections, rawImage, width, height);

					// rewrite to buffer
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(image, "JPG", os);
					buffer = os.toByteArray();
					lastCorrectedImage = applyRemap.isSelected() ? buffer : null;

					if (overlayGenerator != null) overlayGenerator.updateOverlay(width, height);
				} else {
					image = rawImage;
				}

				String msg = Updater.updateURL(Updater.MAP_IMAGE, buffer);
				if (msg != null) logMessage(msg);
				return image;
			} catch (Exception e) {
				logMessage("Exception transferring image: "+e.getMessage());
				return null;
			}
		}

		@Override
		protected void done() {
			if (image != null) {
				//System.out.println("Got image size "+image.getWidth()+"x"+image.getHeight());
				CameraPanel.this.rawImage = rawImage;
				imagePanel.setImage(image);
				Date date = new Date(shotTime);
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				final String logMsg = format.format(date) + ": " +relinfo.size + " bytes (" + image.getWidth()+"x"+image.getHeight()+")";
				logMessage(logMsg);
			}
		}

//	 ProgressMonitor methods
		@Override
		public boolean setProgress(int progress, int status) {
			//System.out.println("Progress = "+progress+", status = "+status);
			setProgress(progress);
			return true;
		}
	}

	private class CaptureThread extends Thread {
		boolean running = false;
		boolean alive = true;
		long delay = 5000;
		long nextCaptureTime;

		public CaptureThread(long delay) {
			this.delay = delay;
		}

		public synchronized boolean isRunning() {
			return running;
		}

		public synchronized long getDelay() {
			return delay;
		}

		public synchronized void setDelay(long millis) {
			long currentTime = System.currentTimeMillis();
			if (nextCaptureTime > currentTime) {
				// a shot is due in the future - reschedule it based on the new delay
				nextCaptureTime = nextCaptureTime - delay + millis;
			}
			delay = millis;
			notify();
		}

		// TODO wait for capture to finish?
		public synchronized void stopCapture() {
			if (running) {
				logMessage("Stopping capture");
				running = false;
				notify();
			}
		}

		public synchronized void startCapture() {
			if (!running) {
				running = true;
				nextCaptureTime = System.currentTimeMillis();
				notify();
			}
		}

		public synchronized void reschedule() {
			nextCaptureTime = System.currentTimeMillis() + delay;
			notify();
		}

//		public synchronized void quitCapture() {
//			running = false;
//			alive = false;
//			notify();
//		}

		@Override
		public void run() {
			while (alive) {
				try {
					synchronized(this) {
						while (!running && alive) {
							//System.out.println("Not running - waiting until we are");
							wait();
						}
						if (!alive) break;	// time to quit
						if (!running) continue;	// something has woken us, but we're not running. wait some more

						long currentTime = System.currentTimeMillis();
						if (nextCaptureTime > currentTime) {
							//System.out.println("Waiting until next capture is due");
							wait(nextCaptureTime - currentTime);
						} else {
							nextCaptureTime = currentTime + delay;
							//System.out.println("Taking shot");
							capture();
						}
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}

} // class Capture

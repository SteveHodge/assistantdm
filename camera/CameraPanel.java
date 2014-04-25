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
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import javax.swing.WindowConstants;

import swing.ImagePanel;
import util.ModuleListener;
import util.ModuleRegistry;
import canon.cdsdk.Constants;
import canon.cdsdk.Enum;
import canon.cdsdk.ImageFormat;
import canon.cdsdk.MaximumZoom;
import digital_table.controller.DigitalTableModule;

// TODO calibrate buttons should be disabled when the camera has not taken a shot
// TODO may be fixed: controls stop responding if you hit stop during a transfer
@SuppressWarnings("serial")
public class CameraPanel extends JPanel implements ActionListener {
	private Camera camera;

	private long defaultDelay = 60000;
	private CaptureThread timer;
	private long lastShot = System.currentTimeMillis();
	private boolean capturing;		// true while we're waiting for an image to be transferred
	private boolean captureASAP;	// true if we want a capture to occur as soon as the current image has completed transferring

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

	public CameraPanel(Camera c) {
		camera = c;

		timer = new CaptureThread(defaultDelay);

		createAndShowGUI();
		camera.addCameraModuleListener(cameraListener);
		try {
			// if the connect succeeds we'll be notified via the cameraListener
			camera.connect();
		} catch (CameraNotFoundException ex) {
			logMessage("No camera was found");
		} catch (Exception ex) {
			logMessage("Exception connecting to camera: " + ex.getMessage());
			//ex.printStackTrace();
		}
		ModuleRegistry.addModuleListener(DigitalTableModule.class, new ModuleListener<DigitalTableModule>() {
			@Override
			public void moduleRegistered(DigitalTableModule module) {
//				System.out.println("DigitalTable controller found");
				calibrateButton.setVisible(true);
			}

			@Override
			public void moduleRemoved(DigitalTableModule module) {
				calibrateButton.setVisible(false);
//				System.out.println("DigitalTable controller lost");
			}
		});
	}

	private final CameraModuleListener cameraListener = new CameraModuleListener() {
		@Override
		public void imageCaptured(BufferedImage image, long size, Exception ex, String updateMsg) {
			if (updateMsg != null) logMessage(updateMsg);

			if (ex != null) {
				logMessage("Exception transferring image: " + ex.getMessage());

			} else {
				imagePanel.setImage(image);
				Date date = new Date(lastShot);
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				final String logMsg = format.format(date) + ": " + size + " bytes (" + image.getWidth() + "x" + image.getHeight() + ")";
				logMessage(logMsg);
				manualButton.setEnabled(true);
			}

			capturing = false;
			if (captureASAP) capture();
		}

		@Override
		public void cameraDisconnected() {
			timer.stopCapture();	// TODO if this does wait for capture to finish, should we?
			setControls(false);
			startStop.setText("Start");
		}

		@Override
		public void cameraConnected(String name) {
			logMessage("Found " + name);
			getCameraOptions();
			setControls(true);
		}

		@Override
		public void cameraError(int error) {
			logMessage("errorOccured(" + error + ")");
		}

		@Override
		public void homographyChanged(Exception ex) {
			if (ex != null) {
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(CameraPanel.this,
							"Autocalibrate failed to find the calibration marks.\nPlease manually calibrate.",
							"Autocalibrate Failed",
							JOptionPane.ERROR_MESSAGE);
				});
			} else {
				applyRemap.setEnabled(true);
				applyRemap.setSelected(true);

				imagePanel.setImage(camera.getLastImage(true, true));
			}
		}
	};

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
		connectButton.addActionListener(e -> {
			try {
				// if the connect succeeds we'll be notified via the cameraListener
				camera.connect();
			} catch (CameraNotFoundException ex) {
				logMessage("No camera was found");
				JOptionPane.showOptionDialog(null, "No camera was found", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
			} catch (Exception ex) {
				logMessage("Exception connecting to camera: " + ex.getMessage());
				//ex.printStackTrace();
			}
		});
		calibrateButton = new JButton("Autocalibrate");
		calibrateButton.addActionListener(e -> {
			try {
				camera.autoCalibrate();
			} catch (Exception ex) {
				logMessage("Exception taking calibration shot: " + ex.getMessage());
			}
		});
		calibrateButton.setVisible(false);
		calibrateButton.setEnabled(false);
		manualButton = new JButton("Manually Calibrate");
		manualButton.addActionListener(e -> manualCalibrate());
		manualButton.setEnabled(false);
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
		frequencyField.setValue(new Integer((int) (defaultDelay / 1000)));
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
				camera.source.setZoomPosition(slider.getValue());
				// set AF lock if it should be set (changing the zoom will unset it)
				if (focusLockCheck.isSelected()) camera.source.setAFLock(true);
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
				camera.source.setAFLock(focusLockCheck.isSelected());
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

		lensCorrection = new JCheckBox("Apply Lens Correction");
		lensCorrection.setSelected(true);
		lensCorrection.addItemListener(e -> {
			imagePanel.setImage(camera.getLastImage(lensCorrection.isSelected(), applyRemap.isSelected()));
		});
		controls.add(lensCorrection);

		applyRemap = new JCheckBox("Remap Image");
		applyRemap.setEnabled(false);
		applyRemap.addItemListener(e -> {
			imagePanel.setImage(camera.getLastImage(lensCorrection.isSelected(), applyRemap.isSelected()));
		});
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

	private void capture() {
		if (capturing) {
			captureASAP = true;		// request capture as soon as this one is complete
			return;
		}
		captureASAP = false;
		capturing = true;
		double secs = (System.currentTimeMillis() - lastShot) / 1000.0;
		lastShot = System.currentTimeMillis();
		logMessage("" + secs + "s since last shot");
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setValue(0);
		try {
			camera.capture(lensCorrection.isSelected(), applyRemap.isSelected(), evt -> {
				if ("progress".equals(evt.getPropertyName())) {
					progressBar.setValue((Integer) evt.getNewValue());
				}
			});
		} catch (CameraNotConnectedException e) {
			logMessage(e.getMessage());
//			e.printStackTrace();
		}
	}

	private void setControls(boolean active) {
		capture.setEnabled(active);
		startStop.setEnabled(active);
		imageFormatCombo.setEnabled(active);
		zoomSlider.setEnabled(active);
		focusLockCheck.setEnabled(active);
		frequencyField.setEnabled(active);
		whiteBalanceCombo.setEnabled(active);
		manualButton.setEnabled(active && camera.getLastImage(false, false) != null);
		calibrateButton.setEnabled(active);

		connectButton.setEnabled(!active);
	}


	private void getCameraOptions() {
		// Zooms
		MaximumZoom max = camera.source.getMaximumZoom();
		zoomSlider.setMaximum(max.maximumZoom);
		zoomSlider.setMinorTickSpacing(1);
		zoomSlider.setMajorTickSpacing(max.maximumOpticalZoom);
		zoomSlider.setValue(camera.source.getZoomPosition());

		// Image formats
		// removing all existing items will trigger an ActionEvent which would cause the camera option to be updated (to an invalid
		// option) so we temporarily suspend ActionEvent processing while we rebuild the list
		imageFormatCombo.removeActionListener(this);
		imageFormatCombo.removeAllItems();
		Enum<ImageFormat> formats = camera.source.getImageFormatEnum();
		boolean foundDefault = false;
		for (int i=0; i<formats.getCount(); i++) {
			ImageFormat f = formats.next();
			if (f.quality == defaultImageQuality && f.size == defaultImageSize) foundDefault = true;
			if (f.quality != Constants.COMP_QUALITY_RAW)
				imageFormatCombo.addItem(new ImageFormatOption(f));
		}
		formats.release();
		imageFormatCombo.addActionListener(this);
		if (foundDefault) camera.source.setImageFormat(defaultImageQuality, defaultImageSize);
		imageFormatCombo.setSelectedItem(new ImageFormatOption(camera.source.getImageFormat()));

		// White balance
		// removing all existing items will trigger an ActionEvent which would cause the camera option to be updated (to an invalid
		// option) so we temporarily suspend ActionEvent processing while we rebuild the list
		whiteBalanceCombo.removeActionListener(this);
		whiteBalanceCombo.removeAllItems();
		Enum<Integer> modes = camera.source.getWhiteBalanceEnum();
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
		if (foundDefault) camera.source.setWhiteBalance(defaultWhiteBalance);
		whiteBalanceCombo.setSelectedIndex(camera.source.getWhiteBalance());
	}

	private void manualCalibrate() {
		final BufferedImage lensCorrected = camera.getLastImage(true, false);

		if (lensCorrected == null) {
			JOptionPane.showMessageDialog(this,
					"Need to take a photo before calibration can occur",
					"Calibration error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		final Point[] regPoints = new Point[4];

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
				camera.setHomography(regPoints);
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

// ActionListener methods
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == imageFormatCombo) {
			ImageFormat f =(ImageFormat)imageFormatCombo.getSelectedItem();
			camera.source.setImageFormat(f.quality, f.size);

		} else if (e.getSource() == whiteBalanceCombo) {
			camera.source.setWhiteBalance(whiteBalanceCombo.getSelectedIndex());
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
					synchronized (this) {
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
							// TODO might need to check that we are still connected
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
}

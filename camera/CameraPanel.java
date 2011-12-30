package camera;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import swing.ImagePanel;
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


// TODO may be fixed: controls stop responding if you hit stop during a transfer
@SuppressWarnings("serial")
public class CameraPanel extends JPanel implements ReleaseEventHandler, ActionListener, ChangeListener, ItemListener, PropertyChangeListener {
	static final String DESTINATION = "m:\\webcam\\ftp\\images\\capture2.jpg";

	long defaultDelay = 10000;
	int defaultImageSize = Constants.IMAGE_SIZE_MEDIUM2;
	int defaultImageQuality = Constants.COMP_QUALITY_NORMAL;
	int defaultWhiteBalance = 3;

	ImagePanel imagePanel;
	JProgressBar progressBar;
	JButton capture, startStop;
	JComboBox imageFormatCombo;
	JSlider zoomSlider;
	JCheckBox focusLockCheck;
	JFormattedTextField frequencyField;
	JPanel controls;
	JButton connectButton;
	JComboBox whiteBalanceCombo;
	JTextArea logArea;

	static final String[] WHITE_BALANCE_NAMES = {"Auto","Daylight","Cloudy","Tungsten","Fluorescent","Flash"};

	CaptureThread timer;
	long lastShot = System.currentTimeMillis();

	CDSDK sdk;
	Source source;

public CameraPanel() {
	sdk = new CDSDK();
	sdk.open();
	timer = new CaptureThread(defaultDelay);
	createAndShowGUI();
	connect(true);
}

public void createAndShowGUI() {
	setLayout(new BorderLayout());

	imagePanel = new ImagePanel(null);
	imagePanel.setAllowEnargements(false);
//	imagePanel.setBackground(Color.RED);
	add(imagePanel,BorderLayout.CENTER);

	controls = new JPanel();
	controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));

	connectButton = new JButton("Connect");
	connectButton.addActionListener(this);
	controls.add(connectButton);
	
	capture = new JButton("Capture");
	capture.addActionListener(this);
	startStop = new JButton("Start");
	startStop.addActionListener(this);
	JPanel buttons = new JPanel();
	buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
	buttons.add(startStop);
	buttons.add(Box.createRigidArea(new Dimension(5,0)));
	buttons.add(capture);
	controls.add(buttons);

	frequencyField = new JFormattedTextField();
	frequencyField.setValue(new Integer((int) (defaultDelay/1000)));
	frequencyField.setColumns(5);
	frequencyField.addPropertyChangeListener("value", this);
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
	zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
	zoomSlider.setPaintTicks(true);
	zoomSlider.addChangeListener(this);
	controls.add(zoomSlider);

	controls.add(new JLabel("Size & Quality"));
	imageFormatCombo  = new JComboBox();
	Dimension prefSize = imageFormatCombo.getPreferredSize();
	prefSize.width = 300;
	imageFormatCombo.setMaximumSize(prefSize);
	imageFormatCombo.addActionListener(this);
	controls.add(imageFormatCombo);

	focusLockCheck = new JCheckBox("AF Lock");
	focusLockCheck.addItemListener(this);
	controls.add(focusLockCheck);

	controls.add(new JLabel("White Balance"));
	whiteBalanceCombo = new JComboBox();
	prefSize = whiteBalanceCombo.getPreferredSize();
	prefSize.width = 300;
	whiteBalanceCombo.setMaximumSize(prefSize);
	whiteBalanceCombo.addActionListener(this);
	controls.add(whiteBalanceCombo);

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

public void logMessage(final String logMsg) {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
			logArea.append(logMsg+"\n");
		}
	});
}

public void setControls(boolean active) {
	capture.setEnabled(active);
	startStop.setEnabled(active);
	imageFormatCombo.setEnabled(active);
	zoomSlider.setEnabled(active);
	focusLockCheck.setEnabled(active);
	frequencyField.setEnabled(active);
	whiteBalanceCombo.setEnabled(active);

	connectButton.setEnabled(!active);
}

public void connect() {
	connect(false);
}

public void connect(boolean noWarning) {
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

			source.enterReleaseControl(this);
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

public void getCameraOptions() {
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

public void capture() {
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
	    		transfer.addPropertyChangeListener(
	    			     new PropertyChangeListener() {
	    			         public  void propertyChange(PropertyChangeEvent evt) {
	    			             if ("progress".equals(evt.getPropertyName())) {
	    			                 progressBar.setValue((Integer)evt.getNewValue());
	    			             }
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

// ReleaseEventHandler methods
public void errorOccured(int error) {
	logMessage("errorOccured("+error+")");
	disconnect();
}

public void parametersChanged() {
	//System.out.println("parametersChanged()");
}

public void releaseButtonPressed() {
	//System.out.println("releaseButtonPressed()");
}

public void releaseComplete(int numData) {
	//System.out.println("releaseComplete("+numData+")");
}

public void releaseStart() {
	//System.out.println("releaseStart()");
}

public void unknownEvent(int eventID) {
	//System.out.println("unknownEvent("+eventID+")");
}

public void viewFinderDisabled() {
	//System.out.println("viewFinderDisabled()");
}

public void viewFinderEnabled() {
	//System.out.println("viewFinderEnabled()");
}

public void rotationAngleChanged(int angle) {
	//System.out.println("rotationAngleChanged("+angle+")");
}

// ActionListener methods
public void actionPerformed(ActionEvent e) {
	if (e.getSource() == capture) {
		capture();
		if (timer.isRunning()) {
			timer.reschedule();
		}
	} else if (e.getSource() == startStop) {
		if (timer.isRunning()) {
			timer.stopCapture();
			startStop.setText("Start");
		} else {
			timer.startCapture();
			startStop.setText("Stop");
		}
	} else if (e.getSource() == imageFormatCombo) {
		ImageFormat f =(ImageFormat)imageFormatCombo.getSelectedItem();
		source.setImageFormat(f.quality, f.size);
	} else if (e.getSource() == whiteBalanceCombo) {
		source.setWhiteBalance(whiteBalanceCombo.getSelectedIndex());
	} else if (e.getSource() == connectButton) {
		connect();
	}
}

// PropertyChangeListener methods
public void propertyChange(PropertyChangeEvent e) {
	Object source = e.getSource();
	if (source == frequencyField) {
		int freq = ((Number)frequencyField.getValue()).intValue();
		freq *= 1000;
		if (timer.getDelay() != freq) {
			timer.setDelay(freq);
		}
	}
}

public class ImageTransfer extends SwingWorker<BufferedImage,BufferedImage> implements ProgressMonitor {
	ReleaseImageInfo relinfo;
	BufferedImage image = null;
	long shotTime;

	public ImageTransfer(ReleaseImageInfo ri, long millis) {
		relinfo = ri;
		shotTime = millis;
	}

	protected BufferedImage doInBackground() {
		try {
			byte[] buffer = source.getReleasedDataToBuffer(relinfo, this, 3);
	    	//System.out.println(" Sequence = "+relinfo.sequence);
	    	//System.out.println(" Type = "+relinfo.type);
	    	//System.out.println(" Format = "+relinfo.format);
	    	//System.out.println(" Size = "+relinfo.size);
	//		System.out.println("Buffer size = "+buffer.length);
			ByteArrayInputStream is = new ByteArrayInputStream(buffer);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(DESTINATION);
				out.write(buffer);
			} catch (FileNotFoundException e) {
				logMessage("Couldn't open "+DESTINATION+": "+e.getMessage());
			} catch (IOException e) {
				logMessage("Couldn't save image to "+DESTINATION+": "+e.getMessage());
			} finally {
				if (out != null) out.close();
			}
			image = ImageIO.read(is);
			return image;
		} catch (Exception e) {
			logMessage("Exception transferring image: "+e.getMessage());
			return null;
		}
	}

	protected void done() {
		if (image != null) {
			//System.out.println("Got image size "+image.getWidth()+"x"+image.getHeight());
			imagePanel.setImage(image);
			Date date = new Date(shotTime);
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			final String logMsg = format.format(date) + ": " +relinfo.size + " bytes (" + image.getWidth()+"x"+image.getHeight()+")";
			logMessage(logMsg);
		}
	}

//	 ProgressMonitor methods
	public boolean setProgress(int progress, int status) {
		//System.out.println("Progress = "+progress+", status = "+status);
		setProgress(progress);
		return true;
	}
}

// ChangeListener methods
public void stateChanged(ChangeEvent e) {
    JSlider slider = (JSlider)e.getSource();
    if (!slider.getValueIsAdjusting()) {
    	//System.out.println("Setting zoom to "+slider.getValue());
    	source.setZoomPosition(slider.getValue());
    	// set AF lock if it should be set (changing the zoom will unset it) 
    	if (focusLockCheck.isSelected()) source.setAFLock(true);
    }
}

// ItemListener methods
public void itemStateChanged(ItemEvent e) {
	if (e.getSource() == focusLockCheck) {
		//System.out.println("Autofocus locked? "+focusLockCheck.isSelected());
		source.setAFLock(focusLockCheck.isSelected());
	}
}

protected class CaptureThread extends Thread {
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

	public synchronized void quitCapture() {
		running = false;
		alive = false;
		notify();
	}

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

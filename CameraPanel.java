import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


@SuppressWarnings("serial")
public class CameraPanel extends JPanel implements ImageScanListener {
	//static final String destination = "c:\\temp\\2\\image.jpg";
	//static final String sourcedir = "c:\\temp\\1";
	static final String destination = "m:\\webcam\\capture2.jpg";
	static final String sourcedir = "c:\\documents and settings\\stephen\\my documents";
	static final String sourceprefix = "Capture";

	Thread cameraThread;
	JTextArea area;
	ImagePanel imagePanel;

	public CameraPanel() {
		area = new JTextArea(20,40);
		JScrollPane scrollPane = new JScrollPane(area); 
		area.setEditable(false);

		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(destination));
		} catch (IOException e) {
			System.err.println("Can't read image file "+destination);
		}
		imagePanel = new ImagePanel(image);
		JScrollPane scrollPane2 = new JScrollPane(imagePanel); 

		//Create a split pane with the two scroll panes in it.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scrollPane, scrollPane2);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(350);

		//Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(100, 50);
		scrollPane.setMinimumSize(minimumSize);
		scrollPane.setPreferredSize(minimumSize);
		scrollPane2.setMinimumSize(minimumSize);
		scrollPane2.setPreferredSize(minimumSize);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);

		CameraMonitor cameraMonitor = new CameraMonitor(sourcedir, sourceprefix, destination, 2000);
		cameraMonitor.addImageScanListener(this);
		cameraThread = new Thread(cameraMonitor);
		cameraThread.start();
	}

	public void imageFound(final ImageScanEvent e) {
		try {
			final BufferedImage image = ImageIO.read(e.getImageFile());
			Date mod = new Date(e.getImageFile().lastModified());
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			final String logMsg = format.format(mod) + " - " + e.getImageFile().getName()+"\n";

			// CameraMonitor has it's own thread but we need to make these changes from the
			// AWT event thread
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					area.append(logMsg);
					imagePanel.setImage(image);
				}
			});
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
}

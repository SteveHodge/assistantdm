import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import party.Party;

@SuppressWarnings("serial")
public class AssistantDM extends javax.swing.JFrame implements ActionListener {
	JMenuBar menuBar;
	JMenu fileMenu;
	JMenuItem saveItem;
	JMenuItem openItem;

	Party party;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AssistantDM inst = new AssistantDM();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public AssistantDM() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setTitle("Assistant DM");

		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		saveItem = new JMenuItem("Save", KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveItem.addActionListener(this);
		openItem = new JMenuItem("Open...", KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openItem.addActionListener(this);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		setJMenuBar(menuBar);

		JTabbedPane tabbedPane = new JTabbedPane();

		party = Party.parseXML("party.xml");

		JComponent panel; 
		panel = new CombatPanel(party);
		tabbedPane.addTab("Combat", null, panel, "Initiative and Combat");

		panel = new RollsPanel(party);
		tabbedPane.addTab("Rolls", null, panel, "Skills and Saves");

		panel = new MagicGeneratorPanel();
		tabbedPane.addTab("Random Magic", null, panel, "Generate Random Magic Items");

		panel = new CameraPanel();
		tabbedPane.addTab("Camera", null, panel, "Camera Latest Image and File Copy");

		getContentPane().add(tabbedPane);
		pack();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveItem) {
			System.out.println("Save");

	        FileWriter outputStream = null;

			try {
				outputStream = new FileWriter("party.xml");
				outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				outputStream.write(System.getProperty("line.separator"));
				outputStream.write(System.getProperty("line.separator"));
				outputStream.write(party.getXML("", "    "));
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}

		} else if(e.getSource() == openItem) {
			System.out.println("Open");
		} else {
			System.err.println("ActionEvent from unknown source: "+e);
		}
	}
}

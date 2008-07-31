import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

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
public class AssistantDM extends javax.swing.JFrame implements ActionListener, WindowListener {
	JMenuBar menuBar;
	JMenu fileMenu;
	JMenuItem saveItem;
	JMenuItem openItem;
	ShoppingPanel shopPanel;

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
		setTitle("Assistant DM");
		addWindowListener(this);

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

		List<Shop> shops = ShoppingPanel.parseShopsXML("shops.xml");
		if (shops.size() > 0) {
			shopPanel = new ShoppingPanel(shops);
			System.out.println("Loaded shops from XML file");
		}
		if (shopPanel == null) {
			shopPanel = new ShoppingPanel();
			System.out.println("Setup shops from scratch");
		}
		tabbedPane.addTab("Shops", null, shopPanel, "Magic Item Shops");

		panel = new MagicGeneratorPanel();
		tabbedPane.addTab("Random Magic", null, panel, "Generate Random Magic Items");

		panel = new CameraPanel();
		tabbedPane.addTab("Camera", null, panel, "Camera Latest Image and File Copy");

		getContentPane().add(tabbedPane);
		pack();
	}

	public void saveParty(String filename) {
        FileWriter outputStream = null;

		try {
			outputStream = new FileWriter(filename);
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
	}

	public ShoppingPanel loadShops() {
		String filename = "shops.ser";
		FileInputStream fis = null;
		ObjectInputStream in = null;
		ShoppingPanel panel = null;
		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			panel = new ShoppingPanel(in); 
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("No shops file found");
			panel = null;
		} catch(IOException ex) {
			ex.printStackTrace();
			panel = null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			panel = null;
		}
		return panel;
	}

	public void saveShops() {
		String filename = "shops.ser";
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			shopPanel.saveShops(out);
			out.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveItem) {
			System.out.println("Save");
			saveParty("party.xml");

		} else if(e.getSource() == openItem) {
			System.out.println("Open");
		} else {
			System.err.println("ActionEvent from unknown source: "+e);
		}
	}

	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	public void windowClosing(WindowEvent e) {
		System.out.println("Exiting");
		saveParty("party_autosave.xml");
		shopPanel.writeShopsXML(shopPanel.shops, "shops.xml");
		System.exit(0);
	}
}

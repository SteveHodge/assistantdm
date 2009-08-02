import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import magicitems.MagicGeneratorPanel;
import magicitems.Shop;
import magicitems.ShoppingPanel;
import monsters.MonstersPanel;

import camera.CameraPanel;

import party.Character;
import party.Party;
import ui.PartyPanel;
import ui.RollsPanel;
import ui.SelectDiffsDialog;
import ui.UpdateCharacterDialog;

//TODO would be nice to have a library of creatures that could be selected for the combat panel
//TODO allow ac components that are not currently included. will probably need to allow multiples of each component 
//TODO refactor classes that should be in ui package
//TODO most/all character properties should all "override" values
//TODO saves and skills need base value and bonus value separated
@SuppressWarnings("serial")
public class AssistantDM extends javax.swing.JFrame implements ActionListener, WindowListener {
	JMenuBar menuBar;
	JMenu fileMenu;
	JMenuItem saveItem;
	JMenuItem saveAsItem;
	JMenuItem openItem;
	JMenuItem updateItem;
	ShoppingPanel shopPanel;
	CameraPanel cameraPanel;
	JTabbedPane tabbedPane;

	Party party;
	File file;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
	    }

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AssistantDM inst = new AssistantDM();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public AssistantDM() {
		file = new File("party.xml");

		setTitle("Assistant DM");
		addWindowListener(this);

		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		saveItem = new JMenuItem("Save", KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveItem.addActionListener(this);
		saveAsItem = new JMenuItem("Save As...");
		saveAsItem.addActionListener(this);
		openItem = new JMenuItem("Open...", KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openItem.addActionListener(this);
		updateItem = new JMenuItem("Update Party...");
		updateItem.addActionListener(this);
		fileMenu.add(openItem);
		fileMenu.add(updateItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		setJMenuBar(menuBar);

		tabbedPane = new JTabbedPane();

		party = Party.parseXML(file);

		JComponent panel; 
		panel = new CombatPanel(party);
		tabbedPane.addTab("Combat", null, panel, "Initiative and Combat");

		panel = new RollsPanel(party);
		tabbedPane.addTab("Rolls", null, panel, "Skills and Saves");

		panel = new PartyPanel(party);
		tabbedPane.addTab("Party", null, panel, "Character Details");

		panel = new MonstersPanel();
		tabbedPane.addTab("Monsters", null, panel, "Monsters");

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

		cameraPanel = new CameraPanel();
		tabbedPane.addTab("Camera", null, cameraPanel, "Camera Remote Image Capture");

		getContentPane().add(tabbedPane);
		pack();
	}

	public void updateParty() {
		// Get file
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
		fc.setCurrentDirectory(file);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		File newfile = fc.getSelectedFile();
		System.out.println("Updating party from "+newfile.getAbsolutePath());

		// Load file into new party
		Party newparty = Party.parseXML(newfile);

		// Match characters
		UpdateCharacterDialog dialog = new UpdateCharacterDialog(this,party,newparty);
		dialog.setVisible(true);
		if (dialog.isCancelled()) return;

       	// Shows diffs and accept
		for (Character inChar : newparty) {
			Character oldChar = dialog.getSelectedCharacter(inChar);
			if (oldChar == null) {
				System.out.println("No character selected for "+inChar.getName());
			} else {
				System.out.println("Selected character for "+inChar.getName()+" = "+oldChar.getName());
				SelectDiffsDialog diffsDialog = new SelectDiffsDialog(this,oldChar,inChar);
				diffsDialog.setVisible(true);
				if (!diffsDialog.isCancelled()) {
					List<String> updates = diffsDialog.getSelectedDiffs();
					for (String prop : updates) {
						oldChar.setProperty(prop,inChar.getProperty(prop));
					}
				}
			}
		}
	}

	// TODO rebuilding the ui is ugly - should be able to just replace the party
	// TODO open should ask to save modified parties
	public void openParty() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
		fc.setCurrentDirectory(file);
		fc.setSelectedFile(file);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		file = fc.getSelectedFile();
       	System.out.println("Opening "+file.getAbsolutePath());

       	party = Party.parseXML(file);

       	int selected = tabbedPane.getSelectedIndex();

       	tabbedPane.removeTabAt(0);	// combat
		tabbedPane.removeTabAt(0);	// rolls
		tabbedPane.removeTabAt(0);	// party

		JComponent panel; 
		panel = new CombatPanel(party);
		tabbedPane.insertTab("Combat", null, panel, "Initiative and Combat", 0);

		panel = new RollsPanel(party);
		tabbedPane.insertTab("Rolls", null, panel, "Skills and Saves", 1);

		panel = new PartyPanel(party);
		tabbedPane.insertTab("Party", null, panel, "Character Details", 2);

       	tabbedPane.setSelectedIndex(selected);
	}

	// TODO saveAs should probably ask to overwrite and forget about backups
	public void saveAsParty() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
		fc.setCurrentDirectory(file);
		fc.setSelectedFile(file);
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
        	System.out.println("Saving to "+file.getAbsolutePath());
        	saveParty(file);
		}
	}

	public void saveParty(File f) {
        FileWriter outputStream = null;

        // check if file exists
        if (f.exists()) {
            String filename = f.getName();
        	String backName;
        	if (filename.contains(".")) {
        		backName = filename.substring(0,filename.lastIndexOf('.'));
        		backName += "_backup";
        		backName += filename.substring(filename.lastIndexOf('.'));
        	} else {
        		backName = filename + "_backup";
        	}
        	File back = new File(f.getParent(),backName);
        	System.out.println("Writing backup to: "+back.getAbsolutePath());
        	if (back.exists()) back.delete();
        	File newF = f;
        	f.renameTo(back);
        	f = newF;
        }

        try {
			outputStream = new FileWriter(f);
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
			if (file != null) saveParty(file);
			else saveAsParty();

		} else if(e.getSource() == saveAsItem) {
			saveAsParty();

		} else if(e.getSource() == updateItem) {
			updateParty();

		} else if(e.getSource() == openItem) {
			openParty();

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
		saveParty(file);
		shopPanel.writeShopsXML("shops.xml");
		cameraPanel.disconnect();
		System.exit(0);
	}
}

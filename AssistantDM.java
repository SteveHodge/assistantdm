import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import camera.Camera;
import camera.CameraPanel;
import combat.CombatPanel;
import combat.InitiativeListModel;
import combat.MonsterCombatEntry;
import digital_table.controller.DigitalTableController;
import gamesystem.RuleSet;
import led_control.LEDController;
import led_control.LEDControllerPanel;
import magicitems.MagicGeneratorPanel;
import magicitems.Shop;
import magicitems.ShoppingPanel;
import monsters.EncounterDialog;
import monsters.MonstersPanel;
import party.Character;
import party.CharacterLibrary;
import party.Party;
import swing.JTableWithToolTips;
import ui.PartyPanel;
import ui.RestDialog;
import ui.RollsPanel;
import ui.SelectPartyDialog;
import ui.XPEntryDialog;
import util.WebsiteMonitor;
import util.ModuleRegistry;
import util.Updater;
import util.XMLUtils;

@SuppressWarnings("serial")
public class AssistantDM extends javax.swing.JFrame implements ActionListener {
	JMenuBar menuBar;
	JMenu fileMenu, encounterMenu, partyMenu;
	JMenuItem saveItem, saveAsItem, openItem, updateItem, tableItem;
	//	JMenuItem tableItem;
	JMenuItem selectPartyItem, xpItem, xpHistoryItem, newCharacterItem;
	ShoppingPanel shopPanel;
	CombatPanel combatPanel;
	CameraPanel cameraPanel;
	JTabbedPane tabbedPane;
	DigitalTableController controller;
	WebsiteMonitor dmmon;
	LEDController ledController;
	LEDControllerPanel ledPanel;

	Party party;
	File file;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		SwingUtilities.invokeLater(() -> {
			AssistantDM inst = new AssistantDM();
			inst.setLocationRelativeTo(null);
			inst.setVisible(true);
		});
	}

	public AssistantDM() {
		ModuleRegistry.parseConfigFile(new File("config.xml"));

		file = new File("party.xml");

		setTitle("Assistant DM - "+file.getName());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		// create modules
		RuleSet.parseXML(new File("rulesets/ptolus.xml"));
		party = new Party();

		combatPanel = new CombatPanel(party);

		ledController = new LEDController();
		ledPanel = new LEDControllerPanel(ledController, party);

		Document dom = Party.parseXML(file);
		party.parseDOM(dom);

		try {
			Camera camera = new Camera();
			cameraPanel = new CameraPanel(camera);
		} catch (UnsatisfiedLinkError e) {
			System.out.println("Caught error: "+e);
		} catch (NoClassDefFoundError e) {
			System.out.println("Caught error: " + e);
		}

		controller = new DigitalTableController() {
			@Override
			protected void quit() {
				close();
			}
		};

		dmmon = new WebsiteMonitor(party, controller);

		// TODO convert to Actions where sensible
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
		tableItem = new JMenuItem("Digital tabletop controller...", KeyEvent.VK_T);
		tableItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		tableItem.addActionListener(this);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(tableItem);
		fileMenu.add(new JMenuItem(new AbstractAction("Exit") {@Override
			public void actionPerformed(ActionEvent arg0) {exit();}}));

		encounterMenu = new JMenu("Encounters");
		encounterMenu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(encounterMenu);
		encounterMenu.add(newEncounterAction);
		encounterMenu.add(openEncounterAction);
		encounterMenu.add(openCombatAsEncounterAction);

		partyMenu = new JMenu("Party");
		partyMenu.setMnemonic(KeyEvent.VK_P);
		menuBar.add(partyMenu);
		newCharacterItem = new JMenuItem("New Character...");
		newCharacterItem.addActionListener(this);
		selectPartyItem = new JMenuItem("Select Party...");
		selectPartyItem.addActionListener(this);
		xpItem = new JMenuItem("Calculate XP...");
		xpItem.addActionListener(this);
		xpHistoryItem = new JMenuItem("XP History...");
		xpHistoryItem.addActionListener(this);
		updateItem = new JMenuItem("Import Characters...");
		updateItem.addActionListener(this);
		partyMenu.add(newCharacterItem);
		partyMenu.add(updateItem);
		partyMenu.add(selectPartyItem);
		partyMenu.add(xpItem);
		partyMenu.add(xpHistoryItem);

		JMenuItem restItem = new JMenuItem("Rest / Reset Sanity...");
		restItem.addActionListener(e -> {
			RestDialog dialog = new RestDialog(this, party);
			dialog.setVisible(true);
		});
		partyMenu.add(restItem);

		setJMenuBar(menuBar);

		tabbedPane = new JTabbedPane();

		JComponent panel;

		tabbedPane.addTab("Combat", null, combatPanel, "Initiative and Combat");

		panel = new PartyPanel(party);
		tabbedPane.addTab("Party", null, panel, "Character Details");

		panel = new RollsPanel(party);
		tabbedPane.addTab("Rolls", null, panel, "Skills and Saves");

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

		if (cameraPanel != null) tabbedPane.addTab("Camera", null, cameraPanel, "Camera Remote Image Capture");

		if (ledPanel != null) tabbedPane.addTab("LED Control", null, ledPanel, "LED Control");

		getContentPane().add(tabbedPane);

		pack();

		// resize the frame back to the size of the default monitor if it is larger
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle b = getBounds();
		Dimension newSize = b.getSize();
		if (b.width > screen.width) newSize.width = screen.width;
		if (b.height > screen.height) newSize.height = screen.height;
		if (newSize.width != b.width || newSize.height != b.height) {
			System.out.println("Bounds = "+b);
			System.out.println("Resizing to "+newSize);
			setSize(newSize);
		}
	}

	private Action newEncounterAction = new AbstractAction("New encounter...") {
		@Override
		public void actionPerformed(ActionEvent e) {
			new EncounterDialog();
		}
	};

	private Action openEncounterAction = new AbstractAction("Open encounter...") {
		@Override
		public void actionPerformed(ActionEvent e) {
			EncounterDialog d = new EncounterDialog(false);
			d.load(AssistantDM.this);
		}
	};

	private Action openCombatAsEncounterAction = new AbstractAction("Open monsters in encounter...") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			EncounterDialog e = new EncounterDialog();
			InitiativeListModel ilm = combatPanel.getInitiativeListModel();
			for (int i = 0; i < ilm.getSize(); i++) {
				if (ilm.getElementAt(i) instanceof MonsterCombatEntry) {
					MonsterCombatEntry p = (MonsterCombatEntry) ilm.getElementAt(i);
					if (!p.isBlank() && p.getMonster() != null) e.addMonster(p.getMonster());
				}
			}
		}
	};

	// WISH provide checkbox on dialog to add new character to party (default:checked)
	public void newCharacter() {
		String s = JOptionPane.showInputDialog(this,"Enter the new character's name:","Add New Character",JOptionPane.PLAIN_MESSAGE);
		if ((s != null) && (s.length() > 0)) {
			Character newc = new Character(s);
			CharacterLibrary.add(newc);
			party.add(newc);	// by default we add the new character to the party
		}
	}

	public void selectParty() {
		SelectPartyDialog partyDialog = new SelectPartyDialog(this,party);
		partyDialog.setVisible(true);
		if (!partyDialog.isCancelled()) {
			List<Character> newParty = partyDialog.getSelectedCharacters();
			Set<Character> changes = new HashSet<>();
			for (Character c : party) {
				if (!newParty.contains(c)) changes.add(c);
			}
			for (Character c : changes) party.remove(c);
			changes = new HashSet<>();
			for (Character c : newParty) {
				if (!party.contains(c)) changes.add(c);
			}
			for (Character c : changes) party.add(c);
		}
	}

	public void calculateXP() {
		XPEntryDialog dialog = new XPEntryDialog(this, party);
		dialog.setVisible(true);
		if (!dialog.isCancelled()) {
			dialog.applyXPEarned();
		}
	}

	public void updateParty() {
		System.err.println("Implementation needs fixing");

//		// Get file
//		JFileChooser fc = new JFileChooser();
//		fc.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
//		fc.setCurrentDirectory(file);
//		int returnVal = fc.showOpenDialog(this);
//		if (returnVal != JFileChooser.APPROVE_OPTION) return;
//
//		File newfile = fc.getSelectedFile();
//		System.out.println("Updating party from "+newfile.getAbsolutePath());
//
//		// Load file into new party
//		Party newparty = Party.parseXML(newfile,true);
//
//		// Match characters
//		UpdateCharacterDialog dialog = new UpdateCharacterDialog(this,party,newparty);
//		dialog.setVisible(true);
//		if (dialog.isCancelled()) return;
//
//		// Shows diffs and accept
//		for (Character inChar : newparty) {
//			Character oldChar = dialog.getSelectedCharacter(inChar);
//			if (oldChar == null) {
//				//System.out.println("No character selected for "+inChar.getName());
//				int n = JOptionPane.showConfirmDialog(this,"Do you want to add "+inChar.getName()+"?","Add New Character",JOptionPane.YES_NO_OPTION);
//				if (n == JOptionPane.YES_OPTION) {
//					CharacterLibrary.add(inChar);
//					party.add(inChar);	// by default we add the new character to the party
//				}
//			} else {
//				//System.out.println("Selected character for "+inChar.getName()+" = "+oldChar.getName());
//				SelectDiffsDialog diffsDialog = new SelectDiffsDialog(this,oldChar,inChar);
//				diffsDialog.setVisible(true);
//				if (!diffsDialog.isCancelled()) {
//					List<String> updates = diffsDialog.getSelectedDiffs();
//					for (String prop : updates) {
//						oldChar.setProperty(prop, inChar.getPropertyValue(prop));
//					}
//				}
//			}
//		}
	}

	// TODO open should ask to save modified parties
	public void openParty() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
		fc.setCurrentDirectory(file);
		fc.setSelectedFile(file);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		file = fc.getSelectedFile();
		setTitle("Assistant DM - "+file.getName());
		System.out.println("Opening "+file.getAbsolutePath());

		CharacterLibrary.characters.clear();
		Document dom = Party.parseXML(file);
		party.parseDOM(dom);

		int selected = tabbedPane.getSelectedIndex();

		// FIXME rebuilding the ui is ugly - should be able to just replace the party
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
			file = fc.getSelectedFile();
			setTitle("Assistant DM - "+file.getName());
			System.out.println("Saving to "+file.getAbsolutePath());
			saveParty(file);
		}
	}

	public void saveParty(File f) {
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
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			doc.appendChild(party.getElement(doc));
			XMLUtils.writeDOM(doc, f);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
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
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			panel = null;
		}
		return panel;
	}

	public void saveShops() {
		String filename = "shops.ser";

		try (FileOutputStream fos = new FileOutputStream(filename);) {
			try (ObjectOutputStream out = new ObjectOutputStream(fos);) {
				shopPanel.saveShops(out);
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveItem) {
			if (file != null) saveParty(file);
			else saveAsParty();

		} else if (e.getSource() == saveAsItem) {
			saveAsParty();

		} else if (e.getSource() == updateItem) {
			updateParty();

		} else if (e.getSource() == selectPartyItem) {
			selectParty();

		} else if (e.getSource() == openItem) {
			openParty();

		} else if (e.getSource() == tableItem) {
			if (!controller.isOpen())
				controller.openRemote();

		} else if (e.getSource() == xpItem) {
			calculateXP();

		} else if (e.getSource() == xpHistoryItem) {
			/*for (CharacterLibrary.PartyXPItem item : CharacterLibrary.getXPHistory()) {
				System.out.println(item);
			}*/
			TableModel model = CharacterLibrary.getXPHistoryTableModel();
			JTable table = new JTableWithToolTips(model);
			//table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getColumnModel().getColumn(0).setPreferredWidth(100);
			table.getColumnModel().getColumn(1).setPreferredWidth(500);
			for (int i = 2; i < model.getColumnCount(); i++) {
				table.getColumnModel().getColumn(i).setPreferredWidth(80);
			}
			JScrollPane scrollpane = new JScrollPane(table);
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(scrollpane);
			JFrame popup = new JFrame("Party XP History");
			popup.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			popup.getContentPane().add(panel);
			// size the popup to be 80% of the parent window's bounds:
			Rectangle bounds = this.getBounds();
			bounds.x += bounds.width*.1;
			bounds.y += bounds.height*.1;
			bounds.width *= .8;
			bounds.height *= .8;
			popup.setBounds(bounds);
			popup.setVisible(true);

		} else if (e.getSource() == newCharacterItem) {
			newCharacter();

		} else {
			System.err.println("ActionEvent from unknown source: "+e);
		}
	}

	public void exit() {
		System.out.println("Exiting");
		saveParty(file);
		shopPanel.writeShopsXML("shops.xml");
		Updater.updaterThread.quit();
		ModuleRegistry.exit();
		System.exit(0);
	}
}

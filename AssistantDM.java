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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import magicitems.MagicGeneratorPanel;
import magicitems.Shop;
import magicitems.ShoppingPanel;
import monsters.EncounterDialog;
import monsters.MonstersPanel;

import org.w3c.dom.Document;

import party.Character;
import party.CharacterLibrary;
import party.Party;
import swing.JTableWithToolTips;
import ui.PartyPanel;
import ui.RollsPanel;
import ui.SelectDiffsDialog;
import ui.SelectPartyDialog;
import ui.UpdateCharacterDialog;
import ui.XPEntryDialog;
import util.Updater;
import util.XMLUtils;
import camera.CameraPanel;

import combat.CombatPanel;
import combat.InitiativeListModel;
import combat.MonsterCombatEntry;

import digital_table.controller.DigitalTableController;

/* TODO current priorities:
 * ENH: add location fields to GroupOptionPanel
 * DarknessMask should load/save cleared cells
 * ENH: consider separate element for darkness cleared cell - should be parent-relative - or perhaps add to LightSource
 * BUG: tries to pop up remote browser on the screen with the corresponding index, not the absolute screen number
 * ENH: Reordering the elements resets the group expanded/collapsed state
 * BUG: LineTemplate: setting image after rotation draws the un-transformed image
 * BUG: LineTemplate: dragging does not correctly pick the orgin/target if line is under group
 * BUG: ShapableTemplate: click does not get correct location when under group
 * BUG: MapImage: cleared cells are not relative to image
 * REF: Factor clear cells code into support class
 * MaskOptionsPanel: delete mask, reorder masks
 * MaskOptionsPanel: optimise masks by removing surrounding transparent pixels?
 * Copy in encounters dialog should copy the current creature, not the base
 * In encounters dialog, adding an image should select it for the current creature
 * Look at the map element order - should moving a tree move all children?
 * Hidden elements in table display should clear ImageMedia transforms
 * ImageMedia could use a soft/weak/strong reference for transformed images
 * MapImage: option to show solid background (at least use the regular background color. allow select colour?)
 *
 * Combat panel should save full monsters, not just combat entries
 * AC Size modifiers should be correctly linked to size stat
 * EncounterDialog: calc encounter level, display CRs
 * Encounterdialog should load/save buffs and maybe DTT selected elements
 * EncounterDialog: allow editing of AC, feats, size, SQ, etc
 * Allow the digital table controller to run without a remote
 * clear all for images. also cleared squares should be translucent on local
 * spell lists webpage
 * class levels
 * spell lists in AssistantDM
 * 
 * cleanup hitpoints/hitdice
 * implement remaining monster statistics
 * cleanup AttackForms in Attack, StatisticBlock and DetailedMonster
 *
 * rework attacks - they need an interface to filter properties like type etc. then filters can be used to build
 *    target lists (e.g  "type=bludgeoning and subclass=one handed melee")
 *
 * rework statistc notification system. a listener registered with the top of a tree (like Skills or Attacks)
 *    should get notification of all sub-statistics. consider whether statistics need to provide old and new values
 *    (this is desirable for mutable Modifiers at least)
 * 
 * consider reimplementing hps. it's not really a statistic, really more a property of the creature or perhaps of the
 *    level or hitdice statistic. figure out how to implement hitdice/character levels. implement negative levels as well
 * 
 * parsing display xml resets the node priorty - need to save the list model order
 * look at standardising attribute naming style in xml documents - currently have camel case for combat.xml, lower with underscores most other cases but a few cases of lower with dashes in party.xml
 *
 * BUG handle io exceptions while reading display.xml
 * BUG exception if image width or height is set to 0 - slightly fixed by returning the unscaled/rotated image
 * asynchronous loading of images
 * soft references for ImageMedia - at least for transformed images
 * character is not registered as a listener on the attack forms so it doesn't get notified of changes. probably should revisit the whole property/statistic notification system
 * when temporary hitpoints from a buff are gone the buff should be removed if it has no other effect
 * should be able to temporarily disable armor/shield
 * add caching of loaded files in MediaManager
 * performance improvements in animation code - bounding boxes for elements
 * grouping - changing parent should modify children to maintain position - probably need consistent location property to implement this
 * rearrange images. also find animal names - stats blocks
 * alternate token image for dead creatures
 * website: simplify updating - updates can be missed at the moment
 * 
 * size (where is up to?)
 * add damage statistic on attackforms. add extra_damage property to damage statistics (for e.g. flamming)
 * implement buffs on attackforms - need to implement add action in AttackFormPanel.AttackFormInfoDialog
 * add combat panel section for pending xp/defeated monsters
 * live character sheet: fix up incomplete fading of character sheet when dialog appears
 * ui: add info dialog for remaining statistics (ac, weapons, armor, level, size)
 * live character sheet: add calculations for remaining statistics (attacks, ac, weapon damage, armor, level, size)
 * live character sheet: consider adding list of buffs/effects
 * Better support for instansiating monsters - size, HPs generation, select token image, show image, etc
 * camera: EOS camera support + refactoring of camera library
 * camera/dtt: Detect token movement
 * properties for statistics: bab, convert temp hps
 * review Statistics vs creature Properties
 * ability checks
 * enum conversions - property and statistics types
 * feats - selecting of feats with target skill/weapon/spells/school. change available list to remove already selected feats
 * equipment, particularly magic item slots, armor, weapons
 */
/* TODO digital tabletop (dtt) priorities:
 * Allow reconnect to remote - partly works but seems to cause exception on 3rd reconnect
 * Threaded remote communication
 * Add colour to the overlay tokens. either indicator of health or settable
 * Consider expanding "selected" support. Would need hierarchy support as with visibility
 * Improve camera integration, fix ui for camera panel
 * Refactor common utility methods into MapElement (e.g. template creation)
 * Alternate button dragging (e.g. resize)
 * Recalibrate - could be done using screen bounds element
 * Auto configure - set defaults according to OS screen layout
 * Make line and spread templates editable?
 * Swarm Token (editable token with replicated painting)
 * Convert MapElements to use location property instead of X and Y properties - will reduce dragging code in OptionsPanel subclasses
 * dice roller element?
 * thrown object scatter?
 */

//TODO change 'value' attributes in xml. these should either be 'base' or 'total' attributes (support 'value' as 'base' for loading only). also fix differences in ac
//TODO convert ui classes that listen to Character to listen to the specific Statistics instead - could do a StatisticsProxy class
//that could be used as a base for statistics that rely on a common set of modifiers such as touch AC, skills etc
//TODO need to review how properties work on Character and BoundIntegerField
//TODO ultimately would like a live DOM. the DOM saved to the party XML file would be a filtered version

/* Things to implement:
 *  (in progress) Feats
 *  (in progress) Grapple modifier
 *  Ability score checks
 *  Class levels
 *  Spell lists / spells per day
 *  Damage reduction
 *  Spell resistance
 *  Magic items slots
 *  Weight/encumberance
 *  Skill synergies
 *  Skill named versions (Crafting, Profession etc)
 *  Size
 *  Speed
 */

/* Architecture:
 * Packages:
 * camera - camera panel ui and functionality
 * combat - combat panel ui and functionality
 * digital_table - local and remote digital table functionality
 * digital_table.controller - local digital table controller ui
 * digital_table.elements - displayable elements common to both local and remote displays
 * digital_table.server - core functionality for both local and remote displays and remote only classes
 * gamesystem - core code related to the 3.5 mechanics
 * gamesystem.dice - classes representing dice
 * magicgenerator - random magic item generator
 * magicitems - shop panel ui and functionality
 * monsters - monsters panel ui and functionality
 * party - party panel functionality (should also contain ui for party and rolls panels)
 * swing - extended generic swing components
 * ui - ui for party and rolls panel and dialogs. should contain only common ui and dialogs
 * util - util classes for external communication (XML handling, file uploading, logging)
 */

//WISH would be nice to have a library of creatures that could be selected for the combat panel
//WISH refactor classes that should be in ui package
//TODO add new party menu option, ask to save modified file
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
	static String tableServer;
	DigitalTableController controller;

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

		if (args.length > 0) {
			tableServer = args[0];
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				AssistantDM inst = new AssistantDM();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}

	public AssistantDM() {
		file = new File("party.xml");

		setTitle("Assistant DM - "+file.getName());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

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
		setJMenuBar(menuBar);

		tabbedPane = new JTabbedPane();

		party = Party.parseXML(file);

		JComponent panel;
		combatPanel = new CombatPanel(party);
		File f = new File("combat.xml");
		if (f.exists()) combatPanel.parseXML(f);
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

		try {
			cameraPanel = new CameraPanel();
			tabbedPane.addTab("Camera", null, cameraPanel, "Camera Remote Image Capture");
		} catch (UnsatisfiedLinkError e) {
			System.out.println("Caught error: "+e);
		}

		controller = new DigitalTableController(AssistantDM.tableServer, cameraPanel) {
			@Override
			protected void quit() {
				close();
			}
		};

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
			Set<Character> changes = new HashSet<Character>();
			for (Character c : party) {
				if (!newParty.contains(c)) changes.add(c);
			}
			for (Character c : changes) party.remove(c);
			changes = new HashSet<Character>();
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
		// Get file
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
		fc.setCurrentDirectory(file);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		File newfile = fc.getSelectedFile();
		System.out.println("Updating party from "+newfile.getAbsolutePath());

		// Load file into new party
		Party newparty = Party.parseXML(newfile,true);

		// Match characters
		UpdateCharacterDialog dialog = new UpdateCharacterDialog(this,party,newparty);
		dialog.setVisible(true);
		if (dialog.isCancelled()) return;

		// Shows diffs and accept
		for (Character inChar : newparty) {
			Character oldChar = dialog.getSelectedCharacter(inChar);
			if (oldChar == null) {
				//System.out.println("No character selected for "+inChar.getName());
				int n = JOptionPane.showConfirmDialog(this,"Do you want to add "+inChar.getName()+"?","Add New Character",JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					CharacterLibrary.add(inChar);
					party.add(inChar);	// by default we add the new character to the party
				}
			} else {
				//System.out.println("Selected character for "+inChar.getName()+" = "+oldChar.getName());
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
		party = Party.parseXML(file);

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
		} catch(IOException ex) {
			ex.printStackTrace();
			panel = null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			panel = null;
		}
		return panel;
	}

	public void saveCombat() {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			doc.appendChild(combatPanel.getElement(doc));
			XMLUtils.writeDOM(doc, new File("combat.xml"));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
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
				controller.openRemote(AssistantDM.tableServer);

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
			popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
		saveCombat();
		if (cameraPanel != null) cameraPanel.disconnect();
		Updater.updaterThread.quit();
		controller.close();
		System.exit(0);
	}
}

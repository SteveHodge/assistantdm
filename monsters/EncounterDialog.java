package monsters;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import combat.EncounterModule;
import combat.MonsterCombatEntry;
import digital_table.controller.ControllerFrame;
import gamesystem.Creature;
import gamesystem.core.PropertyListener;
import monsters.StatisticsBlock.Field;
import monsters.StatisticsBlock.HDAdvancement;
import util.ModuleRegistry;
import util.XMLUtils;

//TODO should keep a map of Field to DetailPanel so we can update them automatically and select the right one more easily
//TODO details button checks for a statistics block, it should probably check for a URL
//TODO need scrollbar on monster detail

@SuppressWarnings("serial")
public class EncounterDialog extends JFrame {
	private static final String BLANK_PANEL = "BLANK";

	private static int ordinal = 1;
	private static List<EncounterDialog> dialogs = new ArrayList<>();

	private JSpinner countSpinner;
	private JList<Monster> monsterList;
	private MonsterListModel monsterListModel;
	private Monster selected;

	private JButton addButton;
	private JButton deleteButton;
	private JButton detailsButton;

	private StatsBlockPanel statsPanel;

	private CardLayout detailLayout;
	private JPanel detailPanel;
	private Map<Field, DetailPanel> detailPanels = new HashMap<>();

	private NotesPanel notesPanel;

	private Map<StatisticsBlock, List<URL>> imageURLs = new HashMap<>();	// null key is used for adhoc monsters with no attached stats block
	private Map<Monster, MonsterData> monsterData = new HashMap<>();		// stores extra data for each monster in the encounter

	class MonsterData {
		int imageIndex = -1;	// -1 = no image
		List<HDAdvancement> hdAdvancements;
		Map<Field, List<Object>> notes = new HashMap<>();

		MonsterData(Monster m) {
			StatsBlockCreatureView view = StatsBlockCreatureView.getView(m);
			hdAdvancements = StatisticsBlock.parseHDAdvancement(view.getField(Field.ADVANCEMENT));
			if (hdAdvancements.size() > 0) {
				// add entry for standard HD and size
				int hd = m.hitDice.getHitDiceCount();
				hdAdvancements.add(0, new HDAdvancement(hd, hd, m.size.getBaseSize()));
			}
//			System.out.println(m.getName() + " hdAdvancements = " + hdAdvancements);
		}

		void addNote(Field f, Object note) {
			List<Object> fieldNotes = notes.get(f);
			if (fieldNotes == null) {
				fieldNotes = new ArrayList<>();
				notes.put(f, fieldNotes);
			}
			fieldNotes.add(note);
			notesPanel.updateNotes();
			statsPanel.updateIcons();
		}
	}

	static void createOrExtendEncounter(Window parentFrame, StatisticsBlock blk) {
		if (EncounterDialog.dialogs.size() > 0) {
			String[] options = new String[EncounterDialog.dialogs.size() + 1];
			int i = 0;
			for (EncounterDialog d : EncounterDialog.dialogs) {
				options[i++] = d.getEncounterName();
			}
			options[i] = "<new encounter>";
			String encounter = (String) JOptionPane.showInputDialog(parentFrame, "Select the encounter to add the monster to:",
					"Select encounter", JOptionPane.PLAIN_MESSAGE, null, options, null);
			for (EncounterDialog d : EncounterDialog.dialogs) {
				if (d.getEncounterName().equals(encounter)) {
					d.addMonster(blk);
					return;
				}
			}
		}

		// create new encounter (note we may have fallen through the above or there may be no encounters open yet)
		new EncounterDialog(blk);
	}

	public EncounterDialog() {
		this(null, true);
	}

	public EncounterDialog(boolean visible) {
		this(null, visible);
	}

	EncounterDialog(final StatisticsBlock s) {
		this(s, true);
	}

	private EncounterDialog(final StatisticsBlock s, boolean visible) {
		super("Encounter " + ordinal++);
		dialogs.add(this);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dialogs.remove(EncounterDialog.this);
			}
		});

		monsterListModel = new MonsterListModel();
		List<URL> urls = new ArrayList<>();
		if (s != null) {
			selected = StatsBlockCreatureView.createMonster(s);
			monsterListModel.addMonster(selected);
			monsterData.put(selected, new MonsterData(selected));
			Collections.addAll(urls, s.getImageURLs());
			imageURLs.put(s, urls);
		}

		NamePanel namePanel = new NamePanel(imageURLs);
		detailPanels.put(Field.NAME, namePanel);
		detailPanels.put(Field.ABILITIES, new AbilitiesPanel());
		detailPanels.put(Field.HITDICE, new HitPointsPanel());
		detailPanels.put(Field.AC, new ACPanel());
		detailPanels.put(Field.SIZE_TYPE, new SizeTypePanel());
		for (Field f : Field.getStandardOrder()) {
			if (!detailPanels.containsKey(f)) {
				detailPanels.put(f, new DefaultDetailPanel(f));
			}
		}

		SpinnerModel model = new SpinnerNumberModel(1, 1, 100, 1);
		countSpinner = new JSpinner(model);

		monsterList = new JList<>(monsterListModel);
		monsterList.setPreferredSize(new Dimension(200, 100));
		monsterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		monsterList.setSelectedIndex(0);
		monsterList.addListSelectionListener(e -> updateFields());

		statsPanel = new StatsBlockPanel(selected, monsterData.get(selected));
		statsPanel.setSelectionForeground(monsterList.getSelectionForeground());
		statsPanel.setSelectionBackground(monsterList.getSelectionBackground());
		statsPanel.addListSelectionListener(e -> updateFields());

		detailLayout = new CardLayout();
		detailPanel = new JPanel(detailLayout);
		for (Field f : detailPanels.keySet()) {
			detailPanel.add(detailPanels.get(f), f.name());
		}
		detailPanel.add(new JPanel(), BLANK_PANEL);
		detailLayout.show(detailPanel, BLANK_PANEL);

		detailsButton = new JButton("Details");
		detailsButton.addActionListener(e -> {
			if (selected == null) return;
			if (selected.statisticsBlock == null) return;
			JFrame frame = new MonsterFrame(selected.statisticsBlock);
			frame.setVisible(true);
		});

		addButton = new JButton("Add: ");
		addButton.addActionListener(addButtonListener);

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(e -> {
			int sel = monsterList.getSelectedIndex();
			monsterListModel.removeElementAt(sel);
		});

		notesPanel = new NotesPanel();

		//@formatter:off
		JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH; c.weightx = 0.5d; c.weighty = 1.0d;
		c.gridwidth = 2; c.gridheight = 1;
		c.gridx = 0; c.gridy = 0;
		main.add(new JScrollPane(monsterList), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.25d; c.weighty = 0d;
		c.gridwidth = 1; c.gridheight = 1;
		c.gridy = 1;
		main.add(detailsButton,c);
		c.gridx = 1;
		main.add(deleteButton, c);

		c.gridx = 0; c.gridy = 2;
		main.add(addButton, c);
		c.gridx = 1;
		JPanel p = new JPanel();
		p.add(countSpinner);
		p.add(new JLabel("copies"));
		main.add(p, c);

		c.fill = GridBagConstraints.BOTH; c.weightx = 0.5d; c.weighty = 0d;
		c.gridheight = 3;
		c.gridx = 2; c.gridy = 0;
		main.add(statsPanel, c);

		c.gridx = 3;
		c.fill = GridBagConstraints.BOTH; c.weightx = 1.0d; c.weighty = 1.0d;
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(detailPanel);
		rightPanel.add(notesPanel, BorderLayout.SOUTH);
		main.add(rightPanel, c);

		// @formatter:on

		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(e -> load(EncounterDialog.this));

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(saveButtonListener);

		JButton addCombatButton = new JButton("Add To Combat");
		addCombatButton.addActionListener(e -> addToCombat());

		JButton addTokenButton = new JButton("Add Tokens");
		addTokenButton.addActionListener(e -> addTokens());

		JPanel buttons = new JPanel();
		buttons.add(loadButton);
		buttons.add(saveButton);
		buttons.add(addCombatButton);
		buttons.add(addTokenButton);

		add(main, BorderLayout.CENTER);
		add(buttons, BorderLayout.NORTH);

		for (DetailPanel dp : detailPanels.values()) {
			dp.setMonster(selected, monsterData.get(selected));
		}
		updateFields();
		namePanel.setSelectedImage(0);

		if (visible) {
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}
	}


	private final ActionListener addButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO this should duplicate the monster directly rather than using the stats block (which may not be present once adhoc monsters can be bought into this dialog)
			StatisticsBlock blk = selected.statisticsBlock;
			if (blk == null) return;

			int ordinal = 1;

			String name = selected.getName();
			// check if the source monster name has a trailing number, if not add one:
			if (name.matches(".*\\s\\d+$")) {
				ordinal = Integer.parseInt(name.substring(name.lastIndexOf(" ") + 1));
				name = name.substring(0, name.lastIndexOf(" "));	// strip the number off the end to leave the base name
			} else {
				System.out.println("Name without trailing digit '" + name + "'");
				selected.setName(name + " 1");
			}

			for (int i = 0; i < (Integer) countSpinner.getValue(); i++) {
				Monster m = StatsBlockCreatureView.createMonster(blk);

				// get the next ordinal that isn't in use
				boolean ok;
				do {
					ok = true;
					ordinal++;
					for (int j = 0; j < monsterListModel.getSize(); j++) {
						Monster mons = monsterListModel.getElementAt(j);
						if (mons.getName().equals(name + " " + ordinal)) ok = false;
					}
				} while (!ok);

				m.setName(name + " " + ordinal);
				monsterListModel.addMonster(m);
				MonsterData customisation = new MonsterData(m);
				monsterData.put(m, customisation);
				if (imageURLs.get(blk).size() > 0) customisation.imageIndex = 0;
			}
		}
	};

	private void addTokens() {
		for (int i = 0; i < monsterListModel.getSize(); i++) {
			Monster m = monsterListModel.getElementAt(i);
			MonsterData data = monsterData.get(m);
			File f = null;
			List<URL> urls = imageURLs.get(m.statisticsBlock);
			if (urls != null && data.imageIndex >= 0) {
				URL url = urls.get(data.imageIndex);
				// TODO fix this. probably addMonster should take a URL for the image
				try {
					f = new File(url.toURI());
				} catch (URISyntaxException e) {
				}
			}
			ControllerFrame.addMonster(m, f);
		}
	}

	private void addToCombat() {
		EncounterModule enc = ModuleRegistry.getModule(EncounterModule.class);

		for (int i = 0; i < monsterListModel.getSize(); i++) {
			Creature m = monsterListModel.getElementAt(i);
			enc.getInitiativeListModel().addEntry(new MonsterCombatEntry(m));
		}
	}

	public void load(Component dialogParent) {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
		fc.setCurrentDirectory(new File("."));
		int returnVal = fc.showOpenDialog(dialogParent);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		File file = fc.getSelectedFile();
		System.out.println("Opening encounter " + file.getAbsolutePath());

		Document dom = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//InputStream is = p.getClass().getClassLoader().getResourceAsStream("party.xsd");
		//factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
		try {
			dom = factory.newDocumentBuilder().parse(file);
		} catch (SAXException | IOException | ParserConfigurationException ex) {
			ex.printStackTrace();
		}

		if (dom != null) {
//				Element displayEl = null;
			NodeList nodes = dom.getDocumentElement().getChildNodes();
			EncounterModule enc = ModuleRegistry.getModule(EncounterModule.class);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals("Elements")) {
//						displayEl = (Element) node;
				} else if (node.getNodeName().equals("Creatures") && enc != null) {
					NodeList children = node.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName().equals("Monster")) {
							XMLMonsterParser parser = new XMLMonsterParser();
							Monster m = parser.parseDOM((Element) children.item(j));
							addMonster(m);
						}
					}
				}
			}
		}

		checkSelection();

		if (!isVisible()) {
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}
	}

	private final ActionListener saveButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			chooser.setSelectedFile(new File(getEncounterName() + ".xml"));
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
			if (chooser.showSaveDialog(EncounterDialog.this) == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();
				if (f != null) {
					save(f);
				}
			}
		}
	};

	private void save(File f) {
		System.out.println("Save to " + f);

		// TODO if f exists then confirm overwrite or confirm add/replace display config if it's an encounter file

		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("Encounter");
			doc.appendChild(root);

			// TODO once the existing combat can be opened as an encounter we'll need to save any buffs that target monsters
			Element el = doc.createElement("Creatures");
			for (int i = 0; i < monsterListModel.getSize(); i++) {
				Creature m = monsterListModel.getElementAt(i);
				XMLOutputMonsterProcessor processor = new XMLOutputMonsterProcessor(doc);
				m.executeProcess(processor);
				// TODO add image URL
				el.appendChild(processor.getElement());
			}
			root.appendChild(el);

			XMLUtils.writeDOM(doc, f);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private String getEncounterName() {
		return getTitle();
	}

	// if no monster is selected but there are monsters in the list, selects the first one
	private void checkSelection() {
		if (selected == null && monsterListModel.getSize() > 0) {
			monsterList.setSelectedIndex(0);
		}
	}

	void addMonster(StatisticsBlock s) {
		// TODO check if the name is already in use, if so should change the existing name and add an ordinal
		Monster m = StatsBlockCreatureView.createMonster(s);
		addMonster(m);
	}

	public void addMonster(Monster m) {
		StatisticsBlock s = m.statisticsBlock;
		MonsterData customisation = new MonsterData(m);
		monsterData.put(m, customisation);
		monsterListModel.addMonster(m);
		if (!imageURLs.containsKey(s)) {
			List<URL> urls = new ArrayList<>();
			if (s != null) Collections.addAll(urls, s.getImageURLs());
			imageURLs.put(s, urls);
		}
		if (imageURLs.get(s).size() > 0) customisation.imageIndex = 0;
		checkSelection();
	}

	private void updateFields() {
		Monster m = monsterList.getSelectedValue();

		if (m != selected) {
			selected = m;
			statsPanel.setCreature(selected, monsterData.get(selected));
			for (DetailPanel p : detailPanels.values()) {
				p.setMonster(selected, monsterData.get(selected));
			}

			if (selected == null) {
				addButton.setEnabled(false);
				deleteButton.setEnabled(false);
				detailsButton.setEnabled(false);
			} else {
				addButton.setEnabled(true);
				deleteButton.setEnabled(true);
				detailsButton.setEnabled(selected.statisticsBlock != null);
			}
		}

		if (selected != null) {
			if (detailPanels.containsKey(statsPanel.getSelectedField())) {
				detailLayout.show(detailPanel, statsPanel.getSelectedField().name());
			} else {
				detailLayout.show(detailPanel, BLANK_PANEL);
			}
		} else {
			detailLayout.show(detailPanel, BLANK_PANEL);
		}

		notesPanel.setMonsterField(selected, statsPanel.getSelectedField());
	}

	private class NotesPanel extends JPanel {
		Monster monster;
		StatsBlockCreatureView view;
		Field field;
		JLabel sourceLabel = new JLabel("Source: ");
		JLabel sourceDetails = new JLabel();

		NotesPanel() {
			setBorder(BorderFactory.createTitledBorder("Notes"));
			setLayout(new GridBagLayout());
			// old code to limit label length:
//			sourceLabel = new JLabel("") {
//				@Override
//				public Dimension getPreferredSize() {
//					// Prefer a width of 200
//					Dimension d = super.getPreferredSize();
//					if (d.width > 200)
//						d.width = 200;
//					return d;
//				}
//			};

		}

		void setMonsterField(Monster m, Field f) {
			if (monster != m) {
				if (view != null) view.removePropertyChangeListener(listener);
				monster = m;
				if (monster != null) {
					view = StatsBlockCreatureView.getView(monster);
					view.addPropertyChangeListener(listener);
				}
			}
			field = f;
			updateNotes();
		}

		void updateNotes() {
			removeAll();
			if (monster == null) return;

			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;

			// show any notes
			List<Object> notes = monsterData.get(monster).notes.get(field);
			if (notes != null) {
				c.gridwidth = 1;
				c.weightx = 0;
				c.gridx = 0;
				for (Object note : notes) {
					JCheckBox check = new JCheckBox();
					check.addActionListener(e -> {
						SwingUtilities.invokeLater(() -> {
							monsterData.get(monster).notes.get(field).remove(note);
							updateNotes();
							statsPanel.updateIcons();
						});
					});
					add(check, c);
				}

				c.gridwidth = 2;
				c.weightx = 1.0d;
				c.gridx = 1;
				for (Object note : notes) {
					add(new JLabel(note.toString()), c);
				}
			}

			// show source if the current field value is different
			StatisticsBlock blk = selected.statisticsBlock;
			if (blk != null) {
				String source = blk.get(field);
				String now = view.getField(field, false);
				if (!view.isEquivalent(field, now)) {
					sourceDetails.setText("<html>" + source + "</html>");
					c.gridwidth = 2;
					c.weightx = 0.0d;
					c.gridx = 0;
					add(sourceLabel, c);
					c.gridx = 2;
					c.weightx = 1.0d;
					c.gridwidth = 1;
					add(sourceDetails, c);
				}
			}
		}

		PropertyChangeListener listener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (field == null || field.name() == e.getPropertyName()) return;
				updateNotes();
			}
		};
	};

	private class MonsterListModel extends AbstractListModel<Monster> {
		private List<Monster> monsters = new ArrayList<>();

		@Override
		public Monster getElementAt(int i) {
			return monsters.get(i);
		}

		public void removeElementAt(int i) {
			Creature m = monsters.remove(i);
			m.removePropertyListener("name", listener);
			fireIntervalRemoved(this, i, i);
		}

		public void addMonster(Monster m) {
			m.addPropertyListener("name", listener);
			monsters.add(m);
			fireIntervalAdded(this, monsters.size() - 1, monsters.size() - 1);
		}

		@Override
		public int getSize() {
			return monsters.size();
		}

		private PropertyListener listener = e -> {
			int idx = monsters.indexOf(e.source.getParent());
			if (idx != -1) {
				fireContentsChanged(this, idx, idx);
			}
		};
	}
}
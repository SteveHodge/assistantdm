package ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import gamesystem.CasterLevels;
import gamesystem.CasterLevels.CasterClass;
import gamesystem.CharacterClass;
import gamesystem.core.SimplePropertyCollection;

// TODO save caster config to file (and/or website)
// TODO load/save spell selections file (and/or website)
// TODO add list of multiple casters
// TODO ui to select feats for caster
// TODO ui to select domains for cleric

@SuppressWarnings("serial")
public class CasterPanel extends JPanel {
	CasterLevels casterLevels;
	CasterLevelsModel classesTableModel;
	CasterClassModel classesComboModel;
	JTabbedPane tabbedPane;
	Map<CharacterClass, List<SpellsPanel>> classTabs = new HashMap<>();
	SimplePropertyCollection propCollect = new SimplePropertyCollection();

	static File casterFile = new File("\\\\armitage\\website\\assistantdm\\characters");
	JsonObject spellsFileJson;

	public CasterPanel() {
		casterLevels = new CasterLevels(propCollect);
		classesTableModel = new CasterLevelsModel();
		classesTableModel.addTableModelListener((e)->{
			// check tab config matches class levels
			Set<CharacterClass> currentClasses = new HashSet<>();
			for (CasterClass c : casterLevels.casterClassesIterable()) {
				currentClasses.add(c.getCharacterClass());
				List<SpellsPanel> tabs = classTabs.get(c.getCharacterClass());
				if (tabs != null && tabs.size() > 0) {
					// updates to level and ability happen automatically
				} else {
					// add tab(s)
					if (tabs == null) {
						tabs = new ArrayList<>();
						classTabs.put(c.getCharacterClass(), tabs);
					}
					if (c.getCharacterClass() == CharacterClass.BARD) {
						SpellsPanel.LearnPanel p = new SpellsPanel.LearnPanel(c);
						tabs.add(p);
						tabbedPane.addTab("Bard", null, p, "Bard Spells Known");
					} else if (c.getCharacterClass() == CharacterClass.CLERIC) {
						SpellsPanel.PreparePanel p = new SpellsPanel.PreparePanel(c);
						tabs.add(p);
						tabbedPane.addTab("Cleric", null, p, "Cleric Spells Prepared");
						p = new SpellsPanel.PreparePanel(c, true);
						tabs.add(p);
						tabbedPane.addTab("Domain", null, p, "Cleric Domain Spells Prepared");
					} else if (c.getCharacterClass() == CharacterClass.DRUID) {
						SpellsPanel.PreparePanel p = new SpellsPanel.PreparePanel(c);
						tabs.add(p);
						tabbedPane.addTab("Druid", null, p, "Druid Spells Prepared");
					} else if (c.getCharacterClass() == CharacterClass.PALADIN) {
						SpellsPanel.PreparePanel p = new SpellsPanel.PreparePanel(c);
						tabs.add(p);
						tabbedPane.addTab("Paladin", null, p, "Paladin Spells Prepared");
					} else if (c.getCharacterClass() == CharacterClass.RANGER) {
						SpellsPanel.PreparePanel p = new SpellsPanel.PreparePanel(c);
						tabs.add(p);
						tabbedPane.addTab("Ranger", null, p, "Ranger Spells Prepared");
					} else if (c.getCharacterClass() == CharacterClass.SORCERER) {
						SpellsPanel.LearnPanel p = new SpellsPanel.LearnPanel(c);
						tabs.add(p);
						tabbedPane.addTab("Sorcerer", null, p, "Sorcerer Spells Known");
					} else if (c.getCharacterClass() == CharacterClass.WIZARD) {
						SpellsPanel.ScribePanel p = new SpellsPanel.ScribePanel(c);
						tabs.add(p);
						tabbedPane.addTab("Spellbook", null, p, "Wizard Spellbook");
						SpellsPanel.PreparePanel pp = new SpellsPanel.PreparePanel(c, p.spellbookModel);
						tabs.add(pp);
						tabbedPane.addTab("Wizard", null, pp, "Wizard Spells Prepared");
					}
				}
			}

			// remove any tabs that are no longer required
			List<CharacterClass> toRemove = new ArrayList<>();
			for (CharacterClass c : classTabs.keySet()) {
				List<SpellsPanel> tabs = classTabs.get(c);
				if (tabs != null && tabs.size() > 0 && !currentClasses.contains(c)) {
					for (JComponent t : tabs) {
						tabbedPane.remove(t);
					}
					toRemove.add(c);
				}
			}
			for (CharacterClass c : toRemove) {
				classTabs.remove(c);
			}
		});
		JTable classesTable = new JTable(classesTableModel);
		TableColumn classCol = classesTable.getColumnModel().getColumn(0);
		classesComboModel = new CasterClassModel();
		JComboBox<CharacterClass> classCombo = new JComboBox<>(classesComboModel);
		classCol.setCellEditor(new DefaultCellEditor(classCombo));

		JScrollPane scroller = new JScrollPane(classesTable);
		scroller.setPreferredSize(new Dimension(400, 100));
		scroller.setBorder(BorderFactory.createTitledBorder("Caster Levels"));
		scroller.setMinimumSize(new Dimension(400, 100));

		JButton exportButton = new JButton("Export Caster");
		exportButton.addActionListener(e -> exportCaster());

		JButton importButton = new JButton("Import Caster");
		importButton.addActionListener(e -> importCaster());

		JButton exportSpellsButton = new JButton("Export Spells");
		exportSpellsButton.addActionListener(e -> exportSpells());

		JButton importSpellsButton = new JButton("Import Spells");
		importSpellsButton.addActionListener(e -> importSpells());

		tabbedPane = new JTabbedPane();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1;
		c.weighty = 0;
		c.anchor = GridBagConstraints.EAST;
		add(scroller, c);

		c.gridx = 1;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(8, 4, 8, 4);
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
		buttons.add(exportButton);
		buttons.add(importButton);
		buttons.add(exportSpellsButton);
		buttons.add(importSpellsButton);
		add(buttons, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		add(tabbedPane, c);
	}

	// TODO backup old version and/or confirm replace
	void exportCaster() {
		JFileChooser fc = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("Character Files", "character");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(casterFile);
		int returnVal = fc.showSaveDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		casterFile = fc.getSelectedFile();

		System.out.println("Export caster to :" + casterFile.getAbsolutePath());
		JsonArrayBuilder classesBuilder = Json.createArrayBuilder();
		for (int i = 0; i < classesTableModel.getRowCount(); i++) {
			CasterClass c = classesTableModel.getCasterClass(i);
			if (c == null || c.getCharacterClass() == null || c.getCasterLevel() == 0) continue;
			JsonObjectBuilder classBuilder = Json.createObjectBuilder()
					.add("class", c.getCharacterClass().toString())
					.add("level", c.getCasterLevel())
					.add("ability", c.getAbilityScore());
			classesBuilder.add(classBuilder);
		}
		JsonArray classes = classesBuilder.build();

		Map<String, Boolean> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory writerFactory = Json.createWriterFactory(config);
		try (OutputStream out = new FileOutputStream(casterFile)) {
			writerFactory.createWriter(out).write(classes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void importCaster() {
		JFileChooser fc = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("Character Files", "character");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(casterFile);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		casterFile = fc.getSelectedFile();
		System.out.println("Import caster from " + casterFile.getAbsolutePath());

		FileInputStream fis;
		try {
			fis = new FileInputStream(casterFile);

			JsonReader reader = Json.createReader(fis);
			JsonArray jArray = reader.readArray();

			// empty the existing classes and reset the class combo
			casterLevels.clearCasterClasses();
			classesComboModel.updateOptions();

			for (JsonValue jValue : jArray) {
				if (jValue.getValueType() != JsonValue.ValueType.OBJECT) {
					System.out.println("Unexpected json value (should be object): " + jValue);
					return;
				}
				JsonObject ccJson = (JsonObject) jValue;
				CharacterClass cls = CharacterClass.getCharacterClass(ccJson.getString("class"));
				CasterClass casterClass = casterLevels.new CasterClass(cls);
				casterClass.setCasterLevel(ccJson.getInt("level"));
				casterClass.setAbilityScore(ccJson.getInt("ability"));
				if (ccJson.containsKey("domains")) {
					JsonArray domainJson = ccJson.getJsonArray("domains");
					List<JsonString> domains = domainJson.getValuesAs(JsonString.class);
					String d1 = null, d2 = null;
					if (domains.size() > 0) d1 = domains.get(0).getString();
					if (domains.size() > 1) d2 = domains.get(1).getString();
					casterClass.setDomains(d1, d2);
				}
				casterLevels.addCasterClass(casterClass);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void exportSpells() {
		JFileChooser fc = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("Character Spell Files", "spells");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(casterFile);
		int returnVal = fc.showSaveDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		casterFile = fc.getSelectedFile();
		System.out.println("Export character spells to " + casterFile.getAbsolutePath());

		JsonObjectBuilder builder = Json.createObjectBuilder();
		if (spellsFileJson == null) {
			// setup new object
			builder
			.add("tab_cast", Json.createArrayBuilder())
			.add("castList", Json.createArrayBuilder())
			.add("dailies", Json.createArrayBuilder())
			.add("charges", Json.createArrayBuilder());
		} else {
			// copy existing object
			spellsFileJson.entrySet().forEach(e -> builder.add(e.getKey(), e.getValue()));
		}
		// replace tabs that we know about in spellsFileJson
		for (CharacterClass cls : classTabs.keySet()) {
			List<SpellsPanel> tabs = classTabs.get(cls);
			for (SpellsPanel tab : tabs) {
				System.out.println("Tab for " + cls + ": " + tab);

				String tag = tab.getJsonTabName();
				JsonValue json = tab.getSpellsJsonBuilder();
				if (json != null) {
					builder.add(tag, json);
				}
			}
		}
		/*
		"tab_sorcerer"
		"tab_wizard"
		"tab_spellbook"
		"tab_bard"
		"tab_ranger"
		"tab_cleric"
		"tab_domain"
		"tab_druid"
		"tab_paladin"
		 */
		spellsFileJson = builder.build();

		Map<String, Boolean> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory writerFactory = Json.createWriterFactory(config);
		try (OutputStream out = new FileOutputStream(casterFile)) {
			writerFactory.createWriter(out).write(spellsFileJson);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void importSpells() {
		JFileChooser fc = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("Character Spell Files", "spells");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(casterFile);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		casterFile = fc.getSelectedFile();
		System.out.println("Import character spells from " + casterFile.getAbsolutePath());

		FileInputStream fis;
		try {
			fis = new FileInputStream(casterFile);

			JsonReader reader = Json.createReader(fis);
			spellsFileJson = reader.readObject();

			System.out.println("Processing character spells file:");
			// process the tabs
			/*
			"tab_cast"
			"castList"
			"dailies"
			"charges"
			"tab_sorcerer"
			"tab_wizard"
			"tab_spellbook"
			"tab_bard"
			"tab_ranger"
			"tab_cleric"
			"tab_domain"
			"tab_druid"
			"tab_paladin"
			 */
			spellsFileJson.forEach((k, v) -> {
				System.out.println("   " + k);
			});

			for (CharacterClass cls : classTabs.keySet()) {
				System.out.println("Class: " + cls);
				for (SpellsPanel panel : classTabs.get(cls)) {
					if (spellsFileJson.containsKey(panel.getJsonTabName())) {
						System.out.println("Found tab for " + cls + ": " + panel.getJsonTabName());
						panel.loadJson(spellsFileJson.get(panel.getJsonTabName()).asJsonArray());
					} else {
						System.out.println("" + cls + " tab " + panel.getJsonTabName() + " not found in input spells file");
					}
				}
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class CasterClassModel extends AbstractListModel<CharacterClass> implements ComboBoxModel<CharacterClass> {
		List<CharacterClass> classes = new ArrayList<CharacterClass>();
		Object selected = null;

		public CasterClassModel() {
			updateOptions();
		}

		public void updateOptions() {
			classes.clear();
			classes.add(CharacterClass.BARD);
			classes.add(CharacterClass.CLERIC);
			classes.add(CharacterClass.DRUID);
			classes.add(CharacterClass.PALADIN);
			classes.add(CharacterClass.RANGER);
			classes.add(CharacterClass.SORCERER);
			classes.add(CharacterClass.WIZARD);
			for (int i = 0; i < classesTableModel.getRowCount(); i++) {
				CharacterClass c = (CharacterClass) classesTableModel.getValueAt(i, 0);	// TODO fragile to access this by column number
				if (c != null) {
					classes.remove(c);
				}
			}
			// maybe could add the current row's class to the list if there are no other options
			fireContentsChanged(this, 0, classes.size());
		}

		@Override
		public CharacterClass getElementAt(int i) {
			return classes.get(i);
		}

		@Override
		public int getSize() {
			return classes.size();
		}

		@Override
		public Object getSelectedItem() {
			return selected;
		}

		@Override
		public void setSelectedItem(Object item) {
			selected = item;
			fireContentsChanged(this, 0, classes.size());
		}
	}

	class CasterLevelsModel extends AbstractTableModel {
		static final public int MAX_ROWS = 7;

		CasterLevelsModel() {
			casterLevels.addPropertyListener(e -> {
				// TODO should fire more specific events - can at least tell if it's an update from a specific row as that'll have a CasterClass as the source
				//System.out.println("Update event from casterLevels: source = " + e.source);
				fireTableDataChanged();
			});
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			if (casterLevels.getCasterClassCount() >= MAX_ROWS) return MAX_ROWS;	// table is full
			return casterLevels.getCasterClassCount() + 1;
		}

		CasterClass getCasterClass(int row) {
			if (row < casterLevels.getCasterClassCount()) {
				return casterLevels.getCasterClass(row);
			}
			return null;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (row < casterLevels.getCasterClassCount()) {
				CasterClass casterClass = casterLevels.getCasterClass(row);
				if (col == 0)
					return casterClass.getCharacterClass();
				else if (col == 1)
					return casterClass.getCasterLevel();
				else if (col == 2)
					return casterClass.getAbilityScore();
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0)
				return CharacterClass.class;
			else if (col == 1 || col == 2)
				return Integer.class;
			return super.getColumnClass(col);
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0)
				return "Class";
			else if (col == 1)
				return "Level";
			else if (col == 2)
				return "Ability";
			return super.getColumnName(col);
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			CasterClass casterClass;
			if (row < casterLevels.getCasterClassCount()) {
				casterClass = casterLevels.getCasterClass(row);
				if (col == 1) {
					casterClass.setCasterLevel((Integer) value);
				} else if (col == 2) {
					casterClass.setAbilityScore((Integer) value);
				}
				fireTableDataChanged();
			} else {
				// editing a blank last row. this shouldn't happen if there are already MAX_ROWS rows in the table (as getSize() won't count a blank row)
				if (col != 0) return;	// shouldn't happen - only class is editable for new rows
				if (value == null) return;	// ignore new row with no class
				casterClass = casterLevels.new CasterClass((CharacterClass) value);
				casterLevels.addCasterClass(casterClass);
				classesComboModel.updateOptions();
				if (casterLevels.getCasterClassCount() < MAX_ROWS)
					fireTableRowsInserted(row + 1, row + 1);	// signal the table there is a new blank row, if one is needed
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (rowIndex < casterLevels.getCasterClassCount())
				return columnIndex != 0;	// class not editable for existing rows
			return columnIndex == 0;	// only class is editable for blank row
		}

	}
}

package ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import gamesystem.CasterLevels;
import gamesystem.CasterLevels.CasterClass;
import gamesystem.CharacterClass;
import gamesystem.core.SimplePropertyCollection;

// TODO fix bug where the editing an existing row's class, the combo box gets defaulted to the previously row's value instead of the current row's value

@SuppressWarnings("serial")
public class CasterPanel extends JPanel {
	CasterLevels casterLevels;
	CasterLevelsModel classesTableModel;
	CasterClassModel classesComboModel;
	JTabbedPane tabbedPane;
	Map<CharacterClass, List<SpellsPanel>> classTabs = new HashMap<>();
	SimplePropertyCollection propCollect = new SimplePropertyCollection();

	public CasterPanel() {
		casterLevels = new CasterLevels(propCollect);
		classesTableModel = new CasterLevelsModel();
		classesTableModel.addTableModelListener((e)->{
			// check tab config matches class levels
			Set<CharacterClass> currentClasses = new HashSet<>();
			for (CasterClass c : casterLevels.classes) {
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
						p = new SpellsPanel.PreparePanel(c);
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
						SpellsPanel.PreparePanel pp = new SpellsPanel.PreparePanel(c);
						tabs.add(pp);
						tabbedPane.addTab("Wizard", null, pp, "Wizard Spells Prepared");
					}
				}
			}

			// remove any tabs that are no longer required
			for (CharacterClass c : classTabs.keySet()) {
				List<SpellsPanel> tabs = classTabs.get(c);
				if (tabs != null && tabs.size() > 0 && !currentClasses.contains(c)) {
					for (JComponent t : tabs) {
						tabbedPane.remove(t);
					}
				}
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

		tabbedPane = new JTabbedPane();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		add(scroller, c);

		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		add(tabbedPane, c);
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

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			if (casterLevels.classes.size() >= MAX_ROWS) return MAX_ROWS;	// table is full
			return casterLevels.classes.size() + 1;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (row < casterLevels.classes.size()) {
				CasterClass casterClass = casterLevels.classes.get(row);
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
			if (row < casterLevels.classes.size()) {
				casterClass = casterLevels.classes.get(row);
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
				casterLevels.classes.add(casterClass);
				classesComboModel.updateOptions();
				if (casterLevels.classes.size() < MAX_ROWS)
					fireTableRowsInserted(row + 1, row + 1);	// signal the table there is a new blank row, if one is needed
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (rowIndex < casterLevels.classes.size())
				return columnIndex != 0;	// class not editable for existing rows
			return columnIndex == 0;	// only class is editable for blank row
		}

	}
}

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Character;
import party.Creature;
import party.Skill;

// TODO Add hitpoints and initiative
@SuppressWarnings("serial")
public class CharacterPanel extends JPanel implements PropertyChangeListener {
	protected Character character;

	protected JFormattedTextField baseSaveFields[] = new JFormattedTextField[3];
	protected JLabel saveModFields[] = new JLabel[3];
	protected JLabel saveTotalFields[] = new JLabel[3];

	public CharacterPanel(Character c) {
		character = c;
		character.addPropertyChangeListener(this);

		TableModel abilityModel = new AbilityTableModel();
		JTable abilityTable = new JTable(abilityModel);
		abilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		abilityTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		abilityTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane abilityScrollpane = new JScrollPane(abilityTable);

		TableModel acModel = new ACTableModel();
		JTable acTable = new JTable(acModel);
		acTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		acTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		acTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane acScrollpane = new JScrollPane(acTable);

		TableModel model = new SkillsTableModel();
		JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane skillsScrollpane = new JScrollPane(table);

		JPanel savesPanel = new JPanel();
		savesPanel.setLayout(new GridLayout(3,4));
		createSavesControls(Creature.SAVE_FORTITUDE,savesPanel);
		createSavesControls(Creature.SAVE_REFLEX,savesPanel);
		createSavesControls(Creature.SAVE_WILL,savesPanel);

		setLayout(new GridBagLayout());
		GridBagConstraints a = new GridBagConstraints();

		a.insets = new Insets(2, 3, 2, 3);
		a.fill = GridBagConstraints.BOTH;
		a.weightx = 1.0; a.weighty = 1.0;
		a.gridx = 0; a.gridy = 0; a.gridheight = 2;
		add(abilityScrollpane,a);
		a.gridx = 0; a.gridy = 2; a.gridheight = 1;
		add(acScrollpane,a);
		a.gridx = 1; a.gridy = 0; a.gridheight = 1;
		a.weightx = 1.0; a.weighty = 0.0;
		add(savesPanel,a);
		a.gridx = 1; a.gridy = 1; a.gridheight = 2;
		a.weightx = 1.0; a.weighty = 1.0;
		add(skillsScrollpane,a);

		setPreferredSize(new Dimension(1000,600));
	}

	protected  void createSavesControls(int type, JPanel panel) {
		panel.add(new JLabel(Creature.getSavingThrowName(type)));
		baseSaveFields[type] = new JFormattedTextField();
		baseSaveFields[type].setValue(new Integer(character.getBaseSavingThrow(type)));
		baseSaveFields[type].setColumns(3);
		baseSaveFields[type].addPropertyChangeListener("value", this);
		panel.add(baseSaveFields[type]);
		saveModFields[type] = new JLabel(""+character.getAbilityModifier(Creature.getSaveAbility(type)));
		panel.add(saveModFields[type]);
		saveTotalFields[type] = new JLabel(""+character.getSavingThrow(type));
		panel.add(saveTotalFields[type]);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (prop.equals("value")) {
			// one of the save controls has been modified
			for (int i = 0; i < 3; i++) {
				if (evt.getSource() == baseSaveFields[i]) {
					int total = (Integer)baseSaveFields[i].getValue();
					character.setSavingThrow(i, total);
				}
			}
			return;
		}

		if (prop.startsWith("ability")) {
			prop = prop.substring(7);
			for (int i = 0; i < 3; i++) {
				if (prop.equals(Creature.getAbilityName(Creature.getSaveAbility(i)))) {
					System.out.println("Ability "+prop+" modified for save "+Creature.getSavingThrowName(i));
					saveModFields[i].setText(""+character.getAbilityModifier(Creature.getSaveAbility(i)));
					saveTotalFields[i].setText(""+character.getSavingThrow(i));
				}
			}
		} else if (prop.startsWith("save")) {
			prop = prop.substring(4);
			for (int i = 0; i < 3; i++) {
				if (prop.equals(Creature.getSavingThrowName(i))) {
					baseSaveFields[i].setValue(new Integer(character.getBaseSavingThrow(i)));
					saveTotalFields[i].setText(""+character.getSavingThrow(i));
				}
			}
		}
	}

	protected class SkillsTableModel extends AbstractTableModel implements PropertyChangeListener {
		String[] skills;

		public SkillsTableModel() {
			skills = new String[character.getSkillNames().size()];
			character.getSkillNames().toArray(this.skills);
			character.addPropertyChangeListener(this);
			Arrays.sort(skills);
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return skills.length;
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) return skills[row];
			if (col == 1) {
				int ability = Skill.getAbilityForSkill(skills[row]);
				if (ability == -1) return null;
				return Creature.getAbilityName(ability);
			}
			if (col == 2) return character.getSkillRanks(skills[row]);
			if (col == 3) {
				int ability = Skill.getAbilityForSkill(skills[row]);
				if (ability == -1) return null;
				return character.getAbilityModifier(ability);
			}
			if (col == 4) return character.getSkill(skills[row]);
			return null;
		}

		public Class<?> getColumnClass(int col) {
			if (col == 0 || col == 1) return String.class;
			return Integer.class;
		}

		public String getColumnName(int col) {
			if (col == 0) return "Skill";
			if (col == 1) return "Ability";
			if (col == 2) return "Ranks";
			if (col == 3) return "Modifier";
			if (col == 4) return "Total";
			return super.getColumnName(col);
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 2) return true;
			return false;
		}

		public void setValueAt(Object arg0, int row, int col) {
			if (col == 2 && arg0 instanceof Integer) {
				character.setSkill(skills[row], ((Integer)arg0).intValue());
				return;
			}
			super.setValueAt(arg0, row, col);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().startsWith("skill")) {
				String skill = evt.getPropertyName().substring(5);
				// TODO this search is inefficient
				for (int i = 0; i < skills.length; i++) {
					if (skills[i].equals(skill)) {
						this.fireTableRowsUpdated(i, i);
					}
				}
			}
		}
	}

	protected class ACTableModel extends AbstractTableModel {
		String[] rows = {"Full AC","Touch","Flat-Footed"};

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// all AC components are editable, but the totals are not
			if (rowIndex >= rows.length) return true;
			return false;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (rowIndex < rows.length) return;
			if (value == null) value = new Integer(0);
			character.setACComponent(rowIndex-rows.length, (Integer)value);
			this.fireTableRowsUpdated(rowIndex, rowIndex);
			this.fireTableRowsUpdated(0, rows.length);
		}

		public String getColumnName(int column) {
			if (column == 0) return "Component";
			return "Value";
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return rows.length+Character.AC_MAX_INDEX;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) {
				if (row < rows.length) return rows[row];
				return Character.getACComponentName(row-rows.length);
			}
			if (row == 0) return character.getAC();
			if (row == 1) return character.getTouchAC();
			if (row == 2) return character.getFlatFootedAC();
			if (row >= rows.length) {
				return character.getACComponent(row-rows.length);
			}
			return null;
		}
	}

	protected class AbilityTableModel extends AbstractTableModel implements PropertyChangeListener {
		public AbilityTableModel() {
			character.addPropertyChangeListener(this);
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 1 || columnIndex == 2) return true;
			return false;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex != 1 && columnIndex != 2) return;
			if (value == null) value = new Integer(0);
			if (columnIndex == 1) character.setAbilityScore(rowIndex, (Integer)value);
			else if (columnIndex == 2) character.setTemporaryAbility(rowIndex, (Integer)value);
		}

		public String getColumnName(int column) {
			if (column == 0) return "Ability";
			if (column == 1) return "Score";
			if (column == 2) return "Current";
			if (column == 3) return "Mod";
			return super.getColumnName(column);
		}

		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return 6;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return Character.getAbilityName(row);
			if (column == 1) return character.getBaseAbilityScore(row);
			if (column == 2) {
				if (character.getAbilityScore(row) == character.getBaseAbilityScore(row)) return null;
				return character.getAbilityScore(row);
			}
			if (column == 3) return character.getAbilityModifier(row);
			return null;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			String abilityName = evt.getPropertyName();
			if (abilityName.startsWith("ability")) {
				abilityName = abilityName.substring(7);
				for (int i = 0; i < 6; i++) {
					if (Creature.getAbilityName(i).equals(abilityName)) {
						this.fireTableRowsUpdated(i, i);
					}
				}
			}
		}
	}
}

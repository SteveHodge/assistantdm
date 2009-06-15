package ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Character;
import party.Creature;
import swing.SpinnerCellEditor;

@SuppressWarnings("serial")
public class CharacterAbilityPanel extends JPanel {
	protected Character character;
	protected TableModel abilityModel;

	public CharacterAbilityPanel(Character c) {
		character = c;
		abilityModel = new AbilityTableModel();

		JTable abilityTable = new JTable(abilityModel);
		abilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		abilityTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		abilityTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane abilityScrollpane = new JScrollPane(abilityTable);

		setLayout(new BorderLayout());
		add(abilityScrollpane);
		setBorder(new TitledBorder("Ability Scores"));
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

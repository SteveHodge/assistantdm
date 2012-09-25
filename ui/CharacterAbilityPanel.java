package ui;

import gamesystem.AbilityScore;
import gamesystem.Modifier;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Character;
import party.Creature;
import swing.JTableWithToolTips;
import swing.SpinnerCellEditor;
import swing.TableModelWithToolTips;

// TODO handle editing of temp scores better. consider adding ability check column

@SuppressWarnings("serial")
public class CharacterAbilityPanel extends JPanel {
	protected Character character;
	protected TableModel abilityModel;

	public CharacterAbilityPanel(Character c) {
		character = c;
		abilityModel = new AbilityTableModel();

		JTable abilityTable = new JTableWithToolTips(abilityModel);
		abilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		abilityTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		abilityTable.setDefaultEditor(Integer.class, new SpinnerCellEditor() {
			public Component getTableCellEditorComponent(JTable table,
					Object value, boolean isSelected, int row, int column) {
				if (value == null) {
					value = character.getAbilityScore(row);
				}
				return super.getTableCellEditorComponent(table, value, isSelected, row, column);
			}
		});
		JScrollPane abilityScrollpane = new JScrollPane(abilityTable);

		setLayout(new BorderLayout());
		add(abilityScrollpane);
		setBorder(new TitledBorder("Ability Scores"));
	}

	protected class AbilityTableModel extends AbstractTableModel implements PropertyChangeListener, TableModelWithToolTips {
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
			if (column == 2) return "Override";
			if (column == 3) return "Current";
			if (column == 4) return "Mod";
			return super.getColumnName(column);
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return 6;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return AbilityScore.getAbilityName(row);
			if (column == 1) return character.getBaseAbilityScore(row);
			if (column == 2) {
				if (character.getTemporaryAbility(row) == -1) return null;
				return character.getAbilityScore(row);
			}
			if (column == 3) return character.getAbilityScore(row);
			if (column == 4) return character.getAbilityModifier(row);
			return null;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			String abilityName = evt.getPropertyName();
			if (abilityName.startsWith(Creature.PROPERTY_ABILITY_PREFIX)) {
				abilityName = abilityName.substring(Creature.PROPERTY_ABILITY_PREFIX.length());
				for (int i = 0; i < 6; i++) {
					if (AbilityScore.getAbilityName(i).equals(abilityName)) {
						this.fireTableRowsUpdated(i, i);
					}
				}
			}
		}

		public String getToolTipAt(int row, int col) {
			System.out.println("getToolTipAt("+row+",...)");
			AbilityScore s = (AbilityScore)character.getStatistic(Creature.STATISTIC_ABILITY[row]);
			StringBuilder text = new StringBuilder();
			text.append("<html><body>");
			if (s.getOverride() > 0) text.append("<s>");
			text.append(s.getBaseValue()).append(" base<br/>");
			Map<Modifier, Boolean> mods = s.getModifiers();
			for (Modifier m : mods.keySet()) {
				if (!mods.get(m)) text.append("<s>");
				text.append(m);
				if (!mods.get(m)) text.append("</s>");
				text.append("<br/>");
			}
			text.append(s.getValue()).append(" total ").append(s.getName());
			if (s.getOverride() > 0) {
				text.append("</s><br/>").append(s.getOverride()).append(" current ").append(s.getName()).append(" (override)");
			}
			text.append("<br/><br/>").append(s.getModifierValue()).append(" ").append(s.getName()).append(" modifier");
			text.append("</body></html>");
			return text.toString();
		}
	}
}

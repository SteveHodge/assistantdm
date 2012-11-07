package ui;

import gamesystem.AbilityScore;
import gamesystem.Modifier;
import gamesystem.Statistic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Character;
import party.Creature;
import swing.JTableWithToolTips;
import swing.SpinnerCellEditor;
import swing.TableModelWithToolTips;

// TODO handle editing of temp scores better. consider adding ability check column
// TODO modifier column should show "+" for non-negative modifiers

@SuppressWarnings("serial")
public class CharacterAbilityPanel extends CharacterSubPanel {
	protected TableModel abilityModel;

	public CharacterAbilityPanel(Character c) {
		super(c);
		summary = getSummary();

		character.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				String abilityName = evt.getPropertyName();
				if (abilityName.startsWith(Creature.PROPERTY_ABILITY_PREFIX)) {
					updateSummaries(getSummary());
				}
			}
		});

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

		setPreferredSize(new Dimension(450,130));
	}

	protected String getSummary() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			if (i > 0) s.append("   ");
			s.append(AbilityScore.ability_abbreviations[i]);
			s.append(" ");
			if (character.getAbilityModifier(i) >= 0) s.append("+");
			s.append(character.getAbilityModifier(i));
		}
		return s.toString();
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
			if (column == 3) {
				AbilityScore s = (AbilityScore)character.getStatistic(Creature.STATISTIC_ABILITY[row]);
				return s.getValue()+((s.hasConditionalModifier() && s.getOverride() == 0)?"*":"");
			}
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
			AbilityScore s = (AbilityScore)character.getStatistic(Creature.STATISTIC_ABILITY[row]);
			StringBuilder text = new StringBuilder();
			text.append("<html><body>");
			if (s.getOverride() > 0) text.append("<s>");
			text.append(s.getBaseValue()).append(" base<br/>");
			Map<Modifier, Boolean> mods = s.getModifiers();
			text.append(Statistic.getModifiersHTML(mods));
			text.append(s.getValue()).append(" total ").append(s.getName()).append("<br/>");
			String conds = Statistic.getModifiersHTML(mods, true);
			if (conds.length() > 0) text.append("<br/>").append(conds);

			if (s.getOverride() > 0) {
				text.append("</s><br/>").append(s.getOverride()).append(" current ").append(s.getName()).append(" (override)");
			}
			
			text.append("<br/>");
			if (s.getModifierValue() >= 0) text.append("+");
			text.append(s.getModifierValue()).append(" ").append(s.getName()).append(" modifier");
			text.append("</body></html>");
			return text.toString();
		}
	}
}

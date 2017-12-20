package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import gamesystem.AbilityScore;
import gamesystem.Creature;
import gamesystem.core.PropertyListener;
import gamesystem.core.SimpleProperty;
import party.Character;
import swing.JTableWithToolTips;
import swing.SpinnerCellEditor;
import swing.TableModelWithToolTips;

// TODO handle editing of temp scores better. consider adding ability check column
// TODO modifier column should show "+" for non-negative modifiers
// TODO review for change to enum AbilityScore.Type

@SuppressWarnings("serial")
class CharacterAbilityPanel extends CharacterSubPanel {
	private AbilityTableModel abilityModel;

	CharacterAbilityPanel(Character c) {
		super(c);
		summary = getSummary();

		character.addPropertyListener("ability_scores", (source, old) -> updateSummaries(getSummary()));

		abilityModel = new AbilityTableModel();

		final JTable abilityTable = new JTableWithToolTips(abilityModel);
		abilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		abilityTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		abilityTable.setDefaultEditor(Integer.class, new SpinnerCellEditor() {
			@Override
			public Component getTableCellEditorComponent(JTable table,
					Object value, boolean isSelected, int row, int column) {
				if (value == null) {
					value = abilityModel.getAbility(row).getValue();
				}
				return super.getTableCellEditorComponent(table, value, isSelected, row, column);
			}
		});
		abilityTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!SwingUtilities.isRightMouseButton(e)) return;
				int row = abilityTable.rowAtPoint(e.getPoint());
				String title = abilityModel.getAbilityName(row);
				String statName = abilityModel.getStatistic(row);
				StatisticInfoDialog dialog = new StatisticInfoDialog(CharacterAbilityPanel.this, title, character, statName);
				dialog.setVisible(true);
			}
		});
		JScrollPane abilityScrollpane = new JScrollPane(abilityTable);

		setLayout(new BorderLayout());
		add(abilityScrollpane);

		setPreferredSize(new Dimension(450,130));
	}

	private String getSummary() {
		StringBuilder s = new StringBuilder();
		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			if (s.length() > 0) s.append("   ");
			s.append(t.getAbbreviation());
			s.append(" ");
			int mod = character.getAbilityStatistic(t).getModifierValue();
			if (mod >= 0) s.append("+");
			s.append(mod);
		}
		return s.toString();
	}

	private class AbilityTableModel extends AbstractTableModel implements TableModelWithToolTips {
		public AbilityTableModel() {
			character.addPropertyListener("ability_scores", new PropertyListener<Integer>() {
				@Override
				public void propertyChanged(SimpleProperty<Integer> source, Integer oldValue) {
					if (source instanceof AbilityScore) {
						for (int i = 0; i < 6; i++) {
							if (AbilityScore.Type.values()[i].toString().equals(((AbilityScore) source).getDescription())) {
								fireTableRowsUpdated(i, i);
							}
						}
					}
				}
			});
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 1 || columnIndex == 2) return true;
			return false;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex != 1 && columnIndex != 2) return;
			if (value == null) value = new Integer(0);
			if (columnIndex == 1)
				getAbility(rowIndex).setBaseValue((Integer) value);
			else if (columnIndex == 2) {
				AbilityScore s = getAbility(rowIndex);
				int val = (Integer) value;
				if (val != s.getBaseValue() && val >= 0) {
					s.setOverride(val);
				} else {
					s.clearOverride();
				}
			}
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Ability";
			if (column == 1) return "Score";
			if (column == 2) return "Override";
			if (column == 3) return "Current";
			if (column == 4) return "Mod";
			return super.getColumnName(column);
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return 6;
		}

		private String getAbilityName(int row) {
			return AbilityScore.Type.values()[row].toString();
		}

		private String getStatistic(int row) {
			return Creature.STATISTIC_ABILITY[row];
		}

		public AbilityScore getAbility(int row) {
			return character.getAbilityStatistic(AbilityScore.Type.values()[row]);
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) return getAbilityName(row);
			if (column == 1) return getAbility(row).getBaseValue();
			if (column == 2) {
				if (getAbility(row).getOverride() == -1) return null;
				return getAbility(row).getValue();
			}
			if (column == 3) {
				AbilityScore s = getAbility(row);
				return s.getValue()+((s.hasConditionalModifier() && s.getOverride() == 0)?"*":"");
			}
			if (column == 4) return getAbility(row).getModifierValue();
			return null;
		}

		@Override
		public String getToolTipAt(int row, int col) {
			StringBuilder text = new StringBuilder();
			text.append("<html><body>").append(getAbility(row).getSummary()).append("</body></html>");
			return text.toString();
		}
	}
}

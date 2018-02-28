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
import gamesystem.core.Property;
import gamesystem.core.PropertyListener;
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
				public void propertyChanged(Property<Integer> source, Integer oldValue) {
					// this is a bit hackish as there is currently no good way to find the ability score or type from the drain and damage properties
					for (int i = 0; i < 6; i++) {
						AbilityScore a = getAbility(i);
						if (a == source || a.getDamage() == source || a.getDrain() == source) {
							fireTableRowsUpdated(i, i);
						}
					}
				}
			});
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex >= 1 && columnIndex <= 4;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex < 1 || columnIndex > 4) return;
			if (value == null) value = new Integer(0);
			AbilityScore a = getAbility(rowIndex);
			if (columnIndex == 1) {
				a.setBaseValue((Integer) value);
			} else if (columnIndex == 2) {
				if ((Integer) value < 0) value = new Integer(0);
				a.getDrain().setValue((Integer) value);
			} else if (columnIndex == 3) {
				if ((Integer) value < 0) value = new Integer(0);
				a.getDamage().setValue((Integer) value);
			} else if (columnIndex == 4) {
				int val = (Integer) value;
				if (val != a.getRegularValue() && val >= 0) {
					a.setOverride(val);
				} else {
					a.clearOverride();
				}
			}
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Ability";
			if (column == 1) return "Score";
			if (column == 2) return "Drain";
			if (column == 3) return "Damage";
			if (column == 4) return "Override";
			if (column == 5) return "Current";
			if (column == 6) return "Mod";
			return super.getColumnName(column);
		}

		@Override
		public int getColumnCount() {
			return 7;
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
			AbilityScore a = getAbility(row);
			if (column == 1) return a.getBaseValue() + a.getModifiersTotal();
			if (column == 2) return a.getDrain().getValue();
			if (column == 3) return a.getDamage().getValue();
			if (column == 4) {
				if (a.getOverride() == -1) return null;
				return a.getValue();
			}
			if (column == 5) {
				return a.getValue() + ((a.hasConditionalModifier() && a.getOverride() == 0) ? "*" : "");
			}
			if (column == 6) return a.getModifierValue();
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

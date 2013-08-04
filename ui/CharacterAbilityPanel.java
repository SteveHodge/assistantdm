package ui;

import gamesystem.AbilityScore;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import party.Character;
import party.Creature;
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

		character.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String abilityName = evt.getPropertyName();
				if (abilityName.startsWith(Creature.PROPERTY_ABILITY_PREFIX)) {
					updateSummaries(getSummary());
				}
			}
		});

		abilityModel = new AbilityTableModel();

		final JTable abilityTable = new JTableWithToolTips(abilityModel);
		abilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		abilityTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		abilityTable.setDefaultEditor(Integer.class, new SpinnerCellEditor() {
			@Override
			public Component getTableCellEditorComponent(JTable table,
					Object value, boolean isSelected, int row, int column) {
				if (value == null) {
					value = character.getAbilityScore(AbilityScore.Type.values()[row]);
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
			if (character.getAbilityModifier(t) >= 0) s.append("+");
			s.append(character.getAbilityModifier(t));
		}
		return s.toString();
	}

	private class AbilityTableModel extends AbstractTableModel implements PropertyChangeListener, TableModelWithToolTips {
		public AbilityTableModel() {
			character.addPropertyChangeListener(this);
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
			if (columnIndex == 1) character.setAbilityScore(AbilityScore.Type.values()[rowIndex], (Integer)value);
			else if (columnIndex == 2) character.setTemporaryAbility(AbilityScore.Type.values()[rowIndex], (Integer)value);
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

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) return getAbilityName(row);
			if (column == 1) return character.getBaseAbilityScore(AbilityScore.Type.values()[row]);
			if (column == 2) {
				if (character.getTemporaryAbility(AbilityScore.Type.values()[row]) == -1) return null;
				return character.getAbilityScore(AbilityScore.Type.values()[row]);
			}
			if (column == 3) {
				AbilityScore s = (AbilityScore) character.getStatistic(Creature.STATISTIC_ABILITY[row]);
				return s.getValue()+((s.hasConditionalModifier() && s.getOverride() == 0)?"*":"");
			}
			if (column == 4) return character.getAbilityModifier(AbilityScore.Type.values()[row]);
			return null;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String abilityName = evt.getPropertyName();
			if (abilityName.startsWith(Creature.PROPERTY_ABILITY_PREFIX)) {
				abilityName = abilityName.substring(Creature.PROPERTY_ABILITY_PREFIX.length());
				for (int i = 0; i < 6; i++) {
					if (AbilityScore.Type.values()[i].toString().equals(abilityName)) {
						this.fireTableRowsUpdated(i, i);
					}
				}
			}
		}

		@Override
		public String getToolTipAt(int row, int col) {
			AbilityScore s = (AbilityScore) character.getStatistic(Creature.STATISTIC_ABILITY[row]);
			StringBuilder text = new StringBuilder();
			text.append("<html><body>").append(s.getSummary()).append("</body></html>");
			return text.toString();
		}
	}
}

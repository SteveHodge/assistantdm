package ui;

import gamesystem.AC;
import gamesystem.Modifier;
import gamesystem.Statistic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import party.Character;
import party.Creature;
import swing.SpinnerCellEditor;

// TODO flag conditional modifiers for touch and flatfooted
@SuppressWarnings("serial")
public class CharacterACPanel extends CharacterSubPanel implements PropertyChangeListener {
	protected TableModel acModel;
	protected JLabel totalLabel;
	protected JLabel touchLabel;
	protected JLabel flatLabel;

	public CharacterACPanel(Character c) {
		super(c);
		summary = getSummary();
		acModel = new ACTableModel();

		JTable acTable = new JTable(acModel);
		acTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		acTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		acTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		acTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
			Color old = null;
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (column == 1 && !acModel.isCellEditable(row, column)) {
					if (old == null) old = cell.getBackground();
					cell.setBackground(Color.LIGHT_GRAY);
				} else {
					if (old != null) cell.setBackground(old);
				}
				return cell;
			}
		});
		JScrollPane acScrollpane = new JScrollPane(acTable);
		acScrollpane.setPreferredSize(new Dimension(450,200));

		AC ac = (AC)character.getStatistic(Creature.STATISTIC_AC);
		totalLabel = new JLabel("Total AC: "+ac.getValue()+(ac.hasConditionalModifier()?"*":""));
		touchLabel = new JLabel("Touch AC: "+ac.getTouchAC().getValue()+(ac.getTouchAC().hasConditionalModifier()?"*":""));
		flatLabel = new JLabel("Flat-footed AC: "+ac.getFlatFootedAC().getValue()+(ac.getFlatFootedAC().hasConditionalModifier()?"*":""));
		updateToolTips();

		setLayout(new GridBagLayout());
		GridBagConstraints a = new GridBagConstraints();

		a.insets = new Insets(2, 3, 2, 3);
		a.fill = GridBagConstraints.BOTH;
		a.weightx = 1.0; a.weighty = 1.0;
		a.gridx = 0; a.gridy = 0; a.gridheight = 4;
		add(acScrollpane,a);

		a.gridx = 1; a.gridy = 0; a.gridheight = 1;
		a.weightx = 0.0; a.weighty = 0.0;
		a.fill = GridBagConstraints.NONE;
		add(totalLabel,a);
		a.gridx = 1; a.gridy = 1; a.gridheight = 1;
		add(touchLabel,a);
		a.gridx = 1; a.gridy = 2; a.gridheight = 1;
		add(flatLabel,a);

		character.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(Creature.PROPERTY_AC)) {
			AC ac = (AC)character.getStatistic(Creature.STATISTIC_AC);
			totalLabel.setText("Total AC: "+ac.getValue()+(ac.hasConditionalModifier()?"*":""));
			touchLabel.setText("Touch AC: "+ac.getTouchAC().getValue()+(ac.getTouchAC().hasConditionalModifier()?"*":""));
			flatLabel.setText("Flat-footed AC: "+ac.getFlatFootedAC().getValue()+(ac.getFlatFootedAC().hasConditionalModifier()?"*":""));
			updateToolTips();
			updateSummaries(getSummary());
		}
	}

	protected String getSummary() {
		StringBuilder s = new StringBuilder();
		s.append("AC ").append(character.getAC());
		s.append("   Touch ").append(character.getTouchAC());
		s.append("   Flat-footed ").append(character.getFlatFootedAC());
		return s.toString();
	}

	protected void updateToolTips() {
		AC ac = (AC)character.getStatistic(Creature.STATISTIC_AC);

		Map<Modifier, Boolean> mods = ac.getModifiers();
		StringBuilder text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getAC()).append(" total");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		totalLabel.setToolTipText(text.toString());

		mods = ac.getTouchAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getTouchAC()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		touchLabel.setToolTipText(text.toString());

		mods = ac.getFlatFootedAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getFlatFootedAC()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		flatLabel.setToolTipText(text.toString());
	}

	protected class ACTableModel extends AbstractTableModel implements PropertyChangeListener {
		// need a row for each custom modifier plus a row for each other modifier on the AC
		Modifier[] modifiers;
		boolean[] active;

		public ACTableModel() {
			character.addPropertyChangeListener(Creature.PROPERTY_AC, this);
			updateModifiers();
		}

		protected void updateModifiers() {
			AC ac = (AC)character.getStatistic(Creature.STATISTIC_AC);
			Map<Modifier, Boolean> mods = ac.getModifiers();
			ArrayList<Modifier> list = new ArrayList<Modifier>();
			for (Modifier m : mods.keySet()) {
				if (m.getSource() == null || !m.getSource().equals("user set")) {
					list.add(m);
				}
			}
			modifiers = list.toArray(new Modifier[list.size()]);
			Arrays.sort(modifiers, new Comparator<Modifier>() {
				public int compare(Modifier a, Modifier b) {
					// TODO should compare source and condition as well
					String aStr = a.getType() == null ? "" : a.getType();
					String bStr = b.getType() == null ? "" : b.getType();
					return aStr.compareTo(bStr);
				}
			});
			active = new boolean[modifiers.length];
			for (int i=0; i<modifiers.length; i++) {
				active[i] = mods.get(modifiers[i]);
			}

			//System.out.println("Have "+modifiers.length+" modifiers");

			fireTableDataChanged();
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) return false;
			//if (rowIndex == Character.ACComponentType.DEX.ordinal()) return false;
			if (rowIndex >= Character.ACComponentType.values().length) return false;
			return true;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (!isCellEditable(rowIndex, columnIndex)) return;
			if (value == null) value = new Integer(0);
			character.setACComponent(Character.ACComponentType.values()[rowIndex], (Integer)value);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}

		public String getColumnName(int column) {
			if (column == 0) return "Component";
			return "Value";
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return Character.ACComponentType.values().length + modifiers.length;
		}

		public Object getValueAt(int row, int column) {
			if (row < Character.ACComponentType.values().length) {
				if (column == 0) return Character.ACComponentType.values()[row].toString();
				return character.getACComponent(Character.ACComponentType.values()[row]);
			} else {
				row -= Character.ACComponentType.values().length;
				if (column == 0) {
					String s = modifiers[row].getType();
					if (s == null) s = modifiers[row].getModifier() >= 0 ? "Bonus" : "Penalty";
					if (modifiers[row].getSource() != null) s += " (from "+modifiers[row].getSource()+")";
					if (modifiers[row].getCondition() != null) s += " ("+modifiers[row].getCondition()+")";
					if (!active[row]) s += " (inactive)";
					return s;
				}
				return modifiers[row].getModifier();
			}
		}

		public void propertyChange(PropertyChangeEvent evt) {
			//if (evt.getPropertyName().equals(Character.PROPERTY_AC)) {	// assumed
			updateModifiers();
			//}
		}
	}
}

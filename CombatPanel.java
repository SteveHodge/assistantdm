import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import party.Character;
import party.Party;
import swing.ReorderableList;

// TODO consider moving initiative reset and next round buttons out of their current panels

@SuppressWarnings("serial")
public class CombatPanel extends JPanel {
	Party party;

	public CombatPanel(Party p) {
		party = p;

		final InitiativeListModel ilm = new InitiativeListModel(party);
		JLayeredPane initiativeList = new ReorderableList(ilm);
		JScrollPane listScroller = new JScrollPane(initiativeList);

		JPanel initiativePanel = new JPanel();
		initiativePanel.setBorder(BorderFactory.createTitledBorder("Combat"));
		initiativePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		JButton resetInitButton = new JButton("Reset");
		resetInitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ilm.reset();
			}
		});
		initiativePanel.add(resetInitButton, c);

		c.gridy = 1;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		initiativePanel.add(listScroller, c);

		EffectListModel m = new EffectListModel();
		ReorderableList effectsList = new ReorderableList(m);
		JScrollPane effectsScroller = new JScrollPane(effectsList);

		JPanel effectsPanel = new JPanel();
		effectsPanel.setBorder(BorderFactory.createTitledBorder("Temporary Effects"));
		effectsPanel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		effectsPanel.add(new NewEffectPanel(p, m, ilm), c);

		c.gridy = 1;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		effectsPanel.add(effectsScroller, c);

		ACModel acmodel = new ACModel();
		JTable acTable = new JTable(acmodel);
		acTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane acScroller = new JScrollPane(acTable);
		Dimension d = acTable.getPreferredSize();
		d.height += 20;
		acScroller.setPreferredSize(d);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initiativePanel, effectsPanel);
		splitPane.setOneTouchExpandable(true);

		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, acScroller);
		splitPane2.setOneTouchExpandable(true);

		setLayout(new BorderLayout());
		add(splitPane2, BorderLayout.CENTER);
	}

	public String getCharacterName(int index) {
		return party.get(index).getName();
	}

//	class HPModel extends AbstractTableModel {
//		String[] columns = {"Name","HP","Wounds","Non-lethal","Current"};
//
//		public HPModel() {
//		}
//
//		public boolean isCellEditable(int rowIndex, int columnIndex) {
//			// HP, wounds, and non-lethal are editable
//			if (columnIndex >= 1 && columnIndex <= 3) return true;
//			return false;
//		}
//
//		public Class<?> getColumnClass(int columnIndex) {
//			if (columnIndex == 0) return String.class;
//			return Integer.class;
//		}
//
//		public void setValueAt(Object value, int rowIndex, int columnIndex) {
//			if (value == null) value = new Integer(0);
//			if (columnIndex == 1) {
//				party.get(rowIndex).setMaximumHitPoints((Integer)value);
//			} else if (columnIndex == 2) {
//				party.get(rowIndex).setWounds((Integer)value);
//			} else if (columnIndex == 3) {
//				party.get(rowIndex).setNonLethal((Integer)value);
//			} else {
//				return;
//			}
//			this.fireTableRowsUpdated(rowIndex, rowIndex);
//		}
//
//		public String getColumnName(int column) {
//			return columns[column];
//		}
//
//		public int getColumnCount() {
//			return 5;
//		}
//
//		public int getRowCount() {
//			return party.size();
//		}
//
//		public Object getValueAt(int row, int column) {
//			if (column == 0) return getCharacterName(row);
//			if (column == 1) return party.get(row).getMaximumHitPoints();
//			if (column == 2) return party.get(row).getWounds();
//			if (column == 3) return party.get(row).getNonLethal();			
//			if (column == 4) return party.get(row).getHPs();
//			return null;
//		}
//	}

	class ACModel extends AbstractTableModel {
		String[] columns = {"Name","AC","Touch","Flat-Footed"};

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// all AC components are editable, but the totals are not
			if (columnIndex >= columns.length) return true;
			return false;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex < columns.length) return;
			if (value == null) value = new Integer(0);
			party.get(rowIndex).setACComponent(columnIndex-columns.length, (Integer)value);
			this.fireTableRowsUpdated(rowIndex, rowIndex);
		}

		public String getColumnName(int column) {
			if (column < columns.length) return columns[column];
			return Character.getACComponentName(column-columns.length);
		}

		public int getColumnCount() {
			return 4+Character.AC_MAX_INDEX;
		}

		public int getRowCount() {
			return party.size();
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return getCharacterName(row);
			if (column == 1) return party.get(row).getAC();
			if (column == 2) return party.get(row).getTouchAC();
			if (column == 3) return party.get(row).getFlatFootedAC();
			if (column >= columns.length) {
				return party.get(row).getACComponent(column-columns.length);
			}
			return null;
		}
	}
}

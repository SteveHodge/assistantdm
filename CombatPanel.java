import java.awt.BorderLayout;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import party.Character;
import party.Party;
import swing.ReorderableList;


@SuppressWarnings("serial")
public class CombatPanel extends JPanel {
	Party party;

	public CombatPanel(Party p) {
		party = p;
		InitiativeListModel ilm = new InitiativeListModel(party);
		JLayeredPane initiativePanel = new ReorderableList(ilm);
		JScrollPane listScroller = new JScrollPane(initiativePanel);

		EffectListModel m = new EffectListModel();
		m.addEntry("Entry 1");
		m.addEntry("Entry 2");
		m.addEntry("Entry 3");
		m.addEntry("Entry 4");
		ReorderableList effectsPanel = new ReorderableList(m);
		JScrollPane effectsScroller = new JScrollPane(effectsPanel);

		HPModel model = new HPModel();
		JTable hpTable = new JTable(model);
		hpTable.setDefaultEditor(Integer.class, new SpinnerCellEditor(0));
		JScrollPane hpScroller = new JScrollPane(hpTable);
		hpScroller.setPreferredSize(hpTable.getPreferredSize());

		ACModel acmodel = new ACModel();
		JTable acTable = new JTable(acmodel);
		acTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane acScroller = new JScrollPane(acTable);
		acScroller.setPreferredSize(acTable.getPreferredSize());

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroller, effectsScroller);
		splitPane.setOneTouchExpandable(true);

		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, hpScroller, acScroller);
		splitPane2.setOneTouchExpandable(true);

		JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, splitPane2);
		splitPane3.setOneTouchExpandable(true);

		setLayout(new BorderLayout());
		add(splitPane3, BorderLayout.CENTER);
	}

	public String getCharacterName(int index) {
		return party.get(index).getName();
	}

	class HPModel extends AbstractTableModel {
		String[] columns = {"Name","HP","Wounds","Non-lethal","Current"};

		public HPModel() {
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// HP, wounds, and non-lethal are editable
			if (columnIndex >= 1 && columnIndex <= 3) return true;
			return false;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (value == null) value = new Integer(0);
			if (columnIndex == 1) {
				party.get(rowIndex).setFullHPs((Integer)value);
			} else if (columnIndex == 2) {
				party.get(rowIndex).setWounds((Integer)value);
			} else if (columnIndex == 3) {
				party.get(rowIndex).setNonLethal((Integer)value);
			} else {
				return;
			}
			this.fireTableRowsUpdated(rowIndex, rowIndex);
		}

		public String getColumnName(int column) {
			return columns[column];
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return party.size();
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return getCharacterName(row);
			if (column == 1) return party.get(row).getFullHPs();
			if (column == 2) return party.get(row).getWounds();
			if (column == 3) return party.get(row).getNonLethal();			
			if (column == 4) return party.get(row).getHPs();
			return null;
		}
	}

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

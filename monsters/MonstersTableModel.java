package monsters;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class MonstersTableModel extends DefaultTableModel {
	public enum Column {
		NAME("Name"),
		SIZE("Size"),
		TYPE("Type"),
		ENVIRONMENT("Environment"),
		CR("CR"),
		SOURCE("Source");

		@Override
		public String toString() {
			return name;
		}

		private Column(String n) {
			name = n;
		}

		private String name;
	}

	MonstersTableModel() {
		MonsterLibrary.instance.addListener(new ListDataListener() {
			@Override
			public void contentsChanged(ListDataEvent e) {
				fireTableDataChanged();
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				fireTableRowsInserted(e.getIndex0(), e.getIndex1());
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
			}
		});
	}

	MonsterEntry getMonsterEntry(int index) {
		return MonsterLibrary.instance.monsters.get(index);
	}

	@Override
	public int getColumnCount() {
		return Column.values().length;
	}

	@Override
	public String getColumnName(int column) {
		if (column >= 0 && column < getColumnCount()) {
			return Column.values()[column].toString();
		}
		return super.getColumnName(column);
	}

	@Override
	public int getRowCount() {
		return MonsterLibrary.instance.monsters.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column >= 0 && column < getColumnCount()) {
			Column col = Column.values()[column];
			MonsterEntry e = MonsterLibrary.instance.monsters.get(row);
			switch (col) {
			case NAME:
				return e.name;
			case SIZE:
				return e.size;
			case TYPE:
				return e.type;
			case ENVIRONMENT:
				return e.environment;
			case CR:
				return e.cr;
			case SOURCE:
				return e.source;
			}
		}

		return super.getValueAt(row, column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}

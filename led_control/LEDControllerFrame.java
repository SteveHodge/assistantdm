package led_control;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import party.Character;
import party.Party;

// TODO need to handle adding/removing characters
// TODO switch between character hp display and other effects
// TODO save config

@SuppressWarnings("serial")
public class LEDControllerFrame extends JFrame {
	HitPointLEDController controller;
	Party party;

	public LEDControllerFrame(HitPointLEDController con, Party party) {
		controller = con;
		this.party = party;

		setTitle("LED Controller");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final PlayerRegionTableModel regionsModel = new PlayerRegionTableModel();
		final JTable regionsTable = new JTable(regionsModel);
		regionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		regionsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		JScrollPane regionsScrollpane = new JScrollPane(regionsTable);

		add(regionsScrollpane);

		pack();
	}

	class PlayerRegionTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return party.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Character chr = party.get(row);
			Region r = controller.regionMap.get(chr);
			if (col > 0 && r == null) return null;
			switch (col) {
			case 0:
				return chr.getName();
			case 1:
				return r.id;
			case 2:
				return r.start;
			case 3:
				return r.count;
			case 4:
				return r.enabled;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 4) return Boolean.class;
			if (col == 0) return String.class;
			return Integer.class;
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Player";
			case 1:
				return "ID";
			case 2:
				return "Start";
			case 3:
				return "Count";
			case 4:
				return "Enabled";
			}
			return super.getColumnName(col);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex != 0) return true;
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int row, int col) {
			Character chr = party.get(row);
			Region r = controller.regionMap.get(chr);
			if (r == null) {
				r = controller.addCharacter(chr);
			}
			switch (col) {
			case 1:
				r.id = (Integer) aValue;
				break;
			case 2:
				r.start = (Integer) aValue;
				break;
			case 3:
				r.count = (Integer) aValue;
				break;
			case 4:
				r.enabled = (Boolean) aValue;
				break;
			}
			fireTableRowsUpdated(row, row);
			controller.updateCharacter(chr, true);
		}
	}

}

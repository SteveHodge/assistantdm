package ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Party;
import party.XP;

public class XPEntryPanel extends JPanel {
	Party party;

	public XPEntryPanel(Party party) {
		this.party = party;

		TableModel model = new XPPartyTableModel();
		JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		table.getColumnModel().getColumn(0).setPreferredWidth(200);
//		table.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane partyScrollpane = new JScrollPane(table);

		setLayout(new BorderLayout());
		add(partyScrollpane);
		setBorder(new TitledBorder("Party"));
	}

	protected class XPPartyTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return 7;
		}

		public int getRowCount() {
			return party.size();
		}

		public Object getValueAt(int row, int col) {
			party.Character c = party.get(row);

			switch (col) {
			case 0:
				return null;	// checkbox
			case 1:
				return c.getName();	// name - String
			case 2:
				return c.getLevel();	// level - integer
			case 3:
				return c.getXP();	// xp - integer
			case 4:
				return 0;	// earned - integer
			case 5:
				return 100.0f*c.getXP()/XP.getXPRequired(c.getLevel()+1);	// % through level - float
			case 6:
				return 0.0f;	// % penalty - float
			default:
				return null;
			}
		}

		public Class<?> getColumnClass(int col) {
			if (col == 0) return Boolean.class;
			if (col == 1) return String.class;
			if (col == 5 || col == 6) return Float.class;
			return Integer.class;
		}

		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Include?";
			case 1:
				return "Name";
			case 2:
				return "Level";
			case 3:
				return "XP";
			case 4:
				return "Earned";
			case 5:
				return "% through level";
			case 6:
				return "% penalty";
			default:
				return super.getColumnName(col);
			}
		}
	}
}

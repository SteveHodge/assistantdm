package ui;

import java.awt.BorderLayout;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import party.XP.Challenge;
import party.XP.XPChange;
import party.XP.XPChangeChallenges;

//TODO track character changes

@SuppressWarnings("serial")
public class XPHistoryPanel extends JPanel {
	public XPHistoryPanel(List<XPChange> changes) {
		XPHistoryTableModel model = new XPHistoryTableModel(changes);
		final JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(80);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(500);

		JScrollPane scrollpane = new JScrollPane(table);

		setLayout(new BorderLayout());
		add(scrollpane);
	}

	protected class XPHistoryTableModel extends AbstractTableModel {
		public List<XPChange> changes;
		int[] totals;
		Format format = new SimpleDateFormat("yyyy-MM-dd");

		public XPHistoryTableModel(List<XPChange> changes) {
			this.changes = changes;
			totals = new int[changes.size()];
			int total = 0;
			int i = 0;
			for (XPChange xp : changes) {
				total += xp.getXP();
				totals[i++] = total;
			}
		}

		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return changes.size();
		}

		public Object getValueAt(int row, int col) {
			XPChange xp = changes.get(row);
			if (col == 0) {
				return xp.getXP();
			} else if (col == 1) {
				return totals[row];
			} else if (col == 2) {
				return format.format(xp.getDate());
			} else if (col == 3) {
				String comment = xp.getComment();
				if (xp instanceof XPChangeChallenges) {
					XPChangeChallenges chals = (XPChangeChallenges)xp;
					for (Challenge c : chals.challenges) {
						if (comment == null || comment.length() == 0) {
							comment = c.toString();
						} else {
							comment += ", " + c;
						}
					}
				}
				return comment;
			}
			return null;
		}

		public Class<?> getColumnClass(int col) {
			if (col < 2) return Integer.class;
			return String.class;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public String getColumnName(int column) {
			if (column == 0) return "Earned";
			if (column == 1) return "Total";
			if (column == 2) return "Date";
			if (column == 3) return "Comments";
			return super.getColumnName(column);
		}
	}
}

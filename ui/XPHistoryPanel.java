package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import party.Character.XPHistoryItem;
import party.XP.Challenge;
import party.Character;

@SuppressWarnings("serial")
public class XPHistoryPanel extends JPanel implements ListSelectionListener, ActionListener {
	JButton deleteButton;
	JButton moveUpButton;
	JButton moveDownButton;
	JTable table;
	Character character;
	XPHistoryTableModel model;

	static final int COLUMN_EARNED = 0;
	static final int COLUMN_TOTAL = 1;
	static final int COLUMN_LEVEL = 2;
	static final int COLUMN_LVL_OK = 3;
	static final int COLUMN_LVL_UP = 4;
	static final int COLUMN_DATE = 5;
	static final int COLUMN_COMMENTS = 6;
	static final int COLUMN_COUNT = 7;

	public XPHistoryPanel(Character c) {
		character = c;
		model = new XPHistoryTableModel();
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(COLUMN_EARNED).setPreferredWidth(80);
		table.getColumnModel().getColumn(COLUMN_TOTAL).setPreferredWidth(80);
		table.getColumnModel().getColumn(COLUMN_LEVEL).setPreferredWidth(60);
		table.getColumnModel().getColumn(COLUMN_LVL_OK).setPreferredWidth(40);
		table.getColumnModel().getColumn(COLUMN_LVL_UP).setPreferredWidth(40);
		table.getColumnModel().getColumn(COLUMN_DATE).setPreferredWidth(100);
		table.getColumnModel().getColumn(COLUMN_COMMENTS).setPreferredWidth(500);
		table.getSelectionModel().addListSelectionListener(this);

		JScrollPane scrollpane = new JScrollPane(table);

		deleteButton = new JButton("Delete");
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(this);
		moveUpButton = new JButton("Move Up");
		moveUpButton.setEnabled(false);
		moveUpButton.addActionListener(this);
		moveDownButton = new JButton("Move Down");
		moveDownButton.setEnabled(false);
		moveDownButton.addActionListener(this);

		setLayout(new BorderLayout());
		add(scrollpane);
		JPanel buttons = new JPanel();
		buttons.add(deleteButton);
		buttons.add(moveUpButton);
		buttons.add(moveDownButton);
		add(buttons,BorderLayout.NORTH);
	}

	public void actionPerformed(ActionEvent e) {
		int index = table.getSelectedRow();	// TODO should convert to model index space
		if (e.getSource() == deleteButton) {
			character.deleteXPHistory(index);
			model.fireTableDataChanged();
		} else if (e.getSource() == moveUpButton) {
			character.moveXPHistory(index, index-1);
			model.fireTableDataChanged();
			table.getSelectionModel().setSelectionInterval(index-1, index-1);
		} else if (e.getSource() == moveDownButton) {
			character.moveXPHistory(index, index+1);
			model.fireTableDataChanged();
			table.getSelectionModel().setSelectionInterval(index+1, index+1);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		if (lsm.isSelectionEmpty()) {
			deleteButton.setEnabled(false);
			moveUpButton.setEnabled(false);
			moveDownButton.setEnabled(false);
		} else {
			deleteButton.setEnabled(true);
			int index = table.getSelectedRow();
			moveUpButton.setEnabled(index > 0);
			moveDownButton.setEnabled(index < model.getRowCount()-1);
		}
	}

	protected class XPHistoryTableModel extends AbstractTableModel {
		Format format = new SimpleDateFormat("yyyy-MM-dd");

		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		public int getRowCount() {
			return character.getXPHistoryCount();
		}

		public Object getValueAt(int row, int col) {
			XPHistoryItem xpItem = character.getXPHistory(row);
			if (col == COLUMN_EARNED) {
				if (xpItem.isLevelChange()) return null;
				return xpItem.getXP();
			} else if (col == COLUMN_TOTAL) {
				return xpItem.getTotal();
			} else if (col == COLUMN_LEVEL) {
				return xpItem.getLevel();
			} else if (col == COLUMN_LVL_OK) {
				return xpItem.isValidLevel();
			} else if (col == COLUMN_LVL_UP) {
				return xpItem.canLevelUp();
			} else if (col == COLUMN_DATE) {
				return format.format(xpItem.getDate());
			} else if (col == COLUMN_COMMENTS) {
				String comment = xpItem.getComment();
				for (Challenge c : xpItem.getChallenges()) {
					if (comment == null || comment.length() == 0) {
						comment = c.toString();
					} else {
						comment += ", " + c;
					}
				}
				if (xpItem.isLevelChange()) {
					if (comment == null || comment.length() == 0) {
						comment = "Level change from "+xpItem.getOldLevel()+" to "+xpItem.getLevel();
					} else {
						comment += ", level change from "+xpItem.getOldLevel()+" to "+xpItem.getLevel();
					}
				}
				return comment;
			}
			return null;
		}

		public Class<?> getColumnClass(int col) {
			if (col == COLUMN_EARNED || col == COLUMN_TOTAL || col == COLUMN_LEVEL) return Integer.class;
			if (col == COLUMN_LVL_OK || col == COLUMN_LVL_UP) return Boolean.class;
			return String.class;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public String getColumnName(int column) {
			if (column == COLUMN_EARNED) return "Earned";
			if (column == COLUMN_TOTAL) return "Total";
			if (column == COLUMN_LEVEL) return "Level";
			if (column == COLUMN_DATE) return "Date";
			if (column == COLUMN_COMMENTS) return "Comments";
			if (column == COLUMN_LVL_OK) return "Level OK";
			if (column == COLUMN_LVL_UP) return "Can Level Up";
			return super.getColumnName(column);
		}
	}
}

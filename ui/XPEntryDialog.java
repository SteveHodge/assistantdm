package ui;

import gamesystem.CR;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import party.Party;
import party.XP;
import party.XP.Challenge;

@SuppressWarnings("serial")
public class XPEntryDialog extends JDialog implements ActionListener {
	Party party;
	List<Challenge> challenges = new ArrayList<Challenge>();
	int[] penalties;	// note this is in party order
	boolean[] exclude;	// note this is in party order
	XPPartyTableModel partyModel;
	XPEntryTableModel entryModel;
	boolean returnOk = false;
	JTextField commentsField;
	JFormattedTextField dateField;
	SpinnerNumberModel partySizeModel;
	static Date lastDate = new Date();

	public XPEntryDialog(JFrame frame, Party p) {
		super(frame, "Enter challenges", true);

		party = p;
		penalties = new int[party.size()];
		exclude = new boolean[party.size()];

		partyModel = new XPPartyTableModel();
		JTable table = new JTable(partyModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
//		table.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane partyScrollpane = new JScrollPane(table);

		entryModel = new XPEntryTableModel();
		entryModel.addTableModelListener(partyModel);
		table = new JTable(entryModel); 
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane entryScrollpane = new JScrollPane(table);

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		c.weightx = 1.0; c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(partyScrollpane,c);
		c.gridx = 1;
		panel.add(entryScrollpane,c);

		JPanel buttons = new JPanel();
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttons.add(okButton);
		buttons.add(cancelButton);

		JPanel commentsPanel = new JPanel();
		commentsField = new JTextField(95);
		commentsPanel.add(new JLabel("Comment: "));
		commentsPanel.add(commentsField);
		dateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
		dateField.setToolTipText("Expects date as 'yyyy-mm-dd'");
		dateField.setValue(lastDate);
		commentsPanel.add(new JLabel("Date: "));
		commentsPanel.add(dateField);
		partySizeModel = new SpinnerNumberModel(party.size(),party.size(),99,1);
		partySizeModel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				partyModel.fireTableDataChanged();
			}
		});
		commentsPanel.add(new JLabel("Party Size:"));
		commentsPanel.add(new JSpinner(partySizeModel));

		add(commentsPanel,"North");
		add(panel);
		add(buttons,"South");
		pack();
		setLocationRelativeTo(frame);	// FIXME this works on laptop but not on desktop (due to multiple screens?)
	}

	public boolean isCancelled() {
		return !returnOk;
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("Ok")) {
			returnOk = true;
		}

		setVisible(false);
	}

	public int getPartySize() {
		return partySizeModel.getNumber().intValue();
	}

	public void applyXPEarned() {
		int i = 0;
		lastDate = (Date)dateField.getValue();
		for (party.Character c : party) {
			if (!exclude[i]) {
				c.addXPChallenges(getPartySize(), penalties[i], challenges, commentsField.getText(), lastDate);
			}
			i++;
		}
	}

	protected class XPEntryTableModel extends AbstractTableModel {
		public static final int COLUMN_COMMENT = 0;
		public static final int COLUMN_CR = 1;
		public static final int COLUMN_NUMBER = 2;
		public static final int COLUMN_COUNT = 3;

		protected Challenge entry;

		public XPEntryTableModel() {
			entry = new Challenge();
			entry.number = 1;
		}

		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		public int getRowCount() {
			return challenges.size()+1;
		}

		public Object getValueAt(int row, int col) {
			Challenge c = entry;
			if (row < challenges.size()) {
				c = challenges.get(row);
			}
			switch (col) {
			case COLUMN_COMMENT:
				return c.comment;
			case COLUMN_CR:
				return c.cr;
			case COLUMN_NUMBER:
				return c.number;
			default:
				return null;
			}
		}

		public Class<?> getColumnClass(int col) {
			if (col == COLUMN_COMMENT) return String.class;
			if (col == COLUMN_CR) return CR.class;
			return Integer.class;
		}

		public String getColumnName(int col) {
			switch (col) {
			case COLUMN_COMMENT:
				return "Comment";
			case COLUMN_CR:
				return "CR";
			case COLUMN_NUMBER:
				return "#";
			default:
				return super.getColumnName(col);
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			Challenge c = entry;
			if (row < challenges.size()) {
				c = challenges.get(row);
			}
			switch (col) {
			case COLUMN_COMMENT:
				c.comment = (String)value;
				break;
			case COLUMN_CR:
				c.cr = (CR)value;
				break;
			case COLUMN_NUMBER:
				if (value == null) c.number = 0;
				else c.number = (Integer)value;
				break;
			}
			if (c == entry) {
				// setup new blank row
				challenges.add(c);
				entry = new Challenge();
				entry.number = 1;
				fireTableRowsInserted(challenges.size(), challenges.size());
			} else {
				fireTableCellUpdated(row, col);
			}
		}
	}

	protected class XPPartyTableModel extends AbstractTableModel implements TableModelListener {
		public static final int COLUMN_NAME = 0;
		public static final int COLUMN_LEVEL = 1;
		public static final int COLUMN_XP = 2;
		public static final int COLUMN_EARNED = 3;
		public static final int COLUMN_TOGO = 4;
		public static final int COLUMN_PENALTY = 5;
		public static final int COLUMN_EXCLUDE = 6;

		public static final int COLUMN_COUNT = 7;

		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		public int getRowCount() {
			return party.size();
		}

		public Object getValueAt(int row, int col) {
			party.Character c = party.get(row);

			switch (col) {
			case COLUMN_NAME:
				return c.getName();	// name - String
			case COLUMN_LEVEL:
				return c.getLevel();	// level - integer
			case COLUMN_XP:
				return c.getXP();	// xp - integer
			case COLUMN_EARNED:
				if (exclude[row]) return 0;
				return XP.getXP(c.getLevel(), getPartySize(), penalties[row], challenges); // earned - integer
			case COLUMN_TOGO:
				int xp = c.getXP();
				if (!exclude[row]) xp += XP.getXP(c.getLevel(), getPartySize(), penalties[row], challenges);
//				int levelXP = XP.getXPRequired(c.getLevel());
//				return 100.0f*(xp-levelXP)/(XP.getXPRequired(c.getLevel()+1)-levelXP);	// % through level - float
				return ((float)xp-XP.getXPRequired(c.getLevel()))/(c.getLevel()*10);	// % through level - float
			case COLUMN_PENALTY:
				return penalties[row];	// % penalty - float
			case COLUMN_EXCLUDE:
				return exclude[row];
			default:
				return null;
			}
		}

		public Class<?> getColumnClass(int col) {
			if (col == COLUMN_NAME) return String.class;
			if (col == COLUMN_TOGO) return Float.class;
			if (col == COLUMN_EXCLUDE) return Boolean.class;
			return Integer.class;
		}

		public String getColumnName(int col) {
			switch (col) {
			case COLUMN_NAME:
				return "Name";
			case COLUMN_LEVEL:
				return "Level";
			case COLUMN_XP:
				return "XP";
			case COLUMN_EARNED:
				return "Earned";
			case COLUMN_TOGO:
				return "% through level";
			case COLUMN_PENALTY:
				return "% penalty";
			case COLUMN_EXCLUDE:
				return "Exclude";
			default:
				return super.getColumnName(col);
			}
		}

		public void tableChanged(TableModelEvent arg0) {
			this.fireTableDataChanged();
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == COLUMN_PENALTY || columnIndex == COLUMN_EXCLUDE) return true;
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			if (col == COLUMN_PENALTY) {
				if (value == null) penalties[row] = 0;
				else penalties[row] = (Integer)value;
				this.fireTableCellUpdated(row, COLUMN_EARNED);
				this.fireTableCellUpdated(row, COLUMN_TOGO);
			} else if (col == COLUMN_EXCLUDE) {
				if (value == null) value = false;
				boolean newVal = (Boolean)value;

				int delta = 0;
				if (exclude[row] && !newVal) delta = 1;
				if (!exclude[row] && newVal) delta = -1;

				exclude[row] = newVal;

				// adjust the minimum and current values of the party size spinner to match the change is party size:
				partySizeModel.setValue(new Integer(partySizeModel.getNumber().intValue()+delta));
				partySizeModel.setMinimum(new Integer(((Number)partySizeModel.getMinimum()).intValue()+delta));

				this.fireTableDataChanged();
			}
		}
	}
}

package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import party.Character;

@SuppressWarnings("serial")
public class SelectDiffsDialog extends JDialog implements ActionListener {
	List<String> diffs;
	Character oldChar, newChar;
	Map<String,Boolean> selected; 
	boolean returnOk = false;

	public SelectDiffsDialog(JFrame frame, Character oldChar, Character newChar) {
		super(frame, "Select attributes to update for "+oldChar.getName(), true);

		diffs = oldChar.getDifferences(newChar);

		this.oldChar = oldChar;
		this.newChar = newChar;
		selected = new HashMap<String,Boolean>();
		for (String diff : diffs) {
			if (!diff.equals(Character.PROPERTY_NAME)
				&& !diff.equals(Character.PROPERTY_WOUNDS)
				&& !diff.equals(Character.PROPERTY_NONLETHAL)
				&& !diff.startsWith(Character.PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
				selected.put(diff,true);
			}
		}

		JTable diffsTable = new JTable(new DiffsTableModel());
		//acTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//acTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		//acTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane diffsScrollPane = new JScrollPane(diffsTable);

		JPanel buttons = new JPanel();
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttons.add(okButton);
		buttons.add(cancelButton);

		add(diffsScrollPane);
		add(buttons,"South");
		pack();
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

	public List<String> getSelectedDiffs() {
		ArrayList<String> select = new ArrayList<String>();
		for (String prop : diffs) {
			Boolean sel = selected.get(prop);
			if (sel != null && sel) select.add(prop);
		}
		return select;
	}

	// TODO should listen to property change event on the two characters
	public class DiffsTableModel extends AbstractTableModel {
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 3) return true;
			return false;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			if (columnIndex == 3) return Boolean.class;
			return Object.class;
		}

		public String getColumnName(int column) {
			if (column == 0) return "Attribute";
			if (column == 1) return "Current";
			if (column == 2) return "New Vallue";
			if (column == 3) return "Update?";
			return super.getColumnName(column);
		}

		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return diffs.size();
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return diffs.get(row);
			if (column == 1) return oldChar.getProperty(diffs.get(row));
			if (column == 2) return newChar.getProperty(diffs.get(row));
			if (column == 3) {
				Boolean sel = selected.get(diffs.get(row));
				if (sel != null) return sel;
				return false;
			}
			return null;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex == 3 && value instanceof Boolean) {
				selected.put(diffs.get(rowIndex), (Boolean)value);
			} else {
				super.setValueAt(value, rowIndex, columnIndex);
			}
		}
	}
}

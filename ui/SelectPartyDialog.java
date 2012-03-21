package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import party.Character;
import party.CharacterLibrary;
import party.Party;

//TODO figure out how to position the dialog so it is centered on the frame
@SuppressWarnings("serial")
public class SelectPartyDialog extends JDialog implements ActionListener {
	protected LibraryTableModel libraryModel;
	protected Party party;

	Map<Character,Boolean> selected; 
	boolean returnOk = false;

	public SelectPartyDialog(JFrame frame, Party p) {
		super(frame, "Select party members", true);

		party = p;
		libraryModel = new LibraryTableModel();
		selected = new HashMap<Character,Boolean>();
		for (Character c : libraryModel.list) {
			if (party.contains(c)) {
				selected.put(c,true);
			}
		}

		JTable characterTable = new JTable(libraryModel);
		characterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		characterTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		JScrollPane scrollpane = new JScrollPane(characterTable);

		JPanel buttons = new JPanel();
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttons.add(okButton);
		buttons.add(cancelButton);

		add(scrollpane);
		add(buttons,"South");
		pack();
		setLocationRelativeTo(frame);
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

	public List<Character> getSelectedCharacters() {
		ArrayList<Character> select = new ArrayList<Character>();
		for (Character c : libraryModel.list) {
			Boolean sel = selected.get(c);
			if (sel != null && sel) select.add(c);
		}
		return select;
	}

	protected class LibraryTableModel extends AbstractTableModel /*implements PropertyChangeListener*/ {
		List<Character> list;

		public LibraryTableModel() {
			list = new ArrayList<Character>(CharacterLibrary.characters);
			Comparator<Character> sorter = new Comparator<Character>() {
				public int compare(Character arg0,Character arg1) {
					return arg0.getName().compareTo(arg1.getName());
				}
			};
			Collections.sort(list,sorter );
		}

		public int getColumnCount() {
			return 3;
		}

		public String getColumnName(int column) {
			if (column == 0) return "Name";
			if (column == 1) return "Level";
			if (column == 2) return "In Party?";
			return super.getColumnName(column);
		}

		public int getRowCount() {
			return list.size();
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 2) return true;
			return false;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			if (columnIndex == 2) return Boolean.class;
			return Integer.class;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex != 2) return;
			Character c = list.get(rowIndex);
			selected.put(c, (Boolean)value);
		}

		public Object getValueAt(int row, int column) {
			Character c = list.get(row);
			if (column == 0) return c.getName();
			if (column == 1) return c.getLevel();
			if (column == 2) {
				if (selected.containsKey(c)) return selected.get(c);
				else return false;
			}
			return null;
		}
	}
}

package ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Character;
import swing.SpinnerCellEditor;

//TODO: the ac table model in this class does not listen to property changes. It should.

@SuppressWarnings("serial")
public class CharacterACPanel extends JPanel {
	protected Character character;
	protected TableModel acModel;

	public CharacterACPanel(Character c) {
		character = c;
		acModel = new ACTableModel();

		JTable acTable = new JTable(acModel);
		acTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		acTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		acTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane acScrollpane = new JScrollPane(acTable);

		setLayout(new BorderLayout());
		add(acScrollpane);
		setBorder(new TitledBorder("Armor Class"));
	}

	protected class ACTableModel extends AbstractTableModel {
		protected String[] rows = {"Full AC","Touch","Flat-Footed"};

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// all AC components are editable, but the totals are not
			if (rowIndex >= rows.length) return true;
			return false;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (rowIndex < rows.length) return;
			if (value == null) value = new Integer(0);
			character.setACComponent(rowIndex-rows.length, (Integer)value);
			this.fireTableRowsUpdated(rowIndex, rowIndex);
			this.fireTableRowsUpdated(0, rows.length);
		}

		public String getColumnName(int column) {
			if (column == 0) return "Component";
			return "Value";
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return rows.length+Character.AC_MAX_INDEX;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) {
				if (row < rows.length) return rows[row];
				return Character.getACComponentName(row-rows.length);
			}
			if (row == 0) return character.getAC();
			if (row == 1) return character.getTouchAC();
			if (row == 2) return character.getFlatFootedAC();
			if (row >= rows.length) {
				return character.getACComponent(row-rows.length);
			}
			return null;
		}
	}
}

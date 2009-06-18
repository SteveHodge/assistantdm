package ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Character;
import swing.SpinnerCellEditor;

@SuppressWarnings("serial")
public class CharacterACPanel extends JPanel implements PropertyChangeListener {
	protected Character character;
	protected TableModel acModel;
	protected JLabel totalLabel;
	protected JLabel touchLabel;
	protected JLabel flatLabel;

	public CharacterACPanel(Character c) {
		character = c;
		acModel = new ACTableModel();

		JTable acTable = new JTable(acModel);
		acTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		acTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		acTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane acScrollpane = new JScrollPane(acTable);

		totalLabel = new JLabel("Total AC: "+c.getAC());
		touchLabel = new JLabel("Touch AC: "+c.getTouchAC());
		flatLabel = new JLabel("Flat-footed AC: "+c.getFlatFootedAC());

		setLayout(new GridBagLayout());
		GridBagConstraints a = new GridBagConstraints();

		a.insets = new Insets(2, 3, 2, 3);
		a.fill = GridBagConstraints.BOTH;
		a.weightx = 1.0; a.weighty = 1.0;
		a.gridx = 0; a.gridy = 0; a.gridheight = 4;
		add(acScrollpane,a);

		a.gridx = 1; a.gridy = 0; a.gridheight = 1;
		a.weightx = 0.0; a.weighty = 0.0;
		a.fill = GridBagConstraints.NONE;
		add(totalLabel,a);
		a.gridx = 1; a.gridy = 1; a.gridheight = 1;
		add(touchLabel,a);
		a.gridx = 1; a.gridy = 2; a.gridheight = 1;
		add(flatLabel,a);

		setBorder(new TitledBorder("Armor Class"));

		character.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals("ac")) {
			totalLabel.setText("Total AC: "+character.getAC());
			touchLabel.setText("Touch AC: "+character.getTouchAC());
			flatLabel.setText("Flat-footed AC: "+character.getFlatFootedAC());
		}
	}

	protected class ACTableModel extends AbstractTableModel implements PropertyChangeListener {
		public ACTableModel() {
			character.addPropertyChangeListener(Character.PROPERTY_AC, this);
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (value == null) value = new Integer(0);
			character.setACComponent(rowIndex, (Integer)value);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}

		public String getColumnName(int column) {
			if (column == 0) return "Component";
			return "Value";
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return Character.AC_MAX_INDEX;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return Character.getACComponentName(row);
			return character.getACComponent(row);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			//if (evt.getPropertyName().equals(Character.PROPERTY_AC)) {	// assumed
				fireTableDataChanged();
			//}
		}
	}
}

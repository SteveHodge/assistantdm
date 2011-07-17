package swing;

import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import party.CharacterLibrary;

@SuppressWarnings("serial")
public class JTableWithToolTips extends JTable {
	public JTableWithToolTips() {
		super();
	}

	public JTableWithToolTips(int arg0, int arg1) {
		super(arg0, arg1);
	}

	public JTableWithToolTips(Object[][] arg0, Object[] arg1) {
		super(arg0, arg1);
	}

	public JTableWithToolTips(TableModel arg0, TableColumnModel arg1, ListSelectionModel arg2) {
		super(arg0, arg1, arg2);
	}

	public JTableWithToolTips(TableModel arg0, TableColumnModel arg1) {
		super(arg0, arg1);
	}

	public JTableWithToolTips(TableModel arg0) {
		super(arg0);
	}

	public JTableWithToolTips(Vector arg0, Vector arg1) {
		super(arg0, arg1);
	}

	public String getToolTipText(MouseEvent e) {
		String tip = null;
		java.awt.Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		int realColumnIndex = convertColumnIndexToModel(colIndex);

		TableModel m = getModel();
		if (m instanceof TableModelWithToolTips) {
			tip = ((TableModelWithToolTips)m).getToolTipAt(rowIndex, realColumnIndex);
		}

		if (tip == null) {
			tip = super.getToolTipText(e);
		}
		return tip;
	}
}

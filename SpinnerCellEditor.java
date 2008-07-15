import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
	JSpinner spinner;

	public SpinnerCellEditor() {
		spinner = new JSpinner();
	}

	public SpinnerCellEditor(int min) {
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMinimum(min);
		spinner = new JSpinner(model);
	}

	public Object getCellEditorValue() {
		return spinner.getValue();
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (value == null) {
			value = 0;
		}
		spinner.setValue(value);
		return spinner;
	}
}

package combat;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class DurationCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	int value;
	JButton button;
	SelectTimeDialog dialog = null;

	public DurationCellEditor() {
		button = new JButton();
        button.setActionCommand("edit");
        button.addActionListener(this);
        button.setBorderPainted(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("edit")) {
			dialog.setValue(value);
			dialog.setVisible(true);
			if (!dialog.isCancelled()) {
				value = dialog.getValue();
			}
			fireEditingStopped();
		}
	}

	@Override
	public Object getCellEditorValue() {
		return value;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.value = ((Integer)value).intValue();
		if (dialog == null) {
			dialog = new SelectTimeDialog(SwingUtilities.getWindowAncestor(table));
		}
		return button;
	}

}

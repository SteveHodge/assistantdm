package monsters;

import javax.swing.table.TableModel;

public interface FilterableTableModel<T> extends TableModel {
	public T getRowObject(int rowIndex);
}

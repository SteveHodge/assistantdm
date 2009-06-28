package monsters;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/* Proxy that filters a source TableModel. This design requires the source TableModel to be able to
 * provide an object that represents a row. This requirement is encapsulated in the interface
 * FilterableTableModel. Filters are used for actually matching rows.
 * 
 * An alternative design would be for the Filter to be given the source TableModel and row index and
 * then let the Filter query the model directly. This would remove the requirement on the TableModel
 * class to implement FilterableTableModel. But it would mean that the filter would need to be aware
 * of the specifics of what each column was (i.e. Filter implementation would be tied closely to the
 * source TableModel's implementation rather than the intermediate row object as it is in currently). 
 */
public class FilterTableModel<T> implements TableModel, TableModelListener, FilterableTableModel<T> {
	protected List<Filter<T>> filters = new ArrayList<Filter<T>>();
	protected FilterableTableModel<T> source;
	protected int[] sourceMap;		// maps source index to filtered index. -1 means hidden
	protected int[] filteredMap;	// maps filtered index to source index - this only needs to be filteredRows long,
									// but we set it up to be the same size as sourceMap for convenience
	protected int filteredRows;		// sourceMap and filteredMap should contain this many values >= 0
	protected EventListenerList listenerList = new EventListenerList();

	public FilterTableModel(FilterableTableModel<T> s) {
		source = s;
		source.addTableModelListener(this);
		sourceMap = new int[source.getRowCount()];
		filteredRows = source.getRowCount();
		filteredMap = new int[filteredRows];
		// set up identity mapping:
		for (int i=0; i<filteredRows; i++) {
			sourceMap[i] = i;
			filteredMap[i] = i;
		}
	}

	public void addFilter(Filter<T> f) {
		f.addFilterTableModel(this);
		filters.add(f);
		filter();
	}

	public void removeFilter(Filter<T> f) {
		f.removeFilterTableModel(this);
		filters.remove(f);
		filter();
	}

	// TODO need to handle structure changes, be more specific when data updates, and map row indexes if forwarding the original event
	public void tableChanged(TableModelEvent e) {
		filter();
		// Forward the event. This is not necessary at the moment because currently filter()
		// always sends a full table update event
		//fireTableChanged(e.getFirstRow(), e.getLastRow(), e.getColumn(), e.getType());
	}

	public void addTableModelListener(TableModelListener l) {
		listenerList.add(TableModelListener.class, l);
	}

	public void removeTableModelListener(TableModelListener l) {
		listenerList.remove(TableModelListener.class, l);
	}

	// TODO implement smart algorithm
	// smart implementation possible: if row X is not currently mapped and this filter doesn't match the
	// row then we don't need to check other filters: it doesn't match. Similarly if row X is mapped and
	// it does match this filter then we don't also don't need to check other filters: it does match.
	public void filterChanged(Filter<T> filter) {
		filter();
	}

	// currently handles on AND of filters
	// TODO add OR mode
	protected void filter() {
		// do a complete filtering
		filteredRows = 0;
		for (int i=0; i<source.getRowCount(); i++) {
			T rowObj = source.getRowObject(i);
			boolean match = true;
			for (Filter<T> f : filters) {
				match &= f.matches(rowObj);
				if (!match) break;
			}
			if (match) {
				sourceMap[i] = filteredRows;
				filteredMap[filteredRows] = i;
				filteredRows++;
			} else {
				sourceMap[i] = -1;
			}
		}
		// blanks any excess filteredMap entries
		for (int i=filteredRows; i<source.getRowCount(); i++) {
			filteredMap[i] = -1;
		}

		fireTableChanged(0, Integer.MAX_VALUE, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
	}

	protected void fireTableChanged(int first, int last, int col, int type) {
		Object[] listeners = listenerList.getListenerList();
		TableModelEvent evt = null;
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==TableModelListener.class) {
				if (evt == null)
					evt = new TableModelEvent(this,first,last,col,type);
				((TableModelListener)listeners[i+1]).tableChanged(evt);
			}
		}
	}

	public Class<?> getColumnClass(int arg0) {
		return source.getColumnClass(arg0);
	}

	public int getColumnCount() {
		return source.getColumnCount();
	}

	public String getColumnName(int arg0) {
		return source.getColumnName(arg0);
	}

	public int getRowCount() {
		return filteredRows;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return source.getValueAt(filteredMap[rowIndex], columnIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return source.isCellEditable(filteredMap[rowIndex], columnIndex);
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		source.setValueAt(aValue, filteredMap[rowIndex], columnIndex);
	}

	public T getRowObject(int rowIndex) {
		return source.getRowObject(filteredMap[rowIndex]);
	}
}

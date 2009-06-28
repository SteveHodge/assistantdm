package monsters;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFilter<T> implements Filter<T> {
	protected List<FilterTableModel<T>> models = new ArrayList<FilterTableModel<T>>();

	public void addFilterTableModel(FilterTableModel<T> m) {
		models.add(m);
	}

	public void removeFilterTableModel(FilterTableModel<T> m) {
		models.remove(m);
	}

	protected void notifyModels() {
		for (FilterTableModel<T> m : models) {
			m.filterChanged(this);
		}
	}
}

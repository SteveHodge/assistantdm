package monsters;

public interface Filter<T> {
	public boolean matches(T object);
	// TODO we can later convert this to a standard event/listener interface
	public void addFilterTableModel(FilterTableModel<T> m);
	public void removeFilterTableModel(FilterTableModel<T> m);
}

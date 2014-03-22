package swing;
import java.awt.Component;
import java.util.Comparator;

import javax.swing.ListModel;


public interface ReorderableListModel<T extends Component> extends ListModel<T> {
	public void sort();				// sort list by default ordering

	public void sort(Comparator<Object> c);	// sort list using supplied comparator

	public void moveTo(Component item, int newPos);

	public int indexOf(Component item);
}

package swing;
import java.util.Comparator;

import javax.swing.ListModel;


public interface ReorderableListModel extends ListModel {
	public void sort();				// sort list by default ordering
	public void sort(Comparator<Object> c);	// sort list using supplied comparator
	public void moveTo(Object item, int newPos);
	public int indexOf(Object item);
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JLabel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import swing.ReorderableListEntry;
import swing.ReorderableListModel;


public class EffectListModel implements ReorderableListModel {
	List<Object> list = new ArrayList<Object>();

	EventListenerList listenerList = new EventListenerList();

	public void moveTo(Object item, int newPos) {
		if (!list.remove(item)) throw new NoSuchElementException();
		list.add(newPos,item);
		fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,0,list.size()-1);
	}

	public void sort() {
		Collections.sort(list, new Comparator<Object>() {
			public int compare(Object arg0, Object arg1) {
				return list.indexOf(arg0) - list.indexOf(arg1);
			}
		});
		fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,0,list.size()-1);
	}

	public void sort(Comparator<Object> c) {
		Collections.sort(list,c);
		fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,0,list.size()-1);
	}

	public Object getElementAt(int index) {
		return list.get(index);
	}

	public int getSize() {
		return list.size();
	}

	public void addEntry(String string) {
		ReorderableListEntry e = new ReorderableListEntry(string);
		list.add(e);
		fireListDataEvent(ListDataEvent.INTERVAL_ADDED,list.size()-1,list.size()-1);
	}

	public void addListDataListener(ListDataListener l) {
		listenerList.add(ListDataListener.class,l);
	}

	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(ListDataListener.class,l);
	}

	protected void fireListDataEvent(int type, int index0, int index1) {
		ListDataEvent e = null;
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ListDataListener.class) {
				if (e == null) e = new ListDataEvent(this, type, index0, index1);
				switch(type) {
				case ListDataEvent.CONTENTS_CHANGED:
					((ListDataListener)listeners[i+1]).contentsChanged(e);
					break;
				case ListDataEvent.INTERVAL_ADDED:
					((ListDataListener)listeners[i+1]).intervalAdded(e);
					break;
				case ListDataEvent.INTERVAL_REMOVED:
					((ListDataListener)listeners[i+1]).intervalRemoved(e);
					break;
				}
			}
		}
	}

	public int indexOf(Object item) {
		return list.indexOf(item);
	}
}

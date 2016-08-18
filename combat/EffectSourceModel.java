package combat;

import javax.swing.ComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

class EffectSourceModel implements ComboBoxModel<Object> {
	private InitiativeListModel initiativeModel;
	private String selected = "";
	private EventListenerList listenerList = new EventListenerList();

	EffectSourceModel(InitiativeListModel ilm) {
		initiativeModel = ilm;
		ilm.addListDataListener(new ListDataListener() {
			// forward list changes to listeners
			@Override
			public void contentsChanged(ListDataEvent e) {
				fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,e.getIndex0(),e.getIndex1());
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				fireListDataEvent(ListDataEvent.INTERVAL_ADDED,e.getIndex0(),e.getIndex1());
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				fireListDataEvent(ListDataEvent.INTERVAL_REMOVED,e.getIndex0(),e.getIndex1());
			}
		});
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}

	@Override
	public void setSelectedItem(Object arg0) {
		if (arg0 != null) selected = arg0.toString();
		else selected = "";
		fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,-1,-1);

	}

	@Override
	public Object getElementAt(int arg0) {
		return initiativeModel.getElementAt(arg0).getCreatureName();
	}

	@Override
	public int getSize() {
		return initiativeModel.getSize();
	}

	int getInitiative(int index) {
		CombatEntry e = initiativeModel.getElementAt(index);
		return e.getTotal();
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listenerList.add(ListDataListener.class,l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(ListDataListener.class,l);
	}

	private void fireListDataEvent(int type, int index0, int index1) {
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
}
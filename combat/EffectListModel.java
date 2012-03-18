package combat;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import swing.ReorderableListModel;

public class EffectListModel implements ReorderableListModel {
	List<EffectEntry> list = new ArrayList<EffectEntry>();

	EventListenerList listenerList = new EventListenerList();

	// these members track the maximum preferred/minimum sizes for the labels in the EffectEntrys
	int effectWidth = 0;
	int sourceWidth = 0;
	int initiativeWidth = 0;
	int durationWidth = 0;

	public void moveTo(Object item, int newPos) {
		if (!list.remove(item)) throw new NoSuchElementException();
		list.add(newPos,(EffectEntry)item);
		fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,0,list.size()-1);
	}

	public void sort() {
		Collections.sort(list, new Comparator<EffectEntry>() {
			public int compare(EffectEntry arg0, EffectEntry arg1) {
				int diff = arg0.duration - arg1.duration;
				if (diff == 0) return arg0.initiative - arg1.initiative;
				return diff;
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

	public void addEntry(String effect, String source, int initiative, int duration) {
		EffectEntry e = new EffectEntry(this, effect, source, initiative, duration);
		addEntry(e);
		fireListDataEvent(ListDataEvent.INTERVAL_ADDED,list.size()-1,list.size()-1);
	}

	protected void addEntry(EffectEntry e) {
		list.add(e);
		Dimension d = e.effectLabel.getPreferredSize();
		if (d.width > effectWidth) {
			//System.out.println("Larger effect width: "+d.width);
			effectWidth = d.width;
			for (EffectEntry ent : list) {
				d = ent.effectLabel.getPreferredSize();
				d.width = effectWidth;
				ent.effectLabel.setPreferredSize(d);
				ent.invalidate();
			}
		} else {
			d.width = effectWidth;
			e.effectLabel.setPreferredSize(d);
		}

		d = e.sourceLabel.getPreferredSize();
		if (d.width > sourceWidth) {
			//System.out.println("Larger source width: "+d.width);
			sourceWidth = d.width;
			for (EffectEntry ent : list) {
				d = ent.sourceLabel.getPreferredSize();
				d.width = sourceWidth;
				ent.sourceLabel.setPreferredSize(d);
				ent.invalidate();
			}
		} else {
			d.width = sourceWidth;
			e.sourceLabel.setPreferredSize(d);
		}

		d = e.initiativeLabel.getPreferredSize();
		if (d.width > initiativeWidth) {
			//System.out.println("Larger initiative width: "+d.width);
			initiativeWidth = d.width;
			for (EffectEntry ent : list) {
				d = ent.initiativeLabel.getPreferredSize();
				d.width = initiativeWidth;
				ent.initiativeLabel.setPreferredSize(d);
				ent.invalidate();
			}
		} else {
			d.width = initiativeWidth;
			e.initiativeLabel.setPreferredSize(d);
		}

		d = e.durationLabel.getPreferredSize();
		if (d.width > durationWidth) {
			//System.out.println("Larger duration width: "+d.width);
			durationWidth = d.width;
			for (EffectEntry ent : list) {
				d = ent.durationLabel.getPreferredSize();
				d.width = durationWidth;
				ent.durationLabel.setPreferredSize(d);
				ent.invalidate();
			}
		} else {
			d.width = durationWidth;
			e.durationLabel.setPreferredSize(d);
		}
	}

	public void clear() {
		int last = list.size()-1;
		list.clear();
		fireListDataEvent(ListDataEvent.INTERVAL_REMOVED,0,last);
	}

	protected void removeEntry(EffectEntry e) {
		int pos = list.indexOf(e);
		list.remove(e);
		fireListDataEvent(ListDataEvent.INTERVAL_REMOVED,pos,pos);
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

	public void parseDOM(Element el) {
		if (!el.getNodeName().equals("EffectList")) return;
		clear();
		int oldSize = list.size();

		NodeList nodes = el.getChildNodes();
		if (nodes != null) {
			for (int i=0; i<nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
				Element e = (Element)nodes.item(i);
				String tag = e.getTagName();
				if (tag.equals("EffectEntry")) {
					addEntry(EffectEntry.parseDOM(this,e));
				}
			}
		}
		if (list.size() != oldSize) {
			fireListDataEvent(ListDataEvent.INTERVAL_ADDED,oldSize,list.size()-1);
		}
	}

	public String getXML(String indent, String nextIndent) {
		StringBuilder b = new StringBuilder();
		String nl = System.getProperty("line.separator");
		b.append(indent).append("<EffectList>").append(nl);
		for(EffectEntry e : list) {
			b.append(e.getXML(indent+nextIndent,nextIndent));
		}
		b.append(indent).append("</EffectList>").append(nl);
		return b.toString();
	}
}

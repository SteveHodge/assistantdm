package party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import gamesystem.ItemDefinition;
import gamesystem.core.AbstractProperty;
import gamesystem.core.PropertyEvent;

// event parameters:
// "added_at" -> index that item(s) were added at
// "count" -> the number of items added or removed. may be missing if it's unknown how many elements were involved
// "removed_at" -> index that item(s) were removed from (-1 means unknown which items were removed)
// "changed_at" -> index that item(s) were changed at (-1 means unknown which items changed)

public class Inventory extends AbstractProperty<Inventory> implements List<ItemDefinition> {
	List<ItemDefinition> items = new ArrayList<>();

	public Inventory(Character c) {
		super("inventory", c);
	}

	@Override
	public Inventory getValue() {
		return this;
	}

	// ------------------------------------- List interface -------------------------------------
	// TODO the default list/collection methods (e.g. replaceAll) will not report events properly

	@Override
	public boolean add(ItemDefinition item) {
		boolean ret = items.add(item);
		if (ret) {
			PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
			e.set("added_at", items.size() - 1);
			e.set("count", 1);
			fireEvent(e);
		}
		return ret;
	}

	@Override
	public void add(int index, ItemDefinition item) {
		items.add(index, item);
		PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
		e.set("added_at", index);
		e.set("count", 1);
		fireEvent(e);
	}

	@Override
	public boolean addAll(Collection<? extends ItemDefinition> c) {
		int index = items.size();
		boolean ret = items.addAll(c);
		if (ret) {
			PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
			e.set("added_at", index);
			e.set("count", items.size() - index);
			fireEvent(e);
		}
		return ret;
	}

	@Override
	public boolean addAll(int index, Collection<? extends ItemDefinition> c) {
		int size = items.size();
		boolean ret = items.addAll(index, c);
		if (ret) {
			PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
			e.set("added_at", index);
			e.set("count", items.size() - size);
			fireEvent(e);
		}
		return ret;
	}

	@Override
	public void clear() {
		int size = items.size();
		items.clear();
		PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
		e.set("removed_at", 0);
		e.set("count", size);
		fireEvent(e);
	}

	@Override
	public boolean contains(Object item) {
		return items.contains(item);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return items.containsAll(c);
	}

	@Override
	public ItemDefinition get(int index) {
		return items.get(index);
	}

	@Override
	public int indexOf(Object item) {
		return items.indexOf(item);
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	// TODO should return read-only iterator or an iterator that generates events
	@Override
	public Iterator<ItemDefinition> iterator() {
		return items.iterator();
	}

	@Override
	public int lastIndexOf(Object item) {
		return items.lastIndexOf(item);
	}

	// TODO should return read-only iterator or an iterator that generates events
	@Override
	public ListIterator<ItemDefinition> listIterator() {
		return items.listIterator();
	}

	// TODO should return read-only iterator or an iterator that generates events
	@Override
	public ListIterator<ItemDefinition> listIterator(int index) {
		return items.listIterator(index);
	}

	@Override
	public boolean remove(Object item) {
		boolean ret = items.remove(item);
		if (ret) {
			PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
			e.set("removed_at", -1);
			e.set("count", 1);
			fireEvent(e);
		}
		return ret;
	}

	@Override
	public ItemDefinition remove(int index) {
		ItemDefinition item = items.remove(index);
		PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
		e.set("removed_at", item);
		e.set("count", 1);
		fireEvent(e);
		return item;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean ret = items.removeAll(c);
		if (ret) {
			PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
			e.set("removed_at", -1);
			fireEvent(e);
		}
		return ret;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean ret = items.retainAll(c);
		if (ret) {
			PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
			e.set("removed_at", -1);
			fireEvent(e);
		}
		return ret;
	}

	@Override
	public ItemDefinition set(int index, ItemDefinition item) {
		ItemDefinition old = items.set(index, item);
		PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
		e.set("changed_at", index);
		e.set("count", 1);
		fireEvent(e);
		return old;
	}

	@Override
	public int size() {
		return items.size();
	}

	// TODO the sublist will not report events properly
	@Override
	public List<ItemDefinition> subList(int fromIndex, int toIndex) {
		return items.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return items.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return items.toArray(a);
	}
}

package gamesystem.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimplePropertyCollection implements PropertyCollection {
	protected Map<String, Property<?>> properties = new HashMap<>();
	protected Map<String, List<PropertyListener>> listeners = new HashMap<>();

	public void debugDumpStructure() {
		List<String> keys = new ArrayList<>(properties.keySet());
		Collections.sort(keys);
		for (String s : keys) {
			System.out.println(s);
		}
	}

	@Override
	public void addProperty(Property<?> property) {
		properties.put(property.getName(), property);
	}

	// doesn't remove any listeners
	@Override
	public void removeProperty(Property<?> property) {
		properties.remove(property);
	}

	@Override
	public void fireEvent(PropertyEvent event) {
		String propName = event.source.getName();
		while (true) {
			List<PropertyListener> list = listeners.get(propName);
			if (list != null) {
				for (PropertyListener l : list) {
					l.propertyChanged(event);
				}
			}
			if (propName.equals("")) break;
			int idx = propName.lastIndexOf('.');
			if (idx >= 0)
				propName = propName.substring(0, idx);
			else
				propName = "";
		}
	}

	@Override
	public void addPropertyListener(String propName, PropertyListener l) {
		// TODO argument checking: check l is not null
		List<PropertyListener> list = listeners.get(propName);
		if (list == null) {
			list = new ArrayList<>();
			listeners.put(propName, list);
		}
		list.add(l);
	}

	@Override
	public void addPropertyListener(Property<?> property, PropertyListener l) {
		addPropertyListener(property.getName(), l);
	}

	@Override
	public void removePropertyListener(String propName, PropertyListener l) {
		Property<?> property = getProperty(propName);
		if (property == null) return;
		List<PropertyListener> list = listeners.get(property);
		if (list == null) return;
		list.remove(l);
	}

	@Override
	public void removePropertyListener(Property<?> property, PropertyListener l) {
		List<PropertyListener> list = listeners.get(property);
		if (list == null) return;
		list.remove(l);
	}

	@Override
	public Property<?> getProperty(String name) {
		return properties.get(name);
	}
}

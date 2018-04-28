package gamesystem.core;

import java.util.HashMap;
import java.util.Map;

// An event representing some change to a property.

public class PropertyEvent {
	// event types
	public static final String OVERRIDE_ADDED = "override_added";
	public static final String OVERRIDE_REMOVED = "override_removed";
	public static final String REGULAR_VALUE_CHANGED = "regular_value_changed";		// The current value (from getValue()) will not have changed if there is an override in place
	public static final String VALUE_CHANGED = "value_changed";
	public static final String MODIFIER_ADDED = "modifier_added";
	public static final String MODIFIER_REMOVED = "modifier_removed";

	// fields
	public static final String PREVIOUS_VALUE = "previous_value";	// previous value of source property

	public final Property<?> source;
	public final String event;
	Map<String, Object> fields = new HashMap<>();

	public PropertyEvent(Property<?> src, String evt) {
		source = src;
		event = evt;
	}

	public void set(String field, Object value) {
		fields.put(field, value);
	}

	public boolean hasField(String field) {
		return fields.containsKey(field);
	}

	public Object get(String field) {
		return fields.get(field);
	}

	public int getInt(String field) {
		Object o = fields.get(field);
		if (o instanceof Number) return ((Number)o).intValue();
		throw new IllegalArgumentException("Field " + field + " not an integer in event (instead value '" + o + "')");
	}

	public String getString(String field) {
		Object o = fields.get(field);
		if (o != null) return o.toString();
		return null;
	}
}

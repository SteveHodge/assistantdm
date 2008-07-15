package magicgenerator;

import java.util.HashMap;
import java.util.Map;

// contains information about a specific item
public class Item {
	public final static int CLASS_MINOR = 0;
	public final static int CLASS_MEDIUM = 1;
	public final static int CLASS_MAJOR = 2;

	int category = Item.CLASS_MINOR;
	Map<String,Field> fields = new HashMap<String,Field>();
	Map<Field,Object> values = new HashMap<Field,Object>();

	// we set up several standard fields:
	// item (text) - should be the final, complete description of the item
	// cost (number)
	// type (text)
	public Item(int cat) {
		category = cat;
		addField(new Field("item",Field.TYPE_TEXT));
		addField(new Field("type",Field.TYPE_TEXT));
		addField(new Field("cost",Field.TYPE_NUMBER));
	}

	public int getCategory() {
		return category;
	}

	public int getCost() {
		return ((Integer)getValue("cost")).intValue();
	}

	public String toString() {
		return getValue("item").toString();
	}

	public String getFullDescription() {
		String s = "Item:\n";
		for (Field f : values.keySet()) {
			s += f.name + " = " + values.get(f) + "\n";
		}
		return s;
	}

	// get the value of the field 'name'
	// this will always return a value if the field exists - it will create a default value if
	// there is no current value
	public Object getValue(String name) {
		Field f = getField(name);
		if (f == null) throw new IllegalArgumentException("No field named '"+name+"'");
		Object value = values.get(f);
		if (value == null) {
			value = f.getValueObject();
			values.put(f, value);
		}
		return value;
	}

	// get the value of the specified field
	// this will add the field if it doesn't exist and create a default value if
	// there is no current value
	public Object getValue(Field f) {
		if (!fields.containsValue(f)) addField(f);
		Object value = values.get(f);
		if (value == null) {
			value = f.getValueObject();
			values.put(f, value);
		}
		return value;
	}

	// get the field 'name' - returns null if the field doesn't exist in the item
	public Field getField(String name) {
		return fields.get(name);
	}

	// get the value of the field called 'name'
	public void setValue(String name, Object c) {
		values.put(getField(name),c);
	}

	// get the value of the field f
	// this method will add f to the collection of fields in this Item if it is not already present
	public void setValue(Field f, Object c) {
		if (!fields.containsValue(f)) addField(f);
		values.put(f,c);
	}

	// add the specified field to this object. a value will be created when the field is accessed
	public void addField(Field f) {
		fields.put(f.name,f);
	}
}

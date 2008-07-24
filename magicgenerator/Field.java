package magicgenerator;

import java.io.Serializable;

// an item field
public class Field implements Serializable {
	private static final long serialVersionUID = 1L;

	public final static int TYPE_TEXT = 0;
	public final static int TYPE_NUMBER = 1;
	public final static int TYPE_OBJECT = 2;

	String name;
	int type = TYPE_TEXT;

	public Field(String n, int t) {
		//TODO check for valid type
		name = n;
		type = t;
	}

	// expects:
	// field("<name>",<type>)
	// field names must be alphanumeric 
	// type must be either "text" or "number" (no quotes)
	public Field(String str) {
		name = str.substring(str.indexOf("field(\"")+7, str.lastIndexOf('"'));
		String t = str.substring(str.indexOf(',')+1, str.indexOf(')'));
		if (t.equals("number")) type = TYPE_NUMBER;
	}

	public String toString() {
		return "Field ("+name+") - " + (type == TYPE_TEXT ? "text" : "number");
	}

	public Object getValueObject() {
		if (type == TYPE_NUMBER) {
			return new Integer(0);
		} else if (type == TYPE_OBJECT) {
			return new Object();
		}
		return new String();
	}
}

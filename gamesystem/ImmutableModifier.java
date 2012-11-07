package gamesystem;

import java.beans.PropertyChangeListener;


/*
 * ImmutableModifier - a single modifier that cannot be changed after creation
 */
public class ImmutableModifier implements Modifier {
	public int modifier;
	public String type;
	public String source;
	public String condition;
	public int id;

	public ImmutableModifier(int m) {
		this(m, null, null, null);
	}

	public ImmutableModifier(int m, String t) {
		this(m, t, null, null);
	}

	public ImmutableModifier(int m, String t, String s) {
		this(m,t,s,null);
	}

	public ImmutableModifier(int m, String t, String s, String c) {
		modifier = m;
		type = t;
		source = s;
		condition = c;
	}

	public String toString() {
		String s = "";
		if (type != null) s += " "+type;
		if (modifier >= 0) {
			s = "+" + modifier + s + " bonus";
		} else {
			s = modifier + s + " penalty";
		}

		if (condition != null) s += " " + condition;
		if (source != null) s += " (from "+source+")";
		return s;
	}

	public int getModifier() {
		return modifier;
	}

	public String getType() {
		return type;
	}

	public String getSource() {
		return source;
	}

	public String getCondition() {
		return condition;
	}

	public int getID() {
		return id;
	}

	// ImmutableModifiers never change so we can ignore listeners
	public void addPropertyChangeListener(PropertyChangeListener listener) {}
	public void removePropertyChangeListener(PropertyChangeListener listener) {}
}
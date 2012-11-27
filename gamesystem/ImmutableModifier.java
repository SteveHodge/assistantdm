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

	public ImmutableModifier(int mod) {
		this(mod, null, null, null);
	}

	public ImmutableModifier(int mod, String type) {
		this(mod, type, null, null);
	}

	public ImmutableModifier(int mod, String type, String source) {
		this(mod,type,source,null);
	}

	public ImmutableModifier(int mod, String type, String source, String condition) {
		this.modifier = mod;
		this.type = type;
		this.source = source;
		this.condition = condition;
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
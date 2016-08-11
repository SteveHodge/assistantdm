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
		if (type != null && type.length() > 0) this.type = type;
		this.source = source;
		this.condition = condition;
	}

	@Override
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

	@Override
	public int getModifier() {
		return modifier;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public String getCondition() {
		return condition;
	}

	@Override
	public int getID() {
		return id;
	}

	// returns true if all modifier, source, type, and condition match (currently implemented
	// by comparing the string representations)
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	// ImmutableModifiers never change so we can ignore listeners
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {}
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {}
}
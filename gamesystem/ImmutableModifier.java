package gamesystem;

import java.beans.PropertyChangeListener;


/*
 * ImmutableModifier - a single modifier that cannot be changed after creation
 */
public class ImmutableModifier implements Modifier {
	public int modifier;
	public String type;
	public String source;

	public ImmutableModifier(int m) {
		this(m, null, null);
	}

	public ImmutableModifier(int m, String t) {
		this(m, t, null);
	}

	public ImmutableModifier(int m, String t, String s) {
		modifier = m;
		type = t;
		source = s;
	}

	public String toString() {
		String s = "";
		if (type != null) {
			s += " "+type;
		}
		if (modifier >= 0) {
			s = "+" + modifier + s + " bonus";
		} else {
			s = modifier + s + " penalty";
		}
		if (source != null) {
			s += " (from "+source+")";
		}
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

	// ImmutableModifiers never change so we can ignore listeners
	public void addPropertyChangeListener(PropertyChangeListener listener) {}
	public void removePropertyChangeListener(PropertyChangeListener listener) {}
}
package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

// TODO overrides are a bit unintuituve - they ignore modifiers, which is fine, but the modifiers are reapplied once the override is removed (which at the moment happens if the override is set to the baseValue in the ui) 
public class AbilityScore extends Statistic {
	public enum Type {
		STRENGTH ("Strength", "STR"),
		DEXTERITY("Dexterity", "DEX"),
		CONSTITUTION("Constitution", "CON"),
		INTELLIGENCE("Intelligence", "INT"),
		WISDOM("Wisdom", "WIS"),
		CHARISMA("Charisma", "CHA");

		private Type(String d, String a) {description = d; abbreviation = a;}

		public String toString() {return description;}

		public String getAbbreviation() {return abbreviation;}

		// XXX brute force implementation - could keep a map
		public static Type getAbilityType(String d) {
			for (Type t : values()) {
				if (t.description.equals(d)) return t;
			}
			return null;	// TODO probably better to throw an exception 
		}

		private final String description, abbreviation;
	}

	protected final Modifier modifier;
	protected Type type;
	protected int baseValue = 0;
	protected int override = -1;

	public static int getModifier(int score) {
		return score/2-5;
	}

	protected class AbilityModifier implements Modifier {
		final PropertyChangeSupport modpcs = new PropertyChangeSupport(this);

		public AbilityModifier(AbilityScore score) {
			score.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					//int oldValue = ((Integer)evt.getOldValue())/2-5;
					//int newValue = ((Integer)evt.getNewValue())/2-5;
					//modpcs.firePropertyChange("value", null, newValue);
					modpcs.firePropertyChange("value", null, getModifier());
				}
			});
		}

		public int getModifier() {
			return AbilityScore.getModifier(getValue());
		}

		public String getType() {
			return name;
		}

		public String getSource() {
			return null;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			modpcs.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			modpcs.removePropertyChangeListener(listener);
		}

		public String toString() {
			StringBuilder s = new StringBuilder();
			if (getModifier() >= 0) s.append("+");
			s.append(getModifier()).append(" ").append(type).append(" modifier ");
			return s.toString();
		}

		public String getCondition() {
			return null;
		}

		public int getID() {
			return 0;
		}
	};

	public AbilityScore(Type type) {
		super(type.toString());
		this.type = type;
		modifier = new AbilityModifier(this); 
	}

	public Type getType() {
		return type;
	}

	public int getValue() {
		if (override == -1) {
			return baseValue + super.getValue();
		} else {
			return override;
		}
	}

	public int getBaseValue() {
		return baseValue;
	}

	// returns the "normal" value of the ability score (the base value + any modifiers that apply)
	// if no override is set then this will be equal to getValue()
	public int getRegularValue() {
		return baseValue + super.getValue();
	}

	public void setBaseValue(int v) {
		//int oldValue = getValue();
		baseValue = v;
		int newValue = getValue();
		//System.out.println(name+".setBaseValue("+v+"). Old = "+oldValue+", new = "+newValue);
		pcs.firePropertyChange("value", null, newValue);	// total maybe unchanged, but some listeners will be interested in any change to the modifiers
	}

	public void setOverride(int v) {
		if (override != v) {
			override = v;
			pcs.firePropertyChange("value", null, override);
		}
	}

	public int getOverride() {
		return override;
	}

	public void clearOverride() {
		if (override != -1) {
			override = -1;
			pcs.firePropertyChange("value", null, getValue());
		}
	}

	public Modifier getModifier() {
		return modifier;
	}

	public int getModifierValue() {
		return modifier.getModifier();
	}

	public Element getElement(Document doc) {
		Element e = doc.createElement("AbilityScore");
		e.setAttribute("type", type.toString());
		e.setAttribute("value", ""+baseValue);
		if (override != -1) e.setAttribute("temp", ""+override);
		return e;
	}

	// TODO notify listeners?
	public void parseDOM(Element e) {
		if (!e.getTagName().equals("AbilityScore")) return;
		if (!e.getAttribute("type").equals(type.toString())) return;
		
		baseValue = Integer.parseInt(e.getAttribute("value"));
		if (e.hasAttribute("temp")) override = Integer.parseInt(e.getAttribute("temp"));
	}
}

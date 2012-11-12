package gamesystem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class SavingThrow extends Statistic {
	public enum Type {
		FORTITUDE("Fortitude", AbilityScore.Type.CONSTITUTION),
		REFLEX("Reflex", AbilityScore.Type.DEXTERITY),
		WILL("Will", AbilityScore.Type.WISDOM);

		private Type(String d, AbilityScore.Type a) {description = d; ability = a;}
	
		public String toString() {return description;}

		public AbilityScore.Type getAbilityType() {return ability;}

		public static Type getSavingThrowType(String d) {
			for (Type t : values()) {
				if (t.description.equals(d)) return t;
			}
			return null;	// TODO probably better to throw an exception 
		}

		private final String description;
		private final AbilityScore.Type ability;
	}

	protected Type type;
	protected int baseValue = 0;

	// TODO verify that the ability is the correct one. alternatively pass all ability scores
	public SavingThrow(Type type, AbilityScore ability) {
		super(type.toString());
		this.type = type;
		addModifier(ability.getModifier());
	}

	public int getValue() {
		return baseValue + super.getValue();
	}

	public int getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(int v) {
		//int oldValue = getValue();
		baseValue = v;
		int newValue = getValue();
		//System.out.println(name+".setBaseValue("+v+"). Old = "+oldValue+", new = "+newValue);
		pcs.firePropertyChange("value", null, newValue);	// total maybe unchanged, but some listeners will be interested in any change to the modifiers
	}

	public Element getElement(Document doc) {
		Element e = doc.createElement("Save");
		e.setAttribute("type", type.toString());
		e.setAttribute("base", ""+baseValue);
		return e;
	}

	// TODO notify listeners?
	public void parseDOM(Element e) {
		if (!e.getTagName().equals("Save")) return;
		if (!e.getAttribute("type").equals(type.toString())) return;
		
		baseValue = Integer.parseInt(e.getAttribute("base"));
		// TODO misc
	}
}

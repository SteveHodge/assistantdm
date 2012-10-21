package gamesystem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class SavingThrow extends Statistic {
	// TODO if these constants are really only applicable in relation to the array in Character then they should be defined there (and protected)
	public static final int SAVE_FORTITUDE = 0;
	public static final int SAVE_REFLEX = 1;
	public static final int SAVE_WILL = 2;
	protected static final String[] save_names = {"Fortitude", "Reflex", "Will"};

	protected int baseValue = 0;

	public static String getSavingThrowName(int save) {
		return save_names[save];
	}

	public static int getSaveAbility(int save) {
		if (save == SAVE_FORTITUDE) return AbilityScore.ABILITY_CONSTITUTION;
		if (save == SAVE_REFLEX) return AbilityScore.ABILITY_DEXTERITY;
		if (save == SAVE_WILL) return AbilityScore.ABILITY_WISDOM;
		return -1;
	}

	public SavingThrow(int type, AbilityScore ability) {
		super(save_names[type]);
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
		e.setAttribute("type", name);
		e.setAttribute("base", ""+baseValue);
		return e;
	}

	// TODO notify listeners?
	public void parseDOM(Element e) {
		if (!e.getTagName().equals("Save")) return;
		if (!e.getAttribute("type").equals(name)) return;
		
		baseValue = Integer.parseInt(e.getAttribute("base"));
		// TODO misc
	}
}

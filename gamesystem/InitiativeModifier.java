package gamesystem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class InitiativeModifier extends Statistic {
	protected int baseValue = 0;

	public InitiativeModifier(AbilityScore dex) {
		super("Initiative");
		addModifier(dex.getModifier());
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
		pcs.firePropertyChange("value", null, newValue);
	}

	public Element getElement(Document doc) {
		Element e = doc.createElement("Initiative");
		e.setAttribute("value", ""+baseValue);
		return e;
	}

	// TODO notify listeners?
	public void parseDOM(Element e) {
		if (!e.getTagName().equals("Initiative")) return;
		baseValue = Integer.parseInt(e.getAttribute("value"));
	}
}

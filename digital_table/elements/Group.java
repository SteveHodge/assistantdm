package digital_table.elements;

import java.awt.Graphics2D;

public class Group extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_LABEL = "label";	// String

	private Property<String> label = new Property<String>(PROPERTY_LABEL, "", String.class);

	@Override
	public void paint(Graphics2D g) {
	}

	public void addChild(MapElement e) {
		e.parent = this;
	}

	public void removeChild(MapElement e) {
		if (e.parent == this) e.parent = null;
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Group (" + getID() + ")";
		return "Group (" + label + ")";
	}
}

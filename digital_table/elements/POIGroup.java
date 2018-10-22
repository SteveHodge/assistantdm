package digital_table.elements;

import java.awt.Color;

public class POIGroup extends Group {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_COLOR = "color";	// Color
	public static final String PROPERTY_BACKGROUND_COLOR = "background_color";	// Color
	public static final String PROPERTY_SOLID_BACKGROUND = "solid_background";	// boolean
	public static final String PROPERTY_FONT_SIZE = "font_size";	// double

	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, 0, Integer.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Integer r) {
			super.setValue(r % 4);
		}
	};
	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Color> backgroundColor = new Property<Color>(PROPERTY_BACKGROUND_COLOR, Color.WHITE, Color.class);
	Property<Boolean> solidBackground = new Property<Boolean>(PROPERTY_SOLID_BACKGROUND, true, Boolean.class);
	Property<Double> fontSize = new Property<Double>(PROPERTY_FONT_SIZE, 0.333d, Double.class);

	protected POIGroup(int id) {
		super(id);
	}

	public POIGroup() {
		super();
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "POI Group (" + getID() + ")";
		return "POI Group (" + label + ")";
	}

	@Override
	public Object getProperty(String property) {
		if (property.equals(PROPERTY_X)) {
			return getX();
		} else if (property.equals(PROPERTY_Y)) {
			return getY();
		} else {
			return super.getProperty(property);
		}
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_X)) {
			setX((Double) value);
		} else if (property.equals(PROPERTY_Y)) {
			setY((Double) value);
		} else {
			super.setProperty(property, value);
		}
	}
}

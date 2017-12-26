package led_control;

import java.awt.Color;

class SolidRGBPattern extends Pattern {
	@Override
	PatternType getType() {
		return PatternType.SOLID_RGB;
	}

	@Override
	String getOptionString() {
		return String.format("R = %s, G = %s, B = %s", formatByte(red), formatByte(green), formatByte(blue));
	}

	Object red, blue, green;

	SolidRGBPattern(Object r, Object g, Object b) {
		red = r;
		green = g;
		blue = b;
	}

	// note this will replace any dynamics
	void setColor(Color c) {
		red = c.getRed();
		green = c.getGreen();
		blue = c.getBlue();
	}

	Color getColor() {
		int r = 0, g = 0, b = 0;

		if (red instanceof Dynamic)
			r = ((Dynamic) red).value;
		else if (red instanceof Integer)
			r = (Integer) red;

		if (green instanceof Dynamic)
			r = ((Dynamic) green).value;
		else if (green instanceof Integer)
			r = (Integer) green;

		if (blue instanceof Dynamic)
			r = ((Dynamic) blue).value;
		else if (blue instanceof Integer)
			r = (Integer) blue;

		return new Color(r, g, b);
	}

	@Override
	String getJSON() {
		StringBuilder json = new StringBuilder("{");
		json.append("\"type\": \"").append(getType().getJSONName());
		json.append("\", \"red\": ").append(getJSONValue(red));
		json.append(", \"green\": ").append(getJSONValue(green));
		json.append(", \"blue\": ").append(getJSONValue(blue));
		json.append("}");
		return json.toString();
	}
}
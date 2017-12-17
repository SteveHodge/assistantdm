package led_control;

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
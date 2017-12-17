package led_control;

class SolidHSVPattern extends Pattern {
	@Override
	PatternType getType() {
		return PatternType.SOLID_HSV;
	}

	@Override
	String getOptionString() {
		return String.format("H = %s, S = %s, V = %s", formatByte(hue), formatByte(saturation), formatByte(value));
	}

	Object hue, saturation, value;

	SolidHSVPattern(Object h, Object s, Object v) {
		hue = h;
		saturation = s;
		value = v;
	}

	@Override
	String getJSON() {
		StringBuilder json = new StringBuilder("{");
		json.append("\"type\": \"").append(getType().getJSONName());
		json.append("\", \"hue\": ").append(getJSONValue(hue));
		json.append(", \"saturation\": ").append(getJSONValue(saturation));
		json.append(", \"value\": ").append(getJSONValue(value));
		json.append("}");
		return json.toString();
	}
}
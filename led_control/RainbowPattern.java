package led_control;

class RainbowPattern extends Pattern {
	@Override
	PatternType getType() {
		return PatternType.RAINBOW;
	}

	@Override
	String getOptionString() {
		return String.format("Hue = %s, Delta = %s", formatByte(startHue), formatByte(delta));
	}

	Object startHue, delta;

	RainbowPattern(Object startHue, Object delta) {
		this.startHue = startHue;
		this.delta = delta;
	}

	@Override
	String getJSON() {
		StringBuilder json = new StringBuilder("{");
		json.append("\"type\": \"").append(getType().getJSONName());
		json.append("\", \"start_hue\": ").append(getJSONValue(startHue));
		json.append(", \"delta\": ").append(getJSONValue(delta));
		json.append("}");
		return json.toString();
	}
}
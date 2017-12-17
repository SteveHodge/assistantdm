package led_control;

abstract class Pattern extends JSONOutput {
	abstract PatternType getType();

	abstract String getOptionString();

	static String formatByte(Object val) {
		if (val instanceof Dynamic) {
			Dynamic d = (Dynamic)val;
			return "Dynamic(" + d.id + ")";
		} else {
			return String.format("%d", val);
		}
	}

	String getJSONValue(Object val) {
		if (val instanceof Dynamic)
			return ((Dynamic) val).getJSON();
		else
			return val.toString();
	}
}
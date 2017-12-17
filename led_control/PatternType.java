package led_control;

public enum PatternType {
	RAINBOW("rainbow", "Rainbow") {
		@Override
		public Pattern createPattern() {
			return new RainbowPattern(0, 1);
		}
	},
	SOLID_RGB("solid_rgb", "Solid RGB") {
		@Override
		public Pattern createPattern() {
			return new SolidRGBPattern(0, 0, 0);
		}
	},
	SOLID_HSV("solid_hsv", "Solid HSV") {
		@Override
		public Pattern createPattern() {
			return new SolidHSVPattern(0, 0, 0);
		}
	};

	@Override
	public String toString() {
		return name;
	}

	public String getJSONName() {
		return json;
	}

	abstract public Pattern createPattern();

	private PatternType(String json, String name) {
		this.json = json;
		this.name = name;
	}

	private String json;
	private String name;
}

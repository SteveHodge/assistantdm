package led_control;

class Region extends JSONOutput {
	int id;
	boolean enabled;
	int start;
	int count;
	Pattern pattern;

	Region(int id, boolean enabled, int start, int count, Pattern pattern) {
		this.id = id;
		this.enabled = enabled;
		this.start = start;
		this.count = count;
		this.pattern = pattern;
	}

	@Override
	String getJSON() {
		StringBuilder json = new StringBuilder("{");
		json.append("\"regionId\": ").append(id);
		json.append(", \"enabled\": ").append(enabled);
		json.append(", \"start\": ").append(start);
		json.append(", \"count\": ").append(count);
		json.append(", \"pattern\": ").append(pattern.getJSON());
		json.append("}");
		return json.toString();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Region(");
		s.append("id = ").append(id);
		s.append(", enabled = ").append(enabled);
		s.append(", start = ").append(start);
		s.append(", count = ").append(count);
		s.append(")");
		return s.toString();
	}
}
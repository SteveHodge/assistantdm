package led_control;

class Dynamic extends JSONOutput {
	int id;
	int mode;
	int value;

	Dynamic(int id, int mode, int value) {
		this.id = id;
		this.mode = mode;
		this.value = value;
	}

	@Override
	String getJSON() {
		StringBuilder json = new StringBuilder("{");
		json.append("\"id\": ").append(id);
		json.append(", \"mode\": ").append(mode);
		json.append(", \"value\": ").append(value);
		json.append("}");
		return json.toString();
	}

	@Override
	public String toString() {
		return "Dynamic " + id + " (" + mode + ", " + value + ")";
	}
}
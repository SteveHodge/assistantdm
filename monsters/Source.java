package monsters;

public class Source {
	private String name;
	private String location;

	Source(String name, String location) {
		this.name = name;
		this.location = location;
	}

	String getName() {
		return name;
	}

	String getLocation() {
		return location;
	}
}

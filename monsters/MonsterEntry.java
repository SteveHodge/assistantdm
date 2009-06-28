package monsters;

public class MonsterEntry {
	String name;
	String url;
	String size;
	String type;
	String environment;
	String cr;

	public String toString() {
		String label = "Size: "+size + ", Type: "+type+", Environment: "+environment+", CR: "+cr;
		return name + " (" + label + ")";
	}
}
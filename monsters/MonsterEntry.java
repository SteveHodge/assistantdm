package monsters;

import java.net.MalformedURLException;
import java.net.URL;

public class MonsterEntry {
	String name;
	String url;
	String size;
	String type;
	String environment;
	String cr;
	String source;

	@Override
	public String toString() {
		String label = "Size: "+size + ", Type: "+type+", Environment: "+environment+", CR: "+cr;
		return name + " (" + label + ")";
	}

	public URL getURL(URL baseURL) {
		URL url = null;
		try {
			url = new URL(this.url);
		} catch (MalformedURLException e1) {
			// try relative URL
			try {
				url = new URL(baseURL, this.url);
			} catch (MalformedURLException e) {
			}
		}
		return url;
	}
}
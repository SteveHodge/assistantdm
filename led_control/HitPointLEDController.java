package led_control;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gamesystem.HPs;
import gamesystem.core.PropertyListener;
import gamesystem.core.SimpleProperty;
import party.Character;
import party.Party;
import util.XMLUtils;

public class HitPointLEDController {
	final static String url = "http://192.168.1.9/assistantdm/leds";	// address of website page that reports the address of the controller
	String address = "192.168.1.132";	// default controller address
	List<Region> regions = new ArrayList<>();
	Map<Character, Region> regionMap = new HashMap<>();
	boolean enabled = false;

	// TODO should rerun the attempt to get controller address if we can't contact the controller at any point
	public HitPointLEDController() {
		fetchControllerAddress();
	}

	void fetchControllerAddress() {
		// try to get the correct address from armitage
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			if (con.getResponseCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine = in.readLine();
				in.close();
				if (inputLine != null && inputLine.length() > 0) {
					System.out.println("Got LED Controller address: " + inputLine);
					address = inputLine;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Region addCharacter(Character c) {
		Pattern p = new SolidRGBPattern(0, 0, 0);
		Region r = new Region(regions.size(), true, 0, 0, p);
		regions.add(r);
		regionMap.put(c, r);
		c.addPropertyListener(c.getHPStatistic(), new PropertyListener<Integer>() {
			@Override
			public void propertyChanged(SimpleProperty<Integer> source, Integer oldValue) {
				updateCharacter(c, false);
			}
		});
		return r;
	}

	void updateCharacter(Character c, boolean force) {
		boolean changed = updateCharacter(c);
		if (changed || force) {
			sendConfig();	// TODO maybe make this an updater type thing where it only sends every X seconds
		}
	}

	boolean updateCharacter(Character c) {
		HPs hps = c.getHPStatistic();
		Color color = HitPointLEDController.getColor(hps);
		Region r = regionMap.get(c);
		SolidRGBPattern p = (SolidRGBPattern) r.pattern;
		boolean changed = (Integer) p.red != color.getRed() || (Integer) p.blue != color.getBlue() || (Integer) p.green != color.getGreen();
//		System.out.println(hps.getShortSummary() + " -> " + color + " changed? " + changed);
		p.red = color.getRed();
		p.blue = color.getBlue();
		p.green = color.getGreen();
		return changed;
	}

	static Color getColor(HPs hps) {
		// color for MaxHPs is green, full brightness
		// color for 0 is red, full brightness
		// color for -10 is black
		int curr = hps.getHPs();
		if (curr >= 0) {
			int value = hps.getMaxHPStat().getValue() > 0 ? curr * 510 / hps.getMaxHPStat().getValue() : 1;
			if (value > 510) value = 510;

			int red;
			int green;
			if (value <= 255) {
				red= 255;
				//green = (int) Math.round(Math.sqrt(value) * 16);
				green = value;
			} else {
				green = 255;
				//value = value - 255;
				//red = Math.round(256 - (value * value / 255));
				red = 510 - value;
			}
			return new Color(red, green, 0);
		} else {
			curr = curr + 10;
			if (curr < 0) curr = 0;
			int red = Math.round(255 * curr / 10);
			return new Color(red, 0, 0);
		}
	}

	void sendConfig() {
		if (enabled) sendConfig(regions);
	}

	void sendConfig(List<Region> regions) {
		URL url;
		try {
			url = new URL("http://" + address + "/config");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			String json = "[" + String.join(",", regions.stream().map(e -> e.getJSON()).toArray(String[]::new)) + "]";
//			System.out.println(json);
			byte[] body = json.getBytes(StandardCharsets.UTF_8);
			http.setFixedLengthStreamingMode(body.length);
			http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			http.connect();
			OutputStream os = http.getOutputStream();
			os.write(body);

			int status = http.getResponseCode();
			if (status != 200) System.out.println("LED Controller Response: " + status);
//			for (Entry<String, List<String>> header : http.getHeaderFields().entrySet()) {
//				System.out.println(header.getKey() + " = " + header.getValue());
//			}

//			String contentType = http.getHeaderField("Content-Type");
//			String charset = null;
//
//			for (String param : contentType.replace(" ", "").split(";")) {
//				if (param.startsWith("charset=")) {
//					charset = param.split("=", 2)[1];
//					break;
//				}
//			}

//			if (charset != null) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String line;
			StringBuffer result = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				result.append(line).append("\n");
			}
			rd.close();
			if (status != 200) System.out.println(result);
//			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Element getElement(Document doc) {
		Element e = doc.createElement("Regions");
		for (Character c : regionMap.keySet()) {
			Region r = regionMap.get(c);
			if (r != null) {
				Element m = doc.createElement("Region");
				m.setAttribute("name", c.getName());
				m.setAttribute("id", Integer.toString(r.id));
				m.setAttribute("start", Integer.toString(r.start));
				m.setAttribute("count", Integer.toString(r.count));
				m.setAttribute("enabled", Boolean.toString(r.enabled));
				e.appendChild(m);
			}
		}
		return e;
	}

	public void parseDOM(Party p, Node parent) {
		Element node = XMLUtils.findNode(parent, "Regions");
		if (node != null) {
			NodeList children = node.getChildNodes();
			for (int i=0; i<children.getLength(); i++) {
				if (children.item(i).getNodeName().equals("Region")) {
					Element m = (Element)children.item(i);
					Character c = p.get(m.getAttribute("name"));
					if (c != null) {
						Region r = addCharacter(c);
						r.id = Integer.parseInt(m.getAttribute("id"));
						r.start = Integer.parseInt(m.getAttribute("start"));
						r.count = Integer.parseInt(m.getAttribute("count"));
						r.enabled = Boolean.parseBoolean(m.getAttribute("enabled"));
						updateCharacter(c);
					}
				}
			}
			sendConfig();
		}
	}
}

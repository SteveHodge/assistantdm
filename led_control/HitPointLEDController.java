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

import gamesystem.HPs;
import gamesystem.core.PropertyListener;
import gamesystem.core.SimpleProperty;
import party.Character;

// TODO Wemos code needs to register its address with the webserver and this class needs to query for that address

public class HitPointLEDController {
	String address = "192.168.1.132";
	List<Region> regions = new ArrayList<>();
	Map<Character, Region> regionMap = new HashMap<>();

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
		HPs hps = c.getHPStatistic();
		Color color = HitPointLEDController.getColor(hps);
		Region r = regionMap.get(c);
		SolidRGBPattern p = (SolidRGBPattern) r.pattern;
		if (!force && (Integer) p.red == color.getRed() && (Integer) p.blue == color.getBlue() && (Integer) p.green == color.getGreen()) {
			System.out.println("Ignoring update for " + c.getName() + ", no change in color");
			return;
		}
		p.red = color.getRed();
		p.blue = color.getBlue();
		p.green = color.getGreen();
		System.out.println("Sending for " + c.getName() + " on " + hps.getHPs() + " hps at " + System.currentTimeMillis() + " ms");
		sendConfig();	// TODO maybe make this an updater type thing where it only sends every X seconds
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
			System.out.println("Response: " + status);
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
			System.out.println(result);
//			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

package led_control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import util.Module;
import util.ModuleRegistry;

//TODO sendConfig should probably be threaded or part of Updater
//XXX should this be moved to util?

public class LEDController implements Module {
	final static String url = "http://192.168.1.9/assistantdm/leds";	// address of website page that reports the address of the controller
	String address = "192.168.1.132";	// default controller address

	// TODO should rerun the attempt to get controller address if we can't contact the controller at any point
	public LEDController() {
		fetchControllerAddress();
		ModuleRegistry.register(LEDController.class, this);
	}

	@Override
	public void moduleExit() {
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

	void sendConfig(int brightness, List<Region> regions) {
		URL url;
		try {
			url = new URL("http://" + address + "/config");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			String json = "{\"brightness\": " + brightness + ", \"regions\": ";
			json += "[" + String.join(",", regions.stream().map(e -> e.getJSON()).toArray(String[]::new)) + "]}";
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

		} catch (ConnectException e) {
			System.err.println("Couldn't connect to LED Controller: " + e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

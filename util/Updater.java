package util;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Updater {
	// NOTE: these constants may be overwritten by the config.xml file
	public static String MAP_IMAGE = "http://armitage/assistantdm/static/map.png";
	public static String TOP_LEGEND_IMAGE = "http://armitage/assistantdm/static/toplegend.png";
	public static String LEFT_LEGEND_IMAGE = "http://armitage/assistantdm/static/leftlegend.png";
	public static String INITIATIVE_FILE = "http://armitage/assistantdm/static/initiative.json";
	public static String DOCUMENT_DIR = "http://armitage/assistantdm/";
	public static String TOKEN_BASE_URL = "http://armitage/assistantdm/static/tokens";

	protected static class Update {
		String url;
		byte[] bytes;
	}

	protected static List<Update> pending = Collections.synchronizedList(new ArrayList<Update>());

	public static class UpdaterThread extends Thread {
		protected volatile boolean quit = false;

		@Override
		public void run() {
			while (!quit) {
				// wait 5 seconds
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					if (quit) return;
				}

				Update update;
				do {
					update = null;

					synchronized(pending) {
						Iterator<Update> i = pending.iterator();
						if (i.hasNext()) {
							// take the first update from the list
							update = i.next();
							i.remove();

							// search the list of any other updates with the same url - we use the bytes for the last matching update
							while (i.hasNext()) {
								Update u = i.next();
								if (update.url.equals(u.url)) {
									update = u;
									i.remove();
								}
							}
						}
					}

					if (update != null) {
						System.out.println("Updating "+update.url);
						String msg = updateURL(update.url, update.bytes);
						if (msg != null) {
							System.out.println(msg);
						}
					}
				} while (update != null);
			}
		}

		public void quit() {
			quit = true;
			interrupt();
		}
	};

	public static UpdaterThread updaterThread = new UpdaterThread();

	private static class UpdaterModule implements Module {
		UpdaterModule() {
			ModuleRegistry.register(UpdaterModule.class, this);
			Element config = ModuleRegistry.getConfig(this);
			if (config.hasAttribute("map_url")) MAP_IMAGE = config.getAttribute("map_url");
			if (config.hasAttribute("left_legend_url")) TOP_LEGEND_IMAGE = config.getAttribute("left_legend_url");
			if (config.hasAttribute("top_legend_url")) LEFT_LEGEND_IMAGE = config.getAttribute("top_legend_url");
			if (config.hasAttribute("initiative_url")) INITIATIVE_FILE = config.getAttribute("initiative_url");
			if (config.hasAttribute("document_url")) DOCUMENT_DIR = config.getAttribute("document_url");
			if (config.hasAttribute("token_url")) TOKEN_BASE_URL = config.getAttribute("token_url");
//			System.out.println("Updater config: ");
//			System.out.println("  map_url = " + MAP_IMAGE);
//			System.out.println("  initiative_url = " + INITIATIVE_FILE);
//			System.out.println("  document_url = " + DOCUMENT_DIR);
//			System.out.println("  token_url = " + TOKEN_BASE_URL);
		}

		@Override
		public void moduleExit() {
		}
	}

	static UpdaterModule module;

	static {
		module = new UpdaterModule();
		updaterThread.start();
	}

	// Note: the buffer is not copied - it should not be changed after being passed to this method
	public static void update(String dest, byte[] buffer) {
		Update update = new Update();
		update.url = dest;
		update.bytes = buffer;
		//System.out.println("Queued "+dest+" for update");
		pending.add(update);
	}

	public static void updateDocument(Document doc, String name) {
		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			FileOutputStream outputStream = new FileOutputStream(new File(name + ".xml"));
			trans.transform(new DOMSource(doc), new StreamResult(outputStream));

			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			trans.transform(new DOMSource(doc), new StreamResult(writer));
			byte[] bytes = writer.toString().getBytes();

			update(DOCUMENT_DIR + name.replace(" ", "%20"), bytes);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private static int sequence = 1;

	public static String updateURL(String dest, byte[] bytes) {
		return updateURL(dest, "application/octet-stream", bytes);
	}

	// TODO probably should propagate exceptions upwards, and consider wrapping responses in exceptions too
	public static String updateURL(String dest, String contentType, byte[] bytes) {
		// write local copy of image:
//		if (dest == MAP_IMAGE) {
//			try (FileOutputStream fos = new FileOutputStream(String.format("images\\image%03d.jpg", sequence++))) {
//				fos.write(bytes);
//				fos.flush();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}

		HttpURLConnection connection = null;
		try {
			// PUT it to the webserver
			//URL url = new URL("http://max/webcam/ftp/HTML_CharacterSheet/upload.php/"+name.replace(" ", "%20")+".xml");
			URL url = new URL(dest);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("PUT");
			connection.setRequestProperty("Content-Type", contentType);
			//connection.setRequestProperty("Content-Disposition", "attachment;filename=initiative.json");
			connection.setRequestProperty("Content-Length", ""+bytes.length);
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(bytes);
			wr.flush();
			wr.close();

			if (connection.getResponseCode() == 200) return null;
			System.out.println("UpdateURL (" + dest + "): " + connection.getResponseCode() + " - " + connection.getResponseMessage());
			return "Response = "+connection.getResponseCode() + " - " + connection.getResponseMessage();


//	    	// Get response
//	    	InputStream is = connection.getInputStream();
//	    	BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//	    	String line;
//	    	StringBuffer response = new StringBuffer();
//	    	while((line = rd.readLine()) != null) {
//	    		response.append(line);
//	    		response.append("\r");
//	    	}
//	    	rd.close();
//	    	return response.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	// TODO probably should propagate exceptions upwards
	protected static String updateFile(String dest, byte[] buffer) {
		String message = null;

		try (FileOutputStream out = new FileOutputStream(dest);) {
			out.write(buffer);
		} catch (FileNotFoundException e) {
			message = "Couldn't open "+dest+": "+e.getMessage();
		} catch (IOException e) {
			message = "Couldn't save to "+dest+": "+e.getMessage();
		}
		return message;
	}
}

package util;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
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


public class Updater {
	public static final String MAP_IMAGE = "http://armitage/assistantdm/upload.php/photo1.jpg";
	public static final String INITIATIVE_FILE = "http://armitage/assistantdm/upload.php/initiative.txt";
	public static final String DOCUMENT_DIR = "http://armitage/assistantdm/upload.php/";
	//URL url = new URL("http://max/webcam/ftp/HTML_CharacterSheet/upload.php/"+name.replace(" ", "%20")+".xml");

	//public static final String MAP_IMAGE = "m:\\webcam\\ftp\\images\\capture2.jpg";
	//public static final String INITIATIVE_FILE = "m:\\webcam\\ftp\\images\\initiative.txt";
	//public static final String DOCUMENT_DIR = "";

	protected static class Update {
		String url;
		byte[] bytes;
	}

	protected static List<Update> pending = Collections.synchronizedList(new ArrayList<Update>());

	public static class UpdaterThread extends Thread {
		protected volatile boolean quit = false;

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

	static {
		updaterThread.start();
	}

	// Note: the buffer is not copied - it should not be changed after being passed to this method
	public static void update(String dest, byte[] buffer) {
		Update update = new Update();
		update.url = dest;
		update.bytes = buffer;
		System.out.println("Queued "+dest+" for update");
		pending.add(update);
	}

	public static void updateDocument(Document doc, String name) {
		try {
	    	Transformer trans = TransformerFactory.newInstance().newTransformer();
	    	trans.setOutputProperty(OutputKeys.INDENT, "yes");
	    	trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	    	FileWriter outputStream = new FileWriter(new File(name+".xml"));
	    	trans.transform(new DOMSource(doc), new StreamResult(outputStream));

	    	StringWriter writer = new StringWriter();
	    	trans.transform(new DOMSource(doc), new StreamResult(writer));
	    	byte[] bytes = writer.toString().getBytes();

	    	update(DOCUMENT_DIR+name.replace(" ", "%20")+".xml", bytes);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO probably should propagate exceptions upwards, and consider wrapping responses in exceptions too
	public static String updateURL(String dest, byte[] bytes) {
    	HttpURLConnection connection = null;
		try {
	    	// PUT it to the webserver
	    	//URL url = new URL("http://max/webcam/ftp/HTML_CharacterSheet/upload.php/"+name.replace(" ", "%20")+".xml");
	    	URL url = new URL(dest);
	    	connection = (HttpURLConnection)url.openConnection();
	    	connection.setRequestMethod("PUT");
	    	connection.setRequestProperty("Content-Type", "");
	    	connection.setRequestProperty("Content-Length", ""+bytes.length);
	    	connection.setRequestProperty("Content-Language", "en-US");
	    	connection.setUseCaches(false);
	    	connection.setDoInput(true);
	    	connection.setDoOutput(true);
	
	    	DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
	    	wr.write(bytes);
	    	wr.flush();
	    	wr.close();
	
//	    	System.out.println("Response = "+connection.getResponseCode() + " - " + connection.getResponseMessage());
	    	if (connection.getResponseCode() == 200) return null;
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
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(dest);
			out.write(buffer);
		} catch (FileNotFoundException e) {
			message = "Couldn't open "+dest+": "+e.getMessage();
		} catch (IOException e) {
			message = "Couldn't save to "+dest+": "+e.getMessage();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					message = "Couldn't close "+dest+": "+e.getMessage();
				}
			}
		}
		return message;
	}
}

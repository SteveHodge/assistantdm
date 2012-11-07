package util;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;


public class Updater {
	public static final String MAP_IMAGE = "http://max/assistantdm/upload.php/photo1.jpg";
	public static final String INITIATIVE_FILE = "http://max/assistantdm/upload.php/initiative.txt";
	public static final String DOCUMENT_DIR = "http://max/assistantdm/upload.php/";
	//URL url = new URL("http://max/webcam/ftp/HTML_CharacterSheet/upload.php/"+name.replace(" ", "%20")+".xml");

	//public static final String MAP_IMAGE = "m:\\webcam\\ftp\\images\\capture2.jpg";
	//public static final String INITIATIVE_FILE = "m:\\webcam\\ftp\\images\\initiative.txt";
	//public static final String DOCUMENT_DIR = "";

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

	    	String msg = update("http://max/assistantdm/upload.php/"+name.replace(" ", "%20")+".xml", bytes);
	    	if (msg != null) System.out.println(msg);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String update(String dest, byte[] buffer) {
		return updateURL(dest, buffer);
	}

	// TODO probably should prograte exceptions upwards, and consider wrapping reponses in exceptions too
	protected static String updateURL(String dest, byte[] bytes) {
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
	
	// TODO probably should prograte exceptions upwards 
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

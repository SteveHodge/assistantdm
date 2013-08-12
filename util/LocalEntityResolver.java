package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LocalEntityResolver implements EntityResolver {

//private static final Logger LOG = ESAPI.getLogger(LocalEntityResolver.class);
	private static final Map<String, String> DTDS;
	static {
		DTDS = new HashMap<String, String>();
		DTDS.put("-//W3C//DTD XHTML 1.0 Transitional//EN",
				"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
		DTDS.put("-//W3C//ENTITIES Latin 1 for XHTML//EN",
				"http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent");
		DTDS.put("-//W3C//ENTITIES Symbols for XHTML//EN",
				"http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent");
		DTDS.put("-//W3C//ENTITIES Special for XHTML//EN",
				"http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent");
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		InputSource input_source = null;
		if (publicId != null && DTDS.containsKey(publicId)) {
			//LOG.debug(Logger.EVENT_SUCCESS, "Looking for local copy of [" + publicId + "]");

			final String dtd_system_id = DTDS.get(publicId);
			final String file_name = dtd_system_id.substring(
					dtd_system_id.lastIndexOf('/') + 1, dtd_system_id.length());

			InputStream input_stream = getClass().getClassLoader().getResourceAsStream("html//"+file_name);
			if (input_stream != null) {
				//LOG.debug(Logger.EVENT_SUCCESS, "Found local file [" + file_name + "]!");
				input_source = new InputSource(input_stream);
			} else {
				System.out.println("Failed to find '"+"html//"+file_name+"'");
			}
		}

		return input_source;
	}
}

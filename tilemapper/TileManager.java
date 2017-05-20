package tilemapper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class TileManager {
	protected static List<Tile> tiles = new ArrayList<>();
	protected static Map<String, Boolean> sets = new HashMap<>();
	protected static Map<String, Boolean> styles = new HashMap<>();
	public static List<TileSet> tileSets = new ArrayList<>();

	public static void addTile(Tile t) {
		tiles.add(t);
		if (!sets.containsKey(t.tileSet)) sets.put(t.tileSet, true);
		for (String s : t.styles) {
			if (!styles.containsKey(t)) styles.put(s, true);
		}
	}

	// TODO more efficient implementation
	public static Tile getTile(String file) {
		for (Tile t : tiles) {
			if (t.file.getName().equals(file)) return t;
		}
		return null;
	}

	// TODO more efficient implementation
	public static Set<String> getSets() {
		return sets.keySet();
	}

	// TODO more efficient implementation
	public static Set<String> getStyles() {
		return styles.keySet();
	}

	public static List<Tile> getTiles() {
		List<Tile> list = new ArrayList<Tile>();
		for (Tile t : tiles) {
			if (!sets.containsKey(t.tileSet) || !sets.get(t.tileSet).booleanValue()) continue; // set not visible; skip
			for (String s : t.styles) {
				if (styles.get(s).booleanValue()) {
					list.add(t);
					break;
				}
			}
		}
		return list;
	}

	public static void setSetVisible(String text, boolean state) {
		if (sets.containsKey(text)) {
			sets.put(text, state);
		}
	}

	public static void setStyleVisible(String text, boolean state) {
		if (styles.containsKey(text)) {
			styles.put(text, state);
		}
	}


//	public static void scanDirectory(File dir) {
//		File[] dirFiles = dir.listFiles();
//		for (File f : dirFiles) {
//			if (f.isDirectory()) {
//				scanDirectory(f);
//			} else {
//				try {
//					Tile t = new Tile();
//					t.file = f;
//					t.image = ImageIO.read(f);
//					System.out.println("Loaded "+f+" width = "+t.image.getWidth()+", height = "+t.image.getHeight());
//					addTile(t);
//				} catch (IOException e) {
//					System.out.println("Failed to load image from "+f+": "+e.getMessage());
//				}
//			}
//		}
//	}

	public static void scanDirectory(File dir) {
		File[] dirFiles = dir.listFiles();
		for (File f : dirFiles) {
			if (f.isDirectory()) {
				scanDirectory(f);
			} else if (f.getName().endsWith(".xml")) {
				tileSets.addAll(readXMLConfig(f));
			}
		}
	}

	public static List<TileSet> readXMLConfig(File xmlFile) {
		ArrayList<TileSet> sets = new ArrayList<TileSet>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", "file:tilemapper/tiles.xsd");
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler() {
				@Override
				public void error(SAXParseException e) throws SAXException {
					System.err.println(e);
				}
				@Override
				public void fatalError(SAXParseException e) {
					System.err.println(e);
				}
				@Override
				public void warning(SAXParseException e) throws SAXException {
					System.err.println(e);
				}
			});
			Document dom = builder.parse(xmlFile);

			// we have a valid document
			//printNode(dom, "");
			NodeList nodes = dom.getChildNodes();
			for (int i=0; i<nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName() == "TileSet") {
					TileSet ts = TileSet.parseTileSet((Element) node, xmlFile.getParentFile());
					sets.add(ts);
				}
			}

		} catch (ParserConfigurationException e) {
			System.err.println("The underlying parser does not support the requested features.");
			System.err.println(e);
		} catch (FactoryConfigurationError e) {
			System.err.println("Error occurred obtaining Document Builder Factory.");
			System.err.println(e);
		} catch (SAXParseException e) {
			System.err.println(e);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return sets;
	}
}

package tilemapper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

// This class sends an "addTile" PropertyChange event when a tile is added with the tile as the new value

// XXX maybe this shouldn't be static?
public class TileManager {
	public enum Visibility {
		INCLUDED,	// group of tiles is included in visible tiles. a tile with this style/set will be visible unless it also belongs to an excluded group
		IGNORED,	// group of tiles is ignored. a tile with this style/set will only be visible if it also belongs to an included group (and no excluded groups)
		EXCLUDED	// group of tiles is excluded from visible tiles. a tile with this style/set will be hidden, even if it also belongs to an included group
	}

	static List<Tile> tiles = new ArrayList<>();
	static List<TileSet> tileSets = new ArrayList<>();
	static PropertyChangeSupport pcs = new PropertyChangeSupport(new Object());

	static void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	static void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	static void addTile(Tile t) {
		tiles.add(t);
		pcs.firePropertyChange("addTile", null, t);
	}

	// TODO more efficient implementation
	static Tile getTile(String file) {
		for (Tile t : tiles) {
			if (t.file.getName().equals(file)) return t;
		}
		return null;
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

	static void scanDirectory(File dir) {
		File[] dirFiles = dir.listFiles();
		if (dirFiles == null) {
			System.err.println("No files found in " + dir);
			return;
		}
		for (File f : dirFiles) {
			if (f.isDirectory()) {
				scanDirectory(f);
			} else if (f.getName().endsWith(".xml")) {
				tileSets.addAll(readXMLConfig(f));
			}
		}
	}

	static List<TileSet> readXMLConfig(File xmlFile) {
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

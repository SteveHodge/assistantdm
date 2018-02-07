package tilemapper;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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

// XXX clean up listener stuff. also maybe this shouldn't be static?
public class TileManager {
	public enum Visibility {
		INCLUDED,	// group of tiles is included in visible tiles. a tile with this style/set will be visible unless it also belongs to an excluded group
		IGNORED,	// group of tiles is ignored. a tile with this style/set will only be visible if it also belongs to an included group (and no excluded groups)
		EXCLUDED	// group of tiles is excluded from visible tiles. a tile with this style/set will be hidden, even if it also belongs to an excluded group
	}

	static List<Tile> tiles = new ArrayList<>();
	static Map<String, Visibility> sets = new HashMap<>();
	static Map<String, Visibility> styles = new HashMap<>();
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
		if (!sets.containsKey(t.tileSet)) sets.put(t.tileSet, Visibility.INCLUDED);
		for (String s : t.styles) {
			if (!styles.containsKey(t)) styles.put(s, Visibility.IGNORED);
		}
	}

	// TODO more efficient implementation
	static Tile getTile(String file) {
		for (Tile t : tiles) {
			if (t.file.getName().equals(file)) return t;
		}
		return null;
	}

	// TODO more efficient implementation
	static Set<String> getSets() {
		return sets.keySet();
	}

	// TODO more efficient implementation
	static Set<String> getStyles() {
		return styles.keySet();
	}

	static JPanel getStylesPanel() {
		return getFilterPanel(styles, "Style", "styles");
	}

	static JPanel getSetsPanel() {
		return getFilterPanel(sets, "Set", "sets");
	}

	private static JPanel getFilterPanel(Map<String, Visibility> map, String title, String propName) {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridy = 0;
		c.insets = new Insets(0, 4, 0, 4);
		p.add(new JLabel("Include"), c);
		p.add(new JLabel("Ignore"), c);
		p.add(new JLabel("Exclude"), c);
		c.anchor = GridBagConstraints.WEST;
		p.add(new JLabel(title), c);

		ArrayList<String> names = new ArrayList<String>(map.keySet());
		Collections.sort(names);
		for (String s : names) {
			Visibility vis = map.get(s);

			c.gridy++;
			c.anchor = GridBagConstraints.CENTER;
			ButtonGroup group = new ButtonGroup();

			JRadioButton inc = new JRadioButton();
			inc.addActionListener(e -> {
				map.put(s, Visibility.INCLUDED);
				pcs.firePropertyChange(propName, s, Visibility.INCLUDED);
			});
			inc.setSelected(vis == Visibility.INCLUDED);
			group.add(inc);
			p.add(inc, c);

			JRadioButton ign = new JRadioButton();
			ign.addActionListener(e -> {
				map.put(s, Visibility.IGNORED);
				pcs.firePropertyChange(propName, s, Visibility.IGNORED);
			});
			ign.setSelected(vis == Visibility.IGNORED);
			group.add(ign);
			p.add(ign, c);

			JRadioButton exc = new JRadioButton();
			exc.addActionListener(e -> {
				map.put(s, Visibility.EXCLUDED);
				pcs.firePropertyChange(propName, s, Visibility.EXCLUDED);
			});
			group.add(exc);
			p.add(exc, c);

			c.anchor = GridBagConstraints.WEST;
			p.add(new JLabel(s), c);
		}
		return p;
	}

	static List<Tile> getTiles() {
		List<Tile> list = new ArrayList<Tile>();
		for (Tile t : tiles) {
			if (!sets.containsKey(t.tileSet) || sets.get(t.tileSet) == Visibility.EXCLUDED) continue; // set not visible; skip
			boolean include = false;
			for (String s : t.styles) {
				if (styles.get(s) == Visibility.EXCLUDED) {
					include = false;
					break;
				} else if (styles.get(s) == Visibility.INCLUDED) {
					include = true;
				}
			}
			if (include) list.add(t);
		}
		return list;
	}

	static void setSetVisible(String text, boolean include) {
		if (sets.containsKey(text)) {
			sets.put(text, include ? Visibility.INCLUDED : Visibility.IGNORED);
			pcs.firePropertyChange("sets", text, include);
		}
	}

	static void setStyleVisible(String text, boolean state) {
		if (styles.containsKey(text)) {
			styles.put(text, state ? Visibility.INCLUDED : Visibility.IGNORED);
			pcs.firePropertyChange("styles", text, state);
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

	static void scanDirectory(File dir) {
		File[] dirFiles = dir.listFiles();
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

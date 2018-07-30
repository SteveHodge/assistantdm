package tilemapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
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

public class ValidateLibrary {

	public static void main(String[] args) {
		File tileDir = new File("media/Tiles");
		System.out.println(tileDir.getAbsolutePath());
		scanDirectory(tileDir);
	}

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
				readXMLConfig(f);
			}
		}
	}

	static void readXMLConfig(File xmlFile) {
//		ArrayList<TileSet> sets = new ArrayList<TileSet>();
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
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName() == "TileSet") {
					parseTileSet((Element) node, xmlFile.getParentFile());
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
	}

	private static Map<String, String> tileNames = new HashMap<String, String>();

	public static void parseTileSet(Element node, File dir) {
		String name = node.getAttribute("name");
		String style = node.getAttribute("style");
		Set<String> styles = new HashSet<>();
		if (style.length() > 0) {
			for (String s : style.split(";")) {
				styles.add(s);
			}
		}
		String widthStr = node.getAttribute("width");
		int width = 6;
		if (widthStr.length() > 0) width = Integer.parseInt(widthStr);
		String heightStr = node.getAttribute("height");
		int height = 6;
		if (heightStr.length() > 0) height = Integer.parseInt(heightStr);

		Map<String, Integer> abbr = new HashMap<>();
		String digits = "0123456789";
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) n;
			if (e.getNodeName() == "Tile") {
				String fileName = parseTile(e, dir, name, styles, width, height);
				if (tileNames.containsKey(fileName)) System.err.println("Duplicate tile name " + fileName + " (original in " + tileNames.get(fileName) + ")");
				tileNames.put(fileName, dir.getAbsolutePath());

				String a = "";
				for (int j = 0; j < fileName.length(); j++) {
					if (digits.contains(Character.toString(fileName.charAt(j)))) {
						a = fileName.substring(0, j);
						break;
					}
				}

				if (a.length() > 0) {
					if (abbr.containsKey(a)) {
						abbr.put(a, abbr.get(a) + 1);
					} else {
						abbr.put(a, 1);
					}
				}
			}
		}

		String abbrStr = "";
		for (String a : abbr.keySet()) {
			if (abbrStr.length() > 0)
				abbrStr += ", ";
			if (abbr.size() == 1) {
				abbrStr = "'" + a + "'";
			} else if (abbr.size() > 1) {
				abbrStr += "'" + a + "' (x" + abbr.get(a) + ")";
			}
		}

		System.out.println("Read tileset " + name + ", product-code = " + node.getAttribute("product-code") + ", style = " + styles + ", width = " + width + ", height = " + height
				+ " abbreviation  = " + abbrStr);
	}

	public static String parseTile(Element node, File dir, String tileset, Set<String> style, int width, int height) {
		File file = null;
		BufferedImage image = null;
		Set<String> styles = new HashSet<String>();

		String widthStr = node.getAttribute("width");
		if (widthStr.length() > 0) width = Integer.parseInt(widthStr);
		String heightStr = node.getAttribute("height");
		if (heightStr.length() > 0) height = Integer.parseInt(heightStr);
		String styleStr = node.getAttribute("style");
		if (styleStr.length() > 0) {
			for (String s : styleStr.split(";")) {
				if (!s.equals("")) styles.add(s);
			}
		} else if (style != null) {
			styles.addAll(style);
		}

		ArrayList<String> errors = new ArrayList<>();
		file = new File(dir, node.getAttribute("file"));
		image = checkImage("Main", file, width, height, errors, true);

		checkImage("Small", findImage(new File(dir, "Small"), node.getAttribute("file")), width, height, errors, false);

		if (errors.size() > 0) {
			System.out.print("  Tile: " + file + ", style = " + styles + ", width = " + width + ", height = " + height);
			if (image != null)
				System.out.print( ", image = " + image.getWidth() + " x " + image.getHeight());
			System.out.println(", error = " + String.join(", ", errors));
		}
		return file.getName();
	}

	static BufferedImage checkImage(String desc, File file, int width, int height, List<String> errors, boolean wantHiRes) {
		if (file == null) {
			errors.add("No " + desc.toLowerCase() + " image found");
		} else {
			try {
				BufferedImage image = ImageIO.read(file);
				if (!wantHiRes && (image.getWidth() / width > 50 || image.getHeight() / height > 50)) {
					errors.add(desc + " image is hi-res (" + (image.getWidth() / width) + "/" + (image.getHeight() / height) + ")");
				} else if (wantHiRes && (image.getWidth() / width < 149 || image.getHeight() / height < 149)) {
					errors.add(desc + " image is lo-res (" + (image.getWidth() / width) + "/" + (image.getHeight() / height) + ")");
				}
				if (Math.abs(((double) image.getWidth() / image.getHeight()) - ((double) width / height)) > 0.1d) {
					errors.add(String.format("%s image aspect ratio wrong (%.3f v %.3f)", desc, ((double) image.getWidth() / image.getHeight()), ((double) width / height)));
				}
				return image;
			} catch (IOException ex) {
				errors.add("Failed to load " + desc.toLowerCase() + " image from " + file.getAbsolutePath() + ": " + ex.getMessage());
			}
		}
		return null;
	}

	static File findImage(File dir, String file) {
		String[] types = {"jpg","png"};
		if (file.contains(".")) {
			file = file.substring(0, file.indexOf('.'));
		}
		for (String type : types) {
			File f = new File(dir, file + "." + type);
			if (f.exists()) return f;
		}
		return null;
	}
}

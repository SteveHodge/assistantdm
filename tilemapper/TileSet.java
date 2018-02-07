package tilemapper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */

public class TileSet {
	public String name;
	public File directory;
	public Set<String> styles = new HashSet<>();
	int width=6;
	int height=6;
	public ArrayList<Tile> tiles = new ArrayList<>();

	public String getXML(String indent) {
		StringBuilder s = new StringBuilder();
		s.append(indent)
		.append("<TileSet name=\"")
		.append(name)
		.append("\" directory=\"Images/")
		.append(directory.getName())
		.append("\" style=\"")
		.append(styles);
		if (width != 6 || height != 6) {
			s.append("\" width=\"")
			.append(width)
			.append("\" height=\"")
			.append(height);
		}
		s.append("\">")
		.append("\n");
		for (Tile t : tiles) {
			s.append(t.getXML(indent + "\t", styles, width, height)).append("\n");
		}
		s.append(indent).append("</TileSet>");
		return s.toString();
	}

	private static Map<String, String> tileNames = new HashMap<String, String>();

	public static TileSet parseTileSet(Element node, File dir) {
		TileSet ts = new TileSet();
		ts.name = node.getAttribute("name");
		ts.directory = dir;	//new File(node.getAttribute("directory"));
		String style = node.getAttribute("style");
		if (style.length() > 0) {
			for (String s : style.split(";")) {
				ts.styles.add(s);
			}
		}
		String widthStr = node.getAttribute("width");
		ts.width = 6;
		if (widthStr.length()>0) ts.width = Integer.parseInt(widthStr);
		String heightStr = node.getAttribute("height");
		ts.height = 6;
		if (heightStr.length()>0) ts.height = Integer.parseInt(heightStr);

		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) n;
			if (e.getNodeName() == "Tile") {
				Tile t = Tile.parseDOM(e, ts.directory, ts.name, ts.styles, ts.width, ts.height);
				ts.tiles.add(t);
				TileManager.addTile(t);
				if (tileNames.containsKey(t.file.getName())) System.err.println("Duplicate tile name " + t.file.getName() + " (original in " + tileNames.get(t.file.getName()) + ")");
				tileNames.put(t.file.getName(), ts.directory.getAbsolutePath());
				//System.out.println("Loaded "+t);
			}
		}

		String abbr = "";
		if (ts.tiles.size() > 0) {
			String digits = "0123456789";
			abbr = ts.tiles.get(0).file.getName();
			for (int i = 0; i < abbr.length(); i++) {
				if (digits.contains(Character.toString(abbr.charAt(i)))) {
					abbr = abbr.substring(0, i);
					break;
				}
			}
		}
		System.out.println("Read tileset " + ts.name + ", product-code = " + node.getAttribute("product-code") + ", style = " + ts.styles + ", width = " + ts.width + ", height = " + ts.height
				+ " abbreviation  = '" + abbr + "'");

		return ts;
	}
}
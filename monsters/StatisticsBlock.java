package monsters;

import gamesystem.AbilityScore.Type;
import gamesystem.CR;
import gamesystem.SizeCategory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import party.Monster;
import xml.LocalEntityResolver;

/**
 * Represents a parsed statistics block from a source (usual an HTML table). The values stored in the
 * StatisticsBlock are accessed via keys (Property values). The raw values are strings which may contain
 * any text. Specific getX() methods attempt to further parse concrete values from these strings.
 * 
 * This is an immutable type.
 * 
 * @author Steve
 * 
 */

public class StatisticsBlock {
	public enum Property {
		NAME("Name:"),
		URL("URL:"),

		SIZE_TYPE("Size/Type:"),
		HITDICE("Hit Dice:"),
		INITIATIVE("Initiative:"),
		SPEED("Speed:"),
		AC("Armor Class:"),
		BASE_ATTACK_GRAPPLE("Base Attack/Grapple:"),
		ATTACK("Attack:"),
		FULL_ATTACK("Full Attack:"),
		SPACE_REACH("Space/Reach:"),
		SPECIAL_ATTACKS("Special Attacks:"),
		SPECIAL_QUALITIES("Special Qualities:"),
		SAVES("Saves:"),
		ABILITIES("Abilities:"),
		SKILLS("Skills:"),
		FEATS("Feats:"),
		ENVIRONMENT("Environment:"),
		ORGANIZATION("Organization:"),
		CR("Challenge Rating:"),
		TREASURE("Treasure:"),
		ALIGNMENT("Alignment:"),
		ADVANCEMENT("Advancement:"),
		LEVEL_ADJUSTMENT("Level Adjustment:");
//		'Type:' - from the dragon pages

		@Override
		public String toString() {return label;}

		public static Property fromString(String p) {
			// TODO more efficient implementation
			for (Property prop : Property.values()) {
				if (prop.toString().equals(p)) return prop;
			}
			return null;
		}

		public static Property[] getStandardOrder() {
			return Arrays.copyOf(standardOrder, standardOrder.length);
		}

		private static final Property[] standardOrder = {
			SIZE_TYPE,
			HITDICE,
			INITIATIVE,
			SPEED,
			AC,
			BASE_ATTACK_GRAPPLE,
			ATTACK,
			FULL_ATTACK,
			SPACE_REACH,
			SPECIAL_ATTACKS,
			SPECIAL_QUALITIES,
			SAVES,
			ABILITIES,
			SKILLS,
			FEATS,
			ENVIRONMENT,
			ORGANIZATION,
			CR,
			TREASURE,
			ALIGNMENT,
			ADVANCEMENT,
			LEVEL_ADJUSTMENT
		};

		private Property(String l) {label = l;}

		private String label;
	}

	static final String STATBLOCKCLASS = "statBlock";

	private Source source;
	private Map<Property, String> properties = new HashMap<Property, String>();

	String get(Property key) {
		return properties.get(key);
	}

	String getName() {
		return get(Property.NAME);
	}

	URL getURL() throws MalformedURLException {
		return new URL(get(Property.URL));
	}

	// type of the creature
	// property has format "<Size> <Type> [(Subtypes)]"
	String getType() {
		String sizeType = get(Property.SIZE_TYPE);
		if (sizeType == null || sizeType.indexOf(' ') < 1) return sizeType;
		sizeType = sizeType.substring(sizeType.indexOf(' ')+1);
		if (sizeType.indexOf('(') > 1) {
			return sizeType.substring(0, sizeType.indexOf('(')).trim();
		}
		return sizeType;
	}

	private String[] getSubtypes() {
		String sizeType = get(Property.SIZE_TYPE);
		if (sizeType == null || sizeType.indexOf('(') < 0) return null;
		sizeType = sizeType.substring(sizeType.indexOf('(') + 1);
		if (sizeType.indexOf(')') >= 0) {
			sizeType = sizeType.substring(0,sizeType.indexOf(')'));
		}
		return sizeType.split("\\s*,\\s*");
	}

	/**
	 * Parses the SIZE_TYPE property value and returns the size category (which is the first word of the property).
	 * If SIZE_TYPE can't be parsed then null is returned
	 * 
	 * @return the SizeCategory of the creature or null
	 */
	SizeCategory getSize() {
		String sizeType = get(Property.SIZE_TYPE);
		if (sizeType == null || sizeType.indexOf(' ') < 1) return null;
		return SizeCategory.getSize(sizeType.substring(0, sizeType.indexOf(' ')));
	}

	/**
	 * Parses the SPACE_REACH property value and returns the space taken by the creature in 6" units.
	 * The SPACE_REACH value should be of the form "X ft./...". X should be an integer or either "2 1/2" or "2½".
	 * If SPACE_REACH has no value or it can't be parsed to extract the space value then -1 is returned
	 * 
	 * @return the space taken by the creature in 6" units or -1 if the SPACE_REACH property can't be parsed
	 */
	int getSpace() {
		String space = get(Property.SPACE_REACH);
		if (space == null || space.indexOf(" ft./") < 1) return -1;
		space = space.substring(0, space.indexOf(" ft./"));
		if (space.equals("2 1/2") || space.equals("2½")) return 5;
		int s = -1;
		try {
			s = Integer.parseInt(space) * 2;
		} catch (NumberFormatException e) {
			// will default to -1 if we can't parse
		}
		return s;
	}

	/**
	 * Parses the SPACE_REACH property value and returns the normal reach of the creature in feet.
	 * The SPACE_REACH value should be of the form ".../X ft....". X should be an integer.
	 * Any additional information (e.g. "20 ft. with tentacles") is ignored.
	 * If SPACE_REACH has no value or it can't be parsed to extract the reach value then -1 is returned
	 * 
	 * @return the reach of the creature in feet or -1 if the SPACE_REACH property can't be parsed
	 */
	int getReach() {
		String reach = get(Property.SPACE_REACH);
		if (reach == null || reach.indexOf(" ft./") < 1) return -1;
		reach = reach.substring(reach.indexOf(" ft./") + 5);
		if (reach.indexOf(" ft.") < 1) return -1;
		reach = reach.substring(0, reach.indexOf(" ft."));
		int s = -1;
		try {
			s = Integer.parseInt(reach);
		} catch (NumberFormatException e) {
			// will default to -1 if we can't parse
		}
		return s;
	}

	// format of property is:
	// Str 25, Dex 10, Con —, Int 1, Wis 11, Cha 1
	// returns -1 for a missing ability
	// TODO should throw exceptions for invalid formats (or at least return -1)
	int getAbilityScore(Type strength) {
		String abilitiesStr = get(Property.ABILITIES);
		String[] abilities = abilitiesStr.split("\\s*,\\s*");
		String a = abilities[strength.ordinal()].substring(abilities[strength.ordinal()].indexOf(' ')+1);
		if (a.equals("-") || a.equals("—") || a.equals("Ø")) return -1;
		return Integer.parseInt(a);
	}

	private CR getCR() {
		String s = get(Property.CR);
		if (s.equals("¼")) s = "1/4";
		if (s.equals("½")) s = "1/2";
		try {
			return new CR(s);
		} catch (NumberFormatException e) {
			// failed to parse, generally this means there was a note in the CR field
			// we could try to parse the initial number, but for now we'll just return null
			//System.out.println("Failed to parse '"+s+"' as CR");
			return null;
		}
	}

	Source getSource() {
		return source;
	}

	private int getInitiativeModifier() {
		int mod = 0;

		String init = get(Property.INITIATIVE);
		if (init == null) {
			System.out.println("WARN: "+getName()+" has no initiative");
			return mod;
		}
		if (init.startsWith("+")) init = init.substring(1);	// strip any leading "+"
		try {
			mod = Integer.parseInt(init);
		} catch (NumberFormatException e) {
			System.out.println(getName()+": "+e);
		}
		return mod;
	}

	// parse hitdice:
	// pattern for a dice roll is "#d#[+#]"
	// multiple dice rolls can be separated by " plus "
	// hitdice section ends with " (# hp)"
	// first number may be "½ "
	HitDice getHitDice() {
		String hd = get(Property.HITDICE);
		if (hd == null || hd.indexOf(" (") < 0) {
			System.out.println("WARN: "+getName()+" has no default hp ending hitdice");
			return null;
		}
		hd = hd.substring(0,hd.indexOf(" ("));
		return HitDice.parse(hd);
	}

	// parse default hitpoints:
	// pattern is "<hitdice> (# hp)"
	private int getDefaultHPs() {
		int hp = 0;
		String hps = get(Property.HITDICE);
		if (hps != null && hps.indexOf(" (") > 0 && hps.indexOf(" hp)") > 0) {
			hps = hps.substring(hps.indexOf(" (")+2,hps.indexOf(" hp)"));
			//System.out.println(block.get("Name:")+"HPs: "+hps);
			try {
				hp = Integer.parseInt(hps);
			} catch (NumberFormatException e) {
				System.out.println(getName()+": "+e);
			}

		} else {
			System.out.println("WARN: "+getName()+" has no default hp entry");
		}
		return hp;
	}

	// parse armor class:
	// pattern is "# (components), touch #, flat-footed #"
	private int[] getACs() {
		int[] acs = new int[3];
		acs[0] = 0; acs[1] = 0; acs[2] = 0;

		String ac = get(Property.AC);
		if (ac == null) {
			System.out.println("WARN: "+getName()+" has no AC");
			return acs;
		}
		int i = ac.indexOf(", touch ");
		if (i == -1) {
			System.out.println("WARN: "+getName()+" couldn't locate ', touch '");
			return acs;
		}
		int j = ac.indexOf(", flat-footed ");
		if (j == -1) {
			System.out.println("WARN: "+getName()+" couldn't locate ', touch '");
			return acs;
		}
		String fullAC = ac.substring(0, i);
		if (fullAC.indexOf(" (") > -1) {
			fullAC = fullAC.substring(0, fullAC.indexOf(" ("));
		}
		String touchAC = ac.substring(i+8,j);
		String ffAC = ac.substring(j+14);

		try {
			acs[0] = Integer.parseInt(fullAC);
		} catch (NumberFormatException e) {
			System.out.println(getName()+": "+e);
		}
		try {
			acs[1] = Integer.parseInt(touchAC);
		} catch (NumberFormatException e) {
			System.out.println(getName()+": "+e);
		}
		try {
			acs[2] = Integer.parseInt(ffAC);
		} catch (NumberFormatException e) {
			System.out.println(getName()+": "+e);
		}

		return acs;
	}

	// TODO should download the URL directly rather than converting to a file. should add Source argument
	static List<StatisticsBlock> parseURL(URL url) {
		try {
			// first remove any fragment from the URL:
			URL u = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
			File f = new File(u.toURI());
			return parseFile(null, f);
		} catch (URISyntaxException ex) {
			ex.printStackTrace();
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	// TODO remove this version - source should always be required
	static List<StatisticsBlock> parseFile(File file) {
		return parseFile(null, file);
	}

	static List<StatisticsBlock> parseFile(Source source, File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document dom;
		List<StatisticsBlock> blocks = new ArrayList<StatisticsBlock>();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new LocalEntityResolver());
			dom = builder.parse(file);

			NodeList htmlBodies = dom.getElementsByTagName("body");
			if (htmlBodies.getLength() != 1) {
				System.out.println("Expected exactly one body tag, found "+htmlBodies.getLength());
				return blocks;
			}

			NodeList children = ((Element)htmlBodies.item(0)).getChildNodes();
			String name = "";
			URL url = null;
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element child = (Element)children.item(i);
					if (child.getTagName().equals("h1")) {
						name = child.getTextContent();
						if (source != null) {
							url = new File(source.getLocation() + "\\" + file.getName()).toURI().toURL();
						} else {
							url = file.toURI().toURL();
						}
						//System.out.println("h1 name = "+name);

					} else if (child.getTagName().equals("h2")) {
						// h2 tag. if this has an child anchor then this is a new monster name
						NodeList anchors = child.getElementsByTagName("a");
						if (anchors.getLength() > 0) {
							name = child.getTextContent();
							Element a = (Element)anchors.item(0);
							if (source != null) {
								url = new File(source.getLocation() + "\\" + file.getName()).toURI().toURL();
								url = new URL(url, "#" + a.getAttribute("name"));
							} else {
								url = new URL(file.toURI().toURL(), "#" + a.getAttribute("name"));
							}
							//System.out.println("h2 name = "+name);
						}

					} else if (child.getTagName().equals("table")) {
						String classString = child.getAttribute("class");
						if (classString != null && classString.contains(STATBLOCKCLASS)) {
							for (StatisticsBlock block : parseStatBlock(child, name, url.toString())) {
								block.source = source;
								blocks.add(block);
							}
						}
					}
				}
			}


		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return blocks;
	}

	private static List<StatisticsBlock> parseStatBlock(Element table, String defaultName, String url) {
		//System.out.println("Found stat block");
		List<StatisticsBlock> statsBlock = new ArrayList<StatisticsBlock>();

		// fetch the rows...
		NodeList rows = table.getElementsByTagName("tr");
		if (rows.getLength() > 23) {
			// note this check is not precise - some block have fewer than 22 rows so even 23 or 23 could mean extra rows
			System.out.println("WARN: extra rows found in "+url);
		}
		for (int j = 0; j < rows.getLength(); j++) {
			Element row = (Element)rows.item(j);
			// row children...
			//System.out.println("Found row "+j);
			NodeList children = row.getChildNodes();
			String stat = "";
			int col = 0;
			for (int k = 0; k < children.getLength(); k++) {
				Node node = children.item(k);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element)node;
					if (el.getTagName().equals("th") || el.getTagName().equals("td")) {
						//System.out.println("Found data: "+el.getTextContent().trim());
						if (col == 0) {
							stat = el.getTextContent().trim();
						} else {
							while (col > statsBlock.size()) {
								StatisticsBlock block = new StatisticsBlock();
								block.properties.put(Property.NAME, defaultName);
								block.properties.put(Property.URL, url);
								statsBlock.add(block);
								//System.out.println("Added block for "+col);
							}
							StatisticsBlock block = statsBlock.get(col-1);
							if (stat.equals("")) {
								block.properties.put(Property.NAME, el.getTextContent().trim());
								//System.out.println("Set name to "+block.properties.get(Property.NAME));
							} else {
								Property p = Property.fromString(stat);
								if (p != null) block.properties.put(p, el.getTextContent().trim());
								//System.out.println(""+col+": "+stat+" = "+el.getTextContent());
							}
						}
						col++;
					} else {
						//System.out.println("Found unknown element: "+el.getTagName());
					}
				} else {
					//System.out.println("Found unknown child of row: "+node.getNodeName()+", contains '"+node.getNodeValue()+"'");
				}
			}
		}
		return statsBlock;
	}

	public String getHTML() {
		StringBuilder s = new StringBuilder();
		s.append("<html><table><tr><td></td><td>").append(getName()).append("</td></tr>");
		for (Property p : Property.getStandardOrder()) {
			s.append("<tr><td>").append(p).append("</td><td>").append(get(p)).append("</td></tr>");
		}
		s.append("</table></html>");
		return s.toString();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		String nl = System.getProperty("line.separator");
		s.append(getName()).append(nl);
		for (Property p : Property.getStandardOrder()) {
			s.append(p).append(" ").append(get(p)).append(nl);
		}
		return s.toString();
	}

	public Monster createMonster() {
		Monster m = new Monster();
		m.setName(getName());
		int[] ac = getACs();
		m.setAC(ac[0]);
		m.setTouchAC(ac[1]);
		m.setFlatFootedAC(ac[2]);
		m.setMaximumHitPoints(getDefaultHPs());
		m.setInitiativeModifier(getInitiativeModifier());
		m.setSpace(getSpace());
		m.setReach(getReach());
		m.setSize(getSize());
		m.setStatisticsBlock(this);
		return m;
	}
}

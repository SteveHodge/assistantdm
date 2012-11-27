package monsters;

import gamesystem.AbilityScore.Type;
import gamesystem.CR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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


import xml.LocalEntityResolver;

public class StatisticsBlock {
	// TODO convert to enum?
	public final static String PROPERTY_NAME = "Name:";
	public final static String PROPERTY_URL = "URL:";

	public final static String PROPERTY_SIZE_TYPE = "Size/Type:";
	public final static String PROPERTY_HITDICE = "Hit Dice:";
	public final static String PROPERTY_INITIATIVE = "Initiative:";
	public final static String PROPERTY_SPEED = "Speed:";
	public final static String PROPERTY_AC = "Armor Class:";
	public final static String PROPERTY_BASE_ATTACK_GRAPPLE = "Base Attack/Grapple:";
	public final static String PROPERTY_ATTACK = "Attack:";
	public final static String PROPERTY_FULL_ATTACK = "Full Attack:";
	public final static String PROPERTY_SPACE_REACH = "Space/Reach:";
	public final static String PROPERTY_SPECIAL_ATTACKS = "Special Attacks:";
	public final static String PROPERTY_SPECIAL_QUALITIES = "Special Qualities:";
	public final static String PROPERTY_SAVES = "Saves:";
	public final static String PROPERTY_ABILITIES = "Abilities:";
	public final static String PROPERTY_SKILLS = "Skills:";
	public final static String PROPERTY_FEATS = "Feats:";
	public final static String PROPERTY_ENVIRONMENT = "Environment:";
	public final static String PROPERTY_ORGANIZATION = "Organization:";
	public final static String PROPERTY_CR = "Challenge Rating:";
	public final static String PROPERTY_TREASURE = "Treasure:";
	public final static String PROPERTY_ALIGNMENT = "Alignment:";
	public final static String PROPERTY_ADVANCEMENT = "Advancement:";
	public final static String PROPERTY_LEVEL_ADJUSTMENT = "Level Adjustment:";

//	'Type:' - from the dragon pages

	static final String STATBLOCKCLASS = "statBlock";

	protected Source source;
	protected Map<String,String> properties  = new HashMap<String,String>();

	public String get(String key) {
		return properties.get(key);
	}

	public String getName() {
		return get(PROPERTY_NAME);
	}

	// type of the creature
	// property has format "<Size> <Type> [(Subtypes)]"
	public String getType() {
		String sizeType = get(PROPERTY_SIZE_TYPE);
		if (sizeType == null || sizeType.indexOf(' ') < 1) return sizeType;
		sizeType = sizeType.substring(sizeType.indexOf(' ')+1);
		if (sizeType.indexOf('(') > 1) {
			return sizeType.substring(0, sizeType.indexOf('(')).trim();
		}
		return sizeType;
	}

	public String[] getSubtypes() {
		String sizeType = get(PROPERTY_SIZE_TYPE);
		if (sizeType == null || sizeType.indexOf('(') < 0) return null;
		sizeType = sizeType.substring(sizeType.indexOf('(') + 1);
		if (sizeType.indexOf(')') >= 0) {
			sizeType = sizeType.substring(0,sizeType.indexOf(')'));
		}
		return sizeType.split("\\s*,\\s*");
	}

	// first word of size/type property
	// TODO could verify size is valid
	public String getSize() {
		String sizeType = get(PROPERTY_SIZE_TYPE);
		if (sizeType == null || sizeType.indexOf(' ') < 1) return sizeType;
		return sizeType.substring(0, sizeType.indexOf(' '));
	}

	// format of property is:
	// Str 25, Dex 10, Con —, Int 1, Wis 11, Cha 1
	// returns -1 for a missing ability
	// TODO should throw exceptions for invalid formats (or at least return -1)
	public int getAbilityScore(Type strength) {
		String abilitiesStr = get(PROPERTY_ABILITIES);
		String[] abilities = abilitiesStr.split("\\s*,\\s*");
		String a = abilities[strength.ordinal()].substring(abilities[strength.ordinal()].indexOf(' ')+1);
		if (a.equals("-") || a.equals("—") || a.equals("Ø")) return -1;
		return Integer.parseInt(a);
	}

	public CR getCR() {
		String s = get(PROPERTY_CR);
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

	public Source getSource() {
		return source;
	}

	public int getInitiativeModifier() {
		int mod = 0;

		String init = get(PROPERTY_INITIATIVE);
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
	public HitDice getHitDice() {
		String hd = get(PROPERTY_HITDICE);
		if (hd == null || hd.indexOf(" (") < 0) {
			System.out.println("WARN: "+getName()+" has no default hp ending hitdice");
			return null;
		}
		hd = hd.substring(0,hd.indexOf(" ("));
		return HitDice.parse(hd);
	}

	// parse default hitpoints:
	// pattern is "<hitdice> (# hp)"
	public int getDefaultHPs() {
		int hp = 0;
		String hps = get(PROPERTY_HITDICE);
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
	public int[] getACs() {
		int[] acs = new int[3];
		acs[0] = 0; acs[1] = 0; acs[2] = 0;
	
		String ac = get(PROPERTY_AC);
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

	// TODO remove this version - source should always be required
	public static List<StatisticsBlock> parseFile(File file) {
		return parseFile(null, file);
	}

	public static List<StatisticsBlock> parseFile(Source source, File file) {
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
			String url = "";
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element child = (Element)children.item(i);
					if (child.getTagName().equals("h1")) {
						name = child.getTextContent();
						if (source != null) {
							url = source.getLocation()+"\\"+file.getName();
						} else {
							url = file.getName();
						}
						//System.out.println("h1 name = "+name);
						
					} else if (child.getTagName().equals("h2")) {
						// h2 tag. if this has an child anchor then this is a new monster name
						NodeList anchors = child.getElementsByTagName("a");
						if (anchors.getLength() > 0) {
							name = child.getTextContent();
							Element a = (Element)anchors.item(0);
							if (source != null) {
								url = source.getLocation()+"\\"+file.getName()+"#"+a.getAttribute("name");
							} else {
								url = file.getName()+"#"+a.getAttribute("name");
							}
							//System.out.println("h2 name = "+name);
						}
	
					} else if (child.getTagName().equals("table")) {
						String classString = child.getAttribute("class");
						if (classString != null && classString.contains(STATBLOCKCLASS)) {
							for (StatisticsBlock block : parseStatBlock(child, name, url)) {
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

	public static List<StatisticsBlock> parseStatBlock(Element table, String defaultName, String url) {
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
								block.properties.put(PROPERTY_NAME,defaultName);
								block.properties.put(PROPERTY_URL, url);
								statsBlock.add(block);
								//System.out.println("Added block for "+col);
							}
							StatisticsBlock block = statsBlock.get(col-1);
							if (stat.equals("")) {
								block.properties.put(PROPERTY_NAME,el.getTextContent().trim());
								//System.out.println("Set name to "+block.properties.get(PROPERTY_NAME));
							} else {
								block.properties.put(stat,el.getTextContent().trim());
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
		s.append("<html><table><tr><td></td><td>").append(getName()).append("</td></tr><tr><td>");
		s.append(PROPERTY_SIZE_TYPE).append("</td><td>").append(get(PROPERTY_SIZE_TYPE)).append("</td></tr><tr><td>");
		s.append(PROPERTY_HITDICE).append("</td><td>").append(get(PROPERTY_HITDICE)).append("</td></tr><tr><td>");
		s.append(PROPERTY_INITIATIVE).append("</td><td>").append(get(PROPERTY_INITIATIVE)).append("</td></tr><tr><td>");
		s.append(PROPERTY_SPEED).append("</td><td>").append(get(PROPERTY_SPEED)).append("</td></tr><tr><td>");
		s.append(PROPERTY_AC).append("</td><td>").append(get(PROPERTY_AC)).append("</td></tr><tr><td>");
		s.append(PROPERTY_BASE_ATTACK_GRAPPLE).append("</td><td>").append(get(PROPERTY_BASE_ATTACK_GRAPPLE)).append("</td></tr><tr><td>");
		s.append(PROPERTY_ATTACK).append("</td><td>").append(get(PROPERTY_ATTACK)).append("</td></tr><tr><td>");
		s.append(PROPERTY_FULL_ATTACK).append("</td><td>").append(get(PROPERTY_FULL_ATTACK)).append("</td></tr><tr><td>");
		s.append(PROPERTY_SPACE_REACH).append("</td><td>").append(get(PROPERTY_SPACE_REACH)).append("</td></tr><tr><td>");
		s.append(PROPERTY_SPECIAL_ATTACKS).append("</td><td>").append(get(PROPERTY_SPECIAL_ATTACKS)).append("</td></tr><tr><td>");
		s.append(PROPERTY_SPECIAL_QUALITIES).append("</td><td>").append(get(PROPERTY_SPECIAL_QUALITIES)).append("</td></tr><tr><td>");
		s.append(PROPERTY_SAVES).append("</td><td>").append(get(PROPERTY_SAVES)).append("</td></tr><tr><td>");
		s.append(PROPERTY_ABILITIES).append("</td><td>").append(get(PROPERTY_ABILITIES)).append("</td></tr><tr><td>");
		s.append(PROPERTY_SKILLS).append("</td><td>").append(get(PROPERTY_SKILLS)).append("</td></tr><tr><td>");
		s.append(PROPERTY_FEATS).append("</td><td>").append(get(PROPERTY_FEATS)).append("</td></tr><tr><td>");
		s.append(PROPERTY_ENVIRONMENT).append("</td><td>").append(get(PROPERTY_ENVIRONMENT)).append("</td></tr><tr><td>");
		s.append(PROPERTY_ORGANIZATION).append("</td><td>").append(get(PROPERTY_ORGANIZATION)).append("</td></tr><tr><td>");
		s.append(PROPERTY_CR).append("</td><td>").append(get(PROPERTY_CR)).append("</td></tr><tr><td>");
		s.append(PROPERTY_TREASURE).append("</td><td>").append(get(PROPERTY_TREASURE)).append("</td></tr><tr><td>");
		s.append(PROPERTY_ALIGNMENT).append("</td><td>").append(get(PROPERTY_ALIGNMENT)).append("</td></tr><tr><td>");
		s.append(PROPERTY_ADVANCEMENT).append("</td><td>").append(get(PROPERTY_ADVANCEMENT)).append("</td></tr><tr><td>");
		s.append(PROPERTY_LEVEL_ADJUSTMENT).append("</td><td>").append(get(PROPERTY_LEVEL_ADJUSTMENT)).append("</td></tr><tr><td>");
		return s.toString();
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		String nl = System.getProperty("line.separator");
		s.append(getName()).append(nl);
		s.append(PROPERTY_SIZE_TYPE).append(" ").append(get(PROPERTY_SIZE_TYPE)).append(nl);
		s.append(PROPERTY_HITDICE).append(" ").append(get(PROPERTY_HITDICE)).append(nl);
		s.append(PROPERTY_INITIATIVE).append(" ").append(get(PROPERTY_INITIATIVE)).append(nl);
		s.append(PROPERTY_SPEED).append(" ").append(get(PROPERTY_SPEED)).append(nl);
		s.append(PROPERTY_AC).append(" ").append(get(PROPERTY_AC)).append(nl);
		s.append(PROPERTY_BASE_ATTACK_GRAPPLE).append(" ").append(get(PROPERTY_BASE_ATTACK_GRAPPLE)).append(nl);
		s.append(PROPERTY_ATTACK).append(" ").append(get(PROPERTY_ATTACK)).append(nl);
		s.append(PROPERTY_FULL_ATTACK).append(" ").append(get(PROPERTY_FULL_ATTACK)).append(nl);
		s.append(PROPERTY_SPACE_REACH).append(" ").append(get(PROPERTY_SPACE_REACH)).append(nl);
		s.append(PROPERTY_SPECIAL_ATTACKS).append(" ").append(get(PROPERTY_SPECIAL_ATTACKS)).append(nl);
		s.append(PROPERTY_SPECIAL_QUALITIES).append(" ").append(get(PROPERTY_SPECIAL_QUALITIES)).append(nl);
		s.append(PROPERTY_SAVES).append(" ").append(get(PROPERTY_SAVES)).append(nl);
		s.append(PROPERTY_ABILITIES).append(" ").append(get(PROPERTY_ABILITIES)).append(nl);
		s.append(PROPERTY_SKILLS).append(" ").append(get(PROPERTY_SKILLS)).append(nl);
		s.append(PROPERTY_FEATS).append(" ").append(get(PROPERTY_FEATS)).append(nl);
		s.append(PROPERTY_ENVIRONMENT).append(" ").append(get(PROPERTY_ENVIRONMENT)).append(nl);
		s.append(PROPERTY_ORGANIZATION).append(" ").append(get(PROPERTY_ORGANIZATION)).append(nl);
		s.append(PROPERTY_CR).append(" ").append(get(PROPERTY_CR)).append(nl);
		s.append(PROPERTY_TREASURE).append(" ").append(get(PROPERTY_TREASURE)).append(nl);
		s.append(PROPERTY_ALIGNMENT).append(" ").append(get(PROPERTY_ALIGNMENT)).append(nl);
		s.append(PROPERTY_ADVANCEMENT).append(" ").append(get(PROPERTY_ADVANCEMENT)).append(nl);
		s.append(PROPERTY_LEVEL_ADJUSTMENT).append(" ").append(get(PROPERTY_LEVEL_ADJUSTMENT)).append(nl);
		return s.toString();
	}
}

package monsters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StatisticsBlock {
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

	protected Map<String,String> properties  = new HashMap<String,String>();

	public String get(String key) {
		return properties.get(key);
	}

	public String getName() {
		return get(PROPERTY_NAME);
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
	public String getHitDice() {
		String hd = get(PROPERTY_HITDICE);
		if (hd == null || hd.indexOf(" (") < 0) {
			System.out.println("WARN: "+getName()+" has no default hp ending hitdice");
			return null;
		}
		hd = hd.substring(0,hd.indexOf(" ("));
		String[] rolls = hd.split(" plus ");
		for (String roll : rolls) {
			System.out.print("|"+roll);
			// should split the roll into dice number, dice type, modifier and fix fractional dice number
		}
		System.out.println("|");
		return hd;
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

	public static List<StatisticsBlock> parseFile(File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document dom;
		List<StatisticsBlock> blocks = new ArrayList<StatisticsBlock>();
		try {
			dom = factory.newDocumentBuilder().parse(file);
	
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
						url = "monster_manual\\"+file.getName();
						//System.out.println("h1 name = "+name);
						
					} else if (child.getTagName().equals("h2")) {
						// h2 tag. if this has an child anchor then this is a new monster name
						NodeList anchors = child.getElementsByTagName("a");
						if (anchors.getLength() > 0) {
							name = child.getTextContent();
							Element a = (Element)anchors.item(0);
							url = "monster_manual\\"+file.getName()+"#"+a.getAttribute("name");
							//System.out.println("h2 name = "+name);
						}
	
					} else if (child.getTagName().equals("table")) {
						String classString = child.getAttribute("class");
						if (classString != null && classString.contains(STATBLOCKCLASS)) {
							for (StatisticsBlock block : parseStatBlock(child, name, url)) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return blocks;
	}

	public static List<StatisticsBlock> parseStatBlock(Element table, String defaultName, String url) {
		//System.out.println("Found stat block");
		List<StatisticsBlock> statsBlock = new ArrayList<StatisticsBlock>();
	
		// fetch the rows...
		NodeList rows = table.getElementsByTagName("tr");
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
						//System.out.println("Found data: "+el.getTextContent());
						if (col == 0) {
							stat = el.getTextContent();
						} else {
							while (col > statsBlock.size()) {
								StatisticsBlock block = new StatisticsBlock();
								block.properties.put(PROPERTY_NAME,defaultName);
								block.properties.put(PROPERTY_URL, url);
								statsBlock.add(block);
								//System.out.println("Added map for "+col);
							}
							StatisticsBlock block = statsBlock.get(col-1);
							if (stat.equals("")) {
								block.properties.put(PROPERTY_NAME,el.getTextContent());
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
}

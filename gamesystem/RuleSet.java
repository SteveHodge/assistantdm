package gamesystem;

import gamesystem.dice.HDDice;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// A RuleSet encapsulates the specific options used in a campaign, e.g. spells, feats, skills, classes, etc.
// Currently only spells are  supported
public class RuleSet {
	File directory;

	private RuleSet() {
	}

	public static void parseXML(File xmlFile) {
		RuleSet r = new RuleSet();
		r.directory = xmlFile.getParentFile();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			InputStream is = p.getClass().getClassLoader().getResourceAsStream("party.xsd");
//			factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
			Element e = factory.newDocumentBuilder().parse(xmlFile).getDocumentElement();
			if (e.getTagName().equals("ruleset")) {
				r.parseRuleSet(e);
			} else {
				System.err.println("Wrong type of file: " + e.getTagName());
				Thread.dumpStack();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseRuleSet(Element el) throws SAXException, IOException, ParserConfigurationException {
		if (!el.getTagName().equals("ruleset")) return;

		System.out.println("Parsing RuleSet " + el.getAttribute("name"));

		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("include")) {
				File f = new File(e.getAttribute("file"));

				if (!f.isAbsolute()) {
					f = new File(directory, f.getPath());
				}
				System.out.println("Including " + f);

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//				InputStream is = p.getClass().getClassLoader().getResourceAsStream("party.xsd");
//				factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
				e = factory.newDocumentBuilder().parse(f).getDocumentElement();
				if (e.getTagName().equals("ruleset")) {
					parseRuleSet(e);
					continue;
				}
				// not a ruleset node so drop through...
			}

			if (e.getTagName().equals("spells")) {
				parseSpells(e);

			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}
	}

	private void parseSpells(Element el) {
		if (!el.getTagName().equals("spells")) return;
		System.out.print("Parsing spells... ");

		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("spell")) {
				Spell s = parseSpell(e);
				Spell.spells.add(s);
			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}
		System.out.println(Spell.spells.size() + " found");
	}

	private Spell parseSpell(Element el) {
		if (!el.getTagName().equals("spell")) return null;
		Spell s = new Spell();
		s.name = el.getAttribute("name");
		s.domNode = el;
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("school")) {
				s.school = e.getTextContent();

			} else if (e.getTagName().equals("level")) {
				s.level = e.getTextContent();

			} else if (e.getTagName().equals("components")) {
				s.components = e.getTextContent();

			} else if (e.getTagName().equals("castingtime")) {
				s.castingTime = e.getTextContent();

			} else if (e.getTagName().equals("range")) {
				s.range = e.getTextContent();

			} else if (e.getTagName().equals("effect")) {
				s.effect = e.getTextContent();

			} else if (e.getTagName().equals("duration")) {
				s.duration = e.getTextContent();

			} else if (e.getTagName().equals("savingthrow")) {
				s.savingThrow = e.getTextContent();

			} else if (e.getTagName().equals("spellresistance")) {
				s.spellResistance = e.getTextContent();

			} else if (e.getTagName().equals("description")) {
				Node body = e.cloneNode(true);
				body.getOwnerDocument().renameNode(body, null, "body");
				Element html = body.getOwnerDocument().createElement("html");
				html.appendChild(body);
				s.description = getNodeString(html);

			} else if (e.getTagName().equals("material")) {
				s.material = e.getTextContent();

			} else if (e.getTagName().equals("focus")) {
				s.focus = e.getTextContent();

			} else if (e.getTagName().equals("xpcost")) {
				s.xpCost = e.getTextContent();

			} else if (e.getTagName().equals("modifiers")) {
				String variant = e.getAttribute("variant");
				String name = s.name;
				if (variant.length() > 0) name += " (" + variant + ")";
				BuffFactory buff = new BuffFactory(name);
				buff.source = s;
				NodeList mods = e.getChildNodes();
				for (int j = 0; j < mods.getLength(); j++) {
					if (mods.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
					Element m = (Element) mods.item(j);
					String stat = m.getAttribute("statistic");
					String type = m.getAttribute("type");
					if (type.length() == 0) type = null;
					String value = m.getAttribute("value");
					int val = 0;
					Object dice = null;
					try {
						val = Integer.parseInt(value);
					} catch (Exception ex) {
						dice = HDDice.parse(value);
					}
					String condition = m.getAttribute("condition");
					if (condition.length() == 0) condition = null;
					String penalty = m.getAttribute("penalty");
					String levelsPerExtra = m.getAttribute("levelsPerExtra");
					String maxExtra = m.getAttribute("maxExtra");

					if (dice != null || levelsPerExtra.length() > 0) {
						Object baseMod = dice;
						if (baseMod == null) baseMod = new Integer(val);
						int perCL = 0, max = 0;
						if (levelsPerExtra.length() > 0) perCL = Integer.parseInt(levelsPerExtra);
						if (maxExtra.length() > 0) max = Integer.parseInt(maxExtra);
						if (penalty.equals("true")) {
							buff.addPenalty(stat, type, baseMod, perCL, max, condition);
						} else {
							buff.addBonus(stat, type, baseMod, perCL, max, condition);
						}
					} else {
						buff.addFixedEffect(stat, type, val, condition);
					}
				}
				s.buffFactories.put(variant, buff);

			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}

		return s;
	}

	private static String getNodeString(Node node) {
		try
		{
			DOMSource domSource = new DOMSource(node);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}


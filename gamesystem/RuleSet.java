package gamesystem;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

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

import gamesystem.dice.HDDice;

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

			String tag = e.getTagName();
			if (tag.equals("spells")) {
				parseSpells(e);

			} else if (tag.equals("skills")) {
				parseSkills(e);

			} else if (tag.equals("feats")) {
				parseFeats(e);

			} else if (tag.equals("items")) {
				parseItems(e);

			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}
	}

	private void parseSkills(Element el) {
		if (!el.getTagName().equals("skills")) return;
		System.out.print("Parsing skills... ");

		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("skill")) {
				parseSkill(e);
			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}
		System.out.println(SkillType.skills.size() + " found");
	}

	private void parseSkill(Element e) {
		SkillType t = new SkillType(e.getAttribute("name"));
		if (e.hasAttribute("ability"))
			t.ability = AbilityScore.Type.getAbilityType(e.getAttribute("ability"));
		t.trainedOnly = e.hasAttribute("trained") && e.getAttribute("trained").equals("true");
		t.armorCheckPenaltyApplies = e.hasAttribute("acp") && e.getAttribute("acp").equals("true");
		t.doubleACP = e.hasAttribute("double_acp") && e.getAttribute("double_acp").equals("true");
		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element s = (Element) nodes.item(i);
			if (s.getTagName().equals("synergy")) {
				t.addSynergy(s.getAttribute("for"), s.getAttribute("condition"));
			}
		}
	}

	private void parseFeats(Element el) {
		if (!el.getTagName().equals("feats")) return;
		System.out.print("Parsing feats... ");

		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("feat")) {
				Feat.FeatDefinition f = new Feat.FeatDefinition(e.getAttribute("name"));
				f.repeatable = e.hasAttribute("repeatable") && e.getAttribute("repeatable").equals("true");
				f.ref(e.getAttribute("ref")).summary = e.getTextContent();
				f.target = e.getAttribute("target");

				NodeList children = e.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					if (nodes.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
					Element child = (Element) children.item(j);
					if (child.getTagName().equals("modifiers")) {
						String name = f.name;
						BuffFactory buff = new BuffFactory(name);
						buff.source = f;
						NodeList mods = child.getChildNodes();
						for (int k = 0; k < mods.getLength(); k++) {
							if (mods.item(k).getNodeType() != Node.ELEMENT_NODE) continue;
							Element m = (Element) mods.item(k);
							String stat = m.getAttribute("statistic");
							String value = m.getAttribute("value");
							int val = 0;
							try {
								val = Integer.parseInt(value);
							} catch (Exception ex) {
								System.err.println("Error parsing value for modifier in feat " + f);
							}
							String condition = m.getAttribute("condition");
							if (condition.length() == 0) condition = null;
							f.addFixedEffect(stat, null, val, condition);
						}

					} else {
						System.err.println("Ignoring unexpected node: " + e.getTagName());
						continue;
					}
				}

				Feat.feats.add(f);

			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}
		System.out.println(Feat.feats.size() + " found");
	}

	private void parseItems(Element el) {
		if (!el.getTagName().equals("items")) return;
		System.out.print("Parsing items... ");

		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("item")) {
				ItemDefinition item = new ItemDefinition();
				item.name = e.getAttribute("name");
				if (e.hasAttribute("slot")) item.slot = e.getAttribute("slot");
				if (e.hasAttribute("cost")) item.cost = e.getAttribute("cost");
				if (e.hasAttribute("weight")) item.weight = e.getAttribute("weight");

				NodeList children = e.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					if (children.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
					Element c = (Element) children.item(j);
					parseAttack(c, item);	// does nothing if the node tag is incorrect
					parseArmor(c, item);	// does nothing if the node tag is incorrect
					parseShield(c, item);	// does nothing if the node tag is incorrect
				}
				ItemDefinition.items.add(item);
			}
		}
		System.out.println(ItemDefinition.items.size() + " found");
	}

	private void parseAttack(Element el, ItemDefinition item) {
		if (!el.getTagName().equals("attack")) return;
		ItemDefinition.Attack a = item.new Attack();
		if (el.hasAttribute("proficiency")) a.proficiency = el.getAttribute("proficiency");
		if (el.hasAttribute("type")) a.type = el.getAttribute("type");
		if (el.hasAttribute("damage")) a.damage = el.getAttribute("damage");
		if (el.hasAttribute("critical")) a.critical = el.getAttribute("critical");
		if (el.hasAttribute("damage-type")) a.damageType = el.getAttribute("damage-type");
		if (el.hasAttribute("range")) a.range = el.getAttribute("range");
		if (item.attacks == null) item.attacks = new ArrayList<>();
		item.attacks.add(a);
	}

	private void parseArmor(Element el, ItemDefinition item) {
		if (!el.getTagName().equals("armor")) return;
		ItemDefinition.Armor a = item.new Armor();
		if (el.hasAttribute("type")) a.type = el.getAttribute("type");
		if (el.hasAttribute("bonus")) a.bonus = el.getAttribute("bonus");
		if (el.hasAttribute("max-dex")) a.maxDex = el.getAttribute("max-dex");
		if (el.hasAttribute("acp")) a.armorCheckPenalty = el.getAttribute("acp");
		if (el.hasAttribute("spell-failure")) a.spellFailure = el.getAttribute("spell-failure");
		if (el.hasAttribute("speed")) a.speed = el.getAttribute("speed");
		a.slowRun = el.hasAttribute("slow-run") && el.getAttribute("slow-run").equals("true");
		item.armor = a;
	}

	private void parseShield(Element el, ItemDefinition item) {
		if (!el.getTagName().equals("shield")) return;
		ItemDefinition.Shield a = item.new Shield();
		if (el.hasAttribute("bonus")) a.bonus = el.getAttribute("bonus");
		if (el.hasAttribute("max-dex")) a.maxDex = el.getAttribute("max-dex");
		if (el.hasAttribute("acp")) a.armorCheckPenalty = el.getAttribute("acp");
		if (el.hasAttribute("spell-failure")) a.spellFailure = el.getAttribute("spell-failure");
		item.shield = a;
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


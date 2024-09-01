package gamesystem;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import gamesystem.Calendar.Event;
import gamesystem.Calendar.Month;
import gamesystem.Calendar.Weekday;
import gamesystem.ItemDefinition.SlotType;
import gamesystem.dice.HDDice;

// A RuleSet encapsulates the specific options used in a campaign, e.g. spells, feats, skills, classes, etc.
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

			} else if (tag.equals("calendar-definition")) {
				parseCalendar(e);

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
		t.doubleACP = e.hasAttribute("double-acp") && e.getAttribute("double-acp").equals("true");
		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element s = (Element) nodes.item(i);
			if (s.getTagName().equals("synergy")) {
				t.addSynergy(s.getAttribute("for"), s.getAttribute("condition"));
			}
		}
	}

	private static void parseModifiers(Element e, EffectorDefinition<?> def) {
		if (!e.getTagName().equals("modifiers")) ;
		NodeList mods = e.getChildNodes();
		for (int j = 0; j < mods.getLength(); j++) {
			if (mods.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
			Element m = (Element) mods.item(j);
			if (m.getTagName().equals("modifier")) {
				String stat = m.getAttribute("statistic");
				String type = m.getAttribute("type");
				if (type.length() == 0) type = null;
				String value = m.getAttribute("value");
				int val = 0;
				Object dice = null;
				try {
					val = Integer.parseInt(value);
				} catch (Exception ex) {
					try {
						dice = HDDice.parse(value);
					} catch (Exception except) {
						System.err.println("Error parsing modifier value '" + value + "' for " + def);
					}
				}
				String condition = m.getAttribute("condition");
				if (condition.length() == 0) condition = null;
				String penalty = m.getAttribute("penalty");
				String levelsPerExtra = m.getAttribute("levelsPerExtra");
				String maxExtra = m.getAttribute("maxExtra");

				if (dice != null || levelsPerExtra.length() > 0) {
					if (def instanceof BuffFactory) {
						BuffFactory buff = (BuffFactory) def;
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
						System.err.println("Caster-level dependent and dice-based modifiers are not supported for " + def);
					}
				} else {
					def.addFixedEffect(stat, type, val, condition);
				}

			} else if (m.getTagName().equals("property") && def instanceof BuffFactory) {
				String stat = m.getAttribute("statistic");
				String prop = m.getAttribute("property");
				String value = m.getAttribute("value");
				Integer val = null;
				try {
					val = Integer.parseInt(value);
				} catch (Exception ex) {
				}
				String description = m.getAttribute("description");
				((BuffFactory) def).addPropertyChange(stat, prop, val == null ? value : val, description);

			} else {
				System.err.println("Ignoring unexpected tag '" + m.getTagName() + "' for " + def.name);
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
						parseModifiers(child, f);
					} else {
						System.err.println("Ignoring unexpected node: " + e.getTagName() + " in " + f.name);
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

//	private void addAttributes(Map<String, Set<String>> attributes, Element e) {
//		for (int j = 0; j < e.getAttributes().getLength(); j++) {
//			Set<String> attrs = attributes.get(e.getNodeName());
//			if (attrs == null) {
//				attrs = new HashSet<String>();
//				attributes.put(e.getNodeName(), attrs);
//			}
//			attrs.add(e.getAttributes().item(j).getNodeName());
//		}
//	}

	private void parseItems(Element el) {
		if (!el.getTagName().equals("items")) return;
		System.out.print("Parsing items... ");

//		Map<String, Set<String>> attributes = new HashMap<>();

		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("item")) {
//				addAttributes(attributes, e);
				ItemDefinition item = new ItemDefinition();
				item.name = e.getAttribute("name");
				if (e.hasAttribute("category")) item.category = e.getAttribute("category");
				if (e.hasAttribute("slot")) item.slot = SlotType.getSlot(e.getAttribute("slot"));
				if (e.hasAttribute("price")) item.price = e.getAttribute("price");
				if (e.hasAttribute("weight")) item.weight = e.getAttribute("weight");
				if (e.hasAttribute("aura")) item.aura = e.getAttribute("aura");
				if (e.hasAttribute("scale-weight")) item.scaleWeight = e.getAttribute("scale-weight").toLowerCase() == "true";
				if (e.hasAttribute("magical")) item.magical = e.getAttribute("magical").toLowerCase() == "true";

				NodeList children = e.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					if (children.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
					Element c = (Element) children.item(j);
//					addAttributes(attributes, c);
					parseAttack(c, item);	// does nothing if the node tag is incorrect
					parseArmor(c, item);	// does nothing if the node tag is incorrect
					parseShield(c, item);	// does nothing if the node tag is incorrect

					if (c.getTagName().equals("modifiers")) {
						item.buff = new BuffFactory(item.name);
						item.buff.source = item;
						parseModifiers(c, item.buff);
					}

					// TODO description tag, creation tag (attributes caster_level, prerequisites)
				}
				ItemDefinition.items.add(item);
			}
		}
		System.out.println(ItemDefinition.items.size() + " found");
//		for (String element : attributes.keySet()) {
//			System.out.println(element + " attributes: " + String.join(", ", attributes.get(element)));
//		}
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
				//checkSpell(s);
				Spell.spells.add(s);
			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}
		System.out.println(Spell.spells.size() + " found");
	}

	private void checkSpell(Spell s) {
		if (s.school == null)
			System.err.println("Spell " + s.name + " has no school");
		if (s.level == null)
			System.err.println("Spell " + s.name + " has no level");
		if (s.components == null)
			System.err.println("Spell " + s.name + " has no components");
		if (s.castingTime == null)
			System.err.println("Spell " + s.name + " has no castingTime");
		if (s.range == null)
			System.err.println("Spell " + s.name + " has no range");
		if (s.effect == null)
			System.err.println("Spell " + s.name + " has no effect");
		if (s.savingThrow == null && (s.range == null || !s.range.trim().equals("Personal")))
			System.err.println("Spell " + s.name + " has no savingThrow");
		if (s.spellResistance == null && (s.range == null || !s.range.trim().equals("Personal")))
			System.err.println("Spell " + s.name + " has no spellResistance");
		if (s.description == null)
			System.err.println("Spell " + s.name + " has no description");
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
				parseModifiers(e, buff);
				s.buffFactories.put(variant, buff);

			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName() + " in spell " + s.name);
				continue;
			}
		}

		return s;
	}

	private void parseCalendar(Element el) {
		if (!el.getTagName().equals("calendar-definition")) return;
		System.out.print("Parsing calendar... ");

		List<Month> months = null;
		List<Weekday> week = null;
		List<Event> events = null;
		Map<Event, String> eventMonths = new HashMap<>();

		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("months")) {
				months = parseMonths(e);

			} else if (e.getTagName().equals("week")) {
				week = parseWeek(e);

			} else if (e.getTagName().equals("events")) {
				events = parseEvents(e, eventMonths);

			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}

		if (months != null) {
			for (Month m : months) {
				Calendar.monthMap.put(m.name, m);
			}
			Calendar.months = new Month[months.size()];
			months.sort((a, b) -> {
				return a.number - b.number;
			});
			Calendar.months = months.toArray(new Month[0]);
		}

		if (week != null) {
			week.sort((a, b) -> {
				return a.number - b.number;
			});
			Calendar.weekdays = week.toArray(new Weekday[0]);
		}

		if (events != null) {
			for (Event evt : events) {
				evt.month = Calendar.monthMap.get(eventMonths.get(evt));
			}
			Calendar.events = events.toArray(new Event[0]);
		}

		Calendar.defaultYear = Integer.parseInt(el.getAttribute("default-year"));
		Calendar.firstWeekday = Calendar.weekdays[Integer.parseInt(el.getAttribute("first-weekday")) - 1];

		System.out.println("done");
	}

	private List<Calendar.Month> parseMonths(Element el) {
		if (!el.getTagName().equals("months")) return null;

		List<Calendar.Month> months = new ArrayList<>();
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("month")) {
				Calendar.Month m = new Calendar.Month();

				m.name = e.getAttribute("name");
				m.season = e.getAttribute("season");
				m.number = Integer.parseInt(e.getAttribute("number"));
				m.days = Integer.parseInt(e.getAttribute("days"));

				Node body = e.cloneNode(true);
				body.getOwnerDocument().renameNode(body, null, "body");
				Element html = body.getOwnerDocument().createElement("html");
				html.appendChild(body);
				m.description = html;

				months.add(m);
			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}
		return months;
	}

	private List<Calendar.Event> parseEvents(Element el, Map<Calendar.Event, String> months) {
		if (!el.getTagName().equals("events")) return null;

		List<Calendar.Event> events = new ArrayList<>();
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("event")) {
				Calendar.Event event = new Calendar.Event();

				event.name = e.getAttribute("name");
				event.day = Integer.parseInt(e.getAttribute("day"));
				months.put(event, e.getAttribute("month"));

				Node body = e.cloneNode(true);
				body.getOwnerDocument().renameNode(body, null, "body");
				Element html = body.getOwnerDocument().createElement("html");
				html.appendChild(body);
				event.description = html;

				events.add(event);
			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}
		return events;
	}

	private List<Calendar.Weekday> parseWeek(Element el) {
		if (!el.getTagName().equals("week")) return null;

		List<Calendar.Weekday> week = new ArrayList<>();
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			if (e.getTagName().equals("weekday")) {
				Calendar.Weekday w = new Calendar.Weekday();

				w.name = e.getAttribute("name");
				w.number = Integer.parseInt(e.getAttribute("number"));
				w.abbreviation = e.getAttribute("abbreviation");

				week.add(w);
			} else {
				System.err.println("Ignoring unexpected node: " + e.getTagName());
				continue;
			}
		}
		return week;
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


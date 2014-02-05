package monsters;

import gamesystem.Buff;
import gamesystem.XMLParserHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import monsters.StatisticsBlock.MonsterDetails;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.Character.ACComponentType;

public class XMLMonsterParser extends XMLParserHelper {
	public Monster parseDOM(Element el) {
		if (!el.getNodeName().equals("Monster")) return null;

		Monster m = null;

		// see if there is a stats block - if so use that as the base
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			String tag = e.getTagName();

			if (tag.equals("StatisticsBlock")) {
				String blockName = e.getAttribute("name");
				String urlString = e.getAttribute("url");
				try {
					URL url = new URL(urlString);
					List<StatisticsBlock> blocks = StatisticsBlock.parseURL(url);
					for (StatisticsBlock block : blocks) {
						if (block.getName().equals(blockName)) {
							m = StatsBlockCreatureView.getMonster(block);
							m.setName(el.getAttribute("name"));
						}
					}
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				}
			}
		}

		if (m == null) m = new Monster(el.getAttribute("name"));

		Element hpElement = null;		// need to process after ability scores to avoid issues with changing con
		Element attacksElement = null;	// need to process after feats so we don't reset any values selected for power attack or combat expertise

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			String tag = e.getTagName();

			if (tag.equals("HitPoints")) {
				// processing hitpoints is deferred as it relies on con being already set and buffs being already loaded
				hpElement = e;

			} else if (tag.equals("Initiative")) {
				parseInitiativeModifier(e, m);

			} else if (tag.equals("AbilityScores")) {
				NodeList abilities = e.getChildNodes();
				for (int j = 0; j < abilities.getLength(); j++) {
					if (!abilities.item(j).getNodeName().equals("AbilityScore")) continue;
					Element s = (Element) abilities.item(j);
					parseAbilityScore(s, m);
				}

			} else if (tag.equals("SavingThrows")) {
				NodeList saves = e.getChildNodes();
				for (int j = 0; j < saves.getLength(); j++) {
					if (!saves.item(j).getNodeName().equals("Save")) continue;
					Element s = (Element) saves.item(j);
//							String value = s.getAttribute("base");
					parseSavingThrow(s, m);
				}

//			} else if (tag.equals("Skills")) {
//				parseSkills(e, m.skills);

			} else if (tag.equals("AC")) {
				parseAC(e, m);

				NodeList acs = e.getChildNodes();
				for (int j = 0; j < acs.getLength(); j++) {
					if (!acs.item(j).getNodeName().equals("ACComponent")) continue;
					Element s = (Element) acs.item(j);
					String value = s.getAttribute("value");
					String type = s.getAttribute("type");
					// TODO hacks to change type:
					if (type.equals("Natural")) type = ACComponentType.NATURAL.toString();
					if (type.equals("Deflect")) type = ACComponentType.DEFLECTION.toString();
				}

			} else if (tag.equals("Attacks")) {
				attacksElement = e;

//			} else if (tag.equals("Feats")) {
//				NodeList children = e.getChildNodes();
//				for (int j = 0; j < children.getLength(); j++) {
//					if (children.item(j).getNodeName().equals("Feat")) {
//						if (!children.item(j).getNodeName().equals("Feat")) continue;
//						Buff b = parseBuff((Element) children.item(j));
//						b.applyBuff(m);
//						m.feats.addElement(b);
//					}
//				}

			} else if (tag.equals("Buffs")) {
				NodeList buffs = e.getChildNodes();
				for (int j = 0; j < buffs.getLength(); j++) {
					if (!buffs.item(j).getNodeName().equals("Buff")) continue;
					Buff b = parseBuff((Element) buffs.item(j));
					b.applyBuff(m);
					m.buffs.addElement(b);
				}

			} else if (tag.equals("Size")) {
				parseSize(e, m);

			} else if (tag.equals("Property")) {
				String prop = e.getAttribute("name");
				String value = e.getAttribute("value");
				m.setProperty(prop, value);
			}
		}

		if (hpElement != null) {
			parseHPs(hpElement, m);
		}

		if (attacksElement != null) {
			parseAttacks(attacksElement, m);
			NodeList children = attacksElement.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				if (children.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
				Element e = (Element) children.item(j);
				String tag = e.getNodeName();
				if (tag.equals("AttackForm")) {
					StatsBlockCreatureView.setAttackList(m, StatisticsBlock.parseAttacks(e.getTextContent(), new MonsterDetails(m)));
				} else if (tag.equals("FullAttackForm")) {
					StatsBlockCreatureView.setFullAttackList(m, StatisticsBlock.parseAttacks(e.getTextContent(), new MonsterDetails(m)));
				}
			}
		}
		return m;
	}

}

package party;

import gamesystem.Buff;
import gamesystem.SavingThrow;
import gamesystem.XMLParserHelper;
import gamesystem.XP;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.Character.ACComponentType;


public class XMLCharacterParser extends XMLParserHelper {
	public Character parseDOM(Element el) {
		if (!el.getNodeName().equals("Character")) return null;

		Character c = new Character(el.getAttribute("name"));
		c.setProperty(Character.PROPERTY_PLAYER, el.getAttribute("player"));
		c.setProperty(Character.PROPERTY_REGION, el.getAttribute("region"));
		c.setProperty(Character.PROPERTY_RACE, el.getAttribute("race"));
		c.setProperty(Character.PROPERTY_GENDER, el.getAttribute("gender"));
		c.setProperty(Character.PROPERTY_ALIGNMENT, el.getAttribute("alignment"));
		c.setProperty(Character.PROPERTY_DEITY, el.getAttribute("deity"));
		c.setProperty(Character.PROPERTY_TYPE, el.getAttribute("type"));
		c.setProperty(Character.PROPERTY_AGE, el.getAttribute("age"));
		c.setProperty(Character.PROPERTY_HEIGHT, el.getAttribute("height"));
		c.setProperty(Character.PROPERTY_WEIGHT, el.getAttribute("weight"));
		c.setProperty(Character.PROPERTY_EYE_COLOUR, el.getAttribute("eye-colour"));
		c.setProperty(Character.PROPERTY_HAIR_COLOUR, el.getAttribute("hair-colour"));
		c.setProperty(Character.PROPERTY_SPEED, el.getAttribute("speed"));
		c.setProperty(Character.PROPERTY_DAMAGE_REDUCTION, el.getAttribute("damage-reduction"));
		c.setProperty(Character.PROPERTY_SPELL_RESISTANCE, el.getAttribute("spell-resistance"));
		c.setProperty(Character.PROPERTY_ARCANE_SPELL_FAILURE, el.getAttribute("arcane-spell-failure"));
		c.setProperty(Character.PROPERTY_ACTION_POINTS, el.getAttribute("action-points"));

		Element hpElement = null;		// need to process after ability scores to avoid issues with changing con
		Element attacksElement = null;	// need to process after feats so we don't reset any values selected for power attack or combat expertise

		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element) nodes.item(i);
			String tag = e.getTagName();

			if (tag.equals("HitPoints")) {
				// processing hitpoints is deferred as it relies on con being already set and buffs being already loaded
				hpElement = e;

			} else if (tag.equals("Initiative")) {
				parseInitiativeModifier(e, c);

			} else if (tag.equals("Level")) {
				parseLevel(e, c.level);
				c.xp = Integer.parseInt(e.getAttribute("xp"));
				NodeList awards = e.getChildNodes();
				for (int j = 0; j < awards.getLength(); j++) {
					XP.XPChange change = null;
					if (awards.item(j).getNodeName().equals("XPAward")) {
						change = parseXPChangeChallenges((Element) awards.item(j));
					} else if (awards.item(j).getNodeName().equals("XPChange")) {
						change = parseXPChangeAdhoc((Element) awards.item(j));
					} else if (awards.item(j).getNodeName().equals("XPLevelChange")) {
						change = parseXPChangeLevel((Element) awards.item(j));
					}
					if (change != null) c.addXPChange(change);
				}

			} else if (tag.equals("AbilityScores")) {
				NodeList abilities = e.getChildNodes();
				for (int j = 0; j < abilities.getLength(); j++) {
					if (!abilities.item(j).getNodeName().equals("AbilityScore")) continue;
					Element s = (Element) abilities.item(j);
					parseAbilityScore(s, c);
				}

			} else if (tag.equals("SavingThrows")) {
				NodeList saves = e.getChildNodes();
				for (int j = 0; j < saves.getLength(); j++) {
					if (!saves.item(j).getNodeName().equals("Save")) continue;
					Element s = (Element) saves.item(j);
//						String value = s.getAttribute("base");
					parseSavingThrow(s, c);
					String misc = s.getAttribute("misc");
					if (misc != "" && !misc.equals("0")) {
						SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(s.getAttribute("type"));
						c.setSavingThrowMisc(type, Integer.parseInt(misc));
					}
				}

			} else if (tag.equals("Skills")) {
				parseSkills(e, c.skills);

			} else if (tag.equals("AC")) {
				parseAC(e, c);

				NodeList acs = e.getChildNodes();
				for (int j = 0; j < acs.getLength(); j++) {
					if (!acs.item(j).getNodeName().equals("ACComponent")) continue;
					Element s = (Element) acs.item(j);
					String value = s.getAttribute("value");
					String type = s.getAttribute("type");
					// TODO hacks to change type:
					if (type.equals("Natural")) type = ACComponentType.NATURAL.toString();
					if (type.equals("Deflect")) type = ACComponentType.DEFLECTION.toString();
					for (ACComponentType t : ACComponentType.values()) {
						if (t.toString().equals(type)) {
							c.setACComponent(t, Integer.parseInt(value));
						}
					}
				}

			} else if (tag.equals("Attacks")) {
				attacksElement = e;

			} else if (tag.equals("Feats")) {
				NodeList children = e.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					if (children.item(j).getNodeName().equals("Feat")) {
						if (!children.item(j).getNodeName().equals("Feat")) continue;
						Buff b = parseBuff((Element) children.item(j));
						b.applyBuff(c);
						c.feats.addElement(b);
					}
				}

			} else if (tag.equals("Buffs")) {
				NodeList buffs = e.getChildNodes();
				for (int j = 0; j < buffs.getLength(); j++) {
					if (!buffs.item(j).getNodeName().equals("Buff")) continue;
					Buff b = parseBuff((Element) buffs.item(j));
					b.applyBuff(c);
					c.buffs.addElement(b);
				}

			} else if (tag.equals("Size")) {
				parseSize(e, c);
			}
		}

		if (hpElement != null) {
			parseHPs(hpElement, c);
		}

		if (attacksElement != null) {
			parseAttacks(attacksElement, c);
			NodeList children = attacksElement.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				if (children.item(j).getNodeName().equals("AttackForm")) {
					Element attackEl = (Element) children.item(j);
					CharacterAttackForm atk = new CharacterAttackForm(c, addAttackForm(c, attackEl.getAttribute("name")));
					parseCharacterAttackForm(attackEl, atk);
					c.attackForms.add(atk);
				}
			}
		}
		return c;
	}

}
package gamesystem;

import gamesystem.Buff.PropertyChange;
import gamesystem.XP.Challenge;
import gamesystem.XP.XPChangeAdhoc;
import gamesystem.XP.XPChangeChallenges;
import gamesystem.XP.XPChangeLevel;
import gamesystem.dice.CombinedDice;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.Character;
import party.Character.ACComponentType;
import party.CharacterAttackForm;
import party.CharacterAttackForm.Kind;
import party.CharacterAttackForm.Usage;

public class XMLCreatureParser {
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
				parseInitiativeModifier(e, c.initiative);

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
					AbilityScore.Type type = AbilityScore.Type.getAbilityType(s.getAttribute("type"));
					parseAbilityScore(s, c.abilities.get(type));
				}

			} else if (tag.equals("SavingThrows")) {
				NodeList saves = e.getChildNodes();
				for (int j = 0; j < saves.getLength(); j++) {
					if (!saves.item(j).getNodeName().equals("Save")) continue;
					Element s = (Element) saves.item(j);
//						String value = s.getAttribute("base");
					SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(s.getAttribute("type"));
					parseSavingThrow(s, c.saves.get(type));
					String misc = s.getAttribute("misc");
					if (misc != "" && !misc.equals("0")) {
						c.setSavingThrowMisc(type, Integer.parseInt(misc));
					}
				}

			} else if (tag.equals("Skills")) {
				parseSkills(e, c.skills);

			} else if (tag.equals("AC")) {
				parseAC(e, c.ac);

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
				parseSize(e, c.size);
			}
		}

		if (hpElement != null) {
			parseHPs(hpElement, c.hps);
		}

		if (attacksElement != null) {
			parseAttacks(attacksElement, c.attacks);
			NodeList children = attacksElement.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				if (children.item(j).getNodeName().equals("AttackForm")) {
					Element attackEl = (Element) children.item(j);
					CharacterAttackForm atk = new CharacterAttackForm(c, c.attacks.addAttackForm(attackEl.getAttribute("name")));
					parseCharacterAttackForm(attackEl, atk);
					c.attackForms.add(atk);
				}
			}
		}
		return c;
	}

	protected void parseInitiativeModifier(Element e, InitiativeModifier init) {
		if (!e.getTagName().equals("Initiative")) return;
		init.setBaseValue(Integer.parseInt(e.getAttribute("value")));
	}

	protected void parseLevel(Element e, Level lvl) {
		if (!e.getTagName().equals("Level")) return;
		lvl.setLevel(Integer.parseInt(e.getAttribute("level")));
	}

	protected void parseAbilityScore(Element e, AbilityScore s) {
		if (!e.getTagName().equals("AbilityScore")) return;
		if (!e.getAttribute("type").equals(s.getType().toString())) return;

		s.setBaseValue(Integer.parseInt(e.getAttribute("value")));
		if (e.hasAttribute("temp")) s.setOverride(Integer.parseInt(e.getAttribute("temp")));
	}

	protected void parseSavingThrow(Element e, SavingThrow s) {
		if (!e.getTagName().equals("Save")) return;
		if (!e.getAttribute("type").equals(s.getType().toString())) return;

		s.setBaseValue(Integer.parseInt(e.getAttribute("base")));
		// TODO misc
	}

	protected void parseSkills(Element e, Skills stat) {
		if (!e.getTagName().equals("Skills")) return;

		NodeList skills = e.getChildNodes();
		for (int j = 0; j < skills.getLength(); j++) {
			if (!skills.item(j).getNodeName().equals("Skill")) continue;
			Element s = (Element) skills.item(j);
			String ranks = s.getAttribute("ranks");
			String type = s.getAttribute("type");
			SkillType skill = SkillType.getSkill(type);
			stat.setRanks(skill, Float.parseFloat(ranks));
			String misc = s.getAttribute("misc");
			if (misc != "") stat.setMisc(skill, Integer.parseInt(misc));
		}
	}

	protected void parseAC(Element e, AC ac) {
		if (!e.getTagName().equals("AC")) return;

		NodeList children = e.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			if (children.item(j).getNodeName().equals("Armor")) {
				parseArmor((Element) children.item(j), ac.armor);
			} else if (children.item(j).getNodeName().equals("Shield")) {
				parseShield((Element) children.item(j), ac.shield);
			}
		}
	}

	protected void parseShield(Element e, AC.Shield shield) {
		if (!e.getTagName().equals("Shield")) return;

		if (e.hasAttribute("description")) shield.description = e.getAttribute("description");
		if (e.hasAttribute("properties")) shield.properties = e.getAttribute("properties");
		if (e.hasAttribute("bonus")) shield.setBonus(Integer.parseInt(e.getAttribute("bonus")));
		if (e.hasAttribute("enhancement")) shield.setEnhancement(Integer.parseInt(e.getAttribute("enhancement")));
		if (e.hasAttribute("weight")) shield.weight = Integer.parseInt(e.getAttribute("weight"));
		if (e.hasAttribute("acp")) shield.setACP(Integer.parseInt(e.getAttribute("acp")));
		if (e.hasAttribute("spell_failure")) shield.spellFailure = Integer.parseInt(e.getAttribute("spell_failure"));
	}

	protected void parseArmor(Element e, AC.Armor armor) {
		if (!e.getTagName().equals("Armor")) return;

		if (e.hasAttribute("description")) armor.description = e.getAttribute("description");
		if (e.hasAttribute("properties")) armor.properties = e.getAttribute("properties");
		if (e.hasAttribute("bonus")) armor.setBonus(Integer.parseInt(e.getAttribute("bonus")));
		if (e.hasAttribute("enhancement")) armor.setEnhancement(Integer.parseInt(e.getAttribute("enhancement")));
		if (e.hasAttribute("weight")) armor.weight = Integer.parseInt(e.getAttribute("weight"));
		if (e.hasAttribute("acp")) armor.setACP(Integer.parseInt(e.getAttribute("acp")));
		if (e.hasAttribute("spell_failure")) armor.spellFailure = Integer.parseInt(e.getAttribute("spell_failure"));
		if (e.hasAttribute("type")) armor.type = e.getAttribute("type");
		if (e.hasAttribute("speed")) armor.speed = Integer.parseInt(e.getAttribute("speed"));
		if (e.hasAttribute("max_dex")) armor.setMaxDex(Integer.parseInt(e.getAttribute("max_dex")));
	}

	protected void parseSize(Element e, Size s) {
		if (!e.getTagName().equals("Size")) return;
		s.setBaseSize(SizeCategory.getSize(e.getAttribute("category")));
		s.setBaseSpace(Integer.parseInt(e.getAttribute("space")));
		s.setBaseReach(Integer.parseInt(e.getAttribute("reach")));
	}

	// TODO this configures HP fields directly - should use methods that inform listeners
	protected void parseHPs(Element e, HPs hp) {
		if (!e.getTagName().equals("HitPoints")) return;
		hp.hps = Integer.parseInt(e.getAttribute("maximum"));
		if (e.hasAttribute("wounds")) hp.wounds = Integer.parseInt(e.getAttribute("wounds"));
		if (e.hasAttribute("non-lethal")) hp.nonLethal = Integer.parseInt(e.getAttribute("non-lethal"));
		if (hp.conMod != null) {
			hp.oldMod = hp.conMod.getModifier();	// we need to set the oldMod so that any future con changes are correctly calculated
		}
		// TODO this means that HPs must be parsed after ability scores. we really need accurate reporting of old con mod in the event

		// set any existing temporary hps to 0. this prevents temporary hitpoints that have been used for a particular
		// buff being reset. after we've parsed this element we'll remove any remaining temporary hitpoints.
		for (HPs.TempHPs temp : hp.tempHPs) {
			//System.out.println("TempHPs: " + temp.id + " = " + temp.hps);
			temp.hps = 0;
		}

		NodeList temps = e.getChildNodes();
		for (int k = 0; k < temps.getLength(); k++) {
			if (!temps.item(k).getNodeName().equals("TempHPs")) continue;
			Element m = (Element) temps.item(k);
			String source = m.getAttribute("source");
			int hps = Integer.parseInt(m.getAttribute("hps"));
			int id = Integer.parseInt(m.getAttribute("id"));

			boolean found = false;
			if (id > 0) {
				// see if we have a modifier to map this to. if we do then adjust the existing temp hps
				for (HPs.TempHPs temp : hp.tempHPs) {
					if (temp.id == id) {
						if (!temp.source.equals(source)) System.out.println("Temp HPs: conflicting source for ID " + id);
						temp.hps = hps;
						found = true;
					}
				}
			}
			if (!found) {
				if (id > 0) {
					// if we don't have a modifier then we set up a temp hps but we'll need to wait for the Buff to link it up
					System.err.println("Unimplemented: parsing temporary hitpoints before buff");
				}
				HPs.TempHPs temp = hp.new TempHPs(source, hps);
				temp.id = id;
				hp.addTemporaryHPs(temp);
			}
		}

		// clean up: check active flags are correctly set and remove any tempHPs set to 0
		List<HPs.TempHPs> toDelete = new ArrayList<HPs.TempHPs>();
		Map<String, HPs.TempHPs> best = new HashMap<String, HPs.TempHPs>();
		for (HPs.TempHPs temp : hp.tempHPs) {
			if (temp.hps == 0) {
				toDelete.add(temp);
			} else {
				HPs.TempHPs currBest = best.get(temp.source);
				if (currBest == null || currBest.hps < temp.hps) {
					if (currBest != null) currBest.active = false;
					best.put(temp.source, temp);
					temp.active = true;
				} else {
					temp.active = false;
				}
			}
		}
		for (HPs.TempHPs temp : toDelete) {
			//System.out.println("Deleting " + temp.id);
			hp.tempHPs.remove(temp);
			// TODO need to notify source / remove source if it has no other effect
		}
		//printTempHPs(tempHPs);
	}

	protected void parseAttacks(Element e, Attacks a) {
		if (!e.getTagName().equals("Attacks")) return;

		a.setBAB(Integer.parseInt(e.getAttribute("base")));

		if (e.hasAttribute("power_attack")) a.setPowerAttack(Integer.parseInt(e.getAttribute("power_attack")));
		if (e.hasAttribute("combat_expertise")) a.setCombatExpertise(Integer.parseInt(e.getAttribute("combat_expertise")));
		if (e.getAttribute("total_defense").equals("true") || e.getAttribute("total_defense").equals("1")) a.setTotalDefense(true);
		if (e.getAttribute("fighting_defensively").equals("true") || e.getAttribute("total_defense").equals("1")) a.setFightingDefensively(true);
	}

	protected Buff parseBuff(Element b) {
		//if (!b.getTagName().equals("Buff")) return null;
		Buff buff = new Buff();
		if (b.hasAttribute("caster_level")) buff.casterLevel = Integer.parseInt(b.getAttribute("caster_level"));
		buff.name = b.getAttribute("name");
		if (b.hasAttribute("id")) {
			buff.id = Integer.parseInt(b.getAttribute("id"));
			if (buff.id >= Buff.nextid) Buff.nextid = buff.id + 1;	// prevent possible future reuse of this id
		}
		NodeList mods = b.getChildNodes();
		for (int k = 0; k < mods.getLength(); k++) {
			if (mods.item(k).getNodeName().equals("Modifier")) {
				Element m = (Element) mods.item(k);
				String target = m.getAttribute("target");
				int value = Integer.parseInt(m.getAttribute("value"));
				String type = m.getAttribute("type");
				String condition = m.getAttribute("condition");
				ImmutableModifier mod;
				if (condition != null && condition.length() > 0) {
					mod = new ImmutableModifier(value, type, buff.name, condition);
				} else {
					mod = new ImmutableModifier(value, type, buff.name);
				}
				mod.id = buff.id;
				buff.modifiers.put(mod, target);

			} else if (mods.item(k).getNodeName().equals("PropertyChange")) {
				Element m = (Element) mods.item(k);
				String target = m.getAttribute("target");
				int value = Integer.parseInt(m.getAttribute("value"));
				PropertyChange p = buff.new PropertyChange();
				p.description = m.getAttribute("description");
				p.property = m.getAttribute("property");
				p.value = value;
				buff.propertyChanges.put(p, target);

			}
		}
		return buff;
	}

	protected void parseCharacterAttackForm(Element e, CharacterAttackForm f) {
		parseAttackForm(e, f.attack);
		f.critical = e.getAttribute("critical");
		if (e.hasAttribute("range")) f.range = Integer.parseInt(e.getAttribute("range"));
		if (e.hasAttribute("weight")) f.weight = Integer.parseInt(e.getAttribute("weight"));
		f.damage_type = e.getAttribute("type");
		f.ammunition = e.getAttribute("ammunition");
		f.kind = Kind.getKind(e.getAttribute("kind"));
		if (e.hasAttribute("usage")) f.usage = Usage.values()[Integer.parseInt(e.getAttribute("usage"))];
		String s = e.getAttribute("properties");
		if (s != null && !s.equals(f.usage)) {
			f.properties = s;
		}
	}

	protected void parseAttackForm(Element e, Attacks.AttackForm f) {
		if (!e.getTagName().equals("AttackForm")) return;

		if (e.hasAttribute("enhancement")) {
			f.setAttackEnhancement(Integer.parseInt(e.getAttribute("enhancement")));
		}
		f.damage = CombinedDice.parse(e.getAttribute("base_damage"));
		if (e.hasAttribute("size")) f.size = SizeCategory.getSize(e.getAttribute("size"));
		f.updateModifiers();
	}

	protected XP.XPChangeLevel parseXPChangeLevel(Element e) {
		if (!e.getNodeName().equals("XPLevelChange")) return null;
		String comment = e.getTextContent();
		Date d = null;
		try {
			d = XP.dateFormat.parse(e.getAttribute("date"));
		} catch (ParseException e1) {
		}
		XPChangeLevel c = new XPChangeLevel(
				Integer.parseInt(e.getAttribute("old")),
				Integer.parseInt(e.getAttribute("new")),
				comment, d
				);
		return c;
	}

	protected XPChangeChallenges parseXPChangeChallenges(Element e) {
		if (!e.getNodeName().equals("XPAward")) return null;
		Date d = null;
		try {
			d = XP.dateFormat.parse(e.getAttribute("date"));
		} catch (ParseException e1) {
		}
		XPChangeChallenges c = new XPChangeChallenges(null, d);
		c.xp = Integer.parseInt(e.getAttribute("xp"));
		c.level = Integer.parseInt(e.getAttribute("level"));
		c.partyCount = Integer.parseInt(e.getAttribute("party"));
		if (e.hasAttribute("penalty")) c.penalty = Integer.parseInt(e.getAttribute("penalty"));

		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element child = (Element) nodes.item(i);
			String tag = child.getTagName();

			if (tag.equals("XPChallenge")) {
				Challenge chal = parseChallenge(child);
				c.challenges.add(chal);
			} else if (tag.equals("Comment")) {
				c.comment = child.getTextContent();
			}
		}
		return c;
	}

	protected Challenge parseChallenge(Element e) {
		if (!e.getNodeName().equals("XPChallenge")) return null;
		Challenge c = new Challenge();
		c.cr = new CR(e.getAttribute("cr"));
		c.number = Integer.parseInt(e.getAttribute("number"));
		c.comment = e.getTextContent();
		return c;
	}

	protected XPChangeAdhoc parseXPChangeAdhoc(Element e) {
		if (!e.getNodeName().equals("XPChange")) return null;
		String comment = e.getTextContent();
		Date d = null;
		try {
			d = XP.dateFormat.parse(e.getAttribute("date"));
		} catch (ParseException e1) {
		}
		XPChangeAdhoc c = new XPChangeAdhoc(Integer.parseInt(e.getAttribute("xp")), comment, d);
		return c;
	}
}

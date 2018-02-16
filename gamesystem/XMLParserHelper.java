package gamesystem;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gamesystem.Attacks.AttackForm;
import gamesystem.Buff.PropertyChange;
import gamesystem.Feat.FeatDefinition;
import gamesystem.XP.Challenge;
import gamesystem.XP.XPChangeAdhoc;
import gamesystem.XP.XPChangeChallenges;
import gamesystem.XP.XPChangeLevel;
import gamesystem.dice.CombinedDice;
import party.CharacterAttackForm;

public class XMLParserHelper {

	protected void parseInitiativeModifier(Element e, Creature c) {
		if (!e.getTagName().equals("Initiative")) return;
		c.initiative.setValue(Integer.parseInt(e.getAttribute("value")));
	}

	protected void parseSanity(Element e, Creature c) {
		if (!e.getTagName().equals("Sanity")) return;
		int val = Integer.parseInt(e.getAttribute("current"));
		c.sanity.setRegularValue(val);
		c.sanity.getKnowledgeSkillProperty().setRegularValue(Integer.parseInt(e.getAttribute("knowledge")));
		if (e.hasAttribute("session")) {
			c.sanity.sessionStart = Integer.parseInt(e.getAttribute("session"));
		} else {
			c.sanity.startSession();
		}
	}

	// TODO Character param here is bad. currently need it to do levelups.
	protected void parseLevel(Element e, Levels lvl, party.Character c) {
		if (!e.getTagName().equals("Level")) return;
		lvl.setLevel(Integer.parseInt(e.getAttribute("level")));

		NodeList classes = e.getChildNodes();
		int i = 1;
		for (int j = 0; j < classes.getLength(); j++) {
			if (classes.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
			Element node = (Element) classes.item(j);
			if (node.getNodeName().equals("Class")) {
				CharacterClass cls = CharacterClass.getCharacterClass(node.getAttribute("class"));
				if (c != null) {
					c.setClass(i, cls);
				} else {
					lvl.setClass(i, cls);
				}
				if (node.hasAttribute("hp-roll")) {
					lvl.setHPRoll(i, Integer.parseInt(node.getAttribute("hp-roll")));
				}
				i++;

			} else if (node.getNodeName().equals("ClassOption")) {
				if (c != null) {
					c.setClassOption(node.getAttribute("id"), node.getAttribute("selection"));
				}
			}
		}

	}

	protected void parseAbilityScore(Element e, Creature c) {
		if (!e.getTagName().equals("AbilityScore")) return;
		AbilityScore.Type type = AbilityScore.Type.getAbilityType(e.getAttribute("type"));
		AbilityScore s = c.getAbilityStatistic(type);

		s.setBaseValue(Integer.parseInt(e.getAttribute("value")));
		if (e.hasAttribute("temp")) s.setOverride(Integer.parseInt(e.getAttribute("temp")));
	}

	protected void parseSavingThrow(Element e, Creature c) {
		if (!e.getTagName().equals("Save")) return;
		SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(e.getAttribute("type"));
		SavingThrow s = c.getSavingThrowStatistic(type);

		if (e.hasAttribute("base")) {
			s.setBaseOverride(Integer.parseInt(e.getAttribute("base")));
		}
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

	protected void parseAC(Element e, Creature c) {
		if (!e.getTagName().equals("AC")) return;

		NodeList children = e.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			if (children.item(j).getNodeName().equals("Armor")) {
				parseArmor((Element) children.item(j), c.ac.armor);
			} else if (children.item(j).getNodeName().equals("Shield")) {
				parseShield((Element) children.item(j), c.ac.shield);
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

	protected void parseSize(Element e, Creature c) {
		if (!e.getTagName().equals("Size")) return;
		c.size.setBaseSize(SizeCategory.getSize(e.getAttribute("category")));
		c.size.setBaseSpace(Integer.parseInt(e.getAttribute("space")));
		c.size.setBaseReach(Integer.parseInt(e.getAttribute("reach")));
	}

	protected void parseHPs(Element e, Creature c) {
		if (!e.getTagName().equals("HitPoints")) return;
		if (e.hasAttribute("maximum")) c.hps.getMaxHPStat().addOverride(Integer.parseInt(e.getAttribute("maximum")));
		if (e.hasAttribute("wounds")) c.hps.getWoundsProperty().setValue(Integer.parseInt(e.getAttribute("wounds")));
		if (e.hasAttribute("non-lethal")) c.hps.getNonLethalProperty().setValue(Integer.parseInt(e.getAttribute("non-lethal")));
		// TODO this means that HPs must be parsed after ability scores. we really need accurate reporting of old con mod in the event

//		System.out.println("Before parse:");
//		c.hps.printTempHPs();

		// set any existing temporary hps to 0. this prevents temporary hitpoints that have been used for a particular
		// buff being reset. after we've parsed this element we'll remove any remaining temporary hitpoints.
		for (HPs.TempHPs temp : c.hps.tempHPs) {
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
				for (HPs.TempHPs temp : c.hps.tempHPs) {
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
				HPs.TempHPs temp = c.hps.new TempHPs(source, hps);
				temp.id = id;
				c.hps.addTemporaryHPs(temp);
			}
		}

		// clean up: check active flags are correctly set and remove any tempHPs set to 0
		List<HPs.TempHPs> toDelete = new ArrayList<>();
		Map<String, HPs.TempHPs> best = new HashMap<>();
		for (HPs.TempHPs temp : c.hps.tempHPs) {
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
			c.hps.tempHPs.remove(temp);
			// TODO need to notify source / remove source if it has no other effect
		}

//		System.out.println("After parse:");
//		c.hps.printTempHPs();
	}

	protected void parseAttacks(Element e, Creature c) {
		if (!e.getTagName().equals("Attacks")) return;

		if (e.hasAttribute("temp")) c.setBABOverride(Integer.parseInt(e.getAttribute("temp")));
		if (e.hasAttribute("power_attack")) c.attacks.setPowerAttack(Integer.parseInt(e.getAttribute("power_attack")));
		if (e.hasAttribute("combat_expertise")) c.attacks.setCombatExpertise(Integer.parseInt(e.getAttribute("combat_expertise")));
		if (e.getAttribute("total_defense").equals("true") || e.getAttribute("total_defense").equals("1")) c.attacks.setTotalDefense(true);
		if (e.getAttribute("fighting_defensively").equals("true") || e.getAttribute("total_defense").equals("1")) c.attacks.setFightingDefensively(true);
	}

	protected Feat parseFeat(Element b) {
		if (!b.getTagName().equals("Feat")) return null;
		FeatDefinition featDef = Feat.getFeatDefinition(b.getAttribute("name"));
		Feat feat = featDef.getFeat();
		return feat;
	}

	protected Buff parseBuff(Element b) {
		if (!b.getTagName().equals("Buff")) return null;
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
		f.setKind(CharacterAttackForm.Kind.getKind(e.getAttribute("kind")));
		if (e.hasAttribute("usage")) f.setUsage(CharacterAttackForm.Usage.values()[Integer.parseInt(e.getAttribute("usage"))]);
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
		if (e.getAttribute("masterwork").equals("true") || e.getAttribute("masterwork").equals("1")) f.setMasterwork(true);
		f.damage = CombinedDice.parse(e.getAttribute("base_damage"));
		if (e.hasAttribute("size")) f.size = SizeCategory.getSize(e.getAttribute("size"));
		f.updateModifiers();
	}

	protected AttackForm addAttackForm(Creature c, String name) {
		return c.attacks.addAttackForm(name);
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

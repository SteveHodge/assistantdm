package gamesystem;

import gamesystem.Attacks.AttackForm;
import gamesystem.Buff.PropertyChange;
import gamesystem.XP.Challenge;
import gamesystem.XP.XPChangeAdhoc;
import gamesystem.XP.XPChangeChallenges;
import gamesystem.XP.XPChangeLevel;

import java.util.ArrayList;
import java.util.Collections;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import party.CharacterAttackForm;

// TODO extract creature generic super class and move character specific code to class in the party package
// TODO this should become a base class of helper methods that doesn't actually implement the interface. the process... methods should return the element instead of adding it. subclasses can implement the processor interface(s)
public class XMLOutputHelper {
	protected Document doc;
	protected Element creatureEl;
	protected Element abilityEl;
	protected Element savesEl;
	protected Element buffEl;
	protected Element featEl;
	protected Element levelEl;
	protected Element attacksEl;

	public XMLOutputHelper(Document d) {
		doc = d;
	}

	public Document getDOM() {
		return doc;
	}

	public Element getElement() {
		return creatureEl;
	}

	public void processProperty(String property, Object value) {
		// TODO handle the "standard" attributes here instead of in processCreature
	}

	protected void setAttributeFromProperty(Creature c, Element e, String name, String prop) {
		String value = (String) c.getProperty(prop);
		if (value != null && value.length() > 0) e.setAttribute(name, value);
	}

	protected Element getAbilityScoreElement(AbilityScore s) {
		if (abilityEl == null) {
			abilityEl = doc.createElement("AbilityScores");
			creatureEl.appendChild(abilityEl);
		}

		Element e = doc.createElement("AbilityScore");
		e.setAttribute("type", s.type.toString());
		e.setAttribute("value", "" + s.baseValue);
		if (s.override != -1) e.setAttribute("temp", "" + s.override);

		abilityEl.appendChild(e);
		return e;
	}

	protected Element getAttacksElement(Attacks attacks) {
		if (creatureEl == null) return null;

		attacksEl = doc.createElement("Attacks");
		attacksEl.setAttribute("base", "" + attacks.getBAB());
		if (attacks.powerAttack != null) attacksEl.setAttribute("power_attack", "" + attacks.getPowerAttack());
		if (attacks.combatExpertise != null) attacksEl.setAttribute("combat_expertise", "" + attacks.getCombatExpertise());
		if (attacks.isTotalDefense) attacksEl.setAttribute("total_defense", "true");
		if (attacks.isFightingDefensively) attacksEl.setAttribute("fighting_defensively", "true");

		creatureEl.appendChild(attacksEl);
		return attacksEl;
	}

	private Element getAttackFormElement(AttackForm a) {
		Element e = doc.createElement("AttackForm");
		e.setAttribute("name", a.name);
		if (a.enhancement != null) {
			e.setAttribute("enhancement", "" + a.enhancement.getModifier());
		} else {
			e.setAttribute("enhancement", "0");
		}
		e.setAttribute("base_damage", a.damage.toString());
		e.setAttribute("damage", a.getDamage());
		e.setAttribute("size", "" + a.size);

		// informational attributes:
		e.setAttribute("total", "" + a.getValue());
//			if (include_info) {
//				e.setAttribute("attacks", a.getAttacksDescription());
//				e.setAttribute("info", a.getSummary());
//				e.setAttribute("damage_info", a.getDamageSummary());
//			}
		return e;
	}

	public void processCharacterAttackForm(CharacterAttackForm a) {
		Element e = getAttackFormElement(a.attack);
		e.setAttribute("critical", a.critical);
		if (a.range > 0) e.setAttribute("range", "" + a.range);
		if (a.weight > 0) e.setAttribute("weight", "" + a.weight);
		e.setAttribute("type", a.damage_type);
		if ((a.properties == null || a.properties.length() == 0) && a.getUsage() != null) {
			e.setAttribute("properties", a.getUsage().toString());
		} else {
			e.setAttribute("properties", a.properties);
		}
		e.setAttribute("ammunition", a.ammunition);
		e.setAttribute("kind", a.getKind().toString());
		e.setAttribute("usage", "" + a.getUsage().ordinal());

		attacksEl.appendChild(e);
	}

	protected Element getHPsElement(HPs hps) {
		if (creatureEl == null) return null;

		Element e = doc.createElement("HitPoints");
		e.setAttribute("maximum", "" + hps.getMaximumHitPoints());
		if (hps.getWounds() != 0) e.setAttribute("wounds", "" + hps.getWounds());
		if (hps.getNonLethal() != 0) e.setAttribute("non-lethal", "" + hps.getNonLethal());

		for (Modifier t : hps.getTemporaryHPsModifiers()) {
			Element me = doc.createElement("TempHPs");
			me.setAttribute("hps", "" + t.getModifier());
			me.setAttribute("source", t.getSource());
			if (t.getID() > 0) me.setAttribute("id", "" + t.getID());
			e.appendChild(me);
		}

		creatureEl.appendChild(e);
		return e;
	}

	public void processBuff(Buff b) {
		if (creatureEl == null) return;
		if (buffEl == null) {
			buffEl = doc.createElement("Buffs");
			creatureEl.appendChild(buffEl);
		}
		buffEl.appendChild(getBuffElement(b, "Buff"));
	}

	public void processFeat(Buff feat) {
		if (featEl == null) {
			featEl = doc.createElement("Feats");
			creatureEl.appendChild(featEl);
		}
		featEl.appendChild(getBuffElement(feat, "Feat"));
	}

	protected Element getBuffElement(Buff b, String tag) {
		Element e = doc.createElement(tag);
		e.setAttribute("name", b.name);
		if (b.casterLevel > 0) e.setAttribute("caster_level", "" + b.casterLevel);
		e.setAttribute("id", "" + b.id);

		for (Modifier m : b.modifiers.keySet()) {
			Element me = doc.createElement("Modifier");
			if (m.getType() != null) me.setAttribute("type", m.getType());
			if (m.getCondition() != null) me.setAttribute("condition", m.getCondition());
			me.setAttribute("value", "" + m.getModifier());
			String target = b.modifiers.get(m);
			me.setAttribute("target", target);		// WISH this can be XML relevant, i.e. an XPath
			me.setAttribute("description", Buff.getTargetDescription(target) + ": " + m.toString());		// TODO this is needed only for the character sheet
			e.appendChild(me);
		}

		for (PropertyChange p : b.propertyChanges.keySet()) {
			Element pe = doc.createElement("PropertyChange");
			pe.setAttribute("description", p.description);	// TODO might need to be more useful for the character sheet
			pe.setAttribute("property", p.property);
			pe.setAttribute("value", p.value.toString());
			String target = b.propertyChanges.get(p);
			pe.setAttribute("target", target);		// WISH this can be XML relevant, i.e. an XPath
			e.appendChild(pe);
		}

		return e;
	}

	protected Element getInitiativeElement(InitiativeModifier initiative) {
		if (creatureEl == null) return null;

		Element e = doc.createElement("Initiative");
		e.setAttribute("value", "" + initiative.getBaseValue());

		creatureEl.appendChild(e);
		return e;
	}

	public void processSize(Size size) {
		if (creatureEl == null) return;

		Element e = doc.createElement("Size");
		e.setAttribute("category", "" + size.getBaseSize());
		e.setAttribute("space", "" + size.getSpace());
		e.setAttribute("reach", "" + size.getReach());

		creatureEl.appendChild(e);
	}

	public void processAbilityScore(AbilityScore s) {
		getAbilityScoreElement(s);
	}

	public void processAttacks(Attacks attacks) {
		getAttacksElement(attacks);
	}

	public void processHPs(HPs hps) {
		getHPsElement(hps);
	}

	public void processSkills(Skills skills) {
		if (creatureEl == null) return;

		Element e = doc.createElement("Skills");

		ArrayList<SkillType> set = new ArrayList<>(skills.skills.keySet());
		Collections.sort(set, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		for (SkillType s : set) {
			Element se = doc.createElement("Skill");
			se.setAttribute("type", s.name);
			Skill skill = skills.skills.get(s);
			se.setAttribute("ranks", "" + skill.ranks);
			if (skill.misc != 0) se.setAttribute("misc", "" + skill.misc);
			e.appendChild(se);
		}

		creatureEl.appendChild(e);
	}

	public void processInitiative(InitiativeModifier initiative) {
		getInitiativeElement(initiative);
	}

	public void processXPChange(XPChangeAdhoc xp) {
		Element e = doc.createElement("XPChange");
		e.setAttribute("xp", "" + xp.xp);
		if (xp.date != null) {
			e.setAttribute("date", XP.dateFormat.format(xp.date));
		}
		if (xp.comment != null && xp.comment.length() > 0) {
			e.setTextContent(xp.comment);
		}
		levelEl.appendChild(e);
	}

	public void processXPChange(XPChangeChallenges xp) {
		Element e = doc.createElement("XPAward");
		e.setAttribute("xp", "" + xp.xp);
		e.setAttribute("level", "" + xp.level);
		e.setAttribute("party", "" + xp.partyCount);
		e.setAttribute("penalty", "" + xp.penalty);
		if (xp.date != null) {
			e.setAttribute("date", XP.dateFormat.format(xp.date));
		}
		if (xp.comment != null && xp.comment.length() > 0) {
			Element c = doc.createElement("Comment");
			c.setTextContent(xp.comment);
			e.appendChild(c);
		}
		for (Challenge c : xp.challenges) {
			Element ce = doc.createElement("XPChallenge");
			ce.setAttribute("cr", c.cr.toString());
			ce.setAttribute("number", "" + c.number);
			ce.setTextContent(c.comment);
			e.appendChild(ce);
		}
		levelEl.appendChild(e);
	}

	public void processXPChange(XPChangeLevel xp) {
		Element e = doc.createElement("XPLevelChange");
		e.setAttribute("old", "" + xp.oldLevel);
		e.setAttribute("new", "" + xp.newLevel);
		if (xp.date != null) {
			e.setAttribute("date", XP.dateFormat.format(xp.date));
		}
		if (xp.comment != null && xp.comment.length() > 0) {
			e.setTextContent(xp.comment);
		}
		levelEl.appendChild(e);
	}

	protected Element getACElement(AC ac) {
		if (creatureEl == null) return null;

		Element e = doc.createElement("AC");
		Element armor = doc.createElement(ac.armor.name);
		armor.setAttribute("description", ac.armor.description);
		armor.setAttribute("bonus", "" + ac.armor.bonus);
		if (ac.armor.enhancement != null) {
			armor.setAttribute("enhancement", "" + ac.armor.enhancement.getModifier());
		}
		armor.setAttribute("weight", "" + ac.armor.weight);
		armor.setAttribute("acp", "" + ac.armor.acp);
		armor.setAttribute("spell_failure", "" + ac.armor.spellFailure);
		armor.setAttribute("properties", "" + ac.armor.properties);
		armor.setAttribute("type", ac.armor.type);
		armor.setAttribute("speed", "" + ac.armor.speed);
		if (ac.dexMod != null) armor.setAttribute("max_dex", "" + ac.dexMod.getLimit());
		e.appendChild(armor);

		Element shield = doc.createElement(ac.shield.name);
		shield.setAttribute("description", ac.shield.description);
		shield.setAttribute("bonus", "" + ac.shield.bonus);
		if (ac.shield.enhancement != null) {
			shield.setAttribute("enhancement", "" + ac.shield.enhancement.getModifier());
		}
		shield.setAttribute("weight", "" + ac.shield.weight);
		shield.setAttribute("acp", "" + ac.shield.acp);
		shield.setAttribute("spell_failure", "" + ac.shield.spellFailure);
		shield.setAttribute("properties", "" + ac.shield.properties);
		e.appendChild(shield);

		return e;
	}
}
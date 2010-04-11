package party;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.XP.Challenge;

import xml.XML;

public class Character extends Creature implements XML {
	protected String name;
	protected int initModifier = 0;
	protected int[] saves = new int[3];
	protected int[] abilities = new int[6];
	protected int[] tempAbilities = new int[6];
	protected Map<Skill,Float> skillRanks = new HashMap<Skill,Float>();
	protected Map<Skill,Integer> skillMisc = new HashMap<Skill,Integer>();
	protected int hps, wounds, nonLethal, xp = 0, level = 1;
	protected int[] ac = new int[AC_MAX_INDEX];
	protected List<XPChange> xpChanges = new ArrayList<XPChange>();

	public static class XPChange {
		//TODO comments
		//TODO date
	}

	public static class XPChangeChallenges extends XPChange {
		int xp;	// xp earned from challenges
		int level;	// level when meeting challenges
		int partyCount;	// number of party members meeting challenges
		int penalty;	// % penalty applied to xp
		List<Challenge> challenges = new ArrayList<Challenge>();

		public String getXML(String indent, String nextIndent) {
			StringBuilder b = new StringBuilder();
			String nl = System.getProperty("line.separator");
			b.append(indent).append("<XPAward xp=\"").append(xp);
			b.append("\" level=\"").append(level);
			b.append("\" party=\"").append(partyCount);
			b.append("\" penalty=\"").append(penalty);
			b.append("\">").append(nl);
			for (Challenge c : challenges) {
				b.append(c.getXML(indent+nextIndent,nextIndent));
			}
			b.append(indent).append("</XPAward>").append(nl);
			return b.toString();
		}

		public static XPChangeChallenges parseDOM(Element e) {
			if (!e.getNodeName().equals("XPAward")) return null;
			XPChangeChallenges c = new XPChangeChallenges();
			c.xp = Integer.parseInt(e.getAttribute("xp"));
			c.level = Integer.parseInt(e.getAttribute("level"));
			c.partyCount = Integer.parseInt(e.getAttribute("party"));
			if (e.hasAttribute("penalty")) c.penalty = Integer.parseInt(e.getAttribute("penalty"));

			NodeList nodes = e.getChildNodes();
			if (nodes != null) {
				for (int i=0; i<nodes.getLength(); i++) {
					if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
					Element child = (Element)nodes.item(i);
					String tag = child.getTagName();

					if (tag.equals("XPChallenge")) {
						Challenge chal = Challenge.parseDOM(child);
						c.challenges.add(chal);
					}
				}
			}
			return c;
		}
	}

	public static class XPChangeAdhoc extends XPChange {
		int xp;
	}

	public class XPChangeLevel extends XPChange {
		int oldLevel;
		int newLevel;
	}

	public Character(String n) {
		name = n;
		for (int i=0; i<6; i++) tempAbilities[i] = -1;
	}

	public String toString() {
		return name;
	}

	public void addXPChallenges(int count, int penalty, Collection<Challenge> challenges) {
		XPChangeChallenges change = new XPChangeChallenges();
		change.level = level;
		change.partyCount = count;
		change.penalty = penalty;
		change.xp = XP.getXP(level, count, penalty, challenges);
		change.challenges.addAll(challenges);
		xpChanges.add(change);
		xp += change.xp;	//TODO fire property change
	}

	public int getAbilityScore(int type) {
		if (tempAbilities[type] != -1) return tempAbilities[type];
		return abilities[type];
	}

	public int getBaseAbilityScore(int type) {
		return abilities[type];
	}

	public int getAbilityModifier(int type) {
		return getModifier(getAbilityScore(type));
	}

	protected void fireAbilityChange(int type, int old, int value) {
        pcs.firePropertyChange(PROPERTY_ABILITY_PREFIX+ability_names[type], old, value);
        int modDelta = Creature.getModifier(value) - Creature.getModifier(old);
        if (modDelta != 0) {
        	// skills
        	// TODO this is inefficient
        	for (Skill skill : Skill.skills.values()) {
        		if (skill.ability == type) {
        	        pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+skill, getSkillTotal(skill)-modDelta, getSkillTotal(skill));
        		}
        	}

        	// saves
        	for (int i = 0; i < 3; i++) {
        		int stat = Creature.getSaveAbility(i);
        		if (stat == type) {
        			pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(i),
        					getSavingThrow(i)-modDelta, getSavingThrow(i)
        				);
        		}
        	}

        	// initiative
        	if (type == Creature.ABILITY_DEXTERITY) {
        		pcs.firePropertyChange(PROPERTY_INITIATIVE, getInitiativeModifier()-modDelta,
        				getInitiativeModifier()
        			);
        	}

        	// TODO other properties
        }
	}

	public void setAbilityScore(int type, int value) {
		int old = abilities[type];
		abilities[type] = value;
		fireAbilityChange(type,old,value);
	}

	public void setTemporaryAbility(int type, int value) {
		int old = tempAbilities[type];
		if (old == -1) old = abilities[type];
		if (value == abilities[type] || value == -1) {
			// reseting to normal score
			tempAbilities[type] = -1;
			fireAbilityChange(type,old,abilities[type]);
		} else {
			tempAbilities[type] = value;
			fireAbilityChange(type,old,value);
		}
	}

	public Set<Skill> getSkills() {
		return new HashSet<Skill>(skillRanks.keySet());
	}

	public int getInitiativeModifier() {
		return initModifier+getAbilityModifier(Creature.ABILITY_DEXTERITY);
	}

	public int getBaseInitiative() {
		return initModifier;
	}

	public void setInitiativeModifier(int i) {
		int old = initModifier;
		initModifier = i;
		pcs.firePropertyChange(PROPERTY_INITIATIVE, old, i);
	}

	public int getSavingThrow(int save) {
		return saves[save]+getAbilityModifier(Creature.getSaveAbility(save));
	}

	public int getBaseSavingThrow(int save) {
		return saves[save];
	}

	public void setSavingThrow(int save, int total) {
		int old = saves[save];
		saves[save] = total;
		pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(save), old, total);
	}

	public int getSkillTotal(Skill s) {
		int ranks = 0;
		if (skillRanks.get(s) != null) ranks += skillRanks.get(s);
		if (skillMisc.get(s) != null) ranks += skillMisc.get(s);
		int ability = s.getAbility();
		if (ability == -1) return ranks;
		return ranks+getAbilityModifier(ability);
	}

	public float getSkillRanks(Skill s) {
		Float ranks = skillRanks.get(s);
		if (ranks == null) return 0;
		return ranks;
	}

	public int getSkillMisc(Skill s) {
		Integer ranks = skillMisc.get(s);
		if (ranks == null) return 0;
		return ranks;
	}

	public void setSkillRanks(Skill s, float ranks) {
		float old = 0;
		if (skillRanks.containsKey(s)) old = skillRanks.get(s);
		skillRanks.put(s,ranks);
        pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+s, old, ranks);	// TODO not actually the correct old and new values
	}

	public void setSkillMisc(Skill s, int misc) {
		int old = 0;
		if (skillMisc.containsKey(s)) old = skillMisc.get(s);
		skillMisc.put(s,misc);
        pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+s, old, misc);		// TODO not actually the correct old and new values
	}

	public void deleteSkill(Skill skill) {
		if (skillRanks.containsKey(skill)) {
			float old = skillRanks.get(skill);
			skillRanks.remove(skill);
	        pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+skill, old, 0);
		}
	}

	public String getName() {
		return name;
	}

	public int getMaximumHitPoints() {
		return hps;
	}

	public void setMaximumHitPoints(int hp) {
		int old = hps;
		hps = hp;
        pcs.firePropertyChange(PROPERTY_MAXHPS, old, hp);
	}		

	public int getWounds() {
		return wounds;
	}

	public void setWounds(int i) {
		int old = wounds;
		wounds = i;
		pcs.firePropertyChange(PROPERTY_WOUNDS, old, i);
	}

	public int getNonLethal() {
		return nonLethal;
	}

	public void setNonLethal(int i) {
		int old = nonLethal;
		nonLethal = i;
		pcs.firePropertyChange(PROPERTY_NONLETHAL, old, i);
	}

	public int getHPs() {
		return hps - wounds - nonLethal;
	}

	public void setACComponent(int type, int value) {
		int oldValue = ac[type];
		ac[type] = value;
		pcs.firePropertyChange(PROPERTY_AC, oldValue, value);
	}

	public int getACComponent(int type) {
		return ac[type];
	}

	public int getAC() {
		int totAC = 10;
		for (int i = 0; i < AC_MAX_INDEX; i++) {
			totAC += ac[i];
		}
		return totAC;
	}

	public int getFlatFootedAC() {
		if (ac[AC_DEX] < 1) return getAC();
		return getAC() - ac[AC_DEX];
	}

	public int getTouchAC() {
		return getAC() - ac[AC_ARMOR] - ac[AC_SHIELD] - ac[AC_NATURAL];
	}

	
	// FIXME these next four methods are from Creature - we shouldn't have them here
	public void setAC(int ac) {
		System.out.println("Setting AC");
		throw new UnsupportedOperationException();
	}

	public void setTouchAC(int ac) {
		System.out.println("Setting Touch AC");
		throw new UnsupportedOperationException();
	}

	public void setFlatFootedAC(int ac) {
		System.out.println("Setting Flat Footed AC");
		throw new UnsupportedOperationException();
	}

	public void setName(String name) {
		System.out.println("Setting Name");
		throw new UnsupportedOperationException();
	}

	public int getXP() {
		return xp;
	}

	// TODO remove when other methods allow full control
	public void setXP(int xp) {
		this.xp = xp;
	}

	public int getLevel() {
		return level;
	}

	// TODO remove when other methods allow full control?
	public void setLevel(int l) {
		level = l;
	}

	public static Character parseDOM(Element el) {
		if (!el.getNodeName().equals("Character")) return null;
		Character c = new Character(el.getAttribute("name"));

		NodeList nodes = el.getChildNodes();
		if (nodes != null) {
			for (int i=0; i<nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
				Element e = (Element)nodes.item(i);
				String tag = e.getTagName();

				if (tag.equals("HitPoints")) {
					c.setMaximumHitPoints(Integer.parseInt(e.getAttribute("maximum")));
					if (e.hasAttribute("wounds")) c.setWounds(Integer.parseInt(e.getAttribute("wounds")));
					if (e.hasAttribute("non-lethal")) c.setNonLethal(Integer.parseInt(e.getAttribute("non-lethal")));

				} else if (tag.equals("Initiative")) {
					String value = e.getAttribute("value");
					if (value != null) c.setInitiativeModifier(Integer.parseInt(value));

				} else if (tag.equals("Level")) {
					c.setLevel(Integer.parseInt(e.getAttribute("level")));
					c.setXP(Integer.parseInt(e.getAttribute("xp")));
					NodeList awards = e.getChildNodes();
					if (awards != null) {
						for (int j=0; j<awards.getLength(); j++) {
							if (!awards.item(j).getNodeName().equals("XPAward")) continue;
							XPChangeChallenges chal = XPChangeChallenges.parseDOM((Element)awards.item(j));
							if (chal != null) c.xpChanges.add(chal);
						}
					}
					

				} else if (tag.equals("AbilityScores")) {
					NodeList abilities = e.getChildNodes();
					if (abilities != null) {
						for (int j=0; j<abilities.getLength(); j++) {
							if (!abilities.item(j).getNodeName().equals("AbilityScore")) continue;
							Element s = (Element)abilities.item(j);
							String value = s.getAttribute("value");
							String type = s.getAttribute("type");
							for (int k=0; k<ability_names.length; k++) {
								if (ability_names[k].equals(type)) {
									c.setAbilityScore(k, Integer.parseInt(value));
								}
							}
						}
					}

				} else if (tag.equals("SavingThrows")) {
					NodeList saves = e.getChildNodes();
					if (saves != null) {
						for (int j=0; j<saves.getLength(); j++) {
							if (!saves.item(j).getNodeName().equals("Save")) continue;
							Element s = (Element)saves.item(j);
							String value = s.getAttribute("value");
							String type = s.getAttribute("type");
							for (int k=0; k<save_names.length; k++) {
								if (save_names[k].equals(type)) {
									c.setSavingThrow(k, Integer.parseInt(value));
								}
							}
						}
					}

				} else if (tag.equals("Skills")) {
					NodeList skills = e.getChildNodes();
					if (skills != null) {
						for (int j=0; j<skills.getLength(); j++) {
							if (!skills.item(j).getNodeName().equals("Skill")) continue;
							Element s = (Element)skills.item(j);
							String ranks = s.getAttribute("ranks");
							String type = s.getAttribute("type");
							Skill skill = Skill.getSkill(type);
							c.setSkillRanks(skill, Float.parseFloat(ranks));
							String misc = s.getAttribute("misc");
							if (misc != "") c.setSkillMisc(skill, Integer.parseInt(misc));
						}
					}

				} else if (tag.equals("AC")) {
					NodeList acs = e.getChildNodes();
					if (acs != null) {
						for (int j=0; j<acs.getLength(); j++) {
							if (!acs.item(j).getNodeName().equals("ACComponent")) continue;
							Element s = (Element)acs.item(j);
							String value = s.getAttribute("value");
							String type = s.getAttribute("type");
							for (int k=0; k<ac_names.length; k++) {
								if (ac_names[k].equals(type)) {
									c.setACComponent(k, Integer.parseInt(value));
								}
							}
						}
					}
				}
			}
		}

		return c;
	}

	public String getXML() {
		return getXML("","    ");
	}

	public String getXML(String indent, String nextIndent) {
		StringBuilder b = new StringBuilder();
		String nl = System.getProperty("line.separator");
		String i2 = indent + nextIndent;
		String i3 = i2 + nextIndent;
		b.append(indent).append("<Character name=\"").append(getName()).append("\">").append(nl);
		b.append(i2).append("<Level level=\"").append(getLevel()).append("\" xp=\"").append(getXP()).append("\">").append(nl);
		for (XPChange c : xpChanges) {
			if (c instanceof XPChangeChallenges) {
				XPChangeChallenges cc = (XPChangeChallenges)c;
				b.append(cc.getXML(i3, nextIndent));
			}
		}
		b.append(i2).append("</Level>").append(nl);
		b.append(i2).append("<AbilityScores>").append(nl);
		for (int i=0; i<ability_names.length; i++) {
			b.append(i3).append("<AbilityScore type=\"").append(ability_names[i]);
			b.append("\" value=\"").append(getBaseAbilityScore(i)).append("\"/>").append(nl);
		}
		b.append(i2).append("</AbilityScores>").append(nl);
		b.append(i2).append("<HitPoints maximum=\"").append(getMaximumHitPoints()).append("\"");
		if (getWounds() != 0) b.append(" wounds=\"").append(getWounds()).append("\"");
		if (getNonLethal() != 0) b.append(" non-lethal=\"").append(getNonLethal()).append("\"");
		b.append("/>").append(nl);
		b.append(i2).append("<Initiative value=\"").append(getBaseInitiative()).append("\"/>").append(nl);
		b.append(i2).append("<SavingThrows>").append(nl);
		for (int i=0; i<save_names.length; i++) {
			b.append(i3).append("<Save type=\"").append(save_names[i]);
			b.append("\" value=\"").append(getBaseSavingThrow(i)).append("\"/>").append(nl);
		}
		b.append(i2).append("</SavingThrows>").append(nl);
		b.append(i2).append("<Skills>").append(nl);
		for (Skill s : skillRanks.keySet()) {
			if (getSkillRanks(s) != 0 || getSkillMisc(s) != 0) {
				b.append(i3).append("<Skill type=\"").append(s);
				b.append("\" ranks=\"").append(getSkillRanks(s));
				if (getSkillMisc(s) != 0) b.append("\" misc=\"").append(getSkillMisc(s));
				b.append("\"/>").append(nl);
			}
		}
		b.append(i2).append("</Skills>").append(nl);
		b.append(i2).append("<AC>").append(nl);
		for (int i=0; i<ac_names.length; i++) {
			if (getACComponent(i) != 0) {
				b.append(i3).append("<ACComponent type=\"").append(ac_names[i]);
				b.append("\" value=\"").append(getACComponent(i)).append("\"/>").append(nl);
			}
		}
		b.append(i2).append("</AC>").append(nl);
		b.append(indent).append("</Character>").append(nl);
		return b.toString();
	}

	// Prints the differences between this and inChar
	public void showDiffs(Character inChar) {
		if (!name.equals(inChar.name)) {
			// new attribute
			System.out.println(PROPERTY_NAME+"|"+name+"|"+inChar.name);
		}
		if (hps != inChar.hps) {
			System.out.println(PROPERTY_MAXHPS+"|"+hps+"|"+inChar.hps);
		}
		if (wounds != inChar.wounds) {
			System.out.println(PROPERTY_WOUNDS+"|"+wounds+"|"+inChar.wounds);
		}
		if (nonLethal != inChar.nonLethal) {
			System.out.println(PROPERTY_NONLETHAL+"|"+nonLethal+"|"+inChar.nonLethal);
		}
		if (initModifier != inChar.initModifier) {
			System.out.println(PROPERTY_INITIATIVE+"|"+initModifier+"|"+inChar.initModifier);
		}
		for (int i=0; i<6; i++) {
			if (abilities[i] != inChar.abilities[i]) {
				System.out.println(PROPERTY_ABILITY_PREFIX+ability_names[i]+"|"+abilities[i]+"|"+inChar.abilities[i]);
			}
		}
		for (int i=0; i<6; i++) {
			if (tempAbilities[i] != inChar.tempAbilities[i]) {
				// new attribute
				System.out.println(PROPERTY_ABILITY_OVERRIDE_PREFIX+getAbilityName(i)+"|"+tempAbilities[i]+"|"+inChar.tempAbilities[i]);
			}
		}
		for (int i=0; i<3; i++) {
			if (saves[i] != inChar.saves[i]) {
				System.out.println(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(i)+"|"+saves[i]+"|"+inChar.saves[i]);
			}
		}
		for (int i=0; i<AC_MAX_INDEX; i++) {
			if (ac[i] != inChar.ac[i]) {
				// new attribute
				System.out.println(PROPERTY_AC_COMPONENT_PREFIX+Creature.getACComponentName(i)+"|"+ac[i]+"|"+inChar.ac[i]);
			}
		}
		HashSet<Skill> allSkills = new HashSet<Skill>(skillRanks.keySet());
		allSkills.addAll(inChar.skillRanks.keySet());
		for (Skill skill : allSkills) {
			if (getSkillRanks(skill) != inChar.getSkillRanks(skill)) {
				System.out.println(PROPERTY_SKILL_PREFIX+skill+"|"+getSkillRanks(skill)+"|"+inChar.getSkillRanks(skill));
			}
		}
	}

	// Returns a list of the properties that differ between this and inChar
	// The list is ordered by the type of property
	public List<String> getDifferences(Character inChar) {
		List<String> diffs = new ArrayList<String>();

		if (!name.equals(inChar.name)) diffs.add(PROPERTY_NAME);
		if (hps != inChar.hps) diffs.add(PROPERTY_MAXHPS);
		if (wounds != inChar.wounds) diffs.add(PROPERTY_WOUNDS);
		if (nonLethal != inChar.nonLethal) diffs.add(PROPERTY_NONLETHAL);
		if (initModifier != inChar.initModifier) diffs.add(PROPERTY_INITIATIVE);
		for (int i=0; i<6; i++) {
			if (abilities[i] != inChar.abilities[i]) {
				diffs.add(PROPERTY_ABILITY_PREFIX+getAbilityName(i));
			}
		}
		for (int i=0; i<6; i++) {
			if (tempAbilities[i] != inChar.tempAbilities[i]) {
				diffs.add(PROPERTY_ABILITY_OVERRIDE_PREFIX+getAbilityName(i));
			}
		}
		for (int i=0; i<3; i++) {
			if (saves[i] != inChar.saves[i]) {
				diffs.add(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(i));
			}
		}
		for (int i=0; i<AC_MAX_INDEX; i++) {
			if (ac[i] != inChar.ac[i]) {
				diffs.add(PROPERTY_AC_COMPONENT_PREFIX+Creature.getACComponentName(i));
			}
		}
		HashSet<Skill> allSkills = new HashSet<Skill>(skillRanks.keySet());
		allSkills.addAll(inChar.skillRanks.keySet());
		for (Skill skill : allSkills) {
			if (getSkillRanks(skill) != inChar.getSkillRanks(skill)) {
				diffs.add(PROPERTY_SKILL_PREFIX+skill);
			}
			if (getSkillMisc(skill) != inChar.getSkillMisc(skill)) {
				diffs.add(PROPERTY_SKILL_MISC_PREFIX+skill);
			}
		}
		return diffs;
	}

	public Object getProperty(String prop) {
		if (prop.equals(PROPERTY_NAME)) return name;
		if (prop.equals(PROPERTY_MAXHPS)) return hps;
		if (prop.equals(PROPERTY_WOUNDS)) return wounds;
		if (prop.equals(PROPERTY_NONLETHAL)) return nonLethal;
		if (prop.equals(PROPERTY_INITIATIVE)) return initModifier;
		
		if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(Creature.getAbilityName(i))) return abilities[i];
			}
		}

		if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(Creature.getAbilityName(i))) return tempAbilities[i];
			}
		}

		if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(Creature.getSavingThrowName(i))) return saves[i];
			}
		}

		if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (int i=0; i<Creature.AC_MAX_INDEX; i++) {
				if (comp.equals(Creature.getACComponentName(i))) return ac[i];
			}
		}

		if (prop.startsWith(PROPERTY_SKILL_PREFIX)) {
			String skill = prop.substring(PROPERTY_SKILL_PREFIX.length());
			Skill s = Skill.getSkill(skill);
			Float ranks = skillRanks.get(s);
			return ranks;
		}

		if (prop.startsWith(PROPERTY_SKILL_MISC_PREFIX)) {
			String skill = prop.substring(PROPERTY_SKILL_MISC_PREFIX.length());
			Skill s = Skill.getSkill(skill);
			Integer misc = skillMisc.get(s);
			return misc;
		}

		return null;
	}

	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_NAME)) setName((String)value);
		if (prop.equals(PROPERTY_MAXHPS)) setMaximumHitPoints((Integer)value);
		if (prop.equals(PROPERTY_WOUNDS)) setWounds((Integer)value);
		if (prop.equals(PROPERTY_NONLETHAL)) setNonLethal((Integer)value);
		if (prop.equals(PROPERTY_INITIATIVE)) setInitiativeModifier((Integer)value);
		
		if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(Creature.getAbilityName(i))) setAbilityScore(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(Creature.getAbilityName(i))) setTemporaryAbility(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(Creature.getSavingThrowName(i))) setSavingThrow(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (int i=0; i<Creature.AC_MAX_INDEX; i++) {
				if (comp.equals(Creature.getACComponentName(i))) setACComponent(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_SKILL_PREFIX)) {
			String skillName = prop.substring(PROPERTY_SKILL_PREFIX.length());
			Skill skill = Skill.getSkill(skillName);
			if (value == null) {
				// TODO not sure we should be deleting skills this way anymore
				deleteSkill(skill);
			} else {
				setSkillRanks(skill, (Float)value);
			}
		}

		if (prop.startsWith(PROPERTY_SKILL_MISC_PREFIX)) {
			String skillName = prop.substring(PROPERTY_SKILL_MISC_PREFIX.length());
			Skill skill = Skill.getSkill(skillName);
			setSkillMisc(skill, (Integer)value);
		}
	}
}

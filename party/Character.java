package party;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

/* TODO:
 * Modifying dex should alter ac - but we need to know the max dex modifier
 * Should allow temporary hitpoints - will require careful consideration of how wounds work
 */
/**
 * @author Steve
 *
 */
public class Character extends Creature implements XML {
	protected String name;
	protected int initModifier = 0;
	protected int[] saves = new int[3];
	protected int[] saveMisc = new int[3];
	protected int[] abilities = new int[6];
	protected int[] tempAbilities = new int[6];
	protected Map<Skill,Float> skillRanks = new HashMap<Skill,Float>();
	protected Map<Skill,Integer> skillMisc = new HashMap<Skill,Integer>();
	protected int hps, wounds, nonLethal, xp = 0, level = 1;
	protected int[] ac = new int[AC_MAX_INDEX];
	protected int tempAC, tempTouch, tempFF;	// ac overrides
	protected boolean hasTempAC, hasTempTouch, hasTempFF;	// flags for overrides
	protected List<XP.XPChange> xpChanges = new ArrayList<XP.XPChange>();

	public Character(String n) {
		name = n;
		for (int i=0; i<6; i++) tempAbilities[i] = -1;
	}

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		System.out.println("Setting Name");
		throw new UnsupportedOperationException();
	}

//------------------- Ability Scores -------------------
// Ability scores have a base value and can have an override value

   /**
	* Returns the current score of the specified ability. The current score
	* is the temporary score if one has been set by setTemporaryAbility,
	* otherwise it is base ability score
	* 
	* @param type  the ability score to get: one of the ABILITY constants from {@link Creature}
	* @return      the current score of the specified ability
	*/
	public int getAbilityScore(int type) {
		if (tempAbilities[type] != -1) return tempAbilities[type];
		return abilities[type];
	}

   /**
    * Returns the base score of the specified ability. 
    * 
    * @param type  the ability score to get: one of the ABILITY constants from {@link Creature}
    * @return      the base score of the specified ability
    */
	public int getBaseAbilityScore(int type) {
		return abilities[type];
	}

   /**
    * Returns the modifier calculated from the specified ability's current score.
    * 
    * @param type  the ability score to get the modifier of: one of the ABILITY constants from {@link Creature}
    * @return      the modifier calculated from the current score of the specified ability
    */
	public int getAbilityModifier(int type) {
		return getModifier(getAbilityScore(type));
	}

   /**
    * Sets the base score of the specified ability
    * 
    * @param type  the ability score to set: one of the ABILITY constants from {@link Creature}
    * @param value the value to set the score to
    */
	public void setAbilityScore(int type, int value) {
		int old = abilities[type];
		abilities[type] = value;
		fireAbilityChange(type,old,value);
	}

   /**
    * Sets the temporary score of the specified ability. When a temporary score is set,
    * <code>getAbilityScore</code> will return it rather than the base score and
    * <code>getAbilityModifier</code> will calculate the modifier using the temporary
    * score rather than the base score.
    * <p>
    * The temporary score can be removed by setting it to -1 or to the base score of
    * the specified ability.
    *  
    * @param type  the ability score to set: one of the ABILITY constants from {@link Creature}
    * @param value the value to set as the temporary score
    */
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

	// fires the property change for the ability and for any dependent stats
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

        	// hit points
        	if (type == Creature.ABILITY_CONSTITUTION) {
        		int oldhps = getMaximumHitPoints();
        		int oldmod = Creature.getModifier(old);
        		int newmod = Creature.getModifier(value);
        		int newhps = oldhps + (level * (newmod - oldmod));
        		if (newhps < level) newhps = level;	// FIXME if we need to use this then it won't be reversable. probably need a max hp override
        		System.out.println("changing max hps from "+oldhps+" to "+newhps);
        		setMaximumHitPoints(newhps);
        	}

        	// TODO other properties: ac
        }
	}

//------------------- Skills -------------------
// Skills have a total value, ranks, misc bonus, and are modified by ability scores

	/**
	 * Returns the set of all skills that this character can perform, that is all
	 * untrained skills and all skills that this characters has at least one rank in.
	 */
	public Set<Skill> getSkills() {
		Set<Skill> set = new HashSet<Skill>(skillRanks.keySet());
		set.addAll(Skill.getUntrainedSkills());
		return set;
	}

   /**
    * Sets the specified skill to the specified total. Ranks in the skill are adjusted
    * to achieve this.
    * 
    * @param skill  the skill to set
    * @param value  the total value to set the skill to
    */
	public void setSkillTotal(Skill skill, int value) {
		int old = getSkillTotal(skill);
		float ranks = getSkillRanks(skill) + value - old;
		if (ranks < 0) ranks = 0;
		setSkillRanks(skill, ranks);
	}

	public int getSkillTotal(Skill s) {
		int ranks = 0;
		if (skillRanks.containsKey(s)) ranks += skillRanks.get(s);
		if (skillMisc.containsKey(s)) ranks += skillMisc.get(s);
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
		Integer misc = skillMisc.get(s);
		if (misc == null) return 0;
		return misc;
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

//------------------- Initiative -------------------
// Initiative has a total value, a base value and is modified by dexterity

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

//------------------- Saving Throws -------------------
// Saves have a total, a base values and are modified by ability scores
// TODO saves should also have a misc mod
// XXX Note that the property change sets old and new to the base scores for setSavingThrowBase and the total scores for setSavingThrow

	public int getSavingThrow(int save) {
		return saves[save]+saveMisc[save]+getAbilityModifier(Creature.getSaveAbility(save));
	}

	public int getSavingThrowBase(int save) {
		return saves[save];
	}

	public int getSavingThrowMisc(int save) {
		return saveMisc[save];
	}

	public void setSavingThrowBase(int save, int total) {
		int old = saves[save];
		saves[save] = total;
		pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(save), old, total);
	}

	public void setSavingThrowMisc(int save, int total) {
		int old = saveMisc[save];
		saveMisc[save] = total;
		pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(save), old, total);
	}

   /**
    * Sets the saving throw total by modifying the base value.
    * 
    * @param save   the saving throw to set
    * @param total  the total required
    */
	public void setSavingThrow(int save, int total) {
		int old = getSavingThrow(save);
		saves[save] = total-getAbilityModifier(Creature.getSaveAbility(save))-saveMisc[save];
		pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(save), old, total);
	}
//------------------- Hit Points -------------------
// Hit points have a maximum value, wounds taken, non-lethal taken and a calculated
// current value
// TODO hit points should also have a temporary bonus value
// TODO hit points should be modified by con

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

//------------------- Armor Class -------------------
// AC has a total value, a touch value and a flat footed value
// it is made up of a number of different components
// TODO dex changes should apply (need to know max dex bonus)
// XXX modification methods always fire a PROPERTY_AC change to listeners but the
// values set depend on exactly what has changed: they may be the old and new values
// of just the modified component or of the full ac or of the touch ac etc. 
	public void setACComponent(int type, int value) {
		int oldValue = ac[type];
		ac[type] = value;
		pcs.firePropertyChange(PROPERTY_AC, oldValue, value);
	}

	public int getACComponent(int type) {
		return ac[type];
	}

   /**
    * Returns the temporary ac if there is one, otherwise calculates the total ac
    * from the ac components
    * 
    * @return current total ac
    */
	public int getAC() {
		return getAC(true);
	}

	protected int getAC(boolean allowTemp) {
		if (allowTemp && hasTempAC) return tempAC;
		int totAC = 10;
		for (int i = 0; i < AC_MAX_INDEX; i++) totAC += ac[i];
		return totAC;
	}

   /**
    * Returns the temporary flat-footed ac if there is one, otherwise calculates the
    * flat-footed ac from the ac components with any positive dexterity modifier
    * ignored.
    * 
    * @return current flat-footed ac
    */
	public int getFlatFootedAC() {
		return getFlatFootedAC(true);
	}

	protected int getFlatFootedAC(boolean allowTemp) {
		if (allowTemp && hasTempFF) return tempFF;
		int totAC = 10;
		for (int i = 0; i < AC_MAX_INDEX; i++) {
			if (i != AC_DEX || ac[AC_DEX] < 1) {
				totAC += ac[i];
			}
		}
		return totAC;
	}

   /**
    * Returns the temporary touch ac if there is one, otherwise calculates the touch
    * ac from the ac components with all armor, shield and natural armor bonuses
    * ignored.
    * 
    * @return current touch ac
    */
	public int getTouchAC() {
		return getTouchAC(true);
	}

	protected int getTouchAC(boolean allowTemp) {
		if (allowTemp && hasTempTouch) return tempTouch;
		int totAC = 10;
		for (int i = AC_DEX; i < AC_MAX_INDEX; i++) totAC += ac[i];
		return totAC;
	}

   /**
    * Sets a temporary full ac score. Setting this to the normal value will remove
    * the temporary score (as will <code>clearTemporaryAC()</code>
    * 
    * @param ac the score to set the full ac to
    */
	public void setAC(int tempac) {
		if (hasTempTouch) {
			int totAC = getAC(false);
			if (totAC == tempac) {
				hasTempAC = false;
				pcs.firePropertyChange(PROPERTY_AC, tempAC, totAC);
				return;
			}
		}
		int old = getAC();
		tempAC = tempac;
		hasTempAC = true;
		pcs.firePropertyChange(PROPERTY_AC, old, ac);
	}

   /**
    * Sets a temporary touch ac score. Setting this to the normal value will remove
    * the temporary score (as will <code>clearTemporaryTouchAC()</code>
    * 
    * @param ac the score to set the touch ac to
    */
	public void setTouchAC(int tempac) {
		if (hasTempTouch) {
			int totAC = getTouchAC(false);
			if (totAC == tempac) {
				hasTempTouch = false;
				pcs.firePropertyChange(PROPERTY_AC, tempTouch, totAC);
				return;
			}
		}
		int old = getTouchAC();
		tempTouch = tempac;
		hasTempTouch = true;
		pcs.firePropertyChange(PROPERTY_AC, old, ac);
	}

   /**
    * Sets a temporary flat-footed ac score. Setting this to the normal value will
    * remove the temporary score (as will <code>clearTemporaryFlatFootedAC()</code>
    * 
    * @param ac the score to set the flat-footed ac to
    */
	public void setFlatFootedAC(int tempac) {
		if (hasTempFF) {
			int totAC = getFlatFootedAC(false);
			if (totAC == tempac) {
				hasTempFF = false;
				pcs.firePropertyChange(PROPERTY_AC, tempFF, totAC);
				return;
			}
		}
		int old = getFlatFootedAC();
		tempFF = tempac;
		hasTempFF = true;
		pcs.firePropertyChange(PROPERTY_AC, old, ac);
	}

//------------------- XP and level -------------------
	public int getXP() {
		return xp;
	}

	public int getRequiredXP() {
		return XP.getXPRequired(level+1);
	}

	// WISH eventually replace this with a real history facility
	public String getXPHistory() {
		StringBuilder b = new StringBuilder();
		for (XP.XPChange change : xpChanges) {
			b.append(change);
			b.append(System.getProperty("line.separator"));
		}
		return b.toString();
	}
	
	public void addXPChallenges(int count, int penalty, Collection<Challenge> challenges) {
		XP.XPChangeChallenges change = new XP.XPChangeChallenges();
		change.level = level;
		change.partyCount = count;
		change.penalty = penalty;
		change.xp = XP.getXP(level, count, penalty, challenges);
		change.challenges.addAll(challenges);
		xpChanges.add(change);
		int old = xp;
		xp += change.xp;
        pcs.firePropertyChange(PROPERTY_XP, old, xp);
	}

	public void addXPAdhocChange(int delta) {
		xpChanges.add(new XP.XPChangeAdhoc(delta));
		int old = xp;
		xp += delta;
        pcs.firePropertyChange(PROPERTY_XP, old, xp);
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int l) {
		if (level == l) return;
		xpChanges.add(new XP.XPChangeLevel(level,l));
		int old = level;
		level = l;
		pcs.firePropertyChange(PROPERTY_LEVEL, old, level);
	}

//------------------- Import/Export and other methods -------------------
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
					c.hps = Integer.parseInt(e.getAttribute("maximum"));
					if (e.hasAttribute("wounds")) c.wounds = Integer.parseInt(e.getAttribute("wounds"));
					if (e.hasAttribute("non-lethal")) c.nonLethal = Integer.parseInt(e.getAttribute("non-lethal"));

				} else if (tag.equals("Initiative")) {
					String value = e.getAttribute("value");
					if (value != null) c.initModifier = Integer.parseInt(value);

				} else if (tag.equals("Level")) {
					c.level = Integer.parseInt(e.getAttribute("level"));
					c.xp = Integer.parseInt(e.getAttribute("xp"));
					NodeList awards = e.getChildNodes();
					if (awards != null) {
						for (int j=0; j<awards.getLength(); j++) {
							XP.XPChange change = null;
							if (awards.item(j).getNodeName().equals("XPAward")) {
								change = XP.XPChangeChallenges.parseDOM((Element)awards.item(j));
							} else if (awards.item(j).getNodeName().equals("XPChange")) {
								change = XP.XPChangeAdhoc.parseDOM((Element)awards.item(j));
							} else if (awards.item(j).getNodeName().equals("XPLevelChange")) {
								change = XP.XPChangeLevel.parseDOM((Element)awards.item(j));
							}
							if (change != null) c.xpChanges.add(change);
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
									c.abilities[k] = Integer.parseInt(value);
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
							String value = s.getAttribute("base");
							String type = s.getAttribute("type");
							String misc = s.getAttribute("misc");
							for (int k=0; k<save_names.length; k++) {
								if (save_names[k].equals(type)) {
									c.saves[k] = Integer.parseInt(value);
									if (misc != "") c.saveMisc[k] = Integer.parseInt(misc);
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
							c.skillRanks.put(skill,Float.parseFloat(ranks));
							String misc = s.getAttribute("misc");
							if (misc != "") c.skillMisc.put(skill, Integer.parseInt(misc));
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
									c.ac[k] = Integer.parseInt(value);
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
		for (XP.XPChange c : xpChanges) {
			if (c instanceof XP.XPChange) {
				XP.XPChange cc = (XP.XPChange)c;
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
			b.append("\" base=\"").append(getSavingThrowBase(i));
			if (getSavingThrowMisc(i) != 0) b.append("\" misc=\"").append(getSavingThrowMisc(i));
			b.append("\"/>").append(nl);
		}
		b.append(i2).append("</SavingThrows>").append(nl);
		b.append(i2).append("<Skills>").append(nl);
		Set<Skill> set = new HashSet<Skill>(skillRanks.keySet());
		set.addAll(skillMisc.keySet());
		for (Skill s : set) {
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
	// XXX does not include level or xp
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
		//if (xp != inChar.xp) diffs.add(PROPERTY_XP);	// TODO not sure we should include this
		//if (level != inChar.level) diffs.add(PROPERTY_LEVEL);	// TODO not sure we should include this
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
			if (saveMisc[i] != inChar.saveMisc[i]) {
				diffs.add(PROPERTY_SAVE_MISC_PREFIX+Creature.getSavingThrowName(i));
			}
		}
		for (int i=0; i<AC_MAX_INDEX; i++) {
			if (ac[i] != inChar.ac[i]) {
				diffs.add(PROPERTY_AC_COMPONENT_PREFIX+Creature.getACComponentName(i));
			}
		}
		HashSet<Skill> allSkills = new HashSet<Skill>(skillRanks.keySet());
		allSkills.addAll(inChar.skillRanks.keySet());
		List<Skill> skillList = new ArrayList<Skill>(allSkills);
		Collections.sort(skillList);
		for (Skill skill : skillList) {
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
		if (prop.equals(PROPERTY_LEVEL)) return level;
		if (prop.equals(PROPERTY_XP)) return xp;
		
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

		if (prop.startsWith(PROPERTY_SAVE_MISC_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_MISC_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(Creature.getSavingThrowName(i))) return saveMisc[i];
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
		if (prop.equals(PROPERTY_LEVEL)) setLevel((Integer)value);
		//if (prop.equals(PROPERTY_XP)) setXP((Integer)value);	// TODO should this be permitted as an adhoc change?

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
				if (save.equals(Creature.getSavingThrowName(i))) setSavingThrowBase(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_SAVE_MISC_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_MISC_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(Creature.getSavingThrowName(i))) setSavingThrowMisc(i, (Integer)value);
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
				setSkillRanks(skill, 0);
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

package party;
import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.HPs;
import gamesystem.ImmutableModifier;
import gamesystem.InitiativeModifier;
import gamesystem.Level;
import gamesystem.Modifier;
import gamesystem.SavingThrow;
import gamesystem.Skill;
import gamesystem.SkillType;
import gamesystem.Skills;
import gamesystem.Statistic;
import gamesystem.XP;
import gamesystem.XP.Challenge;
import gamesystem.XP.XPChange;
import gamesystem.XP.XPChangeChallenges;
import gamesystem.XP.XPChangeLevel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import xml.XML;

// TODO priorities:
// dice rolling for buffs
// clean up handling of HPs, wounds, healing etc
// review Statistics vs Properties
// ui for adding adhoc modifiers
// size
// equipment, particularly magic item slots, armor, weapons

// TODO rework ac ui: list all bonuses, allow multiple bonuses of each type, AC temp scores?
// TODO convert ui classes that listen to Character to listen to the specific Statistics instead - could do a StatisticsProxy class
// that could be used as a base for statistics that rely on a common set of modifiers such as touch AC, skills etc
// TODO need to review how properties work on Character and BoundIntegerField

/* Things to implement:
 *  Ability score checks
 *  Class levels
 *  Spell lists / spells per day
 *  Temporary hitpoints
 *  Damage reduction
 *  Spell resistance
 *  Base attack bonus
 *  Grapple modifier
 *  Weapons/attacks
 *  Armor
 *  Magic items slots
 *  Weight/encumberance
 *  Feats
 *  Skill synergies
 *  Skill named versions (Crafting, Profession etc)
 *  Size
 */

/**
 * @author Steve
 *
 */
public class Character extends Creature implements XML {
	protected String name;

	protected InitiativeModifier initiative;

	protected SavingThrow[] saves = new SavingThrow[3];
	protected Modifier[] saveMisc = new Modifier[3];
	
	protected AbilityScore[] abilities = new AbilityScore[6];
	//protected int[] tempAbilities = new int[6];

	protected Skills skills;
	//protected Map<SkillType,Skill> skills = new HashMap<SkillType,Skill>();
	protected Map<SkillType,Modifier> skillMisc = new HashMap<SkillType,Modifier>();
	
	protected HPs hps;
	protected Level level; 
	protected int xp = 0;

	protected AC ac;
	protected Modifier[] acMods = new Modifier[AC.AC_MAX_INDEX];
	protected int tempAC, tempTouch, tempFF;	// ac overrides
	protected boolean hasTempAC, hasTempTouch, hasTempFF;	// flags for overrides

	protected Attacks attacks;

	protected List<XPHistoryItem> xpChanges = new ArrayList<XPHistoryItem>();

	public class XPHistoryItem {
		protected XPChange xpChange;
		protected int total;
		protected int index;

		public int getTotal() {
			return total;
		}

		public int getXP() {
			return xpChange.getXP();
		}

		public Date getDate() {
			return xpChange.getDate();
		}

		public String getComment() {
			return xpChange.getComment();
		}

		public int getLevel() {
			for (int i = index; i >= 0; i--) {
				XPHistoryItem item = xpChanges.get(i);
				if (item.xpChange instanceof XPChangeLevel) {
					return ((XPChangeLevel)item.xpChange).getNewLevel();
				}
			}
			return 1;
		}

		public int getOldLevel() {
			if (xpChange instanceof XPChangeLevel) {
				return ((XPChangeLevel)xpChange).getOldLevel();
			}
			return getLevel();
		}

		public boolean isLevelChange() {
			return (xpChange instanceof XPChangeLevel);
		}

		public List<Challenge> getChallenges() {
			if (xpChange instanceof XP.XPChangeChallenges) {
				return ((XPChangeChallenges)xpChange).challenges;
			} else {
				return new ArrayList<Challenge>();
			}
		}

		public int getPartyCount() {
			if (xpChange instanceof XP.XPChangeChallenges) {
				return ((XPChangeChallenges)xpChange).partyCount;
			} else {
				return 0;
			}
		}

		// returns false if the latest level is too high for the current XP
		public boolean isValidLevel() {
			return XP.getXPRequired(getLevel()) <= total;
		}

		// returns true if the total xp exceeds the requirements for the next level
		public boolean canLevelUp() {
			return XP.getXPRequired(getLevel()+1) <= total;
		}
	}

	protected PropertyChangeListener statListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == hps) {
				pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				return;
			}

			// only care about "value" updates from other statistics...
			if (!evt.getPropertyName().equals("value")) return;

			if (evt.getSource() == initiative) {
				//System.out.println("Inititative change event: "+getName()+" old = "+evt.getOldValue()+", new = "+evt.getNewValue());
				pcs.firePropertyChange(PROPERTY_INITIATIVE, evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() instanceof AbilityScore) {
				AbilityScore score = (AbilityScore)evt.getSource();
		        pcs.firePropertyChange(PROPERTY_ABILITY_PREFIX+score.getName(), evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() instanceof Skill) {
				Skill s = (Skill)evt.getSource();
				pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+s.getName(), evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() instanceof Skills) {
				if (evt.getPropertyName().equals("value")) {
					// change to global modifiers or ability modifiers - update all skills
					for (SkillType s : getSkills()) {
						pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+s.getName(), null, null);
					}
				} else {
					pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				}

			} else if (evt.getSource() instanceof SavingThrow) {
				SavingThrow save = (SavingThrow)evt.getSource();
				pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+save.getName(), evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() == ac) {
				pcs.firePropertyChange(PROPERTY_AC, evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() == attacks) {
				pcs.firePropertyChange(PROPERTY_BAB, evt.getOldValue(), evt.getNewValue());

			} else {
				System.out.println("Unknown property change event: "+evt);
			}
		
		}
	};

	public Character(String n) {
		name = n;
		for (int i=0; i<6; i++) {
			abilities[i] = new AbilityScore(i);
			abilities[i].addPropertyChangeListener(statListener);
		}

		initiative = new InitiativeModifier(abilities[AbilityScore.ABILITY_DEXTERITY]);
		initiative.addPropertyChangeListener(statListener);

		for (int i=0; i<3; i++) {
			saves[i] = new SavingThrow(i,abilities[SavingThrow.getSaveAbility(i)]);
			saves[i].addPropertyChangeListener(statListener);
		}

		ac = new AC(abilities[AbilityScore.ABILITY_DEXTERITY]);
		ac.addPropertyChangeListener(statListener);

		skills = new Skills(abilities);
		skills.addPropertyChangeListener(statListener);

		level = new Level();

		hps = new HPs(abilities[AbilityScore.ABILITY_CONSTITUTION], level);
		hps.addPropertyChangeListener(statListener);

		attacks = new Attacks(abilities[AbilityScore.ABILITY_STRENGTH], abilities[AbilityScore.ABILITY_DEXTERITY]);
		attacks.addPropertyChangeListener(statListener);
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
		return abilities[type].getValue();
	}

   /**
    * Returns the base score of the specified ability. 
    * 
    * @param type  the ability score to get: one of the ABILITY constants from {@link Creature}
    * @return      the base score of the specified ability
    */
	public int getBaseAbilityScore(int type) {
		return abilities[type].getBaseValue();
	}

   /**
    * Returns the modifier calculated from the specified ability's current score.
    * 
    * @param type  the ability score to get the modifier of: one of the ABILITY constants from {@link Creature}
    * @return      the modifier calculated from the current score of the specified ability
    */
	public int getAbilityModifier(int type) {
		return abilities[type].getModifierValue();
	}

   /**
    * Sets the base score of the specified ability
    * 
    * @param type  the ability score to set: one of the ABILITY constants from {@link Creature}
    * @param value the value to set the score to
    */
	public void setAbilityScore(int type, int value) {
		abilities[type].setBaseValue(value);
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
		if (value != abilities[type].getBaseValue() && value >= 0) {
			abilities[type].setOverride(value);
		} else {
			abilities[type].clearOverride();
		}
	}

	public int getTemporaryAbility(int type) {
		return abilities[type].getOverride();
	}
//------------------- Skills -------------------
// Skills have a total value, ranks, misc bonus, and are modified by ability scores

	/**
	 * Returns the set of all skills that this character can perform, that is all
	 * untrained skills and all skills that this characters has at least one rank in.
	 */
	public Set<SkillType> getSkills() {
		Set<SkillType> set = skills.getTrainedSkills();
		set.addAll(SkillType.getUntrainedSkills());
		return set;
	}

   /**
    * Sets the specified skill to the specified total. Ranks in the skill are adjusted
    * to achieve this.
    * 
    * @param skill  the skill to set
    * @param value  the total value to set the skill to
    */
	public void setSkillTotal(SkillType skill, int value) {
		int old = getSkillTotal(skill);
		float ranks = getSkillRanks(skill) + value - old;
		if (ranks < 0) ranks = 0;
		setSkillRanks(skill, ranks);
	}

	public int getSkillTotal(SkillType s) {
		return skills.getValue(s);
	}

	public float getSkillRanks(SkillType s) {
		return skills.getRanks(s);
	}

	public int getSkillMisc(SkillType s) {
		Modifier misc = skillMisc.get(s);
		if (misc == null) return 0;
		return misc.getModifier();
	}

	public void setSkillRanks(SkillType s, float ranks) {
		skills.setRanks(s, ranks);
	}

	public void setSkillMisc(SkillType s, int misc) {
		Modifier m = skillMisc.get(s);
		if (m != null) skills.removeModifier(s, m);
		m = new ImmutableModifier(misc);
		skillMisc.put(s,m);
		skills.addModifier(s, m);
	}

//------------------- Initiative -------------------
// Initiative has a total value, a base value and is modified by dexterity

	public int getInitiativeModifier() {
		return initiative.getValue();
	}

	public int getBaseInitiative() {
		return initiative.getBaseValue();
	}

	public void setInitiativeModifier(int i) {
		initiative.setBaseValue(i);
	}

//------------------- Saving Throws -------------------
// Saves have a total, a base values and are modified by ability scores
// XXX Note that the property change sets old and new to the base scores for setSavingThrowBase and the total scores for setSavingThrow

	public int getSavingThrow(int save) {
		return saves[save].getValue();
	}

	public int getSavingThrowBase(int save) {
		return saves[save].getBaseValue();
	}

	public int getSavingThrowMisc(int save) {
		if (saveMisc[save] != null) return saveMisc[save].getModifier();
		else return 0;
	}

	public void setSavingThrowBase(int save, int v) {
		int old = saves[save].getValue();
		saves[save].setBaseValue(v);
		pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+SavingThrow.getSavingThrowName(save), old, saves[save].getValue());
	}

	// TODO currently this works by removing the old modifier and adding a new one. could make the modifiers mutable instead
	public void setSavingThrowMisc(int save, int misc) {
		int old = getSavingThrow(save);
		if (saveMisc[save] != null) saves[save].removeModifier(saveMisc[save]);
		saveMisc[save] = new ImmutableModifier(misc);
		saves[save].addModifier(saveMisc[save]);
		int now = getSavingThrow(save);
		if (old != now) {
			pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+SavingThrow.getSavingThrowName(save), old, now);
		}
	}

   /**
    * Sets the saving throw total by modifying the base value.
    * 
    * @param save   the saving throw to set
    * @param total  the total required
    */
	// TODO this is used by RollsPanel. it should probably be removed
	public void setSavingThrow(int save, int total) {
		int old = getSavingThrow(save);
		saves[save].setBaseValue(total-saves[save].getModifiersTotal());
		int now = getSavingThrow(save);
		if (old != now) {
			pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+SavingThrow.getSavingThrowName(save), old, now);
		}
	}

//------------------- Hit Points -------------------
// Hit points have a maximum value, wounds taken, non-lethal taken and a calculated
// current value
// TODO hit points should also have a temporary bonus value

	public int getMaximumHitPoints() {
		return hps.getMaximumHitPoints();
	}

	public void setMaximumHitPoints(int hp) {
		hps.setMaximumHitPoints(hp);
	}		

	public int getWounds() {
		return hps.getWounds();
	}

	public void setWounds(int i) {
		hps.setWounds(i);
	}

	public int getNonLethal() {
		return hps.getNonLethal();
	}

	public void setNonLethal(int i) {
		hps.setNonLethal(i);
	}

	public int getHPs() {
		return hps.getHPs();
	}

//------------------- Armor Class -------------------
// AC has a total value, a touch value and a flat footed value
// it is made up of a number of different components
	// TODO dex modifier should be filtered by armor/sheild max dex bonus
	public void setACComponent(int type, int value) {
		if (type == AC.AC_DEX) return;	// TODO temporary hack to filter out dex because it is added automatically
		if (acMods[type] != null) ac.removeModifier(acMods[type]);
		acMods[type] = new ImmutableModifier(value,AC.getACComponentName(type));
		ac.addModifier(acMods[type]);
	}

	public int getACComponent(int type) {
		return ac.getModifiersTotal(AC.getACComponentName(type));
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
		return ac.getValue();
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
		return ac.getFlatFootedAC().getValue();
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
		return ac.getTouchAC().getValue();
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
		return XP.getXPRequired(level.getLevel()+1);
	}

	/*public String getXPHistory() {
		StringBuilder b = new StringBuilder();
		int total = 0;
		for (XPHistoryItem item : xpChanges) {
			XP.XPChange change = item.xpChange;
			b.append(total);
			b.append("\t");
			b.append(change.getXP());
			b.append("\t");
			b.append(change);
			b.append(System.getProperty("line.separator"));
			total += change.getXP();
		}
		return b.toString();
	}*/
	
	public void addXPChallenges(int count, int penalty, Collection<Challenge> challenges, String comment, Date d) {
		XP.XPChangeChallenges change = new XP.XPChangeChallenges(comment, d);
		change.level = level.getLevel();
		change.partyCount = count;
		change.penalty = penalty;
		change.xp = XP.getXP(level.getLevel(), count, penalty, challenges);
		change.challenges.addAll(challenges);
		addXPChange(change);
		int old = xp;
		xp += change.xp;
        pcs.firePropertyChange(PROPERTY_XP, old, xp);
	}

	public void addXPAdhocChange(int delta, String comment, Date d) {
		addXPChange(new XP.XPChangeAdhoc(delta, comment, d));
		int old = xp;
		xp += delta;
        pcs.firePropertyChange(PROPERTY_XP, old, xp);
	}

	public int getLevel() {
		return level.getLevel();
	}

	public void setLevel(int l, String comment, Date d) {
		if (level.getLevel() == l) return;
		addXPChange(new XP.XPChangeLevel(level.getLevel(), l, comment, d));
		int old = level.getLevel();
		level.setLevel(l);
		pcs.firePropertyChange(PROPERTY_LEVEL, old, level.getLevel());
	}

//------------------- XP History ------------------
	protected void addXPChange(XP.XPChange change) {
		XPHistoryItem item = new XPHistoryItem();
		item.index = xpChanges.size();
		item.xpChange = change;
		item.total = change.getXP();
		if (item.index > 0) {
			item.total += xpChanges.get(item.index-1).total;
		}
		xpChanges.add(item);
	}

	public int getXPHistoryCount() {
		return xpChanges.size();
	}

	public XPHistoryItem getXPHistory(int index) {
		return xpChanges.get(index);
	}

	public void deleteXPHistory(int index) {
		XPHistoryItem removed = xpChanges.remove(index);
		for (int i = index; i < xpChanges.size(); i++) {
			XPHistoryItem item = xpChanges.get(i);
			item.total -= removed.xpChange.getXP();
			item.index--;
		}
		int old = xp;
		if (xpChanges.size() > 0) {
			xp = xpChanges.get(xpChanges.size()-1).total;
		} else {
			xp = 0;
		}
        pcs.firePropertyChange(PROPERTY_XP, old, xp);
	}

	public void moveXPHistory(int from, int to) {
		XPHistoryItem removed = xpChanges.remove(from);
		xpChanges.add(to, removed);
		int low = Math.min(from, to);
		int high = Math.max(from, to);
		int total = 0;
		if (low > 0) total = xpChanges.get(low-1).total;
		for (int i = low; i <= high; i++) {
			XPHistoryItem item = xpChanges.get(i);
			total = total + item.xpChange.getXP();
			item.total = total;
			item.index = i;
		}
	}

//------------------- Import/Export and other methods -------------------
	public Statistic getStatistic(String name) {
		if (name.equals(STATISTIC_STRENGTH)) {
			return abilities[AbilityScore.ABILITY_STRENGTH];
		} else if (name.equals(STATISTIC_INTELLIGENCE)) {
			return abilities[AbilityScore.ABILITY_INTELLIGENCE];
		} else if (name.equals(STATISTIC_WISDOM)) {
			return abilities[AbilityScore.ABILITY_WISDOM];
		} else if (name.equals(STATISTIC_DEXTERITY)) {
			return abilities[AbilityScore.ABILITY_DEXTERITY];
		} else if (name.equals(STATISTIC_CONSTITUTION)) {
			return abilities[AbilityScore.ABILITY_CONSTITUTION];
		} else if (name.equals(STATISTIC_CHARISMA)) {
			return abilities[AbilityScore.ABILITY_CHARISMA];
		} else if (name.equals(STATISTIC_FORTITUDE_SAVE)) {
			return saves[SavingThrow.SAVE_FORTITUDE];
		} else if (name.equals(STATISTIC_WILL_SAVE)) {
			return saves[SavingThrow.SAVE_WILL];
		} else if (name.equals(STATISTIC_REFLEX_SAVE)) {
			return saves[SavingThrow.SAVE_REFLEX];
		} else if (name.equals(STATISTIC_AC)) {
			return ac;
		} else if (name.equals(STATISTIC_INITIATIVE)) {
			return initiative;
		} else if (name.equals(STATISTIC_SKILLS)) {
			return skills;
		} else if (name.equals(STATISTIC_SAVING_THROWS)) {
			// TODO implement?
			return null;
		} else if (name.equals(STATISTIC_LEVEL)) {
			return level;
		} else if (name.equals(STATISTIC_HPS)) {
			return hps;
		} else if (name.equals(STATISTIC_ATTACKS)) {
			return attacks;
		} else {
			System.out.println("Unknown statistic "+name);
			return null;
		}
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
					if (value != null) c.initiative.setBaseValue(Integer.parseInt(value));

				} else if (tag.equals("Level")) {
					c.level.setLevel(Integer.parseInt(e.getAttribute("level")));
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
							if (change != null) c.addXPChange(change);
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
							String temp = s.getAttribute("temp");
							for (int k=0; k<6; k++) {
								if (AbilityScore.getAbilityName(k).equals(type)) {
									c.abilities[k].setBaseValue(Integer.parseInt(value));
									if (temp != "") c.abilities[k].setOverride(Integer.parseInt(temp));
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
							for (int k=0; k<3; k++) {
								if (SavingThrow.getSavingThrowName(k).equals(type)) {
									c.saves[k].setBaseValue(Integer.parseInt(value));
									if (misc != "") c.setSavingThrowMisc(k, Integer.parseInt(misc));
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
							SkillType skill = SkillType.getSkill(type);
							c.setSkillRanks(skill, Float.parseFloat(ranks));
							//c.skillRanks.put(skill,Float.parseFloat(ranks));
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
							// TODO hacks to change type:
							if (type.equals("Dex")) type = AC.getACComponentName(AC.AC_DEX);
							if (type.equals("Natural")) type = AC.getACComponentName(AC.AC_NATURAL);
							if (type.equals("Deflect")) type = AC.getACComponentName(AC.AC_DEFLECTION);
							if (!type.equals(AC.getACComponentName(AC.AC_DEX))) {	// TODO ignore dex component when loading
								for (int k=0; k<AC.AC_MAX_INDEX; k++) {
									if (AC.getACComponentName(k).equals(type)) {
										c.setACComponent(k, Integer.parseInt(value));
									}
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
		for (XPHistoryItem i : xpChanges) {
			XP.XPChange cc = i.xpChange;
			b.append(cc.getXML(i3, nextIndent));
		}
		b.append(i2).append("</Level>").append(nl);
		b.append(i2).append("<AbilityScores>").append(nl);
		for (int i=0; i<6; i++) {
			b.append(i3).append("<AbilityScore type=\"").append(AbilityScore.getAbilityName(i));
			b.append("\" value=\"").append(getBaseAbilityScore(i));
			if (getAbilityScore(i) != getBaseAbilityScore(i)) {
				b.append("\" temp=\"").append(getAbilityScore(i));
			}
			b.append("\"/>").append(nl);
		}
		b.append(i2).append("</AbilityScores>").append(nl);
		b.append(i2).append("<HitPoints maximum=\"").append(getMaximumHitPoints()).append("\"");
		if (getWounds() != 0) b.append(" wounds=\"").append(getWounds()).append("\"");
		if (getNonLethal() != 0) b.append(" non-lethal=\"").append(getNonLethal()).append("\"");
		b.append("/>").append(nl);
		b.append(i2).append("<Initiative value=\"").append(getBaseInitiative()).append("\"/>").append(nl);
		b.append(i2).append("<SavingThrows>").append(nl);
		for (int i=0; i<3; i++) {
			b.append(i3).append("<Save type=\"").append(SavingThrow.getSavingThrowName(i));
			b.append("\" base=\"").append(getSavingThrowBase(i));
			if (getSavingThrowMisc(i) != 0) b.append("\" misc=\"").append(getSavingThrowMisc(i));
			b.append("\"/>").append(nl);
		}
		b.append(i2).append("</SavingThrows>").append(nl);
		b.append(i2).append("<Skills>").append(nl);
		Set<SkillType> set = skills.getTrainedSkills();
		set.addAll(skillMisc.keySet());
		for (SkillType s : set) {
			if (getSkillRanks(s) != 0 || getSkillMisc(s) != 0) {
				b.append(i3).append("<Skill type=\"").append(s);
				b.append("\" ranks=\"").append(getSkillRanks(s));
				if (getSkillMisc(s) != 0) b.append("\" misc=\"").append(getSkillMisc(s));
				b.append("\"/>").append(nl);
			}
		}
		b.append(i2).append("</Skills>").append(nl);
		b.append(i2).append("<AC>").append(nl);
		for (int i=0; i<AC.AC_MAX_INDEX; i++) {
			if (getACComponent(i) != 0) {
				b.append(i3).append("<ACComponent type=\"").append(AC.getACComponentName(i));
				b.append("\" value=\"").append(getACComponent(i)).append("\"/>").append(nl);
			}
		}
		b.append(i2).append("</AC>").append(nl);
		b.append(indent).append("</Character>").append(nl);
		return b.toString();
	}

	// Prints the differences between this and inChar
	// XXX does not include level or xp
	// TODO add attacks and other new statistcs here
	public void showDiffs(Character inChar) {
		if (!name.equals(inChar.name)) {
			// new attribute
			System.out.println(PROPERTY_NAME+"|"+name+"|"+inChar.name);
		}
		if (hps.getMaximumHitPoints() != inChar.hps.getMaximumHitPoints()) {
			System.out.println(PROPERTY_MAXHPS+"|"+hps+"|"+inChar.hps);
		}
		if (hps.getWounds() != inChar.hps.getWounds()) {
			System.out.println(PROPERTY_WOUNDS+"|"+hps.getWounds()+"|"+inChar.hps.getWounds());
		}
		if (hps.getNonLethal() != inChar.hps.getNonLethal()) {
			System.out.println(PROPERTY_NONLETHAL+"|"+hps.getNonLethal()+"|"+inChar.hps.getNonLethal());
		}
		if (initiative.getBaseValue() != inChar.initiative.getBaseValue()) {
			System.out.println(PROPERTY_INITIATIVE+"|"+initiative.getBaseValue()+"|"+inChar.initiative.getBaseValue());
		}
		for (int i=0; i<6; i++) {
			if (abilities[i] != inChar.abilities[i]) {
				System.out.println(PROPERTY_ABILITY_PREFIX+AbilityScore.getAbilityName(i)+"|"+abilities[i]+"|"+inChar.abilities[i]);
			}
		}
		for (int i=0; i<6; i++) {
			if (abilities[i].getOverride() != inChar.abilities[i].getOverride()) {
				// new attribute
				System.out.println(PROPERTY_ABILITY_OVERRIDE_PREFIX+AbilityScore.getAbilityName(i)+"|"+abilities[i].getOverride()+"|"+inChar.abilities[i].getOverride());
			}
		}
		for (int i=0; i<3; i++) {
			if (saves[i] != inChar.saves[i]) {
				System.out.println(PROPERTY_SAVE_PREFIX+SavingThrow.getSavingThrowName(i)+"|"+saves[i]+"|"+inChar.saves[i]);
			}
		}
		for (int i=0; i<AC.AC_MAX_INDEX; i++) {
			if (getACComponent(i) != inChar.getACComponent(i)) {
				// new attribute
				System.out.println(PROPERTY_AC_COMPONENT_PREFIX+AC.getACComponentName(i)+"|"+getACComponent(i)+"|"+inChar.getACComponent(i));
			}
		}
		Set<SkillType> allSkills = skills.getTrainedSkills();
		allSkills.addAll(inChar.skills.getTrainedSkills());
		for (SkillType skill : allSkills) {
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
		if (hps.getMaximumHitPoints() != inChar.hps.getMaximumHitPoints()) diffs.add(PROPERTY_MAXHPS);
		if (hps.getWounds() != inChar.hps.getWounds()) diffs.add(PROPERTY_WOUNDS);
		if (hps.getNonLethal() != inChar.hps.getNonLethal()) diffs.add(PROPERTY_NONLETHAL);
		if (initiative.getBaseValue() != inChar.initiative.getBaseValue()) diffs.add(PROPERTY_INITIATIVE);
		//if (xp != inChar.xp) diffs.add(PROPERTY_XP);	// TODO not sure we should include this
		//if (level != inChar.level) diffs.add(PROPERTY_LEVEL);	// TODO not sure we should include this
		for (int i=0; i<6; i++) {
			if (abilities[i] != inChar.abilities[i]) {
				diffs.add(PROPERTY_ABILITY_PREFIX+AbilityScore.getAbilityName(i));
			}
		}
		for (int i=0; i<6; i++) {
			if (abilities[i].getOverride() != inChar.abilities[i].getOverride()) {
				diffs.add(PROPERTY_ABILITY_OVERRIDE_PREFIX+AbilityScore.getAbilityName(i));
			}
		}
		for (int i=0; i<3; i++) {
			if (saves[i] != inChar.saves[i]) {
				diffs.add(PROPERTY_SAVE_PREFIX+SavingThrow.getSavingThrowName(i));
			}
			if (saveMisc[i] != inChar.saveMisc[i]) {
				diffs.add(PROPERTY_SAVE_MISC_PREFIX+SavingThrow.getSavingThrowName(i));
			}
		}
		for (int i=0; i<AC.AC_MAX_INDEX; i++) {
			if (getACComponent(i) != inChar.getACComponent(i)) {
				diffs.add(PROPERTY_AC_COMPONENT_PREFIX+AC.getACComponentName(i));
			}
		}
		Set<SkillType> allSkills = skills.getTrainedSkills();
		allSkills.addAll(inChar.skills.getTrainedSkills());
		List<SkillType> skillList = new ArrayList<SkillType>(allSkills);
		Collections.sort(skillList);
		for (SkillType skill : skillList) {
			if (getSkillRanks(skill) != inChar.getSkillRanks(skill)) {
				diffs.add(PROPERTY_SKILL_PREFIX+skill);
			}
			if (getSkillMisc(skill) != inChar.getSkillMisc(skill)) {
				diffs.add(PROPERTY_SKILL_MISC_PREFIX+skill);
			}
		}
		return diffs;
	}

	// TODO not sure this is right for ability scores. probably needs reimplementing now
	public Object getProperty(String prop) {
		if (prop.equals(PROPERTY_NAME)) return name;
		if (prop.equals(PROPERTY_MAXHPS)) return hps.getMaximumHitPoints();
		if (prop.equals(PROPERTY_WOUNDS)) return hps.getWounds();
		if (prop.equals(PROPERTY_NONLETHAL)) return hps.getNonLethal();
		if (prop.equals(PROPERTY_INITIATIVE)) return initiative.getBaseValue();
		if (prop.equals(PROPERTY_LEVEL)) return level.getLevel();
		if (prop.equals(PROPERTY_XP)) return xp;
		if (prop.equals(PROPERTY_BAB)) return attacks.getBAB();
		
		if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(AbilityScore.getAbilityName(i))) return abilities[i].getValue();
			}
		}

		if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(AbilityScore.getAbilityName(i))) return abilities[i].getOverride();
			}
		}

		if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(SavingThrow.getSavingThrowName(i))) return saves[i];
			}
		}

		if (prop.startsWith(PROPERTY_SAVE_MISC_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_MISC_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(SavingThrow.getSavingThrowName(i))) return saveMisc[i];
			}
		}

		if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (int i=0; i<AC.AC_MAX_INDEX; i++) {
				if (comp.equals(AC.getACComponentName(i))) return getACComponent(i);
			}
		}

		if (prop.startsWith(PROPERTY_SKILL_PREFIX)) {
			Float ranks = 0.0f;
			String skill = prop.substring(PROPERTY_SKILL_PREFIX.length());
			SkillType s = SkillType.getSkill(skill);
			if (s != null) ranks = skills.getRanks(s);
			return ranks;
		}

		if (prop.startsWith(PROPERTY_SKILL_MISC_PREFIX)) {
			String skill = prop.substring(PROPERTY_SKILL_MISC_PREFIX.length());
			SkillType s = SkillType.getSkill(skill);
			Modifier m = skillMisc.get(s);
			if (m != null) return m.getModifier();
			return 0;
		}

		return null;
	}

	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_NAME)) setName((String)value);
		if (prop.equals(PROPERTY_MAXHPS)) setMaximumHitPoints((Integer)value);
		if (prop.equals(PROPERTY_WOUNDS)) setWounds((Integer)value);
		if (prop.equals(PROPERTY_NONLETHAL)) setNonLethal((Integer)value);
		if (prop.equals(PROPERTY_INITIATIVE)) setInitiativeModifier((Integer)value);
		if (prop.equals(PROPERTY_LEVEL)) setLevel((Integer)value,null,null);
		//if (prop.equals(PROPERTY_XP)) setXP((Integer)value);	// TODO should this be permitted as an adhoc change?
		if (prop.equals(PROPERTY_BAB)) attacks.setBAB((Integer)value);

		if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(AbilityScore.getAbilityName(i))) setAbilityScore(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(AbilityScore.getAbilityName(i))) setTemporaryAbility(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(SavingThrow.getSavingThrowName(i))) setSavingThrowBase(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_SAVE_MISC_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_MISC_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(SavingThrow.getSavingThrowName(i))) setSavingThrowMisc(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (int i=0; i<AC.AC_MAX_INDEX; i++) {
				if (comp.equals(AC.getACComponentName(i))) setACComponent(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_SKILL_PREFIX)) {
			String skillName = prop.substring(PROPERTY_SKILL_PREFIX.length());
			SkillType skill = SkillType.getSkill(skillName);
			if (value == null) {
				setSkillRanks(skill, 0);
			} else {
				setSkillRanks(skill, (Float)value);
			}
		}

		if (prop.startsWith(PROPERTY_SKILL_MISC_PREFIX)) {
			String skillName = prop.substring(PROPERTY_SKILL_MISC_PREFIX.length());
			SkillType skill = SkillType.getSkill(skillName);
			setSkillMisc(skill, (Integer)value);
		}
	}
}

package party;
import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Buff;
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
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import ui.CharacterBuffsPanel.BuffListModel;
import util.Updater;

// TODO priorities:
// ability checks
// enum conversions - property and statistics types
// feats - selecting of feats with target skill/weapon/spells/school. change available list to remove already selected feats
// haste extra attack should be shown
// clean up handling of HPs, wounds, healing etc, particularly ui
// review Statistics vs Properties
// ui for adding adhoc modifiers
// size
// equipment, particularly magic item slots, armor, weapons

// TODO change 'value' attributes in xml. these should either be 'base' or 'total' attributes (support 'value' as 'base' for loading only). also fix differences in ac
// TODO rework ac ui: allow multiple bonuses of each type?, AC temp scores? add armor
// TODO convert ui classes that listen to Character to listen to the specific Statistics instead - could do a StatisticsProxy class
// that could be used as a base for statistics that rely on a common set of modifiers such as touch AC, skills etc
// TODO need to review how properties work on Character and BoundIntegerField
// TODO ultimately would like a live DOM. the DOM saved to the party XML file would be a filtered version

/* Things to implement:
 *  (in progress) Feats
 *  (in progress) Grapple modifier
 *  Ability score checks
 *  Class levels
 *  Spell lists / spells per day
 *  Damage reduction
 *  Spell resistance
 *  Magic items slots
 *  Weight/encumberance
 *  Skill synergies
 *  Skill named versions (Crafting, Profession etc)
 *  Size
 *  Speed
 */

/**
 * @author Steve
 *
 */
public class Character extends Creature {
	// TODO the armor component override may go away
	public enum ACComponentType {
		//ARMOR("Armor"),
		//SHIELD("Shield"),
		NATURAL("Natural Armor"),
		//DEX("Dexterity"),
		SIZE("Size"),
		DEFLECTION("Deflection"),
		DODGE("Dodge"),
		OTHER("Misc");
		
		public String toString() {return description;}

		private ACComponentType(String d) {description = d;}

		private final String description;
	}

	protected String name;
	protected String classDescription;
	protected String player;
	protected String region;
	protected String race;
	protected String gender;
	protected String alignment;
	protected String deity;
	protected String size;
	protected String type;
	protected String age;
	protected String height;
	protected String weight;
	protected String eyeColour;
	protected String hairColour;
	protected String speed;
	protected String damageReduction;
	protected String spellResistance;
	protected String arcaneSpellFailure;
	protected String actionPoints;

	protected InitiativeModifier initiative;

	protected EnumMap<SavingThrow.Type,SavingThrow> saves = new EnumMap<SavingThrow.Type,SavingThrow>(SavingThrow.Type.class);
	protected EnumMap<SavingThrow.Type,Modifier> saveMisc = new EnumMap<SavingThrow.Type,Modifier>(SavingThrow.Type.class);
	
	protected EnumMap<AbilityScore.Type,AbilityScore> abilities = new EnumMap<AbilityScore.Type,AbilityScore>(AbilityScore.Type.class);

	protected Skills skills;

//	protected Set<Feat> feats = new HashSet<Feat>();

	protected HPs hps;
	protected Level level; 
	protected int xp = 0;

	protected AC ac;
	protected EnumMap<ACComponentType,Modifier> acMods = new EnumMap<ACComponentType,Modifier>(ACComponentType.class); // TODO should move to AC panel
	protected int tempAC, tempTouch, tempFF;	// ac overrides
	protected boolean hasTempAC, hasTempTouch, hasTempFF;	// flags for overrides

	protected Attacks attacks;

	public BuffListModel buffs = new BuffListModel();	// TODO reimplement for better encapsulation
	public BuffListModel feats = new BuffListModel();	// TODO reimplement for better encapsulation

	protected List<XPHistoryItem> xpChanges = new ArrayList<XPHistoryItem>();

	protected boolean autoSave = false;

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
				firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				return;
			}

			// only care about "value" updates from other statistics except skills
			// TODO not sure this is what we want to do
			if (!evt.getPropertyName().equals("value")) {
				if (!(evt.getSource() instanceof Skills)) {
					System.out.println("Ignoring update to "+evt.getPropertyName()+" on "+((Statistic)evt.getSource()).getName());
					return;
				}
			}

			if (evt.getSource() == initiative) {
				//System.out.println("Inititative change event: "+getName()+" old = "+evt.getOldValue()+", new = "+evt.getNewValue());
				firePropertyChange(PROPERTY_INITIATIVE, evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() instanceof AbilityScore) {
				AbilityScore score = (AbilityScore)evt.getSource();
		        firePropertyChange(PROPERTY_ABILITY_PREFIX+score.getName(), evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() instanceof Skill) {
				Skill s = (Skill)evt.getSource();
				firePropertyChange(PROPERTY_SKILL_PREFIX+s.getName(), evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() instanceof Skills) {
				if (evt.getPropertyName().equals("value")) {
					// change to global modifiers or ability modifiers - update all skills
					for (SkillType s : getSkills()) {
						firePropertyChange(PROPERTY_SKILL_PREFIX+s.getName(), null, null);
					}
				} else {
					firePropertyChange(PROPERTY_SKILL_PREFIX+evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				}

			} else if (evt.getSource() instanceof SavingThrow) {
				SavingThrow save = (SavingThrow)evt.getSource();
				firePropertyChange(PROPERTY_SAVE_PREFIX+save.getName(), evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() == ac) {
				firePropertyChange(PROPERTY_AC, evt.getOldValue(), evt.getNewValue());

			} else if (evt.getSource() == attacks) {
				firePropertyChange(PROPERTY_BAB, evt.getOldValue(), evt.getNewValue());

			} else {
				System.out.println("Unknown property change event: "+evt);
			}
		}
	};

	public Character(String n) {
		name = n;
		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			final AbilityScore s = new AbilityScore(t);
			s.addPropertyChangeListener(statListener);
			s.getModifier().addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					//System.out.println(PROPERTY_ABILITY_PREFIX+s.getName()+": "+e.getOldValue()+" -> "+ e.getNewValue());
			        firePropertyChange(PROPERTY_ABILITY_PREFIX+s.getName(), e.getOldValue(), e.getNewValue());
				}
			});
			abilities.put(t, s);
		}

		initiative = new InitiativeModifier(abilities.get(AbilityScore.Type.DEXTERITY));
		initiative.addPropertyChangeListener(statListener);

		for (SavingThrow.Type t : SavingThrow.Type.values()) {
			SavingThrow s = new SavingThrow(t, abilities.get(t.getAbilityType()));
			s.addPropertyChangeListener(statListener);
			saves.put(t, s);
		}

		ac = new AC(abilities.get(AbilityScore.Type.DEXTERITY));
		ac.addPropertyChangeListener(statListener);

		skills = new Skills(abilities.values(), ac.getArmorCheckPenalty());
		skills.addPropertyChangeListener(statListener);

		level = new Level();

		hps = new HPs(abilities.get(AbilityScore.Type.CONSTITUTION), level);
		hps.addPropertyChangeListener(statListener);

		attacks = new Attacks(abilities.get(AbilityScore.Type.STRENGTH), abilities.get(AbilityScore.Type.DEXTERITY), this);
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

	public void firePropertyChange(String property, Object oldValue, Object newValue) {
		if (autoSave) {
			// if there has been no change we don't trigger an update (pcs.firePropertyChange() also implements this logic)
			if (oldValue == null) {
				if (newValue == null) return;
			} else {
				if (oldValue.equals(newValue)) return;
			}
			System.out.println("Autosave "+name+" triggered by property change on "+property);
			saveCharacterSheet();
		}

		pcs.firePropertyChange(property, oldValue, newValue);
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
	public int getAbilityScore(AbilityScore.Type type) {
		return abilities.get(type).getValue();
	}

   /**
    * Returns the base score of the specified ability. 
    * 
    * @param type  the ability score to get: one of the ABILITY constants from {@link Creature}
    * @return      the base score of the specified ability
    */
	public int getBaseAbilityScore(AbilityScore.Type type) {
		return abilities.get(type).getBaseValue();
	}

   /**
    * Returns the modifier calculated from the specified ability's current score.
    * 
    * @param type  the ability score to get the modifier of: one of the ABILITY constants from {@link Creature}
    * @return      the modifier calculated from the current score of the specified ability
    */
	public int getAbilityModifier(AbilityScore.Type type) {
		return abilities.get(type).getModifierValue();
	}

   /**
    * Sets the base score of the specified ability
    * 
    * @param type  the ability score to set: one of the ABILITY constants from {@link Creature}
    * @param value the value to set the score to
    */
	public void setAbilityScore(AbilityScore.Type type, int value) {
		abilities.get(type).setBaseValue(value);
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
	public void setTemporaryAbility(AbilityScore.Type type, int value) {
		if (value != abilities.get(type).getBaseValue() && value >= 0) {
			abilities.get(type).setOverride(value);
		} else {
			abilities.get(type).clearOverride();
		}
	}

	public int getTemporaryAbility(AbilityScore.Type type) {
		return abilities.get(type).getOverride();
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
		return skills.getMisc(s);
	}

	public void setSkillRanks(SkillType s, float ranks) {
		skills.setRanks(s, ranks);
	}

	public void setSkillMisc(SkillType s, int misc) {
		skills.setMisc(s, misc);
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

	public int getSavingThrow(SavingThrow.Type save) {
		return saves.get(save).getValue();
	}

	public int getSavingThrowBase(SavingThrow.Type save) {
		return saves.get(save).getBaseValue();
	}

	public int getSavingThrowMisc(SavingThrow.Type save) {
		if (saveMisc.containsKey(save)) return saveMisc.get(save).getModifier();
		else return 0;
	}

	public void setSavingThrowBase(SavingThrow.Type save, int v) {
		int old = saves.get(save).getValue();
		saves.get(save).setBaseValue(v);
		firePropertyChange(PROPERTY_SAVE_PREFIX+save.toString(), old, saves.get(save).getValue());
	}

	// TODO currently this works by removing the old modifier and adding a new one. could make the modifiers mutable instead
	public void setSavingThrowMisc(SavingThrow.Type save, int misc) {
		int old = getSavingThrow(save);
		if (saveMisc.containsKey(save)) saves.get(save).removeModifier(saveMisc.get(save));
		saveMisc.put(save, new ImmutableModifier(misc));
		saves.get(save).addModifier(saveMisc.get(save));
		int now = getSavingThrow(save);
		if (old != now) {
			firePropertyChange(PROPERTY_SAVE_PREFIX+save.toString(), old, now);
		}
	}

   /**
    * Sets the saving throw total by modifying the base value.
    * 
    * @param save   the saving throw to set
    * @param total  the total required
    */
	// TODO this is used by RollsPanel. it should probably be removed
	public void setSavingThrow(SavingThrow.Type save, int total) {
		int old = getSavingThrow(save);
		saves.get(save).setBaseValue(total-saves.get(save).getModifiersTotal());
		int now = getSavingThrow(save);
		firePropertyChange(PROPERTY_SAVE_PREFIX+save.toString(), old, now);
	}

//------------------- Hit Points -------------------
// Hit points have a maximum value, wounds taken, non-lethal taken and a calculated
// current value

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
	public void setACComponent(ACComponentType type, int value) {
		//if (type == ACComponentType.DEX) return;	// TODO temporary hack to filter out dex because it is added automatically
		if (acMods.containsKey(type)) {
			ac.removeModifier(acMods.get(type));
			acMods.remove(type);
		}
		if (value != 0) {
			acMods.put(type, new ImmutableModifier(value,type.toString(),"user set"));
			ac.addModifier(acMods.get(type));
		}
	}

	// value of the user-set modifier, or 0 if none has been set
	public int getACComponent(ACComponentType type) {
		if (acMods.containsKey(type)) return acMods.get(type).getModifier();
		return 0;
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
    * the temporary score (as will <code>clearTemporaryAC()</code>)
    * 
    * @param ac the score to set the full ac to
    */
	public void setAC(int tempac) {
		if (hasTempTouch) {
			int totAC = getAC(false);
			if (totAC == tempac) {
				hasTempAC = false;
				firePropertyChange(PROPERTY_AC, tempAC, totAC);
				return;
			}
		}
		int old = getAC();
		tempAC = tempac;
		hasTempAC = true;
		firePropertyChange(PROPERTY_AC, old, ac);
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
				firePropertyChange(PROPERTY_AC, tempTouch, totAC);
				return;
			}
		}
		int old = getTouchAC();
		tempTouch = tempac;
		hasTempTouch = true;
		firePropertyChange(PROPERTY_AC, old, ac);
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
				firePropertyChange(PROPERTY_AC, tempFF, totAC);
				return;
			}
		}
		int old = getFlatFootedAC();
		tempFF = tempac;
		hasTempFF = true;
		firePropertyChange(PROPERTY_AC, old, ac);
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
        firePropertyChange(PROPERTY_XP, old, xp);
	}

	public void addXPAdhocChange(int delta, String comment, Date d) {
		addXPChange(new XP.XPChangeAdhoc(delta, comment, d));
		int old = xp;
		xp += delta;
        firePropertyChange(PROPERTY_XP, old, xp);
	}

	public int getLevel() {
		return level.getLevel();
	}

	public void setLevel(int l, String comment, Date d) {
		if (level.getLevel() == l) return;
		addXPChange(new XP.XPChangeLevel(level.getLevel(), l, comment, d));
		int old = level.getLevel();
		level.setLevel(l);
		firePropertyChange(PROPERTY_LEVEL, old, level.getLevel());
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
        firePropertyChange(PROPERTY_XP, old, xp);
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

//------------ Feats -------------
	public boolean hasFeat(String name) {
		for (int i = 0; i < feats.size(); i++) {
			Buff f = (Buff)feats.get(i);
			if (f.name.equals(name)) return true;
		}
		return false;
	}

//------------ getters and setters for informational fields -------------
	public String getPlayer() {return player;}
	public String getRegion() {return region;}
	public String getRace() {return race;}
	public String getGender() {return gender;}
	public String getAlignment() {return alignment;}
	public String getDeity() {return deity;}
	public String getSize() {return size;}
	public String getType() {return type;}
	public String getAge() {return age;}
	public String getHeight() {return height;}
	public String getWeight() {return weight;}
	public String getEyeColour() {return eyeColour;}
	public String getHairColour() {return hairColour;}
	public String getSpeed() {return speed;}
	public String getDamageReduction() {return damageReduction;}
	public String getSpellResistance() {return spellResistance;}
	public String getArcaneSpellFailure() {return arcaneSpellFailure;}
	public String getActionPoints() {return actionPoints;}
	public String getClassDescription() {return classDescription;}

	public void setPlayer(String s) {
		String old = player;
		player = s;
		firePropertyChange(PROPERTY_PLAYER, old, s);
	}

	public void setRegion(String s) {
		String old = region;
		region = s;
		firePropertyChange(PROPERTY_REGION, old, s);
	}
	
	public void setRace(String s) {
		String old = race;
		race = s;
		firePropertyChange(PROPERTY_RACE, old, s);
	}
	
	public void setGender(String s) {
		String old = gender;
		gender = s;
		firePropertyChange(PROPERTY_GENDER, old, s);
	}
	
	public void setAlignment(String s) {
		String old = alignment;
		alignment = s;
		firePropertyChange(PROPERTY_ALIGNMENT, old, s);
	}
	
	public void setDeity(String s) {
		String old = deity;
		deity = s;
		firePropertyChange(PROPERTY_DEITY, old, s);
	}
	
	public void setSize(String s) {
		String old = size;
		size = s;
		firePropertyChange(PROPERTY_SIZE, old, s);
	}
	
	public void setType(String s) {
		String old = type;
		type = s;
		firePropertyChange(PROPERTY_TYPE, old, s);
	}
	
	public void setAge(String s) {
		String old = age;
		age = s;
		firePropertyChange(PROPERTY_AGE, old, s);
	}
	
	public void setHeight(String s) {
		String old = height;
		height = s;
		firePropertyChange(PROPERTY_HEIGHT, old, s);
	}

	public void setWeight(String s) {
		String old = weight;
		weight = s;
		firePropertyChange(PROPERTY_WEIGHT, old, s);
	}
	
	public void setEyeColour(String s) {
		String old = eyeColour;
		eyeColour = s;
		firePropertyChange(PROPERTY_EYE_COLOUR, old, s);
	}
	
	public void setHairColour(String s) {
		String old = hairColour;
		hairColour = s;
		firePropertyChange(PROPERTY_HAIR_COLOUR, old, s);
	}
	
	public void setSpeed(String s) {
		String old = speed;
		speed = s;
		firePropertyChange(PROPERTY_SPEED, old, s);
	}
	
	public void setDamageReduction(String s) {
		String old = damageReduction;
		damageReduction = s;
		firePropertyChange(PROPERTY_DAMAGE_REDUCTION, old, s);
	}
	
	public void setSpellResistance(String s) {
		String old = spellResistance;
		spellResistance = s;
		firePropertyChange(PROPERTY_SPELL_RESISTANCE, old, s);
	}
	
	public void setArcaneSpellFailure(String s) {
		String old = arcaneSpellFailure;
		arcaneSpellFailure = s;
		firePropertyChange(PROPERTY_ARCANE_SPELL_FAILURE, old, s);
	}

	public void setActionPoints(String s) {
		String old = actionPoints;
		actionPoints = s;
		firePropertyChange(PROPERTY_ACTION_POINTS, old, s);
	}

	public void setClassDescription(String s) {
		String old = classDescription;
		classDescription = s;
		firePropertyChange(PROPERTY_CLASS, old, s);
	}

//------------------- Import/Export and other methods -------------------
	public Statistic getStatistic(String name) {
		if (name.equals(STATISTIC_STRENGTH)) {
			return abilities.get(AbilityScore.Type.STRENGTH);
		} else if (name.equals(STATISTIC_INTELLIGENCE)) {
			return abilities.get(AbilityScore.Type.INTELLIGENCE);
		} else if (name.equals(STATISTIC_WISDOM)) {
			return abilities.get(AbilityScore.Type.WISDOM);
		} else if (name.equals(STATISTIC_DEXTERITY)) {
			return abilities.get(AbilityScore.Type.DEXTERITY);
		} else if (name.equals(STATISTIC_CONSTITUTION)) {
			return abilities.get(AbilityScore.Type.CONSTITUTION);
		} else if (name.equals(STATISTIC_CHARISMA)) {
			return abilities.get(AbilityScore.Type.CHARISMA);
		} else if (name.equals(STATISTIC_FORTITUDE_SAVE)) {
			return saves.get(SavingThrow.Type.FORTITUDE);
		} else if (name.equals(STATISTIC_WILL_SAVE)) {
			return saves.get(SavingThrow.Type.WILL);
		} else if (name.equals(STATISTIC_REFLEX_SAVE)) {
			return saves.get(SavingThrow.Type.REFLEX);
		} else if (name.equals(STATISTIC_AC)) {
			return ac;
		} else if (name.equals(STATISTIC_ARMOR)) {
			return ac.getArmor();
		} else if (name.equals(STATISTIC_SHIELD)) {
			return ac.getShield();
		} else if (name.equals(STATISTIC_NATURAL_ARMOR)) {
			return ac.getNaturalArmor();
		} else if (name.equals(STATISTIC_INITIATIVE)) {
			return initiative;
		} else if (name.equals(STATISTIC_SKILLS)) {
			return skills;
		} else if (name.startsWith(STATISTIC_SKILLS+".")) {
			SkillType type = SkillType.getSkill(name.substring(STATISTIC_SKILLS.length()+1));
			return skills.getSkill(type);
//		} else if (name.equals(STATISTIC_SAVING_THROWS)) {
//			// TODO implement?
//			return null;
		} else if (name.equals(STATISTIC_LEVEL)) {
			return level;
		} else if (name.equals(STATISTIC_HPS)) {
			return hps;
		} else if (name.equals(STATISTIC_ATTACKS)) {
			return attacks;
		} else if (name.equals(STATISTIC_DAMAGE)) {
			return attacks.getDamageStatistic();
		} else {
			System.out.println("Unknown statistic "+name);
			return null;
		}
	}

	public static Character parseDOM(Element el) {
		if (!el.getNodeName().equals("Character")) return null;
		Character c = new Character(el.getAttribute("name"));
		c.player = el.getAttribute("player");
		c.region = el.getAttribute("region");
		c.race = el.getAttribute("race");
		c.gender = el.getAttribute("gender");
		c.alignment = el.getAttribute("alignment");
		c.deity = el.getAttribute("deity");
		c.size = el.getAttribute("size");
		c.type = el.getAttribute("type");
		c.age = el.getAttribute("age");
		c.height = el.getAttribute("height");
		c.weight = el.getAttribute("weight");
		c.eyeColour = el.getAttribute("eye-colour");
		c.hairColour = el.getAttribute("hair-colour");
		c.speed = el.getAttribute("speed");
		c.damageReduction = el.getAttribute("damage-reduction");
		c.spellResistance = el.getAttribute("spell-resistance");
		c.arcaneSpellFailure = el.getAttribute("arcane-spell-failure");
		c.actionPoints = el.getAttribute("action-points");
		
		Element hpElement = null;		// need to process after ability scores to avoid issues with changing con
		Element attacksElement = null;	// need to process after feats so we don't reset any values selected for power attack or combat expertise

		NodeList nodes = el.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element)nodes.item(i);
			String tag = e.getTagName();

			if (tag.equals("HitPoints")) {
				// processing hitpoints is deferred as it relies on con being already set and buffs being already loaded
				hpElement = e;

			} else if (tag.equals("Initiative")) {
				c.initiative.parseDOM(e);
//				String value = e.getAttribute("value");
//				if (value != null) c.initiative.setBaseValue(Integer.parseInt(value));

			} else if (tag.equals("Level")) {
//				c.level.setLevel(Integer.parseInt(e.getAttribute("level")));
				c.level.parseDOM(e);
				c.xp = Integer.parseInt(e.getAttribute("xp"));
				NodeList awards = e.getChildNodes();
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
					

			} else if (tag.equals("AbilityScores")) {
				NodeList abilities = e.getChildNodes();
				for (int j=0; j<abilities.getLength(); j++) {
					if (!abilities.item(j).getNodeName().equals("AbilityScore")) continue;
					Element s = (Element)abilities.item(j);
					AbilityScore.Type type = AbilityScore.Type.getAbilityType(s.getAttribute("type"));
					c.abilities.get(type).parseDOM(s);
				}

			} else if (tag.equals("SavingThrows")) {
				NodeList saves = e.getChildNodes();
				for (int j=0; j<saves.getLength(); j++) {
					if (!saves.item(j).getNodeName().equals("Save")) continue;
					Element s = (Element)saves.item(j);
//						String value = s.getAttribute("base");
					SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(s.getAttribute("type"));
					String misc = s.getAttribute("misc");

					c.saves.get(type).parseDOM(s);
					if (misc != "" && !misc.equals("0")) {
						c.setSavingThrowMisc(type, Integer.parseInt(misc));
					}
				}

			} else if (tag.equals("Skills")) {
				c.skills.parseDOM(e);

			} else if (tag.equals("AC")) {
				c.ac.parseDOM(e);

				NodeList acs = e.getChildNodes();
				for (int j=0; j<acs.getLength(); j++) {
					if (!acs.item(j).getNodeName().equals("ACComponent")) continue;
					Element s = (Element)acs.item(j);
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
				for (int j=0; j<children.getLength(); j++) {
					if (children.item(j).getNodeName().equals("Feat")) {
						if (!children.item(j).getNodeName().equals("Feat")) continue;
						Buff b = Buff.parseDOM((Element)children.item(j));
						b.applyBuff(c);
						c.feats.addElement(b);
					}
				}

			} else if (tag.equals("Buffs")) {
				NodeList buffs = e.getChildNodes();
				for (int j=0; j<buffs.getLength(); j++) {
					if (!buffs.item(j).getNodeName().equals("Buff")) continue;
					Buff b = Buff.parseDOM((Element)buffs.item(j));
					b.applyBuff(c);
					c.buffs.addElement(b);
				}
			}
		}

		if (hpElement != null) {
			c.hps.parseDOM(hpElement);
		}

		if (attacksElement != null) {
			c.attacks.parseDOM(attacksElement);
		}

		if (c.name.equals("Cain Warforger")) {
			System.out.println("Enabling autosave on "+c.name);
			c.autoSave = true;	// TODO temp hack
		}
		return c;
	}

//	public String getXML() {
//		return getXML("","    ");
//	}
//
//	public String getXML(String indent, String nextIndent) {
//		StringBuilder b = new StringBuilder();
//		String nl = System.getProperty("line.separator");
//		String i2 = indent + nextIndent;
//		String i3 = i2 + nextIndent;
//		b.append(indent).append("<Character name=\"").append(getName()).append("\">").append(nl);
//		b.append(i2).append("<Level level=\"").append(getLevel()).append("\" xp=\"").append(getXP()).append("\">").append(nl);
//		for (XPHistoryItem i : xpChanges) {
//			XP.XPChange cc = i.xpChange;
//			b.append(cc.getXML(i3, nextIndent));
//		}
//		b.append(i2).append("</Level>").append(nl);
//		b.append(i2).append("<AbilityScores>").append(nl);
//		for (AbilityScore.Type t : abilities.keySet()) {
//			b.append(i3).append("<AbilityScore type=\"").append(t.toString());
//			b.append("\" value=\"").append(getBaseAbilityScore(t));
//			if (getAbilityScore(t) != getBaseAbilityScore(t)) {
//				b.append("\" temp=\"").append(getAbilityScore(t));
//			}
//			b.append("\"/>").append(nl);
//		}
//		b.append(i2).append("</AbilityScores>").append(nl);
//		b.append(i2).append("<HitPoints maximum=\"").append(getMaximumHitPoints()).append("\"");
//		if (getWounds() != 0) b.append(" wounds=\"").append(getWounds()).append("\"");
//		if (getNonLethal() != 0) b.append(" non-lethal=\"").append(getNonLethal()).append("\"");
//		b.append("/>").append(nl);
//		b.append(i2).append("<Initiative value=\"").append(getBaseInitiative()).append("\"/>").append(nl);
//		b.append(i2).append("<SavingThrows>").append(nl);
//		for (SavingThrow.Type t : saves.keySet()) {
//			b.append(i3).append("<Save type=\"").append(t.toString());
//			b.append("\" base=\"").append(getSavingThrowBase(t));
//			if (getSavingThrowMisc(t) != 0) b.append("\" misc=\"").append(getSavingThrowMisc(t));
//			b.append("\"/>").append(nl);
//		}
//		b.append(i2).append("</SavingThrows>").append(nl);
//		b.append(i2).append("<Skills>").append(nl);
//		ArrayList<SkillType> set = new ArrayList<SkillType>(skills.skills.keySet());
//		Collections.sort(set, new Comparator<SkillType>() {
//			public int compare(SkillType o1, SkillType o2) {
//				return o1.getName().compareTo(o2.getName());
//			}
//		});
//		for (SkillType s : set) {
//			if (getSkillRanks(s) != 0 || getSkillMisc(s) != 0) {
//				b.append(i3).append("<Skill type=\"").append(s);
//				b.append("\" ranks=\"").append(getSkillRanks(s));
//				if (getSkillMisc(s) != 0) b.append("\" misc=\"").append(getSkillMisc(s));
//				b.append("\"/>").append(nl);
//			}
//		}
//		b.append(i2).append("</Skills>").append(nl);
//		b.append(i2).append("<AC>").append(nl);
//		for (int i=0; i<AC.AC_MAX_INDEX; i++) {
//			if (getACComponent(i) != 0) {
//				b.append(i3).append("<ACComponent type=\"").append(AC.getACComponentName(i));
//				b.append("\" value=\"").append(getACComponent(i)).append("\"/>").append(nl);
//			}
//		}
//		b.append(i2).append("</AC>").append(nl);
//		b.append(indent).append("</Character>").append(nl);
//		return b.toString();
//	}

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
		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			if (abilities.get(t) != inChar.abilities.get(t)) {
				System.out.println(PROPERTY_ABILITY_PREFIX+t.toString()+"|"+abilities.get(t)+"|"+inChar.abilities.get(t));
			}
		}
		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			if (abilities.get(t).getOverride() != inChar.abilities.get(t).getOverride()) {
				// new attribute
				System.out.println(PROPERTY_ABILITY_OVERRIDE_PREFIX+t.toString()+"|"+abilities.get(t).getOverride()+"|"+inChar.abilities.get(t).getOverride());
			}
		}
		for (SavingThrow.Type t : SavingThrow.Type.values()) {
			if (saves.get(t) != inChar.saves.get(t)) {
				System.out.println(PROPERTY_SAVE_PREFIX+t.toString()+"|"+saves.get(t)+"|"+inChar.saves.get(t));
			}
		}
		for (ACComponentType t: ACComponentType.values()) {
			if (getACComponent(t) != inChar.getACComponent(t)) {
				// new attribute
				System.out.println(PROPERTY_AC_COMPONENT_PREFIX+t+"|"+getACComponent(t)+"|"+inChar.getACComponent(t));
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
		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			if (abilities.get(t) != inChar.abilities.get(t)) {
				diffs.add(PROPERTY_ABILITY_PREFIX+t.toString());
			}
		}
		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			if (abilities.get(t).getOverride() != inChar.abilities.get(t).getOverride()) {
				diffs.add(PROPERTY_ABILITY_OVERRIDE_PREFIX+t.toString());
			}
		}
		for (SavingThrow.Type t : SavingThrow.Type.values()) {
			if (saves.get(t) != inChar.saves.get(t)) {
				diffs.add(PROPERTY_SAVE_PREFIX+t.toString());
			}
			if (saveMisc.get(t) != inChar.saveMisc.get(t)) {
				diffs.add(PROPERTY_SAVE_MISC_PREFIX+t.toString());
			}
		}
		for (ACComponentType t: ACComponentType.values()) {
			if (getACComponent(t) != inChar.getACComponent(t)) {
				diffs.add(PROPERTY_AC_COMPONENT_PREFIX+t);
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
		if (prop.equals(PROPERTY_PLAYER)) return player;
		if (prop.equals(PROPERTY_CLASS)) return classDescription;
		if (prop.equals(PROPERTY_REGION)) return region;
		if (prop.equals(PROPERTY_RACE)) return race;
		if (prop.equals(PROPERTY_GENDER)) return gender;
		if (prop.equals(PROPERTY_ALIGNMENT)) return alignment;
		if (prop.equals(PROPERTY_DEITY)) return deity;
		if (prop.equals(PROPERTY_SIZE)) return size;
		if (prop.equals(PROPERTY_TYPE)) return type;
		if (prop.equals(PROPERTY_AGE)) return age;
		if (prop.equals(PROPERTY_HEIGHT)) return height;
		if (prop.equals(PROPERTY_WEIGHT)) return weight;
		if (prop.equals(PROPERTY_EYE_COLOUR)) return eyeColour;
		if (prop.equals(PROPERTY_HAIR_COLOUR)) return hairColour;
		if (prop.equals(PROPERTY_SPEED)) return speed;
		if (prop.equals(PROPERTY_DAMAGE_REDUCTION)) return damageReduction;
		if (prop.equals(PROPERTY_SPELL_RESISTANCE)) return spellResistance;
		if (prop.equals(PROPERTY_ARCANE_SPELL_FAILURE)) return arcaneSpellFailure;
		if (prop.equals(PROPERTY_ACTION_POINTS)) return actionPoints;

		if (prop.equals(PROPERTY_MAXHPS)) return hps.getMaximumHitPoints();
		if (prop.equals(PROPERTY_WOUNDS)) return hps.getWounds();
		if (prop.equals(PROPERTY_NONLETHAL)) return hps.getNonLethal();
		if (prop.equals(PROPERTY_INITIATIVE)) return initiative.getBaseValue();
		if (prop.equals(PROPERTY_LEVEL)) return level.getLevel();
		if (prop.equals(PROPERTY_XP)) return xp;
		if (prop.equals(PROPERTY_BAB)) return attacks.getBAB();
		
		if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			AbilityScore.Type type = AbilityScore.Type.getAbilityType(ability);
			if (type != null) return abilities.get(type).getValue();
		}

		if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			AbilityScore.Type type = AbilityScore.Type.getAbilityType(ability);
			if (type != null) return abilities.get(type).getOverride();
		}

		if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(save);
			if (type != null) return saves.get(type);
		}

		if (prop.startsWith(PROPERTY_SAVE_MISC_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_MISC_PREFIX.length());
			SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(save);
			if (type != null) return saveMisc.get(type);
		}

		if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			// TODO meaning of this has changed (from total of component type to user set value for component type)
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (ACComponentType t: ACComponentType.values()) {
				if (comp.equals(t.toString())) return getACComponent(t);
			}
		}

		if (prop.startsWith(PROPERTY_SKILL_PREFIX)) {
			String skill = prop.substring(PROPERTY_SKILL_PREFIX.length());
			SkillType s = SkillType.getSkill(skill);
			if (s != null) return skills.getRanks(s);
			return 0f;
		}

		if (prop.startsWith(PROPERTY_SKILL_MISC_PREFIX)) {
			String skill = prop.substring(PROPERTY_SKILL_MISC_PREFIX.length());
			SkillType s = SkillType.getSkill(skill);
			if (s != null) return skills.getMisc(s);
			return 0;
		}

		return null;
	}

	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_NAME)) setName((String)value);
		if (prop.equals(PROPERTY_PLAYER)) setPlayer((String)value);
		if (prop.equals(PROPERTY_CLASS)) setClassDescription((String)value);
		if (prop.equals(PROPERTY_REGION)) setRegion((String)value);
		if (prop.equals(PROPERTY_RACE)) setRace((String)value);
		if (prop.equals(PROPERTY_GENDER)) setGender((String)value);
		if (prop.equals(PROPERTY_ALIGNMENT)) setAlignment((String)value);
		if (prop.equals(PROPERTY_DEITY)) setDeity((String)value);
		if (prop.equals(PROPERTY_SIZE)) setSize((String)value);
		if (prop.equals(PROPERTY_TYPE)) setType((String)value);
		if (prop.equals(PROPERTY_AGE)) setAge((String)value);
		if (prop.equals(PROPERTY_HEIGHT)) setHeight((String)value);
		if (prop.equals(PROPERTY_WEIGHT)) setWeight((String)value);
		if (prop.equals(PROPERTY_EYE_COLOUR)) setEyeColour((String)value);
		if (prop.equals(PROPERTY_HAIR_COLOUR)) setHairColour((String)value);
		if (prop.equals(PROPERTY_SPEED)) setSpeed((String)value);
		if (prop.equals(PROPERTY_DAMAGE_REDUCTION)) setDamageReduction((String)value);
		if (prop.equals(PROPERTY_SPELL_RESISTANCE)) setSpellResistance((String)value);
		if (prop.equals(PROPERTY_ARCANE_SPELL_FAILURE)) setArcaneSpellFailure((String)value);
		if (prop.equals(PROPERTY_ACTION_POINTS)) setActionPoints((String)value);

		if (prop.equals(PROPERTY_MAXHPS)) setMaximumHitPoints((Integer)value);
		if (prop.equals(PROPERTY_WOUNDS)) setWounds((Integer)value);
		if (prop.equals(PROPERTY_NONLETHAL)) setNonLethal((Integer)value);
		if (prop.equals(PROPERTY_INITIATIVE)) setInitiativeModifier((Integer)value);
		if (prop.equals(PROPERTY_LEVEL)) setLevel((Integer)value,null,null);
		//if (prop.equals(PROPERTY_XP)) setXP((Integer)value);	// TODO should this be permitted as an adhoc change?
		if (prop.equals(PROPERTY_BAB)) attacks.setBAB((Integer)value);

		if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			AbilityScore.Type type = AbilityScore.Type.getAbilityType(ability);
			if (type != null) setAbilityScore(type, (Integer)value);
		}

		if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			AbilityScore.Type type = AbilityScore.Type.getAbilityType(ability);
			if (type != null) setTemporaryAbility(type, (Integer)value);
		}

		if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(save);
			if (type != null) setSavingThrowBase(type, (Integer)value);
		}

		if (prop.startsWith(PROPERTY_SAVE_MISC_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_MISC_PREFIX.length());
			SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(save);
			if (type != null) setSavingThrowMisc(type, (Integer)value);
		}

		if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (ACComponentType t: ACComponentType.values()) {
				if (comp.equals(t.toString())) setACComponent(t, (Integer)value);
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

	public void saveCharacterSheet() {
    	Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"CharacterSheetTemplate.xsl\"");
			doc.appendChild(pi);
	    	doc.appendChild(getCharacterSheet(doc));
	    	doc.setXmlStandalone(true);
	    	Updater.updateDocument(doc, name);
	    	System.out.println("Saved character sheet "+name);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getModifierString(int mod) {
		if (mod >= 0) return "+"+mod;
		return Integer.toString(mod);
	}

	protected Element getCharacterElement(Document doc) {
		Element e = doc.createElement("Character");
		e.setAttribute("name", name);
		if(player != null && player.length() > 0) e.setAttribute("player",player);
		if(region != null && region.length() > 0) e.setAttribute("region",region);
		if(race != null && race.length() > 0) e.setAttribute("race",race);
		if(gender != null && gender.length() > 0) e.setAttribute("gender",gender);
		if(alignment != null && alignment.length() > 0) e.setAttribute("alignment",alignment);
		if(deity != null && deity.length() > 0) e.setAttribute("deity",deity);
		if(size != null && size.length() > 0) e.setAttribute("size",size);
		if(type != null && type.length() > 0) e.setAttribute("type",type);
		if(age != null && age.length() > 0) e.setAttribute("age",age);
		if(height != null && height.length() > 0) e.setAttribute("height",height);
		if(weight != null && weight.length() > 0) e.setAttribute("weight",weight);
		if(eyeColour != null && eyeColour.length() > 0) e.setAttribute("eye-colour",eyeColour);
		if(hairColour != null && hairColour.length() > 0) e.setAttribute("hair-colour",hairColour);
		if(speed != null && speed.length() > 0) e.setAttribute("speed",speed);
		if(damageReduction != null && damageReduction.length() > 0) e.setAttribute("damage-reduction",damageReduction);
		if(spellResistance != null && spellResistance.length() > 0) e.setAttribute("spell-resistance",spellResistance);
		if(arcaneSpellFailure != null && arcaneSpellFailure.length() > 0) e.setAttribute("arcane-spell-failure",arcaneSpellFailure);
		if(actionPoints != null && actionPoints.length() > 0) e.setAttribute("action-points",actionPoints);
		return e;
	}

	protected static void setACComponent(Document doc, Element e, String type, int mod) { 
		if (mod != 0) {
			Element comp = doc.createElement("ACComponent");
			comp.setAttribute("type", type);
			comp.setAttribute("value", ""+getModifierString(mod));
			e.appendChild(comp);
		}
	}

	// TODO this should include all the same data as the regular save as well as additional stuff
	public Element getCharacterSheet(Document doc) {
		Element charEl = getCharacterElement(doc);

		Element e = level.getElement(doc);
		e.setAttribute("class", classDescription);
		charEl.appendChild(e);

		e = doc.createElement("AbilityScores");
		for (AbilityScore s : abilities.values()) {
			Element e1 = doc.createElement("AbilityScore");
			e1.setAttribute("type", s.getType().toString());
			e1.setAttribute("total", ""+s.getRegularValue());		// this is the base value plus modifiers to the ability score
			e1.setAttribute("modifier", getModifierString(AbilityScore.getModifier(s.getRegularValue())));

			if (s.getOverride() != -1) {
				e1.setAttribute("temp", ""+s.getOverride());
				e1.setAttribute("temp-modifier", getModifierString(AbilityScore.getModifier(s.getOverride())));
			}
			e.appendChild(e1);
		}
		charEl.appendChild(e);

		charEl.appendChild(hps.getElement(doc));

		e = doc.createElement("Initiative");
		e.setAttribute("total", getModifierString(initiative.getValue()));
		e.setAttribute("misc", getModifierString(initiative.getValue()-abilities.get(AbilityScore.Type.DEXTERITY).getModifierValue()));	// assumes only 1 dex modifier that will always apply
		charEl.appendChild(e);

        e = doc.createElement("SavingThrows");
        for (SavingThrow.Type t : saves.keySet()) {
        	SavingThrow s = saves.get(t);
			Element saveEl = doc.createElement("Save");
			saveEl.setAttribute("type", s.getName());
			saveEl.setAttribute("base", getModifierString(s.getBaseValue()));
			saveEl.setAttribute("total", getModifierString(s.getValue()));
			int temp = 0;
			if (saveMisc.get(t) != null) temp = saveMisc.get(t).getModifier();
			if (temp != 0) saveEl.setAttribute("misc", getModifierString(temp));	// the misc/temp modifier applied through the ui
			int misc = s.getValue() - s.getBaseValue() - abilities.get(t.getAbilityType()).getModifierValue() - temp;
			if (misc != 0) saveEl.setAttribute("mods", getModifierString(misc));	// mods is the total combined modifiers other than the misc/temp modifier and the ability modifier
			e.appendChild(saveEl);
		}
		charEl.appendChild(e);
		
		e = doc.createElement("Skills");
		ArrayList<SkillType> set = new ArrayList<SkillType>(getSkills());
		Collections.sort(set, new Comparator<SkillType>() {
			public int compare(SkillType o1, SkillType o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (SkillType s : set) {
			Element se = doc.createElement("Skill");
			se.setAttribute("type", s.getName());
			if (skills.getRanks(s) == (int)skills.getRanks(s)) {
				se.setAttribute("ranks", Integer.toString((int)skills.getRanks(s)));
			} else {
				se.setAttribute("ranks", ""+skills.getRanks(s));
			}
			// cross-class="true"
			se.setAttribute("untrained", Boolean.toString(!s.isTrainedOnly()));
			se.setAttribute("ability", s.getAbility().getAbbreviation());
			se.setAttribute("ability-modifier", ""+abilities.get(s.getAbility()).getModifierValue());
			se.setAttribute("total", getModifierString(skills.getValue(s)));
			if (skills.getMisc(s) != 0) se.setAttribute("misc", ""+skills.getMisc(s));
			e.appendChild(se);
		}
		charEl.appendChild(e);

		e = doc.createElement("AC");
		e.setAttribute("total", ""+ac.getValue());
		e.setAttribute("flat-footed", ""+ac.getFlatFootedAC().getValue());
		e.setAttribute("touch" ,""+ac.getTouchAC().getValue());
		e.setAttribute("armor-check-penalty",""+ac.getArmorCheckPenalty().getModifier());
		setACComponent(doc, e, ACComponentType.SIZE.toString(), ac.getModifiersTotal(ACComponentType.SIZE.toString()));
		setACComponent(doc, e, ACComponentType.NATURAL.toString(), ac.getNaturalArmor().getValue());
		setACComponent(doc, e, ACComponentType.DEFLECTION.toString(), ac.getModifiersTotal(ACComponentType.DEFLECTION.toString()));
		int value = ac.getModifiersTotal(ACComponentType.OTHER.toString());
		value += ac.getModifiersTotal(ACComponentType.DODGE.toString());
		setACComponent(doc, e, ACComponentType.OTHER.toString(), value);
		setACComponent(doc, e, "Dexterity", ac.getModifiersTotal("Dexterity"));
		setACComponent(doc, e, "Armor", ac.getArmor().getValue());
		setACComponent(doc, e, "Shield", ac.getShield().getValue());
		charEl.appendChild(e);

		e = attacks.getElement(doc);
		e.setAttribute("temp", "");					// TODO implement
		e.setAttribute("size-modifier", "+0");			// TODO implement
		Element e1 = doc.createElement("Attack");
		e1.setAttribute("type","Grapple");
		e1.setAttribute("total",getModifierString(attacks.getGrappleValue()));
		e1.setAttribute("misc","+0");				// TODO implement
		e.appendChild(e1);
		e1 = doc.createElement("Attack");
		e1.setAttribute("type","Melee");
		e1.setAttribute("total",getModifierString(attacks.getValue()));
		e1.setAttribute("misc","+0");				// TODO implement
		e1.setAttribute("temp-modifier", "");				// TODO implement
		e.appendChild(e1);
		e1 = doc.createElement("Attack");
		e1.setAttribute("type","Ranged");
		e1.setAttribute("total",getModifierString(attacks.getRangedValue()));
		e1.setAttribute("misc","+0");					// TODO implement
		e1.setAttribute("temp-modifier", "");				// TODO implement
		e.appendChild(e1);
		charEl.appendChild(e);

		e = doc.createElement("Feats");
		for (int i = 0; i < feats.getSize(); i++) {
			Buff b = (Buff)feats.get(i);
			e.appendChild(b.getElement(doc, "Feat"));
		}
		charEl.appendChild(e);

		// TODO move description out of the Modifier element created by Buff.getElement - should only apply to charactersheet output
		// TODO modifiers that are not active should be flagged
		e = doc.createElement("Buffs");
		for (int i = 0; i < buffs.getSize(); i++) {
			Buff b = (Buff)buffs.get(i);
			e.appendChild(b.getElement(doc));
		}
		charEl.appendChild(e);
		return charEl;
	}

	public Element getElement(Document doc) {
		Element charEl = getCharacterElement(doc);

		Element e = level.getElement(doc);
		e.setAttribute("xp", ""+xp);
		for (XPHistoryItem i : xpChanges) {
			XP.XPChange cc = i.xpChange;
			e.appendChild(cc.getElement(doc));
		}
		charEl.appendChild(e);
		e = doc.createElement("AbilityScores");
		for (AbilityScore s : abilities.values()) {
			e.appendChild(s.getElement(doc));
		}
		charEl.appendChild(e);
		charEl.appendChild(hps.getElement(doc));
		charEl.appendChild(initiative.getElement(doc));
		e = doc.createElement("SavingThrows");
		for (SavingThrow.Type t : saves.keySet()) {
			SavingThrow s = saves.get(t);
			Element saveEl = s.getElement(doc);
			if (saveMisc.containsKey(t)) {
				saveEl.setAttribute("misc", ""+saveMisc.get(t).getModifier());
			}
			e.appendChild(saveEl);
		}
		charEl.appendChild(e);
		charEl.appendChild(skills.getElement(doc));

		e = ac.getElement(doc);
		for (Modifier m: acMods.values()) {
			if (m.getModifier() != 0) {
				Element comp = doc.createElement("ACComponent");
				comp.setAttribute("type", m.getType());
				comp.setAttribute("value", ""+m.getModifier());
				e.appendChild(comp);
			}
		}
		charEl.appendChild(e);

		charEl.appendChild(attacks.getElement(doc));

		e = doc.createElement("Feats");
		for (int i = 0; i < feats.getSize(); i++) {
			Buff b = (Buff)feats.get(i);
			e.appendChild(b.getElement(doc, "Feat"));
		}
		charEl.appendChild(e);
		
		e = doc.createElement("Buffs");
		for (int i = 0; i < buffs.getSize(); i++) {
			Buff b = (Buff)buffs.get(i);
			e.appendChild(b.getElement(doc));
		}
		charEl.appendChild(e);
		return charEl;
	}
}

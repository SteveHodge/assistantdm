package party;
import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Buff;
import gamesystem.Creature;
import gamesystem.CreatureProcessor;
import gamesystem.HPs;
import gamesystem.ImmutableModifier;
import gamesystem.InitiativeModifier;
import gamesystem.Levels;
import gamesystem.Modifier;
import gamesystem.SavingThrow;
import gamesystem.Size;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ui.BuffUI;

/**
 * @author Steve
 *
 */
public class Character extends Creature {
	public final static String PROPERTY_LEVEL = "Level";

	public final static String PROPERTY_BUFFS = "Buffs";	// this is sent to listeners, it's not gettable or settable

	// informational string properties (these are simple values that can be set/retrieved)
	public static final String PROPERTY_PLAYER = "Player";
	//public final static String PROPERTY_CLASS = "Class";
	public final static String PROPERTY_CAMPAIGN = "Campaign";
	public final static String PROPERTY_REGION = "Region";
	public final static String PROPERTY_RACE = "Race";
	public final static String PROPERTY_GENDER = "Gender";
	public final static String PROPERTY_ALIGNMENT = "Alignment";
	public final static String PROPERTY_DEITY = "Deity";
	public final static String PROPERTY_TYPE = "Type";
	public final static String PROPERTY_AGE = "Age";
	public final static String PROPERTY_HEIGHT = "Height";
	public final static String PROPERTY_WEIGHT = "Weight";
	public final static String PROPERTY_EYE_COLOUR = "Eye Colour";
	public final static String PROPERTY_HAIR_COLOUR = "Hair Colour";
	public final static String PROPERTY_SPEED = "Speed";
	public final static String PROPERTY_DAMAGE_REDUCTION = "Damage Reduction";
	public final static String PROPERTY_SPELL_RESISTANCE = "Spell Resistance";
	public final static String PROPERTY_ARCANE_SPELL_FAILURE = "Arcane Spell Failure";
	public final static String PROPERTY_ACTION_POINTS = "Action Points";

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

		@Override
		public String toString() {return description;}

		private ACComponentType(String d) {description = d;}

		private final String description;
	}

	public EnumMap<SavingThrow.Type, Modifier> saveMisc = new EnumMap<>(SavingThrow.Type.class);	// TODO shouldn't be public - change when XMLOutputProcessor has character specific subclass

	public Skills skills;		// TODO shouldn't be public - change when XMLCreatureParser has character specific subclass

	//	private Set<Feat> feats = new HashSet<>();

	public Levels level;	// TODO shouldn't be public - change when XMLOutputProcessor has character specific subclass
	public int xp = 0;	// TODO shouldn't be public - change when XMLOutputProcessor has character specific subclass

	public EnumMap<ACComponentType, Modifier> acMods = new EnumMap<>(ACComponentType.class); // TODO should move to AC panel

	public List<CharacterAttackForm> attackForms = new ArrayList<>();

	public BuffUI.BuffListModel<Buff> feats = new BuffUI.BuffListModel<>();	// TODO reimplement for better encapsulation

	public List<XPHistoryItem> xpChanges = new ArrayList<>();	// TODO shouldn't be public - change when XMLOutputProcessor has character specific subclass

	public class XPHistoryItem {
		public XPChange xpChange;	// TODO shouldn't be public - change when XMLOutputProcessor has character specific subclass
		private int total;
		private int index;

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
				return new ArrayList<>();
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

	private PropertyChangeListener statListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == hps) {
				firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				return;

			} else if (evt.getSource() == size) {
				if (evt.getPropertyName().equals("value")) {
					firePropertyChange(PROPERTY_SIZE, evt.getOldValue(), evt.getNewValue());
				} else if (evt.getPropertyName().equals("space")) {
					firePropertyChange(PROPERTY_SPACE, evt.getOldValue(), evt.getNewValue());
				} else if (evt.getPropertyName().equals("reach")) {
					firePropertyChange(PROPERTY_REACH, evt.getOldValue(), evt.getNewValue());
				}
				return;
			}

			//System.out.println("Change to " + evt.getSource() + ", property = " + evt.getPropertyName());
			// only care about "value" updates from other statistics except skills
			// TODO not sure this is what we want to do
			// TODO need to handle extra_attacks at least
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
			s.getModifier().addPropertyChangeListener(e -> firePropertyChange(PROPERTY_ABILITY_PREFIX+s.getName(), e.getOldValue(), e.getNewValue()));
			abilities.put(t, s);
		}

		initiative = new InitiativeModifier(abilities.get(AbilityScore.Type.DEXTERITY));
		initiative.addPropertyChangeListener(statListener);

		level = new Levels();

		for (SavingThrow.Type t : SavingThrow.Type.values()) {
			SavingThrow s = new SavingThrow(t, abilities.get(t.getAbilityType()), level);
			s.addPropertyChangeListener(statListener);
			saves.put(t, s);
		}

		ac = new AC(abilities.get(AbilityScore.Type.DEXTERITY));
		ac.addPropertyChangeListener(statListener);

		skills = new Skills(abilities.values(), ac.getArmorCheckPenalty());
		skills.addPropertyChangeListener(statListener);

		hps = new HPs(abilities.get(AbilityScore.Type.CONSTITUTION), level);
		hps.addPropertyChangeListener(statListener);

		bab = level.new BAB();

		attacks = new Attacks(this);
		attacks.addPropertyChangeListener(statListener);

		size = new Size();
		size.addPropertyChangeListener(statListener);
	}

	@Override
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
	public int getAbilityModifierValue(AbilityScore.Type type) {
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

	@Override
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
		saves.get(save).setBaseOverride(v);
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
		saves.get(save).setBaseOverride(total-saves.get(save).getModifiersTotal());
		int now = getSavingThrow(save);
		firePropertyChange(PROPERTY_SAVE_PREFIX+save.toString(), old, now);
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
			acMods.put(type, new ImmutableModifier(value, type.toString(), "user set"));
			ac.addModifier(acMods.get(type));
		}
	}

// value of the user-set modifier, or 0 if none has been set
	public int getACComponent(ACComponentType type) {
		if (acMods.containsKey(type)) return acMods.get(type).getModifier();
		return 0;
	}
//
//	/**
//	 * Returns the temporary ac if there is one, otherwise calculates the total ac
//	 * from the ac components
//	 *
//	 * @return current total ac
//	 */
//	@Override
//	public int getAC() {
//		return getAC(true);
//	}
//
//	private int getAC(boolean allowTemp) {
//		if (allowTemp && hasTempAC) return tempAC;
//		return ac.getValue();
//	}
//
//	/**
//	 * Returns the temporary flat-footed ac if there is one, otherwise calculates the
//	 * flat-footed ac from the ac components with any positive dexterity modifier
//	 * ignored.
//	 *
//	 * @return current flat-footed ac
//	 */
//	@Override
//	public int getFlatFootedAC() {
//		return getFlatFootedAC(true);
//	}
//
//	private int getFlatFootedAC(boolean allowTemp) {
//		if (allowTemp && hasTempFF) return tempFF;
//		return ac.getFlatFootedAC().getValue();
//	}
//
//	/**
//	 * Returns the temporary touch ac if there is one, otherwise calculates the touch
//	 * ac from the ac components with all armor, shield and natural armor bonuses
//	 * ignored.
//	 *
//	 * @return current touch ac
//	 */
//	@Override
//	public int getTouchAC() {
//		return getTouchAC(true);
//	}
//
//	private int getTouchAC(boolean allowTemp) {
//		if (allowTemp && hasTempTouch) return tempTouch;
//		return ac.getTouchAC().getValue();
//	}
//
//	/**
//	 * Sets a temporary full ac score. Setting this to the normal value will remove
//	 * the temporary score (as will <code>clearTemporaryAC()</code>)
//	 *
//	 * @param ac the score to set the full ac to
//	 */
//	@Override
//	public void setAC(int tempac) {
//		if (hasTempTouch) {
//			int totAC = getAC(false);
//			if (totAC == tempac) {
//				hasTempAC = false;
//				firePropertyChange(PROPERTY_AC, tempAC, totAC);
//				return;
//			}
//		}
//		int old = getAC();
//		tempAC = tempac;
//		hasTempAC = true;
//		firePropertyChange(PROPERTY_AC, old, ac);
//	}
//
//	/**
//	 * Sets a temporary touch ac score. Setting this to the normal value will remove
//	 * the temporary score (as will <code>clearTemporaryTouchAC()</code>
//	 *
//	 * @param ac the score to set the touch ac to
//	 */
//	@Override
//	public void setTouchAC(int tempac) {
//		if (hasTempTouch) {
//			int totAC = getTouchAC(false);
//			if (totAC == tempac) {
//				hasTempTouch = false;
//				firePropertyChange(PROPERTY_AC, tempTouch, totAC);
//				return;
//			}
//		}
//		int old = getTouchAC();
//		tempTouch = tempac;
//		hasTempTouch = true;
//		firePropertyChange(PROPERTY_AC, old, ac);
//	}
//
//	/**
//	 * Sets a temporary flat-footed ac score. Setting this to the normal value will
//	 * remove the temporary score (as will <code>clearTemporaryFlatFootedAC()</code>
//	 *
//	 * @param ac the score to set the flat-footed ac to
//	 */
//	@Override
//	public void setFlatFootedAC(int tempac) {
//		if (hasTempFF) {
//			int totAC = getFlatFootedAC(false);
//			if (totAC == tempac) {
//				hasTempFF = false;
//				firePropertyChange(PROPERTY_AC, tempFF, totAC);
//				return;
//			}
//		}
//		int old = getFlatFootedAC();
//		tempFF = tempac;
//		hasTempFF = true;
//		firePropertyChange(PROPERTY_AC, old, ac);
//	}

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
	public void addXPChange(XP.XPChange change) {	// TODO should not be public
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
	@Override
	public boolean hasFeat(String name) {
		for (int i = 0; i < feats.size(); i++) {
			Buff f = feats.get(i);
			if (f.name.equals(name)) return true;
		}
		return false;
	}

//------------ Buffs -------------
// XXX this is a hack to enable autosave on buff changes

	@Override
	public void addBuff(Buff b) {
		super.addBuff(b);
		firePropertyChange(PROPERTY_BUFFS, null, 1);
	}

	@Override
	public void removeBuff(Buff b) {
		super.removeBuff(b);
		firePropertyChange(PROPERTY_BUFFS, null, 1);
	}

	@Override
	public void removeBuff(int id) {
		super.removeBuff(id);
		firePropertyChange(PROPERTY_BUFFS, null, 1);
	}

//------------------- Import/Export and other methods -------------------
	@Override
	public Statistic getStatistic(String name) {
		if (name.equals(STATISTIC_SKILLS)) {
			return skills;
		} else if (name.startsWith(STATISTIC_SKILLS+".")) {
			SkillType type = SkillType.getSkill(name.substring(STATISTIC_SKILLS.length()+1));
			return skills.getSkill(type);
//		} else if (name.equals(STATISTIC_SAVING_THROWS)) {
//			// TODO implement?
//			return null;
		} else if (name.equals(STATISTIC_LEVEL)) {
			return level;
		} else {
			return super.getStatistic(name);
		}
	}

	public static Character parseDOM(Element el) {
		XMLCharacterParser parser = new XMLCharacterParser();
		Character c = parser.parseDOM(el);
		return c;
	}

	private CharacterSheetView autosaver;

	public void setAutoSave(boolean auto) {
		if (auto && autosaver == null) {
			autosaver = new CharacterSheetView(this, true);
		} else {
			autosaver = null;
		}
	}

	public boolean isAutoSaving() {
		return autosaver != null;
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
//		ArrayList<SkillType> set = new ArrayList<>(skills.skills.keySet());
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
		List<String> diffs = new ArrayList<>();

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
		List<SkillType> skillList = new ArrayList<>(allSkills);
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
	@Override
	public Object getProperty(String prop) {
		if (prop.equals(PROPERTY_INITIATIVE))
			// TODO this is inconsistent with Monster. fix.
			return initiative.getBaseValue();

		else if (prop.equals(PROPERTY_LEVEL))
			return level.getLevel();

		else if (prop.equals(PROPERTY_XP))
			return xp;

		else if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			AbilityScore.Type type = AbilityScore.Type.getAbilityType(ability);
			if (type != null) return abilities.get(type).getValue();
		}

		else if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			AbilityScore.Type type = AbilityScore.Type.getAbilityType(ability);
			if (type != null) return abilities.get(type).getOverride();
		}

		else if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(save);
			if (type != null) return saves.get(type);
		}

		else if (prop.startsWith(PROPERTY_SAVE_MISC_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_MISC_PREFIX.length());
			SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(save);
			if (type != null) return saveMisc.get(type);
		}

		else if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			// TODO meaning of this has changed (from total of component type to user set value for component type)
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (ACComponentType t: ACComponentType.values()) {
				if (comp.equals(t.toString())) return getACComponent(t);
			}
		}

		else if (prop.startsWith(PROPERTY_SKILL_PREFIX)) {
			String skill = prop.substring(PROPERTY_SKILL_PREFIX.length());
			SkillType s = SkillType.getSkill(skill);
			if (s != null) return skills.getRanks(s);
			return 0f;
		}

		else if (prop.startsWith(PROPERTY_SKILL_MISC_PREFIX)) {
			String skill = prop.substring(PROPERTY_SKILL_MISC_PREFIX.length());
			SkillType s = SkillType.getSkill(skill);
			if (s != null) return skills.getMisc(s);
			return 0;

		} else {
			return super.getProperty(prop);
		}

		return null;
	}

	@Override
	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_LEVEL))
			setLevel((Integer) value, null, null);
		//else if (prop.equals(PROPERTY_XP)) setXP((Integer)value);	// TODO should this be permitted as an adhoc change?

		else if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			AbilityScore.Type type = AbilityScore.Type.getAbilityType(ability);
			if (type != null) setAbilityScore(type, (Integer)value);
		}

		else if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			AbilityScore.Type type = AbilityScore.Type.getAbilityType(ability);
			if (type != null) setTemporaryAbility(type, (Integer)value);
		}

		else if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(save);
			if (type != null) setSavingThrowBase(type, (Integer)value);
		}

		else if (prop.startsWith(PROPERTY_SAVE_MISC_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_MISC_PREFIX.length());
			SavingThrow.Type type = SavingThrow.Type.getSavingThrowType(save);
			if (type != null) setSavingThrowMisc(type, (Integer)value);
		}

		else if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (ACComponentType t: ACComponentType.values()) {
				if (comp.equals(t.toString())) setACComponent(t, (Integer)value);
			}
		}

		else if (prop.startsWith(PROPERTY_SKILL_PREFIX)) {
			String skillName = prop.substring(PROPERTY_SKILL_PREFIX.length());
			SkillType skill = SkillType.getSkill(skillName);
			if (value == null) {
				setSkillRanks(skill, 0);
			} else {
				setSkillRanks(skill, (Float)value);
			}
		}

		else if (prop.startsWith(PROPERTY_SKILL_MISC_PREFIX)) {
			String skillName = prop.substring(PROPERTY_SKILL_MISC_PREFIX.length());
			SkillType skill = SkillType.getSkill(skillName);
			setSkillMisc(skill, (Integer)value);
		}

		else {
			super.setProperty(prop, value);
		}
	}

	// TODO remove these next three
	private void setAttributeFromProperty(Element e, String name, String prop) {
		String value = (String) getProperty(prop);
		if (value != null && value.length() > 0) e.setAttribute(name, value);
	}

	Element XgetCharacterElement(Document doc) {
		Element e = doc.createElement("Character");
		e.setAttribute("name", getName());
		setAttributeFromProperty(e, "player", Character.PROPERTY_PLAYER);
		setAttributeFromProperty(e, "region", Character.PROPERTY_REGION);
		setAttributeFromProperty(e, "race", Character.PROPERTY_RACE);
		setAttributeFromProperty(e, "gender", Character.PROPERTY_GENDER);
		setAttributeFromProperty(e, "alignment", Character.PROPERTY_ALIGNMENT);
		setAttributeFromProperty(e, "deity", Character.PROPERTY_DEITY);
		setAttributeFromProperty(e, "type", Character.PROPERTY_TYPE);
		setAttributeFromProperty(e, "age", Character.PROPERTY_AGE);
		setAttributeFromProperty(e, "height", Character.PROPERTY_HEIGHT);
		setAttributeFromProperty(e, "weight", Character.PROPERTY_WEIGHT);
		setAttributeFromProperty(e, "eye-colour", Character.PROPERTY_EYE_COLOUR);
		setAttributeFromProperty(e, "hair-colour", Character.PROPERTY_HAIR_COLOUR);
		setAttributeFromProperty(e, "speed", Character.PROPERTY_SPEED);
		setAttributeFromProperty(e, "damage-reduction", Character.PROPERTY_DAMAGE_REDUCTION);
		setAttributeFromProperty(e, "spell-resistance", Character.PROPERTY_SPELL_RESISTANCE);
		setAttributeFromProperty(e, "arcane-spell-failure", Character.PROPERTY_ARCANE_SPELL_FAILURE);
		setAttributeFromProperty(e, "action-points", Character.PROPERTY_ACTION_POINTS);
		return e;
	}

	// XXX this is a reimplementation of the Creature method in order to produce identical output to the orginal version - if exact order is not required we can revert to using the base version
	@Override
	public void executeProcess(CreatureProcessor processor) {
		processor.processCreature(this);

		processor.processLevel(level);
		for (XPHistoryItem i : xpChanges) {
			XP.XPChange cc = i.xpChange;
			cc.executeProcess(processor);
		}

		for (AbilityScore s : abilities.values()) {
			processor.processAbilityScore(s);
		}

		processor.processHPs(hps);
		processor.processInitiative(initiative);
		processor.processSize(size);

		for (SavingThrow.Type t : saves.keySet()) {
			SavingThrow s = saves.get(t);
			processor.processSavingThrow(s);
		}

		processor.processSkills(skills);
		processor.processAC(ac);

		processor.processAttacks(attacks);
		for (CharacterAttackForm a : attackForms) {
			processor.processCharacterAttackForm(a);
		}

		for (int i = 0; i < feats.getSize(); i++) {
			processor.processFeat(feats.get(i));
		}

		for (int i = 0; i < buffs.getSize(); i++) {
			Buff b = buffs.get(i);
			processor.processBuff(b);
		}

		for (String prop : extraProperties.keySet()) {
			processor.processProperty(prop, extraProperties.get(prop));
		}
	}
}

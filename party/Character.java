package party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Attacks.AttackForm;
import gamesystem.BAB;
import gamesystem.Buff;
import gamesystem.CharacterClass;
import gamesystem.CharacterClass.ClassOption;
import gamesystem.CharacterClass.LevelUpAction;
import gamesystem.ClassFeature;
import gamesystem.Creature;
import gamesystem.CreatureProcessor;
import gamesystem.Feat;
import gamesystem.GrappleModifier;
import gamesystem.HPs;
import gamesystem.HitDiceProperty;
import gamesystem.ImmutableModifier;
import gamesystem.InitiativeModifier;
import gamesystem.ItemDefinition;
import gamesystem.Levels;
import gamesystem.Modifier;
import gamesystem.Race;
import gamesystem.Sanity;
import gamesystem.SavingThrow;
import gamesystem.Size;
import gamesystem.SkillType;
import gamesystem.Skills;
import gamesystem.Statistic;
import gamesystem.XP;
import gamesystem.XP.Challenge;
import gamesystem.XP.XPChange;
import gamesystem.XP.XPChangeChallenges;
import gamesystem.XP.XPChangeLevel;
import gamesystem.core.PropertyEvent;
import gamesystem.core.SimpleValueProperty;
import party.InventorySlots.Slot;

/**
 * @author Steve
 *
 */
public class Character extends Creature {
	public final static String xPROPERTY_LEVEL = "Level";

	public final static String xPROPERTY_BUFFS = "Buffs";		// this is sent to listeners, it's not gettable or settable
	public final static String xPROPERTY_ATTACKS = "Attacks";	// this is sent to listeners, it's not gettable or settable

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

	//	private Set<Feat> feats = new HashSet<>();

	public SimpleValueProperty<Integer> xp;	// TODO shouldn't be public - change when XMLOutputProcessor has character specific subclass

	public EnumMap<ACComponentType, Modifier> acMods = new EnumMap<>(ACComponentType.class); // TODO should move to AC panel

	public List<CharacterAttackForm> attackForms = new ArrayList<>();

	public BuffListModel<Feat> feats = new BuffListModel<>();	// TODO reimplement for better encapsulation

	public List<XPHistoryItem> xpChanges = new ArrayList<>();	// TODO shouldn't be public - change when XMLOutputProcessor has character specific subclass

	public InventorySlots slots;

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

	public Character(String name) {
		this.name = new SimpleValueProperty<>("name", this, name);

		xp = new SimpleValueProperty<>("xp", this, 0);

		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			final AbilityScore s = new AbilityScore(t, this);
			abilities.put(t, s);
		}

		initiative = new InitiativeModifier(abilities.get(AbilityScore.Type.DEXTERITY), this);

		race = new Race(this);
		level = new Levels(this);
		level.setLevel(1);
		hitDice = new HitDiceProperty(this, race, level, abilities.get(AbilityScore.Type.CONSTITUTION));

		for (SavingThrow.Type t : SavingThrow.Type.values()) {
			SavingThrow s = new SavingThrow(t, abilities.get(t.getAbilityType()), hitDice, this);
			saves.put(t, s);
		}

		ac = new AC(abilities.get(AbilityScore.Type.DEXTERITY), this);

		skills = new Skills(abilities.values(), ac.getArmorCheckPenalty(), this);

		hps = new HPs(hitDice, this);

		bab = new BAB(this, race, level);

		grapple = new GrappleModifier(this, bab, size, abilities.get(AbilityScore.Type.STRENGTH));

		size = new Size(this);

		attacks = new Attacks(this);

		sanity = new Sanity(this, abilities.get(AbilityScore.Type.WISDOM));

		slots = new InventorySlots(this);
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

	public void setSkillRanks(SkillType s, float ranks) {
		skills.setRanks(s, ranks);
	}

	public Skills getSkillsStatistic() {
		return skills;
	}

	//------------------- Saving Throws -------------------
	public int getSavingThrowMisc(SavingThrow.Type save) {
		if (saveMisc.containsKey(save)) return saveMisc.get(save).getModifier();
		else return 0;
	}

// TODO currently this works by removing the old modifier and adding a new one. could make the modifiers mutable instead
	public void setSavingThrowMisc(SavingThrow.Type save, int misc) {
		if (saveMisc.containsKey(save)) saves.get(save).removeModifier(saveMisc.get(save));
		saveMisc.put(save, new ImmutableModifier(misc));
		saves.get(save).addModifier(saveMisc.get(save));
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

//------------------- XP and level -------------------
	public int getXP() {
		return xp.getValue();
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
		xp.setValue(xp.getValue() + change.xp);
	}

	public void addXPAdhocChange(int delta, String comment, Date d) {
		addXPChange(new XP.XPChangeAdhoc(delta, comment, d));
		xp.setValue(xp.getValue() + delta);
	}

	public int getLevel() {
		return level.getLevel();
	}

	public void setLevel(int l, String comment, Date d) {
		if (level.getLevel() == l) return;
		addXPChange(new XP.XPChangeLevel(level.getLevel(), l, comment, d));
		level.setLevel(l);
	}

	// TODO this functionality should move to the Levels object
	public void setClass(int lvl, CharacterClass cls) {
		CharacterClass old = level.getClass(lvl);
		if (old == cls) return;	// no change, do nothing

		level.setClass(lvl, cls);

		if (old != null) {
			// class features will need to rebuilt as a class level has been removed
			rebuildClassFeatures();
		} else {
			// get the levelup actions for the class in question. only need the actions for the latest class level
			Set<LevelUpAction> actions = cls.getActions(level.getClassLevel(cls));
			//System.out.println("Applying actions for " + cls + " level " + level.getClassLevel(cls));
			for (LevelUpAction action : actions) {
				action.apply(this);
			}
		}

		fireEvent(level.createEvent(PropertyEvent.VALUE_CHANGED));	// TODO more appropriate event type
	}

	private void rebuildClassFeatures() {
		// remove any bonus feats

		for (int i = feats.getSize() - 1; i >= 0; i--) {
			Feat f = feats.get(i);
			if (f.bonus) feats.remove(i);
		}

		// remove all class features
		for (ClassFeature f : features) {
			f.remove(this);
		}
		features.clear();

		Map<CharacterClass, Integer> classes = new HashMap<>();
		for (int i = 1; i <= level.getLevel(); i++) {
			CharacterClass c = level.getClass(i);
			if (c == null) continue;
			int l = 1;
			if (classes.containsKey(c)) {
				l = classes.get(c) + 1;
			}
			classes.put(c, l);
			//System.out.println("Reapplying " + c + " level " + l);
			Set<LevelUpAction> actions = c.getActions(l);
			for (LevelUpAction action : actions) {
				//System.out.println(action);
				action.apply(this);
			}
		}
	}

	public void setClassOption(String id, String selection) {
		ClassOption opt = classOptions.get(id);
		if (opt != null && (opt.selection == null && selection == null || opt.selection != null && !opt.selection.equals(selection))) return;	// no change
		if (opt == null) {
			opt = new ClassOption(id);
			classOptions.put(id, opt);
		}
		opt.selection = selection;
		rebuildClassFeatures();
		fireEvent(level.createEvent(PropertyEvent.VALUE_CHANGED));	// TODO more appropriate event type
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
		if (xpChanges.size() > 0) {
			xp.setValue(xpChanges.get(xpChanges.size() - 1).total);
		} else {
			xp.setValue(0);
		}
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
			Feat f = feats.get(i);
			if (f.getName().equals(name)) return true;
		}
		return false;
	}

	@Override
	public boolean hasFeat(String name, String target) {
		for (int i = 0; i < feats.size(); i++) {
			Feat f = feats.get(i);
			if (f.getName().equals(name) && f.target.equals(target)) return true;
		}
		return false;
	}

	@Override
	public void addFeat(Feat f) {
		feats.addElement(f);
	}

	// TODO handling of AttackForm selection should probably be done by the Attacks statistic
	@Override
	public Set<Statistic> getStatistics(String selector) {
		HashSet<Statistic> stats = new HashSet<>();
		if (selector.startsWith(STATISTIC_ATTACKS + "[id=")) {
			String idStr = selector.substring(selector.indexOf("[id=") + 4, selector.indexOf("]"));
			int id = Integer.parseInt(idStr);
			try {
				for (CharacterAttackForm f : attackForms) {
					if (f.id == id) stats.add(f.attack);
				}
				if (stats.size() == 0) {
					System.err.println("Statistic " + selector + " not found amoungst " + attackForms.size() + " attacks");
				}
			} catch (NumberFormatException e) {
				System.err.println("Invalid statistic designation '" + selector + "'");
				Thread.dumpStack();
			}
		} else if (selector.startsWith(STATISTIC_DAMAGE + "[id=")) {
			String idStr = selector.substring(selector.indexOf("[id=") + 4, selector.indexOf("]"));
			int id = Integer.parseInt(idStr);
			try {
				for (CharacterAttackForm f : attackForms) {
					if (f.id == id) stats.add(f.attack.getDamageStatistic());
				}
				if (stats.size() == 0) {
					System.err.println("Statistic " + selector + " not found amoungst " + attackForms.size() + " attacks");
				}
			} catch (NumberFormatException e) {
				System.err.println("Invalid statistic designation '" + selector + "'");
				Thread.dumpStack();
			}
		} else {
			return super.getStatistics(selector);
		}
		return stats;
	}

//------------ Attacks -------------
	private int nextAttackId = 1;

	public CharacterAttackForm addAttackForm(AttackForm attack) {
		return addAttackForm(attack, nextAttackId++);
	}

	public CharacterAttackForm addAttackForm(AttackForm attack, int id) {
		if (id >= nextAttackId) nextAttackId = id + 1;
		CharacterAttackForm a = new CharacterAttackForm(this, attack, id);
		attackForms.add(a);
		fireEvent(attacks.createEvent(PropertyEvent.VALUE_CHANGED));	// TODO more appropriate event type
		return a;
	}

	public void removeAttackForm(CharacterAttackForm a) {
		int i = attackForms.indexOf(a);
		if (i > -1) {
			attackForms.remove(a);
			fireEvent(attacks.createEvent(PropertyEvent.VALUE_CHANGED));	// TODO more appropriate event type
		}
	}

	public ItemDefinition getSlotItem(Slot slot) {
		return slots.getItem(slot);
	}

	public void setSlotItem(Slot slot, ItemDefinition item) {
		slots.setItem(slot, item);
	}

//------------------- Import/Export and other methods -------------------
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
		if (hps.getMaxHPStat().getValue() != inChar.hps.getMaxHPStat().getValue()) {
			System.out.println(PROPERTY_MAXHPS+"|"+hps+"|"+inChar.hps);
		}
		if (hps.getWounds() != inChar.hps.getWounds()) {
			System.out.println(PROPERTY_WOUNDS+"|"+hps.getWounds()+"|"+inChar.hps.getWounds());
		}
		if (hps.getNonLethal() != inChar.hps.getNonLethal()) {
			System.out.println(PROPERTY_NONLETHAL+"|"+hps.getNonLethal()+"|"+inChar.hps.getNonLethal());
		}
		if (initiative.getRegularValue() != inChar.initiative.getRegularValue()) {
			System.out.println(PROPERTY_INITIATIVE+"|"+initiative.getRegularValue()+"|"+inChar.initiative.getRegularValue());
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
		if (hps.getMaxHPStat().getValue() != inChar.hps.getMaxHPStat().getValue()) diffs.add(PROPERTY_MAXHPS);
		if (hps.getWounds() != inChar.hps.getWounds()) diffs.add(PROPERTY_WOUNDS);
		if (hps.getNonLethal() != inChar.hps.getNonLethal()) diffs.add(PROPERTY_NONLETHAL);
		if (initiative.getRegularValue() != inChar.initiative.getRegularValue()) diffs.add(PROPERTY_INITIATIVE);
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
		}
		return diffs;
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
		processor.processSanity(sanity);
		processor.processSize(size);

		for (SavingThrow.Type t : saves.keySet()) {
			SavingThrow s = saves.get(t);
			processor.processSavingThrow(s);
		}

		processor.processSkills(skills);
		processor.processAC(ac);

		processor.processAttacks(attacks, grapple);
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

		for (String prop : properties.keySet()) {
			if (prop.startsWith("extra")) {
				processor.processProperty(prop, properties.get(prop).getValue());
			}
		}
	}
}

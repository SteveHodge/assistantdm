package gamesystem;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;

import gamesystem.Attacks.AttackForm;
import gamesystem.CharacterClass.ClassOption;
import gamesystem.core.OverridableProperty;
import gamesystem.core.Property;
import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;
import gamesystem.core.PropertyListener;
import gamesystem.core.SettableProperty;
import gamesystem.core.SimpleValueProperty;
import swing.ListModelWithToolTips;

// TODO the initiative property should either be the base value or the total - pick one
// TODO should probably convert these constants to enums
public abstract class Creature implements StatisticsCollection, PropertyCollection {
	// properties
	public final static String PROPERTY_NAME = "Name";	// not currently sent to listeners
	public final static String PROPERTY_MAXHPS = "Hit Points";
	public final static String PROPERTY_WOUNDS = "Wounds";
	public final static String PROPERTY_NONLETHAL = "Non Lethal Damage";
	public final static String PROPERTY_HPS = "Current Hit Points";
	public final static String PROPERTY_INITIATIVE = "Initiative";
	public final static String PROPERTY_ABILITY_PREFIX = "Ability: ";
	public final static String PROPERTY_ABILITY_OVERRIDE_PREFIX = "Temporary Ability: ";	// not currently sent to listeners
	public final static String PROPERTY_SAVE_PREFIX = "Save: ";
	public final static String PROPERTY_SAVE_MISC_PREFIX = "Save (misc mod): ";	// not currently sent to listeners
//	public final static String PROPERTY_AC = "AC";
	public final static String PROPERTY_AC_COMPONENT_PREFIX = "AC: ";	// not currently sent to listeners
	public final static String PROPERTY_SKILL_PREFIX = "Skill: ";
//	public final static String PROPERTY_XP = "XP";
//	public final static String PROPERTY_BAB = "BAB";
//	public final static String PROPERTY_SIZE = "Size";
//	public final static String PROPERTY_TYPE = "Type";	// not currently sent to listeners
//	public final static String PROPERTY_SANITY = "Sanity";
//	public final static String PROPERTY_STARTING_SANITY = "Starting Sanity";
//	public final static String PROPERTY_MAX_SANITY = "Maximum Sanity";
//	public final static String PROPERTY_SANITY_KNOWLEDGE = "Sanity-related Knowledge";

//	public final static String PROPERTY_SPACE = "Space";	// currently only a property on Monster
//	public final static String PROPERTY_REACH = "Reach";	// currently only a property on Monster

	// statistics
	// TODO should be combined with properties
	public final static String STATISTIC_STRENGTH = "ability_scores.strength";
	public final static String STATISTIC_INTELLIGENCE = "ability_scores.intelligence";
	public final static String STATISTIC_WISDOM = "ability_scores.wisdom";
	public final static String STATISTIC_DEXTERITY = "ability_scores.dexterity";
	public final static String STATISTIC_CONSTITUTION = "ability_scores.constitution";
	public final static String STATISTIC_CHARISMA = "ability_scores.charisma";
	//public final static String STATISTIC_ABILITY_CHECKS = "ability_check";
	public final static String STATISTIC_SAVING_THROWS = "saving_throws";
	public final static String STATISTIC_FORTITUDE_SAVE = "saving_throws.fortitude";
	public final static String STATISTIC_WILL_SAVE = "saving_throws.will";
	public final static String STATISTIC_REFLEX_SAVE = "saving_throws.reflex";
	public final static String STATISTIC_SKILLS = "skills";
	public final static String STATISTIC_AC = "ac";
	public final static String STATISTIC_ARMOR = "ac.armor";
	public final static String STATISTIC_SHIELD = "ac.shield";
	public final static String STATISTIC_NATURAL_ARMOR = "ac.natural_armor";
	public final static String STATISTIC_INITIATIVE = "initiative";
	public final static String STATISTIC_HPS = "hit_points";
	public final static String STATISTIC_LEVEL = "level";
	public final static String STATISTIC_ATTACKS = "attacks";
	public final static String STATISTIC_DAMAGE = "attacks.damage";
	public final static String STATISTIC_SIZE = "size";
	public final static String STATISTIC_GRAPPLE = "grapple";

	// The order of these needs to be the same as the ability enum in AbilityScore
	public final static String[] STATISTIC_ABILITY = {STATISTIC_STRENGTH,STATISTIC_DEXTERITY,STATISTIC_CONSTITUTION,STATISTIC_INTELLIGENCE,STATISTIC_WISDOM,STATISTIC_CHARISMA};
	// The order of these needs to be the same as the save enum in SavingThrow
	public final static String[] STATISTIC_SAVING_THROW = {STATISTIC_FORTITUDE_SAVE,STATISTIC_REFLEX_SAVE,STATISTIC_WILL_SAVE};

	// ************************* Non static members and methods **************************

	protected SimpleValueProperty<String> name;

	protected HPs hps;
	public Size size;	// TODO shouldn't be public
	protected InitiativeModifier initiative;
	protected EnumMap<SavingThrow.Type, SavingThrow> saves = new EnumMap<>(SavingThrow.Type.class);
	protected EnumMap<AbilityScore.Type, AbilityScore> abilities = new EnumMap<>(AbilityScore.Type.class);

	protected AC ac;
	protected Attacks attacks;
	public BAB bab;	// TODO shouldn't be public - change where StatBlockCreatureView doesn't need to manipulate bab when adding levels
	protected GrappleModifier grapple;
	public Race race;		// TODO shouldn't be public
	public Levels level;	// TODO shouldn't be public - change when XMLOutputProcessor has character specific subclass
	public Map<String, ClassOption> classOptions = new HashMap<>();
	public List<ClassFeature> features = new ArrayList<>();		// TODO shouldn't be public
	public HitDiceProperty hitDice;		// TODO shouldn't be public
	protected Feats feats;

	protected Sanity sanity;

	protected Map<String, Property<?>> properties = new HashMap<>();
	protected Map<String, List<PropertyListener>> listeners = new HashMap<>();

	@SuppressWarnings("serial")
	public class BuffListModel<T> extends DefaultListModel<T> implements ListModelWithToolTips<T> {
		@Override
		public String getToolTipAt(int index) {
			if (index < 0) return null;
			Object o = get(index);
			if (o instanceof BuffFactory) {
				return ((BuffFactory) o).getDescription();
			} else if (o instanceof Buff) {
				return ((Buff) o).getDescription();
			}
			return null;
		}
	}

	public BuffListModel<Buff> buffs = new BuffListModel<>();		// TODO should be protected

	public Skills skills;		// TODO shouldn't be public

	// TODO this id stuff might be better off in CombatPanel for now
	private static int nextID = 1;
	private static Map<Integer, Creature> idMap = new HashMap<>();
	int id = -1;

	public int getID() {
		if (id == -1) {
			do {
				id = nextID++;
			} while (idMap.containsKey(id));
			idMap.put(id, this);
		}
		return id;
	}

	// TODO shouldn't be here. should check for existing use of new id
	public void setID(int newID) {
		if (id != -1) {
			idMap.remove(id);
		}
		id = newID;
		idMap.put(id, this);
	}

	protected static Creature getCreature(int id) {
		return idMap.get(id);
	}

	//------------------------ PropertyCollection --------------------------
	public void debugDumpStructure() {
		List<String> keys = new ArrayList<>(properties.keySet());
		Collections.sort(keys);
		for (String s : keys) {
			System.out.println(s);
		}
	}

	@Override
	public void addProperty(Property<?> property) {
		properties.put(property.getName(), property);
	}

	// doesn't remove any listeners
	@Override
	public void removeProperty(Property<?> property) {
		properties.remove(property);
	}

	@Override
	public void fireEvent(PropertyEvent event) {
		String propName = event.source.getName();
		while (true) {
			List<PropertyListener> list = listeners.get(propName);
			if (list != null) {
				for (PropertyListener l : list) {
					l.propertyChanged(event);
				}
			}
			if (propName.equals("")) break;
			int idx = propName.lastIndexOf('.');
			if (idx >= 0)
				propName = propName.substring(0, idx);
			else
				propName = "";
		}
	}

	@Override
	public void addPropertyListener(String propName, PropertyListener l) {
		// TODO argument checking: check l is not null
		List<PropertyListener> list = listeners.get(propName);
		if (list == null) {
			list = new ArrayList<>();
			listeners.put(propName, list);
		}
		list.add(l);
	}

	@Override
	public void addPropertyListener(Property<?> property, PropertyListener l) {
		addPropertyListener(property.getName(), l);
	}

	@Override
	public void removePropertyListener(String propName, PropertyListener l) {
		Property<?> property = getProperty(propName);
		if (property == null) return;
		List<PropertyListener> list = listeners.get(property);
		if (list == null) return;
		list.remove(l);
	}

	@Override
	public void removePropertyListener(Property<?> property, PropertyListener l) {
		List<PropertyListener> list = listeners.get(property);
		if (list == null) return;
		list.remove(l);
	}

	@Override
	public Property<?> getProperty(String name) {
		return properties.get(name);
	}
	//----------------------------------------------------------------------

	public void setName(String n) {
		name.setValue(n);
	}

	public HPs getHPStatistic() {
		return hps;
	}

	public Size getSizeStatistic() {
		return size;
	}

	public InitiativeModifier getInitiativeStatistic() {
		return initiative;
	}

	public AC getACStatistic() {
		return ac;
	}

	public AbilityScore getAbilityStatistic(AbilityScore.Type t) {
		return abilities.get(t);
	}

	public SavingThrow getSavingThrowStatistic(SavingThrow.Type t) {
		return saves.get(t);
	}

	public Attacks getAttacksStatistic() {
		return attacks;
	}

	public OverridableProperty<Integer> getBAB() {
		return bab;
	}

	public GrappleModifier getGrappleModifier() {
		return grapple;
	}

	public Sanity getSanity() {
		return sanity;
	}

	public Feats getFeats() {
		return feats;
	}

	//------------ Features -------------
	public void addClassFeature(ClassFeature f) {
		features.add(f);
		f.apply(this);
		//System.out.println("Added " + f);
	}

	public void removeClassFeature(String id) {
		ClassFeature feature = getClassFeature(id);
		if (feature == null) throw new IllegalArgumentException("Character " + this + " does not have class feature " + id);
		//System.out.println("Removed " + feature);
		feature.remove(this);
		features.remove(feature);
	}

	public ClassFeature getClassFeature(String id) {
		for (ClassFeature f : features) {
			if (f.definition.id.equals(id)) return f;
		}
		return null;
	}

	public void setClassFeatureParameter(String id, String parameter, Object value) {
		ClassFeature feature = getClassFeature(id);
		if (feature == null) throw new IllegalArgumentException("Character " + this + " does not have class feature " + id);
		feature.setParameter(parameter, value);
		//System.out.println("Updated " + feature);
	}

	/*	public Object getClassFeatureParameter(String id, String parameter) {
			ClassFeature feature = getClassFeature(id);
			if (feature == null) throw new IllegalArgumentException("Character " + this + " does not have class feature " + id);
			return feature.getParameter(parameter);
		}*/

	//------------------- BAB -------------------
	private OverridableProperty.PropertyValue<Integer> babOverride;

	public void setBABOverride(int val) {
		clearBABOverride();
		babOverride = bab.addOverride(val);
	}

	public void clearBABOverride() {
		if (babOverride != null) {
			bab.removeOverride(babOverride);
			babOverride = null;
		}
	}

	// buff related methods
	@Override
	public StatisticDescription[] getStatistics() {
		List<StatisticDescription> targets = new ArrayList<>();
		targets.add(new StatisticDescription(AbilityScore.Type.STRENGTH.toString(), STATISTIC_STRENGTH));
		targets.add(new StatisticDescription(AbilityScore.Type.INTELLIGENCE.toString(), STATISTIC_INTELLIGENCE));
		targets.add(new StatisticDescription(AbilityScore.Type.WISDOM.toString(), STATISTIC_WISDOM));
		targets.add(new StatisticDescription(AbilityScore.Type.DEXTERITY.toString(), STATISTIC_DEXTERITY));
		targets.add(new StatisticDescription(AbilityScore.Type.CONSTITUTION.toString(), STATISTIC_CONSTITUTION));
		targets.add(new StatisticDescription(AbilityScore.Type.CHARISMA.toString(), STATISTIC_CHARISMA));
		targets.add(new StatisticDescription("Saving Throws", STATISTIC_SAVING_THROWS));
		targets.add(new StatisticDescription(SavingThrow.Type.FORTITUDE.toString(), STATISTIC_FORTITUDE_SAVE));
		targets.add(new StatisticDescription(SavingThrow.Type.WILL.toString(), STATISTIC_WILL_SAVE));
		targets.add(new StatisticDescription(SavingThrow.Type.REFLEX.toString(), STATISTIC_REFLEX_SAVE));
		targets.add(new StatisticDescription(ac.getDescription(), STATISTIC_AC));
		StatisticDescription[] tgts = ac.getStatistics();
		for (StatisticDescription t : tgts) {
			t.name = "... " + t.name;
			targets.add(t);
		}
		targets.add(new StatisticDescription(initiative.getDescription(), STATISTIC_INITIATIVE));
		targets.add(new StatisticDescription(hps.getDescription(), STATISTIC_HPS));
		targets.add(new StatisticDescription(level.getDescription(), STATISTIC_LEVEL));
		targets.add(new StatisticDescription(attacks.getDescription(), STATISTIC_ATTACKS));
		// add attacks subtargets
		targets.add(new StatisticDescription(attacks.getDamageStatistic().getDescription(), STATISTIC_DAMAGE));
		// add attacks subtargets
		targets.add(new StatisticDescription(size.getDescription(), STATISTIC_SIZE));
		targets.add(new StatisticDescription(grapple.getDescription(), STATISTIC_GRAPPLE));
		targets.add(new StatisticDescription(skills.getDescription(), STATISTIC_SKILLS));
		tgts = skills.getStatistics();
		for (StatisticDescription t : tgts) {
			t.name = "... " + t.name;
			targets.add(t);
		}
		return targets.toArray(new StatisticDescription[targets.size()]);
	}

// returns any statistics that match the supplied selector. always returns a valid Set, which may be empty
	public Set<Statistic> getStatistics(String selector) {
		HashSet<Statistic> stats = new HashSet<>();
		if (selector.equals(STATISTIC_SAVING_THROWS)) {
			stats.add(saves.get(SavingThrow.Type.FORTITUDE));
			stats.add(saves.get(SavingThrow.Type.WILL));
			stats.add(saves.get(SavingThrow.Type.REFLEX));
		} else {
			Statistic s = getStatistic(selector);
			if (s != null) stats.add(s);
		}
		return stats;
	}

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
		} else if (name.startsWith(STATISTIC_SKILLS + ".")) {
			SkillType type = SkillType.getSkill(name.substring(STATISTIC_SKILLS.length() + 1));
			return skills.getSkill(type);
		} else if (name.equals(STATISTIC_LEVEL)) {
			return level;
		} else if (name.equals(STATISTIC_HPS)) {
			return hps;
		} else if (name.equals(STATISTIC_HPS + ".max_hps")) {
			return hps.getMaxHPStat();
		} else if (name.equals(STATISTIC_ATTACKS)) {
			return attacks;
		} else if (name.equals(STATISTIC_DAMAGE)) {
			return attacks.getDamageStatistic();
		} else if (name.equals(STATISTIC_SIZE)) {
			return size;
		} else if (name.equals(STATISTIC_GRAPPLE)) {
			return grapple;
		} else {
			System.err.println("Unknown statistic '" + name + "' in " + this + " (Creature.getStatistic)");
			Thread.dumpStack();
			return null;
		}
	}

// TODO refactor the BuffListModel class and this accessor
	public ListModelWithToolTips<Buff> getBuffListModel() {
		return buffs;
	}

	public void addBuff(Buff b) {
		buffs.addElement(b);	// we add the buff to the buffs list first because b.apply will trigger change events and we want buffs to be up to date for those events
		b.apply(this);
	}

	public void removeBuff(Buff b) {
		buffs.removeElement(b);	// we add the buff to the buffs list first because b.apply will trigger change events and we want buffs to be up to date for those events
		b.remove(this);
	}

// remove a buff by id
	public void removeBuff(int id) {
		for (int i = 0; i < buffs.getSize(); i++) {
			Buff b = buffs.get(i);
			if (b.id == id) {
				buffs.removeElement(b);	// we add the buff to the buffs list first because b.apply will trigger change events and we want buffs to be up to date for those events
				b.remove(this);
			}
		}
	}

	public Object getPropertyValue(String prop) {
		Property<?> property = getProperty(prop);
		if (property == null && !prop.startsWith("extra.") && !prop.startsWith("field.")) {
			property = getProperty("extra." + prop);
		}
		if (property == null) {
			System.err.println("Attempt to get unknown property: " + prop);
//			Thread.dumpStack();
			return null;
		}
		return property.getValue();
	}

	@SuppressWarnings("unchecked")
	public void setProperty(String prop, String value) {
		if (!prop.startsWith("extra.") && !prop.startsWith("field.")) prop = "extra." + prop;
		Property<?> property = getProperty(prop);
		if (property == null) {
//			System.out.println("Adding adhoc property " + prop + " with value '" + value + "'");
			property = new SimpleValueProperty<String>(prop, this, value);
		}
		if (property instanceof SettableProperty) {
//			System.out.println("Setting adhoc property " + prop + " to '" + value + "'");
			((SettableProperty<String>) property).setValue(value);
		} else {
			System.err.println("Can't set value on " + property);
		}
	}

	public boolean hasProperty(String name) {
		if (properties.containsKey(name)) return true;
		return properties.containsKey("extra." + name);
	}

	@Override
	public String toString() {
		return name.getValue();
	}

	public String getName() {
		return name.getValue();
	}

//------------------- property level get/set methods -------------------
// TODO most of these should be removed - they should be manipulated via the statistic

	public Modifier getAbilityModifier(AbilityScore.Type ability) {
		if (abilities.get(ability) == null) return null;
		return abilities.get(ability).getModifier();
	}

//------------- Others --------------
	public boolean hasFeat(String feat) {
		if (feats == null || feat == null) return false;
		return feats.hasFeat(feat);
	}

	// currently used for checking (greater) weapon spec, (greater) weapon focus
	abstract public boolean hasFeat(String name, AttackForm attack);	// check if a feat applies to a specific attack

// ----- visitor pattern for processing -----
	public void executeProcess(CreatureProcessor processor) {
		processor.processCreature(this);

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

		processor.processAC(ac);
		processor.processAttacks(attacks, grapple);

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

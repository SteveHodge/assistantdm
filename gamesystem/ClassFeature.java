package gamesystem;

import gamesystem.ClassFeature.ClassFeatureDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Class Features/Special Abilities
 *
 * Classes:
 * ClassFeature - instantiated class feature that's attached to a character. may have parameters with associated current values
 * ClassFeatureDefinition - definition of a class feature, including defining parameters with default values
 * LevelUpAction - abstract base class for classes that represent actions that affect class features (and feats) that happen on level up
 *   AddBonusFeatAction - add a bonus feature for a specifed list of options
 *   AddFeatureAction - add a class feature
 *   RemoveFeatureAction - remove a class feature
 *   SetParameterAction - change a parameter on an existing class feature
 *
 * ToDo:
 * Calculated parameters in output
 * Calculation system for determining variable bonuses, DCs, etc
 * UI for selecting options
 * ClassFeatures might need to track what classes they came from. this might be important for things that stack across classes
 * Extend calculated parameters to FeatDefinition
 */

// a class feature as instantiated for a particular character. tracks the values of parameters
public class ClassFeature extends Feature<ClassFeature, ClassFeatureDefinition> {
	Map<String, Object> parameters;	// maps parameter name to value
	int template = 0;	// index of current template

	ClassFeature(ClassFeatureDefinition def) {
		super(def);
	}

	public void setParameter(String param, Object val) {
		if ("template".equals(param)) {
			template = (Integer) val;
		} else {
			if (parameters == null) parameters = new HashMap<>();
			parameters.put(param, val);
		}
	}

	public String getNameAndType() {
		if (definition.type == SpecialAbilityType.NATURAL) return definition.name;
		return String.format("%s (%s)", definition.name, definition.type.getAbbreviation());
	}

	public String getSummary() {
		String out = new String(definition.summaries.get(template));
		if (parameters != null) {
			for (String param : parameters.keySet()) {
				out = out.replace("&(" + param + ")", parameters.get(param).toString());
			}
		}
		return out;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(definition);
		if (template != 0 || parameters != null && parameters.size() > 0) {
			String[] pieces = new String[parameters.size() + (template == 0 ? 0 : 1)];
			int i = 0;
			if (template != 0) pieces[i++] = "template: "+template;
			if (parameters != null) {
				for (String p : parameters.keySet()) {
					pieces[i++] = p + ": " + parameters.get(p);
				}
			}
			b.append(" [").append(String.join(", ", pieces)).append("]");
		}
		return b.toString();
	}

	static enum SpecialAbilityType {
		NATURAL,
		EXTRAORDINARY,
		SPELL_LIKE,
		SUPERNATURAL;

		public String getAbbreviation() {
			if (this == EXTRAORDINARY) return "Ex";
			if (this == SUPERNATURAL) return "Su";
			if (this == SPELL_LIKE) return "Sp";
			return "";
		}
	}

	// represents the definition of a class feature. generates instances of ClassFeature as required
	public static class ClassFeatureDefinition extends FeatureDefinition<ClassFeatureDefinition> {
		public String id;
		SpecialAbilityType type;
		List<String> summaries = new ArrayList<>();
		Map<String, Object> parameters = new HashMap<>();	// maps parameter name to default value

		ClassFeatureDefinition(String id, String name, SpecialAbilityType type) {
			super(name);
			this.id = id;
			this.type = type;
		}

		ClassFeatureDefinition(String id, String name) {
			this(id, name, SpecialAbilityType.NATURAL);
		}

		ClassFeatureDefinition addSummary(String summary) {
			summaries.add(summary);
			return this;
		}

		ClassFeatureDefinition addParameter(String name, Object def) {
			parameters.put(name, def);
			return this;
		}

		ClassFeature getFeature() {
			ClassFeature f = new ClassFeature(this);

			// setup any parameters
			for (String param : parameters.keySet()) {
				if (f.parameters == null) f.parameters = new HashMap<>();
				f.parameters.put(param, parameters.get(param));
			}

			// add any effects:
			for (Effect e : effects) {
				f.modifiers.put(e.getModifier(name), e.target);
			}

			return f;
		}

		@Override
		public String toString() {
			if (type == SpecialAbilityType.NATURAL) return name;
			return String.format("%s (%s)", name, type.getAbbreviation());
		}

		static ClassFeatureDefinition findFeatureFactory(String feature) {
			for (ClassFeatureDefinition f : ClassFeature.featureDefinitions) {
				if (f.id.equals(feature)) return f;
			}
			return null;
		}
	}

	static ClassFeatureDefinition[] featureDefinitions = {
		new ClassFeatureDefinition("ac_bonus", "AC Bonus", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("Add Wis bonus + &(bonus) AC; this bonus is only lost if you are immobilized, wearing armor, carrying a shield, or carrying a medium/heavy load.")		// TODO calculate value
		.addParameter("bonus", 0),

		new ClassFeatureDefinition("flurry_of_blows", "Flurry of Blows", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("Take &(attacks) extra attacks when taking a full attack action. All attacks in the round suffer a &(penalty) penalty.")
		.addSummary("Take &(attacks) extra attacks when taking a full attack action.")
		.addParameter("penalty", -2)
		.addParameter("attacks", 1),

		new ClassFeatureDefinition("unarmed_strike", "Unarmed Strike")
		.addSummary("Your unarmed attacks deal &(damage) lethal damage and apply full strength bonus.")		// TODO calculate value
		.addParameter("damage", "1d6"),

		new ClassFeatureDefinition("evasion", "Evasion", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("If an attack allows a Reflex save for half damage, then take no damage on a successful save."),

		new ClassFeatureDefinition("improved_evasion", "Improved Evasion", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You take half damage from attacks that allow a Reflex save. Take no damage on a successful save."),

		new ClassFeatureDefinition("monk_fast_movement", "Fast Movement", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("Your speed increases by &(bonus) (limited by armor and encumbrance).")
		.addParameter("bonus", 10),

		new ClassFeatureDefinition("still_mind", "Still Mind", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("+2 to saves vs enchantment spells and effects.")
		.addBonus(Creature.STATISTIC_SAVING_THROWS, 2, "vs enchantments"),

		new ClassFeatureDefinition("ki_strike", "Ki Strike", SpecialAbilityType.SUPERNATURAL)
		.addSummary("Your unarmed attacks are treated as &(type) weapons.")
		.addParameter("type", "magic"),

		new ClassFeatureDefinition("slow_fall", "Slow Fall", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("As long as a wall is within arm's reach, take damage from a fall as if it were &(height) feet shorter.")
		.addSummary("As long as a wall is within arm's reach, take no damage from a fall.")
		.addParameter("height", 20),

		new ClassFeatureDefinition("purity_of_body", "Purity of Body", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("Immune to all diseases except supernatural and magical diseases."),

		new ClassFeatureDefinition("wholeness_of_body", "Wholeness of Body", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can heal your own wounds, up to double your monk level points per day."),	// TODO calculated value

		new ClassFeatureDefinition("diamond_body", "Diamond Body", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You are immune to all poisons."),

		new ClassFeatureDefinition("abundant_step", "Abundant Step", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can slip between spaces as if using the spell dimension door once per day, cast at half your monk level."),		// TODO calculate value

		new ClassFeatureDefinition("diamond_soul", "Diamond Soul", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You have spell resistance of 10 + your monk level."),		// TODO calculate value

		new ClassFeatureDefinition("quivering_palm", "Quivering Palm", SpecialAbilityType.SUPERNATURAL)
		.addSummary("(1/week) If you damage the victim with an unarmed attack, you can slay them with an act of will any time within monk level days."),		// TODO calculate value

		new ClassFeatureDefinition("timeless_body", "Timeless Body", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You no longer suffer additional penalties for aging, and cannot be magically aged. Your lifespan is not increased."),

		new ClassFeatureDefinition("tongue_of_the_sun_and_moon", "Tongue of the Sun and Moon", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You can speak with any living creature."),

		new ClassFeatureDefinition("empty_body", "Empty Body", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can become ethereal for monk level rounds per day, as the spell etherealness."),		// TODO calculate value

		new ClassFeatureDefinition("perfect_self", "Perfect Self")
		.addSummary("You are now considered an outsider. In addition, you gain damage resistance 10/magic.")
	};
}

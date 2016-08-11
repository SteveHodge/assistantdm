package gamesystem;


import gamesystem.dice.Dice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/*
 * A Buff is a particular instantiation of an effect of some sort (e.g. a casting of a spell). The specific details of a Buff
 * may differ based on factors such as caster level. Buffs allow those details to be set before the Buff is applied to a Character.
 * Once the Buff is applied, it's effects are fixed.
 */

public class Buff extends Feature<Buff, BuffFactory> {
	public String name;
	public List<FeatureDefinition.Effect> effects = new ArrayList<>();
	boolean realised = false;	// set to true once the effects have been fixed
	Map<PropertyChange, String> propertyChanges = new HashMap<>();	// map of property changes to the target
	int casterLevel = 0;
	Map<Dice, Integer> rolls = new HashMap<>();
	public boolean maximized = false;
	public boolean empowered = false;
	public int id;
	static int nextid = 1;	// TODO not threadsafe, should be private (used in parsing at the moment)

	class PropertyChange {
		String property;
		Object value;
		String description;
	}

	final static Map<String, String> TARGET_DESC;
	static {
		HashMap<String, String> map = new HashMap<>();
		map.put(Creature.STATISTIC_STRENGTH, AbilityScore.Type.STRENGTH.toString());
		map.put(Creature.STATISTIC_INTELLIGENCE, AbilityScore.Type.INTELLIGENCE.toString());
		map.put(Creature.STATISTIC_WISDOM, AbilityScore.Type.WISDOM.toString());
		map.put(Creature.STATISTIC_DEXTERITY, AbilityScore.Type.DEXTERITY.toString());
		map.put(Creature.STATISTIC_CONSTITUTION, AbilityScore.Type.CONSTITUTION.toString());
		map.put(Creature.STATISTIC_CHARISMA, AbilityScore.Type.CHARISMA.toString());
		map.put(Creature.STATISTIC_SAVING_THROWS, "saves");
		map.put(Creature.STATISTIC_FORTITUDE_SAVE, SavingThrow.Type.FORTITUDE + " save");
		map.put(Creature.STATISTIC_WILL_SAVE, SavingThrow.Type.WILL + " save");
		map.put(Creature.STATISTIC_REFLEX_SAVE, SavingThrow.Type.REFLEX + " save");
		map.put(Creature.STATISTIC_SKILLS, "skills");
		map.put(Creature.STATISTIC_AC, "AC");
		map.put(Creature.STATISTIC_ARMOR, "armor");
		map.put(Creature.STATISTIC_SHIELD, "shield");
		map.put(Creature.STATISTIC_NATURAL_ARMOR, "natural_armor");
		map.put(Creature.STATISTIC_INITIATIVE, "initiative");
		map.put(Creature.STATISTIC_ATTACKS, "attack rolls");
		map.put(Creature.STATISTIC_DAMAGE, "damage");
		map.put(Creature.STATISTIC_HPS, "temporary hit points");
		TARGET_DESC = Collections.unmodifiableMap(map);
	}

	public Buff() {
		super(null);
		//System.out.println("Creating Buff with id " + nextid);
		id = nextid++;
	}

	public boolean requiresCasterLevel() {
		for (FeatureDefinition.Effect e : effects) {
			if (e.requiresCasterLevel()) return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	public int getCasterLevel() {
		return casterLevel;
	}

	public void setCasterLevel(int cl) {
		casterLevel = cl;
	}

	public void setRoll(Dice d, int r) {
		rolls.put(d, r);
	}

	@Override
	public void apply(Creature c) {
		if (!realised) {
			// create modifiers/properties for each Effect. this will fix the specifics of the Effects
			for (FeatureDefinition.Effect e : effects) {
				if (e instanceof FeatureDefinition.ModifierEffect) {
					modifiers.put(((FeatureDefinition.ModifierEffect)e).getModifier(this), e.target);
				} else if (e instanceof PropertyEffect) {
					PropertyChange change = new PropertyChange();
					change.property = ((PropertyEffect)e).property;
					change.value = ((PropertyEffect)e).value;
					change.description = ((PropertyEffect)e).description;
					propertyChanges.put(change, e.target);
				}
			}
			realised = true;
		}

		super.apply(c);
		for (PropertyChange p : propertyChanges.keySet()) {
			for (Statistic s : Feature.getTargetStats(c, propertyChanges.get(p))) {
				s.setProperty(p.property, p.value, name, id);
			}
		}
	}

	@Override
	public void remove(Creature c) {
		super.remove(c);
		for (PropertyChange p : propertyChanges.keySet()) {
			for (Statistic s : Feature.getTargetStats(c, propertyChanges.get(p))) {
				s.resetProperty(p.property, id);
			}
		}
	}

	public String getDescription() {
		StringBuilder s = new StringBuilder();
		s.append("<html><body>");
		for (Modifier m : modifiers.keySet()) {
			String target = modifiers.get(m);
			if (m.getModifier() >= 0) s.append("+");
			s.append(m.getModifier());
			if (m.getType() != null) s.append(" ").append(m.getType());
			if (m.getModifier() >= 0) {
				s.append(" bonus");
			} else {
				s.append(" penalty");
			}

			s.append(" to ").append(getTargetDescription(target));
			if (m.getCondition() != null) s.append(" ").append(m.getCondition());
			s.append("<br/>");
		}
		for (PropertyChange p : propertyChanges.keySet()) {
			String target = propertyChanges.get(p);
			s.append(p.description).append(" to ").append(getTargetDescription(target));
			s.append("<br/>");
		}
		s.append("</html></body>");
		return s.toString();
	}

	public static String getTargetDescription(String target) {
		String d = TARGET_DESC.get(target);
		if (target.startsWith(Creature.STATISTIC_SKILLS + ".")) {
			return target.substring(Creature.STATISTIC_SKILLS.length() + 1);
		}
		if (d != null) return d;
		return target;
	}

	public static class PropertyEffect extends FeatureDefinition.Effect {
		String property;
		Object value;
		String description;

		@Override
		public String toString() {
			return description;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof PropertyEffect)) return false;
			PropertyEffect p = (PropertyEffect) o;
			if (!target.equals(p.target)) return false;
			if (!property.equals(p.property)) return false;
			if (!value.equals(p.value)) return false;
			if (description != null && (p.description == null || !description.equals(p.description))
					|| description == null && p.description != null) return false;
			return true;
		}
	}

	protected static class CLEffect extends FeatureDefinition.ModifierEffect {
		Object baseMod;
		int perCL;
		int maxPerCL;
		boolean penalty = false;

		@Override
		public Modifier getModifier(Buff b) {
			int mod = 0;
			if (perCL > 0) mod = b.casterLevel/perCL;
			if (mod > maxPerCL) mod = maxPerCL;
			if (baseMod != null) {
				if (baseMod instanceof Integer) {
					mod += (Integer)baseMod;
				} else if (baseMod instanceof Dice) {
					int roll;
					if (b.rolls.containsKey(baseMod)) {
						roll = b.rolls.get(baseMod);
					} else {
						roll = ((Dice)baseMod).roll();
					}
					if (b.empowered) mod += (mod + roll)/2;
					if (b.maximized) {
						mod += ((Dice)baseMod).getMaximum();
					} else {
						mod += roll;
					}
				}
			}
			if (mod < 1) mod = 1;
			if (penalty) mod = -mod;
			ImmutableModifier m = new ImmutableModifier(mod, type, b.name, condition);
			m.id = b.id;
			return m;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();

			if (type != null) s.append(" ").append(type);
			if (penalty) {
				s.append(" penalty of ");
			} else {
				s.append(" bonus of ");
			}
			if (baseMod != null) {
				if (baseMod instanceof Integer && ((Integer) baseMod).intValue() > 0) {
					if (penalty) s.append("-");
					else s.append("+");
					s.append(baseMod);
				}
				if (baseMod instanceof Dice) s.append(baseMod);
			}
			if (perCL > 0) {
				s.append(" + 1 per ");
				if (perCL == 1) {
					s.append(" caster level");
				} else {
					s.append(perCL).append(" caster levels");
				}
			}

			s.append(" to ").append(getTargetDescription(target));
			if (condition != null) s.append(" ").append(condition);
			return s.toString();
		}

		@Override
		public boolean requiresCasterLevel() {
			return true;
		}

		@Override
		public Dice getDice() {
			if (baseMod instanceof Dice) return (Dice)baseMod;
			return null;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof CLEffect)) return false;
			CLEffect c = (CLEffect) o;
			if (!target.equals(c.target)) return false;

			if (type != null && (c.type == null || !type.equals(c.type))
					|| type == null && c.type != null) return false;
			if (condition != null && (c.condition == null || !condition.equals(c.condition))
					|| condition == null && c.condition != null) return false;

			// maxPerCL ignored if perCL is 0
			return perCL == c.perCL && (perCL == 0 || maxPerCL == c.maxPerCL) && penalty == c.penalty && baseMod.equals(c.baseMod);
		}
	}

	public static class FixedEffect extends FeatureDefinition.FixedEffect {
		public int modifier;

		@Override
		public ImmutableModifier getModifier(Buff b) {
			ImmutableModifier m = super.getModifier(b);
			m.id = b.id;
			return m;
		}
	}

	/*
	 * If the Buff has not been applied to the character then this returns null
	 */
	public Element getElement(Document doc) {
		return getElement(doc, "Buff");
	}

	public Element getElement(Document doc, String tag) {
		//if (modifiers.size() == 0) return null;

		Element e = doc.createElement(tag);
		e.setAttribute("name", name);
		if (casterLevel > 0) e.setAttribute("caster_level", ""+casterLevel);
		e.setAttribute("id", ""+id);

		for (Modifier m : modifiers.keySet()) {
			Element me = doc.createElement("Modifier");
			if (m.getType() != null) me.setAttribute("type", m.getType());
			if (m.getCondition() != null) me.setAttribute("condition", m.getCondition());
			me.setAttribute("value", ""+m.getModifier());
			String target = modifiers.get(m);
			me.setAttribute("target", target);		// WISH this can be XML relevant, i.e. an XPath
			me.setAttribute("description", getTargetDescription(target)+": "+m.toString());		// TODO this is needed only for the character sheet
			e.appendChild(me);
		}

		for (PropertyChange p : propertyChanges.keySet()) {
			Element pe = doc.createElement("PropertyChange");
			pe.setAttribute("description", p.description);	// TODO might need to be more useful for the character sheet
			pe.setAttribute("property", p.property);
			pe.setAttribute("value", p.value.toString());
			String target = propertyChanges.get(p);
			pe.setAttribute("target", target);		// WISH this can be XML relevant, i.e. an XPath
			e.appendChild(pe);
		}

		return e;
	}
}

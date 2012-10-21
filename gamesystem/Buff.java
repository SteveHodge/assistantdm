package gamesystem;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import party.Character;
import party.Creature;

/*
 * A Buff is a particular instantiation of an effect of some sort (e.g. a casting of a spell). The specific details of a Buff
 * may differ based on factors such as caster level. Buffs allow those details to be set before the Buff is applied to a Character.
 * Once the Buff is applied, it's effects are fixed.
 */

public class Buff {
	public String name;
	public List<Effect> effects = new ArrayList<Effect>();
	Map<Modifier,String> modifiers = new HashMap<Modifier,String>();
	int casterLevel = 0;
	Map<Dice,Integer> rolls = new HashMap<Dice,Integer>();
	public boolean maximized = false;
	public boolean empowered = false;

	public boolean requiresCasterLevel() {
		for (Effect e : effects) {
			if (e instanceof CLEffect) return true;
		}
		return false;
	}

	public String toString() {
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

	public void applyBuff(Character c) {
		if (modifiers.size() == 0) {
			// create modifiers for each Effect. this will fixed the specifics of the Effects
			for (Effect e : effects) {
				modifiers.put(e.getModifier(this),e.target);
			}
		}

		for (Modifier m : modifiers.keySet()) {
			// add modifier to target stat
			for (Statistic s : getTargetStats(c, modifiers.get(m))) {
				s.addModifier(m);
			}
		}
	}

	public void removeBuff(Character c) {
		for (Modifier m : modifiers.keySet()) {
			// remove modifier from target stat
			for (Statistic s : getTargetStats(c, modifiers.get(m))) {
				s.removeModifier(m);
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
			
			s.append(" to ").append(Creature.STATISTIC_DESC.get(target));
			if (m.getCondition() != null) s.append(" ").append(m.getCondition());
			s.append("<br/>");
		}
		s.append("</html></body>");
		return s.toString();
	}

	protected Statistic[] getTargetStats(Character c, String target) {
		Statistic[] stats;
		if (target.equals(Creature.STATISTIC_SAVING_THROWS)) {
			stats = new Statistic[3];
			stats[0] = c.getStatistic(Creature.STATISTIC_FORTITUDE_SAVE);
			stats[1] = c.getStatistic(Creature.STATISTIC_WILL_SAVE);
			stats[2] = c.getStatistic(Creature.STATISTIC_REFLEX_SAVE);
		} else {
			stats = new Statistic[1];
			stats[0] = c.getStatistic(target);
		}
		return stats;
	}

	public abstract static class Effect {
		String target;
		String type;
		String condition;
	
		public abstract Modifier getModifier(Buff b);

		public boolean requiresCasterLevel() {
			return false;
		};

		public Dice getDice() {
			return null;
		}
	}

	protected static class CLEffect extends Effect {
		Object baseMod;
		int perCL;
		int maxPerCL;
		boolean penalty = false;

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
			return new ImmutableModifier(mod, type, b.name, condition);
		}

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
			
			s.append(" to ").append(Creature.STATISTIC_DESC.get(target));
			if (condition != null) s.append(" ").append(condition);
			return s.toString();
		}

		public boolean requiresCasterLevel() {
			return true;
		}

		public Dice getDice() {
			if (baseMod instanceof Dice) return (Dice)baseMod;
			return null;
		}
	}

	protected static class FixedEffect extends Effect {
		int modifier;
	
		public Modifier getModifier(Buff b) {
			return new ImmutableModifier(modifier, type, b.name, condition);
		}
	
		public String toString() {
			StringBuilder s = new StringBuilder();
	
			if (modifier >= 0) s.append("+");
			s.append(modifier);
			if (type != null) s.append(" ").append(type);
			if (modifier >= 0) {
				s.append(" bonus");
			} else {
				s.append(" penalty");
			}
			
			s.append(" to ").append(Creature.STATISTIC_DESC.get(target));
			if (condition != null) s.append(" ").append(condition);
			return s.toString();
		}
	}

	/*
	 * If the Buff has not been applied to the character then this returns null
	 */
	public Element getElement(Document doc) {
		if (modifiers.size() == 0) return null;

		Element e = doc.createElement("Buff");
		e.setAttribute("name", name);
		e.setAttribute("caster_level", ""+casterLevel);

		for (Modifier m : modifiers.keySet()) {
			Element me = doc.createElement("Modifier");
			if (m.getType() != null) me.setAttribute("type", m.getType());
			if (m.getCondition() != null) me.setAttribute("condition", m.getCondition());
			me.setAttribute("value", ""+m.getModifier());
			me.setAttribute("target", modifiers.get(m));		// WISH this should probably be XML relevant, i.e. an XPath
			e.appendChild(me);
		}

		return e;
	}

	public static Buff parseDOM(Element b) {
		if (!b.getTagName().equals("Buff")) return null;
		Buff buff = new Buff();
		buff.casterLevel = Integer.parseInt(b.getAttribute("caster_level"));
		buff.name = b.getAttribute("name");
		NodeList mods = b.getChildNodes();
		if (mods != null) {
			for (int k=0; k<mods.getLength(); k++) {
				if (!mods.item(k).getNodeName().equals("Modifier")) continue;
				Element m = (Element)mods.item(k);
				String target = m.getAttribute("target");
				int value = Integer.parseInt(m.getAttribute("value"));
				String type = m.getAttribute("type");
				Modifier mod = new ImmutableModifier(value, type, buff.name);
				buff.modifiers.put(mod,target);
			}
		}
		return buff;
	}
}

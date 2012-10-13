package gamesystem;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public boolean requiresCasterLevel() {
		for (Effect e : effects) {
			if (e instanceof CLEffect) return true;
		}
		return false;
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

		public Modifier getModifier(Buff b) {
			int mod = b.casterLevel/perCL;
			if (mod > maxPerCL) mod = maxPerCL;
			if (baseMod != null) {
				if (baseMod instanceof Integer) {
					mod += (Integer)baseMod;
				} else if (baseMod instanceof Dice) {
					if (b.rolls.containsKey(baseMod)) {
						mod += b.rolls.get(baseMod);
					} else {
						mod += ((Dice)baseMod).roll();
					}
				}
			}
			if (mod < 1) mod = 1;
			return new ImmutableModifier(mod, type, b.name, condition);
		}

		// TODO this assumes caster level based stuff is always a bonus
		public String toString() {
			StringBuilder s = new StringBuilder();
	
			if (type != null) s.append(" ").append(type);
			s.append(" bonus of ");
			if (baseMod != null) {
				if (baseMod instanceof Integer && ((Integer) baseMod).intValue() > 0) s.append("+").append(baseMod);
				if (baseMod instanceof Dice) s.append(baseMod);
			}
			s.append(" + 1 per ");
			if (perCL == 1) {
				s.append(" caster level");
			} else {
				s.append(perCL).append(" caster levels");
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

}

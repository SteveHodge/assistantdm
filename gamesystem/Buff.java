package gamesystem;

import java.util.HashMap;
import java.util.Map;

import party.Character;
import party.Creature;

public class Buff {
	public String name;
	Map<Modifier,String> modifiers = new HashMap<Modifier,String>();

	public void applyBuff(Character c) {
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
}

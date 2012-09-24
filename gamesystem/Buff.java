package gamesystem;

import java.util.HashMap;
import java.util.Map;

import party.Creature;

public class Buff {
	public String name;
	Map<Modifier,String> modifiers = new HashMap<Modifier,String>();

	public Buff(String name) {
		this.name = name;
	}

	public void applyBuff(party.Character c) {
		for (Modifier m : modifiers.keySet()) {
			Statistic[] stats;
			String target = modifiers.get(m);
			if (target.equals(Creature.STATISTIC_SAVING_THROWS)) {
				stats = new Statistic[3];
				stats[0] = c.getStatistic(Creature.STATISTIC_FORTITUDE_SAVE);
				stats[1] = c.getStatistic(Creature.STATISTIC_WILL_SAVE);
				stats[2] = c.getStatistic(Creature.STATISTIC_REFLEX_SAVE);
			} else {
				stats = new Statistic[1];
				stats[0] = c.getStatistic(target);
			}

			// add modifier to target stat
			for (Statistic s : stats) {
				s.addModifier(m);
			}
		}
	}

	public void removeBuff(party.Character c) {
		for (Modifier m : modifiers.keySet()) {
			Statistic[] stats;
			String target = modifiers.get(m);
			if (target.equals(Creature.STATISTIC_SAVING_THROWS)) {
				stats = new Statistic[3];
				stats[0] = c.getStatistic(Creature.STATISTIC_FORTITUDE_SAVE);
				stats[1] = c.getStatistic(Creature.STATISTIC_WILL_SAVE);
				stats[2] = c.getStatistic(Creature.STATISTIC_REFLEX_SAVE);
			} else {
				stats = new Statistic[1];
				stats[0] = c.getStatistic(target);
			}

			// add modifier to target stat
			for (Statistic s : stats) {
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
			
			s.append(" to ").append(target);
			s.append("<br/>");
		}
		s.append("</html></body>");
		return s.toString();
	}

	protected Buff addEffect(String target, String type, int modifier) {
		Modifier m = new ImmutableModifier(modifier, type, name);
		modifiers.put(m,target);
		return this;
	}

	public static Buff[] buffs = {
		(new Buff("Shield Other"))
			.addEffect("ac","Deflection",1)
			.addEffect("saves","Resistence",1),
		(new Buff("Resistance"))
			.addEffect("saves","Resistence",1),
		(new Buff("Mage Armor"))
			.addEffect("ac","Armor",4),
		(new Buff("Cloak of Chaos"))
			.addEffect("ac","Deflection",4)
			.addEffect("saves","Resistence",4),
		(new Buff("Holy Aura"))
			.addEffect("ac","Deflection",4)
			.addEffect("saves","Resistence",4),
		(new Buff("Shield of Law"))
			.addEffect("ac","Deflection",4)
			.addEffect("saves","Resistence",4),
		(new Buff("Unholy Aura"))
			.addEffect("ac","Deflection",4)
			.addEffect("saves","Resistence",4),
		(new Buff("Eagle's Splendor"))
			.addEffect("abilities.charisma","Enhancement",4),
		(new Buff("Bear's Endurance"))
			.addEffect("abilities.constitution","Enhancement",4),
		(new Buff("Cat's Grace"))
			.addEffect("abilities.dexterity","Enhancement",4),
		(new Buff("Fox's Cunning"))
			.addEffect("abilities.intelligence","Enhancement",4),
		(new Buff("Bull's Strength"))
			.addEffect("abilities.strength","Enhancement",4),
		(new Buff("Owl's Wisdom"))
			.addEffect("abilities.wisdom","Enhancement",4),
		(new Buff("Shield"))
			.addEffect("ac","Shield",4)
	};
}

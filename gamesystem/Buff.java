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
			
			s.append(" to ").append(Creature.STATISTIC_DESC.get(target));
			if (m.getCondition() != null) s.append(" ").append(m.getCondition());
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

	protected Buff addEffect(String target, String type, int modifier, String condition) {
		Modifier m = new ImmutableModifier(modifier, type, name, condition);
		modifiers.put(m,target);
		return this;
	}

	public static Buff[] buffs = {
		(new Buff("Shield Other"))
			.addEffect("ac","Deflection",1)
			.addEffect("saves","Resistence",1),
		(new Buff("Resistance"))
			//.addEffect("initiative","Luck",2,"vs evil")
			//.addEffect("ac","Dodge",2,"vs evil")
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
			.addEffect("ac","Shield",4),
		(new Buff("Haste"))
			//.addEffect("attacks",null,1)
			.addEffect("ac","Dodge",1)
			.addEffect("saves.reflex","Dodge",1),
		(new Buff("Slow"))
			//.addEffect("attacks",null,1)
			.addEffect("ac",null,-1) 
			.addEffect("saves.reflex",null,-1),
		(new Buff("Heroism"))
			//.addEffect("attacks","morale",2)
			.addEffect("saves","morale",2)
			.addEffect("skills","morale",2),
		(new Buff("Greater Heroism"))
			//.addEffect("attacks","morale",4)
			// +CL (max +20) temp hps
			.addEffect("saves","morale",4)
			.addEffect("skills","morale",4),
		(new Buff("Hero's Feast"))
			//.addEffect("attacks","morale",1)
			// 1d8 + 1 per 2 CL (max +10) temp hps
			.addEffect("saves.will","morale",1),
		(new Buff("Iron Body"))
			.addEffect("abilities.strength","Enhancement",6)
			// -8 armor check penalty
			.addEffect("abilities.dexterity",null,-6),	// TODO to minimum of 1
		(new Buff("Bless"))
			//.addEffect("attacks","morale",1)
			.addEffect("saves", "morale", 1, "vs fear"),
		(new Buff("Bane"))
			//.addEffect("attacks",null,-1)
			.addEffect("saves", null, -1, "vs fear"),
		(new Buff("Rage"))
			.addEffect("abilities.strength", "morale", 2)
			.addEffect("abilities.constitution", "morale", 2)
			.addEffect("saves.will", "morale", 1)
			.addEffect("ac", null, -2),

// needs caster level:
			//Divine Favor		yes, CL	+1 luck on attack, dmg per 3 cl (max +3)
			//Aid	character		+1 morale to attack, saves vs fear; 1d8+cl temporary hps (cl max +10)
			//Barkskin	character	yes, CL	+2 enhancement to na, +1 per 3 levels to max of +5
			//Shield of Faith			+2 deflections bonus to ac +1/6 levels (max +5 at 18th)
			//False Life			1d10 + 1 per cl (max +10) temp hps

// character level:
			//Tenser's Transformation		yes, bab	+4 enhancement to str, con, dex, +4 na to ac, +5 competence to fort, bab equals character level (20 max)
			//Divine Power			+6 enhancement to str, 1 temp hp per cl, base attack becomes character level (+20 max)

// multiple optional effects:
			//Prayer			+1 luck bonus on attack, dmg, saves, skills, -1 penalty on same
			//Bestow Curse			-6 decrease to ability, or -4 penalty to attacks, saves, ability checks, skill checks

// size change:
			//Animal Growth	monster		increase size one category, +8 size to str, +4 size to con, -2 size to dex, na +2, +4 resist to saves
			//Enlarge Person			increase size one category, +8 size to str, +4 size to con, -2 size to dex, na +2, +4 resist to saves
			//Righteous Might			increase size one category, +8 size to str, +4 size to con, -2 size to dex, na +2, +4 resist to saves, ac and attacks modified for new size
			//Reduce Animal	monsters		one category smaller, +2 size bonus to dex, -2 size penalty to str, +1 bonus to attacks and ac
			//Reduce Person			one category smaller, +2 size bonus to dex, -2 size penalty to str, +1 bonus to attacks and ac

	};
}

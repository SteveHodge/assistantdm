package gamesystem;

import java.util.HashSet;
import java.util.Set;

import party.Creature;

/*
 * BuffSource is a source of a Buff. It is a description of an effect of some sort in terms of game mechanics (e.g. a spell). A Buff
 * is a particular instantiation of the effect (e.g. a casting of a spell), the specific details of a Buff may differ based on factors
 * such as caster level.
 */

public class BuffFactory {
	public String name;
	Set<Effect> effects = new HashSet<Effect>();

	public boolean requiresCasterLevel() {
		for (Effect e : effects) {
			if (e instanceof CLEffect) return true;
		}
		return false;
	}

	public Buff getBuff() {
		return getBuff(0);
	}

	public Buff getBuff(int casterLevel) {
		Buff b = new Buff();
		b.name = name;
		for (Effect e : effects) {
			b.modifiers.put(e.getModifier(casterLevel),e.target);
		}
		return b;
	}


	public String getDescription() {
		StringBuilder s = new StringBuilder();
		s.append("<html><body>");
		for (Effect e : effects) {
			s.append(e).append("<br/>");
		}
		s.append("</html></body>");
		return s.toString();
	}

	protected BuffFactory(String name) {
		this.name = name;
	}

	protected BuffFactory addEffect(String target, String type, int modifier) {
		addEffect(target,type,modifier,null);
		return this;
	}

	protected BuffFactory addEffect(String target, String type, int modifier, String condition) {
		FixedEffect e = new FixedEffect();
		e.target = target;
		e.type = type;
		e.modifier = modifier;
		e.condition = condition;
		effects.add(e);
		return this;
	}

	protected BuffFactory addEffectCL(String target, String type, int baseMod, int perCL, int maxMod) {
		CLEffect e = new CLEffect();
		e.target = target;
		e.type = type;
		e.baseMod = baseMod;
		e.perCL = perCL;
		e.maxMod = maxMod;
		effects.add(e);
		return this;
	}

	protected abstract class Effect {
		String target;
		String type;
		String condition;

		public abstract Modifier getModifier(int cl);
	}

	protected class FixedEffect extends Effect {
		int modifier;

		public Modifier getModifier(int cl) {
			return new ImmutableModifier(modifier, type, name, condition);
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

	protected class CLEffect extends Effect {
		int baseMod;
		int perCL;
		int maxMod;

		public Modifier getModifier(int cl) {
			int mod = baseMod+cl/perCL;
			if (mod > maxMod) mod = maxMod;
			return new ImmutableModifier(mod, type, name, condition);
		}

		// TODO this assumes caster level based stuff is always a bonus
		public String toString() {
			StringBuilder s = new StringBuilder();

			if (type != null) s.append(" ").append(type);
			s.append(" bonus of");
			if (baseMod > 0) s.append(" +").append(baseMod);
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
	}

	public static BuffFactory[] buffs = {
		(new BuffFactory("Shield Other"))
			.addEffect("ac","Deflection",1)
			.addEffect("saves","Resistence",1),
		(new BuffFactory("Resistance"))
			.addEffect("saves","Resistence",1),
		(new BuffFactory("Mage Armor"))
			.addEffect("ac","Armor",4),
		(new BuffFactory("Cloak of Chaos"))
			.addEffect("ac","Deflection",4)
			.addEffect("saves","Resistence",4),
		(new BuffFactory("Holy Aura"))
			.addEffect("ac","Deflection",4)
			.addEffect("saves","Resistence",4),
		(new BuffFactory("Shield of Law"))
			.addEffect("ac","Deflection",4)
			.addEffect("saves","Resistence",4),
		(new BuffFactory("Unholy Aura"))
			.addEffect("ac","Deflection",4)
			.addEffect("saves","Resistence",4),
		(new BuffFactory("Eagle's Splendor"))
			.addEffect("abilities.charisma","Enhancement",4),
		(new BuffFactory("Bear's Endurance"))
			.addEffect("abilities.constitution","Enhancement",4),
		(new BuffFactory("Cat's Grace"))
			.addEffect("abilities.dexterity","Enhancement",4),
		(new BuffFactory("Fox's Cunning"))
			.addEffect("abilities.intelligence","Enhancement",4),
		(new BuffFactory("Bull's Strength"))
			.addEffect("abilities.strength","Enhancement",4),
		(new BuffFactory("Owl's Wisdom"))
			.addEffect("abilities.wisdom","Enhancement",4),
		(new BuffFactory("Shield"))
			.addEffect("ac","Shield",4),
		(new BuffFactory("Haste"))
			.addEffect("attacks",null,1)
			.addEffect("ac","Dodge",1)
			.addEffect("saves.reflex","Dodge",1),
		(new BuffFactory("Slow"))
			.addEffect("attacks",null,-1)
			.addEffect("ac",null,-1) 
			.addEffect("saves.reflex",null,-1),
		(new BuffFactory("Heroism"))
			.addEffect("attacks","Morale",2)
			.addEffect("saves","Morale",2)
			.addEffect("skills","Morale",2),
		(new BuffFactory("Greater Heroism"))
			.addEffect("attacks","Morale",4)
			.addEffect("saves","Morale",4)
			.addEffect("skills","Morale",4)
			.addEffectCL("hps", null, 0, 1, 20),		// +1/CL (max +20) temp hps
		(new BuffFactory("Hero's Feast"))
			.addEffect("attacks","Morale",1)
			// 1d8 + 1/2 CL (max +10) temp hps
			.addEffect("saves.will","Morale",1),
		(new BuffFactory("Iron Body"))
			.addEffect("abilities.strength","Enhancement",6)
			// -8 armor check penalty
			.addEffect("abilities.dexterity",null,-6),	// TODO to minimum of 1
		(new BuffFactory("Bless"))
			.addEffect("attacks","Morale",1)
			.addEffect("saves", "Morale", 1, "vs fear"),
		(new BuffFactory("Bane"))
			.addEffect("attacks",null,-1)
			.addEffect("saves", null, -1, "vs fear"),
		(new BuffFactory("Rage"))
			.addEffect("abilities.strength", "Morale", 2)
			.addEffect("abilities.constitution", "Morale", 2)
			.addEffect("saves.will", "Morale", 1)
			.addEffect("ac", null, -2),
		(new BuffFactory("Shield of Faith"))
			.addEffectCL("ac", "Deflection", 2, 6, 5),			// 2+CL/6 deflection bonus to ac (max +5 at 18th)
		(new BuffFactory("Divine Favor"))
			.addEffectCL("attacks", "Luck", 0, 3, 3),			// CL/3 luck on attack, dmg (min 1, max 3)
		(new BuffFactory("Aid"))
			.addEffect("attacks","Morale",1)
			//Aid	+1 morale to attack, saves vs fear; 1d8+CL temporary hps (cl max +10)
			.addEffect("saves.will","Morale",1,"vs fear")

// needs caster level:
			//Barkskin	1+CL/3 enhancement to na, (min +2, max of +5) * note enhancement to NA bonus, i.e NA is a Statistic (much like armor bonus, shield bonus). but can treat as type "Natural Armor Ehancement"
			//False Life 1d10+CL (max +10) temp hps

// character level:
			//Tenser's Transformation		+4 enhancement to str, con, dex, +4 na to ac, +5 competence to fort, bab equals character level (20 max)
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

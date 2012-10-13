package gamesystem;

import gamesystem.Buff.Effect;

import java.util.HashSet;
import java.util.Set;


/*
 * BuffFactory is a source of a Buff. It is a description of an effect of some sort in terms of game mechanics (e.g. a spell).
 */

public class BuffFactory {
	public String name;
	Set<Effect> effects = new HashSet<Effect>();

	public Buff getBuff() {
		Buff b = new Buff();
		b.name = name;
		b.effects.addAll(effects);
		return b;
	}

	public String toString() {
		return name;
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
		Buff.FixedEffect e = new Buff.FixedEffect();
		e.target = target;
		e.type = type;
		e.modifier = modifier;
		e.condition = condition;
		effects.add(e);
		return this;
	}

	// basic formula is: modifier = baseMod + 1/perCL
	// 1/perCL is limited to max
	// the total modifier must be at least 1
	// baseMod should be either an Integer or a Dice
	protected BuffFactory addEffectCL(String target, String type, Object baseMod, int perCL, int max) {
		Buff.CLEffect e = new Buff.CLEffect();
		e.target = target;
		e.type = type;
		e.baseMod = baseMod;
		e.perCL = perCL;
		e.maxPerCL = max;
		effects.add(e);
		return this;
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
			.addEffectCL("hps", null, 0, 1, 20),					// +1/CL (max +20) temp hps
		(new BuffFactory("Hero's Feast"))
			.addEffect("attacks","Morale",1)
			.addEffectCL("hps", null, new SimpleDice(8), 2, 10)		// 1d8 + 1/2 CL (max +10) temp hps
			.addEffect("saves.will","Morale",1),
		(new BuffFactory("Iron Body"))
			.addEffect("abilities.strength","Enhancement",6)
			// -8 armor check penalty
			.addEffect("abilities.dexterity",null,-6),	// TODO to a minimum dex of 1
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
			.addEffectCL("ac", "Deflection", 2, 6, 3),				// 2+CL/6 deflection bonus to ac (max +5 at 18th)
		(new BuffFactory("Divine Favor"))
			.addEffectCL("attacks", "Luck", 0, 3, 3),				// CL/3 luck on attack, dmg (min 1, max 3)
		(new BuffFactory("Aid"))
			.addEffect("attacks","Morale",1)
			.addEffectCL("hps", null, new SimpleDice(8), 1, 10)		// 1d8+CL temporary hps (cl max +10)
			.addEffect("saves.will","Morale",1,"vs fear"),
		(new BuffFactory("False Life"))
			.addEffectCL("hps", null, new SimpleDice(10), 1, 10)	// 1d10+CL (max +10) temp hps
		//(new BuffFactory("Ray of Enfeeblement"))
			//.addEffectCL("abilities.strength", null, new SimpleDice(6), 2, 5)	// TODO needs to penalty, min of 1

// needs caster level:
			//Barkskin	1+CL/3 enhancement to na, (min +2, max of +5) * note enhancement to NA bonus, i.e NA is a Statistic (much like armor bonus, shield bonus). but can treat as type "Natural Armor Ehancement"

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


//Death Knell	+2 bonus to Str, 1d8 temp hps, effective caster lvl +1
//Dispel Evil	+4 deflection bonus to AC by evil
//Dispel Good	+4 deflection bonus to AC by good
//Find Traps	+1/2 cl insight bonus to Search (for traps)
//Foresight		+2 insight to AC and reflex (lost when flat footed)
//Glibness		+30 bonus to bluff (to convince another of the truth of your words)
//Glitterdust	-40 penalty to hide
//Good Hope		+2 morale bonus to saves, attacks, ability checks, skill checks, weapon damage
//Crushing Dispair -2 penalty to saves, attacks, ability checks, skill checks, weapon damage
//Jump			+10 enhancement bonus to jump, +20 at cl 5, +30 at cl 9
//Longstrider	+10ft enhancement bonus to speed
//Protection from Evil/Good/Chaos/Law			+2 deflection bonus to ac vs evil, +2 resistence bonus on saves vs evil
//Magic Circle against Evil/Good/Chaos/Law		+2 deflection bonus to ac vs evil, +2 resistence bonus on saves vs evil
//Mind Fog		-10 comptence penalty to wisdom checks, will saves
//Otto's Irresistable Dance		-4 penalty to ac, -10 penalty to reflex saves, negate ac bonus of shield
//Protection from Spells	+8 resistence bonus to saves vs spells and spell-like effects
//Remove Fear	+4 morale bonus to saves vs fear
//Tree Shape	+10 natural armor bonus, effective dex of 0, spped of 0
//Touch of Idiocy	1d6 penalty to wisdom, intelligence, charisma (to minimum 1)
//Symbol of Pain	-4 penalty to attack rolls, skill checks, ability checks

	};
}

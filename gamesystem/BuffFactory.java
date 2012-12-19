package gamesystem;

import gamesystem.Buff.Effect;
import gamesystem.dice.HDDice;
import party.Creature;

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
		return addEffect(target,type,modifier,null);
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

	protected BuffFactory addPenalty(String target, String type, Object baseMod, int perCL, int max) {
		return addPenalty(target, type, baseMod, perCL, max, null);
	}

	protected BuffFactory addPenalty(String target, String type, Object baseMod, int perCL, int max, String condition) {
		Buff.CLEffect e = new Buff.CLEffect();
		e.target = target;
		e.type = type;
		e.baseMod = baseMod;
		e.perCL = perCL;
		e.maxPerCL = max;
		e.condition = condition;
		e.penalty = true;
		effects.add(e);
		return this;
	}

	protected BuffFactory addBonus(String target, String type, Object baseMod, int perCL, int max) {
		return addBonus(target, type, baseMod, perCL, max, null);
	}

	// basic formula is: modifier = baseMod + 1/perCL
	// 1/perCL is limited to max
	// the total modifier must be at least 1
	// baseMod should be either an Integer or a Dice
	protected BuffFactory addBonus(String target, String type, Object baseMod, int perCL, int max, String condition) {
		Buff.CLEffect e = new Buff.CLEffect();
		e.target = target;
		e.type = type;
		e.baseMod = baseMod;
		e.perCL = perCL;
		e.maxPerCL = max;
		e.condition = condition;
		effects.add(e);
		return this;
	}

	protected BuffFactory addPropertyChange(String target, String property, Object value, String description) {
		Buff.PropertyEffect e = new Buff.PropertyEffect();
		e.target = target;
		e.property = property;
		e.value = value;
		e.description = description;
		effects.add(e);
		return this;
	}

	public static BuffFactory[] buffs = {
		(new BuffFactory("Shield Other"))
			.addEffect(Creature.STATISTIC_AC,"Deflection",1)
			.addEffect(Creature.STATISTIC_SAVING_THROWS,"Resistence",1),
		(new BuffFactory("Resistance"))
			.addEffect(Creature.STATISTIC_SAVING_THROWS,"Resistence",1),
		(new BuffFactory("Mage Armor"))
			.addEffect(Creature.STATISTIC_AC,"Armor",4),
		(new BuffFactory("Cloak of Chaos"))
			.addEffect(Creature.STATISTIC_AC,"Deflection",4)
			.addEffect(Creature.STATISTIC_SAVING_THROWS,"Resistence",4),
		(new BuffFactory("Holy Aura"))
			.addEffect(Creature.STATISTIC_AC,"Deflection",4)
			.addEffect(Creature.STATISTIC_SAVING_THROWS,"Resistence",4),
		(new BuffFactory("Shield of Law"))
			.addEffect(Creature.STATISTIC_AC,"Deflection",4)
			.addEffect(Creature.STATISTIC_SAVING_THROWS,"Resistence",4),
		(new BuffFactory("Unholy Aura"))
			.addEffect(Creature.STATISTIC_AC,"Deflection",4)
			.addEffect(Creature.STATISTIC_SAVING_THROWS,"Resistence",4),
		(new BuffFactory("Eagle's Splendor"))
			.addEffect(Creature.STATISTIC_CHARISMA,"Enhancement",4),
		(new BuffFactory("Bear's Endurance"))
			.addEffect(Creature.STATISTIC_CONSTITUTION,"Enhancement",4),
		(new BuffFactory("Cat's Grace"))
			.addEffect(Creature.STATISTIC_DEXTERITY,"Enhancement",4),
		(new BuffFactory("Fox's Cunning"))
			.addEffect(Creature.STATISTIC_INTELLIGENCE,"Enhancement",4),
		(new BuffFactory("Bull's Strength"))
			.addEffect(Creature.STATISTIC_STRENGTH,"Enhancement",4),
		(new BuffFactory("Owl's Wisdom"))
			.addEffect(Creature.STATISTIC_WISDOM,"Enhancement",4),
		(new BuffFactory("Shield"))
			.addEffect(Creature.STATISTIC_AC,"Shield",4),
		(new BuffFactory("Haste"))
			.addEffect(Creature.STATISTIC_ATTACKS,null,1)
			.addPropertyChange(Creature.STATISTIC_ATTACKS,"extra_attacks",1,"one extra attack at highest bonus")
			.addEffect(Creature.STATISTIC_AC,"Dodge",1)
			.addEffect(Creature.STATISTIC_REFLEX_SAVE,"Dodge",1),
		(new BuffFactory("Slow"))
			.addEffect(Creature.STATISTIC_ATTACKS,null,-1)
			.addEffect(Creature.STATISTIC_AC,null,-1) 
			.addEffect(Creature.STATISTIC_REFLEX_SAVE,null,-1),
		(new BuffFactory("Heroism"))
			.addEffect(Creature.STATISTIC_ATTACKS,"Morale",2)
			.addEffect(Creature.STATISTIC_SAVING_THROWS,"Morale",2)
			.addEffect(Creature.STATISTIC_SKILLS,"Morale",2),
		(new BuffFactory("Greater Heroism"))
			.addEffect(Creature.STATISTIC_ATTACKS,"Morale",4)
			.addEffect(Creature.STATISTIC_SAVING_THROWS,"Morale",4)
			.addEffect(Creature.STATISTIC_SKILLS,"Morale",4)
			.addBonus(Creature.STATISTIC_HPS, null, 0, 1, 20),					// +1/CL (max +20) temp hps
		(new BuffFactory("Hero's Feast"))
			.addEffect(Creature.STATISTIC_ATTACKS,"Morale",1)
			.addBonus(Creature.STATISTIC_HPS, null, new HDDice(8), 2, 10)		// 1d8 + 1/2 CL (max +10) temp hps
			.addEffect(Creature.STATISTIC_WILL_SAVE,"Morale",1),
		(new BuffFactory("Iron Body"))
			.addEffect(Creature.STATISTIC_STRENGTH,"Enhancement",6)
			// TODO -8 armor check penalty
			.addEffect(Creature.STATISTIC_DEXTERITY,null,-6),	// TODO to a minimum dex of 1
		(new BuffFactory("Bless"))
			.addEffect(Creature.STATISTIC_ATTACKS,"Morale",1)
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Morale", 1, "vs fear"),
		(new BuffFactory("Bane"))
			.addEffect(Creature.STATISTIC_ATTACKS,null,-1)
			.addEffect(Creature.STATISTIC_SAVING_THROWS, null, -1, "vs fear"),
		(new BuffFactory("Rage"))
			.addEffect(Creature.STATISTIC_STRENGTH, "Morale", 2)
			.addEffect(Creature.STATISTIC_CONSTITUTION, "Morale", 2)
			.addEffect(Creature.STATISTIC_WILL_SAVE, "Morale", 1)
			.addEffect(Creature.STATISTIC_AC, null, -2),
		(new BuffFactory("Shield of Faith"))
			.addBonus(Creature.STATISTIC_AC, "Deflection", 2, 6, 3),				// 2+CL/6 deflection bonus to ac (max +5 at 18th)
		(new BuffFactory("Divine Favor"))
			.addBonus(Creature.STATISTIC_ATTACKS, "Luck", 0, 3, 3)				// CL/3 luck on attack, dmg (min 1, max 3)
			.addBonus(Creature.STATISTIC_DAMAGE, "Luck", 0, 3, 3),				// CL/3 luck on attack, dmg (min 1, max 3)
		(new BuffFactory("Aid"))
			.addEffect(Creature.STATISTIC_ATTACKS,"Morale",1)
			.addBonus(Creature.STATISTIC_HPS, null, new HDDice(8), 1, 10)		// 1d8+CL temporary hps (cl max +10)
			.addEffect(Creature.STATISTIC_WILL_SAVE,"Morale",1,"vs fear"),
		(new BuffFactory("False Life"))
			.addBonus(Creature.STATISTIC_HPS, null, new HDDice(10), 1, 10),	// 1d10+CL (max +10) temp hps
		(new BuffFactory("Death Knell"))
			.addEffect(Creature.STATISTIC_STRENGTH,null,2)
			//effective caster lvl +1
			.addBonus(Creature.STATISTIC_HPS, null, new HDDice(8), 0, 0),
		(new BuffFactory("Dispel Evil"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 4, "vs evil"),
		(new BuffFactory("Dispel Good"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 4, "vs good"),
		(new BuffFactory("Find Traps"))
			.addBonus(Creature.STATISTIC_SKILLS+".Search", "Insight", 0, 2, 10, "to find traps"),
		(new BuffFactory("Foresight"))
			.addEffect(Creature.STATISTIC_AC, "Insight", 2)
			.addEffect(Creature.STATISTIC_REFLEX_SAVE, "Insight", 2),				//(lost when flat footed)
		(new BuffFactory("Protection from Evil"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 2, "vs evil")
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Resistance", 2, "vs evil"),
		(new BuffFactory("Protection from Good"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 2, "vs good")
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Resistance", 2, "vs good"),
		(new BuffFactory("Protection from Chaos"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 2, "vs chaotic")
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Resistance", 2, "vs chaotic"),
		(new BuffFactory("Protection from Law"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 2, "vs lawful")
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Resistance", 2, "vs lawful"),
		(new BuffFactory("Magic Circle against Evil"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 2, "vs evil")
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Resistance", 2, "vs evil"),
		(new BuffFactory("Magic Circle against Good"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 2, "vs good")
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Resistance", 2, "vs good"),
		(new BuffFactory("Magic Circle against Chaos"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 2, "vs chaotic")
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Resistance", 2, "vs chaotic"),
		(new BuffFactory("Magic Circle against Law"))
			.addEffect(Creature.STATISTIC_AC, "Deflection", 2, "vs lawful")
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Resistance", 2, "vs lawful"),
		(new BuffFactory("Mind Fog"))
			//-10 comptence penalty to wisdom checks
			.addEffect(Creature.STATISTIC_WILL_SAVE, "Competence", -10),
		(new BuffFactory("Otto's Irresistable Dance"))
			.addEffect(Creature.STATISTIC_AC, null, -4)
			//negate ac bonus of shield
			.addEffect(Creature.STATISTIC_REFLEX_SAVE, null, -10),
		(new BuffFactory("Protection from Spells"))
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Resistance", 8, "vs spells and spell-like effects"),
		(new BuffFactory("Remove Fear"))
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Morale", 4, "vs fear"),
		(new BuffFactory("Ray of Enfeeblement"))
			.addPenalty(Creature.STATISTIC_STRENGTH, null, new HDDice(6), 2, 5),	// to min str of 1
		(new BuffFactory("Prayer (from friend)"))
			.addEffect(Creature.STATISTIC_ATTACKS, "Luck", 1)
			.addEffect(Creature.STATISTIC_DAMAGE, "Luck", 1)
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Luck", 1)
			.addEffect(Creature.STATISTIC_SKILLS, "Luck", 1),
		(new BuffFactory("Prayer (from foe)"))
			.addEffect(Creature.STATISTIC_ATTACKS, null, -1)
			.addEffect(Creature.STATISTIC_DAMAGE, null, -1)
			.addEffect(Creature.STATISTIC_SAVING_THROWS, null, -1)
			.addEffect(Creature.STATISTIC_SKILLS, null, -1),
		(new BuffFactory("Good Hope"))
			.addEffect(Creature.STATISTIC_SAVING_THROWS, "Morale", 2)
			.addEffect(Creature.STATISTIC_ATTACKS, "Morale", 2)
			.addEffect(Creature.STATISTIC_DAMAGE, "Morale", 2)
			.addEffect(Creature.STATISTIC_SKILLS, "Morale", 2),
		(new BuffFactory("Crushing Dispair"))
			.addEffect(Creature.STATISTIC_SAVING_THROWS, null, -2)
			.addEffect(Creature.STATISTIC_ATTACKS, null, -2)
			.addEffect(Creature.STATISTIC_DAMAGE, null, -2)
			.addEffect(Creature.STATISTIC_SKILLS, null, -2),
		(new BuffFactory("Touch of Idiocy"))
			.addPenalty(Creature.STATISTIC_INTELLIGENCE, null, new HDDice(6), 0, 6)	// to min of 1
			.addPenalty(Creature.STATISTIC_WISDOM, null, new HDDice(6), 0, 6)	// to min of 1
			.addPenalty(Creature.STATISTIC_CHARISMA, null, new HDDice(6), 0, 6),	// to min of 1
		(new BuffFactory("Enlarge Person"))
			// increase size category
			.addEffect(Creature.STATISTIC_STRENGTH,"Size",2)
			.addEffect(Creature.STATISTIC_DEXTERITY,"Size",-2)
			.addEffect(Creature.STATISTIC_AC,"Size",-1)
			.addEffect(Creature.STATISTIC_ATTACKS,"Size",-1),
		(new BuffFactory("Reduce Person"))
			// reduce size category
			.addEffect(Creature.STATISTIC_STRENGTH,"Size",-2)
			.addEffect(Creature.STATISTIC_DEXTERITY,"Size",2)
			.addEffect(Creature.STATISTIC_AC,"Size",1)
			.addEffect(Creature.STATISTIC_ATTACKS,"Size",1),
		(new BuffFactory("Righteous Might"))
			// increase size category
			.addEffect(Creature.STATISTIC_STRENGTH,"Size",4)
			.addEffect(Creature.STATISTIC_CONSTITUTION,"Size",2)
			.addEffect(Creature.STATISTIC_NATURAL_ARMOR,"Enhancement",2)
			// damage reduction
			.addEffect(Creature.STATISTIC_AC,"Size",-1)			// note: spell says use modifier for new size
			.addEffect(Creature.STATISTIC_ATTACKS,"Size",-1),	// note: spell says use modifier for new size
		(new BuffFactory("Divine Power"))
			//base attack becomes character level (+20 max)
			.addEffect(Creature.STATISTIC_STRENGTH,"Enhancement",6)
			.addBonus(Creature.STATISTIC_HPS, null, 0, 1, 20),		// +CL temporary hps (cl max +20)
		(new BuffFactory("Glibness"))
			.addEffect(Creature.STATISTIC_SKILLS+".Bluff", null, 30, "to convince others you speak the truth"),
		(new BuffFactory("Glitterdust"))
			.addEffect(Creature.STATISTIC_SKILLS+".Hide", null, -40),
		(new BuffFactory("Symbol of Pain"))
			.addEffect(Creature.STATISTIC_ATTACKS, null, -4)
			.addEffect(Creature.STATISTIC_SKILLS, null, -4),
			//.addEffect(Creature.STATISTIC_ABILITY_CHECKS, null, -4)
		(new BuffFactory("Magic Vestment (armor)"))
			.addBonus(Creature.STATISTIC_ARMOR, "Enhancement", 0, 4, 5),		// +1/4 CL (max +5)
		(new BuffFactory("Magic Vestment (shield)"))
			.addBonus(Creature.STATISTIC_SHIELD, "Enhancement", 0, 4, 5),		// +1/4 CL (max +5)
		(new BuffFactory("Barkskin"))
			.addBonus(Creature.STATISTIC_NATURAL_ARMOR, "Enhancement", 1, 3, 4),	// 1+CL/3 enhancement to na, (min +2, max of +5)
		(new BuffFactory("Tenser's Transformation"))
			.addEffect(Creature.STATISTIC_STRENGTH, "Enhancement", 4)
			.addEffect(Creature.STATISTIC_CONSTITUTION, "Enhancement", 4)
			.addEffect(Creature.STATISTIC_DEXTERITY, "Enhancement", 4)
			.addEffect(Creature.STATISTIC_FORTITUDE_SAVE, "Competence", 5)
			// bab equals character level (20 max), also proficiency with simple and martial weapons
			.addEffect(Creature.STATISTIC_AC, "Natural Armor", 4),	// TODO it's not clear if this should stack with existing NA or not. it's not clear if enhancements to NA should apply to Tenser's Transformation if other NA is not present...
			// I think TT NA bonus should overlap with other NA bonus (such as racial NA), and should be enhanceable. this would perhaps be easiest to implement as a temp score on NA  

			//Jump			+10 enhancement bonus to jump, +20 at cl 5, +30 at cl 9
			//Longstrider	+10ft enhancement bonus to speed
			//Tree Shape	+10 natural armor bonus, effective dex of 0, speed of 0

// buff to items/attacks, or create new attacks:
			//magic fang	+1 enhancement bonus to attack and dmg to one weapon (unarmed strike or natural weapon)
			//magic weapon	+1 enhancement bonus to attack and dmg to one weapon (not unarmed strike or natural weapon except for monk)
			//magic weapon, greater	+1/4 CL enhancement bonus to attack and dmg to one weapon (not unarmed strike or natural weapon except for monk) or to up to 50 ammo
			//magic stone	+1 enhancement bonus to attack and dmg to up to 3 pebbles/sling bullets 1d6+1 dmg, 2d6+2 v undead (including enhancement bonus)
			//align weapon	make weapon or up to 50 ammo lawful, good, chaotic, or evil
			//shillelagh	+1 enhancement bonus to attack and dmg for one club or quarterstaff, plus increase damage as if 2 sizes larger 
			//magic fang, greater	+1/4 CL enhancement bonus to weapon (unarmed strike or natural weapon) or +1 to all natural weapons
			//spritual weapon	attack at BAB+wisdom (allows multiple attacks). feats not applied. 1d8 + 1/3CL damage
			//bigby's hand spells
			//bless weapon

// multiple optional effects:
			//Bestow Curse			-6 decrease to ability, or -4 penalty to attacks, saves, ability checks, skill checks

// size change:
			//Animal Growth	monster		increase size one category, +8 size to str, +4 size to con, -2 size to dex, na +2, +4 resist to saves
			//Reduce Animal	monsters		one category smaller, +2 size bonus to dex, -2 size penalty to str, +1 bonus to attacks and ac

//conditions
//blinded -2 ac, lose dex bonus to ac, move at half speed, -4 to search and most strength and dex skills, visual skills fail automatically (spot, reading etc)
//cowering -2 ac, lose dex bonus to ac
//dazzled -1 attack, search, spot
//deafened -4 initiative fail listen, 20% chance of spell failure for verbal spells
//energy drain (per level) -1 attack, saves, skills, ability checks, loss of 5 hp, -1 effective level, spell lose
//entangled move at half speed, -2 to attack, -4 to dex, can't run or charge
//exhausted move at half speed, -6 str and dex
//fascinated -4 spot, listen
//fatigued -2 str, dex. can't run or charge
//flat-footed lose dex to ac
//frightened -2 attack, save, skills, ability checks
//panicked -2 saves, skills, ability checks
//shaken -2 attack, saves, skills, ability checks
//skickened -2 attack, damage, saves, skills, ability checks
//stunned -2 to ac, lose dex bonus to ac
	};
}

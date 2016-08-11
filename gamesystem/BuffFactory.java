package gamesystem;

/*
 * BuffFactory is a source of a Buff. It is a description of an effect of some sort in terms of game mechanics (e.g. a spell).
 */

public class BuffFactory extends FeatureDefinition<BuffFactory> implements Comparable<BuffFactory> {
	public Object source;	// source object with more details, e.g. Spell

	public Buff getBuff() {
		Buff b = new Buff();
		b.name = name;
		b.effects.addAll(effects);
		return b;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BuffFactory)) return false;
		BuffFactory b = (BuffFactory) o;
		if (!name.equals(b.name)) return false;
		if (effects.size() != b.effects.size()) return false;
		for (Effect e : effects) {
			boolean found = false;
			for (Effect be : b.effects) {
				if (e.equals(be)) {
					found = true;
					break;
				}
			}
			if (!found) return false;
		}
		return true;
	}

	public BuffFactory(String name) {
		super(name);
	}

	@Override
	public int compareTo(BuffFactory arg0) {
		return name.compareTo(arg0.name);
	}

	public BuffFactory addEffect(String target, String type, int modifier) {
		return addFixedEffect(target, type, modifier, null);
	}

	protected BuffFactory addPenalty(String target, String type, Object baseMod, int perCL, int max) {
		return addPenalty(target, type, baseMod, perCL, max, null);
	}

	protected BuffFactory addPenalty(String target, String type, Object baseMod, int perCL, int max, String condition) {
		Buff.CLEffect e = new Buff.CLEffect();
		e.target = target;
		if (type != null && type.length() > 0) e.type = type;
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
		if (type != null && type.length() > 0) e.type = type;
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

	//@formatter:off
	public static final BuffFactory AUGMENTED_SUMMONING = (new BuffFactory("Augmented Summoning"))
			.addEffect(Creature.STATISTIC_STRENGTH,"Enhancement",4)
			.addEffect(Creature.STATISTIC_CONSTITUTION,"Resistence",4);

/*
		(new BuffFactory("Iron Body"))
		.addEffect(Creature.STATISTIC_STRENGTH,"Enhancement",6)
		// TODO Iron Body: -8 armor check penalty
		.addEffect(Creature.STATISTIC_DEXTERITY,null,-6),	// TODO to a minimum dex of 1

		(new BuffFactory("Death Knell"))
		.addEffect(Creature.STATISTIC_STRENGTH,null,2)
		//effective caster lvl +1
		.addBonus(Creature.STATISTIC_HPS, null, new HDDice(8), 0, 0),

		(new BuffFactory("Otto's Irresistible Dance"))
		.addEffect(Creature.STATISTIC_AC, null, -4)
		//negate ac bonus of shield
		.addEffect(Creature.STATISTIC_REFLEX_SAVE, null, -10),

		(new BuffFactory("Ray of Enfeeblement"))
		.addPenalty(Creature.STATISTIC_STRENGTH, null, new HDDice(6), 2, 5),	// to min str of 1

		(new BuffFactory("Touch of Idiocy"))
		.addPenalty(Creature.STATISTIC_INTELLIGENCE, null, new HDDice(6), 0, 6)	// to min of 1
		.addPenalty(Creature.STATISTIC_WISDOM, null, new HDDice(6), 0, 6)	// to min of 1
		.addPenalty(Creature.STATISTIC_CHARISMA, null, new HDDice(6), 0, 6),	// to min of 1

		(new BuffFactory("Righteous Might"))
		.addEffect(Creature.STATISTIC_SIZE,"Enhancement",1)
		.addEffect(Creature.STATISTIC_STRENGTH,"Size",4)
		.addEffect(Creature.STATISTIC_CONSTITUTION,"Size",2)
		.addEffect(Creature.STATISTIC_NATURAL_ARMOR,"Enhancement",2)
		// damage reduction
		.addEffect(Creature.STATISTIC_AC,"Size",-1)			// note: spell says use modifier for new size
		.addEffect(Creature.STATISTIC_ATTACKS,"Size",-1),	// note: spell says use modifier for new size

		(new BuffFactory("Symbol of Pain"))
		.addEffect(Creature.STATISTIC_ATTACKS, null, -4)
		.addEffect(Creature.STATISTIC_SKILLS, null, -4),
		//.addEffect(Creature.STATISTIC_ABILITY_CHECKS, null, -4)

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
 */
}

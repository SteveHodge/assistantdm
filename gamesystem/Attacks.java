package gamesystem;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import gamesystem.core.OverridableProperty;
import gamesystem.core.PropertyCollection;
import gamesystem.dice.CombinedDice;
import party.Character;

// TODO attack form specific combat options (+DOM)
// TODO implement modifier particular to one attack mode (ranged/grapple etc) (note that grapple size modifier is different from regular attack size modifier)
// TODO consider if it is worth including situational modifiers (e.g. flanking, squeezing, higher, shooting into combat etc)
// TODO cleanup AttackForm - hide public fields and update modifier type methods
// TODO need to listen for changes to attacks

/*
 * Statistics provided:
 * Attacks - the melee attack statistic
 * Attacks.damageStat - stat for collecting damage specific modifiers
 * .AttackForm - a particular attack mode
 * .AttackForm.damageStat - stat for collecting damage specific modifiers
 * Properties provided:
 * extra_attacks - additional attacks at top attack bonus
 * base_attack_bonus - TODO
 */

public class Attacks extends Statistic {
	private OverridableProperty<Integer> bab;
	private Modifier strMod;	// may be null (if the creature has no strength score)
	private Modifier dexMod;	// may be null (if the creature has no dex score)
	private Creature creature;
	private AC ac;
	private Set<AttackForm> attackForms = new HashSet<>();
	Modifier powerAttack = null;
	Modifier combatExpertise = null;
	private Modifier combatExpertiseAC = null;	// assumed to be null/not-null in sync with combatExpertise
	boolean isTotalDefense = false;
	private Modifier totalDefenseAC = new ImmutableModifier(4, "Dodge", "Total defense");
	boolean isFightingDefensively = false;
	private Modifier fightingDefensively = new ImmutableModifier(-4, null, "Fighting defensively");
	private Modifier fightingDefensivelyAC = new ImmutableModifier(2, "Dodge", "Fighting defensively");
	Statistic damageStat;

	final PropertyChangeListener listener = evt -> fireEvent();

	final ListDataListener featsListener = new ListDataListener() {
		@Override
		public void contentsChanged(ListDataEvent e) {update();}
		@Override
		public void intervalAdded(ListDataEvent e) {update();}
		@Override
		public void intervalRemoved(ListDataEvent e) {update();}

		protected void update() {
			setPowerAttack(getPowerAttack());
			setCombatExpertise(getCombatExpertise());
			for (AttackForm a : attackForms) {
				a.updateModifiers();
			}
			fireEvent();	// may have been fired by the power attack or combate expertise, but maybe not
		}
	};

	// FIXME monsters have feats now so clean up feats handling
	public Attacks(Creature c) {
		super("attacks", "Attacks", c);

		damageStat = new Damage(parent);

		creature = c;
		if (creature instanceof Character) {
			Character chr = (Character) creature;
			chr.feats.addListDataListener(featsListener);
		}
		ac = creature.getACStatistic();
		bab = creature.getBAB();
		bab.addPropertyListener((source, oldValue) -> {
			fireEvent();	// may have been fired by the power attack or combate expertise, but maybe not
		});

		strMod = creature.getAbilityModifier(AbilityScore.Type.STRENGTH);
		if (strMod != null) strMod.addPropertyChangeListener(listener);

		dexMod = creature.getAbilityModifier(AbilityScore.Type.DEXTERITY);
		if (dexMod != null) dexMod.addPropertyChangeListener(listener);

		addModifier(c.getSizeStatistic().getSizeModifier());
	}

	@Override
	protected void fireEvent() {
		super.fireEvent();
		for (AttackForm a : attackForms) {
			// TODO get rid of this eventually
			a.refireEvent();
		}
	}

	@Override
	protected void fireEvent(Integer oldVal) {
		super.fireEvent(oldVal);
		for (AttackForm a : attackForms) {
			// TODO get rid of this eventually
			a.refireEvent();
		}
	}

	protected class ExtraAttacks {
		Object key;
		String source;
		int attacks;
	}

	protected List<ExtraAttacks> extraAttacks = new ArrayList<>();

	public int getExtraAttacks() {
		HashMap<String, Integer> attacks = new HashMap<>();
		for (ExtraAttacks atk : extraAttacks) {
			attacks.put(atk.source, atk.attacks);
		}

		int total = 0;
		for (Integer v : attacks.values()) {
			total += v;
		}
		return total;
	}

	// "extra_attacks" - integer value - number of extra attacks at the highest base attack bonus
	// FIXME "extra_attacks" should be a property
	@Override
	public void setProperty(String property, Object value, String source, Object key) {
		if (property == null || !property.equals("extra_attacks")) return;
		//int old = getExtraAttacks();
		ExtraAttacks atk = new ExtraAttacks();
		atk.key = key;
		atk.source = source;
		atk.attacks = (Integer)value;
		extraAttacks.add(atk);
		fireEvent();
	}

	@Override
	public void resetProperty(String property, Object key) {
		if (property == null || !property.equals("extra_attacks")) return;
//		int old = getExtraAttacks();
		Iterator<ExtraAttacks> iter = extraAttacks.iterator();
		while (iter.hasNext()) {
			ExtraAttacks atk = iter.next();
			if (atk.key.equals(key)) {
				iter.remove();
			}
		}
		fireEvent();
	}

	// TODO remove acccess to BAB via attack - used only by CharacterSheetView
	public OverridableProperty<Integer> getBAB() {
		return bab;
	}

	// returns the str-modified ("melee") statistic
	@Override
	public Integer getValue() {
		return bab.getValue() + getModifiersTotal();
	}

	@Override
	public Set<Modifier> getModifierSet() {
		Set<Modifier> mods = new HashSet<>();
		mods.addAll(modifiers);
		if (strMod != null) {
			mods.add(strMod);
		} else if (dexMod != null) {
			mods.add(dexMod);	// creatures with no strength score add dex mod to attack
		}
		if (powerAttack != null) mods.add(powerAttack);
		return mods;
	}

	private Set<Modifier> getRangedMods() {
		Set<Modifier> mods = new HashSet<>();
		mods.addAll(modifiers);
		if (dexMod != null) mods.add(dexMod);
		return mods;
	}

	public int getRangedValue() {
		return bab.getValue() + getModifiersTotal(getRangedMods(), null);
	}

	public Map<Modifier,Boolean> getRangedModifiers() {
		return getModifiers(getRangedMods());
	}

	public int getRangedModifiersTotal(String... excluding) {
		return getModifiersTotalExcluding(getRangedMods(), excluding);
	}

	public AttackForm addAttackForm(String name) {
		AttackForm a = new AttackForm(name, getParent());
		attackForms.add(a);
		return a;
	}

	public void removeAttackForm(AttackForm a) {
		attackForms.remove(a);
	}

	// --------------------- combat options -----------------
	// sets up the new power attack modifier if the value has changed.
	// note it doesn't add the modifier, instead the modifier is included in getValue() and AttackForms for melee attacks
	// TODO bounds checking?
	public void setPowerAttack(int value) {
		if (!creature.hasFeat(Feat.FEAT_POWER_ATTACK)) value = 0;

		if (powerAttack == null && value == 0) return;
		if (powerAttack != null && powerAttack.getModifier() == value) return;

		if (value == 0) {
			powerAttack = null;
		} else {
			powerAttack = new ImmutableModifier(-value,null,"Power attack");
		}
		fireEvent();
	}

	// returns the amount of power attack applied (0 to BAB inclusive)
	public int getPowerAttack() {
		if (powerAttack == null) return 0;
		return -powerAttack.getModifier();	// note the modifier value is negative
	}

	// combat expertise applies to all attacks so we add it as a modifier
	// TODO bounds checking?
	public void setCombatExpertise(int value) {
		if (!creature.hasFeat(Feat.FEAT_COMBAT_EXPERTISE)) value = 0;

		if (combatExpertise == null && value == 0) return;	// unchanged value

		if (combatExpertise != null) {
			if (combatExpertise.getModifier() == value) return;	// unchanged value
			removeModifier(combatExpertise);
			ac.removeModifier(combatExpertiseAC);
			combatExpertise = null;
		}

		if (value > 0) {
			combatExpertise = new ImmutableModifier(-value,null,"Combat Expertise");
			addModifier(combatExpertise);
			combatExpertiseAC = new ImmutableModifier(value,"Dodge","Combat Expertise");
			ac.addModifier(combatExpertiseAC);
		}

		//firePropertyChange("value", null, getValue());	// will be fired by when the modifier is changed
	}

	// returns the amount of combat expertise applied (i.e. 0 to 5 inclusive)
	public int getCombatExpertise() {
		if (combatExpertise == null) return 0;
		return -combatExpertise.getModifier();
	}

	public void setFightingDefensively(boolean enabled) {
		if (enabled == isFightingDefensively) return;	// value is unchanged

		isFightingDefensively = enabled;
		if (isFightingDefensively) {
			addModifier(fightingDefensively);
			ac.addModifier(fightingDefensivelyAC);
		} else {
			removeModifier(fightingDefensively);
			ac.removeModifier(fightingDefensivelyAC);
		}
	}

	public boolean isFightingDefensively() {
		return isFightingDefensively;
	}

	public void setTotalDefense(boolean enabled) {
		if (enabled == isTotalDefense) return;	// value is unchanged

		isTotalDefense = enabled;
		if (isTotalDefense) {
			ac.addModifier(totalDefenseAC);
		} else {
			ac.removeModifier(totalDefenseAC);
		}
		fireEvent();	// need to fire because no modifiers on this have changed
	}

	public boolean isTotalDefense() {
		return isTotalDefense;
	}

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		text.append(bab.getValue()).append(" base attack bonus<br/>");
		Map<Modifier, Boolean> mods = getModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(getValue()).append(" total melee attack");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		return text.toString();
	}

	public String getRangedSummary() {
		StringBuilder text = new StringBuilder();
		text.append(bab.getValue()).append(" base attack bonus<br/>");
		Map<Modifier, Boolean> mods = getRangedModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(getRangedValue()).append(" total ranged attack");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		return text.toString();
	}

	// ------------ Damage statistic --------------
	// TODO may need to split this into melee and ranged versions
	// this is really just an interface for adding modifiers.
	// listener modifications are forwarded to the Attacks instance (which means the source of any events will be that instance)
	// getValue() returns the total of the modifiers.
	class Damage extends Statistic {
		Damage(PropertyCollection parent) {
			super("attacks.damage", "Damage", parent);
		}

		@Override
		public String getSummary() {
			StringBuilder text = new StringBuilder();
			Map<Modifier, Boolean> mods = getModifiers();
			text.append(Statistic.getModifiersHTML(mods));
			text.append(getValue()).append(" total extra damage").append("<br/>");
			String conds = Statistic.getModifiersHTML(mods, true);
			if (conds.length() > 0) text.append("<br/>").append(conds);
			return text.toString();
		}
	};

	public Statistic getDamageStatistic() {
		return damageStat;
	}

	public String getAttacksDescription(int total) {
		return getAttacksDescription(total, bab.getValue());
	}

	// get attacks string (e.g. +12/+7/+2) for specified total and bab
	public String getAttacksDescription(int total, int bab) {
		StringBuilder s = new StringBuilder();
		if (total >= 0) s.append("+");
		s.append(total);

		for (int i = 0; i < getExtraAttacks(); i++) {
			s.append("/");
			if (total >= 0) s.append("+");
			s.append(total);
		}

		bab -= 5;
		while (bab >= 1) {
			s.append("/");
			total -= 5;
			if (total >= 0) s.append("+");
			s.append(total);
			bab -= 5;
		}
		return s.toString();
	}

	/*
	 * Weapons (specific attacks):
	 * Name (of the AttackForm)
	 * Base name (of the weapon type)
	 * Total attack bonus: BAB according to melee/ranged + weapon bonus (which may be enhanced by buff)
	 * Damage: base damage of weapon + strength bonus for melee/thrown + enhancement bonus, may be further enhanced by buff
	 * Critical
	 * Range
	 * Weight
	 * Type (damage type: P,B,S)
	 * Kind (weapon type: unarmed, light melee, one-handed melee, two-handed melee, ranged, thrown)
	 * Type/difficulty (simple/martial/exotic)
	 * Other flags: reach, improvised melee, non-lethal damage, set v charge, monk weapon
	 * Size
	 * Special properties
	 * Ammunition description
	 *
	 * Other considerations:
	 * Double weapons/two-weapon fighting/two-handed use
	 * proficiency, other feats
	 *
	 * Temporary factors:
	 * Power attack
	 * Combat expertise
	 * Combat actions?
	 *
	 * This will need modifiers for both attack and damage
	 */

//	Options applied to specific attacks:
//	Rapid Shot			// extra atack at top bonus, all attacks -2 - needs flag
//	Manyshot			// choose 2-4, though note BAB requirements
//	Weapon Focus		// specific weapon type
//	Greater Weapon Focus	// specific weapon type

	// TODO some of the stuff in here is defining the weapon, some is defining the attack. eventually will want to separate
	// TODO enhancement doesn't need to be handled this way, masterwork too - both should be dropped in favour of simple modifiers applied by the ui/CharacterAttackForm
	public class AttackForm extends Statistic {
		protected final Statistic dmgStat;

		protected String attackFormName;	// used to check weapon focus and specialization feats

		private Modifier twoWeaponPenalty = null;	// TODO rename as this includes natural secondary attacks
		Modifier enhancement = null;
		private boolean masterwork = false;		// if true then enhancement should not be added to damage (enhancement should be +1)

		CombinedDice damage = new CombinedDice();
		public SizeCategory size = SizeCategory.MEDIUM;	// weapon size // TODO should default to character's size

		public boolean natural = false;	// natural weapons use -5 penalty for all non-primary attacks (-2 with multiattack)
		public boolean primary = true;
		public boolean twoWeaponFighting = false;	// only valid if natural == false
		public boolean offhandLight = false;		// only valid if twoWeaponFighting == true (valid for primary and secondary)
		public boolean ranged = false;		// if true then must use dex instead of strength
		public boolean canUseDex = false;	// if true then can use dex if it is better than strength (e.g. weapon finesse)
		public int strMultiplier = 2;		// strength multiplier to apply to damage in units of 1/2 str mod
		public int strLimit = Integer.MAX_VALUE;	// maximum str bonus to apply (generally only compound bows)
		public boolean doublePADmg = false;	// true if the power attack damage bonus is double the penalty taken (ie. two handed melee weapon)
		public int maxAttacks = 4;	// limit on number of attacks with due to high BAB (e.g. weapons that need to be reloaded)
		public boolean weaponSpecApplies = false;	// FIXME remove

		private AttackForm(String name, PropertyCollection parent) {
			super("attacks." + name.toLowerCase(), name, parent);
			attackFormName = name;
			dmgStat = new Damage(name, parent);
		}

		public void setName(String s) {
			description = s;
		}

		public void refireEvent() {
			fireEvent();
		}

		// convenient passthrough of Attacks method:
		public int getBAB() {
			return Attacks.this.bab.getValue();
		}

		// convenient passthrough of Attacks method:
		public boolean isTotalDefense() {
			return Attacks.this.isTotalDefense();
		}

		@Override
		public Integer getValue() {
			return getBAB() + getModifiersTotal();
		}

		public void setAttackEnhancement(int val) {
			if (enhancement == null && val == 0) return;
			if (enhancement != null && enhancement.getModifier() == val && enhancement.getSource().equals("Weapon Enhancement")) return;
			setAttackEnhancement(val, "Weapon enhancement");
		}

		void setAttackEnhancement(int val, String source) {
			if (enhancement != null) {
				Modifier m = enhancement;
				enhancement = null;
				removeModifier(m);
			}
			if (val == 0) return;
			enhancement = new ImmutableModifier(val, null, source);
			addModifier(enhancement);
		}

		public int getAttackEnhancement() {
			if (enhancement == null) return 0;
			return enhancement.getModifier();
		}

		public void setMasterwork(boolean val) {
			if (masterwork == val) return;
			masterwork = val;
			setAttackEnhancement(masterwork ? 1 : 0, "Masterwork weapon");
		}

		public boolean isMasterwork() {
			return masterwork;
		}

		// returns the String version of the base damage
		public String getBaseDamage() {
			return damage.toString();
		}

		// expects format acceptable to CombinedDice.parse()
		public void setBaseDamage(String s) {
			damage = CombinedDice.parse(s);
			fireEvent();
		}

		public String getDamage() {
			int oldC = damage.getConstant();
			damage.setConstant(oldC + getModifiersTotal(dmgStat.getModifierSet(), null));
			String dmg = damage.toString();
			damage.setConstant(oldC);
			return dmg;
		}

		// ------------ Damage statistic --------------
		// this is really just an interface for adding modifiers.
		// listener modifications are forwarded to the Attacks instance (which means the source of any events will be that instance)
		// getValue() returns the total of the modifiers.
		class Damage extends Statistic {
			Damage(String name, PropertyCollection parent) {
				super("attacks." + name.toLowerCase() + ".damage", "Damage", parent);
			}

			@Override
			public String getSummary() {
				StringBuilder text = new StringBuilder();
				text.append(getBaseDamage()).append(" base damage<br/>");
				Map<Modifier, Boolean> mods = getModifiers();
				text.append(Statistic.getModifiersHTML(mods));
				text.append(getDamage()).append(" total damage");
				String conds = Statistic.getModifiersHTML(mods, true);
				if (conds.length() > 0) text.append("<br/><br/>").append(conds);
				return text.toString();
			}

			// TODO inefficient as we create new modifiers every time this is called
			@Override
			public Set<Modifier> getModifierSet() {
				Set<Modifier> mods = new HashSet<>();
				mods.addAll(damageStat.modifiers);

				if (strMod != null) {
					// strength modifier
					// could use modifiers that track the strength mod dynamically, but the parent Attacks stat has a listener that will fire a value change if strength changes so it's not necessary.
					int mod = strMod.getModifier();
					if (mod < 0 && strMultiplier > 0) {
						// penalty normal applies (an exception is some missile weapons)
						mods.add(new ImmutableModifier(mod, null, "Strength"));
					} else if (mod > 0) {
						mods.add(new ImmutableModifier(Math.min(mod * strMultiplier / 2, strLimit), null, "Strength"));
					}
				}

				// powerAttack modifier
				if (powerAttack != null && powerAttack.getModifier() < 0) {
					mods.add(new ImmutableModifier(-powerAttack.getModifier() * (doublePADmg ? 2 : 1), null, "Power Attack"));
				}

				// enhancement modifier
				if (enhancement != null && !masterwork) mods.add(enhancement);

				if (creature.hasFeat(Feat.FEAT_WEAPON_SPECIALIZATION, attackFormName)) {
					mods.add(new ImmutableModifier(2, null, Feat.FEAT_WEAPON_SPECIALIZATION));
				}
				if (creature.hasFeat(Feat.FEAT_GREATER_WEAPON_SPECIALIZATION, attackFormName)) {
					mods.add(new ImmutableModifier(2, null, Feat.FEAT_GREATER_WEAPON_SPECIALIZATION));
				}

				// weapon specialization
				// FIXME should no longer be necessary
				if (weaponSpecApplies) mods.add(new ImmutableModifier(2, null, "Weapon specialization"));

				mods.addAll(dmgStat.modifiers);

				return mods;
			}
		};

		public Statistic getDamageStatistic() {
			return dmgStat;
		}

		// recalculates the two-weapon fighting penalty
		public void updateModifiers() {
			// two-weapon fighting modifiers:
			Modifier newMod = null;

			if (twoWeaponFighting) {
				int penalty = 10;
				if (primary) penalty -= 4;
				if (creature.hasFeat(Feat.FEAT_TWO_WEAPON_FIGHTING) || creature.hasFeat(Feat.FEAT_MULTI_WEAPON_FIGHTING)) penalty = 4;
				if (offhandLight) penalty -= 2;
				if (creature.hasFeat("enhanced multiweapon fighting")) penalty -= 2;
				newMod = new ImmutableModifier(-penalty, null, "Two-weapon fighting (" + (primary ? "primary" : "secondary") + ")");
			}

			if (!primary && (natural || !twoWeaponFighting)) {
				// secondary natural attack or manufactured weapon secondary to natural primary
				int penalty = 5;
				if (creature.hasFeat(Feat.FEAT_MULTIATTACK)) penalty = 2;
				newMod = new ImmutableModifier(-penalty, null, "Secondary attack");
			}

			if (twoWeaponPenalty == null) {
				if (newMod != null) {
					twoWeaponPenalty = newMod;
					addModifier(newMod);
				}

			} else if (newMod == null) {
				removeModifier(twoWeaponPenalty);
				twoWeaponPenalty = null;

			} else {
				// both modifiers non-null
				if (twoWeaponPenalty.getModifier() != newMod.getModifier()) {
					removeModifier(twoWeaponPenalty);
					twoWeaponPenalty = newMod;
					addModifier(newMod);
				}
			}
		}

		@Override
		public Set<Modifier> getModifierSet() {
			Set<Modifier> mods = new HashSet<>(modifiers);
			mods.addAll(Attacks.this.modifiers);
			if (ranged) {
				if (dexMod != null) mods.add(dexMod);
			} else {
				if (powerAttack != null) mods.add(powerAttack);
				if (strMod == null || (canUseDex && dexMod.getModifier() > strMod.getModifier())) {
					if (dexMod != null) mods.add(dexMod);
				} else {
					if (strMod != null) mods.add(strMod);
				}
			}

			if (creature.hasFeat(Feat.FEAT_WEAPON_FOCUS, attackFormName)) {
				mods.add(new ImmutableModifier(1, null, Feat.FEAT_WEAPON_FOCUS));
			}
			if (creature.hasFeat(Feat.FEAT_GREATER_WEAPON_FOCUS, attackFormName)) {
				mods.add(new ImmutableModifier(1, null, Feat.FEAT_GREATER_WEAPON_FOCUS));
			}

			return mods;
		}

		// typically returns a string version of the full attack array (e.g. "+12/+7/+2")
		// exceptions are:
		// secondary weapon - returns max of 1 attack
		// secondary weapon + improved 2 weapon fighting - returns max of 2 attacks
		// secondary weapon + greater 2 weapon fighting - returns max of 3 attacks
		// missile weapon + rapid shot - returns extra attack at the top bonus
		// flurry of blows - provides 1 or 2 extra attacks at the top bonus (depends on level)
		// natural weapons - returns max of 1 attack
		public String getAttacksDescription() {
			StringBuilder s = new StringBuilder();

			int total = getValue();
			if (total >= 0) s.append("+");
			s.append(total);

			// TODO insert extra attacks for rapid shot or flurry of blows
			for (int i = 0; i < getExtraAttacks(); i++) {
				s.append("/");
				if (total >= 0) s.append("+");
				s.append(total);
			}

			int max = 3;	// limit for PHB rules is BAB of 20 which gives 4 attacks
			if (natural) {
				// generally the rules say natural weapons should have a single attack, but there are exceptions
				// in the MM. we'll use the maxAttacks value which is derived from the statblock
				max = maxAttacks - 1;
			} else if (!primary) {
				max = 0;
				if (creature.hasFeat(Feat.FEAT_IMPROVED_TWO_WEAPON_FIGHTING)) max++;
				if (creature.hasFeat(Feat.FEAT_GREATER_TWO_WEAPON_FIGHTING)) max++;
			}

			if (max > maxAttacks - 1) max = maxAttacks - 1;

			int bab = getBAB() - 5;
			while (bab >= 1 && max > 0) {
				s.append("/");
				total -= 5;
				if (total >= 0) s.append("+");
				s.append(total);
				bab -= 5;
				max--;
			}
			return s.toString();
		}

		@Override
		public String getSummary() {
			StringBuilder text = new StringBuilder();
			text.append(getBAB()).append(" base attack bonus<br/>");
			Map<Modifier, Boolean> mods = getModifiers();
			text.append(Statistic.getModifiersHTML(mods));
			text.append(getValue()).append(" total");
			String conds = Statistic.getModifiersHTML(mods, true);
			if (conds.length() > 0) text.append("<br/><br/>").append(conds);
			return text.toString();
		}

		@Override
		public String toString() {
			return name;
		}
	}
}

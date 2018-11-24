package gamesystem;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gamesystem.SavingThrow.Type;
import gamesystem.core.AbstractProperty;
import gamesystem.core.OverridableProperty;
import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;
import gamesystem.core.PropertyListener;
import gamesystem.dice.DiceList;
import gamesystem.dice.HDDice;
import gamesystem.dice.SimpleDice;
import monsters.StatisticsBlock.Field;

/* HitDiceProperty monitors Levels and monster hitdice (Race) and provides a combined hitdice property that other properties such as BAB can be based on.
 * Note that while this has a list value, it's not a list of individual hitdice, it's a list of hitdice types. I.e. a monster with 6d8 hitdice will have a single
 * HDDice entry in the list, while a monster with 2d8 + 2d10 hitdice will have two.
 */

// XXX perhaps this should use the soon be added collection property? Or perhaps it shouldn't have a List<HDDice> value.
public class HitDiceProperty extends AbstractProperty<List<HDDice>> {
	private Creature creature;
	private Modifier conMod;
	private List<HDDice> hitDice;
	MaxHPs maxHPs;
	ConstructBonusHPs constructBonus;
	UnholyToughnessHPs unholyToughness;
	DesecratingAuraHPs desecratingAura;
	ImprovedToughnessHPs improvedToughness;

	public HitDiceProperty(Creature c, PropertyCollection parent) {
		super("hitdice", parent);
		creature = c;
		AbilityScore con = c.abilities.get(AbilityScore.Type.CONSTITUTION);

		if (con != null) conMod = con.getModifier();

		creature.race.addPropertyListener(e -> {
			updateConstructMod();
			updateHitDice(true);
		});

		creature.level.addPropertyListener(e -> updateHitDice(true));

		maxHPs = new MaxHPs(parent);
		maxHPs.addPropertyListener(e -> updateHitDice(false));

		creature.addPropertyListener("field." + Field.SPECIAL_QUALITIES.name(), e -> updateUnholyToughness());
		creature.addPropertyListener("field." + Field.SPECIAL_ATTACKS.name(), e -> updateDesecratingAura());
		creature.addPropertyListener("feats", e -> updateImprovedToughness());

		updateConstructMod();
		updateUnholyToughness();
		updateDesecratingAura();
		updateImprovedToughness();
		updateHitDice(false);
	}

	// We need to fire changes if races changes even if the hitDice value doesn't change because BAB or saves might need to be change even if hitdice don't.
	// But this method gets called when maxHPs changes and maxHPs forwards all events from this statistic so infinite recursion would result if we always
	// fired an event. Therefore alwaysFire is set only for race and level changes (the cases where BAB or saves could change even though hitDice is unchanged).
	private void updateHitDice(boolean alwaysFire) {
		String old = "";
		if (hitDice != null) old = DiceList.toString(hitDice);
		HDDice raceHD = creature.race.getHitDice();
		int bonus = maxHPs.getStaticModifiersTotal();
		int mod = maxHPs.getModifiersPerHDTotal();
		//System.out.println("updateHitDice static bonus HPs = " + bonus + ", per HD bonus = " + mod);
		boolean doneBonus = bonus == 0;	// will be initialised to true if we don't need to worry about this

		hitDice = new ArrayList<>();	// this needs to be after we get the per HD modifiers as if there are no hitdice then the mods will be disabled
		if (creature.level.getHitDiceCount() == 0 || raceHD.getNumber() > 1) {
			int constant = raceHD.getConstant() + mod * (raceHD.getNumber() < 1 ? 1 : raceHD.getNumber());
			if (!doneBonus) {
				constant += bonus;
				doneBonus = true;
			}
			raceHD = new HDDice(raceHD.getNumber(), raceHD.getType(), constant);
			hitDice.add(raceHD);
		}

		for (SimpleDice s : creature.level.getHitDice()) {
			int constant = mod * s.getNumber();
			if (!doneBonus) {
				constant += bonus;
				doneBonus = true;
			}
			hitDice.add(new HDDice(s.getNumber(), s.getType(), constant));
		}

		if (alwaysFire || !DiceList.toString(hitDice).equals(old)) {
			fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
		}
	}

	private List<HDDice> getLevelsHD() {
		List<HDDice> hd = new ArrayList<>();
		int mod = 0;
		if (conMod != null) mod = conMod.getModifier();

		for (SimpleDice s : creature.level.getHitDice()) {
			int constant = mod * s.getNumber();
			hd.add(new HDDice(s.getNumber(), s.getType(), constant));
		}
		return hd;
	}

	// Sets the racial hitdice based on the supplied total hitdice and existing class levels. Modifiers are ignored as these are calculated later (though they are used to identify which parts of hd are from levels).
	public void setHitDice(List<HDDice> hd) {
		//System.out.println("Setting HD to " + DiceList.toString(hd) + ", class HD = " + levels.getHitDice() + " based on level " + levels.getLevel());
		if (creature.level.getLevel() != 0) {
			List<HDDice> diff = HDDice.difference(hd, getLevelsHD());
			if (diff.size() == 0) {
				// no difference - reset race hitdice back to 1
				creature.race.setHitDiceCount(1);
				return;
			}
			if (diff.size() > 1) throw new IllegalArgumentException("Remaining HD not suitable: total = " + hd + ", class HD = " + creature.level.getHitDice());
			HDDice d = diff.get(0);
			creature.race.setHitDice(new HDDice(d.getNumber(), d.getType(), 0));
		} else {
			HDDice d = hd.get(0);
			creature.race.setHitDice(new HDDice(d.getNumber(), d.getType(), 0));
		}
		if (unholyToughness != null) unholyToughness.fireUpdate();
		updateHitDice(false);
	}

	// Returns true if the base value includes racial hitdice. Creatures without class levels should always have racial hitdice.
	// Creatures with class levels lose their racial hitdice if they have 1 HD or less.
	public boolean hasRaceHD() {
		if (creature.level.getLevel() == 0) return true;
		if (creature.race.getHitDiceCount() > 1) return true;
		return false;
	}

	@Override
	public List<HDDice> getValue() {
		return hitDice;
	}

	public int getHitDiceCount() {
		if (hitDice == null) return 0;
		int num = 0;
		for (HDDice d : hitDice) {
			if (d.getNumber() >= 1)
				num += d.getNumber();
			else
				num++;
		}
		return num;
	}

	public int getBaseSave(Type type) {
		int save = 0;
		if (hasRaceHD()) save += creature.race.getBaseSave(type);
		save += creature.level.getBaseSave(type);
		return save;
	}

	abstract class PerHDModifier extends AbstractModifier {
		String source;

		PerHDModifier(String source) {
			this.source = source;
			//HitDiceProperty.this.addPropertyListener(e -> fireUpdate());	// should be unnecessary. if the HD change then max HPs will fire an even and the modifiers will get updated anyway
		}

		abstract int getModPerHD();

		@Override
		public int getModifier() {
			return getHitDiceCount() * getModPerHD();
		}

		void fireUpdate() {
			pcs.firePropertyChange("value", null, getModifier());
		}

		@Override
		public String getSource() {
			return source;
		}
	}

	// FIXME temporarily only supports a single override value
	// Overrides on MaxHPs are applied as a total, but in fact override the base value. This means that modifiers still apply so subsequent changes to (e.g.) con will behave as expected.
	// The rule of every hitdice contributing a minimum of 1 HP regardless of con modifier is implemented by adjusting the total of the dice rolls rather than adjusting the total con modifier.
	public class MaxHPs extends Statistic implements OverridableProperty<Integer> {
		Integer override = null;
		PerHDModifier totalConMod = new PerHDModifier(AbilityScore.Type.CONSTITUTION.name()) {
			{
				if (conMod != null) {
					conMod.addPropertyChangeListener(e -> fireUpdate());
				}
			}

			@Override
			int getModPerHD() {
				if (conMod == null) return 0;
				return conMod.getModifier();
			}
		};

		public MaxHPs(PropertyCollection parent) {
			super("hit_points.max_hps", "Max Hit Points", parent);

			HitDiceProperty.this.addPropertyListener(e -> {
				if (override != null && override.equals(getTotalRolled() + getModifiersTotal())) {
					override = null;	// XXX not sure if it's best to remove override if it now matches the correct value
				}
				fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
			});

			if (conMod != null)
				super.addModifier(totalConMod);	// super.addModifier because we don't want to trigger an updateHitDice while this is still being constructed
		}

		@Override
		public OverridableProperty.PropertyValue<Integer> addOverride(Integer val) {
			Integer old = override;
			if (val == null || val == 0 || val.equals(getTotalRolled() + getModifiersTotal())) {
				override = null;	// TODO removing override should be done through removeOverride method - remove this hack when overrides are properly suported in the ui and implemented in statistic
			} else {
				override = val - getModifiersTotal();
			}
			if (override == null && old != null || override != null && !override.equals(old)) fireEvent(createEvent(PropertyEvent.OVERRIDE_ADDED));
			return new PropertyValue<Integer>(val);
		}

		@Override
		public int getBaseValue() {
			if (override != null) return override;
			return getTotalRolled();
		}

		int getStaticModifiersTotal() {
			int total = 0;
			Map<Modifier, Boolean> map = getModifiers(modifiers);
			for (Modifier m : map.keySet()) {
				if (map.get(m) && m.getCondition() == null && !(m instanceof PerHDModifier)) {
					total += m.getModifier();
				}
			}
			return total;
		}

		// note that if there are no hitdice then any per HD mods will be disabled. we shortcut this case to make it obvious
		int getModifiersPerHDTotal() {
			if (hitDice == null) return 0;
			int total = 0;
			Map<Modifier, Boolean> map = getModifiers(modifiers);
			for (Modifier m : map.keySet()) {
				if (m instanceof PerHDModifier)
					if (map.get(m) && m.getCondition() == null && m instanceof PerHDModifier) {
						total += ((PerHDModifier) m).getModPerHD();
					}
			}
			return total;
		}

		int getTotalRolled() {
			int mod = 0;
			if (conMod != null) mod = conMod.getModifier();
			int hps = 0;
			for (int i = 1; i <= creature.level.getLevel(); i++) {
				Integer roll = creature.level.getHPRoll(i);
				if (roll != null) {
					int r = roll.intValue();
					if (r + mod < 1) r = 1 - mod;
					hps += r;
				}
			}
			// TODO add race hps once we have them stored
			return hps;
		}

		@Override
		public boolean hasOverride() {
			return override != null;
		}
	}

	void updateConstructMod() {
		MonsterType t = creature.race.getAugmentedType();
		if (t == null) t = creature.race.getType();
		if (t == MonsterType.CONSTRUCT && !creature.race.hasSubtype("Living Construct")) {
			if (constructBonus == null) constructBonus = new ConstructBonusHPs();
		} else if (constructBonus != null) {
			constructBonus.remove();
			constructBonus = null;
		}
	}

	// This modifier handles the bonus hitpoints that constructs receive. Note that it doesn't check the creature's type, it is assumed that
	// this modifier will only be applied when appropriate.
	class ConstructBonusHPs extends AbstractModifier {
		PropertyListener l = e -> pcs.firePropertyChange("value", null, getModifier());

		ConstructBonusHPs() {
			creature.size.addPropertyListener(l);
			maxHPs.addModifier(this);
		}

		void remove() {
			creature.size.removePropertyListener(l);
			maxHPs.removeModifier(this);
		}

		@Override
		public int getModifier() {
			switch (creature.size.getSize()) {
			case SMALL:
				return 10;
			case MEDIUM:
				return 20;
			case LARGE:
				return 30;
			case HUGE:
				return 40;
			case GARGANTUAN:
				return 60;
			case COLOSSAL:
				return 80;
			default:
				return 0;
			}
		}

		@Override
		public String getSource() {
			return "Construct Type";
		}
	}

	void updateUnholyToughness() {
		// unholy toughness: add charisma bonus per hd
		if (!creature.hasProperty("field." + Field.SPECIAL_QUALITIES.name())) return;
		String quals = (String) creature.getPropertyValue("field." + Field.SPECIAL_QUALITIES.name());
		boolean hasUT = (quals != null && quals.toLowerCase().contains("unholy toughness"));
		if (hasUT && unholyToughness == null) {
			unholyToughness = new UnholyToughnessHPs();
		} else if (!hasUT && unholyToughness != null) {
			unholyToughness.remove();
			unholyToughness = null;
		}
	}

	// unholy toughness: add charisma bonus per hd
	class UnholyToughnessHPs extends PerHDModifier {
		PropertyChangeListener l = e -> fireUpdate();

		UnholyToughnessHPs() {
			super("Unholy Toughness");
			creature.getAbilityModifier(AbilityScore.Type.CHARISMA).addPropertyChangeListener(l);
			maxHPs.addModifier(this);
		}

		void remove() {
			creature.getAbilityModifier(AbilityScore.Type.CHARISMA).removePropertyChangeListener(l);
			maxHPs.removeModifier(this);
		}

		@Override
		void fireUpdate() {
			pcs.firePropertyChange("value", null, getModifier());
		}

		@Override
		int getModPerHD() {
			Modifier chr = creature.getAbilityModifier(AbilityScore.Type.CHARISMA);
			if (chr != null) {
				return chr.getModifier();
			}
			return 0;
		}
	}

	void updateDesecratingAura() {
		if (!creature.hasProperty("field." + Field.SPECIAL_ATTACKS.name())) return;
		String quals = (String) creature.getPropertyValue("field." + Field.SPECIAL_ATTACKS.name());
		boolean hasDA = (quals != null && quals.toLowerCase().contains("desecrating aura"));
		if (hasDA && desecratingAura == null) {
			desecratingAura = new DesecratingAuraHPs();
		} else if (!hasDA && desecratingAura != null) {
			desecratingAura.remove();
			desecratingAura = null;
		}
	}

	// desecrating aura (+2 hp per hd)
	class DesecratingAuraHPs extends PerHDModifier {
		DesecratingAuraHPs() {
			super("Descecrating Aura");
			maxHPs.addModifier(this);
		}

		void remove() {
			maxHPs.removeModifier(this);
		}

		@Override
		int getModPerHD() {
			return 2;
		}
	}

	void updateImprovedToughness() {
		if (creature.hasFeat("Improved Toughness")) {
			if (improvedToughness == null)
				improvedToughness = new ImprovedToughnessHPs();
		} else if (improvedToughness != null) {
			improvedToughness.remove();
			improvedToughness = null;
		}
	}

	// improved toughness +1 HP per HD
	class ImprovedToughnessHPs extends PerHDModifier {
		ImprovedToughnessHPs() {
			super("Improved Toughness");
			maxHPs.addModifier(this);
		}

		void remove() {
			maxHPs.removeModifier(this);
		}

		@Override
		int getModPerHD() {
			return 1;
		}
	}
}

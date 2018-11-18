package gamesystem;

import java.util.ArrayList;
import java.util.List;

import gamesystem.SavingThrow.Type;
import gamesystem.core.AbstractProperty;
import gamesystem.core.OverridableProperty;
import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;
import gamesystem.dice.HDDice;
import gamesystem.dice.SimpleDice;
import monsters.Monster;
import monsters.StatisticsBlock.Field;

/* HitDiceProperty monitors Levels and monster hitdice (Race) and provides a combined hitdice property that other properties such as BAB can be based on.
 * Note that while this has a list value, it's not a list of individual hitdice, it's a list of hitdice types. I.e. a monster with 6d8 hitdice will have a single
 * HDDice entry in the list, while a monster with 2d8 + 2d10 hitdice will have two.
 */

// XXX perhaps this should use the soon be added collection property? Or perhaps it shouldn't have a List<HDDice> value.
public class HitDiceProperty extends AbstractProperty<List<HDDice>> {
	private Race race;
	private Levels levels;
	private List<HDDice> hitDice;
	private Modifier conMod;
	public int bonusHPs = 0;	// TODO remove this hack
	MaxHPs maxHPs;

	public HitDiceProperty(PropertyCollection parent, Race r, Levels l, AbilityScore con) {
		super("hitdice", parent);
		if (r == null) throw new IllegalArgumentException("Race parameter cannot be null");
		if (l == null) throw new IllegalArgumentException("Levels parameter cannot be null");
		race = r;
		levels = l;

		if (con != null) {
			conMod = con.getModifier();
			conMod.addPropertyChangeListener(e -> {
				updateHitDice();
			});
		}

		race.addPropertyListener(e -> updateHitDice());

		levels.addPropertyListener(e -> updateHitDice());

		maxHPs = new MaxHPs(parent);

		updateHitDice();
	}

	private void updateHitDice() {
		int mod = 0;
		if (conMod != null) mod = conMod.getModifier();
		hitDice = new ArrayList<>();
		HDDice raceHD = race.getHitDice();
		int bonus = bonusHPs + maxHPs.getModifiersTotal() - mod * HDDice.getTotalConstant(hitDice);
		boolean doneBonus = bonus == 0;	// will be initialised to true if we don't need to worry about this

		if (levels.getHitDiceCount() == 0 || raceHD.getNumber() > 1) {
			int constant = raceHD.getConstant() + mod * (raceHD.getNumber() < 1 ? 1 : raceHD.getNumber());
			if (!doneBonus) {
				constant += bonus;
				doneBonus = true;
			}
			raceHD = new HDDice(raceHD.getNumber(), raceHD.getType(), constant);
			hitDice.add(raceHD);
		}

		for (SimpleDice s : levels.getHitDice()) {
			int constant = mod * s.getNumber();
			if (!doneBonus) {
				constant += bonus;
				doneBonus = true;
			}
			hitDice.add(new HDDice(s.getNumber(), s.getType(), constant));
		}

		fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
	}

	private List<HDDice> getLevelsHD() {
		List<HDDice> hd = new ArrayList<>();
		int mod = 0;
		if (conMod != null) mod = conMod.getModifier();

		for (SimpleDice s : levels.getHitDice()) {
			int constant = mod * s.getNumber();
			hd.add(new HDDice(s.getNumber(), s.getType(), constant));
		}
		return hd;
	}

	// Sets the racial hitdice based on the supplied total hitdice and existing class levels. Modifiers are ignored as these are calculated later (though they are used to identify which parts of hd are from levels)
	public void setHitDice(List<HDDice> hd) {
		//System.out.println("Setting HD to " + DiceList.toString(hd) + ", class HD = " + levels.getHitDice() + " based on level " + levels.getLevel());
		if (levels.getLevel() != 0) {
			List<HDDice> diff = HDDice.difference(hd, getLevelsHD());
			if (diff.size() == 0) {
				// no difference - reset race hitdice back to 1
				race.setHitDiceCount(1);
				return;
			}
			if (diff.size() > 1) throw new IllegalArgumentException("Remaining HD not suitable: total = " + hd + ", class HD = " + levels.getHitDice());
			HDDice d = diff.get(0);
			race.setHitDice(new HDDice(d.getNumber(), d.getType(), 0));
		} else {
			HDDice d = hd.get(0);
			race.setHitDice(new HDDice(d.getNumber(), d.getType(), 0));
		}
		updateHitDice();
	}

	// Returns true if the base value includes racial hitdice. Creatures without class levels should always have racial hitdice.
	// Creatures with class levels lose their racial hitdice if they have 1 HD or less.
	public boolean hasRaceHD() {
		if (levels.getLevel() == 0) return true;
		if (race.getHitDiceCount() > 1) return true;
		return false;
	}

	@Override
	public List<HDDice> getValue() {
		return hitDice;
	}

	public int getHitDiceCount() {
		int num = 0;
		for (HDDice d : hitDice) {
			if (d.getNumber() >= 1) num += d.getNumber();
		}
		return num;
	}

	public int getBaseSave(Type type) {
		int save = 0;
		if (hasRaceHD()) save += race.getBaseSave(type);
		save += levels.getBaseSave(type);
		return save;
	}

	// FIXME replace all of these with modifiers on maxHPs
	public void updateBonusHPs(Monster m) {
		int old = bonusHPs;
		bonusHPs = 0;

		// bonus hitpoints for constructs
		MonsterType t = race.getAugmentedType();
		if (t == null) t = race.getType();
		if (t == MonsterType.CONSTRUCT && !race.hasSubtype("Living Construct")) {
			switch (m.size.getSize()) {
			case SMALL:
				bonusHPs = 10;
				break;
			case MEDIUM:
				bonusHPs = 20;
				break;
			case LARGE:
				bonusHPs = 30;
				break;
			case HUGE:
				bonusHPs = 40;
				break;
			case GARGANTUAN:
				bonusHPs = 60;
				break;
			case COLOSSAL:
				bonusHPs = 80;
				break;
			default:
				break;
			}
		}

		// bonus hitpoints from feats
		// TODO doesn't work with feats field. revist when feats are reworked
		for (int i = 0; i < m.feats.getSize(); i++) {
			Feat feat = m.feats.get(i);
			String f = feat.getName();
			if (f.equals("Toughness")) {
//				bonusHPs += 3;	// implemented via MaxHPs mod
			} else if (f.equals("Improved Toughness")) {
				bonusHPs += getHitDiceCount();
			}
		}

		// unholy toughness: add charisma bonus per hd
		if (m.hasProperty("field." + Field.SPECIAL_QUALITIES.name())) {
			String quals = (String) m.getPropertyValue("field." + Field.SPECIAL_QUALITIES.name());
			if (quals != null && quals.toLowerCase().contains("unholy toughness")) {
				Modifier chr = m.getAbilityModifier(AbilityScore.Type.CHARISMA);
				if (chr != null) {
					bonusHPs += getHitDiceCount() * chr.getModifier();
				}
			}
		}

		// desecrating aura (+2 hp per hd)
		if (m.hasProperty("field." + Field.SPECIAL_ATTACKS.name())) {
			String quals = (String) m.getPropertyValue("field." + Field.SPECIAL_ATTACKS.name());
			if (quals != null && quals.toLowerCase().contains("desecrating aura")) {
				bonusHPs += getHitDiceCount() * 2;
			}
		}

		//System.out.println("updateBonusHPs from " + old + " to " + bonusHPs);

		if (bonusHPs != old) {
			updateHitDice();
			//System.out.println("HD updated to " + this.getValue());
		}
	}

	public void setupBonusHPs(Monster m) {
		m.addPropertyListener("", e -> {
			String source = e.source.getName();
			if (source.equals("race")
					|| source.equals("hitdice")
					|| source.equals("size")
					|| source.equals("ability_scores.charisma")
					|| source.equals("feats")
					|| source.equals("field." + Field.ABILITIES.name())	// TODO this won't trigger for feats added by monster.addFeat() - feats should be a property
					|| source.equals("field." + Field.SPECIAL_ATTACKS.name())
					|| source.equals("field." + Field.SPECIAL_QUALITIES.name())) {
				//System.out.print(source + ": ");
				updateBonusHPs(m);
			}
		});
	}

	// FIXME temporarily only supports a single override value
	// Overrides on MaxHPs are applied as a total, but in fact override the base value. This means that modifiers still apply so subsequent changes to (e.g.) con will behave as expected.
	// The rule of every hitdice contributing a minimum of 1 HP regardless of con modifier is implemented by adjusting the total of the dice rolls rather than adjusting the total con modifier.
	public class MaxHPs extends Statistic implements OverridableProperty<Integer> {
		Integer override = null;
		AbstractModifier totalConMod = new AbstractModifier() {
			{
				if (conMod != null) {
					conMod.addPropertyChangeListener(e -> pcs.firePropertyChange("value", null, getModifier()));
				}
			}

			@Override
			public int getModifier() {
				return HDDice.getTotalNumber(hitDice) * conMod.getModifier();
			}

			@Override
			public String getType() {
				return conMod.getType();
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
			System.out.println("Overriding to total of " + val + ". Old override = " + override + ", modifiers = " + getModifiersTotal());
			Integer old = override;
			if (val == null || val == 0 || val.equals(getTotalRolled() + getModifiersTotal())) {
				override = null;	// TODO removing override should be done through removeOverride method - remove this hack when overrides are properly suported in the ui and implemented in statistic
			} else {
				override = val - getModifiersTotal() - bonusHPs;
			}
			System.out.println("Value now " + getValue());
			if (!override.equals(old)) fireEvent(createEvent(PropertyEvent.OVERRIDE_ADDED));
			return new PropertyValue<Integer>(val);
		}

		@Override
		public int getBaseValue() {
			if (override != null)
				return override + bonusHPs;

			return getTotalRolled();
		}

		@Override
		public void addModifier(Modifier m) {
			super.addModifier(m);
			updateHitDice();
		}

		@Override
		public void removeModifier(Modifier m) {
			super.removeModifier(m);
			updateHitDice();
		}

		int getTotalRolled() {
			int mod = 0;
			if (conMod != null) mod = conMod.getModifier();
			int hps = 0;
			for (int i = 1; i <= levels.getLevel(); i++) {
				Integer roll = levels.getHPRoll(i);
				if (roll != null) {
					int r = roll.intValue();
					if (r + mod < 1) r = 1 - mod;
					hps += r;
				}
			}
			// TODO add race hps once we have them stored
			return hps + bonusHPs;
		}

		@Override
		public boolean hasOverride() {
			return override != null;
		}
	}
}

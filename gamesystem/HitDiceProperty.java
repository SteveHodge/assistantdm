package gamesystem;

import gamesystem.SavingThrow.Type;
import gamesystem.core.AbstractProperty;
import gamesystem.dice.HDDice;

import java.util.List;

/* HitDiceProperty monitors Levels and monster hitdice (Race) and provides a combined hitdice property that other properties such as BAB can be based on.
 */

public class HitDiceProperty extends AbstractProperty<HitDice> {
	private Race race;
	private Levels levels;
	private HitDice hitDice;	// TODO switch to CombinedDice
	private Modifier conMod;
	public int bonusHPs = 0;	// TODO remove this hack

	public HitDiceProperty(Race r, Levels l, AbilityScore con) {
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

		race.addPropertyListener(new PropertyListener<String>() {
			@Override
			public void valueChanged(gamesystem.core.Property.PropertyEvent<String> event) {
				updateHitDice();
			}

			@Override
			public void compositionChanged(gamesystem.core.Property.PropertyEvent<String> event) {
				updateHitDice();
			}
		});

		levels.addPropertyChangeListener((e) -> {
			updateHitDice();
		});

		updateHitDice();
	}

	private void updateHitDice() {
		HitDice old = hitDice;
		hitDice = levels.getHitDice();
		HitDice raceHD = new HitDice(race.getHitDice().getNumber(), race.getHitDice().getType(), race.getHitDice().getConstant());
		if (hitDice == null) {
			hitDice = raceHD;
		} else if (race.getHitDiceCount() > 1) {
			hitDice.add(raceHD);
		}
		// clear the modifiers
		for (int i = 0; i < hitDice.getComponentCount(); i++) {
			if (conMod != null && i == 0) {
				hitDice.setModifier(i, conMod.getModifier() * getHitDiceCount() + bonusHPs);
			} else if (i == 0) {
				hitDice.setModifier(i, bonusHPs);
			} else {
				hitDice.setModifier(i, 0);
			}
		}
		if (hitDice == null && old != null || hitDice != null && !hitDice.equals(old)) {
			firePropertyChanged(old, false);
		}
	}

	// Sets the racial hitdice based on the supplied total hitdice and existing class levels. Assumes that hd includes the con bonus which is factored out.
	// Though note that currently updateHitDice() removes all modifiers.
	public void setHitDice(HitDice hd) {
		//System.out.println("Setting HD to " + hd + ", class HD = " + levels.getHitDice() + " based on level " + levels.getLevel());
		int mod = hd.getModifier();
		if (conMod != null) mod -= conMod.getModifier() * getHitDiceCount();
		if (levels.getHitDice() != null) {
			HitDice diff = HitDice.difference(hd, levels.getHitDice());
			if (diff.getComponentCount() == 0) {
				// no difference - reset race hitdice back to 1
				race.setHitDiceCount(1);
				return;
			}
			if (diff.getComponentCount() > 1) throw new IllegalArgumentException("Remaining HD not suitable: total = " + hd + ", class HD = " + levels.getHitDice());
			race.setHitDice(new HDDice(diff.getNumber(0), diff.getType(0), mod));
		} else {
			race.setHitDice(new HDDice(hd.getNumber(0), hd.getType(0), mod));
		}
		updateHitDice();
	}

	// Returns true if the base value includes racial hitdice. Creatures without class levels should always have racial hitdice.
	// Creatures with class levels lose their racial hitdice if they have 1 HD or less.
	public boolean hasRaceHD() {
		if (levels.getHitDice() == null) return true;
		if (race.getHitDiceCount() > 1) return true;
		return false;
	}

	@Override
	public HitDice getBaseValue() {
		return hitDice;
	}

	public int getHitDiceCount() {
		return hitDice.getHitDiceCount();
	}

	public int getBaseSave(Type type) {
		int save = 0;
		if (hasRaceHD()) save += race.getBaseSave(type);
		save += levels.getBaseSave(type);
		return save;
	}

	// TODO this is a bit of a hack
	public void updateBonusHPs(Size size, List<Feat> feats) {
		int old = bonusHPs;

		// bonus hitpoints for constructs
		MonsterType t = race.getAugmentedType();
		if (t == null) t = race.getType();
		if (t == MonsterType.CONSTRUCT) {
			switch (size.getSize()) {
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
				bonusHPs = 0;
				break;
			}
		}

		// bonus hitpoints for toughness
		for (Feat feat : feats) {
			String f = feat.getName();
			if (f.startsWith("Toughness")) {
				int count = 1;
				if (f.contains("(")) {
					count = Integer.parseInt(f.substring(f.indexOf("(") + 1, f.indexOf(")")));
				}
				bonusHPs += 3 * count;
			}
		}

		if (bonusHPs != old) updateHitDice();
	}
}

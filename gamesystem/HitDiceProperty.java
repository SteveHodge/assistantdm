package gamesystem;

import gamesystem.SavingThrow.Type;
import gamesystem.core.AbstractProperty;

/* HitDiceProperty monitors Levels and monster hitdice (Race) and provides a combined hitdice property that other properties such as BAB can be based on.
 */

public class HitDiceProperty extends AbstractProperty<HitDice> {
	private Race race;
	private Levels levels;
	private HitDice hitDice;	// TODO switch to CombinedDice
	private Modifier conMod;

	public HitDiceProperty(Race r, Levels l, AbilityScore con) {
		if (r == null) throw new IllegalArgumentException("Race parameter cannot be null");
		if (l == null) throw new IllegalArgumentException("Levels parameter cannot be null");
		race = r;
		levels = l;

		updateHitDice();

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

		if (conMod != null) {
			conMod = con.getModifier();
			conMod.addPropertyChangeListener(e -> {
				updateHitDice();
			});
		}
	}

	private void updateHitDice() {
		HitDice old = hitDice;
		hitDice = levels.getHitDice();
		if (hitDice == null) {
			hitDice = race.getHitDice();
		} else if (race.getHitDiceCount() > 1) {
			hitDice.add(race.getHitDice());
		}
		// clear the modifiers
		for (int i = 0; i < hitDice.getComponentCount(); i++) {
			if (conMod != null && i == 0) {
				hitDice.setModifier(i, conMod.getModifier() * getHitDiceCount());
			} else {
				hitDice.setModifier(i, 0);
			}
		}
		if (hitDice == null && old != null || hitDice != null && !hitDice.equals(old)) {
			firePropertyChanged(old, false);
		}
	}

	// Sets the racial hitdice based on the supplied total hitdice and existing class levels.
	public void setHitDice(HitDice hd) {
		//System.out.println("Setting HD to " + hd + ", class HD = " + levels.getHitDice() + " based on level " + levels.getLevel());
		if (levels.getHitDice() != null) {
			HitDice diff = HitDice.difference(hd, levels.getHitDice());
			if (diff.getComponentCount() == 0) {
				// no difference - reset race hitdice back to 1
				race.setHitDiceCount(1);
				return;
			}
			if (diff.getComponentCount() > 1) throw new IllegalArgumentException("Remaining HD not suitable: total = " + hd + ", class HD = " + levels.getHitDice());
			race.setHitDice(diff);
		} else {
			race.setHitDice(hd);
		}
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
}

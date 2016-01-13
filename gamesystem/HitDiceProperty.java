package gamesystem;

import gamesystem.SavingThrow.Type;
import gamesystem.core.AbstractProperty;
import monsters.HitDice;

/* HitDiceProperty monitors Levels and monster hitdice (Race) and provides a combined hitdice property that other properties such as BAB can be based on.
 */

// TODO probably should track con bonus too
public class HitDiceProperty extends AbstractProperty<HitDice> {
	private Race race;
	private Levels levels;
	private HitDice hitDice;

	public HitDiceProperty(Race r, Levels l) {
		race = r;
		levels = l;

		updateHitDice();

		if (race != null) {
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
		}
		if (levels != null) {
			levels.addPropertyChangeListener((e) -> {
				updateHitDice();
			});
		}
	}

	private void updateHitDice() {
		HitDice old = hitDice;
		hitDice = null;
		if (levels != null) {
			hitDice = levels.getHitDice();
		}
		if (race != null) {
			if (hitDice == null) {
				hitDice = race.getHitDice();
			} else if (race.getHitDiceCount() > 1) {
				hitDice.add(race.getHitDice());
			}
		}
		if (hitDice == null && old != null || hitDice != null && !hitDice.equals(old)) {
			firePropertyChanged(old, false);
		}
	}

	@Override
	public HitDice getBaseValue() {
		return hitDice;
	}

	public int getHitDiceCount() {
		return hitDice.getHitDiceCount();
	}

	public int getBaseSave(Type type) {
		// FIXME implement properly
		if (levels != null) return levels.getBaseSave(type);
		if (race != null) return race.getBaseSave(type);
		return 0;
	}
}

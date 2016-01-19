package gamesystem;

import gamesystem.core.AbstractProperty;

// TODO fix this. it should listen to the relavent stats instead of requiring recalculateBAB. change once hitDice and type are notified properties/statistics
// once the monster's type has it's own property we can promote this to a top level class
public class BAB extends AbstractProperty<Integer> {
	final Race race;
	final Levels levels;
	int bab;	// latest value, used for change notification

	public BAB(Race r, Levels l) {
		race = r;
		levels = l;
		bab = getBAB();

		levels.addPropertyChangeListener((e) -> recalculateBAB());

		race.addPropertyListener(new PropertyListener<String>() {
			@Override
			public void valueChanged(gamesystem.core.Property.PropertyEvent<String> event) {
				recalculateBAB();
			}

			@Override
			public void compositionChanged(gamesystem.core.Property.PropertyEvent<String> event) {
				recalculateBAB();
			}
		});
	}

	private int getBAB() {
		// get any bab from levels
		int bab = levels.getBAB();

		int hd = race.getHitDiceCount();
		if (hd > 1 || levels.getHitDice() == null) {
			MonsterType t = race.getAugmentedType();
			if (t == null) t = race.getType();
			if (t != null) bab += t.getBAB(hd);
		}
		return bab;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("(");

		MonsterType t = race.getAugmentedType();
		if (t == null) t = race.getType();
		int hd = race.getHitDiceCount();

		if (levels.getHitDice() != null) {
			s.append(levels.getBAB()).append(" from classes");
			if (t != null && hd > 1) s.append(" + ");
		}

		if (t != null && hd > 1) {
			s.append(t.getBAB(hd)).append(" from " + hd + " " + t + " hitdice");
		}

		s.append(")");
		return s.toString();
	}

	@Override
	public Integer getBaseValue() {
		// we don't use cached value just incase it's stale
		assert (bab == getBAB());
		return getBAB();
	}

	public void recalculateBAB() {
		if (getBAB() != bab) {
			firePropertyChanged(bab, false);
			bab = getBAB();
		}
	}
}
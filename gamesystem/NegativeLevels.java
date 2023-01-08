package gamesystem;

import gamesystem.core.AbstractProperty;
import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;
import gamesystem.core.SettableProperty;

/* Records the number of negative levels a creature has. If this is greater then zero then modifiers are added to attacks, skills, saves, and maximum hps.
 * The penalty to maximum hit points makes sense conceptually but probably doesn't match the rules as written which state that each negative level imposes:
 * "loss of 5 hit points" (DMG300)
 * "the following penalties... 5 hit points" (DMG293)
 * "A loss of 5 hit points" (RC49)
 * It's not clear if there is intended to be a distinction between a loss of hit points and damage. Probably not.
 * Note that the MM (page 309) doesn't mention the hit point loss at all.
 * In Pathfinder current and total hitpoints are reduced by 5 for each negative level
 */
// TODO ability check modifier

public class NegativeLevels extends AbstractProperty<Integer> implements SettableProperty<Integer> {
	private Creature creature;
	int negativeLevels = 0;
	Modifier modifier = null;
	Modifier hpModifier = null;

	public NegativeLevels(Creature c, PropertyCollection parent) {
		super("negative_levels", parent);
		creature = c;
	}

	@Override
	public Integer getValue() {
		return negativeLevels;
	}

	@Override
	public String toString() {
		return Integer.toString(negativeLevels);
	}

	@Override
	public void setValue(Integer newValue) {
		if (newValue == negativeLevels)
			return;
		int old = negativeLevels;
		negativeLevels = newValue;
		fireEvent(createEvent(PropertyEvent.VALUE_CHANGED, old));

		if (old == 0 && negativeLevels > 0) {
			if (modifier == null)
				modifier = new NegativeLevelsModifier();
			if (hpModifier == null)
				hpModifier = new NegativeLevelsHPModifier();
			creature.attacks.addModifier(modifier);
			creature.skills.addModifier(modifier);
			creature.getSavingThrowStatistic(SavingThrow.Type.WILL).addModifier(modifier);
			creature.getSavingThrowStatistic(SavingThrow.Type.FORTITUDE).addModifier(modifier);
			creature.getSavingThrowStatistic(SavingThrow.Type.REFLEX).addModifier(modifier);
			creature.hitDice.maxHPs.addModifier(hpModifier);

		} else if (old > 0 && negativeLevels == 0) {
			creature.attacks.removeModifier(modifier);
			creature.skills.removeModifier(modifier);
			creature.getSavingThrowStatistic(SavingThrow.Type.WILL).removeModifier(modifier);
			creature.getSavingThrowStatistic(SavingThrow.Type.FORTITUDE).removeModifier(modifier);
			creature.getSavingThrowStatistic(SavingThrow.Type.REFLEX).removeModifier(modifier);
			creature.hitDice.maxHPs.removeModifier(hpModifier);
		}
	}

	protected class NegativeLevelsModifier extends AbstractModifier {
		public NegativeLevelsModifier() {
			NegativeLevels.this.addPropertyListener(e -> pcs.firePropertyChange("value", null, getModifier()));
		}

		@Override
		public int getModifier() {
			return -negativeLevels;
		}

		@Override
		public String getSource() {
			return "negative levels";
		}
	};

	protected class NegativeLevelsHPModifier extends AbstractModifier {
		public NegativeLevelsHPModifier() {
			NegativeLevels.this.addPropertyListener(e -> pcs.firePropertyChange("value", null, getModifier()));
		}

		@Override
		public int getModifier() {
			return -5 * negativeLevels;
		}

		@Override
		public String getSource() {
			return "negative levels";
		}
	};
}

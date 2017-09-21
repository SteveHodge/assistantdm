package gamesystem;

import gamesystem.core.AbstractProperty;
import gamesystem.core.Property;
import gamesystem.core.ValueProperty;

public class Sanity extends ValueProperty<Integer> {
	ValueProperty<Integer> knowledgeSkill = new ValueProperty<Integer>(0);

	AbstractProperty<Integer> startingSanity;

	int sessionStart;

	AbstractProperty<Integer> maxSanity = new AbstractProperty<Integer>() {
		{
			knowledgeSkill.addPropertyListener(new PropertyListener<Integer>() {
				@Override
				public void valueChanged(gamesystem.core.Property.PropertyEvent<Integer> event) {
					firePropertyChanged(99 - event.getOldValue(), false);
				}

				@Override
				public void compositionChanged(gamesystem.core.Property.PropertyEvent<Integer> event) {
					firePropertyChanged(99 - event.getOldValue(), true);
				}
			});
		}

		@Override
		public Integer getBaseValue() {
			return 99 - knowledgeSkill.getValue();
		}
	};

	public Sanity(AbilityScore wis) {
		super(calculateStarting(wis.getValue()));

		startingSanity = new AbstractProperty<Integer>() {
			{
				wis.addPropertyChangeListener((e) -> {
					Integer old = (Integer) e.getOldValue();
					int oldValue = 0;
					if (old != null) oldValue = calculateStarting(old);
					firePropertyChanged(oldValue, false);
				});
			}

			@Override
			public Integer getBaseValue() {
				return calculateStarting(wis.getValue());
			}
		};

		startSession();
	}

	private static int calculateStarting(int wis) {
		return Math.min(wis * 5, 99);
	}

	public ValueProperty<Integer> getKnowledgeSkillProperty() {
		return knowledgeSkill;
	}

	public Property<Integer> getStartingSanityProperty() {
		return startingSanity;
	}

	public Property<Integer> getMaximumSanityProperty() {
		return maxSanity;
	}

	public int getSessionStartingSanity() {
		return sessionStart;
	}

	public void startSession() {
		sessionStart = getValue();
		firePropertyChanged(getValue(), true);
	}

	// heal must be > 0. note this affects the base value so it will have no apparent effect if an override is applied
	// TODO handle overrides somehow
	public void applyHealing(int heal) {
		if (heal <= 0) throw new IllegalArgumentException("Cannot heal <= 0 sanity");
		int newVal = getValue() + heal;
		if (newVal > startingSanity.getValue()) newVal = startingSanity.getValue();
		setBaseValue(newVal);
	}

	// dmg must be > 0. note this affects the base value so it will have no apparent effect if an override is applied
	// TODO handle overrides somehow
	public void applyDamage(int dmg) {
		if (dmg <= 0) throw new IllegalArgumentException("Cannot do <= 0 sanity damage");
		setBaseValue(getValue() - dmg);
	}

}

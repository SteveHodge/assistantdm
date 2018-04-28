package gamesystem;

import gamesystem.core.AbstractOverridableProperty;
import gamesystem.core.OverridableProperty;
import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;
import gamesystem.core.ValueProperty;

public class Sanity extends ValueProperty<Integer> {
	ValueProperty<Integer> knowledgeSkill;
	AbstractOverridableProperty<Integer> startingSanity;
	AbstractOverridableProperty<Integer> maxSanity;
	int sessionStart;

	public Sanity(PropertyCollection parent, AbilityScore wis) {
		super("sanity", parent, calculateStarting(wis.getValue()));
		knowledgeSkill = new ValueProperty<Integer>("sanity.knowledge_skill", parent, 0);

		maxSanity = new AbstractOverridableProperty<Integer>("sanity.maximum", parent) {
			{
				knowledgeSkill.addPropertyListener(e -> {
					PropertyEvent evt = createEvent(PropertyEvent.REGULAR_VALUE_CHANGED);
					if (e.hasField(PropertyEvent.PREVIOUS_VALUE))
						evt.set(PropertyEvent.PREVIOUS_VALUE, 99 - e.getInt(PropertyEvent.PREVIOUS_VALUE));
					fireEvent(evt);
				});
			}

			@Override
			public Integer getRegularValue() {
				return 99 - knowledgeSkill.getValue();
			}
		};

		startingSanity = new AbstractOverridableProperty<Integer>("sanity.starting", parent) {
			{
				wis.addPropertyListener(e -> {
//					int oldValue = 0;
//					if (old != null) oldValue = calculateStarting(old);
					fireEvent(createEvent(PropertyEvent.REGULAR_VALUE_CHANGED));
				});
			}

			@Override
			public Integer getRegularValue() {
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

	public OverridableProperty<Integer> getStartingSanityProperty() {
		return startingSanity;
	}

	public OverridableProperty<Integer> getMaximumSanityProperty() {
		return maxSanity;
	}

	public int getSessionStartingSanity() {
		return sessionStart;
	}

	public void startSession() {
		sessionStart = getValue();
		fireEvent(createEvent(PropertyEvent.REGULAR_VALUE_CHANGED));
	}

	// heal must be > 0. note this affects the base value so it will have no apparent effect if an override is applied
	// TODO handle overrides somehow
	public void applyHealing(int heal) {
		if (heal <= 0) throw new IllegalArgumentException("Cannot heal <= 0 sanity");
		int newVal = getValue() + heal;
		if (newVal > startingSanity.getValue()) newVal = startingSanity.getValue();
		setValue(newVal);
	}

	// dmg must be > 0. note this affects the base value so it will have no apparent effect if an override is applied
	// TODO handle overrides somehow
	public void applyDamage(int dmg) {
		if (dmg <= 0) throw new IllegalArgumentException("Cannot do <= 0 sanity damage");
		setValue(getValue() - dmg);
	}

}

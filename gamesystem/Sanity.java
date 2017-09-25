package gamesystem;

import gamesystem.core.AbstractProperty;
import gamesystem.core.Property;
import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEventType;
import gamesystem.core.ValueProperty;

public class Sanity extends ValueProperty<Integer> {
	ValueProperty<Integer> knowledgeSkill;
	AbstractProperty<Integer> startingSanity;
	AbstractProperty<Integer> maxSanity;
	int sessionStart;

	public Sanity(PropertyCollection parent, AbilityScore wis) {
		super("sanity", parent, calculateStarting(wis.getValue()));
		knowledgeSkill = new ValueProperty<Integer>("sanity.knowledge_skill", parent, 0);

		maxSanity = new AbstractProperty<Integer>("sanity.maximum", parent) {
			{
				knowledgeSkill.addPropertyListener((source, type, oldValue, newValue) -> {
					parent.fireEvent(maxSanity, PropertyEventType.VALUE_CHANGED, 99 - oldValue);
				});
			}

			@Override
			public Integer getBaseValue() {
				return 99 - knowledgeSkill.getValue();
			}
		};

		startingSanity = new AbstractProperty<Integer>("sanity.starting", parent) {
			{
				wis.addPropertyChangeListener((e) -> {
					Integer old = (Integer) e.getOldValue();
					int oldValue = 0;
					if (old != null) oldValue = calculateStarting(old);
					parent.fireEvent(startingSanity, PropertyEventType.VALUE_CHANGED, oldValue);
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
		parent.fireEvent(this, PropertyEventType.COMPOSITION_CHANGED, getValue());
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

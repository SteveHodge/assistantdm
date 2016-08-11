package gamesystem;

import java.util.HashMap;
import java.util.Map;

/*
 * Feature is a generic base class for features like Feats and ClassFeatures that can provide simple bonuses via Modifiers
 */
public abstract class Feature<T extends Feature<T, S>, S extends FeatureDefinition<S>> {
	public S definition;
	Map<Modifier, String> modifiers = new HashMap<>();	// map of modifier to the target it will be applied to

	protected Feature(S def) {
		definition = def;
	}

	@Override
	public String toString() {
		if (definition != null)
			return definition.name;
		return "";
	}

	public String getName() {
		if (definition != null)
			return definition.name;
		return null;
	}

	public void apply(Creature c) {
		for (Modifier m : modifiers.keySet()) {
			// add modifier to target stat
			for (Statistic s : getTargetStats(c, modifiers.get(m))) {
				s.addModifier(m);
			}
		}
	}

	public void remove(Creature c) {
		for (Modifier m : modifiers.keySet()) {
			// remove modifier from target stat
			for (Statistic s : getTargetStats(c, modifiers.get(m))) {
				s.removeModifier(m);
			}
		}
	}

	protected static Statistic[] getTargetStats(Creature c, String target) {
		Statistic[] stats;
		if (target.equals(Creature.STATISTIC_SAVING_THROWS)) {
			stats = new Statistic[3];
			stats[0] = c.getStatistic(Creature.STATISTIC_FORTITUDE_SAVE);
			stats[1] = c.getStatistic(Creature.STATISTIC_WILL_SAVE);
			stats[2] = c.getStatistic(Creature.STATISTIC_REFLEX_SAVE);
		} else {
			stats = new Statistic[1];
			stats[0] = c.getStatistic(target);
		}
		if (stats[0] == null) return new Statistic[0];
		return stats;
	}
}
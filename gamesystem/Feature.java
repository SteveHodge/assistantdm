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
		return definition.name;
	}

	public String getName() {
		return definition.name;
	}

	public void apply(Creature c) {
		for (Modifier m : modifiers.keySet()) {
			// add modifier to target stat
			for (Statistic s : Buff.getTargetStats(c, modifiers.get(m))) {
				s.addModifier(m);
			}
		}
	}

	public void remove(Creature c) {
		for (Modifier m : modifiers.keySet()) {
			// remove modifier from target stat
			for (Statistic s : Buff.getTargetStats(c, modifiers.get(m))) {
				s.removeModifier(m);
			}
		}
	}
}
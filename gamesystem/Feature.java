package gamesystem;

import java.util.ArrayList;
import java.util.List;

/*
 * Feature is a generic base class for features like Feats and ClassFeatures that can provide simple bonuses via Modifiers
 */
public abstract class Feature<T extends Feature<T, S>, S extends FeatureDefinition<S>> {
	public S definition;
	private List<Modifier> modifiers = new ArrayList<>();
	private List<String> targets = new ArrayList<>();

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

	public int getModifierCount() {
		return modifiers.size();
	}

	public Modifier getModifier(int i) {
		return modifiers.get(i);
	}

	public String getModifierTarget(int i) {
		return targets.get(i);
	}

	protected void addModifier(Modifier m, String t) {
		modifiers.add(m);
		targets.add(t);
	}

	public void apply(Creature c) {
		for (int i = 0; i < getModifierCount(); i++) {
			// add modifier to target stat
			for (Statistic s : c.getStatistics(getModifierTarget(i))) {
				s.addModifier(getModifier(i));
			}
		}
	}

	public void remove(Creature c) {
		for (int i = 0; i < getModifierCount(); i++) {
			// remove modifier from target stat
			for (Statistic s : c.getStatistics(getModifierTarget(i))) {
				s.removeModifier(getModifier(i));
			}
		}
	}
}
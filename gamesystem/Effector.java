package gamesystem;

import java.util.ArrayList;
import java.util.List;

/*
 * Effector is a generic base class for things that modify a creature's statistics and/or properties, e.g. Feats and ClassFeatures. An Effector can have multiple effects.
 */
public abstract class Effector<T extends Effector<T, S>, S extends EffectorDefinition<S>> {
	public S definition;
	private List<Modifier> modifiers = new ArrayList<>();
	private List<String> targets = new ArrayList<>();

	protected Effector(S def) {
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
			if (getModifierTarget(i).equals("target")) {
				System.err.println("Can't add modifiers for " + this + ", user specified targets not yet implemented");
			} else {
				for (Statistic s : c.getStatistics(getModifierTarget(i))) {
					s.addModifier(getModifier(i));
				}
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
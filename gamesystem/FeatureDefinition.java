package gamesystem;

import java.util.HashSet;
import java.util.Set;

/*
 * FeatureDefinition is a base class for factories like ClassFeatureDefinition and FeatDefinition. It allows the definition of simple fixed effects that are realised as
 * ImmutableModifiers when the feature is added to a Creature.
 */
public abstract class FeatureDefinition<S extends FeatureDefinition<S>> {
	public String name;
	Set<Effect> effects = new HashSet<>();

	protected FeatureDefinition(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getDescription() {
		StringBuilder s = new StringBuilder();
		s.append("<html><body>");
		for (Effect e : effects) {
			s.append(e).append("<br/>");
		}
		s.append("</html></body>");
		return s.toString();
	}

	@SuppressWarnings("unchecked")
	protected S addBonus(String target, int modifier, String condition) {
		addFixedEffect(target, null, modifier, condition);
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	protected S addBonus(String target, int modifier) {
		addFixedEffect(target, null, modifier, null);
		return (S) this;
	}

	protected void addFixedEffect(String target, String type, int modifier, String condition) {
		Effect e = new Effect();
		e.target = target;
		e.type = type;
		e.modifier = modifier;
		e.condition = condition;
		effects.add(e);
	}

	static class Effect {
		String target;
		String type;
		String condition;
		int modifier;

		Modifier getModifier(String name) {
			ImmutableModifier m = new ImmutableModifier(modifier, type, name, condition);
			return m;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();

			if (modifier >= 0) s.append("+");
			s.append(modifier);
			if (type != null) s.append(" ").append(type);
			if (modifier >= 0) {
				s.append(" bonus");
			} else {
				s.append(" penalty");
			}

			s.append(" to ").append(Buff.getTargetDescription(target));
			if (condition != null) s.append(" ").append(condition);
			return s.toString();
		}
	}
}
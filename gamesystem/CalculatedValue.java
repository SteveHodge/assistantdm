package gamesystem;

import gamesystem.AbilityScore.Type;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/*
 * CalculatedValue represents an expression that is calculated dynamically, usually using values from a Creature (which the CalculatedValue is bound to). Provide change notification to Listeners.
 * Expressions are constructed using instances of Calculation classes.
 */

public class CalculatedValue {
	Term root;
	PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	CalculatedValue(Calculation calc, Creature c) {
		root = calc.bind(this, c);
	}

	protected void firePropertyChange(String prop, float newVal) {
		pcs.firePropertyChange(prop, null, newVal);
	}

	void updateValue() {
		firePropertyChange("value", value());
	}

	int value() {
		return root.value();
	}

	@Override
	public
	String toString() {
		return Integer.toString(value());
	}

	void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	static abstract class Term {
		abstract int value();
	}

	static abstract class Calculation {
		abstract Term bind(CalculatedValue calc, Creature c);
	}

	public static class Constant extends Calculation {
		int constant;

		Constant(int c) {
			constant = c;
		}

		@Override
		Term bind(CalculatedValue calc, Creature c) {
			return new Term() {
				@Override
				int value() {
					return constant;
				}
			};
		}

	}

	public static class Sum extends Calculation {
		private Calculation[] addends;

		Sum(Calculation... addends) {
			this.addends = addends;
		}

		@Override
		Term bind(CalculatedValue calc, Creature c) {
			Term[] terms = new Term[addends.length];
			for (int i = 0; i < addends.length; i++) {
				terms[i] = addends[i].bind(calc, c);
			}
			return new Term() {
				@Override
				int value() {
					int sum = 0;
					for (Term t : terms) {
						sum += t.value();
					}
					return sum;
				}
			};
		}
	}

	public static class Product extends Calculation {
		Calculation a, b;

		Product(Calculation a, Calculation b) {
			this.a = a;
			this.b = b;
		}

		@Override
		Term bind(CalculatedValue calc, Creature c) {
			Term x = a.bind(calc, c);
			Term y = b.bind(calc, c);
			return new Term() {
				@Override
				int value() {
					return x.value() * y.value();
				}
			};
		}
	}

	public static class ClassLevel extends Calculation {
		CharacterClass cls;
		float factor = 1;

		ClassLevel(CharacterClass cls) {
			this.cls = cls;
		}

		ClassLevel(CharacterClass cls, float f) {
			this.cls = cls;
			factor = f;
		}

		@Override
		Term bind(CalculatedValue calc, Creature c) {
			Levels lvl = (Levels) c.getStatistic(Creature.STATISTIC_LEVEL);
			if (lvl != null) lvl.addPropertyChangeListener(e -> calc.updateValue());
			return new Term() {
				@Override
				int value() {
					if (lvl == null) return 0;
					return (int) Math.floor(lvl.getClassLevel(cls) * factor);
				}
			};
		}
	}

	public static class CharacterLevel extends Calculation {
		float factor;

		CharacterLevel(CharacterClass cls, float f) {
			factor = f;
		}

		@Override
		Term bind(CalculatedValue calc, Creature c) {
			Levels lvl = (Levels) c.getStatistic(Creature.STATISTIC_LEVEL);
			if (lvl != null) lvl.addPropertyChangeListener(e -> calc.updateValue());
			return new Term() {
				@Override
				int value() {
					if (lvl == null) return 0;
					return (int) Math.floor(lvl.getLevel() * factor);
				}
			};
		}
	}

	public static class AbilityMod extends Calculation {
		boolean bonus = false;
		Type ability;

		AbilityMod(Type ability) {
			this.ability = ability;
		}

		AbilityMod(Type ability, boolean bonus) {
			this.ability = ability;
			this.bonus = bonus;
		}

		@Override
		Term bind(CalculatedValue calc, Creature c) {
			Modifier mod = c.getAbilityModifier(ability);
			mod.addPropertyChangeListener(e -> calc.updateValue());
			return new Term() {
				@Override
				int value() {
					int val = mod.getModifier();
					if (bonus && val < 0) val = 0;
					return val;
				}
			};
		}
	}
}

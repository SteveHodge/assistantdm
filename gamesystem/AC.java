package gamesystem;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AC extends Statistic {
	public AC(AbilityScore dex) {
		super("AC");
		addModifier(dex.getModifier());
	}

	public int getValue() {
		return 10 + super.getValue();
	}

	public Statistic getTouchAC() {
		return touchAC;
	}

	public Statistic getFlatFootedAC() {
		return flatFootedAC;
	}

	// note that listener requests are forwarded to the outer AC instance. this means the source of events will be the AC instance,
	// not the touchAC instance
	protected final Statistic touchAC = new Statistic("Flat-footed AC") {
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			AC.this.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			AC.this.removePropertyChangeListener(listener);
		}

		public void addModifier(Modifier m) {
			AC.this.addModifier(m);
		}

		public void removeModifier(Modifier m) {
			AC.this.removeModifier(m);
		}

		// this implementation matches the definition in the Rule Compendium (take the total AC and subtract Dex and dodge modifiers)
		public int getValue() {
			int ac = AC.this.getValue();

			Map<Modifier,Boolean> map = AC.this.getModifiers();
			for (Modifier m : map.keySet()) {
				if (map.get(m) && m.getCondition() == null && m.getType() != null && (m.getType().equals("Armor") || m.getType().equals("Shield") || m.getType().equals("Natural"))) {
					ac -= m.getModifier();
				}
			}
			return ac;
		}

		protected Set<Modifier> getModifierSet() {
			Set<Modifier> mods = new HashSet<Modifier>(AC.this.modifiers);
			for (Modifier m : AC.this.modifiers) {
				if (m.getType() != null && (m.getType().equals("Armor") || m.getType().equals("Shield") || m.getType().equals("Natural"))) {
					mods.remove(m);
				}
			}
			return mods;
		}
	};

	// note that listener requests are forwarded to the outer AC instance. this means the source of events will be the AC instance,
	// not the flatFootedAC instance
	protected final Statistic flatFootedAC = new Statistic("Flat-footed AC") {
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			AC.this.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			AC.this.removePropertyChangeListener(listener);
		}

		public void addModifier(Modifier m) {
			AC.this.addModifier(m);
		}

		public void removeModifier(Modifier m) {
			AC.this.removeModifier(m);
		}

		// this implementation matches the definition in the Rule Compendium (take the total AC and subtract Dex and dodge modifiers)
		public int getValue() {
			int ac = AC.this.getValue();

			Map<Modifier,Boolean> map = AC.this.getModifiers();
			for (Modifier m : map.keySet()) {
				if (map.get(m) && m.getCondition() == null && m.getType() != null && (m.getType().equals("Dexterity") || m.getType().equals("Dodge"))) {
					ac -= m.getModifier();
				}
			}
			return ac;
		}

		protected Set<Modifier> getModifierSet() {
			Set<Modifier> mods = new HashSet<Modifier>(AC.this.modifiers);
			for (Modifier m : AC.this.modifiers) {
				if (m.getType() != null && (m.getType().equals("Dexterity") || m.getType().equals("Dodge"))) {
					mods.remove(m);
				}
			}
			return mods;
		}
	};
}

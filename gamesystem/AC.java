package gamesystem;

import java.beans.PropertyChangeListener;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AC extends Statistic {
	// TODO these constants should become unnecessary eventually
	public static final int AC_ARMOR = 0;
	public static final int AC_SHIELD = 1;
	public static final int AC_NATURAL = 2;
	public static final int AC_DEX = 3;
	public static final int AC_SIZE = 4;
	public static final int AC_DEFLECTION = 5;
	public static final int AC_DODGE = 6;
	public static final int AC_OTHER = 7;
	public static final int AC_MAX_INDEX = 8;
	protected static final String[] ac_names = {"Armor","Shield","Natural Armor","Dexterity","Size","Deflection","Dodge","Misc"};

	public static String getACComponentName(int type) {
		return ac_names[type];
	}

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

	public Element getElement(Document doc) {
		Element e = doc.createElement("AC");
		for (int i=0; i<AC.AC_MAX_INDEX; i++) {
			if (getModifiersTotal(AC.getACComponentName(i)) != 0) {
				Element comp = doc.createElement("ACComponent");
				comp.setAttribute("type", AC.getACComponentName(i));
				comp.setAttribute("value", ""+getModifiersTotal(AC.getACComponentName(i)));
				e.appendChild(comp);
			}
		}
		return e;
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

		public Map<Modifier, Boolean> getModifiers() {
			Map<Modifier,Boolean> map = AC.this.getModifiers();
			for (Modifier m : AC.this.modifiers) {
				if (m.getType() != null && (m.getType().equals("Armor") || m.getType().equals("Shield") || m.getType().equals("Natural"))) {
					map.remove(m);
				}
			}
			return map;
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

		public Map<Modifier, Boolean> getModifiers() {
			Map<Modifier,Boolean> map = AC.this.getModifiers();
			for (Modifier m : AC.this.modifiers) {
				if (m.getType() != null && (m.getType().equals("Dexterity") || m.getType().equals("Dodge"))) {
					map.remove(m);
				}
			}
			return map;
		}
	};
}

package gamesystem;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AC extends Statistic {
	final protected ArmorCheckPenalty armorCheckPenalty = new ArmorCheckPenalty();
	final protected Armor armor = new Armor();
	final protected Shield shield = new Shield();
	protected LimitModifier dexMod;

	public AC(AbilityScore dex) {
		super("AC");
		dexMod = new LimitModifier(dex.getModifier());
		addModifier(dexMod);
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

	public Modifier getACPModifier() {
		return armorCheckPenalty;
	}

	public Armor getArmor() {
		return armor;
	}

	public Shield getShield() {
		return shield;
	}

	public Modifier getArmorCheckPenalty() {
		return armorCheckPenalty;
	}

	public Element getElement(Document doc) {
		Element e = doc.createElement("AC");
		e.appendChild(armor.getElement(doc));
		e.appendChild(shield.getElement(doc));
		return e;
	}

	public void parseDOM(Element e) {
		if (!e.getTagName().equals("AC")) return;

		NodeList children = e.getChildNodes();
		for (int j=0; j<children.getLength(); j++) {
			if (children.item(j).getNodeName().equals("Armor")) {
				armor.parseDOM((Element)children.item(j));
			} else if (children.item(j).getNodeName().equals("Shield")) {
				shield.parseDOM((Element)children.item(j));
			}
		}
	}

	// note that listener requests are forwarded to the outer AC instance. this means the source of events will be the AC instance,
	// not the touchAC instance
	protected final Statistic touchAC = new Statistic("Touch AC") {
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
				if (map.get(m) && m.getCondition() == null && m.getType() != null && 
						(m.getType().equals("Dexterity") && m.getModifier() > 0 || m.getType().equals("Dodge"))) {
					// note: only dex bonuses are removed, penalties should remain
					ac -= m.getModifier();
				}
			}
			return ac;
		}

		protected Set<Modifier> getModifierSet() {
			Set<Modifier> mods = new HashSet<Modifier>(AC.this.modifiers);
			for (Modifier m : AC.this.modifiers) {
				if (m.getType() != null && (m.getType().equals("Dexterity") && m.getModifier() > 0 || m.getType().equals("Dodge"))) {
					mods.remove(m);
				}
			}
			return mods;
		}
	};

	public class Shield extends Statistic {
		public String description;
		protected int bonus = 0;
		public int weight;
		protected int acp = 0;
		public int spellFailure;
		public String properties;

		protected Modifier modifier = null;		// the armor/shield modifier generated by this item that is applied to the AC
		protected Modifier enhancement = null;	// enhancement modifier applied to this item

		protected Shield() {
			super("Shield");
		}

		protected Shield(String n) {
			super(n);
		}

		public int getValue() {
			return bonus + super.getValue();
		}

		public int getBonus() {
			return bonus;
		}

		public void setBonus(int b) {
			if (bonus == b) return;
			bonus = b;

			if (modifier != null) {
				AC.this.removeModifier(modifier);
				modifier = null;
			}
			if (getValue() != 0) {
				modifier = new ImmutableModifier(getValue(), getName());
				AC.this.addModifier(modifier);
			}
		}

		public int getEnhancement() {
			if (enhancement == null) return 0;
			return enhancement.getModifier();
		}

		public void setEnhancement(int e) {
			if (enhancement == null && e == 0) return;
			if (enhancement != null && e == enhancement.getModifier()) return;

			if (enhancement != null) {
				removeModifier(enhancement);
				enhancement = null;
			}
			if (e != 0) {
				enhancement = new ImmutableModifier(e, "Enhancement");
				addModifier(enhancement);
			}

			if (modifier != null) {
				AC.this.removeModifier(modifier);
				modifier = null;
			}
			if (getValue() != 0) {
				modifier = new ImmutableModifier(getValue(), getName());
				AC.this.addModifier(modifier);
			}
		}

		public int getACP() {
			return acp;
		}

		public void setACP(int v) {
			if (acp == v) return;
			acp = v;
			armorCheckPenalty.update();
		}

		public Element getElement(Document doc) {
			Element e = doc.createElement(name);
			e.setAttribute("description", description);
			e.setAttribute("bonus", ""+bonus);
			if (enhancement != null) {
				e.setAttribute("enhancement", ""+enhancement.getModifier());
			}
			e.setAttribute("weight", ""+weight);
			e.setAttribute("acp", ""+acp);
			e.setAttribute("spell_failure", ""+spellFailure);
			e.setAttribute("properties", ""+properties);			
			return e;
		}

		public void parseDOM(Element e) {
			if (!e.getTagName().equals(name)) return;

			if (e.hasAttribute("description")) description = e.getAttribute("description");
			if (e.hasAttribute("properties")) properties = e.getAttribute("properties");
			if (e.hasAttribute("bonus")) setBonus(Integer.parseInt(e.getAttribute("bonus")));
			if (e.hasAttribute("enhancement")) setEnhancement(Integer.parseInt(e.getAttribute("enhancement")));
			if (e.hasAttribute("weight")) weight = Integer.parseInt(e.getAttribute("weight"));
			if (e.hasAttribute("acp")) setACP(Integer.parseInt(e.getAttribute("acp")));
			if (e.hasAttribute("spell_failure")) spellFailure = Integer.parseInt(e.getAttribute("spell_failure"));
		}

		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append(name);
			s.append("\nDescription = ").append(description);
			s.append("\nProperties = ").append(properties);
			s.append("\nBonus = ").append(getBonus());
			s.append("\nEnhancement = ").append(getEnhancement());
			s.append("\nACP = ").append(getACP());
			s.append("\nSpell Failure = ").append(spellFailure);
			s.append("\nWeight = ").append(weight);
			return s.toString();
		}
	}

	public class Armor extends Shield {
		public String type;
		public int speed;

		protected Armor() {
			super("Armor");
		}

		public int getMaxDex() {
			return dexMod.getLimit();
		}

		public void setMaxDex(int v) {
			if (v == dexMod.getLimit()) return;
			dexMod.setLimit(v);
		}

		public Element getElement(Document doc) {
			Element e = super.getElement(doc);
			e.setAttribute("type", type);
			e.setAttribute("speed", ""+speed);
			e.setAttribute("max_dex", ""+dexMod.getLimit());
			return e;
		}

		public void parseDOM(Element e) {
			if (!e.getTagName().equals(name)) return;
			super.parseDOM(e);

			if (e.hasAttribute("type")) type = e.getAttribute("type");
			if (e.hasAttribute("speed")) speed = Integer.parseInt(e.getAttribute("speed"));
			if (e.hasAttribute("max_dex")) setMaxDex(Integer.parseInt(e.getAttribute("max_dex")));
		}
	}

	// TODO not sure if this should have type set or source set or if it should depend on where the penalty is from
	// TODO will need to include encumberance eventually
	// TODO should possibly be separate modifiers for armor and shield. will need separate modifiers or separate implementation for non-proficiency
	protected class ArmorCheckPenalty extends AbstractModifier {
		public int getModifier() {
			return armor.acp + shield.acp;
		}

		public String getType() {
			return "Armor Check Penalty";
		}

		public void update() {
			pcs.firePropertyChange("value", null, armor.acp + shield.acp);
		}
	}
}

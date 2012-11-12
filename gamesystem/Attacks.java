package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import party.Character;
import party.Creature;

// TODO combat options need to be included in DOM
// TODO damage
// TODO implement modifier particular to one attack mode (ranged/grapple etc) (note that grapple size modifier is different from regular attack size modifier)
// TODO think about grapple - most modifier to attack shouldn't apply...
// TODO consider if it is worth including situational modifiers (e.g. flanking, squeezing, higher, shooting into combat etc)

public class Attacks extends Statistic {
	public enum Usage {
		ONE_HANDED("One-handed"),
		TWO_HANDED("Two-handed"),
		PRIMARY("Two-Weapon (Primary)"),
		SECONDARY("Two-Weapon (Secondary)"),
		THROWN("Thown");

		private Usage(String d) {description = d;}

		public String toString() {return description;}

		private final String description;
	}

	// TODO the Kind enum value are really shorthand for a bunch of different properties. will need to split the properties out
	// properties include permitted use (1h,2h,thrown,ranged), stat for damage adjustment, flag if weapon finesse can be applied, etc
	public enum Kind {
		LIGHT("Light Melee"),				// can't be used 2 handed, adds str
		ONE_HANDED("One-handed Melee"),		// can be used 1 or 2 handed. adds str for primary hand use, 1/2 str for off hand use, 3/2 str for 2 handed use
		TWO_HANDED("Two-handed Melee"),		// must be used 2 handed. adds 3/2 str
		THROWN("Ranged - Thrown"),			// adds str. can be thrown in off hand 
		MISSILE("Ranged - Projectile");		// adds str only if mighty bow or sling

		private Kind(String d) {description = d;}

		public String toString() {return description;}

		public static Kind getKind(String d) {
			for (Kind k : values()) {
				if (k.description.equals(d)) return k;
			}
			return null;	// TODO probably better to throw an exception 
		}

		private final String description;
	}

	protected int BAB = 0;
	protected Modifier strMod;
	protected Modifier dexMod;
	protected Character character;
	protected AC ac;
	protected AttackFormListModel attackFormsModel = new AttackFormListModel();
	protected List<AttackForm> attackFroms = new ArrayList<AttackForm>();
	protected Modifier powerAttack = null;
	protected Modifier combatExpertise = null;
	protected Modifier combatExpertiseAC = null;	// assumed to be null/not-null in sync with combatExpertise
	protected boolean isTotalDefense = false;
	protected Modifier totalDefenseAC = new ImmutableModifier(4,"Dodge","Total defense");
	protected boolean isFightingDefensively = false;
	protected Modifier fightingDefensively = new ImmutableModifier(-4,null,"Fighting defensively");
	protected Modifier fightingDefensivelyAC = new ImmutableModifier(2,"Dodge","Fighting defensively");

	final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			firePropertyChange("value", null, getValue());
		}
	};

	// TODO would prefer not to require a Character. however we do need abilityscores, feats, and AC for some combat options
	public Attacks(AbilityScore str, AbilityScore dex, Character c) {
		super("Attacks");

		character = c;
		ac = (AC)character.getStatistic(Creature.STATISTIC_AC);

		strMod = str.getModifier();
		strMod.addPropertyChangeListener(listener);

		dexMod = dex.getModifier();
		dexMod.addPropertyChangeListener(listener);
	}

	protected void firePropertyChange(String prop, Integer oldVal, Integer newVal) {
		super.firePropertyChange(prop, oldVal, newVal);
		for (AttackForm a : attackFroms) {
			// TODO source will be wrong...
			a.pcs.firePropertyChange("value", null, getValue());
		}
	}

	public int getBAB() {
		return BAB;
	}

	public void setBAB(int bab) {
		BAB = bab;
		firePropertyChange("value", null, getValue());
	}

	// returns the str-modified ("melee") statistic
	public int getValue() {
		return BAB + getModifiersTotal();
	}

	protected Set<Modifier> getModifierSet() {
		Set<Modifier> mods = new HashSet<Modifier>();
		mods.addAll(modifiers);
		mods.add(strMod);
		if (powerAttack != null) mods.add(powerAttack);
		return mods;
	}

	public int getRangedValue() {
		Set<Modifier> mods = new HashSet<Modifier>();
		mods.addAll(modifiers);
		mods.add(dexMod);
		return BAB + getModifiersTotal(mods,null);
	}

	public Map<Modifier,Boolean> getRangedModifiers() {
		Set<Modifier> mods = new HashSet<Modifier>();
		mods.addAll(modifiers);
		mods.add(dexMod);
		return getModifiers(mods);
	}

	public int getGrappleValue() {
		return getValue();
	}

	// --------------------- combat options -----------------
	// sets up the new power attack modifier if the value has changed.
	// note it doesn't add the modifier, instead the modifier is included in getValue() and AttackForms for melee attacks
	// TODO bounds checking?
	public void setPowerAttack(int value) {
		if (powerAttack == null && value == 0) return;
		if (powerAttack != null && powerAttack.getModifier() == value) return;
		if (value == 0) {
			powerAttack = null;
		} else {
			powerAttack = new ImmutableModifier(-value,null,"Power attack");
		}
		firePropertyChange("value", null, getValue());
	}

	// returns the amount of power attack applied (0 to BAB inclusive)
	public int getPowerAttack() {
		if (powerAttack == null) return 0;
		return -powerAttack.getModifier();
	}

	// combat expertise applies to all attacks so we add it as a modifier
	// TODO bounds checking?
	public void setCombatExpertise(int value) {
		if (combatExpertise == null && value == 0) return;	// unchanged value

		if (combatExpertise != null) {
			if (combatExpertise.getModifier() == value) return;	// unchanged value
			removeModifier(combatExpertise);
			ac.removeModifier(combatExpertiseAC);
			combatExpertise = null;
		}

		if (value > 0) {
			combatExpertise = new ImmutableModifier(-value,null,"Combat Expertise");
			addModifier(combatExpertise);
			combatExpertiseAC = new ImmutableModifier(value,"Dodge","Combat Expertise");
			ac.addModifier(combatExpertiseAC);
		}

		//firePropertyChange("value", null, getValue());	// will be fired by when the modifier is changed 
	}

	// returns the amount of combat expertise applied (i.e. 0 to 5 inclusive)
	public int getCombatExpertise() {
		if (combatExpertise == null) return 0;
		return -combatExpertise.getModifier();
	}

	public void setFightingDefensively(boolean enabled) {
		if (enabled == isFightingDefensively) return;	// value is unchanged

		isFightingDefensively = enabled;
		if (isFightingDefensively) {
			addModifier(fightingDefensively);
			ac.addModifier(fightingDefensivelyAC);
		} else {
			removeModifier(fightingDefensively);
			ac.removeModifier(fightingDefensivelyAC);
		}
	}

	public boolean isFightingDefensively() {
		return isFightingDefensively;
	}

	public void setTotalDefense(boolean enabled) {
		if (enabled == isTotalDefense) return;	// value is unchanged

		isTotalDefense = enabled;
		if (isTotalDefense) {
			ac.addModifier(totalDefenseAC);
		} else {
			ac.removeModifier(totalDefenseAC);
		}
		firePropertyChange("value", null, getValue());	// need to fire because no modifiers on this have changed
	}

	public boolean isTotalDefense() {
		return isTotalDefense;
	} 

	// ------------ DOM conversion --------------
	public Element getElement(Document doc) {
		Element e = doc.createElement("Attacks");
		e.setAttribute("base", ""+getBAB());
		// combat options:
		if (powerAttack != null) {
			e.setAttribute("power_attack", ""+getPowerAttack());
		}
		if (combatExpertise != null) {
			e.setAttribute("combat_expertise", ""+getCombatExpertise());
		}
		if (isTotalDefense) e.setAttribute("total_defense","true");
		if (isFightingDefensively) e.setAttribute("fighting_defensively","true");

		for (AttackForm a : attackFroms) {
			e.appendChild(a.getElement(doc));
		}
		return e;
	}

	public void parseDOM(Element e) {
		if (!e.getTagName().equals("Attacks")) return;

		setBAB(Integer.parseInt(e.getAttribute("base")));

		if (e.hasAttribute("power_attack")) setPowerAttack(Integer.parseInt(e.getAttribute("power_attack")));
		if (e.hasAttribute("combat_expertise")) setCombatExpertise(Integer.parseInt(e.getAttribute("combat_expertise")));
		if (e.getAttribute("total_defense").equals("true") || e.getAttribute("total_defense").equals("1")) setTotalDefense(true);
		if (e.getAttribute("fighting_defensively").equals("true") || e.getAttribute("total_defense").equals("1")) setFightingDefensively(true);

		NodeList children = e.getChildNodes();
		for (int j=0; j<children.getLength(); j++) {
			if (children.item(j).getNodeName().equals("AttackForm")) {
				Attacks.AttackForm w = addAttackForm();
				w.parseDOM((Element)children.item(j));
			}
		}
	}

	// ------------ Attack forms / weapons list related ------------
	@SuppressWarnings("serial")
	protected class AttackFormListModel extends AbstractListModel {
		public AttackForm get(int i) {
			return attackFroms.get(i);
		}

		public Object getElementAt(int i) {
			return get(i);
		}

		public int getSize() {
			return attackFroms.size();
		}

		public void addElement(AttackForm a) {
			attackFroms.add(a);
			fireIntervalAdded(this, attackFroms.size()-1, attackFroms.size()-1);
		}

		public void removeElement(AttackForm a) {
			int i = attackFroms.indexOf(a);
			if (i > -1) {
				attackFroms.remove(a);
				fireIntervalRemoved(this, i, i);
			}
		}

		public void move(int fromIndex, int toIndex) {
			if (fromIndex < 0 || fromIndex > attackFormsModel.getSize()-1
					|| toIndex < 0 || toIndex > attackFormsModel.getSize()-1) {
				throw new IndexOutOfBoundsException();	// TODO message
			}

			AttackForm a = attackFroms.remove(fromIndex);
			fireIntervalRemoved(this, fromIndex, fromIndex);
			attackFroms.add(toIndex, a);
			fireIntervalAdded(this, toIndex, toIndex);
		}

		/*
		 * Tell the list model to signal its listeners that the specified element has been updated
		 */
		public void updated(AttackForm attackForm) {
			int i = attackFroms.indexOf(attackForm);
			if (i > -1) fireContentsChanged(this, i, i);
		}
	}

	public ListModel getAttackFormsListModel() {
		return attackFormsModel;
	}

	public int getAttackFormsCount() {
		return attackFormsModel.getSize();
	}
	
	public AttackForm getAttackForm(int i) {
		return attackFormsModel.get(i);
	}

	public AttackForm addAttackForm() {
		AttackForm a = new AttackForm("new weapon");
		attackFormsModel.addElement(a);
		return a;
	}

	public void removeAttackForm(AttackForm a) {
		attackFormsModel.removeElement(a);
	}

	public void moveAttackForm(int fromIndex, int toIndex) {
		attackFormsModel.move(fromIndex, toIndex);
	}

	public String getAttacksDescription(int total) {
		StringBuilder s = new StringBuilder();
		if (total >= 0) s.append("+");
		s.append(total);
		int bab = getBAB() - 5;
		while (bab >= 1) {
			s.append("/");
			total -= 5;
			if (total >= 0) s.append("+");
			s.append(total);
			bab -= 5;
		}
		return s.toString();
	}

	/*
	 * Weapons (specific attacks):
	 * Name (of the AttackForm)
	 * Base name (of the weapon type)
	 * Total attack bonus: BAB according to melee/ranged + weapon bonus (which may be enhanced by buff)
	 * Damage: base damage of weapon + strength bonus for melee/thrown + enhancement bonus, may be further enhanced by buff
	 * Critical
	 * Range
	 * Weight
	 * Type (damage type: P,B,S)
	 * Kind (weapon type: unarmed, light melee, one-handed melee, two-handed melee, ranged, thrown)
	 * Type/difficulty (simple/martial/exotic)
	 * Other flags: reach, improvised melee, non-lethal damage, set v charge, monk weapon
	 * Size
	 * Special properties
	 * Ammunition description
	 *
	 * Other considerations:
	 * Double weapons/two-weapon fighting/two-handed use
	 * proficiency, other feats
	 * 
	 * Temporary factors:
	 * Power attack
	 * Combat expertise
	 * Combat actions?
	 * 
	 * This will need modifiers for both attack and damage
	 */

//	Options applied to specific attacks:
//	Rapid Shot			// extra atack at top bonus, all attacks -2 - needs flag
//	Manyshot			// choose 2-4, though note BAB requirements
//	Weapon Focus		// specific weapon type
//	Greater Weapon Focus	// specific weapon type

	public class AttackForm extends Statistic {
		protected Modifier twoWeaponPenalty = null;
		protected Modifier enhancement = null;
		protected Modifier powerAttackMod = null;
		protected Modifier combatExpertiseMod = null;

		public String damage;				// TODO change to dice
		public String critical;				// TODO split into range and multiplier
		public int range;
		public int weight;
		public String damage_type;			// TODO convert to enum/constants/bitfield
		public String properties;			// TODO eventually will be derived
		public String ammunition;
		public int size = Size.SIZE_MEDIUM;	// weapon size // TODO should default to character's size 
		protected Kind  kind;					// weapon kind (melee/ranged/thrown etc)
		protected Usage usage;					// style of use (one-handed, two-handed, primary, etc) // TODO when we have weapon definitions this should default to the correct "normal" use of the weapon

		public AttackForm(String name) {
			super(name);
		}

		public void setName(String s) {
			name = s;
			attackFormsModel.updated(this);
		}

		// convenient passthrough of Attacks method:
		public int getBAB() {
			return Attacks.this.getBAB();
		}

		// convenient passthrough of Attacks method:
		public boolean isTotalDefense() {
			return Attacks.this.isTotalDefense();
		} 

		public int getValue() {
			return getBAB() + getModifiersTotal();
		}

		public void setAttackEnhancement(int val) {
			if (enhancement != null) {
				if (enhancement.getModifier() == val) return;
				removeModifier(enhancement);
			}
			if (val == 0) return;
			enhancement = new ImmutableModifier(val,null,"Weapon enhancement");
			addModifier(enhancement);
		}

		public int getAttackEnhancement() {
			if (enhancement == null) return 0;
			return enhancement.getModifier();
		}

		public void setKind(Kind k) {
			// TODO check argument for validity? (kind will go away at some point so is there any point?)
			kind = k;
			updateModifiers();
		}

		public Kind getKind() {
			return kind;
		}

		public void setUsage(Usage u) {
			// TODO validate argument
			usage = u;
			updateModifiers();
		}

		public Usage getUsage() {
			return usage;
		}

		// calculates any penalties and applies them
		protected void updateModifiers() {			
			// two-weapon fighting modifiers:
			Modifier newMod = null;

			if (usage == Usage.PRIMARY) {
				int penalty = 6;
				if (character.hasFeat(Feat.FEAT_TWO_WEAPON_FIGHTING)) penalty = 4;

				// find the next "secondary" weapon - if it's light then penalty is reduced by 2
				for (int i = attackFroms.indexOf(this)+1; i < attackFroms.size(); i++) {
					AttackForm a = attackFroms.get(i);
					if (a.usage == Usage.SECONDARY) {
						if (a.kind == Kind.LIGHT) penalty -= 2;
						break;
					}
				}
				
				newMod = new ImmutableModifier(-penalty,null,"Two-weapon fighting (primary)");
			} else if (usage == Usage.SECONDARY) {
				int penalty = 10;
				if (character.hasFeat(Feat.FEAT_TWO_WEAPON_FIGHTING)) penalty = 4;
				if (kind == Kind.LIGHT) penalty -= 2;
				newMod = new ImmutableModifier(-penalty,null,"Two-weapon fighting (secondary)");
			}

			if (twoWeaponPenalty == null) {
				if (newMod != null) {
					twoWeaponPenalty = newMod;
					addModifier(newMod);
				}

			} else if (newMod == null) {
				removeModifier(twoWeaponPenalty);
				twoWeaponPenalty = null;

			} else {
				// both modifiers non-null
				if (twoWeaponPenalty.getModifier() != newMod.getModifier()) {
					removeModifier(twoWeaponPenalty);
					twoWeaponPenalty = newMod;
					addModifier(newMod);
				}
			}
		}

		// if kind is unset then it is assumed to be a melee weapon
		public Set<Modifier> getModifierSet() {
			Set<Modifier> mods = new HashSet<Modifier>(modifiers);
			mods.addAll(Attacks.this.modifiers);
			if (kind == Kind.THROWN || kind == Kind.MISSILE) {
				mods.add(dexMod);
			} else {
				// melee attack
				if (powerAttack != null) mods.add(powerAttack);
				if (kind == Kind.LIGHT && character.hasFeat(Feat.FEAT_WEAPON_FINESSE) && dexMod.getModifier() > strMod.getModifier()) {
					// TODO weapon finesse also applies to some other weapons...
					mods.add(dexMod);
				} else {
					mods.add(strMod);
				}
			}
			return mods;
		}

		// typically returns a string version of the full attack array (e.g. "+12/+7/+2")
		// exceptions are:
		// secondary weapon - returns max of 1 attack
		// secondary weapon + improved 2 weapon fighting - returns max of 2 attacks
		// secondary weapon + greater 2 weapon fighting - returns max of 3 attacks
		// missile weapon + rapid shot - returns extra attack at the top bonus
		// flurry of blows - provides 1 or 2 extra attacks at the top bonus (depends on level) 
		public String getAttacksDescription() {
			StringBuilder s = new StringBuilder();

			int total = getValue();
			if (total >= 0) s.append("+");
			s.append(total);

			// TODO insert extra attacks for rapid shot or flurry of blows

			int max = 3;	// limit for PHB rules is BAB of 20 which gives 4 attacks
			if (usage == Usage.SECONDARY) {
				max = 0;
				if (character.hasFeat(Feat.FEAT_IMPROVED_TWO_WEAPON_FIGHTING)) max++;
				if (character.hasFeat(Feat.FEAT_GREATER_TWO_WEAPON_FIGHTING)) max++;
			}

			int bab = getBAB() - 5;
			while (bab >= 1 && max > 0) {
				s.append("/");
				total -= 5;
				if (total >= 0) s.append("+");
				s.append(total);
				bab -= 5;
				max--;
			}
			return s.toString();
		}

		public String toString() {
			return name;
		}

		public Element getElement(Document doc) {
			Element e = doc.createElement("AttackForm");
			e.setAttribute("name", name);
			if (enhancement != null) {
				e.setAttribute("enhancement", ""+enhancement.getModifier());
			} else {
				e.setAttribute("enhancement", "0");
			}
			e.setAttribute("damage", damage);
			e.setAttribute("critical", critical);
			if (range > 0) e.setAttribute("range", ""+range);
			if (weight > 0) e.setAttribute("weight", ""+weight);
			e.setAttribute("type", damage_type);
			e.setAttribute("size", ""+size);
			if (properties == null || properties.length() == 0) {
				e.setAttribute("properties", usage.toString());
			} else {
				e.setAttribute("properties", properties);
			}
			e.setAttribute("ammunition", ammunition);
			e.setAttribute("kind", kind.toString());
			e.setAttribute("usage", ""+usage.ordinal());

			// informational attributes:
			e.setAttribute("size_description", Size.SIZES[size]);
			e.setAttribute("total", ""+getValue());
			return e;
		}

		public void parseDOM(Element e) {
			if (!e.getTagName().equals("AttackForm")) return;

			name = e.getAttribute("name");
			if (e.hasAttribute("enhancement")) {
				setAttackEnhancement(Integer.parseInt(e.getAttribute("enhancement")));
			}
			damage = e.getAttribute("damage");
			critical = e.getAttribute("critical");
			if (e.hasAttribute("range")) range = Integer.parseInt(e.getAttribute("range"));
			if (e.hasAttribute("weight")) weight = Integer.parseInt(e.getAttribute("weight"));
			damage_type = e.getAttribute("type");
			if (e.hasAttribute("size")) size = Integer.parseInt(e.getAttribute("size"));
			ammunition = e.getAttribute("ammunition");
			kind = Kind.getKind(e.getAttribute("kind"));
			if (e.hasAttribute("usage")) usage = Usage.values()[Integer.parseInt(e.getAttribute("usage"))];
			String s = e.getAttribute("properties");
			if (s != null && !s.equals(usage)) {
				properties = s;
			}
			updateModifiers();
		}
	}
}

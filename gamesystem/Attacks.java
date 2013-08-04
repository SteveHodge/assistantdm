package gamesystem;

import gamesystem.dice.CombinedDice;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import party.Character;
import party.Creature;

// TODO attack form specific combat options (+DOM)
// TODO damage
// TODO implement modifier particular to one attack mode (ranged/grapple etc) (note that grapple size modifier is different from regular attack size modifier)
// TODO think about grapple - most modifier to attack shouldn't apply...
// TODO consider if it is worth including situational modifiers (e.g. flanking, squeezing, higher, shooting into combat etc)

/*
 * Statistics provided:
 * Attacks - the melee attack statistic
 * .AttackForm - a particular attack mode
 * Properties provided:
 * extra_attacks - additional attacks at top attack bonus
 * base_attack_bonus - TODO
 */

public class Attacks extends Statistic {
	public enum Usage {
		ONE_HANDED("One-handed"),
		TWO_HANDED("Two-handed"),
		PRIMARY("Two-Weapon (Primary)"),
		SECONDARY("Two-Weapon (Secondary)"),
		THROWN("Thown");

		private Usage(String d) {description = d;}

		@Override
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

		@Override
		public String toString() {return description;}

		public static Kind getKind(String d) {
			for (Kind k : values()) {
				if (k.description.equals(d)) return k;
			}
			return null;	// TODO probably better to throw an exception
		}

		private final String description;
	}

	private int BAB = 0;
	private Modifier strMod;
	private Modifier dexMod;
	private Character character;
	private AC ac;
	private AttackFormListModel attackFormsModel = new AttackFormListModel();
	private List<AttackForm> attackForms = new ArrayList<AttackForm>();
	private Modifier powerAttack = null;
	private Modifier combatExpertise = null;
	private Modifier combatExpertiseAC = null;	// assumed to be null/not-null in sync with combatExpertise
	private boolean isTotalDefense = false;
	private Modifier totalDefenseAC = new ImmutableModifier(4, "Dodge", "Total defense");
	private boolean isFightingDefensively = false;
	private Modifier fightingDefensively = new ImmutableModifier(-4, null, "Fighting defensively");
	private Modifier fightingDefensivelyAC = new ImmutableModifier(2, "Dodge", "Fighting defensively");

	final PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			firePropertyChange("value", null, getValue());
		}
	};

	final ListDataListener featsListener = new ListDataListener() {
		@Override
		public void contentsChanged(ListDataEvent e) {update();}
		@Override
		public void intervalAdded(ListDataEvent e) {update();}
		@Override
		public void intervalRemoved(ListDataEvent e) {update();}

		protected void update() {
			setPowerAttack(getPowerAttack());
			setCombatExpertise(getCombatExpertise());
			for (AttackForm a : attackForms) {
				a.updateModifiers();
			}
			firePropertyChange("value", null, getValue());	// may have been fired by the power attack or combate expertise, but maybe not
		}
	};

	// TODO would prefer not to require a Character. however we do need abilityscores, feats, and AC for some combat options
	public Attacks(AbilityScore str, AbilityScore dex, Character c) {
		super("Attacks");

		character = c;
		character.feats.addListDataListener(featsListener);
		ac = (AC)character.getStatistic(Creature.STATISTIC_AC);

		strMod = str.getModifier();
		strMod.addPropertyChangeListener(listener);

		dexMod = dex.getModifier();
		dexMod.addPropertyChangeListener(listener);
	}

	@Override
	protected void firePropertyChange(String prop, Integer oldVal, Integer newVal) {
		super.firePropertyChange(prop, oldVal, newVal);
		for (AttackForm a : attackForms) {
			// TODO source will be wrong...
			a.pcs.firePropertyChange("value", null, getValue());
		}
	}

	protected class ExtraAttacks {
		Object key;
		String source;
		int attacks;
	}

	protected List<ExtraAttacks> extraAttacks = new ArrayList<ExtraAttacks>();

	public int getExtraAttacks() {
		HashMap<String,Integer> attacks = new HashMap<String,Integer>();
		for (ExtraAttacks atk : extraAttacks) {
			attacks.put(atk.source, atk.attacks);
		}

		int total = 0;
		for (Integer v : attacks.values()) {
			total += v;
		}
		return total;
	}

	// "extra_attacks" - integer value - number of extra attacks at the highest base attack bonus
	@Override
	public void setProperty(String property, Object value, String source, Object key) {
		if (property == null || !property.equals("extra_attacks")) return;
		//int old = getExtraAttacks();
		ExtraAttacks atk = new ExtraAttacks();
		atk.key = key;
		atk.source = source;
		atk.attacks = (Integer)value;
		extraAttacks.add(atk);
		//firePropertyChange(property, old, getExtraAttacks());
		firePropertyChange("value", null, getValue());
	}

	@Override
	public void resetProperty(String property, Object key) {
		if (property == null || !property.equals("extra_attacks")) return;
		int old = getExtraAttacks();
		Iterator<ExtraAttacks> iter = extraAttacks.iterator();
		while (iter.hasNext()) {
			ExtraAttacks atk = iter.next();
			if (atk.key.equals(key)) {
				iter.remove();
			}
		}
		firePropertyChange(property, old, getExtraAttacks());
	}

	public int getBAB() {
		return BAB;
	}

	public void setBAB(int bab) {
		BAB = bab;
		firePropertyChange("value", null, getValue());
	}

	// returns the str-modified ("melee") statistic
	@Override
	public int getValue() {
		return BAB + getModifiersTotal();
	}

	@Override
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
		if (!character.hasFeat(Feat.FEAT_POWER_ATTACK)) value = 0;

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
		return -powerAttack.getModifier();	// note the modifier value is negative
	}

	// combat expertise applies to all attacks so we add it as a modifier
	// TODO bounds checking?
	public void setCombatExpertise(int value) {
		if (!character.hasFeat(Feat.FEAT_COMBAT_EXPERTISE)) value = 0;

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

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		text.append(getBAB()).append(" base attack bonus<br/>");
		Map<Modifier, Boolean> mods = getModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(getValue()).append(" total melee attack");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		return text.toString();
	}

	public String getRangedSummary() {
		StringBuilder text = new StringBuilder();
		text.append(getBAB()).append(" base attack bonus<br/>");
		Map<Modifier, Boolean> mods = getRangedModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(getRangedValue()).append(" total ranged attack");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		return text.toString();
	}

	// ------------ DOM conversion --------------
	@Override
	public Element getElement(Document doc) {
		return getElement(doc, false);
	}

	public Element getElement(Document doc, boolean include_info) {
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

		for (AttackForm a : attackForms) {
			e.appendChild(a.getElement(doc, include_info));
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

	// ------------ Damage statstic --------------
	// TODO may need to split this into melee and ranged versions
	// this is really just an interface for adding modifiers.
	// listener modifications are forwarded to the Attacks instance (which means the source of any events will be that instance)
	// getValue() returns the total of the modifiers.
	protected final Statistic damageStat = new Statistic("Damage") {
		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			Attacks.this.addPropertyChangeListener(listener);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			Attacks.this.removePropertyChangeListener(listener);
		}
	};

	public Statistic getDamageStatistic() {
		return damageStat;
	}

	// ------------ Attack forms / weapons list related ------------
	@SuppressWarnings("serial")
	protected class AttackFormListModel extends AbstractListModel {
		public AttackForm get(int i) {
			return attackForms.get(i);
		}

		@Override
		public Object getElementAt(int i) {
			return get(i);
		}

		@Override
		public int getSize() {
			return attackForms.size();
		}

		public void addElement(AttackForm a) {
			attackForms.add(a);
			fireIntervalAdded(this, attackForms.size()-1, attackForms.size()-1);
		}

		public void removeElement(AttackForm a) {
			int i = attackForms.indexOf(a);
			if (i > -1) {
				attackForms.remove(a);
				fireIntervalRemoved(this, i, i);
			}
		}

		public void move(int fromIndex, int toIndex) {
			if (fromIndex < 0 || fromIndex > attackFormsModel.getSize()-1
					|| toIndex < 0 || toIndex > attackFormsModel.getSize()-1) {
				throw new IndexOutOfBoundsException();	// TODO message
			}

			AttackForm a = attackForms.remove(fromIndex);
			fireIntervalRemoved(this, fromIndex, fromIndex);
			attackForms.add(toIndex, a);
			fireIntervalAdded(this, toIndex, toIndex);
		}

		/*
		 * Tell the list model to signal its listeners that the specified element has been updated
		 */
		public void updated(AttackForm attackForm) {
			int i = attackForms.indexOf(attackForm);
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

		for (int i = 0; i < getExtraAttacks(); i++) {
			s.append("/");
			if (total >= 0) s.append("+");
			s.append(total);
		}

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

	// TODO some of the stuff in here is defining the weapon, some is defining the attack. eventually will want to separate
	public class AttackForm extends Statistic {
		protected Modifier twoWeaponPenalty = null;
		protected Modifier enhancement = null;
		protected Modifier powerAttackMod = null;
		protected Modifier combatExpertiseMod = null;

		protected CombinedDice damage = new CombinedDice();				// TODO change to dice
		public String critical;				// TODO split into range and multiplier
		public int range;
		public int weight;
		public String damage_type;			// TODO convert to enum/constants/bitfield - not sure if it's practical since multiple values can be "and" or "or"
		public String properties;			// TODO eventually will be derived
		public String ammunition;
		public SizeCategory size = SizeCategory.MEDIUM;	// weapon size // TODO should default to character's size
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

		@Override
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
			pcs.firePropertyChange("damage", null, getDamage());
		}

		public Kind getKind() {
			return kind;
		}

		public void setUsage(Usage u) {
			// TODO validate argument
			usage = u;
			updateModifiers();
			pcs.firePropertyChange("damage", null, getDamage());
		}

		public Usage getUsage() {
			return usage;
		}

		// returns the String version of the base damage
		public String getBaseDamage() {
			return damage.toString();
		}

		// expects format acceptable to CombinedDice.parse()
		public void setBaseDamage(String s) {
			damage = CombinedDice.parse(s);
			pcs.firePropertyChange("damage", null, damage.toString());
		}

		public String getDamage() {
			int oldC = damage.getConstant();
			damage.setConstant(oldC + getModifiersTotal(getDamageModifiersSet(), null));
			String dmg = damage.toString();
			damage.setConstant(oldC);
			return dmg;
		}

		public Map<Modifier,Boolean> getDamageModifiers() {
			return getModifiers(getDamageModifiersSet());
		}

		// rules adopted here:
		// strength penalties apply fully to melee weapons, thrown weapons (including sling), and non-composite bows
		// 0.5x strength bonus applies to light or one-handed melee weapon in off-hand
		// 1x strength bonus applies to light or one-handed melee weapon in primary hand, thrown weapons (including sling),
		// and mighty bows (up to mighty limit)
		// 1.5x strength bonus applies to one- or two-handed melee weapons used two-handed
		public Set<Modifier> getDamageModifiersSet() {
			Set<Modifier> mods = new HashSet<Modifier>();
			mods.addAll(damageStat.modifiers);

			// calculate strength modifier
			int mod = strMod.getModifier();
			if (mod < 0 && kind != Kind.MISSILE) {
				// TODO should also apply to non-composite bows and slings
				mods.add(new ImmutableModifier(mod, null, "Strength"));
			} else if (mod > 0) {
				int s = 0;
				if (usage == Usage.SECONDARY) {
					s = mod/2;
				} else if (usage == Usage.TWO_HANDED && (kind == Kind.ONE_HANDED || kind == Kind.TWO_HANDED)) {
					s = 3*mod/2;
				} else {
					if (kind == Kind.THROWN	// TODO should include sling
							|| ((kind == Kind.LIGHT || kind == Kind.ONE_HANDED) && (usage == Usage.PRIMARY || usage == Usage.ONE_HANDED)))
					{
						s = mod;
					}
					// TODO mighty bows
					//if (kind == Kind.MISSILE && ...mighty...)
				}
				if (s > 0) {
					mods.add(new ImmutableModifier(s, null, "Strength"));
				}
			}

			// note powerAttack modifier is the attack penalty, so we need to negate it
			if (powerAttack != null && powerAttack.getModifier() < 0) {
				if (usage == Usage.TWO_HANDED && (kind == Kind.ONE_HANDED || kind == Kind.TWO_HANDED)) {
					mods.add(new ImmutableModifier(-powerAttack.getModifier() * 2, null, "Power Attack"));
				} else if (kind == Kind.ONE_HANDED && (usage == Usage.PRIMARY || usage == Usage.ONE_HANDED || usage == Usage.SECONDARY)) {
					mods.add(new ImmutableModifier(-powerAttack.getModifier(), null, "Power Attack"));
				}
			}

			return mods;
		}

		// calculates any penalties and applies them
		protected void updateModifiers() {
			// two-weapon fighting modifiers:
			Modifier newMod = null;

			if (usage == Usage.PRIMARY) {
				int penalty = 6;
				if (character.hasFeat(Feat.FEAT_TWO_WEAPON_FIGHTING)) penalty = 4;

				// find the next "secondary" weapon - if it's light then penalty is reduced by 2
				for (int i = attackForms.indexOf(this)+1; i < attackForms.size(); i++) {
					AttackForm a = attackForms.get(i);
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
		@Override
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
			for (int i = 0; i < getExtraAttacks(); i++) {
				s.append("/");
				if (total >= 0) s.append("+");
				s.append(total);
			}

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

		@Override
		public String getSummary() {
			StringBuilder text = new StringBuilder();
			text.append(getBAB()).append(" base attack bonus<br/>");
			Map<Modifier, Boolean> mods = getModifiers();
			text.append(Statistic.getModifiersHTML(mods));
			text.append(getValue()).append(" total");
			String conds = Statistic.getModifiersHTML(mods, true);
			if (conds.length() > 0) text.append("<br/><br/>").append(conds);
			return text.toString();
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public Element getElement(Document doc) {
			return getElement(doc, false);
		}

		public Element getElement(Document doc, boolean include_info) {
			Element e = doc.createElement("AttackForm");
			e.setAttribute("name", name);
			if (enhancement != null) {
				e.setAttribute("enhancement", ""+enhancement.getModifier());
			} else {
				e.setAttribute("enhancement", "0");
			}
			e.setAttribute("base_damage", damage.toString());
			e.setAttribute("damage", getDamage());
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
			e.setAttribute("total", ""+getValue());
			if (include_info) {
				e.setAttribute("attacks", getAttacksDescription());
				e.setAttribute("info", getSummary());
			}
			return e;
		}

		public void parseDOM(Element e) {
			if (!e.getTagName().equals("AttackForm")) return;

			name = e.getAttribute("name");
			if (e.hasAttribute("enhancement")) {
				setAttackEnhancement(Integer.parseInt(e.getAttribute("enhancement")));
			}
			damage = CombinedDice.parse(e.getAttribute("base_damage"));
			critical = e.getAttribute("critical");
			if (e.hasAttribute("range")) range = Integer.parseInt(e.getAttribute("range"));
			if (e.hasAttribute("weight")) weight = Integer.parseInt(e.getAttribute("weight"));
			damage_type = e.getAttribute("type");
			if (e.hasAttribute("size")) size = SizeCategory.getSize(e.getAttribute("size"));
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

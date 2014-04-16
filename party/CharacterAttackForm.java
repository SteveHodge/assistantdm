package party;

import gamesystem.Attacks.AttackForm;
import gamesystem.Feat;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CharacterAttackForm {
	// TODO the Kind enum value are really shorthand for a bunch of different properties. will need to split the properties out
	// properties include permitted use (1h,2h,thrown,ranged), stat for damage adjustment, flag if weapon finesse can be applied, etc
	public enum Kind {
		LIGHT("Light Melee"),				// can't be used 2 handed, adds str
		ONE_HANDED("One-handed Melee"),		// can be used 1 or 2 handed. adds str for primary hand use, 1/2 str for off hand use, 3/2 str for 2 handed use
		TWO_HANDED("Two-handed Melee"),		// must be used 2 handed. adds 3/2 str
		THROWN("Ranged - Thrown"),			// adds str. can be thrown in off hand
		MISSILE("Ranged - Projectile");		// adds str only if mighty bow or sling

		private Kind(String d) {
			description = d;
		}

		@Override
		public String toString() {
			return description;
		}

		public static Kind getKind(String d) {
			for (Kind k : values()) {
				if (k.description.equals(d)) return k;
			}
			return null;	// TODO probably better to throw an exception
		}

		private final String description;
	}

	public enum Usage {
		ONE_HANDED("One-handed"),
		TWO_HANDED("Two-handed"),
		PRIMARY("Two-Weapon (Primary)"),
		SECONDARY("Two-Weapon (Secondary)"),
		THROWN("Thown");

		private Usage(String d) {
			description = d;
		}

		@Override
		public String toString() {
			return description;
		}

		private final String description;
	}

	private Character character;
	public AttackForm attack;	// TODO change to private
	public String critical;				// TODO split into range and multiplier
	public int range;
	public int weight;
	public String damage_type;			// TODO convert to enum/constants/bitfield - not sure if it's practical since multiple values can be "and" or "or"
	public String properties;			// TODO eventually will be derived
	public String ammunition;
	public Kind kind;					// weapon kind (melee/ranged/thrown etc)
	public Usage usage;					// style of use (one-handed, two-handed, primary, etc) // TODO when we have weapon definitions this should default to the correct "normal" use of the weapon

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private final PropertyChangeListener propertyListener = e -> pcs.firePropertyChange(e);

	public CharacterAttackForm(Character c, AttackForm a) {
		character = c;
		attack = a;
		attack.natural = false;	// character attacks are currently always manufactured
		attack.addPropertyChangeListener(propertyListener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public String toString() {
		return attack.toString();
	}

	// rules adopted here:
	// strength penalties apply fully to melee weapons, thrown weapons (including sling), and non-composite bows
	// 0.5x strength bonus applies to light or one-handed melee weapon in off-hand
	// 1x strength bonus applies to light or one-handed melee weapon in primary hand, thrown weapons (including sling),
	// and mighty bows (up to mighty limit)
	// 1.5x strength bonus applies to one- or two-handed melee weapons used two-handed

	private void updateAttack() {
		// str/dex modifier
		if (kind == Kind.THROWN || kind == Kind.MISSILE) {
			attack.ranged = true;
		} else {
			attack.ranged = false;
			attack.canUseDex = (kind == Kind.LIGHT && character.hasFeat(Feat.FEAT_WEAPON_FINESSE));		// TODO weapon finesse also applies to some other weapons...
		}

		// two-weapon fighting modifiers:
		if (usage == Usage.PRIMARY) {
			attack.twoWeaponFighting = true;
			attack.primary = true;

			// find the next "secondary" weapon - if it's light then penalty is reduced by 2
			for (int i = character.attackForms.indexOf(this) + 1; i < character.attackForms.size(); i++) {
				CharacterAttackForm a = character.attackForms.get(i);
				if (a.usage == Usage.SECONDARY) {
					if (a.kind == Kind.LIGHT) attack.offhandLight = true;
					break;
				}
			}

		} else if (usage == Usage.SECONDARY) {
			attack.twoWeaponFighting = true;
			attack.primary = false;
			attack.offhandLight = kind == Kind.LIGHT;

		} else {
			attack.primary = true;
		}

		// strength modifier to damage
		if (usage == Usage.SECONDARY) {
			attack.strMultiplier = 1;	// 1/2 str mod on secondary weapons
		} else if (usage == Usage.TWO_HANDED && (kind == Kind.ONE_HANDED || kind == Kind.TWO_HANDED)) {
			attack.strMultiplier = 3;	// 3/2 str mod on non-light melee weapons used two-handed
		} else if (kind == Kind.THROWN	// TODO should include sling
				|| ((kind == Kind.LIGHT || kind == Kind.ONE_HANDED) && (usage == Usage.PRIMARY || usage == Usage.ONE_HANDED))) {
			attack.strMultiplier = 2;
		} else {
			// should be missile weapons
			// TODO mighty bows
			//if (kind == Kind.MISSILE && ...mighty...)
			attack.strMultiplier = 0;
		}

		// strength bonus/penalty to damage doesn't apply for missile weapons
		if (kind == Kind.MISSILE) attack.strMultiplier = 0;		// TODO should also apply to non-composite bows and slings

		// power attack damage bonus
		// note powerAttack modifier is the attack penalty, so we need to negate it
		if (usage == Usage.TWO_HANDED && (kind == Kind.ONE_HANDED || kind == Kind.TWO_HANDED)) {
			attack.doublePADmg = true;
		} else if (kind == Kind.ONE_HANDED && (usage == Usage.PRIMARY || usage == Usage.ONE_HANDED || usage == Usage.SECONDARY)) {
			attack.doublePADmg = false;
		}

		attack.updateModifiers();
	}

	public void setKind(Kind k) {
		// TODO check argument for validity? (kind will go away at some point so is there any point?)
		kind = k;
		updateAttack();
		pcs.firePropertyChange("damage", null, getDamage());
	}

	public Kind getKind() {
		return kind;
	}

	public void setUsage(Usage u) {
		// TODO validate argument
		usage = u;
		updateAttack();
		pcs.firePropertyChange("damage", null, getDamage());
	}

	public Usage getUsage() {
		return usage;
	}

	public String getBaseDamage() {
		return attack.getBaseDamage();
	}

	public void setBaseDamage(String text) {
		attack.setBaseDamage(text);
	}

	public String getName() {
		return attack.getName();
	}

	public void setName(String text) {
		String old = attack.getName();
		attack.setName(text);
		pcs.firePropertyChange("name", old, text);
	}

	public int getAttackEnhancement() {
		return attack.getAttackEnhancement();
	}

	public void setAttackEnhancement(int val) {
		attack.setAttackEnhancement(val);
	}

	public String getAttacksDescription() {
		return attack.getAttacksDescription();
	}

	public boolean isTotalDefense() {
		return attack.isTotalDefense();
	}

	public String getSummary() {
		return attack.getSummary();
	}

	public String getDamage() {
		return attack.getDamage();
	}

	public String getDamageSummary() {
		return attack.getDamageSummary();
	}
}
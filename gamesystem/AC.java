package gamesystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gamesystem.core.PropertyCollection;

/*
 * Natural armor is implemented as both a Statistic and also as modifiers on AC. The modifiers provide the base NA value.
 * This does not stack, as is usual for modifiers of the same type (e.g. NA provided by race and by Tenser's Transformation).
 * Modifiers to NA are applied to the NA Statistic. The total value of these modifiers is applied to each NA modifier on
 * AC, if there is none then one is created. Note this will give incorrect results if there are multiple active NA modifiers
 * on AC, but that should never happen.
 * A alternative implementation would be to treat enhancements to NA as a modifiers to AC (e.g. "Enhancement to NA" becomes
 * "NA Enhancement to AC"). This would be a simplier implementation and should achieve the same results, but is not according
 * to the rules as written. It also means using "non-standard" modifier types.
 */
// TODO proficiencies - acp to attacks when not proficient
// XXX should touch and flatfooted ac be statistics? can they be targeted by modifiers?
// TODO handle null dex more elegantly
public class AC extends Statistic implements StatisticsCollection {
	final ArmorCheckPenalty armorCheckPenalty = new ArmorCheckPenalty();
	final Armor armor;
	final Shield shield;
	final Statistic naturalArmor;	// statistic that captures modifiers to NA
	protected final Statistic touchAC;
	protected final Statistic flatFootedAC;
	LimitModifier dexMod = null;

	public AC(AbilityScore dex, PropertyCollection parent) {
		super("ac", "AC", parent);

		naturalArmor = new Statistic("ac.natural_armor", "Natural Armor", parent);
		armor = new Armor(parent);
		shield = new Shield(parent);
		touchAC = new TouchAC(parent);
		flatFootedAC = new FlatFootedAC(parent);

		if (dex != null) {
			dexMod = new LimitModifier(dex.getModifier());
			addModifier(dexMod);
		}

		naturalArmor.addPropertyListener((source, oldValue) -> fireEvent());	// FIXME hierarchy might mean events are forwarded though this AC instance anyway
	}

	// touch ac and flat-footed ac are also statistics, but they are not targettable (they are entirely based on the main AC stat with certain modifiers ignored)
	@Override
	public StatisticDescription[] getStatistics() {
		StatisticDescription[] targets = {
				new StatisticDescription(armor.getDescription(), Creature.STATISTIC_ARMOR),
				new StatisticDescription(shield.getDescription(), Creature.STATISTIC_SHIELD),
				new StatisticDescription(naturalArmor.getDescription(), Creature.STATISTIC_NATURAL_ARMOR)
		};
		return targets;
	}

	@Override
	public Integer getValue() {
		return 10 + super.getValue();
	}

	@Override
	public Map<Modifier, Boolean> getModifiers() {
		Map<Modifier, Boolean> mods = super.getModifiers();
		if (naturalArmor.getValue() != 0) {
			ArrayList<Modifier> toFix = new ArrayList<>();	// the mods we will change
			for (Modifier m : mods.keySet()) {
				if (m.getType() != null && m.getType().equals("Natural Armor")) {
					toFix.add(m);	// we modify all NA mods to add the enhancement value. if there is more than one active NA modifier then this will be incorrect but that should never happen
				}
			}
			for (Modifier m : toFix) {
				boolean active = mods.remove(m);
				Modifier newMod = new ImmutableModifier(m.getModifier()+naturalArmor.getValue(), m.getType(), m.getSource(), m.getCondition());
				mods.put(newMod, active);
			}
			if (toFix.size() == 0) {
				// no NA bonus was found so we need to add one
				mods.put(new ImmutableModifier(naturalArmor.getValue(), "Natural Armor"), true);
			}
		}
		return mods;
	}

	@Override
	public int getModifiersTotal() {
		return getModifiersTotal(getModifierSet(), null) + naturalArmor.getValue();
	}

	@Override
	public int getModifiersTotal(String type) {
		int total = getModifiersTotal(getModifierSet(), type);
		if (type != null && type.equals("Natural Armor")) {
			total += naturalArmor.getValue();
		}
		return total;
	}

	public Statistic getTouchAC() {
		return touchAC;
	}

	public Statistic getFlatFootedAC() {
		return flatFootedAC;
	}

	public Armor getArmor() {
		return armor;
	}

	public Shield getShield() {
		return shield;
	}

	public Statistic getNaturalArmor() {
		return naturalArmor;
	}

	public Modifier getArmorCheckPenalty() {
		return armorCheckPenalty;
	}

	// note that any modifiers added are added to the parent AC instance, not the touchAC instance
	class TouchAC extends Statistic {
		TouchAC(PropertyCollection parent) {
			super("ac.touch_ac", "Touch AC", parent);
		}

		@Override
		public void addModifier(Modifier m) {
			AC.this.addModifier(m);
		}

		@Override
		public void removeModifier(Modifier m) {
			AC.this.removeModifier(m);
		}

		// this implementation matches the definition in the Rule Compendium (take the total AC and subtract armor, shield and natural)
		@Override
		public Integer getValue() {
			int ac = AC.this.getValue();

			Map<Modifier,Boolean> map = AC.this.getModifiers();
			for (Modifier m : map.keySet()) {
				if (map.get(m) && m.getCondition() == null && m.getType() != null
						&& (m.getType().equals(Modifier.StandardType.ARMOR.toString())
								|| m.getType().equals(Modifier.StandardType.SHIELD.toString())
								|| m.getType().equals(Modifier.StandardType.NATURAL_ARMOR.toString()))) {
					ac -= m.getModifier();
				}
			}
			return ac;
		}

		@Override
		public Set<Modifier> getModifierSet() {
			Set<Modifier> mods = new HashSet<>(AC.this.modifiers);
			for (Modifier m : AC.this.modifiers) {
				if (m.getType() != null
						&& (m.getType().equals(Modifier.StandardType.ARMOR.toString())
								|| m.getType().equals(Modifier.StandardType.SHIELD.toString())
								|| m.getType().equals(Modifier.StandardType.NATURAL_ARMOR.toString()))) {
					mods.remove(m);
				}
			}
			return mods;
		}
	};

	// note that any modifiers added are added to the parent AC instance, not the flatFootedAC instance
	class FlatFootedAC extends Statistic {
		FlatFootedAC(PropertyCollection parent) {
			super("ac.flat-footed", "Flat-footed AC", parent);
		}

		@Override
		public void addModifier(Modifier m) {
			AC.this.addModifier(m);
		}

		@Override
		public void removeModifier(Modifier m) {
			AC.this.removeModifier(m);
		}

		// this implementation matches the definition in the Rule Compendium (take the total AC and subtract Dex and dodge modifiers)
		@Override
		public Integer getValue() {
			int ac = AC.this.getValue();

			Map<Modifier,Boolean> map = AC.this.getModifiers();
			for (Modifier m : map.keySet()) {
				if (map.get(m) && m.getCondition() == null && m.getType() != null &&
						(m.getType().equals(AbilityScore.Type.DEXTERITY.toString()) && m.getModifier() > 0 || m.getType().equals(Modifier.StandardType.DODGE.toString()))) {
					// note: only dex bonuses are removed, penalties should remain
					ac -= m.getModifier();
				}
			}
			return ac;
		}

		@Override
		public Set<Modifier> getModifierSet() {
			Set<Modifier> mods = new HashSet<>(AC.this.modifiers);
			for (Modifier m : AC.this.modifiers) {
				if (m.getType() != null && (m.getType().equals(AbilityScore.Type.DEXTERITY.toString()) && m.getModifier() > 0 || m.getType().equals(Modifier.StandardType.DODGE.toString()))) {
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
		public boolean proficient;
		public ItemDefinition item;

		protected Modifier modifier = null;		// the armor/shield modifier generated by this item that is applied to the AC
		protected Modifier enhancement = null;	// enhancement modifier applied to this item

		protected Shield(PropertyCollection parent) {
			super("ac.shield", "Shield", parent);
		}

		protected Shield(String n, String d, PropertyCollection parent) {
			super(n, d, parent);
		}

		@Override
		public Integer getValue() {
			return bonus + super.getValue();
		}

		@Override
		public void addModifier(Modifier m) {
			super.addModifier(m);
			updateModifier();
		}

		@Override
		public void removeModifier(Modifier m) {
			super.removeModifier(m);
			updateModifier();
		}

		protected void updateModifier() {
			if (modifier != null) {
				if (modifier.getModifier() == getValue()) return;	// no change
				AC.this.removeModifier(modifier);
				modifier = null;
			}
			if (getValue() != 0) {
				modifier = new ImmutableModifier(getValue(), getDescription());
				AC.this.addModifier(modifier);
			}
		}

		public int getBonus() {
			return bonus;
		}

		public void setBonus(int b) {
			if (bonus == b) return;
			bonus = b;
			updateModifier();
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
			updateModifier();
		}

		public int getACP() {
			return acp;
		}

		public void setACP(int v) {
			if (acp == v) return;
			acp = v;
			armorCheckPenalty.update();
		}

		@Override
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

		protected Armor(PropertyCollection parent) {
			super("ac.armor", "Armor", parent);
		}

		public int getMaxDex() {
			return dexMod == null ? 0 : dexMod.getLimit();
		}

		public void setMaxDex(int v) {
			if (dexMod == null || v == dexMod.getLimit()) return;
			dexMod.setLimit(v);
		}
	}

// TODO not sure if this should have type set or source set or if it should depend on where the penalty is from
// TODO will need to include encumberance eventually
// TODO should possibly be separate modifiers for armor and shield. will need separate modifiers or separate implementation for non-proficiency
	protected class ArmorCheckPenalty extends AbstractModifier {
		@Override
		public int getModifier() {
			return armor.acp + shield.acp;
		}

		@Override
		public String getType() {
			return "Armor Check Penalty";
		}

		private void update() {
			pcs.firePropertyChange("value", null, armor.acp + shield.acp);
		}
	}
}

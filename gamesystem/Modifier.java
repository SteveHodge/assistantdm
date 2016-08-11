package gamesystem;

import java.beans.PropertyChangeListener;

/*
 * Modifiers are bonuses or penalties that can be applied to various statistics. Bonuses generally have a type and do not stack
 * with other bonuses of the same type - only the higher value is used. Dodge bonuses, circumstance bonuses, and bonuses with no
 * type do stack (PHB 171 also includes racial bonuses). Penalties typically do not have a type, but in cases where they do they
 * do not stack with other penalties of the same type - only the larger penalty is used (PHB 171, Rules Compendium, but note that
 * the PHB glossary page 311 states that penalties always stack).
 *
 * Two modifiers from the same source don't stack (e.g. two ray of enfeeblements), even if they are untyped. PHB 171 (combining
 * magical effects).
 *
 * Bonus types as listed in the DMG:
 * Alchemical
 * Armor
 * Circumstance
 * Competence
 * Deflection
 * Dodge
 * Enhancement
 * Inherent
 * Insight
 * Luck
 * Morale
 * Natural Armor
 * Profane
 * Racial
 * Resistance
 * Sacred
 * Shield
 * Size
 *
 * Other values used in much the same way as modifiers include ability score modifiers and the armor check penalty. Note however
 * that the armor check penalty from armor stacks with the armor check penalty from a shield, but the penalty from high encumbrance
 * does not stack with armor check penalties.
 *
 *
 * Targets for enhancements:
 * Ability scores (strength, dexterity, constitution, intelligence, wisdom, charisma)
 * Saving throws (reflex, fortitude, will)
 * AC
 * Skills
 * Attack rolls
 * Damage rolls
 * Initiative
 *
 * Need to think about how this will apply with magic weapons and armor.
 *
 * A source creates a modifier which applies to a target. Any updates flow in that way too. A source needs to be able to remove
 * the modifier. But for ability scores it makes more sense for the setup to be driven by the target - e.g. AC has an interest in
 * the dexterity modifier.
 */

public interface Modifier {
	public enum StandardType {
		UNTYPED(null),
		ALCHEMICAL("Alchemical"),
		ARMOR("Armor"),
		CIRCUMSTANCE("Circumstance"),
		COMPETENCE("Competence"),
		DEFLECTION("Deflection"),
		DODGE("Dodge"),
		ENHANCEMENT("Enhancement"),
		INHERENT("Inherent"),
		INSIGHT("Insight"),
		LUCK("Luck"),
		MORALE("Morale"),
		NATURAL_ARMOR("Natural Armor"),
		PROFANE("Profane"),
		RACIAL("Racial"),
		RESISTANCE("Resistance"),
		SACRED("Sacred"),
		SHIELD("Shield"),
		SIZE("Size");

		private StandardType(String desc) {
			description = desc;
		}

		@Override
		public String toString() {
			return description;
		}

		private String description;
	}

	public int getModifier();
	public String getType();
	public String getSource();		// returns null if no specific source (typically source is only necessary for magic spells
	public String getCondition();	// returns null if no condition
	public void addPropertyChangeListener(PropertyChangeListener listener);
	public void removePropertyChangeListener(PropertyChangeListener listener);
	public int getID();
}

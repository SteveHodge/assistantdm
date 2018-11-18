package monsters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Attacks.AttackForm;
import gamesystem.BAB;
import gamesystem.Buff;
import gamesystem.Creature;
import gamesystem.CreatureProcessor;
import gamesystem.Feat;
import gamesystem.Feats;
import gamesystem.GrappleModifier;
import gamesystem.HPs;
import gamesystem.HitDiceProperty;
import gamesystem.InitiativeModifier;
import gamesystem.Levels;
import gamesystem.Modifier;
import gamesystem.MonsterType;
import gamesystem.Race;
import gamesystem.SaveProgression;
import gamesystem.SavingThrow;
import gamesystem.Size;
import gamesystem.Skills;
import gamesystem.core.SimpleValueProperty;
import monsters.StatisticsBlock.Field;


// TODO HitDice should be a property/statistic that registers listeners
// TODO fixup flatfooted/touch ac - currently setProperty() is setting these to override values which don't get updated by the AC stat

public class Monster extends Creature {
	public final static String xPROPERTY_AC_TOUCH = "AC: Touch";
	public final static String xPROPERTY_AC_FLATFOOTED = "AC: Flat Footed";

	public List<MonsterAttackRoutine> attackList;		// TODO should not be public. should be notified
	public List<MonsterAttackRoutine> fullAttackList;	// TODO should not be public. should be notified

	StatisticsBlock statisticsBlock;

	// Creatures a "blank" monster
	public Monster(String name) {
		this(name, new int[] { 10, 10, 10, 10, 10, 10 });
	}

	// array of ability score values (str,dex,con,int,wis,cha: same order as AbilityScore.Type). Scores of -1 indicate
	// non-abilities
	public Monster(String name, int[] scores) {
		this.name = new SimpleValueProperty<>("name", this, name);

		int i = 0;
		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			if (scores[i] >= 0) {
				final AbilityScore s = new AbilityScore(t, this);
				s.setBaseValue(scores[i]);
				abilities.put(t, s);
			}
			i++;
		}

		AbilityScore dex = abilities.get(AbilityScore.Type.DEXTERITY);
		initiative = new InitiativeModifier(dex, this);
		initiative.setValue(0);

		race = new Race(this);
		level = new Levels(this);
		hitDice = new HitDiceProperty(this, race, level, abilities.get(AbilityScore.Type.CONSTITUTION));
		hitDice.setupBonusHPs(this);

		for (SavingThrow.Type t : SavingThrow.Type.values()) {
			SavingThrow s = new SavingThrow(t, abilities.get(t.getAbilityType()), hitDice, this);
			//s.setBaseOverride(0);
			saves.put(t, s);
		}

		hps = new HPs(hitDice, this);

		size = new Size(this);

		// Missing dex: The rules on nonabilities state that the modifier is 0, but the MM is inconsistent in this
		// regard. Formian Queens are listed as not having a dex modifier to AC (following the rules as written) but
		// do have a -5 initiative (the rules state they should use their int modifier of +5). "Cooper's Corrected
		// Creature Codex" allegedly has the Formian Queen with a -5 modifier. Shriekers have a -5 dex modifier on
		// ac and initiative (correct for initiative as int is 0). Conceptually it makes more sense for something
		// missing dex to have a -5 modifier, just as helpless creatures and inanimate objects do (both are considered
		// to have a dex of 0). It's reasonable to argue that a Shrieker should have a dex of 0 (as it has no control
		// over it's form), and that a Formian Queen should either have no dex or an actual dex (she can't move but she's
		// not rigidly immobile).

		boolean missingAsZero = false;

		if (dex == null && missingAsZero) {
			// if there is no dex score then treat it as a score of 0
			AbilityScore zeroDex = new AbilityScore(AbilityScore.Type.DEXTERITY, this);
			zeroDex.setBaseValue(0);
			ac = new AC(zeroDex, size, this);
		} else {
			ac = new AC(dex, size, this);
		}

		skills = new Skills(abilities.values(), ac.getArmorCheckPenalty(), this);

		bab = new BAB(this, race, level);

		grapple = new GrappleModifier(this, bab, size, abilities.get(AbilityScore.Type.STRENGTH));

		feats = new Feats(this, this);

		attacks = new Attacks(this);
		// TODO size modifier to attack needs to be setup correctly
//		int sizeMod = getSize().getSizeModifier();
//		if (sizeMod != 0) attacks.addModifier(new ImmutableModifier(sizeMod, "Size"));
		attackList = new ArrayList<>();
		fullAttackList = new ArrayList<>();
	}

	public HitDiceProperty getHitDice() {
		return hitDice;
	}

	// TODO will want store progression for some monsters eventually
	SaveProgression getSaveProgression(SavingThrow.Type type) {
		MonsterType t = race.getType();
		if (t == MonsterType.ELEMENTAL) {
			if (type == SavingThrow.Type.FORTITUDE && (race.hasSubtype("Earth") || race.hasSubtype("Water"))) return SaveProgression.FAST;
			if (type == SavingThrow.Type.REFLEX && (race.hasSubtype("Air") || race.hasSubtype("Fire"))) return SaveProgression.FAST;
		}
		return t.getProgression(type);
	}

	// used for testing progression
	public int getSaveUsingProgression(SavingThrow.Type type, SaveProgression progression) {
		int value = getSavingThrowStatistic(type).getModifiersTotal();
		//System.out.println("Evaluating " + type + " using " + progression + " progression");
		//System.out.println("modifiers = " + value);
		if (hitDice.hasRaceHD()) {
			value += progression.getBaseSave(race.getHitDiceCount());
			//System.out.println("racial (" + race.getHitDiceCount() + " HD) = " + progression.getBaseSave(race.getHitDiceCount()));
		}
		value += level.getBaseSave(type);
		//System.out.println("level = " + level.getBaseSave(type));
		return value;
	}

	public class MonsterAttackRoutine {
		public List<MonsterAttackForm> attackForms;

		MonsterAttackRoutine(List<MonsterAttackForm> atks) {
			attackForms = atks;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for (int j = 0; j < attackForms.size(); j++) {
				MonsterAttackForm a = attackForms.get(j);
				if (j > 0) b.append((a.attack != null && a.attack.primary) ? " plus " : " and ");
				b.append(a.toString(j == 0));
			}
			return b.toString();
		}
	}

	public class MonsterAttackForm {
		public AttackForm attack;
		public int number;
		public boolean touch;
		public boolean automatic = false;
		public String description;		// copy of the description (this is also copied to AttackForm name if the attack is not automatic)
		public String damageExtra;		// any extra damage (this part is unaffected by changes to the creature's stats)
		public boolean damageParsed;	// true if 'damage' string was successfully parsed
		public String damageCritical;	// currently only used for output
		public boolean damageByWeapon;

		@Override
		public String toString() {
			return toString(false);
		}

		public String toString(boolean showSecLabel) {
			StringBuilder s = new StringBuilder();
			if (number > 1) s.append(number).append(" ");
			s.append(description);
			if (!automatic) {
				if (attack.ranged && attack.strLimit < Integer.MAX_VALUE) {
					s.append("(+").append(attack.strLimit).append(" Str bonus)");
				}

				// modifiers that apply to attack and damage
				Set<Modifier> dmgMods = attack.getDamageStatistic().getModifierSet();
				if (dmgMods != null && dmgMods.size() > 0) {
					boolean hasMod = false;
					for (Modifier m : dmgMods) {
						if (m.getSource().equals(AbilityScore.Type.STRENGTH.toString())) continue;
						if (!hasMod) s.append(" (");
						if (s.charAt(s.length() - 1) != '(') s.append(", ");
						if (m.getModifier() > 0) s.append("+");
						s.append(m.getModifier()).append(" ");
						if (m.getType() != null) {
							s.append(m.getType());
						} else {
							s.append(m.getSource());
						}
					}
					if (hasMod) s.append(")");
				}

				s.append(" ");
				s.append(attack.getAttacksDescription());

				if (showSecLabel && !attack.primary) s.append(" (secondary)");

				Set<Modifier> modifiers = attack.getModifierSet();
				Iterator<Modifier> iter = modifiers.iterator();
				while (iter.hasNext()) {
					Modifier m = iter.next();
					if (m.getType() != null && (m.getType().equals(size.getName())
							|| m.getType().equals(AbilityScore.Type.STRENGTH.toString())
							|| m.getType().equals(AbilityScore.Type.DEXTERITY.toString()))) {
						iter.remove();
					} else if (dmgMods != null) {
						// check if the modifier also applies to damage:
						for (Modifier mod : dmgMods) {
							if (mod.equals(m)) iter.remove();
						}
					}
				}
				if (modifiers.size() > 0) {
					boolean first = true;
					for (Modifier m : modifiers) {
						if (m.getType() != null) {
							if (first) {
								s.append(" (");
								first = false;
							} else {
								s.append(", ");
							}
							if (m.getModifier() > 0) s.append("+");
							s.append(m.getModifier()).append(" ").append(m.getType());
						}
					}
					if (!first) s.append(")");
				}
				if (attack.ranged)
					s.append(" ranged");
				else
					s.append(" melee");
				if (touch) s.append(" touch");
			}

			s.append(" (");
			if (damageParsed && attack != null) {
				if (damageByWeapon) s.append("by weapon ");
				s.append(attack.getDamage());
				if (damageCritical != null) s.append(damageCritical);
			}
			if (damageExtra.length() > 0) s.append(damageExtra);
			s.append(")");
			return s.toString();
		}
	}

	public boolean isEditable() {
		return true;
	}

	// we check both feats and special qualities
	@Override
	public boolean hasFeat(String feat) {
		if (feat == null) return false;
		if (feats.hasFeat(feat)) return true;		// check recognised feats
		// check other feats and properties
		String f = feat.toLowerCase();
		String feats = (String) getPropertyValue("field." + Field.FEATS.name());
		if (feats != null && feats.toLowerCase().contains(f)) return true;
		feats = (String) getPropertyValue("field." + Field.SPECIAL_QUALITIES.name());
		if (feats != null && feats.toLowerCase().contains(f)) return true;
		return false;
	}

	@Override
	public boolean hasFeat(String feat, AttackForm attack) {
		if (feat == null) return false;
		String f = feat.toLowerCase();
		// check recognised feats
		for (int i = 0; i < feats.getSize(); i++) {
			Feat ff = feats.get(i);
			if (ff.getName().equals(name) && ff.target.equals(attack.getDescription())) return true;
		}
		// check other feats and properties
		String feats = (String) getPropertyValue("field." + Field.FEATS.name());
		if (feats != null && feats.toLowerCase().contains(f + " (" + attack.getDescription() + ")")) return true;
		feats = (String) getPropertyValue("field." + Field.SPECIAL_QUALITIES.name());
		if (feats != null && feats.toLowerCase().contains(f + " (" + attack.getDescription() + ")")) return true;
		return false;
	}

	// returns the counts of regular feats and bonus feats parsed from the FEATS property. Does not consider the feats member.
	public int[] countFeats() {
		int[] counts = { 0, 0 };
		String list = (String) getPropertyValue("field." + Field.FEATS.name());
		if (list == null || list.equals("—")) return counts;

		String[] feats = list.split(",(?![^()]*+\\))");	// split on commas that aren't in parentheses
		for (String f : feats) {
			int count = 1;
			boolean bonus = false;
			f.trim();
			if (f.endsWith("B")) {
				f = f.substring(0, f.length() - 1);
				bonus = true;
			}
			if (f.contains("(")) {
				try {
					count = Integer.parseInt(f.substring(f.indexOf("(") + 1, f.indexOf(")")));
				} catch (NumberFormatException e) {
					// assume if it's not a number then it's a subtype list
					count = f.substring(f.indexOf("(") + 1, f.indexOf(")")).split(",").length;
				}
				f = f.substring(0, f.indexOf("(")).trim();
			}
			counts[bonus ? 1 : 0] += count;
		}
		return counts;
	}

	// TODO this is reimplementation of Creature version. should fold into that
	@Override
	public void executeProcess(CreatureProcessor processor) {
		processor.processCreature(this);

		for (AbilityScore s : abilities.values()) {
			processor.processAbilityScore(s);
		}

//		processor.processHitdice(hitDice);
		// FIXME need to process level and race
		processor.processHPs(hps);
		processor.processInitiative(initiative);
		processor.processSize(size);

		for (SavingThrow.Type t : saves.keySet()) {
			SavingThrow s = saves.get(t);
			processor.processSavingThrow(s);
		}

		processor.processAC(ac);

		processor.processAttacks(attacks, grapple);
		for (MonsterAttackRoutine a : attackList) {
			processor.processMonsterAttackForm(a);
		}
		for (MonsterAttackRoutine a : fullAttackList) {
			processor.processMonsterFullAttackForm(a);
		}

//		for (int i = 0; i < feats.getSize(); i++) {
//			processor.processFeat((Buff) feats.get(i));
//		}

		for (int i = 0; i < buffs.getSize(); i++) {
			Buff b = buffs.get(i);
			processor.processBuff(b);
		}

		if (statisticsBlock != null) processor.processStatisticsBlock(statisticsBlock);

		for (String prop : properties.keySet()) {
			if (prop.startsWith("extra")) {
				processor.processProperty(prop, properties.get(prop).getValue());
			}
		}
	}
}

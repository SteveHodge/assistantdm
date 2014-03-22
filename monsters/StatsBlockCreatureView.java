package monsters;

import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Creature;
import gamesystem.HPs;
import gamesystem.ImmutableModifier;
import gamesystem.Modifier;
import gamesystem.SavingThrow;
import gamesystem.Size;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import monsters.Monster.MonsterAttackForm;
import monsters.Monster.MonsterAttackRoutine;
import monsters.StatisticsBlock.AttackRoutine;
import monsters.StatisticsBlock.Field;
import monsters.StatisticsBlock.MonsterDetails;

// StatsBlockCreatureView is an adapter that provides a traditional statistics block style view of a Monster.
// Because listeners register with this class, there is the potential for updates to be missed if there are
// events generated by this view that do not generate events in the underlying Monster - only listeners of the
// event-generating instance would receive the event. There are two ways to avoid this issue: either ensure that
// all notifiable changes trigger an event on the monster, or ensure that only one instance of the view is
// created for each instance of a monster. Currently the second method is implemented but it might be limiting.
// The first method is generally in use too (as most field changes affect Monster properties that are notified).
// Attack and Full Attack changes, however, only generate property change events on the field (this should probably
// be changed).

public class StatsBlockCreatureView {
	public final static String PROPERTY_STATS_BLOCK = "statsblock";

	private static Map<Monster, StatsBlockCreatureView> views = new HashMap<>();

	private Monster creature;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private StatsBlockCreatureView(Monster c) {
		creature = c;
		creature.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				// XXX seem to need to invokeLater() but I don't think it should be necessary
				// TODO recheck without invokeLater - perhaps the issue was caused by multiple view instances
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						String prop = e.getPropertyName();
						pcs.firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());

						// need to generate events for the fields that are built from statistics
						// TODO need to handle attacks somehow
						if (prop.equals(Creature.PROPERTY_INITIATIVE)) {
							pcs.firePropertyChange(Field.INITIATIVE.name(), null, getField(Field.INITIATIVE));
						} else if (prop.equals(Creature.PROPERTY_AC)
								|| prop.equals(Monster.PROPERTY_AC_FLATFOOTED)
								|| prop.equals(Monster.PROPERTY_AC_TOUCH)
								|| prop.startsWith(Creature.PROPERTY_AC_COMPONENT_PREFIX)) {
							pcs.firePropertyChange(Field.AC.name(), null, getField(Field.AC));
						} else if (prop.startsWith(Creature.PROPERTY_ABILITY_PREFIX)) {
							pcs.firePropertyChange(Field.ABILITIES.name(), null, getField(Field.ABILITIES));
						} else if (prop.startsWith(Creature.PROPERTY_SAVE_PREFIX)) {
							pcs.firePropertyChange(Field.SAVES.name(), null, getField(Field.SAVES));
						} else if (prop.equals(Creature.PROPERTY_SIZE)) {
							pcs.firePropertyChange(Field.SIZE_TYPE.name(), null, getField(Field.SIZE_TYPE));
						} else if (prop.equals(Creature.PROPERTY_REACH)
								|| prop.equals(Creature.PROPERTY_SPACE)) {
							pcs.firePropertyChange(Field.SIZE_TYPE.name(), null, getField(Field.SIZE_TYPE));
						} else if (prop.equals(Creature.PROPERTY_HPS)
								|| prop.equals(Creature.PROPERTY_MAXHPS)) {
							pcs.firePropertyChange(Field.HITDICE.name(), null, getField(Field.HITDICE));
						} else if (prop.equals(Creature.PROPERTY_BAB)) {
							pcs.firePropertyChange(Field.BASE_ATTACK_GRAPPLE.name(), null, getField(Field.BASE_ATTACK_GRAPPLE));
							pcs.firePropertyChange(Field.ATTACK.name(), null, getField(Field.ATTACK));
							pcs.firePropertyChange(Field.FULL_ATTACK.name(), null, getField(Field.FULL_ATTACK));
						} else if (prop.equals(Creature.PROPERTY_NAME)) {
							pcs.firePropertyChange(Field.NAME.name(), null, getField(Field.NAME));
						} else {
							System.out.println("Unused update to " + prop);
						}
					}
				});
			}
		});
	}

	public static StatsBlockCreatureView getView(Monster c) {
		if (views.containsKey(c)) return views.get(c);
		StatsBlockCreatureView v = new StatsBlockCreatureView(c);
		views.put(c, v);
		return v;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		//System.out.println(this + " addPropertyChangeListener");
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public String getName() {
		return creature.getName();
	}

	public String getMonsterName() {
		StatisticsBlock stats = getStatsBlock();
		if (stats == null) return null;
		return stats.getName();
	}

	public static Monster createMonster(StatisticsBlock blk) {
		String name = blk.getName();

		int[] abilities = new int[6];
		int i = 0;
		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			abilities[i++] = blk.getAbilityScore(t);
		}

		Monster m = new Monster(name, abilities);

		m.setInitiativeModifier(blk.getInitiativeModifier());

		for (SavingThrow.Type t : SavingThrow.Type.values()) {
			SavingThrow s = m.getSavingThrowStatistic(t);
			int save = blk.getSavingThrow(t);
			if (save > Integer.MIN_VALUE) {
				s.setBaseValue(save - s.getValue());
			}
		}

		m.setHitDice(blk.getHitDice());

		HPs hps = m.getHPStatistic();
		hps.setMaximumHitPoints(blk.getDefaultHPs());

		Size size = m.getSizeStatistic();
		size.setBaseSize(blk.getSize());
		size.setBaseReach(blk.getReach());
		size.setBaseSpace(blk.getSpace());

		// TODO should apply armor and shield bonuses correctly
		AbilityScore dex = m.getAbilityStatistic(AbilityScore.Type.DEXTERITY);
		AC ac = m.getACStatistic();
		Set<Modifier> acMods = blk.getACModifiers();
		for (Modifier mod : acMods) {
			if (mod.getType().equals(AbilityScore.Type.DEXTERITY.name())) {
				if (dex == null) {
					System.out.println("WARN: " + blk.getName() + " has dex modifier in AC (" + mod.getModifier() + ") but no dex score");
				} else if (mod.getModifier() < dex.getModifierValue()) {
					// assume that armor is restricting the max dex bonus
					ac.getArmor().setMaxDex(mod.getModifier());
				} else if (mod.getModifier() > dex.getModifierValue()) {
					System.out.println("WARN: " + blk.getName() + " dex modifier in AC (" + mod.getModifier() + ") does not match ability score modifier (" + dex.getModifierValue() + ")");
				}
			} else if (mod.getType().equals(Modifier.StandardType.ARMOR.toString())) {
				ac.getArmor().setBonus(mod.getModifier());
				ac.getArmor().description = mod.getSource();
			} else if (mod.getType().equals(Modifier.StandardType.SHIELD.toString())) {
				ac.getShield().setBonus(mod.getModifier());
				ac.getShield().description = mod.getSource();
			} else {
				if (mod.getType().equals(AbilityScore.Type.WISDOM.name())) {
					AbilityScore wis = m.getAbilityStatistic(AbilityScore.Type.WISDOM);
					if (wis == null) {
						System.out.println("WARN: " + blk.getName() + " has wis modifier in AC (" + mod.getModifier() + ") but no wis score");
					} else {
						mod = wis.getModifier();
					}
				}
				ac.addModifier(mod);
			}
		}

		Attacks attacks = m.getAttacksStatistic();
		attacks.setBAB(blk.getBAB());
		int sizeMod = m.getSize().getSizeModifier();
		if (sizeMod != 0) attacks.addModifier(new ImmutableModifier(sizeMod, "Size"));

		m.setProperty(PROPERTY_STATS_BLOCK, blk);
		// need feats/special qualities before setting up attacks
		m.setProperty(Field.SPECIAL_QUALITIES.name(), blk.get(Field.SPECIAL_QUALITIES));
		m.setProperty(Field.FEATS.name(), blk.get(Field.FEATS));

		setAttackList(m, blk.getAttacks(false));
		setFullAttackList(m, blk.getAttacks(true));

		// add fields we don't use as extra properties:
		m.setProperty(Field.CLASS_LEVELS.name(), blk.get(Field.CLASS_LEVELS));
		m.setProperty(Field.SPEED.name(), blk.get(Field.SPEED));
		m.setProperty(Field.SPECIAL_ATTACKS.name(), blk.get(Field.SPECIAL_ATTACKS));
		m.setProperty(Field.SKILLS.name(), blk.get(Field.SKILLS));
		m.setProperty(Field.ENVIRONMENT.name(), blk.get(Field.ENVIRONMENT));
		m.setProperty(Field.ORGANIZATION.name(), blk.get(Field.ORGANIZATION));
		m.setProperty(Field.CR.name(), blk.get(Field.CR));
		m.setProperty(Field.TREASURE.name(), blk.get(Field.TREASURE));
		m.setProperty(Field.ALIGNMENT.name(), blk.get(Field.ALIGNMENT));
		m.setProperty(Field.ADVANCEMENT.name(), blk.get(Field.ADVANCEMENT));
		m.setProperty(Field.LEVEL_ADJUSTMENT.name(), blk.get(Field.LEVEL_ADJUSTMENT));

		return m;
	}

	static void setAttackList(Monster m, List<AttackRoutine> attacks) {
		m.attackList = getAttackList(m, attacks);
	}

	static void setFullAttackList(Monster m, List<AttackRoutine> attacks) {
		m.fullAttackList = getAttackList(m, attacks);
	}

	private static List<MonsterAttackRoutine> getAttackList(Monster m, List<AttackRoutine> attacks) {
		List<MonsterAttackRoutine> list = new ArrayList<>();
		for (AttackRoutine r : attacks) {
			List<MonsterAttackForm> atks = new ArrayList<>();
			for (AttackRoutine.Attack a : r.attacks) {
				atks.add(getMonsterAttackForm(m, r, a));
			}
			list.add(m.new MonsterAttackRoutine(atks));
		}
		return list;
	}

	private static MonsterAttackForm getMonsterAttackForm(Monster m, AttackRoutine r, AttackRoutine.Attack a) {
		MonsterAttackForm f = m.new MonsterAttackForm();
		Attacks attacks = m.getAttacksStatistic();

		f.description = a.description;
		f.number = a.number;
		f.touch = a.touch;
		f.automatic = a.automatic;
		f.damageExtra = a.damageExtra;
		f.damageParsed = a.damageParsed;
		f.damageByWeapon = a.byWeapon;

		if (!a.automatic) {
			a.calculateAttackBonus();	// TODO currently need this to set the enhancement bonus correctly. should move that code
			f.attack = attacks.addAttackForm(a.description);
			f.attack.natural = !a.manufactured;
			f.attack.primary = a.primary;
			f.attack.twoWeaponFighting = r.has_mfg_primary && r.has_mfg_secondary;
			if (f.attack.twoWeaponFighting) {
				if (f.attack.primary && !f.attack.natural) {
					f.attack.offhandLight = !r.has_non_light_secondary;
				} else if (!f.attack.primary && (f.attack.natural || !a.non_light)) {
					f.attack.offhandLight = true;
				}
			}
			f.attack.ranged = a.ranged;
			f.attack.canUseDex = a.weaponFinesseApplies;
			f.attack.strLimit = a.strLimit;
			f.attack.maxAttacks = a.attackBonuses.length;
//		attack.noStrPenalty =
//		attack.doublePADmg =
			if (a.damageParsed) {
				if (!a.byWeapon) f.attack.setBaseDamage(a.damageDice);
				f.attack.strMultiplier = a.strMultiplier;
				f.damageCritical = a.damageCritical;
				f.attack.weaponSpecApplies = a.weaponSpecApplies;
			}

			for (String mod : a.modifiers.keySet()) {
				f.attack.addModifier(new ImmutableModifier(a.modifiers.get(mod), mod));
			}

			for (String mod : a.damageModifiers.keySet()) {
				f.attack.addDamageModifier(new ImmutableModifier(a.modifiers.get(mod), mod));
			}

			if (a.enhancementBonus != 0)
				f.attack.setAttackEnhancement(a.enhancementBonus);
			else if (a.masterwork) f.attack.setMasterwork(true);

			if (a.weaponFocusApplies) {
				f.attack.addModifier(new ImmutableModifier(1, null, "Weapon Focus"));
			}

			f.attack.updateModifiers();

		} else if (a.damageParsed) {
			// automatic - we're prepending the dice and bonus in reverse order
			if (a.damageBonus != 0) f.damageExtra = a.damageBonus + f.damageExtra;
			if (a.damageBonus > 0) f.damageExtra = "+" + f.damageExtra;
			f.damageExtra = a.damageDice + f.damageExtra;
		}
		return f;
	}

	public void setField(Field field, String value) {
		// TODO remaining parsable fields need to be reparsed - AC

		if (field == Field.ABILITIES) {
			for (AbilityScore.Type t : AbilityScore.Type.values()) {
				int score = StatisticsBlock.parseAbilityScore(value, t);
				AbilityScore ability = creature.getAbilityStatistic(t);
				ability.setBaseValue(score);
			}

		} else if (field == Field.AC) {
//			AbilityScore dex = (AbilityScore) m.getStatistic(Creature.STATISTIC_DEXTERITY);
//			AC ac = (AC) m.getStatistic(Creature.STATISTIC_AC);
//			Set<Modifier> acMods = blk.getACModifiers();
//			for (Modifier mod : acMods) {
//				if (mod.getType().equals(AbilityScore.Type.DEXTERITY.name())) {
//					if (dex == null) {
//						System.out.println("WARN: " + blk.getName() + " has dex modifier in AC (" + mod.getModifier() + ") but no dex score");
//					} else if (mod.getModifier() < dex.getModifierValue()) {
//						// assume that armor is restricting the max dex bonus
//						ac.getArmor().setMaxDex(mod.getModifier());
//					} else if (mod.getModifier() > dex.getModifierValue()) {
//						System.out.println("WARN: " + blk.getName() + " dex modifier in AC (" + mod.getModifier() + ") does not match ability score modifier (" + dex.getModifierValue() + ")");
//					}
//				} else {
//					if (mod.getType().equals(AbilityScore.Type.WISDOM.name())) {
//						mod = ((AbilityScore) m.getStatistic(Creature.STATISTIC_DEXTERITY)).getModifier();
//					}
//					ac.addModifier(mod);
//				}
//			}

		} else if (field == Field.BASE_ATTACK_GRAPPLE) {
			int bab = StatisticsBlock.parseBAB(value);
			Attacks attacks = creature.getAttacksStatistic();
			attacks.setBAB(bab);

		} else if (field == Field.INITIATIVE) {
			int init = StatisticsBlock.parseInitiativeModifier(value);
			creature.setInitiativeModifier(init);

		} else if (field == Field.SAVES) {
			for (SavingThrow.Type t : SavingThrow.Type.values()) {
				SavingThrow s = creature.getSavingThrowStatistic(t);
				int save = StatisticsBlock.parseSavingThrow(value, t);
				if (save > Integer.MIN_VALUE) {
					s.setBaseValue(s.getBaseValue() + save - s.getValue());
				}
			}
		} else if (field == Field.ATTACK) {
			// TODO this should trigger a property change on the Monster
			setAttackList(creature, StatisticsBlock.parseAttacks(value, new MonsterDetails(creature)));
			pcs.firePropertyChange(Field.ATTACK.name(), null, getField(Field.ATTACK));

		} else if (field == Field.FULL_ATTACK) {
			// TODO this should trigger a property change on the Monster
			setFullAttackList(creature, StatisticsBlock.parseAttacks(value, new MonsterDetails(creature)));
			pcs.firePropertyChange(Field.FULL_ATTACK.name(), null, getField(Field.FULL_ATTACK));
		}

		if (creature.hasProperty(field.name())) {
			creature.setProperty(field.name(), value);
		}
	}

	public String getField(Field field) {
		StringBuilder s = new StringBuilder();
		if (field == Field.INITIATIVE) {
			if (creature.getInitiativeModifier() >= 0) s.append("+");
			s.append(creature.getInitiativeModifier());
		} else if (field == Field.AC) {
			s.append(creature.getAC());
			Map<Modifier, Boolean> map = creature.getACStatistic().getModifiers();
			if (map.size() > 0) {
				s.append(" (");
				boolean first = true;
				for (Modifier m : map.keySet()) {
					if (map.get(m)) {
						if (first) {
							first = false;
						} else {
							s.append(", ");
						}
						s.append(m);
					}
				}
				s.append(")");
			}
			s.append(", touch ").append(creature.getTouchAC()).append(", flat-footed ").append(creature.getFlatFootedAC());
		} else if (field == Field.ABILITIES) {
			for (AbilityScore.Type t : AbilityScore.Type.values()) {
				s.append(t.toString().substring(0, 3)).append(" ");
				AbilityScore score = creature.getAbilityStatistic(t);
				if (score != null) {
					s.append(score.getValue());
				} else {
					s.append("-");
				}
				if (t != AbilityScore.Type.CHARISMA) s.append(", ");
			}
		} else if (field == Field.SAVES) {
			for (SavingThrow.Type t : SavingThrow.Type.values()) {
				s.append(t.getAbbreviation()).append(" ");
				SavingThrow save = creature.getSavingThrowStatistic(t);
				if (save != null) {
					if (save.getValue() >= 0) s.append("+");
					s.append(save.getValue());
				} else {
					s.append("-");
				}
				if (t != SavingThrow.Type.WILL) s.append(", ");
			}
		} else if (field == Field.HITDICE) {
			// TODO need to add con mod to this:
			s.append(creature.hitDice).append(" (");
			if (creature.getHPStatistic().getValue() != creature.getHPStatistic().getMaximumHitPoints()) {
				s.append(creature.getHPStatistic().getValue()).append("/");
			}
			s.append(creature.getHPStatistic().getMaximumHitPoints()).append(" hp)");
		} else if (field == Field.SIZE_TYPE) {
			s.append(creature.getSizeStatistic().getSize());
		} else if (field == Field.SPACE_REACH) {
			s.append(creature.getSizeStatistic().getSpace() / 2);
			if (creature.getSizeStatistic().getSpace() % 2 == 1) s.append("�");
			s.append(" ft./").append(creature.getSizeStatistic().getReach()).append(" ft.");
		} else if (field == Field.ATTACK) {
			s.append(getAttackHTML(creature.attackList));
		} else if (field == Field.FULL_ATTACK) {
			s.append(getAttackHTML(creature.fullAttackList));
		} else if (field == Field.BASE_ATTACK_GRAPPLE) {
			Attacks a = creature.getAttacksStatistic();
			if (a.getBAB() >= 0) s.append("+");
			s.append(a.getBAB()).append("/");

			if (getStatsBlock() != null) {
				Set<String> subtypes = getStatsBlock().getSubtypes();
				if (subtypes.contains("Incorporeal") || subtypes.contains("Swarm")) {
					s.append("�");
				} else {
					if (a.getGrappleValue() >= 0) s.append("+");
					s.append(a.getGrappleValue());
				}
			} else {
				if (a.getGrappleValue() >= 0) s.append("+");
				s.append(a.getGrappleValue());
			}
		}

		StatisticsBlock stats = getStatsBlock();
		if (s.length() == 0) {
			if (creature.hasProperty(field.name()) && creature.getProperty(field.name()) != null) {
				s.append(creature.getProperty(field.name()));
			} else if (stats != null && stats.get(field) != null) {
				s.append(stats.get(field));
			}
		}

		if (stats != null && !isEquivalent(field, s.toString())) {
			return "<b>" + s.append("</b>");
		} else {
			return s.toString();
		}
	}

	// returns true if the calculated field is apparently the same as the field from the original block.
	// several common alternate characters are treated as equal (e.g. various dashes and minus, commas
	// and semicolons, etc). Special cases are included for certain fields.
	private boolean isEquivalent(Field field, String calculated) {
		StatisticsBlock stats = getStatsBlock();

		if (calculated == null) calculated = "";
		if (stats.get(field) == null && !calculated.equals("")) return false;

		String cleanValue = "";
		if (stats.get(field) != null) cleanValue = stats.get(field).replaceAll("[���]", "-").replaceAll("\\s+", " ");
		String cleanBuilt = calculated.replaceAll("[��]", "-");

		if (field == Field.AC) {
			cleanValue = cleanValue.replaceFirst("\\(.*\\)", "()");	// strip modifiers from ac
			cleanBuilt = cleanBuilt.replaceFirst("\\(.*\\)", "()");	// strip modifiers from ac
		} else if (field == Field.ATTACK || field == Field.FULL_ATTACK) {
			cleanValue = cleanValue.replace(";", ",").replace("*", "");
			cleanBuilt = cleanBuilt.replace(";", ",");
		}

		if (!cleanValue.equals(cleanBuilt)) {
			System.out.println("   " + cleanValue);
			System.out.println("!= " + cleanBuilt);
			System.out.println();
		}
		return cleanValue.equals(cleanBuilt);
	}

	private StatisticsBlock getStatsBlock() {
		return (StatisticsBlock) creature.getProperty(PROPERTY_STATS_BLOCK);
	}

	private String getAttackHTML(List<MonsterAttackRoutine> attackRoutines) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < attackRoutines.size(); i++) {
			if (i > 0) {
				b.append("; or ");
			}
			b.append(attackRoutines.get(i));
		}
		return b.toString();
	}

	public String getHTML() {
		StringBuilder s = new StringBuilder();
		s.append("<html><table><tr><td></td><td>").append(getName()).append("</td><td>").append(getName()).append("</td></tr>");

		for (Field p : Field.getStandardOrder()) {
			s.append("<tr><td>").append(p).append("</td><td>");
			s.append(getField(p));
			StatisticsBlock stats = getStatsBlock();
			if (stats != null) s.append("</td><td>").append(stats.get(p));
			s.append("</td></tr>");
		}

		s.append("</table></html>");
		return s.toString();
	}
}

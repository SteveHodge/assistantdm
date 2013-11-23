package party;

import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Attacks.AttackForm;
import gamesystem.HPs;
import gamesystem.ImmutableModifier;
import gamesystem.InitiativeModifier;
import gamesystem.Modifier;
import gamesystem.SavingThrow;
import gamesystem.Size;
import gamesystem.Statistic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import monsters.HitDice;
import monsters.StatisticsBlock;
import monsters.StatisticsBlock.AttackRoutine;
import monsters.StatisticsBlock.Field;

/*
 * Monster implemented with Statistics. Will probably be renamed to Monster once AdhocMonster is removed
 */

public class DetailedMonster extends DetailedCreature implements Monster {
	public StatisticsBlock stats;
	public List<MonsterAttackRoutine> attackList;
	public List<MonsterAttackRoutine> fullAttackList;
	private HitDice hitDice;

	private PropertyChangeListener statListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == hps) {
				pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
			} else if (evt.getSource() == size) {
				if (evt.getPropertyName().equals("value")) {
					pcs.firePropertyChange(PROPERTY_SIZE, evt.getOldValue(), evt.getNewValue());
				} else if (evt.getPropertyName().equals("space")) {
					pcs.firePropertyChange(PROPERTY_SPACE, evt.getOldValue(), evt.getNewValue());
				} else if (evt.getPropertyName().equals("reach")) {
					pcs.firePropertyChange(PROPERTY_REACH, evt.getOldValue(), evt.getNewValue());
				}
			} else if (evt.getSource() instanceof AbilityScore) {
				AbilityScore score = (AbilityScore) evt.getSource();
				pcs.firePropertyChange(PROPERTY_ABILITY_PREFIX + score.getName(), evt.getOldValue(), evt.getNewValue());
			} else if (evt.getSource() == initiative) {
				pcs.firePropertyChange(PROPERTY_INITIATIVE, evt.getOldValue(), evt.getNewValue());
			} else if (evt.getSource() instanceof SavingThrow) {
				SavingThrow save = (SavingThrow) evt.getSource();
				pcs.firePropertyChange(PROPERTY_SAVE_PREFIX + save.getName(), evt.getOldValue(), evt.getNewValue());
			} else if (evt.getSource() == ac) {
				pcs.firePropertyChange(PROPERTY_AC, evt.getOldValue(), evt.getNewValue());
			} else if (evt.getSource() == attacks) {
				pcs.firePropertyChange(PROPERTY_BAB, evt.getOldValue(), evt.getNewValue());
			} else {
				System.out.println("Unknown property change event: " + evt);
			}
		}
	};

	public DetailedMonster(StatisticsBlock blk) {
		stats = blk;

		name = blk.getName();

		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			final AbilityScore s = new AbilityScore(t);
			int score = blk.getAbilityScore(t);
			if (score >= 0) {
				s.setBaseValue(score);
				s.addPropertyChangeListener(statListener);
				s.getModifier().addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						//System.out.println(PROPERTY_ABILITY_PREFIX+s.getName()+": "+e.getOldValue()+" -> "+ e.getNewValue());
						pcs.firePropertyChange(PROPERTY_ABILITY_PREFIX + s.getName(), e.getOldValue(), e.getNewValue());
					}
				});
				abilities.put(t, s);
			}
		}

		AbilityScore dex = abilities.get(AbilityScore.Type.DEXTERITY);
		initiative = new InitiativeModifier(dex);
		initiative.setBaseValue(blk.getInitiativeModifier() - initiative.getValue());
		initiative.addPropertyChangeListener(statListener);

		for (SavingThrow.Type t : SavingThrow.Type.values()) {
			int save = blk.getSavingThrow(t);
			if (save > Integer.MIN_VALUE) {
				SavingThrow s = new SavingThrow(t, abilities.get(t.getAbilityType()));
				s.setBaseValue(save - s.getValue());
				s.addPropertyChangeListener(statListener);
				saves.put(t, s);
			}
		}

		hitDice = blk.getHitDice();

		hps = new HPs(abilities.get(AbilityScore.Type.CONSTITUTION), hitDice);
		hps.setMaximumHitPoints(blk.getDefaultHPs());
		hps.addPropertyChangeListener(statListener);

		size = new Size();
		size.setBaseSize(blk.getSize());
		size.setBaseReach(blk.getReach());
		size.setBaseSpace(blk.getSpace());
		size.addPropertyChangeListener(statListener);

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
			AbilityScore zeroDex = new AbilityScore(AbilityScore.Type.DEXTERITY);
			zeroDex.setBaseValue(0);
			ac = new AC(zeroDex);
		} else {
			ac = new AC(dex);
		}
		ac.addPropertyChangeListener(statListener);

		Set<Modifier> acMods = blk.getACModifiers();
		for (Modifier m : acMods) {
			if (m.getType().equals(AbilityScore.Type.DEXTERITY.name())) {
				if (dex == null) {
					System.out.println("WARN: " + blk.getName() + " has dex modifier in AC (" + m.getModifier() + ") but no dex score");
				} else if (m.getModifier() < dex.getModifierValue()) {
					// assume that armor is restricting the max dex bonus
					ac.getArmor().setMaxDex(m.getModifier());
				} else if (m.getModifier() > dex.getModifierValue()) {
					System.out.println("WARN: " + blk.getName() + " dex modifier in AC (" + m.getModifier() + ") does not match ability score modifier (" + dex.getModifierValue() + ")");
				}
			} else {
				if (m.getType().equals(AbilityScore.Type.WISDOM.name())) {
					m = abilities.get(AbilityScore.Type.WISDOM).getModifier();
				}
				ac.addModifier(m);
			}
		}

		attacks = new Attacks(this);
		attacks.setBAB(stats.getBAB());
		int sizeMod = getSize().getSizeModifier();
		if (sizeMod != 0) attacks.addModifier(new ImmutableModifier(sizeMod, "Size"));
		attackList = getAttackList(false);
		fullAttackList = getAttackList(true);
		attacks.addPropertyChangeListener(statListener);
	}

	public HitDice getHitDice() {
		return hitDice;
	}

	private List<MonsterAttackRoutine> getAttackList(boolean full) {
		List<MonsterAttackRoutine> list = new ArrayList<MonsterAttackRoutine>();
		for (AttackRoutine r : stats.getAttacks(full)) {
			List<MonsterAttackForm> atks = new ArrayList<MonsterAttackForm>();
			for (AttackRoutine.Attack a : r.attacks) {
				atks.add(new MonsterAttackForm(r, a));
			}
			list.add(new MonsterAttackRoutine(atks));
		}
		return list;
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

		MonsterAttackForm(AttackRoutine r, AttackRoutine.Attack a) {
			description = a.description;
			number = a.number;
			touch = a.touch;
			automatic = a.automatic;
			damageExtra = a.damageExtra;
			damageParsed = a.damageParsed;
			damageByWeapon = a.byWeapon;

			if (!a.automatic) {
				a.calculateAttackBonus();	// TODO currently need this to set the enhancement bonus correctly. should move that code
				attack = attacks.addAttackForm(a.description);
				attack.natural = !a.manufactured;
				attack.primary = a.primary;
				attack.twoWeaponFighting = r.has_mfg_primary && r.has_mfg_secondary;
				if (attack.twoWeaponFighting) {
					if (attack.primary && !attack.natural) {
						attack.offhandLight = !r.has_non_light_secondary;
					} else if (!attack.primary && (attack.natural || !a.non_light)) {
						attack.offhandLight = true;
					}
				}
				attack.ranged = a.ranged;
				attack.canUseDex = a.weaponFinesseApplies;
				attack.strLimit = a.strLimit;
				attack.maxAttacks = a.attackBonuses.length;
//			attack.noStrPenalty =
//			attack.doublePADmg =
				if (a.damageParsed) {
					if (!a.byWeapon) attack.setBaseDamage(a.damageDice);
					attack.strMultiplier = a.strMultiplier;
					damageCritical = a.damageCritical;
					attack.weaponSpecApplies = a.weaponSpecApplies;
				}

				for (String mod : a.modifiers.keySet()) {
					attack.addModifier(new ImmutableModifier(a.modifiers.get(mod), mod));
				}

				for (String mod : a.damageModifiers.keySet()) {
					attack.addDamageModifier(new ImmutableModifier(a.modifiers.get(mod), mod));
				}

				if (a.enhancementBonus != 0) attack.setAttackEnhancement(a.enhancementBonus);
				else if (a.masterwork) attack.setMasterwork(true);

				if (a.weaponFocusApplies) {
					attack.addModifier(new ImmutableModifier(1, null, "Weapon Focus"));
				}

				attack.updateModifiers();

			} else if (a.damageParsed) {
				// automatic - we're prepending the dice and bonus in reverse order
				if (a.damageBonus != 0) damageExtra = a.damageBonus + damageExtra;
				if (a.damageBonus > 0) damageExtra = "+" + damageExtra;
				damageExtra = a.damageDice + damageExtra;
			}
		}

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
				if (attack.damageMods != null && attack.damageMods.size() > 0) {
					s.append("(");
					for (Modifier m : attack.damageMods) {
						if (s.charAt(s.length() - 1) != '(') s.append(", ");
						if (m.getModifier() > 0) s.append("+");
						s.append(m.getModifier()).append(" ").append(m.getType());
					}
					s.append(")");
				}

				s.append(" ");
				s.append(attack.getAttacksDescription());

				if (showSecLabel && !attack.primary) s.append(" (secondary)");

				Set<Modifier> modifiers = attack.getModifierSet();
				Iterator<Modifier> iter = modifiers.iterator();
				while (iter.hasNext()) {
					Modifier m = iter.next();
					if (m.getType() != null && (m.getType().equals("Size")
							|| m.getType().equals(AbilityScore.Type.STRENGTH.toString())
							|| m.getType().equals(AbilityScore.Type.DEXTERITY.toString()))) {
						iter.remove();
					} else if (attack.damageMods != null) {
						// check if the modifier also applies to damage:
						for (Modifier mod : attack.damageMods) {
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

	@Override
	public Statistic getStatistic(String name) {
		if (name.equals(STATISTIC_STRENGTH)) {
			return abilities.get(AbilityScore.Type.STRENGTH);
		} else if (name.equals(STATISTIC_INTELLIGENCE)) {
			return abilities.get(AbilityScore.Type.INTELLIGENCE);
		} else if (name.equals(STATISTIC_WISDOM)) {
			return abilities.get(AbilityScore.Type.WISDOM);
		} else if (name.equals(STATISTIC_DEXTERITY)) {
			return abilities.get(AbilityScore.Type.DEXTERITY);
		} else if (name.equals(STATISTIC_CONSTITUTION)) {
			return abilities.get(AbilityScore.Type.CONSTITUTION);
		} else if (name.equals(STATISTIC_CHARISMA)) {
			return abilities.get(AbilityScore.Type.CHARISMA);
		} else if (name.equals(STATISTIC_FORTITUDE_SAVE)) {
			return saves.get(SavingThrow.Type.FORTITUDE);
		} else if (name.equals(STATISTIC_WILL_SAVE)) {
			return saves.get(SavingThrow.Type.WILL);
		} else if (name.equals(STATISTIC_REFLEX_SAVE)) {
			return saves.get(SavingThrow.Type.REFLEX);
		} else if (name.equals(STATISTIC_AC)) {
			return ac;
		} else if (name.equals(STATISTIC_ARMOR)) {
			return ac.getArmor();
		} else if (name.equals(STATISTIC_SHIELD)) {
			return ac.getShield();
		} else if (name.equals(STATISTIC_NATURAL_ARMOR)) {
			return ac.getNaturalArmor();
		} else if (name.equals(STATISTIC_INITIATIVE)) {
			return initiative;
//		} else if (name.equals(STATISTIC_SKILLS)) {
//			return skills;
//		} else if (name.startsWith(STATISTIC_SKILLS+".")) {
//			SkillType type = SkillType.getSkill(name.substring(STATISTIC_SKILLS.length()+1));
//			return skills.getSkill(type);
////		} else if (name.equals(STATISTIC_SAVING_THROWS)) {
////			// TODO implement?
////			return null;
//		} else if (name.equals(STATISTIC_LEVEL)) {
//			return level;
		} else if (name.equals(STATISTIC_HPS)) {
			return hps;
		} else if (name.equals(STATISTIC_ATTACKS)) {
			return attacks;
		} else if (name.equals(STATISTIC_DAMAGE)) {
			return attacks.getDamageStatistic();
		} else if (name.equals(STATISTIC_SIZE)) {
			return size;
		} else {
			System.out.println("Unknown statistic " + name);
			return null;
		}
	}

	public String getField(Field p) {
		StringBuilder s = new StringBuilder();
		if (p == Field.INITIATIVE) {
			if (getInitiativeModifier() >= 0) s.append("+");
			s.append(getInitiativeModifier());
		} else if (p == Field.AC) {
			s.append(getAC());
			Map<Modifier, Boolean> map = ac.getModifiers();
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
			s.append(", touch ").append(getTouchAC()).append(", flat-footed ").append(getFlatFootedAC());
		} else if (p == Field.ABILITIES) {
			for (AbilityScore.Type t : AbilityScore.Type.values()) {
				s.append(t.toString().substring(0, 3)).append(" ");
				AbilityScore score = abilities.get(t);
				if (score != null) {
					s.append(score.getValue());
				} else {
					s.append("-");
				}
				if (t != AbilityScore.Type.CHARISMA) s.append(", ");
			}
		} else if (p == Field.SAVES) {
			for (SavingThrow.Type t : SavingThrow.Type.values()) {
				s.append(t.getAbbreviation()).append(" ");
				SavingThrow save = saves.get(t);
				if (save != null) {
					if (save.getValue() >= 0) s.append("+");
					s.append(save.getValue());
				} else {
					s.append("-");
				}
				if (t != SavingThrow.Type.WILL) s.append(", ");
			}
		} else if (p == Field.HITDICE) {
			// TODO need to add con mod to this:
			s.append(hitDice).append(" (");
			if (hps.getValue() != hps.getMaximumHitPoints()) {
				s.append(hps.getValue()).append("/");
			}
			s.append(hps.getMaximumHitPoints()).append(" hp)");
		} else if (p == Field.SIZE_TYPE) {
			s.append(size.getSize());
		} else if (p == Field.SPACE_REACH) {
			s.append(size.getSpace() / 2);
			if (size.getSpace() % 2 == 1) s.append("½");
			s.append(" ft./").append(size.getReach()).append(" ft.");
		} else if (p == Field.ATTACK) {
			s.append(getAttackHTML(attackList));
		} else if (p == Field.FULL_ATTACK) {
			s.append(getAttackHTML(fullAttackList));
		}

		if (s.length() == 0) {
			s.append(stats.get(p));
		}

		return s.toString();
	}

	@Override
	public String getStatsBlockHTML() {
//		return stats.getHTML();

		StringBuilder s = new StringBuilder();
		s.append("<html><table><tr><td></td><td>").append(getName()).append("</td><td>").append(stats.getName()).append("</td></tr>");

		for (Field p : Field.getStandardOrder()) {
			s.append("<tr><td>").append(p).append("</td><td>");
			s.append(getField(p));
			s.append("</td><td>").append(stats.get(p)).append("</td></tr>");
		}

		s.append("</table></html>");
		return s.toString();
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

	public boolean isEditable() {
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void setName(String name) {
//		System.out.println("setName('" + name + "')");
		String old = this.name;
		this.name = name;
		firePropertyChange(PROPERTY_NAME, old, name);
	}

// XXX this sets the total value but the Character version sets the base value
	@Override
	public void setInitiativeModifier(int i) {
		initiative.setBaseValue(i - initiative.getValue());
	}

// we check both feats and special qualities
	@Override
	public boolean hasFeat(String feat) {
		if (feat == null) return false;
		String f = feat.toLowerCase();
		String feats = stats.get(StatisticsBlock.Field.FEATS);
		if (feats != null && feats.toLowerCase().contains(f)) return true;
		feats = stats.get(StatisticsBlock.Field.SPECIAL_QUALITIES);
		if (feats != null && feats.toLowerCase().contains(f)) return true;
		return false;
	}

	@Override
	public Object getProperty(String prop) {
		if (prop.equals(PROPERTY_NAME)) return name;
		if (prop.equals(PROPERTY_MAXHPS)) return getMaximumHitPoints();
		if (prop.equals(PROPERTY_WOUNDS)) return getWounds();
		if (prop.equals(PROPERTY_NONLETHAL)) return getNonLethal();
		if (prop.equals(PROPERTY_INITIATIVE)) return getInitiativeModifier();
		if (prop.equals(PROPERTY_AC)) return getAC();
		if (prop.equals(PROPERTY_AC_FLATFOOTED)) return getFlatFootedAC();
		if (prop.equals(PROPERTY_AC_TOUCH)) return getTouchAC();
		if (prop.equals(PROPERTY_SPACE)) return getSpace();
		if (prop.equals(PROPERTY_REACH)) return getReach();
		if (prop.equals(PROPERTY_BAB)) return attacks.getBAB();
		System.out.println("Attempt to get unknown property: " + prop);
		return null;
	}

	@Override
	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_NAME)) setName((String) value);
		else if (prop.equals(PROPERTY_MAXHPS))
			setMaximumHitPoints((Integer) value);
		else if (prop.equals(PROPERTY_WOUNDS))
			setWounds((Integer) value);
		else if (prop.equals(PROPERTY_NONLETHAL))
			setNonLethal((Integer) value);
		else if (prop.equals(PROPERTY_INITIATIVE))
			setInitiativeModifier((Integer) value);
		else if (prop.equals(PROPERTY_AC))
			setAC((Integer) value);
		else if (prop.equals(PROPERTY_AC_FLATFOOTED))
			setFlatFootedAC((Integer) value);
		else if (prop.equals(PROPERTY_AC_TOUCH))
			setTouchAC((Integer) value);
		else if (prop.equals(PROPERTY_SPACE))
			setSpace((Integer) value);
		else if (prop.equals(PROPERTY_REACH))
			setReach((Integer) value);
		else if (prop.equals(PROPERTY_BAB))
			attacks.setBAB((Integer) value);
		else {
			System.out.println("Attempt to set unknown property: " + prop + " to " + value);
		}
	}
}

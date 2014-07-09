package monsters;

import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Attacks.AttackForm;
import gamesystem.Buff;
import gamesystem.Creature;
import gamesystem.CreatureProcessor;
import gamesystem.HPs;
import gamesystem.InitiativeModifier;
import gamesystem.Modifier;
import gamesystem.SavingThrow;
import gamesystem.Size;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import monsters.StatisticsBlock.Field;


// TODO HitDice should be a property/statistic that registers listeners
// TODO fixup flatfooted/touch ac - currently setProperty() is setting these to override values which don't get updated by the AC stat

public class Monster extends Creature {
	public final static String PROPERTY_AC_TOUCH = "AC: Touch";
	public final static String PROPERTY_AC_FLATFOOTED = "AC: Flat Footed";

	public List<MonsterAttackRoutine> attackList;		// TODO should not be public. should be notified
	public List<MonsterAttackRoutine> fullAttackList;	// TODO should not be public. should be notified
	HitDice hitDice;	// TODO should be notified

	// this listener forwards events from Statstics as property changes
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
				pcs.firePropertyChange(PROPERTY_AC_TOUCH, 0, getTouchAC());
				pcs.firePropertyChange(PROPERTY_AC_FLATFOOTED, 0, getFlatFootedAC());
			} else if (evt.getSource() == attacks) {
				pcs.firePropertyChange(PROPERTY_BAB, evt.getOldValue(), evt.getNewValue());
			} else {
				System.out.println("Unknown property change event: " + evt);
			}
		}
	};

	// Creatures a "blank" monster
	public Monster(String name) {
		this(name, new int[] { 10, 10, 10, 10, 10, 10 });
	}

	// array of ability score values (str,dex,con,int,wis,cha: same order as AbilityScore.Type). Scores of -1 indicate
	// non-abilities
	public Monster(String name, int[] scores) {
		this.name = name;

		int i = 0;
		for (AbilityScore.Type t : AbilityScore.Type.values()) {
			if (scores[i] >= 0) {
				final AbilityScore s = new AbilityScore(t);
				s.setBaseValue(scores[i]);
				s.addPropertyChangeListener(statListener);
				s.getModifier().addPropertyChangeListener(e -> pcs.firePropertyChange(PROPERTY_ABILITY_PREFIX + s.getName(), e.getOldValue(), e.getNewValue()));
				abilities.put(t, s);
			}
			i++;
		}

		AbilityScore dex = abilities.get(AbilityScore.Type.DEXTERITY);
		initiative = new InitiativeModifier(dex);
		initiative.setBaseValue(0);
		initiative.addPropertyChangeListener(statListener);

		for (SavingThrow.Type t : SavingThrow.Type.values()) {
			SavingThrow s = new SavingThrow(t, abilities.get(t.getAbilityType()), null);
			s.setBaseOverride(0);
			s.addPropertyChangeListener(statListener);
			saves.put(t, s);
		}

		hitDice = HitDice.parse("1d8");

		hps = new HPs(abilities.get(AbilityScore.Type.CONSTITUTION), hitDice);
		hps.setMaximumHitPoints(0);
		hps.addPropertyChangeListener(statListener);

		size = new Size();
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

		attacks = new Attacks(this);
		attacks.setBAB(0);
		// TODO size modifier to attack needs to be setup correctly
//		int sizeMod = getSize().getSizeModifier();
//		if (sizeMod != 0) attacks.addModifier(new ImmutableModifier(sizeMod, "Size"));
		attackList = new ArrayList<>();
		fullAttackList = new ArrayList<>();
		attacks.addPropertyChangeListener(statListener);
	}

	public HitDice getHitDice() {
		return hitDice;
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

	public void setHitDice(HitDice hd) {
		// TODO should check con bonus is applied correctly
		hitDice = hd;
		hps.setHitDice(hitDice);
		hps.setMaximumHitPoints((int) hitDice.getMeanRoll());
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
		String feats = (String) getProperty(Field.FEATS.name());
		if (feats != null && feats.toLowerCase().contains(f)) return true;
		feats = (String) getProperty(Field.SPECIAL_QUALITIES.name());
		if (feats != null && feats.toLowerCase().contains(f)) return true;
		return false;
	}

	@Override
	public Object getProperty(String prop) {
		if (prop.equals(PROPERTY_AC_FLATFOOTED))
			return getFlatFootedAC();
		else if (prop.equals(PROPERTY_AC_TOUCH))
			return getTouchAC();
		else
			return super.getProperty(prop);
	}

	@Override
	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_AC_FLATFOOTED))
			setFlatFootedAC((Integer) value);
		else if (prop.equals(PROPERTY_AC_TOUCH))
			setTouchAC((Integer) value);
		else
			super.setProperty(prop, value);
	}

	// TODO this is reimplementation of Creature version. should fold into that
	@Override
	public void executeProcess(CreatureProcessor processor) {
		processor.processCreature(this);

		for (AbilityScore s : abilities.values()) {
			processor.processAbilityScore(s);
		}

		processor.processHitdice(hitDice);
		processor.processHPs(hps);
		processor.processInitiative(initiative);
		processor.processSize(size);

		for (SavingThrow.Type t : saves.keySet()) {
			SavingThrow s = saves.get(t);
			processor.processSavingThrow(s);
		}

		processor.processAC(ac);

		processor.processAttacks(attacks);
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

		for (String prop : extraProperties.keySet()) {
			processor.processProperty(prop, extraProperties.get(prop));
		}
	}
}

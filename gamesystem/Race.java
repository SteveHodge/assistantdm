package gamesystem;

import java.util.ArrayList;
import java.util.List;

import gamesystem.core.AbstractProperty;
import gamesystem.core.PropertyValueEvent;
import gamesystem.core.PropertyCollection;
import gamesystem.dice.HDDice;

/* Race encapsulates monster advancement: it is the monster equivalent of Levels. As a Property it encapsulates a creature's type and subtypes (if any).
 * The String value reported is a description: "type (subtype, subtype,...)".
 */

public class Race extends AbstractProperty<String> {
	protected MonsterType type;
	public List<String> subtypes = new ArrayList<String>();	// subtypes			// TODO should be protected
	protected HDDice hitDice;

	public Race(PropertyCollection parent) {
		super("race", parent);
		type = MonsterType.HUMANOID;
		hitDice = new HDDice(type.getHitDiceType());
	}

	@Override
	public String getValue() {
		return toString();
	}

	public MonsterType getType() {
		return type;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(type);
		if (subtypes.size() > 0) {
			b.append(" (");
			b.append(String.join(", ", subtypes));
			b.append(")");
		}
		return b.toString();
	}

	public void setType(MonsterType t) {
		if (type == t) return;
		String old = toString();
		type = t;
		fireEvent(new PropertyValueEvent<>(this, old));
	}

	public boolean hasSubtype(String t) {
		return subtypes.contains(t);
	}

	public void addSubtype(String s) {
		String old = toString();
		subtypes.add(s);
		fireEvent(new PropertyValueEvent<>(this, old));
	}

	// Returns the original type as specified in the "Augmented..." subtype, if any
	public MonsterType getAugmentedType() {
		for (String subtype : subtypes) {
			if (subtype.startsWith("Augmented ")) {
				return MonsterType.getMonsterType(subtype.substring(10));
			}
		}
		return null;
	}

	public int getBAB() {
		if (type != null) {
			return type.getBAB(hitDice.getNumber());
		}
		return 0;
	}

	// save progression can vary even within a monster type so this might not be correct for a specific monster
	public int getBaseSave(SavingThrow.Type t) {
		if (type != null) {
			return type.getBaseSave(t, hitDice.getNumber());
		}
		return 0;
	}

	public int getHitDiceCount() {
		return hitDice.getNumber();
	}

	public HDDice getHitDice() {
		return hitDice;
	}

	// TODO do some verification on this
	public void setHitDice(HDDice hd) {
		//System.out.println("Setting racial hitdice to " + hd);
		String old = toString();
		hitDice = hd;
		fireEvent(new PropertyValueEvent<>(this, old));
	}

	public void setHitDiceCount(int count) {
		String old = toString();
		hitDice = new HDDice(count, hitDice.getType(), hitDice.getConstant());
		// TODO add any additional dice?
		fireEvent(new PropertyValueEvent<>(this, old));
	}
}

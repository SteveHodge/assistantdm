package gamesystem.dice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// CombinedDice is similar to DiceList except that it only accepts SimpleDice and it will combine dice of the same type
// (e.g. 1d6 + 1d6 becomes 2d6). It also includes a constant modifier.
public class CombinedDice implements Dice {
	Map<Integer, SimpleDice> dice = new HashMap<>();
	int constant = 0;

	public void add(SimpleDice d) {
		SimpleDice existing = dice.get(d.type);
		if (existing != null) {
			existing.number += d.number;
		} else {
			dice.put(d.type, new SimpleDice(d.number, d.type));
		}
	}

	// note: will throw an exception if d has fractional number
	public void add(HDDice d) {
		if (d.number < 1) throw new IllegalArgumentException("CombinedDice cannot accept dice with fractional numbers");
		SimpleDice existing = dice.get(d.type);
		if (existing != null) {
			existing.number += d.number;
		} else {
			dice.put(d.type, new SimpleDice(d.number, d.type));
		}
		constant += d.constant;
	}

	public int getConstant() {
		return constant;
	}

	public void setConstant(int c) {
		constant = c;
	}

	@Override
	public int roll() {
		int roll = 0;
		for (Dice d : dice.values()) {
			roll += d.roll();
		}
		return roll;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CombinedDice) {
			CombinedDice d = (CombinedDice) o;
			if (d.constant != constant) return false;
			if (d.dice.size() != dice.size()) return false;
			for (int t : dice.keySet()) {
				if (!dice.get(t).equals(d.dice.get(t))) return false;
			}
			return true;
		}
		return false;
	}

	// TODO this should return the dice in a constant order
	@Override
	public String toString() {
		String s = "";
		for (Dice d : dice.values()) {
			if (s.length() > 0) s += " + ";
			s += d.toString();
		}
		if (constant > 0) s += "+"+constant;
		if (constant < 0) s += constant;
		if (s.length() == 0) s = "0";
		return s;
	}

	@Override
	public int getMinimum() {
		int min = 0;
		for (Dice d : dice.values()) {
			min += d.getMinimum();
		}
		return min+constant;
	}

	@Override
	public int getMaximum() {
		int max = 0;
		for (Dice d : dice.values()) {
			max += d.getMaximum();
		}
		return max+constant;
	}

	// permitted format is any number of components separated by '+'. a component can be a dice expression (NdX)
	// or it can be a constant. If the constant is negative then the '+' is optional. Multiple constants will be
	// combined on parsing
	// note: doesn't handle double negatives
	// null, empty, or strings consisting entirely of whitespace will result in an empty DiceCombiner
	public static CombinedDice parse(String s) {
		CombinedDice dice = new CombinedDice();
		if (s == null) return dice;

		// strip out whitespace:
		s = s.replaceAll("\\s", "").toLowerCase();
		if (s.length() == 0) return dice;

		// first we split by '+'
		String[] parts = s.split("[+]");
		for (String part : parts) {
			// now we should have dice expressions (NdX[-C]) or constants [-]C (which may be repeating, e.g. 5-4-7)
			String[] subparts = part.split("-");
			for (int i = 0; i < subparts.length; i++) {
				String subpart = subparts[i];
				// now each subpart will be a dice expression (NdX) or a constant C
				if (subpart.contains("d")) {
					dice.add(SimpleDice.parse(subpart));
				} else if (subpart.length() > 0) {
					int c = Integer.parseInt(subpart);
					if (i > 0 || part.startsWith("-")) c = -c;	// If the first subpart is a constant then it may be positive, all others are always negative
					dice.constant += c;
				}
			}
		}
		return dice;
	}

	public static CombinedDice fromList(List<HDDice> list) {
		CombinedDice dice = new CombinedDice();
		for (HDDice d : list) {
			dice.add(d);
		}
		return dice;
	}

	// Subtracts the dice in c2 that correspond to dice in this. Does not change any type that has more dice in c2 than in this. Returns a new ConstantDice containing any remaining dice from c2.
	// The constant in c2 is always subtracted from this object's constant so the remainder's constant will always be 0.
	CombinedDice subtract(CombinedDice c2) {
		CombinedDice remainder = new CombinedDice();
		for (int t : c2.dice.keySet()) {
			SimpleDice d2 = c2.dice.get(t);
			if (dice.containsKey(t)) {
				SimpleDice d = dice.get(t);
				if (d2.number == d.number) {
					dice.remove(t);
				} else if (d2.number < d.number) {
					d.number -= d2.number;
				} else {
					remainder.add(new SimpleDice(d2.number, t));
				}
			} else {
				remainder.add(new SimpleDice(d2.number, t));
			}
		}
		constant -= c2.constant;
		return remainder;
	}
}

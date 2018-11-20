package gamesystem.dice;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

//FIXME settle on whether operations on List<HDDice> should be static methods here or in DiceList

// HDDice is similar to SimpleDice but designed for monster hit dice. As such it allows 1/2 and 1/4 number of dice and also can include a constant.
public class HDDice extends SimpleDice {
	int constant = 0;

	public HDDice(int type) {
		super(type);
	}

	// num of 0 means 1/2, -1 means 1/4
	public HDDice(int num, int type) {
		super(num, type);
	}

	// num of 0 means 1/2, -1 means 1/4
	public HDDice(int num, int type, int mod) {
		super(num, type);
		this.constant = mod;
	}

	public HDDice(HDDice s) {
		super(s.number, s.type);
		constant = s.constant;
	}

	public int getConstant() {
		return constant;
	}

	// returns a minimum of 1 per dice, even if the constant is negative
	// currently applies the constant to the final total which can result in an incorrect minimum if the constant is negative. e.g. 2d6-4 with a rolls of 1 and 4 should total 3 but currently this returns 2
	// TODO currently the logic isn't quite right. should calculate the per-die constant and apply that per die so that the minimum of 1 is worked out correctly
	@Override
	public int roll() {
		int roll = 0;
		if (number == 0) {
			roll = rand.nextInt(type/2)+1;
		} else if (number == -1) {
			roll = rand.nextInt(type/4)+1;
		} else {
			for (int i = 0; i < number; i++) {
				roll += rand.nextInt(type)+1;
			}
		}
		roll += constant;
		if (roll < number) roll = number;
		return roll;
	}

	// XXX currently returns a minimum of 1 for the overall roll. i think it is more correct to check each die, or at least return a minimum of number of dice
	// returns a random total where each die is re-rolled until its value is at least half of the maximum
	// TODO this should also give max hitpoints for the first HD, as with the character rule
	// XXX this might be incorrect for odd dice types (e.g. d5, d7)
	// TODO the player rule can also be minimum of over half. need to handle that variant somehow
	int rollMinHalf() {
		int roll = 0;
		if (type > 0) {
			if (number == 0) {
				roll += rand.nextInt(type / 4 + 1) + type / 4;
			} else if (number == -1) {
				roll += rand.nextInt(type / 8 + 1) + type / 8;
			} else {
				for (int j = 0; j < number; j++) {
					roll += rand.nextInt(type / 2 + 1) + type / 2;
				}
			}
		}
		roll += constant;
		if (roll < number) roll = number;
		return roll;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HDDice) {
			HDDice d = (HDDice) o;
			return d.constant == constant && d.number == number && d.type == type;
		} else if (o instanceof SimpleDice) {
			SimpleDice d = (SimpleDice) o;
			return 0 == constant && d.number == number && d.type == type;
		}
		return false;
	}

	@Override
	public String toString() {
		String s = "";
		if (number > 0) s+= number;
		else if (number == 0) s += "½ ";
		else if (number == -1) s += "¼ ";
		s += "d"+type;
		if (constant > 0) s += "+"+constant;
		else if (constant < 0) s += constant;
		return s;
	}

// format is [num]d<type>[+/-<mod>]. white space between components is ok. recognises 1/2 and 1/4 in num
	public static HDDice parse(String s) {
		int n = 1;
		int t;
		int m = 0;

		s = s.toLowerCase();
		if (s.indexOf('d') == -1) throw new IllegalArgumentException("Can't parse "+s);

		String numStr = s.substring(0, s.indexOf('d')).trim();
		if (numStr.length() > 0) {
			if (numStr.equals("½") || numStr.equals("1/2")) n = 0;
			else if (numStr.equals("¼") || numStr.equals("1/4")) n = -1;
			else n = Integer.parseInt(numStr);
		}

		s = s.substring(s.indexOf('d')+1);
		if (s.indexOf('+') > 0) {
			t = Integer.parseInt(s.substring(0, s.indexOf('+')).trim());
			m = Integer.parseInt(s.substring(s.indexOf('+')+1).trim());
		} else if (s.indexOf('-') > 0) {
			t = Integer.parseInt(s.substring(0, s.indexOf('-')).trim());
			m = Integer.parseInt(s.substring(s.indexOf('-')).trim());
		} else if (s.indexOf('–') > 0) {
			t = Integer.parseInt(s.substring(0, s.indexOf('–')).trim());
			m = Integer.parseInt("-"+s.substring(s.indexOf('–')+1).trim());
		} else {
			t = Integer.parseInt(s.trim());
		}
		return new HDDice(n, t, m);
	}

// parses a hitdice description. s can have any number of components separated by " plus ". each component has
// format [num]d<type>[+/-<mod>]. white space between components is ok. recognises 1/2 and 1/4 in num
	public static List<HDDice> parseList(String s) {
		String[] rolls = s.split(" plus ");

		List<HDDice> dice = new ArrayList<>();
		for (String roll : rolls) {
			dice.add(parse(roll));
		}
		if (dice.size() == 0) return null;
		return dice;
	}

	@Override
	public int getMinimum() {
		if (number < 1) return 1 + constant;
		return number + constant;
	}

	@Override
	public int getMaximum() {
		int max = 0;
		if (number > 0) {
			max += number * type;
		} else if (number == 0) {
			max += type / 2;
		} else if (number == -1) {
			max += type / 4;
		}
		return max + constant;
	}

	public static int getTotalConstant(List<HDDice> list) {
		Integer total = list.stream().reduce(0, (sum, b) -> sum + b.getConstant(), Integer::sum);
		return total.intValue();
	}

	public static int getTotalNumber(List<HDDice> list) {
		Integer total = list.stream().reduce(0, (sum, b) -> sum + b.getNumber(), Integer::sum);
		return total.intValue();
	}

	public static int getTotalConstant(DiceList<HDDice> list) {
		return getTotalConstant(list.dice);
	}

	public static List<HDDice> difference(List<HDDice> list1, List<HDDice> list2) {
		List<HDDice> l1 = new ArrayList<>(list1);
		List<HDDice> l2 = new ArrayList<>(list2);

		// remove all components that are common to both lists
		for (int i = l1.size() - 1; i >= 0; i--) {
			HDDice d1 = l1.get(i);
			for (int j = l2.size() - 1; j >= 0; j--) {
				HDDice d2 = l2.get(j);
				if (d1.equals(d2)) {
					l1.remove(i);
					l2.remove(j);
				}
			}
		}

		// if there are remaining components of list2 then convert the lists to CombinedDice and calculate the difference between them
		if (list2.size() > 0) {
			CombinedDice c1 = convertToCombined(l1);
			CombinedDice c2 = convertToCombined(l2);
			c1.subtract(c2);
			convertFromCombined(l1, c1);
		}

		return l1;
	}

	private static CombinedDice convertToCombined(List<HDDice> list) {
		CombinedDice c = new CombinedDice();
		for (HDDice d : list) {
			c.add(d);
		}
		return c;
	}

	// note will empty list first
	private static void convertFromCombined(List<HDDice> list, CombinedDice c) {
		list.clear();
		for (int t : c.dice.keySet()) {
			SimpleDice s = c.dice.get(t);
			HDDice d;
			if (list.size() == 0) {
				d = new HDDice(s.number, s.type, c.constant);
			} else {
				d = new HDDice(s.number, s.type);
			}
			list.add(d);
		}
	}

// returns an array containing the number of combinations of possible total of <num>d<type>. the array is indexed
// by total so initial element(s) will be 0.
	BigInteger[] getCombinations() {
		int num = number;
		int type = this.type;
		if (num == 0) {
			num = 1;
			type = type / 2;
		}
		if (num == -1) {
			num = 1;
			type = type / 4;
		}

		// set probabilities for first die
		BigInteger[] totals = new BigInteger[type + 1];
		for (int i = 1; i <= type; i++)
			totals[i] = BigInteger.ONE;

		// add remaining dice
		for (int i = 2; i <= num; i++) {
			BigInteger[] newTotals = new BigInteger[totals.length + type];
			// copy the existing totals to new output array (at one higher index for the '1' on the new die)
			System.arraycopy(totals, 0, newTotals, 1, totals.length);
			// do the remaining faces:
			for (int j = 2; j <= type; j++) {
				for (int k = 0; k < totals.length; k++) {
					if (totals[k] != null) {
						if (newTotals[k + j] == null) newTotals[k + j] = BigInteger.ZERO;
						newTotals[k + j] = newTotals[k + j].add(totals[k]);
					}
				}
			}
			totals = newTotals;
		}

		return totals;
	}

	float getMeanRoll() {
		float mean = 0;
		if (number > 0) {
			mean += number * (type + 1) / 2.0f;
		} else if (number == 0) {
			mean += ((type / 2) + 1) / 2.0f;
		} else if (number == -1) {
			mean += ((type / 4) + 1) / 2.0f;
		}
		return mean + constant;
	}
}

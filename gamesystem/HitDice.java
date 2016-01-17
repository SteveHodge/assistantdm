package gamesystem;

import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// FIXME switch to using regular dice classes

public class HitDice {
	public static java.util.Random rand = new Random();

	// the length of these lists are always identical
	private List<Integer> number = new ArrayList<>();		// num of 0 means 1/2, -1 means 1/4
	private List<Integer> type = new ArrayList<>();
	private List<Integer> modifier = new ArrayList<>();

	private SoftReference<BigInteger[]> probabilities;

	private HitDice() {
	}

	// num of 0 means 1/2, -1 means 1/4
	public HitDice(int num, int type) {
		this(num, type, 0);
	}

	// num of 0 means 1/2, -1 means 1/4
	HitDice(int num, int type, int mod) {
		this.number.add(num);
		this.type.add(type);
		this.modifier.add(mod);
	}

	public void add(int num, int type) {
		add(num, type, 0);
	}

	// TODO should probably support fractional hitdice. currently doesn't (throws an exception)
	public void add(int num, int type, int mod) {
		if (num < 1) throw new IllegalArgumentException("Fractional dice are not supported");
		// try to find that dice type first
		for (int i = 0; i < this.type.size(); i++) {
			if (this.type.get(i) == type) {
				int existing = number.get(i);
				if (existing < 1) throw new IllegalArgumentException("Fractional dice are not supported");
				number.set(i, existing + num);
				modifier.set(i, modifier.get(i) + mod);
				return;
			}
		}

		// didn't return in the loop so we need to add the type as a new component
		number.add(num);
		this.type.add(type);
		modifier.add(mod);
	}

	public void add(HitDice hd) {
		for (int i = 0; i < hd.type.size(); i++) {
			add(hd.number.get(i), hd.type.get(i), hd.modifier.get(i));
		}
	}

	// produces a new HitDice with only one component of each die type (combines any components with the same die type)
	// TODO won't handle fractions correctly
	private HitDice simplify() {
		Map<Integer, Integer> dice = new HashMap<>();
		Map<Integer, Integer> mods = new HashMap<>();

		for (int i = 0; i < type.size(); i++) {
			int t = type.get(i);
			int n = number.get(i);
			int m = modifier.get(i);
			if (dice.containsKey(t)) {
				n += dice.get(t);
				m += mods.get(t);
			}
			dice.put(t, n);
			mods.put(t, m);
		}

		HitDice hd = new HitDice();
		for (int t : dice.keySet()) {
			hd.add(dice.get(t), t, mods.get(t));
		}
		return hd;
	}

	// TODO should probably support fractional hitdice. currently doesn't (throws an exception)
	// Note: returns a valid HitDice in all cases. If there is no difference then the returned value will have no components.
	// If the only difference is in the modifier then will return a value with 1 dice of type 0 plus the modifier.
	// FIXME won't work right if a component of hd is greater than the corresponding component of hitDice
	// TODO reimplement so we start with diff = hitDice.simplify
	public static HitDice difference(HitDice hitDice, HitDice hd) {
		HitDice diff = new HitDice();
		hitDice = hitDice.simplify();
		int extraMod = 0;
		for (int i = 0; i < hitDice.type.size(); i++) {
			int type = hitDice.type.get(i);
			int num = hitDice.number.get(i);
			if (num < 1) throw new IllegalArgumentException("Fractional dice are not supported");
			int mod = hitDice.modifier.get(i);
			for (int j = 0; j < hd.type.size(); j++) {
				if (hd.type.get(j) == type) {
					num -= hd.number.get(j);
					mod -= hd.modifier.get(j);
				}
			}
			if (num >= 1) {
				diff.add(num, type, mod);
			} else if (mod != 0) {
				extraMod += mod;
			}
		}
		if (extraMod != 0) {
			// we've got a modifier than hasn't been included with any dice. add it to the first dice
			if (diff.type.size() >= 1) {
				diff.modifier.set(0, diff.modifier.get(0) + extraMod);
			} else {
				diff.add(1, 0, extraMod);
			}
		}
		return diff;
	}

	@Override
	public boolean equals(Object x) {
		if (x == null) return false;
		if (x instanceof HitDice) {
			HitDice hd = (HitDice)x;
			// first check total modifiers
			if (getModifier() != hd.getModifier()) return false;
			// check number and type. these are not sorted so this is inefficient for HitDice with large numbers of different types
			for (int i = 0; i < type.size(); i++) {
				int t = type.get(i);
				int n = number.get(i);
				boolean found = false;
				for (int j = 0; i < hd.type.size(); i++) {
					if (hd.type.get(j) == t) {
						if (hd.number.get(j) != n) return false;
						found = true;
					}
				}
				if (!found) return false;
			}
			return true;
		}
		return false;
	}

	// returns the number of combinations on the dice in this HitDice that total i (note that any modifiers
	// are not included in the total). E.g. for "2d6+2" this will return 1 for i = 2
	public BigInteger getCombinations(int i) {
		return getCombinations()[i];
	}

	private BigInteger[] getCombinations() {
		if (probabilities == null || probabilities.get() == null) {
//			long time = System.nanoTime();
			BigInteger[] probs = getCombinations(number.get(0), type.get(0));

			for (int i = 1; i < number.size(); i++) {
				BigInteger[] newProbs = getCombinations(number.get(i), type.get(i));
				probs = getCombinations(probs, newProbs);
			}

//			time = System.nanoTime() - time;
//			System.out.println("Calculated probabilities in " + time + "ns");

			probabilities = new SoftReference<>(probs);
		}
		return probabilities.get();
	}

	// returns 1 in the case of no dice
	long getTotalCombinations() {
		long total = 1;
		for (int i = 0; i < number.size(); i++) {
			int t = type.get(i);
			int n = number.get(i);
			if (n == 0) {
				n = 1;
				t = t / 2;
			} else if (n == -1) {
				n = 1;
				t = t / 4;
			}
			total *= Math.pow(t, n);
		}
		return total;
	}

	// returns an array containing the number of combinations of possible total of <num>d<type>. the array is indexed
	// by total so initial element(s) will be 0.
	private static BigInteger[] getCombinations(int num, int type) {
		if (num == 0) return getCombinations(1, type / 2);
		if (num == -1) return getCombinations(1, type / 4);

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

	private static BigInteger[] getCombinations(BigInteger[] d1, BigInteger[] d2) {
		BigInteger[] totals = new BigInteger[d1.length + d2.length - 1];

		for (int i = 0; i < d1.length; i++) {
			for (int j = 0; j < d2.length; j++) {
				if (d1[i] != null && d2[j] != null) {
					if (totals[i + j] == null) totals[i + j] = BigInteger.ZERO;
					totals[i + j] = totals[i + j].add(d1[i].multiply(d2[j]));
				}
			}
		}

		return totals;
	}

	public int getMinimumRoll() {
		return getNumber() + getModifier();
	}

	public int getMaximumRoll() {
		int max = 0;
		for (int i = 0; i < number.size(); i++) {
			int n = number.get(i);
			int t = type.get(i);

			if (n > 0)
				max += n * t;
			else if (n == 0)
				max += t / 2;
			else if (n == -1)
				max += t / 4;
		}
		return max + getModifier();
	}

	public float getMeanRoll() {
		float mean = 0;
		for (int i = 0; i < number.size(); i++) {
			int n = number.get(i);
			int t = type.get(i);

			if (n > 0)
				mean += n * (t + 1) / 2.0f;
			else if (n == 0)
				mean += ((t / 2) + 1) / 2.0f;
			else if (n == -1)
				mean += ((t / 4) + 1) / 2.0f;
		}
		return mean + getModifier();
	}

	public double getStdDeviation() {
		BigInteger[] probs = getCombinations();

		BigInteger count = BigInteger.ZERO;
		double sum = 0;
		double mean = getMeanRoll() - getModifier();

		for (int i = 0; i < probs.length; i++) {
			if (probs[i] == null || probs[i].equals(BigInteger.ZERO)) continue;

			count = count.add(probs[i]);
			sum += probs[i].doubleValue() * (i - mean) * (i - mean);
		}

		return Math.sqrt(sum / count.doubleValue());
	}

// returns the total modifier applied to all dice
	public int getModifier() {
		int mod = 0;
		for (int m : modifier) {
			mod += m;
		}
		return mod;
	}

// returns the total number of dice (any fractional components are treated as 1)
	public int getNumber() {
		int num = 0;
		for (int n : number) {
			if (n <= 0) n = 1;
			num += n;
		}
		return num;
	}

	public int getComponentCount() {
		return type.size();
	}

	int getModifier(int i) {
		return modifier.get(i);
	}

	public void setModifier(int i, int mod) {
		modifier.set(i, mod);
	}

	public int getNumber(int i) {
		return number.get(i);
	}

	public int getType(int i) {
		return type.get(i);
	}

// returns the components of this HitDice separately. Each of the returned HitDice will have exactly one
// dice and modifier. All the returned HitDice are newly created even if this HitDice only has a single
// component
	public Set<HitDice> getComponents() {
		Set<HitDice> components = new HashSet<>();
		for (int i = 0; i < type.size(); i++) {
			HitDice d = new HitDice(number.get(i), type.get(i), modifier.get(i));
			components.add(d);
		}
		return components;
	}

	public int getHitDiceCount() {
		return getNumber();
	}

// XXX currently returns a minimum of 1 for the overall roll. i think it is more correct to check each die, or at least return a minimum of number of dice
	public int roll() {
		int roll = 0;

		for (int i = 0; i < number.size(); i++) {
			roll += modifier.get(i);

			int type = this.type.get(i);
			if (type > 0) {
				int num = number.get(i);
				if (num == 0) {
					roll += rand.nextInt(type/2)+1;
				} else if (num == -1) {
					roll += rand.nextInt(type/4)+1;
				} else {
					for (int j = 0; j < num; j++) {
						roll += rand.nextInt(type)+1;
					}
				}
			}
		}
		return (roll < 1 ? 1 : roll);
	}

// XXX currently returns a minimum of 1 for the overall roll. i think it is more correct to check each die, or at least return a minimum of number of dice
// returns a random total where each die is re-rolled until its value is at least half of the maximum
// TODO this should also give max hitpoints for the first HD, as with the character rule
// XXX this might be incorrect for odd dice types (e.g. d5, d7)
	public int rollMinHalf() {
		int roll = 0;

		for (int i = 0; i < number.size(); i++) {
			roll += modifier.get(i);

			int type = this.type.get(i);
			if (type > 0) {
				int num = number.get(i);
				if (num == 0) {
					roll += rand.nextInt(type / 4 + 1) + type / 4;
				} else if (num == -1) {
					roll += rand.nextInt(type / 8 + 1) + type / 8;
				} else {
					for (int j = 0; j < num; j++) {
						roll += rand.nextInt(type / 2 + 1) + type / 2;
					}
				}
			}
		}
		return (roll < 1 ? 1 : roll);
	}

	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < number.size(); i++) {
			int type = this.type.get(i);
			if (type > 0) {
				if (s.length() > 0) s += " plus ";

				int num = number.get(i);
				if (num > 0) s+= num;
				else if (num == 0) s += "½ ";
				else if (num == -1) s += "¼ ";
				s += "d"+type;
			}
			int mod = modifier.get(i);
			if (mod > 0) s += "+"+mod;
			else if (mod < 0) s += mod;
		}
		return s;
	}

// parses a hitdice description. s can have any number of components separated by " plus ". each component has
// format [num]d<type>[+/-<mod>]. white space between components is ok. recognises 1/2 and 1/4 in num
	public static HitDice parse(String s) {
		String[] rolls = s.split(" plus ");

		HitDice dice = new HitDice();
		for (String roll : rolls) {
			dice.add(roll);
		}
		if (dice.number.size() == 0) return null;
		return dice;
	}

// parses one component of a hitdice description
// format is [num]d<type>[+/-<mod>]. white space between components is ok. recognises 1/2 and 1/4 in num
	private void add(String s) {
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

		this.number.add(n);
		this.type.add(t);
		this.modifier.add(m);
	}
}

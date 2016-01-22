package gamesystem.dice;



import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

//FIXME settle on whether operations on List<HDDice> should be static methods here or in HDDice

public class DiceList<T extends Dice> implements Dice /*, Iterable<T>*/{
	List<T> dice = new ArrayList<>();

	public void add(T d) {
		dice.add(d);
	}

	public DiceList() {
	}

/*
 	// shallow clone constructor
 	private DiceList(DiceList<T> from) {
		dice.addAll(from.dice);
	}
 */

	@Override
	public int roll() {
		int roll = 0;
		for (T d : dice) {
			roll += d.roll();
		}
		return roll;
	}

	@Override
	public String toString() {
		return toString(dice);
	}

	public static <U extends Dice> String toString(List<U> list) {
		StringBuilder s = new StringBuilder();
		for (U d : list) {
			if (s.length() > 0) s.append(" plus ");
			s.append(d.toString());
		}
		return s.toString();
	}

	@Override
	public int getMinimum() {
		int min = 0;
		for (T d : dice) {
			min += d.getMinimum();
		}
		return min;
	}

	@Override
	public int getMaximum() {
		int max = 0;
		for (T d : dice) {
			max += d.getMaximum();
		}
		return max;
	}

	/*
	@Override
	public Iterator<T> iterator() {
		return dice.iterator();
	}

	public int getComponentCount() {
		return dice.size();
	}

	public T getComponent(int i) {
		return dice.get(i);
	}
	 */

	public static <U extends Dice> DiceList<U> fromList(List<U> list) {
		DiceList<U> l = new DiceList<>();
		l.dice = list;
		return l;
	}

	// returns the total number of dice (any fractional components are treated as 1)
	// ignores any components that are not SimpleDice
	// TODO not ideal that this method needs to know the details of HDDice and SimpleDice. Maybe better to move to static method HDDice or make static here
	public int getNumber() {
		int num = 0;
		for (Dice d : dice) {
			if (d instanceof SimpleDice) {
				num += ((SimpleDice) d).number;
			} else if (d instanceof HDDice) {
				int n = ((HDDice) d).number;
				if (n <= 0) n = 1;
				num += n;
			}
		}
		return num;
	}

	public int getModifier() {
		int num = 0;
		for (Dice d : dice) {
			if (d instanceof HDDice) {
				num += ((HDDice) d).constant;
			}
		}
		return num;
	}

	public BigInteger getCombinations(int i) {
		return getCombinations()[i];
	}

	private SoftReference<BigInteger[]> probabilities;	// cache

	private BigInteger[] getCombinations() {
		if (probabilities == null || probabilities.get() == null) {
//			long time = System.nanoTime();

			BigInteger[] probs = null;

			for (Dice d : dice) {
				if (d instanceof HDDice) {
					BigInteger[] newProbs = ((HDDice) d).getCombinations();
					if (probs == null) {
						probs = newProbs;
					} else {
						probs = getCombinations(probs, newProbs);
					}
				}
			}

//			time = System.nanoTime() - time;
//			System.out.println("Calculated probabilities in " + time + "ns");

			probabilities = new SoftReference<>(probs);
		}
		return probabilities.get();
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

	// TODO currently only works on DiceList<HDDice>. Probably should make static
	public float getMeanRoll() {
		int mean = 0;

		for (Dice d : dice) {
			if (d instanceof HDDice) {
				mean += ((HDDice) d).getMeanRoll();
			}
		}
		return mean;
	}

	// TODO currently only works on DiceList<HDDice>. Probably should make static
	public int rollMinHalf() {
		int roll = 0;

		for (Dice d : dice) {
			if (d instanceof HDDice) {
				roll += ((HDDice) d).rollMinHalf();
			}
		}
		return roll;
	}
}

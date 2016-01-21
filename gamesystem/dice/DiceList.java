package gamesystem.dice;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DiceList<T extends Dice> implements Dice, Iterable<T> {
	List<T> dice = new ArrayList<>();

	public void add(T d) {
		dice.add(d);
	}

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
		String s = "";
		for (T d : dice) {
			if (s.length() > 0) s += " plus ";
			s += d.toString();
		}
		return s;
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

	@Override
	public Iterator<T> iterator() {
		return dice.iterator();
	}

}

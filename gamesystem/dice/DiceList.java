package gamesystem.dice;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DiceList implements Dice, Iterable<Dice> {
	List<Dice> dice = new ArrayList<>();

	public void add(Dice d) {
		dice.add(d);
	}

	@Override
	public int roll() {
		int roll = 0;
		for (Dice d : dice) {
			roll += d.roll();
		}
		return roll;
	}

	@Override
	public String toString() {
		String s = "";
		for (Dice d : dice) {
			if (s.length() > 0) s += " plus ";
			s += d.toString();
		}
		return s;
	}

	@Override
	public int getMinimum() {
		int min = 0;
		for (Dice d : dice) {
			min += d.getMinimum();
		}
		return min;
	}

	@Override
	public int getMaximum() {
		int max = 0;
		for (Dice d : dice) {
			max += d.getMaximum();
		}
		return max;
	}

	@Override
	public Iterator<Dice> iterator() {
		return dice.iterator();
	}

}

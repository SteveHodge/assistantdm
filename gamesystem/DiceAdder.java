package gamesystem;


import java.util.ArrayList;
import java.util.List;

public class DiceAdder implements Dice {
	List<Dice> dice = new ArrayList<Dice>();

	public void add(Dice d) {
		dice.add(d);
	}

	public int roll() {
		int roll = 0;
		for (Dice d : dice) {
			roll += d.roll();
		}
		return roll;
	}

	public String toString() {
		String s = "";
		for (Dice d : dice) {
			if (s.length() > 0) s += " plus ";
			s += d.toString();
		}
		return s;
	}

	public int getMinimum() {
		int min = 0;
		for (Dice d : dice) {
			min += d.getMinimum();
		}
		return min;
	}

	public int getMaximum() {
		int max = 0;
		for (Dice d : dice) {
			max += d.getMaximum();
		}
		return max;
	}
}

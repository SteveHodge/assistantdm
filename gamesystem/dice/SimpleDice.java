package gamesystem.dice;

import java.util.Random;

// basic dice: NdX where N and X must be greater than 0
// note this is a mutable type
public class SimpleDice implements Dice {
	int number = 1;
	int type;
	public static java.util.Random rand = new Random();

	public SimpleDice(int type) {
		this(1, type);
	}

	public SimpleDice(int num, int type) {
		if (type < 1 || number < 1) throw new IllegalArgumentException("SimpleDice number and type must be > 1. "+num+"d"+type+" is invalid");
		this.number = num;
		this.type = type;
	}

	public SimpleDice(SimpleDice s) {
		number = s.number;
		type = s.type;
	}

	@Override
	public int roll() {
		int roll = 0;
		for (int i = 0; i < number; i++) {
			roll += rand.nextInt(type)+1;
		}
		return roll;
	}

	@Override
	public String toString() {
		String s = String.valueOf(number);
		s += "d"+type;
		return s;
	}

	// format is [num]d<type>. white space between components is ok
	public static SimpleDice parse(String s) {
		int n = 1;
		int t;

		s = s.toLowerCase();
		if (s.indexOf('d') == -1) throw new IllegalArgumentException("Can't parse "+s);

		String numStr = s.substring(0, s.indexOf('d')).trim();
		if (numStr.length() > 0) n = Integer.parseInt(numStr);

		s = s.substring(s.indexOf('d')+1);
		t = Integer.parseInt(s.trim());
		return new SimpleDice(n, t);
	}

	@Override
	public int getMinimum() {
		return 1;
	}

	@Override
	public int getMaximum() {
		return type;
	}
}

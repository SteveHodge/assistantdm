package gamesystem.dice;


import java.util.Random;

// HDDice is similar to SimpleDice but designed for monster hit dice. As such it allows 1/2 and 1/4 number of dice and also can include a constant.
public class HDDice implements Dice {
	int number = 1;
	int type;
	int constant = 0;
	public static java.util.Random rand = new Random();

	public HDDice(int type) {
		this.type = type;
	}

	// num of 0 means 1/2, -1 means 1/4
	public HDDice(int num, int type) {
		this.number = num;
		this.type = type;
	}

	// num of 0 means 1/2, -1 means 1/4
	public HDDice(int num, int type, int mod) {
		this.number = num;
		this.type = type;
		this.constant = mod;
	}

	public HDDice(HDDice s) {
		number = s.number;
		type = s.type;
		constant = s.constant;
	}

	public int getConstant() {
		return constant;
	}

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
		return roll+constant;
	}

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

	public int getMinimum() {
		return 1;
	}

	public int getMaximum() {
		return type;
	}
}

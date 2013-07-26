package monsters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HitDice {
	public static java.util.Random rand = new Random();

	private List<Integer> number = new ArrayList<Integer>();		// num of 0 means 1/2, -1 means 1/4
	private List<Integer> type = new ArrayList<Integer>();
	private List<Integer> modifier = new ArrayList<Integer>();

	//private List<Boolean> class_levels = new ArrayList<Boolean>();	// later might want to distinguish between HD from base creature and HD from class levels

	private HitDice() {
	}

	// num of 0 means 1/2, -1 means 1/4
	private HitDice(int num, int type) {
		this(num, type, 0);
	}

	// num of 0 means 1/2, -1 means 1/4
	private HitDice(int num, int type, int mod) {
		this.number.add(num);
		this.type.add(type);
		this.modifier.add(mod);
	}

	// returns the total modifier applied to all dice
	int getModifier() {
		int mod = 0;
		for (int m : modifier) mod += m;
				return mod;
	}

	// returns the total number of dice (any fractional components are treated as 1)
	int getNumber() {
		int num = 0;
		for (int n : number) {
			if (n < 0) n = 1;
			num += n;
		}
		return num;
	}

	int roll() {
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
	static HitDice parse(String s) {
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

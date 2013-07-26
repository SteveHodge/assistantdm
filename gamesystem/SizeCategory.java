package gamesystem;

public enum SizeCategory {
	FINE("Fine", 1,0 ,0),
	DIMINUTIVE("Diminutive", 2,0,0),
	TINY("Tiny", 5,0,0),
	SMALL("Small", 10,5,5),
	MEDIUM("Medium", 10,5,5),
	LARGE("Large", 20,5,10),
	HUGE("Huge", 30,10,15),
	GARGANTUAN("Gargantuan", 40,15,20),
	COLOSSAL("Colossal", 60,20,30);

	public int getSpace() {
		return space;
	}

	public double getSpaceFeet() {
		return (double) space / 2;
	}

	public int getReachTall() {
		return tallReach;
	}

	public int getReachLong() {
		return longReach;
	}

	@Override
	public String toString() {
		return description;
	}

	public SizeCategory resize(int delta) {
		int ord = ordinal() + delta;
		System.out.println("Original size = " + this + " ordinal = " + ordinal() + ", delta = " + delta);
		if (ord < 0) return FINE;
		if (ord >= values().length) return COLOSSAL;
		return values()[ord];
	}

	public static SizeCategory getSize(String d) {
		for (SizeCategory s : values()) {
			if (s.toString().equals(d)) return s;
		}
		return null;		// TODO should throw exception
	}

	private SizeCategory(String d, int s, int tallR, int longR) {
		description = d;
		space = s;
		tallReach = tallR;
		longReach = longR;
	}

	private String description;
	private int space;	// in 1/2 foot units
	private int tallReach;	// in feet
	private int longReach;	// in feet
}

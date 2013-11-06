package gamesystem;


public enum SizeCategory {
	FINE("Fine", 1, 0, 0, 8, -16),
	DIMINUTIVE("Diminutive", 2, 0, 0, 4, -12),
	TINY("Tiny", 5, 0, 0, 2, -8),
	SMALL("Small", 10, 5, 5, 1, -4),
	MEDIUM("Medium", 10, 5, 5, 0, 0),
	LARGE("Large", 20, 5, 10, -1, 4),
	HUGE("Huge", 30, 10, 15, -2, 8),
	GARGANTUAN("Gargantuan", 40, 15, 20, -4, 12),
	COLOSSAL("Colossal", 60, 20, 30, -8, 16);

	public int getSizeModifier() {
		return sizeModifier;
	}

	public int getGrappleModifier() {
		return grappleModifier;
	}

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

	private SizeCategory(String d, int s, int tallR, int longR, int sizeMod, int grappleMod) {
		description = d;
		space = s;
		tallReach = tallR;
		longReach = longR;
		sizeModifier = sizeMod;
		grappleModifier = grappleMod;
	}

	private String description;
	private int space;	// in 1/2 foot units
	private int tallReach;	// in feet
	private int longReach;	// in feet
	private int sizeModifier;		// modifier that should be applied to attack and ac
	private int grappleModifier;	// modifier applied to grapple (and hide with inverted sign)
}

package gamesystem;

import java.util.Map;

public class AC extends Statistic {
	// TODO these constants should become unnecessary eventually
	// note that Character.getTouchAC() requires the order of constants here (more specifically it assumes that
	// all components less than AC_DEX are to be excluded from touch ac).
	public static final int AC_ARMOR = 0;
	public static final int AC_SHIELD = 1;
	public static final int AC_NATURAL = 2;
	public static final int AC_DEX = 3;
	public static final int AC_SIZE = 4;
	public static final int AC_DEFLECTION = 5;
	public static final int AC_DODGE = 6;
	public static final int AC_OTHER = 7;
	public static final int AC_MAX_INDEX = 8;
	protected static final String[] ac_names = {"Armor","Shield","Natural Armor","Dexterity","Size","Deflect","Dodge","Misc"};
	// TODO some of the names need changing

	public static String getACComponentName(int type) {
		return ac_names[type];
	}

	public AC(AbilityScore dex) {
		super("AC");
		baseValue = 10;
		addModifier(dex.getModifier());
	}

	public int getTouch() {
		int ac = getValue();

		Map<Modifier,Boolean> map = getModifiers();
		for (Modifier m : map.keySet()) {
			if (map.get(m) && (	m.getType().equals("Armor") || m.getType().equals("Shield") || m.getType().equals("Natural"))) {
				ac -= m.getModifier();
			}
		}
		return ac;
	}

	public int getFlatFooted() {
		int ac = getValue();

		Map<Modifier,Boolean> map = getModifiers();
		for (Modifier m : map.keySet()) {
			if (map.get(m) && (	m.getType().equals("Dexterity") || m.getType().equals("Dodge"))) {
				ac -= m.getModifier();
			}
		}
		return ac;
	}

	// The base AC value of 10 is immutable so setBaseValue is overridden to be a no-op
	public void setBaseValue(int v) {
	}
}

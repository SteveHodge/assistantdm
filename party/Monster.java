package party;

/*
 * Class of common functionality for AdhocMonster and DetailedMonster. Will be removed once AdhocMonster is removed.
 */

public interface Monster {
	public final static String PROPERTY_AC_TOUCH = "AC: Touch";
	public final static String PROPERTY_AC_FLATFOOTED = "AC: Flat Footed";

	public abstract String getStatsBlockHTML();
}

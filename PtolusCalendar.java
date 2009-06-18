/*
 * Class encapsulating the calendar used in Ptolus. It interprets integers representing the number of
 * days since the start of epoch. Times are not currently handled.
 * 
 * This class is not part of the standard Java date handling framework - it might be interesting
 * to implement it in that frame later.
 *
 * The Ptolus years is 364 days long, divided into 12 months of 30 or 31 days. Weeks are 7 days longs.
 * There are exactly 52 weeks in the year which means that each day of the year falls on the same day
 * of the week every year.
 * 
 * Months:
 * Newyear - 31 days
 * Birth - 30 days
 * Wind - 30 days
 * Rain - 31 days
 * Bloom - 30 days
 * Sun - 30 days
 * Growth - 31 days
 * Blessing - 30 days
 * Toil - 30 days
 * Harvest - 31 days
 * Moons - 30 days
 * Yearsend - 30 days
 *
 * Days:
 * Theoday
 * Kingsday
 * Airday
 * Waterday
 * Earthday
 * Fireday
 * Queensday
 * 
 * The Ptolus calendar epoch starts on the 1st of Newyear 1IA (Imperial Age). Years before the start of
 * the epoch are denoted BE (Before Empire). So day 0 = 1 Newyear 1 IA.
 */

public class PtolusCalendar {
	static String[] months = {"Newyear","Birth","Wind","Rain","Bloom","Sun","Growth","Blessing","Toil","Harvest","Moons","Yearsend"};
	static int[] months_days = {31,30,30,31,30,30,31,30,30,31,30,30};
	static int year_days = 364;	// should be calculated from months_days
	static String epochName = "IA";
	static String beforeEpochName = "BE";
	static String[] days = {"Theoday","Kingsday", "Airday", "Waterday", "Earthday", "Fireday", "Queensday"};

	static class Components {
		int year = 0;			// 0 is invalid - -1 = 1 BE, 1 = 1 IA
		int month = 0;			// 0 based
		int day = 0;			// 0 based
		int day_of_week = 0;	// 0 based

		Components(int date) {
			if (date > 0) {
				year = date / year_days + 1;
				int day_of_year = date - (year-1) * year_days;
				int d = day_of_year;
				for (int i=0; i < months_days.length; i++) {
					if (d < months_days[i]) {
						day = d;
						break;
					}
					d -= months_days[i];
					month++;
				}
				day_of_week = day / 7;
			} else {
				// TODO handle BE case
				//year = ((date+1) / year_days)-1;
			}
		}
	}

	// returns date in format "Theoday, 1st of Newyear, 721 IA"
	static public String getLongDescription(int date) {
		Components c = new Components(date);
		String ret = "" + days[c.day_of_week] + ", " + c.day + getOrdinal(c.day) + " of " + months[c.month] + ", " + c.year;
		if (date >= 0) return ret + " IA";
		else return ret + " BE";
	}

	// returns date in format "1 Newyear 721" (BE is appended for years before 1 IA)
	static public String getShortDescription(int date) {
		Components c = new Components(date);
		String ret = "" + c.day + " " + months[c.month] + " " + c.year;
		if (date >= 0) return ret;
		else return ret + " BE";
	}

	static String getOrdinal(int value) {
		int hundredRemainder = value % 100;
		int tenRemainder = value % 10;
		if(hundredRemainder - tenRemainder == 10) {
			return "th";
		}
		switch (tenRemainder) {
			case 1:
				return "st";
			case 2:
				return "nd";
			case 3:
				return "rd";
			default:
				return "th";
		}
	}
}

package gamesystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

public class Calendar {
//	<months>
//	<month name="Newyear" number="1" days="31" season="Winter">
//		<description></description>
//	</month>
//</months>
//
//<week>
//	<weekday name="Theoday" abbreviation="T" number="1"/>
//</week>
//
//<events>
//	<event month="Newyear" day="1" name="Newyear's Day">
//		<description></decription>
//	</event>

	public static class Month {
		public String name;
		public int number;
		public int days;
		public String season;
		public Element description;

		@Override
		public String toString() {
//			return "" + number + " " + name + " (" + days + " days, " + season + ")";
			return "" + number + " " + name;
		}
	}

	public static class Weekday {
		public String name;
		public String abbreviation;
		public int number;

		@Override
		public String toString() {
//			return "" + number + " " + name + " ('" + abbreviation + "')";
			return "" + number + " " + name;
		}
	}

	public static class Event {
		public Month month;
		public int day;
		public String name;
		public Element description;

		@Override
		public String toString() {
			return "" + day + " " + month.name + ": " + name;
		}
	}

	public static class Comment {
		public int year;
		public Month month;
		public int day;
		public String comment;

		@Override
		public String toString() {
			return "" + day + " " + month.name + " " + year + ": " + comment;
		}
	}

	public static Month[] months;
	public static Map<String, Month> monthMap = new HashMap<>();
	public static Weekday[] weekdays;
	public static Event[] events;

	public static int currentYear = 721;
	public static int currentMonth = 1;
	public static int currentDay = 1;

	public static List<Event> customEvents = new ArrayList<>();
	public static List<Comment> comments = new ArrayList<>();

	public static int defaultYear = 1;
	public static Weekday firstWeekday;

//	final protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
//
//	@Override
//	public void addPropertyChangeListener(PropertyChangeListener listener) {
//		pcs.addPropertyChangeListener(listener);
//	}
//
//	@Override
//	public void removePropertyChangeListener(PropertyChangeListener listener) {
//		pcs.removePropertyChangeListener(listener);
//	}

	public static boolean hasEvent(Month month, int day) {
		for (int i = 0; i < events.length; i++)
			if (events[i].month == month && events[i].day == day) return true;
		for (Event e : customEvents)
			if (e.month == month && e.day == day) return true;
		return false;
	}

	public static List<Event> getEvents(Month month, int day) {
		List<Event> l = new ArrayList<>();
		for (int i = 0; i < events.length; i++)
			if (events[i].month == month && events[i].day == day) l.add(events[i]);
		return l;
	}

	public static Event getCustomEvent(Month month, int day) {
		for (Event e : customEvents)
			if (e.month == month && e.day == day) return e;
		return null;
	}

	public static void setCustomEvent(Month month, int day, String text) {
		Event e = getCustomEvent(month, day);
		if (e == null && text != null && text.length() > 0) {
			e = new Event();
			e.month = month;
			e.day = day;
			e.name = text;
			customEvents.add(e);
		} else if (e != null && text != null && text.length() > 0) {
			e.name = text;
		} else if (e != null && (text == null || text.length() == 0)) {
			customEvents.remove(e);
		}
	}

	public static Comment getComment(int year, Month month, int day) {
		for (Comment c : comments)
			if (c.year == year && c.month == month && c.day == day) return c;
		return null;
	}

	public static void setComment(int year, Month month, int day, String text) {
		Comment c = getComment(year, month, day);
		if (c == null && text != null && text.length() > 0) {
			c = new Comment();
			c.year = year;
			c.month = month;
			c.day = day;
			c.comment = text;
			comments.add(c);
		} else if (c != null && text != null && text.length() > 0) {
			c.comment = text;
		} else if (c != null && (text == null || text.length() == 0)) {
			comments.remove(c);
		}
	}

	public static void dumpCalendar() {
		System.out.println("Calendar:");
		if (months != null) {
			System.out.println("  Months:");
			for (int i = 0; i < months.length; i++) {
				System.out.println(months[i]);
			}
		}

		if (weekdays != null) {
			System.out.println("  Weekdays:");
			for (int i = 0; i < weekdays.length; i++) {
				System.out.println(weekdays[i]);
			}
		}

		if (events != null) {
			System.out.println("  Events:");
			for (int i = 0; i < events.length; i++) {
				System.out.println(events[i]);
			}
		}
	}

	public static void outputCalendar(int year, int month) {
		int currentDay = firstWeekday.number - 1;
		// add any days for the months before firstMonth
		for (int i = 0; i < month - 1; i++) {
			currentDay = (currentDay + months[i].days) % 7;
		}

		for (int i = 0; i < 3; i++) {
			System.out.println(months[month - 1].name + " " + year);
			// print out weekday header
			for (Weekday w : weekdays) {
				System.out.print(" " + w.abbreviation + " ");
			}
			System.out.println();
			// first week, skip to the first day
			for (int j = 0; j < currentDay; j++)
				System.out.print("   ");
			// output the month
			for (int j = 1; j <= months[month - 1].days; j++) {
				if (j < 10)
					System.out.print(" ");
				System.out.print("" + j + " ");
				if (++currentDay == 7) {
					System.out.println();
					currentDay = 0;
				}
			}
			if (currentDay != 0)
				System.out.println();
			System.out.println();

			month++;
			if (month > months.length) {
				year++;
				month = 1;
			}
		}
	}
}

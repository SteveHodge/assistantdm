package gamesystem;

public class Size {
	public enum Category {
		FINE ("Fine"),
		DIMINUTIVE ("Diminutive"),
		TINY ("Tiny"),
		SMALL ("Small"),
		MEDIUM ("Medium"),
		LARGE ("Large"),
		HUGE ("Huge"),
		GARGANTUAN ("Gargantuan"),
		COLOSSAL ("Colossal");

		public String toString() {return description;}
		
		public static Category getCategory(String d) {
			for (Category s : values()) {
				if (s.toString().equals(d)) return s;
			}
			return null;		// TODO should throw exception
		}

		private Category(String d) {description = d;}

		private String description;		
	}
}

package gamesystem;

// XXX this seems to overlap a lot with the Property heirarchy. it's currently only used for custom buffs. it probably needs refactoring
public interface StatisticsCollection {
	// returns an array of any subtargets that this object handles. each element of the array is a two element array containing name and target designation
	// note: does not include this statistic in the array; it's assumed that the parent already nows about this statistic
	public StatisticDescription[] getStatistics();

	public class StatisticDescription {
		public String target;
		public String name;

		public StatisticDescription(String n, String t) {
			target = t;
			name = n;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}

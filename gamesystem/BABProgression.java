package gamesystem;

public enum BABProgression {
	FAST {
		@Override
		public int getBAB(int level) {
			return level;
		}
	},

	AVERAGE {
		@Override
		public int getBAB(int level) {
			return 3 * level / 4;
		}
	},

	SLOW {
		@Override
		public int getBAB(int level) {
			return level / 2;
		}
	};

	abstract public int getBAB(int level);
}

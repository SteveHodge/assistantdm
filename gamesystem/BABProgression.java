package gamesystem;

public enum BABProgression {
	FAST {
		@Override
		public int getBAB(int level) {
			if (level < 1) level = 1;	// level = 0 -> 1/2 hitdice, level = -1 -> 1/4 hitdice, as with HDDice
			return level;
		}
	},

	AVERAGE {
		@Override
		public int getBAB(int level) {
			if (level < 1) level = 1;	// level = 0 -> 1/2 hitdice, level = -1 -> 1/4 hitdice, as with HDDice
			return 3 * level / 4;
		}
	},

	SLOW {
		@Override
		public int getBAB(int level) {
			if (level < 1) level = 1;	// level = 0 -> 1/2 hitdice, level = -1 -> 1/4 hitdice, as with HDDice
			return level / 2;
		}
	};

	abstract public int getBAB(int level);
}

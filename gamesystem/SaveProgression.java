package gamesystem;

public enum SaveProgression {
	FAST {
		@Override
		public int getBaseSave(int level) {
			return 2 + level / 2;
		}
	},

	SLOW {
		@Override
		public int getBaseSave(int level) {
			return level / 3;
		}
	};

	abstract public int getBaseSave(int level);
}

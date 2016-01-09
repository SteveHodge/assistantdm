package gamesystem;


// TODO this should be folded into Levels

public abstract class HitDice extends Statistic {
	public HitDice(String name) {
		super(name);
	}

	public abstract int getHitDiceCount();

	public abstract int getBaseSave(SavingThrow.Type type);

	public abstract int getBAB();
}

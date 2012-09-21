package gamesystem;


public class InitiativeModifier extends Statistic {
	public InitiativeModifier(AbilityScore dex) {
		super("Initiative");
		addModifier(dex.getModifier());
	}
}

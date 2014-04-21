package combat;

import gamesystem.Creature;

import java.util.Map;

import util.Module;

public interface EncounterModule extends Module {

	Map<Integer, Creature> getCharacterIDMap();

	Map<Integer, Creature> getIDMap();

	InitiativeListModel getInitiativeListModel();

	void addInitiativeListener(InitiativeListener l);

}

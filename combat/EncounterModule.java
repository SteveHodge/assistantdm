package combat;

import java.util.Map;

import gamesystem.Creature;
import util.Module;

public interface EncounterModule extends Module {

	Map<Integer, Creature> getCharacterIDMap();

	Map<Integer, Creature> getIDMap();

	InitiativeListModel getInitiativeListModel();

	void addInitiativeListener(InitiativeListener l);

	void setRoll(Creature c, int roll);
}

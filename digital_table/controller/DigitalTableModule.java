package digital_table.controller;

import party.Character;
import util.Module;

public interface DigitalTableModule extends Module {
	void setCalibrateDisplay(boolean on);

	void moveToken(Character character, String newLoc);
}

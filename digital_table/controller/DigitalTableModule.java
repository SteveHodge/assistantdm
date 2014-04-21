package digital_table.controller;

import util.Module;

public interface DigitalTableModule extends Module {

	void setCalibrateDisplay(boolean on);

	void updateOverlay(int width, int height);
}

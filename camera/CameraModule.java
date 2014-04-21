package camera;

import util.Module;

public interface CameraModule extends Module {
	byte[] getLatestCorrectedImage();
}

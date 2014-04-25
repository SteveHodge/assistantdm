package camera;

import util.Module;

public interface CameraModule extends Module {
	byte[] getLatestCorrectedJPEG();

	void addCameraModuleListener(CameraModuleListener l);

	void removeCameraModuleListener(CameraModuleListener l);
}

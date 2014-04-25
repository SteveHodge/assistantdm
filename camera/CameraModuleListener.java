package camera;

import java.awt.image.BufferedImage;
import java.util.EventListener;

public interface CameraModuleListener extends EventListener {
	void imageCaptured(BufferedImage image, long size, Exception ex, String updateMsg);

	void cameraConnected(String name);

	void cameraDisconnected();	// TODO might need a disconnecting event that fires first

	void cameraError(int error);

	void homographyChanged(Exception ex);
}

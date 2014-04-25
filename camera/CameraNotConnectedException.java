package camera;

public class CameraNotConnectedException extends Exception {
	private static final long serialVersionUID = 1L;

	public CameraNotConnectedException() {
		super("Camera not connected");
	}
}

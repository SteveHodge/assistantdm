package camera;

public class CameraNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public CameraNotFoundException() {
		super("No cameras found");
	}
}

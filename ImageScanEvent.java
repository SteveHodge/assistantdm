import java.io.File;
import java.util.EventObject;


@SuppressWarnings("serial")
public class ImageScanEvent extends EventObject {
	protected File imageFile;

	public ImageScanEvent(Object source, File file) {
		super(source);
		imageFile = file;
	}

	public File getImageFile() {
		return imageFile;
	}
}

package camera;
import java.util.EventListener;


// Note that CameraMontior runs in it's own thread so imageFound will be called from that thread.
public interface ImageScanListener extends EventListener {
	void imageFound(ImageScanEvent e);
}

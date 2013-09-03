package digital_table.server;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;


// TODO implemented as singleton, but really just needs to be locateable

// basic process:
// optionspanel control will let the user pick a file which will return a URL? ImageManager (would need to record the source URL) ?
// displaymanager will set the local property
// then query the remote system to see if it has the media file
// if it does not then it should send the media file
// then it sets the remote system

// might want separate local and remote versions of this

public enum MediaManager {
	INSTANCE;

	private File mediaPath = new File("media/");
	private URI mediaURI = mediaPath.getAbsoluteFile().toURI();
	private File cachePath = new File("media/cache/");

	private Map<URI, File> cached = new HashMap<URI, File>();

	private JFileChooser chooser = new JFileChooser();
	private File lastDir = mediaPath;

	public ImageMedia getImageMedia(MapCanvas canvas, URI uri) {
		System.out.println("getImageMedia(" + uri + ")");
		File f = cached.get(uri);
		byte[] bytes = null;
		if (f != null) {
			try {
				FileInputStream in = new FileInputStream(f);
				byte[] b = new byte[(int) f.length()];
				in.read(b);
				bytes = b;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			bytes = getFile(uri);
		}

		if (bytes == null) return null;
		return ImageMedia.createImageManager(canvas, bytes);
	}

	// presents a file chooser dialog using the supplied component as the parent component for the dialog.
	// if a file is chosen then it is returned as a uri relative to mediaPath if the selected file is a descendant
	// of mediaPath or a absolute uri otherwise.
	public URI showFileChooser(Component parent) {
		if (lastDir != null) chooser.setCurrentDirectory(lastDir);
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (f != null) {
				URI uri = f.toURI();
				uri = mediaURI.relativize(uri);
				return uri;
			}
		} else {
			System.out.println("Cancelled");
		}
		return null;
	}

	public boolean hasMedia(URI uri) {
		boolean ret = hasMediaImpl(uri);
		System.out.println("hasMedia(" + uri + ") = " + ret);
		return ret;
	}

	private boolean hasMediaImpl(URI uri) {
		if (uri.isAbsolute() && !uri.getScheme().equals("file")) return true;	// assume non-file URIs can be fetched

		uri = uri.normalize();	// normalize the uri
		if (cached.containsKey(uri)) return true;

		if (!uri.isAbsolute()) {
			// relative uri, see if the corresponding file under mediaPath exists
			uri = mediaURI.resolve(uri);
			return new File(uri).exists();
		}

		return false;
	}

	// TODO cache the contents of the file
	public byte[] getFile(URI uri) {
		System.out.println("getFile(" + uri + ")");
		uri = mediaURI.resolve(uri.normalize());
		File f = new File(uri);
		FileInputStream in;
		try {
			in = new FileInputStream(f);
			byte[] bytes = new byte[(int) f.length()];
			in.read(bytes);
			return bytes;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getName(URI uri) {
		uri = mediaURI.resolve(uri.normalize());
		File f = new File(uri);
		return f.getName();
	}

	// if the URI is relative then it's assumed to be relative to mediaPath and we'll create a persistent copy of the media
	// if the URI is absolute then we create a temporary local copy under cachePath
	public void addMedia(URI uri, byte[] bytes) {
		System.out.print("addMedia(" + uri + ")");
		uri = uri.normalize();

		File mediaFile = null;

		if (uri.isAbsolute()) {
			String filename = String.format("%08x", uri.hashCode());
			String realname = new File(uri).getName();
			String ext = "";
			if (realname.lastIndexOf('.') > -1) {
				ext = realname.substring(realname.lastIndexOf('.'));
				realname = realname.substring(0, realname.lastIndexOf('.'));
				filename = realname + "_" + filename + ext;
			}
			mediaFile = new File(cachePath, filename);
			cached.put(uri, mediaFile);

		} else {
			uri = mediaPath.toURI().resolve(uri);
			mediaFile = new File(uri);
			mediaFile.getParentFile().mkdirs();
		}

		System.out.println(" - stored as " + mediaFile);
		FileOutputStream outStream;
		try {
			outStream = new FileOutputStream(mediaFile);
			outStream.write(bytes);
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private MediaManager() {
		if (!mediaPath.exists()) mediaPath.mkdirs();
		if (!cachePath.exists()) cachePath.mkdirs();
	}
}

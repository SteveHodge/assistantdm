package camera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.swing.event.EventListenerList;

// TODO obsolete and unused
/**
 * Periodically monitors a directory for files of the form prefixX.jpg where X is a number.
 * When a file of the specified type appears it is copied to the specified
 * destination and any registered action listeners are notified. Only the file with the
 * latest creation date is copied. Whether a file is copied or not the monitor sleeps for the
 * specified period before scanning the directory again.
 * 
 * Files that match the specified pattern are renamed (prefixing 'done') in order to prevent
 * them being scanned again.
 * 
 * @author Steve
 *
 */
// TODO could implement the image monitoring as an ImageProducer. we'd still need the event stuff to handle logging
public class CameraMonitor implements Runnable {
	File source;
	File destination;
	String prefix;
	int period;
	boolean renameFiles = false;		// deletes the files if this is false

	EventListenerList listenerList = new EventListenerList();

	/**
	 * 
	 * @param source Path to directory to monitor
	 * @param prefix Static part of filename (case insensitive)
	 * @param destination Filename of destination copy, including path
	 * @param period Sleep duration between checks in microseconds
	 */
	public CameraMonitor(String source, String prefix, String destination, int period) {
		this.source = new File(source);
		if (!this.source.exists()) {
			System.err.println("Can't file source directory: "+source);
		}
		this.destination = new File(destination);
		this.prefix = prefix.toLowerCase();
		this.period = period;
	}

	public void addImageScanListener(ImageScanListener l) {
		listenerList.add(ImageScanListener.class, l);
	}

	public void removeImageScanListener(ImageScanListener l) {
		listenerList.remove(ImageScanListener.class, l);
	}

	protected void fireImageFound(File file) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		ImageScanEvent event = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ImageScanListener.class) {
				if (event == null)
					event = new ImageScanEvent(this,file);
				((ImageScanListener)listeners[i+1]).imageFound(event);
			}
		}
	}

	protected void cleanupFile(File file) {
		if (renameFiles) {
			// rename source
			File dest = new File(source, "done"+file.getName());
			//System.out.println("Renaming to "+dest);
			file.renameTo(dest);
		} else {
			if (!file.delete()) {
				System.err.println("Failed to delete "+file.getAbsolutePath());
			}
		}
	}

	@Override
	public void run() {
		while(true) {
			//System.out.println("Camera Monitor scanning");

			File file = scanDirectory();
			if (file != null) {
				//System.out.println("Copying most recent '"+file+"' to '"+destination+"'");
				try {
					fileCopy(file,destination);
					fireImageFound(file);
					cleanupFile(file);
				} catch (IOException e) {
					System.out.println("Failed to copy '"+file+"' to '"+destination+"': "+e.getMessage());
				}
			}

			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
				System.out.println("Camera Monitor sleep interrupted");
			}
		}
	}

	/*
	 * Scans the directory for files matching the specified name
	 * (currently just tests that the prefix matches and the suffix is '.jpg').
	 * Returns the matching file with the most recent last modification time.
	 * Renames or deletes all other matching files so they are prefixed with 'done'.
	 */
	private File scanDirectory() {
		File[] files = source.listFiles((dir, name) -> {
			String lowerName = name.toLowerCase();
			if (!lowerName.startsWith(prefix)) return false;
			if (!lowerName.endsWith(".jpg")) return false;
			// TODO check for number between prefix and suffix
			return true;
		});

		if (files == null || files.length == 0) return null;

		// find the most recent matching file
		File mostRecent = null;
		long mostRecentTime = -1;
		for (File f : files) {
			//System.out.println("Found "+f.getName());
			if (f.lastModified() >= mostRecentTime) {
				mostRecent = f;
				mostRecentTime = f.lastModified();
			}
		}

		// rename or delete all files but the most recent
		for (File f : files) {
			if (f != mostRecent) {
				cleanupFile(f);
			}
		}

		return mostRecent;
	}

	public static void fileCopy( File in, File out ) throws IOException {
		try (
				FileInputStream fis = new FileInputStream(in);
				FileOutputStream fos = new FileOutputStream(out);) {
			try (
					FileChannel inChannel = fis.getChannel();
					FileChannel outChannel = fos.getChannel();) {
				//  inChannel.transferTo(0, inChannel.size(), outChannel);      // original -- apparently has trouble copying large files on Windows

				// magic number for Windows, 64Mb - 32Kb)
				int maxCount = (64 * 1024 * 1024) - (32 * 1024);
				long size = inChannel.size();
				long position = 0;
				while ( position < size ) {
					position += inChannel.transferTo( position, maxCount, outChannel );
				}
			}
		}
	}
}

package digital_table.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import digital_table.elements.GridCoordinates;
import digital_table.elements.Initiative;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Layer;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.WebMessage;
import util.Updater;

/**
 * Generates a image of the display intended to replace the the camera image.
 *
 * @author Steve
 *
 */

// Note repaints also repaint the supplied TokenOverlay. Therefore changes should be applied to the TokenOverlay first
// TODO probably better to create our own token overlay and forward changes to it.

class RemoteImageDisplay extends TokenOverlay {
	final static int PIXELS_PER_CELL = 20;

	final int rows = 39;
	final int columns = 32;
	public RepaintThread repaintThread = new RepaintThread();
	private static Object monitor = new Object();
	final TokenOverlay tokens;
	boolean outputEnabled = false;

	public class RepaintThread extends Thread {
		protected volatile boolean quit = false;
		protected volatile boolean repaint = false;
		protected long lastUpdate = 0;

		@Override
		public void run() {
			while (!quit) {
				// wait until repaint is required (or we need to quit)
				//System.out.println("Repaint thread: waiting for repaint");
				while (!repaint && !quit) {
					// wait until repaint is required
					synchronized (monitor) {
						try {
							monitor.wait();
						} catch (InterruptedException e) {
						}
					}
				}
				if (quit) return;

				// if it's been < 5 seconds since last file update then wait until 5s is up
				long toWait = lastUpdate + 5000 - System.currentTimeMillis();
				if (toWait > 0) {
					//System.out.println("Repaint thread: waiting for " + toWait + "ms");
					try {
						Thread.sleep(toWait);
					} catch (InterruptedException e) {
					}
				}
				if (quit) return;

				// set repaint to false and start painting
				BufferedImage image;
				BufferedImage tokenImg;
				SortedMap<String, MapElement> descriptions = new TreeMap<>();
				do {
					repaint = false;
					image = getImage(PIXELS_PER_CELL * rows, PIXELS_PER_CELL * columns, BufferedImage.TYPE_INT_RGB);
					tokenImg = tokens.getImage(PIXELS_PER_CELL * rows, PIXELS_PER_CELL * columns, descriptions);
				} while (repaint && !quit);			// if repaint is required then repeat
				if (quit) return;

				// update the files
				try {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					ImageIO.write(image, "png", stream);
					//System.out.println("Repaint thread: sending image");
					Updater.updateURL(Updater.MAP_IMAGE, stream.toByteArray());

					// top legend
					BufferedImage img = image.getSubimage(0, 0, PIXELS_PER_CELL, image.getHeight());
					stream = new ByteArrayOutputStream();
					ImageIO.write(img, "png", stream);
					Updater.updateURL(Updater.TOP_LEGEND_IMAGE, stream.toByteArray());

					// left legend
					img = image.getSubimage(0, 0, image.getWidth(), PIXELS_PER_CELL);
					stream = new ByteArrayOutputStream();
					ImageIO.write(img, "png", stream);
					Updater.updateURL(Updater.LEFT_LEGEND_IMAGE, stream.toByteArray());

					lastUpdate = System.currentTimeMillis();
					tokens.updateOverlay(tokenImg, descriptions);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} // 6. go to 1
		}

		public void quit() {
			quit = true;
			interrupt();
		}

		public void repaint() {
			if (!outputEnabled) return;
			repaint = true;
			synchronized (monitor) {
				monitor.notifyAll();
			}
		}
	}

	RemoteImageDisplay(TokenOverlay t) {
		super();
		tokens = t;

		repaintThread.start();

		// if the token overlay gets updated (e.g. for a label change) then we need to update too
		tokens.canvas.addRepaintListener(() -> {
			if (outputEnabled) repaintThread.repaint();
		});

		canvas.addRepaintListener(() -> {
			if (outputEnabled) repaintThread.repaint();
		});
	}

	public void setOutputEnabled(boolean out) {
		outputEnabled = out;
		if (outputEnabled) repaintThread.repaint();
	}

	public boolean isOutputEnabled() {
		return outputEnabled;
	}

	@Override
	void updateOverlay(int width, int height) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImageIO.write(getImage(width, height), "png", stream);
			Updater.update(Updater.MAP_IMAGE, stream.toByteArray());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	BufferedImage getImage(int width, int height) {
		long time = System.currentTimeMillis();
		BufferedImage img = getImage(width, height, BufferedImage.TYPE_INT_RGB);
		time = System.currentTimeMillis() - time;
		//System.out.printf("getImage took %d ms\n", time);
		return img;
	}

	@Override
	void addElement(MapElement e, MapElement parent) {
		if (parent != null) parent = elements.get(parent.getID());

		// XXX would clone() work?
		// we serialize the element to a byte array and then deserialize it to a new MapElement
		// this produces a private copy of the element in it's current state
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(e);
			os.close();
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ObjectInputStream is = new ObjectInputStream(bis);
			MapElement copy = (MapElement) is.readObject();
			is.close();

			if ((copy instanceof Initiative) || (copy instanceof WebMessage)) {
				copy.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.HIDDEN);
			}

			if (e instanceof GridCoordinates) {
				copy.setProperty(GridCoordinates.PROPERTY_RULER_ROW, 37);
				copy.setProperty(GridCoordinates.PROPERTY_INVERT_ROW, true);
				copy.setProperty(GridCoordinates.PROPERTY_RULER_COLUMN, 0);
				copy.setProperty(GridCoordinates.PROPERTY_INVERT_COLUMN, true);
			}

			canvas.addElement(copy, parent);
			elements.put(copy.getID(), copy);

		} catch (IOException | ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	void setProperty(MapElement element, String property, Object value) {
		MapElement e = elements.get(element.getID());
		if (e == null) return;
		if ((e instanceof Initiative) || (e instanceof WebMessage)) return;
		if (e instanceof GridCoordinates && (property == GridCoordinates.PROPERTY_RULER_ROW || property == GridCoordinates.PROPERTY_RULER_COLUMN)) return;
		if (property.equals(MapElement.PROPERTY_LAYER)) {
			canvas.changeLayer(e, (Layer) value);
		} else {
			e.setProperty(property, value);
		}
	}
}

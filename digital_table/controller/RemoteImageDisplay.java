package digital_table.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

import util.Updater;
import digital_table.elements.GridCoordinates;
import digital_table.elements.Initiative;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;

/**
 * Generates a image of the display intended to replace the the camera image.
 *
 * @author Steve
 *
 */

class RemoteImageDisplay extends TokenOverlay {
	final int rows = 39;
	final int columns = 32;

	RemoteImageDisplay() {
		super();
		canvas.addRepaintListener(() -> {
			updateOverlay(20 * rows, 20 * columns);
		});
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

	// TODO the same as the superclass except for the BufferedImage type - merge
	BufferedImage getImage(int width, int height) {
		return getImage(width, height, BufferedImage.TYPE_INT_RGB);
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

			if ((copy instanceof Initiative)) {
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
		if (e instanceof Initiative) return;
		if (e instanceof GridCoordinates && (property == GridCoordinates.PROPERTY_RULER_ROW || property == GridCoordinates.PROPERTY_RULER_COLUMN)) return;
		e.setProperty(property, value);
	}
}

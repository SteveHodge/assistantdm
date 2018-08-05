package tilemapper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;


@SuppressWarnings("serial")
public class DraggableTile extends JComponent implements ChangeListener, DragSource {
	protected Dimension size;

	protected Image image;
	public Tile tile;
	public int orientation = 0;
	public int gridSize = 0;	// in pixels

	EventListenerList listenerList = new EventListenerList();
	ChangeEvent changeEvent = null;

	public DraggableTile(Tile t, int gridSize) {
		tile = t;
		setGridSize(gridSize);
		addChangeListener(this);
		updateSize();
	}

	DraggableTile drag = null;

	@Override
	public Component getDragComponent() {
		drag = new DraggableTile(tile, gridSize);
		drag.setOrientation(orientation);
		return drag;
	}

	@Override
	public void dragFinished(boolean cancelled) {
		drag = null;
	}

	protected void updateSize() {
		Image image = getImage();
		if (image != null) {
			size = new Dimension(image.getWidth(this)+2,image.getHeight(this)+2);
			setSize(size);
			setMinimumSize(size);
			setPreferredSize(size);
		} else {
			size = new Dimension(0,0);
		}
	}

	public void setGridSize(int s) {
		if (s != gridSize) {
			gridSize = s;
			if (tile != null) {
				image = tile.getTileImage(gridSize, orientation);
			}
			fireChangeEvent();
			updateSize();
			repaint();
		}
	}

	public void rotateTile() {
		setOrientation(orientation+1);
		updateSize();
		repaint();
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		Graphics g = graphics.create();

		//Draw in our entire space, even if isOpaque is false.
		Image image = getImage();	// TODO should check size is unchanged
		if (image != null) {
			int w = image.getWidth(this)+1;
			int h = image.getHeight(this)+1;
			if (size.width != w || size.height != h) {
				size.width = w;
				size.height = h;
				setMinimumSize(size);
				setPreferredSize(size);
			}
		}

		// don't paint background, honour tiles transparency...
//		g.setColor(Color.WHITE);
//		g.fillRect(0, 0, image == null ? getPreferredSize().width : size.width,
//				image == null ? getPreferredSize().height : size.height);

		if (image != null) {
			g.drawImage(image, 1, 1, this);
		}

		//Add a border, red if picture currently has focus
		if (isFocusOwner()) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLACK);
		}
		g.drawRect(0, 0, image == null ? getPreferredSize().width : size.width,
				image == null ? getPreferredSize().height : size.height);
		g.dispose();
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		repaint();
	}

	public void setOrientation(int o) {
		orientation = o % 4;
		if (tile != null) {
			image = tile.getTileImage(gridSize, orientation);
			updateSize();
		}
		fireChangeEvent();
	}

	public void mirrorTile() {
		if (tile != null) {
			tile = tile.getMirror();
			setOrientation(4-orientation);
		}
	}

	public Image getImage() {
		return image;
	}

/*	public Object getValue() {
		return this;
	}

	public void setValue(Object object) {
		if (object instanceof DraggableTileModel) {
			DraggableTileModel source = (DraggableTileModel)object;
			tile = source.tile;
			orientation = source.orientation;
			image = tile.getScaledImage(tile.width*gridSize,orientation);
		}
	}*/

	public boolean isDraggable() {
		return true;
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	protected void fireChangeEvent() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ChangeListener.class) {
				if (changeEvent == null) changeEvent = new ChangeEvent(this);
				((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
			}
		}
	}
}

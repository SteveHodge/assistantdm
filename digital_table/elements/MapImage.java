package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.ImageMedia;
import digital_table.server.MeasurementLog;
import digital_table.server.MediaManager;

// TODO could have border color property
// TODO dragging the image after clearing cells does not move the cleared cells which is weird

public class MapImage extends Group {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ALPHA = "alpha";	// float
	//	public final static String PROPERTY_FILENAME = "filename";	// String - read only
	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_MIRRORED = "mirrored";	// boolean - image is flipped horizontally
	public final static String PROPERTY_WIDTH = "width";	// double
	public final static String PROPERTY_HEIGHT = "height";	// double
	public final static String PROPERTY_CLEARCELL = "clear";	// Point - when this property is set the specified cell will be cleared
	public final static String PROPERTY_UNCLEARCELL = "unclear";	// Point - when this property is set the specified cell will be shown again
	public final static String PROPERTY_IMAGE = "image";	// URI currently write-only (but change to read-write)
	public final static String PROPERTY_IMAGE_PLAY = "play";	// write only no value
	public final static String PROPERTY_IMAGE_STOP = "stop";	// write only no value
	public final static String PROPERTY_SHOW_BORDER = "show_border";	// boolean
	public final static String PROPERTY_SHOW_BACKGROUND = "show_background";	// boolean
	public final static String PROPERTY_BACKGROUND_COLOR = "background";	// Color
	public final static String PROPERTY_ASPECT_LOCKED = "aspect_locked";	// boolean	// TODO shouldn't this be handled in ui instead of here?

	transient ImageMedia image = null;
	transient Mask mask = null;

	// scaled dimensions in grid coordinate-space:
	Property<Double> width = new Property<Double>(PROPERTY_WIDTH, 0d, Double.class);
	Property<Double> height = new Property<Double>(PROPERTY_HEIGHT, 0d, Double.class);

	Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, 0, Integer.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Integer r) {
			r = r % 4;
			int oldR = rotations.getValue();
			if (oldR == r) return;
			super.setValue(r);

			if ((r + oldR) % 2 == 1) {
				// change is an odd number of quadrants so we need to swap width and height
				double w = width.getValue();
				width.setValue(height.getValue());
				height.setValue(w);
			}
		}
	};
	Property<Boolean> mirrored = new Property<Boolean>(PROPERTY_MIRRORED, false, Boolean.class);

	private Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	private Property<String> label;
	private Property<Boolean> border = new Property<Boolean>(PROPERTY_SHOW_BORDER, false, Boolean.class);
	private Property<Boolean> aspectLocked = new Property<Boolean>(PROPERTY_ASPECT_LOCKED, true, Boolean.class);

	private Property<Boolean> background = new Property<Boolean>(PROPERTY_SHOW_BACKGROUND, false, Boolean.class);
	private Property<Color> color = new Property<Color>(PROPERTY_BACKGROUND_COLOR, Color.WHITE, Color.class);

	private List<Point> cleared = new ArrayList<>();

	public MapImage(String label) {
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
	}

	protected MapImage(int id, String label) {
		super(id);
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
	}

	@Override
	public String getIDString() {
		return "Image (" + label + ")";
	}

	@Override
	public MeasurementLog getMemoryUsage() {
		MeasurementLog m = new MeasurementLog("Image (" + label + ")", id);
		if (image != null) {
			m.components = new MeasurementLog[1];
			m.components[0] = image.getMemoryUsage();
			m.updateTotal();
		}
		return m;
	}

	@Override
	public Layer getDefaultLayer() {
		return Layer.MAP_BACKGROUND;
	}

	@Override
	public void addChild(MapElement e) {
		super.addChild(e);
		if (e instanceof Mask) {
			if (mask != null) mask.setImageElement(null);
			mask = (Mask) e;
			mask.setImageElement(this);
		}
	}

	@Override
	public void removeChild(MapElement e) {
		if (e == mask) {
			mask.setImageElement(null);
			mask = null;
		}
		super.removeChild(e);
	}

	// returns the AffineTransform that would transform an image of the specified width and height to the
	// dimensions of this element. the AffineTransform includes and rotations and mirroring set on this element
	AffineTransform getTransform(int srcWidth, int srcHeight) {
		// get the unrotated size of the element in display coordinates
		double w, h;
		if (rotations.getValue() % 2 == 0) {
			w = width.getValue();
			h = height.getValue();
		} else {
			w = height.getValue();
			h = width.getValue();
		}
		Dimension displaySize = canvas.getDisplayDimension(w, h);

		AffineTransform transform;
		if (mirrored.getValue()) {
			transform = AffineTransform.getScaleInstance(-1, 1);
			transform.quadrantRotate(-rotations.getValue());
		} else {
			transform = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
		}
		transform.scale(displaySize.getWidth() / srcWidth, displaySize.getHeight() / srcHeight);
		return transform;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;
		if (image == null || image.getImage() == null) return;

		Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
		g.translate(o.getX(), o.getY());

		Shape oldClip = g.getClip();
		// build the shape
		Area area = new Area(g.getClip());
		// using indexed loop instead of iterator to avoid concurrency issues
		for (int i = 0; i < cleared.size(); i++) {
			Point p = cleared.get(i);
			Point tl = canvas.convertGridCoordsToDisplay(p.x, p.y);
			Point br = canvas.convertGridCoordsToDisplay(p.x + 1, p.y + 1);
			area.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
		}
		g.setClip(area);

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

		Point offset = canvas.convertGridCoordsToDisplay(location.getValue());

		// update the image transform. this needs to be done on every repaint as the grid size may have changed
		AffineTransform transform = getTransform(image.getSourceWidth(), image.getSourceHeight());
		image.setTransform(transform);
		BufferedImage img = image.getImage();

		// background
		if (background.getValue()) {
			g.setColor(color.getValue());
			g.fillRect(offset.x, offset.y, image.getImage().getWidth(), image.getImage().getHeight());
		}

		// if we have a visible mask then we combine it with our image before painting to guarantee that an unmasked image is never shown
		if (mask != null) {
			BufferedImage maskImg = mask.getMaskImage();
			BufferedImage overImg = mask.getCombinedImage();
			BufferedImage bgImg = img;
			img = new BufferedImage(bgImg.getWidth(), bgImg.getHeight(), bgImg.getType());
			Graphics2D imgG = img.createGraphics();
			imgG.drawImage(bgImg, 0, 0, null);
			imgG.drawImage(overImg, 0, 0, null);
			if (mask.getVisibility() == Visibility.VISIBLE) {
				imgG.drawImage(maskImg, 0, 0, null);
			}
		}

		g.drawImage(img, offset.x, offset.y, null);

		// explicit border or highlight border for selected element
		if (border.getValue() || selected) {
			g.setColor(selected ? Color.BLUE : Color.RED);
			g.drawRect(offset.x, offset.y, image.getImage().getWidth(), image.getImage().getHeight());
			g.drawRect(offset.x + 1, offset.y + 1, image.getImage().getWidth() - 2, image.getImage().getHeight() - 2);
		}

		g.setComposite(c);
		g.setClip(oldClip);
		g.translate(-o.getX(), -o.getY());
	}

	/**
	 *
	 * @return array of the points defining the centres of the cubes
	 */
	public Point[] getCells() {
		Point[] ps = new Point[cleared.size()];
		for (int i = 0; i < ps.length; i++) {
			ps[i] = new Point(cleared.get(i));
		}
		return ps;
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Image ("+getID()+")";
		return "Image ("+label+")";
	}

	private void setURI(URI uri) {
		image = MediaManager.INSTANCE.getImageMedia(canvas, uri);
		if (image == null) return;
		width.setValue(image.getSourceGridWidth());
		height.setValue(image.getSourceGridHeight());
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_CLEARCELL)) {
			setCleared((Point)value, true);
		} else if (property.equals(PROPERTY_UNCLEARCELL)) {
			setCleared((Point)value, false);
		} else if (property.equals(PROPERTY_IMAGE)) {
			setURI((URI) value);
		} else if (property.equals(PROPERTY_IMAGE_PLAY)) {
			if (image != null) image.playOrPause();
		} else if (property.equals(PROPERTY_IMAGE_STOP)) {
			if (image != null) image.stop();
		} else if (property.equals(PROPERTY_WIDTH) && aspectLocked.value) {
			double aspect = width.value / height.value;
			super.setProperty(property, value);
			if (Double.isFinite(aspect) && aspect != 0) {
				super.setProperty(PROPERTY_HEIGHT, width.value / aspect);
			}
		} else if (property.equals(PROPERTY_HEIGHT) && aspectLocked.value) {
			double aspect = width.value / height.value;
			super.setProperty(property, value);
			if (Double.isFinite(aspect) && aspect != 0) {
				super.setProperty(PROPERTY_WIDTH, height.value * aspect);
			}
		} else {
			super.setProperty(property, value);
		}

		if ((property.equals(PROPERTY_X)
				|| property.equals(PROPERTY_Y)
				|| property.equals(PROPERTY_ROTATIONS)
				|| property.equals(PROPERTY_MIRRORED)
				|| property.equals(PROPERTY_WIDTH)
				|| property.equals(PROPERTY_HEIGHT)
				|| property.equals(PROPERTY_LOCATION))
				&& canvas != null) {
			for (MapElement e : canvas) {
//			ListModel<MapElement> model = canvas.getModel();
//			for (int i = 0; i < model.getSize(); i++) {
//				MapElement e = model.getElementAt(i);
				if (e instanceof Walls && e.parent == this) {
					Walls w = (Walls) e;
					w.imageUpdated();
				}
			}
		}

	}

	public boolean isCleared(Point p) {
		return cleared.contains(p);
	}

	public void setCleared(Point p, boolean clear) {
		if (!clear) {
			cleared.remove(p);
			canvas.repaint();
		} else if (!cleared.contains(p)) {
			cleared.add(p);
			canvas.repaint();
		}
	}
}

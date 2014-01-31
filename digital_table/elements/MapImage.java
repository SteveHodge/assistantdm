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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.ImageMedia;
import digital_table.server.MapCanvas.Order;
import digital_table.server.MediaManager;

// TODO could have border color property

public class MapImage extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ALPHA = "alpha";	// float
	//	public final static String PROPERTY_FILENAME = "filename";	// String - read only
	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_WIDTH = "width";	// double
	public final static String PROPERTY_HEIGHT = "height";	// double
	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double
	public final static String PROPERTY_CLEARCELL = "clear";	// Point - when this property is set the specified cell will be cleared
	public final static String PROPERTY_UNCLEARCELL = "unclear";	// Point - when this property is set the specified cell will be shown again
	public final static String PROPERTY_IMAGE = "image";	// URI currently write-only (but change to read-write)
	public final static String PROPERTY_IMAGE_PLAY = "play";	// write only no value
	public final static String PROPERTY_IMAGE_STOP = "stop";	// write only no value
	public final static String PROPERTY_SHOW_BORDER = "show_border";	// boolean
	public final static String PROPERTY_ASPECT_LOCKED = "aspect_locked";	// boolean

	private transient ImageMedia image = null;

	// position in grid coordinate-space:
	private Property<Double> x = new Property<Double>(PROPERTY_X, 0d, Double.class);
	private Property<Double> y = new Property<Double>(PROPERTY_Y, 0d, Double.class);

	// scaled dimensions in grid coordinate-space:
	private Property<Double> width = new Property<Double>(PROPERTY_WIDTH, 0d, Double.class);
	private Property<Double> height = new Property<Double>(PROPERTY_HEIGHT, 0d, Double.class);

	private Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, 0, Integer.class) {
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

	private Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	private Property<String> label;
	private Property<Boolean> border = new Property<Boolean>(PROPERTY_SHOW_BORDER, false, Boolean.class);
	private Property<Boolean> aspectLocked = new Property<Boolean>(PROPERTY_ASPECT_LOCKED, true, Boolean.class);

	private List<Point> cleared = new ArrayList<Point>();

	public MapImage(String label) {
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
	}

	public MapImage(URI uri, String label) {
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
		setURI(uri);
	}

	protected MapImage(int id, String label) {
		super(id);
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
	}

	@Override
	public Order getDefaultOrder() {
		return Order.BOTTOM;
	}

	@Override
	public void paint(Graphics2D g, Point2D off) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;
		if (image.getImage() == null) return;
		//long startTime = System.nanoTime();
		Point2D o = canvas.getDisplayCoordinates((int) off.getX(), (int) off.getY());
		g.translate(o.getX(), o.getY());

		Shape oldClip = g.getClip();
		// build the shape
		Area area = new Area(g.getClip());
		// using indexed loop instead of iterator to avoid concurrency issues
		for (int i = 0; i < cleared.size(); i++) {
			Point p = cleared.get(i);
			Point tl = canvas.getDisplayCoordinates(p.x, p.y);
			Point br = canvas.getDisplayCoordinates(p.x+1, p.y+1);
			area.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
		}
		g.setClip(area);

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

		Point offset = canvas.getDisplayCoordinates(new Point2D.Double(x.getValue(), y.getValue()));
		// update the image transform. this needs to be done on every repaint as the grid size may have changed
		AffineTransform transform = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
		double w, h;
		if (rotations.getValue() % 2 == 0) {
			w = width.getValue();
			h = height.getValue();
		} else {
			w = height.getValue();
			h = width.getValue();
		}
		Dimension displaySize = canvas.getDisplayDimension(w, h);
		transform.scale(displaySize.getWidth() / image.getSourceWidth(), displaySize.getHeight() / image.getSourceHeight());
		image.setTransform(transform);
		g.drawImage(image.getImage(), offset.x, offset.y, null);

		// border
		if (border.getValue()) {
			g.setColor(Color.RED);
			g.drawRect(offset.x, offset.y, image.getImage().getWidth(), image.getImage().getHeight());
		}

		g.setComposite(c);
		g.setClip(oldClip);
		g.translate(-o.getX(), -o.getY());
		//long micros = (System.nanoTime() - startTime) / 1000;
		//logger.info("Painting complete for " + this + " in " + micros + "ms");
		//System.out.println("Image painting took "+micros+"ms");
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
			super.setProperty(PROPERTY_HEIGHT, width.value / aspect);
		} else if (property.equals(PROPERTY_HEIGHT) && aspectLocked.value) {
			double aspect = width.value / height.value;
			super.setProperty(property, value);
			super.setProperty(PROPERTY_WIDTH, height.value * aspect);
		} else {
			super.setProperty(property, value);
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

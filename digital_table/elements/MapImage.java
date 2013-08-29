package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.MapCanvas.Order;

// TODO cache scaled image for performance
// TODO should have some sort of persistent cache so we don't have to keep the image file bytes in memory and don't have to resend the image each time
// TODO create grid-aligned property

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
	public final static String PROPERTY_IMAGE = "image";	// byte[]

	private transient ImageManager image = null;
	private byte[] bytes = null;	// used to store the raw bytes so we can be serialised

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
			if (rotations.getValue().equals(r)) return;
			if (image != null) image.rotatedImage = null;	// TODO remove - shouldn't be visible
			super.setValue(r);
		}
	};

	private Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	private Property<String> label;

	private List<Point> cleared = new ArrayList<Point>();

	public MapImage(String label) {
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
	}

	public MapImage(byte[] b, String label) {
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
		bytes = b;
	}

	protected MapImage(int id, String label) {
		super(id);
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
	}

	@Override
	public Order getDefaultOrder() {
		return Order.BOTTOM;
	}

	private BufferedImage getImage() {
		if (image == null) {
			image = ImageManager.createImageManager(canvas, bytes);
		}
		BufferedImage img = image.getImage(rotations.getValue());
		if (width.getValue() == 0 || height.getValue() == 0) {
			width.setValue(image.getSourceGridWidth());
			height.setValue(image.getSourceGridHeight());
		}
		return img;
	}

	/* (non-Javadoc)
	 * @see server.MapRenderer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g, Point2D off) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;
		long startTime = System.nanoTime();
		Point2D o = canvas.getDisplayCoordinates((int) off.getX(), (int) off.getY());
		g.translate(o.getX(), o.getY());

		Rectangle bounds = g.getClipBounds();
		//System.out.println("Clip = "+bounds);

		BufferedImage img = getImage();
		if (img != null) {
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

			Point2D p = new Point2D.Double(width.getValue(),height.getValue());
			Point bottomRight = canvas.getDisplayCoordinates(p);
			Point offset = canvas.getDisplayCoordinates(new Point2D.Double(x.getValue(), y.getValue()));
			//System.out.println("Grid coordinates: ("+x+","+y+") x ("+p.getX()+","+p.getY()+")");
			//System.out.println("Display coordinates: "+offset+" x "+bottomRight);

			int left, right, top, bottom;
			left = (bounds.x - offset.x) * img.getWidth() / bottomRight.x;
			top = (bounds.y - offset.y) * img.getHeight() / bottomRight.y;
			right = (bounds.x + bounds.width - offset.x) * img.getWidth() / bottomRight.x;
			bottom = (bounds.y + bounds.height - offset.y) * img.getHeight() / bottomRight.y;

//			g.drawImage(img, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
//					left, top, right, bottom, new Color(255,255,255,0), null);
			g.drawImage(img, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
					left, top, right, bottom, null);

			g.setComposite(c);
			g.setClip(oldClip);
		}
		g.translate(-o.getX(), -o.getY());
		long micros = (System.nanoTime() - startTime) / 1000;
		//logger.info("Painting complete for " + this + " in " + micros + "ms");
		//System.out.println("Image painting took "+micros+"ms");
	}

	public Dimension getImageSize() {
		BufferedImage img = getImage();
		return new Dimension(img.getWidth(), img.getHeight());
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

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_CLEARCELL)) {
			setCleared((Point)value, true);
		} else if (property.equals(PROPERTY_UNCLEARCELL)) {
			setCleared((Point)value, false);
		} else if (property.equals(PROPERTY_IMAGE)) {
			bytes = (byte[]) value;
			image = ImageManager.createImageManager(canvas, bytes);
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

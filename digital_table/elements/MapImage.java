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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import digital_table.server.MapCanvas.Order;

// TODO cache scaled image for performance
// TODO should have some sort of persistent cache so we don't have to keep the image file bytes in memory and don't have to resend the image each time

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

	protected transient BufferedImage sourceImage = null;
	protected transient BufferedImage rotatedImage = null;
	byte[] bytes;	// used to store the raw bytes so we can be serialised

	// position in grid coordinate-space:
	Property<Double> x = new Property<Double>(PROPERTY_X, 0d, Double.class);
	Property<Double> y = new Property<Double>(PROPERTY_Y, 0d, Double.class);

	// scaled dimensions in grid coordinate-space:
	Property<Double> width = new Property<Double>(PROPERTY_WIDTH, 0d, Double.class);
	Property<Double> height = new Property<Double>(PROPERTY_HEIGHT, 0d, Double.class);

	Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, 0, Integer.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Integer r) {
			r = r % 4;
			if (rotations.getValue().equals(r)) return;
			rotatedImage = null;
			super.setValue(r);
		}
	};

	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<String> label;

	List<Point> cleared = new ArrayList<Point>();

	public MapImage(byte[] b, String label) throws IOException {
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
		bytes = b;
	}

	@Override
	public Order getDefaultOrder() {
		return Order.BOTTOM;
	}

	protected void createRotatedImage() {
		if (sourceImage == null) {
			//			sourceImage = ImageIO.read(f);
			//			if (label == null) setLabel(f.getName());
			try {
				ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
				sourceImage = ImageIO.read(stream);
				// we could now drop the bytes array at the cost of no longer being serializable
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (sourceImage != null) {
			AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
			Point p = new Point(sourceImage.getWidth(),sourceImage.getHeight());
			t.transform(p,p);	// transform to get new dimensions

			rotatedImage = new BufferedImage(Math.abs(p.x), Math.abs(p.y), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D)rotatedImage.getGraphics();
			g2d.rotate(Math.toRadians(rotations.getValue()*90), rotatedImage.getWidth() / 2, rotatedImage.getHeight() / 2);
			g2d.translate((rotatedImage.getWidth() - sourceImage.getWidth()) / 2, (rotatedImage.getHeight() - sourceImage.getHeight()) / 2);
			g2d.drawImage(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
			g2d.dispose();

			// get the dimensions in grid-coordinate space of the remote display:
			// TODO strictly speaking we should calculate the bottom left corner and then use that to determine the size
			Point2D size = canvas.getRemoteGridCellCoords(rotatedImage.getWidth(), rotatedImage.getHeight());
			width.setValue(size.getX());
			height.setValue(size.getY());
		}
	}

	/* (non-Javadoc)
	 * @see server.MapRenderer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || !isVisible()) return;

		Rectangle bounds = g.getClipBounds();
		//System.out.println("Clip = "+bounds);

		if (rotatedImage == null) {
			createRotatedImage();		// TODO invoke this later and then schedule repaint?
		}
		if (rotatedImage != null) {
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
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));

			Point2D p = new Point2D.Double(width.getValue(),height.getValue());
			Point bottomRight = canvas.getDisplayCoordinates(p);
			Point offset = canvas.getDisplayCoordinates(new Point2D.Double(x.getValue(), y.getValue()));
			//System.out.println("Grid coordinates: ("+x+","+y+") x ("+p.getX()+","+p.getY()+")");
			//System.out.println("Display coordinates: "+offset+" x "+bottomRight);

			int left, right, top, bottom;
			left = (bounds.x - offset.x) * rotatedImage.getWidth() / bottomRight.x;
			top = (bounds.y - offset.y) * rotatedImage.getHeight() / bottomRight.y;
			right = (bounds.x + bounds.width - offset.x) * rotatedImage.getWidth() / bottomRight.x;
			bottom = (bounds.y + bounds.height - offset.y) * rotatedImage.getHeight() / bottomRight.y;

			g.drawImage(rotatedImage, bounds.x, bounds.y, bounds.x+bounds.width, bounds.y+bounds.height,
					left, top, right, bottom, new Color(255,255,255,0), null);

			g.setComposite(c);
			g.setClip(oldClip);
		}
	}

	public Dimension getImageSize() {
		if (rotatedImage == null) {
			createRotatedImage();
		}
		return new Dimension(rotatedImage.getWidth(), rotatedImage.getHeight());
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

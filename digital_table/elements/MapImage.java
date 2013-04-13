package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

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

	protected transient BufferedImage sourceImage = null;
	protected transient BufferedImage rotatedImage = null;
	byte[] bytes;	// used to store the raw bytes so we can be serialised  

	double x, y;	// position in grid coordinate-space
	double width, height;	// scaled dimensions in grid coordinate-space
	int rotations = 0;
	float alpha = 1.0f;
	String label;

	public MapImage(byte[] b, String label) throws IOException {
		this.label = label;
		bytes = b;
	}
	
	public Order getDefaultOrder() {
		return Order.Bottom;
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
			AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations);
			Point p = new Point(sourceImage.getWidth(),sourceImage.getHeight());
			t.transform(p,p);	// transform to get new dimensions
	
			rotatedImage = new BufferedImage(Math.abs(p.x), Math.abs(p.y), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D)rotatedImage.getGraphics();
			g2d.rotate(Math.toRadians(rotations*90), rotatedImage.getWidth() / 2, rotatedImage.getHeight() / 2);
			g2d.translate((rotatedImage.getWidth() - sourceImage.getWidth()) / 2, (rotatedImage.getHeight() - sourceImage.getHeight()) / 2);
			g2d.drawImage(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
			g2d.dispose();
	
			// get the dimensions in grid-coordinate space of the remote display:
			// TODO strictly speaking we should calculate the bottom left corner and then use that to determine the size
			Point2D size = canvas.getRemoteGridCellCoords(rotatedImage.getWidth(), rotatedImage.getHeight());
			setWidth(size.getX());
			setHeight(size.getY());
		}
	}

	/* (non-Javadoc)
	 * @see server.MapRenderer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		Rectangle bounds = g.getClipBounds();
		//System.out.println("Clip = "+bounds);

		if (rotatedImage == null) {
			createRotatedImage();		// TODO invoke this later and then schedule repaint?
		}
		if (rotatedImage != null) {
			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

			Point2D p = new Point2D.Double(width,height);
			Point bottomRight = canvas.getDisplayCoordinates(p);
			Point offset = canvas.getDisplayCoordinates(getLocation("IMAGE"));
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
		}
	}

	public Dimension getImageSize() {
		if (rotatedImage == null) {
			createRotatedImage();
		}
		return new Dimension(rotatedImage.getWidth(), rotatedImage.getHeight());
	}

	public String toString() {
		if (label == null || label.length() == 0) return "Image ("+getID()+")";
		return "Image ("+label+")";
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String l) {
		String old = label;
		label = l;
		pcs.firePropertyChange(PROPERTY_LABEL, old, label);
	}

	public Object getProperty(String property) {
		if (property.equals(PROPERTY_ALPHA)) {
			return getAlpha();
		} else if (property.equals(PROPERTY_X)) {
			return getX();
		} else if (property.equals(PROPERTY_Y)) {
			return getY();
		} else if (property.equals(PROPERTY_WIDTH)) {
			return getWidth();
		} else if (property.equals(PROPERTY_HEIGHT)) {
			return getHeight();
		} else if (property.equals(PROPERTY_ROTATIONS)) {
			return getRotations();
		} else if (property.equals(PROPERTY_LABEL)) {
			return getLabel();
		} else {
			// throw exception?
			return null;
		}
	}

	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_ALPHA)) {
			setAlpha((Float)value);
		} else if (property.equals(PROPERTY_X)) {
			setX((Double)value);
		} else if (property.equals(PROPERTY_Y)) {
			setY((Double)value);
		} else if (property.equals(PROPERTY_WIDTH)) {
			setWidth((Double)value);
		} else if (property.equals(PROPERTY_HEIGHT)) {
			setHeight((Double)value);
		} else if (property.equals(PROPERTY_ROTATIONS)) {
			setRotations((Integer)value);
		} else if (property.equals(PROPERTY_LABEL)) {
			setLabel((String)value);
		} else {
			// throw exception?
		}
	}

	public float getAlpha() {
		return alpha;
	}
	
	public void setAlpha(float a) {
		if (alpha == a) return;
		float old = alpha;
		alpha = a;
		pcs.firePropertyChange(PROPERTY_ALPHA, old, alpha);
		if (canvas != null) canvas.repaint();
	}

	public double getX() {
		return x;
	}
	
	public void setX(double newX) {
		if (x == newX) return;
		double old = x;
		x = newX;
		pcs.firePropertyChange(PROPERTY_X, old, x);
		if (canvas != null) canvas.repaint();
	}

	public double getY() {
		return y;
	}
	
	public void setY(double newY) {
		if (y == newY) return;
		double old = y;
		y = newY;
		pcs.firePropertyChange(PROPERTY_Y, old, y);
		if (canvas != null) canvas.repaint();
	}

	public double getWidth() {
		return width;
	}
	
	public void setWidth(double w) {
		if (width == w) return;
		double old = width;
		width = w;
		pcs.firePropertyChange(PROPERTY_WIDTH, old, width);
		if (canvas != null) canvas.repaint();
	}

	public double getHeight() {
		return height;
	}
	
	public void setHeight(double h) {
		if (height == h) return;
		double old = height;
		height = h;
		pcs.firePropertyChange(PROPERTY_HEIGHT, old, height);
		if (canvas != null) canvas.repaint();
	}

	public int getRotations() {
		return rotations;
	}
	
	public void setRotations(int r) {
		r = r % 4;
		if (rotations == r) return;
		int old = rotations;
		rotations = r;
		rotatedImage = null;
		pcs.firePropertyChange(PROPERTY_ROTATIONS, old, rotations);
		if (canvas != null) canvas.repaint();
	}
	
	public DragMode getDragMode() {
		return DragMode.MOVE;
	}

	public Object getDragTarget(Point2D gridLocation) {
		return "IMAGE";
	}

	public Point2D getLocation(Object target) {
		if (target.equals("IMAGE")) {
			return new Point2D.Double(x,y);
		}
		return null;
	}

	public void setLocation(Object target, Point2D p) {
		if (target.equals("IMAGE")) {
			setX(p.getX());
			setY(p.getY());
		}
	}
}

package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.net.URI;

import digital_table.server.ImageMedia;
import digital_table.server.MediaManager;

public class LineTemplate extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_X = "x";	// int
	public final static String PROPERTY_Y = "y";	// int
	public final static String PROPERTY_ORIGIN_X = "origin_x";	// int
	public final static String PROPERTY_ORIGIN_Y = "origin_y";	// int
	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_RANGE = "range";	// int
	public final static String PROPERTY_LABEL = "label";
	public static final String PROPERTY_TARGET_LOCATION = "target";	// Point
	public static final String PROPERTY_ORIGIN_LOCATION = "origin";	// Point
	public final static String PROPERTY_IMAGE = "image";	// URI - write only (change to read/write)
	public final static String PROPERTY_IMAGE_VISIBLE = "image_visible";	// boolean
	public final static String PROPERTY_IMAGE_PLAY = "play";	// write only
	public final static String PROPERTY_IMAGE_STOP = "stop";	// write only
	public final static String PROPERTY_SHADING_VISIBLE = "shading_visible";	// boolean

	private Property<Integer> originX, originY, targetX, targetY;
	private Property<Integer> range = new Property<Integer>(PROPERTY_RANGE, 12, Integer.class);
	private Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.RED, Color.class);
	private Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 0.5f, Float.class);
	private Property<String> label = new Property<String>(PROPERTY_LABEL, false, "", String.class);
	private Property<Boolean> imageVisible = new Property<Boolean>(PROPERTY_IMAGE_VISIBLE, true, false, Boolean.class);
	private Property<Boolean> shadingVisible = new Property<Boolean>(PROPERTY_SHADING_VISIBLE, true, true, Boolean.class);

	private transient ImageMedia image = null;	// don't access directly as it's transient
	private transient AffineTransform transform = null;

	public LineTemplate(int ox, int oy, int tx, int ty) {
		originX = new Property<Integer>(PROPERTY_ORIGIN_X, ox, Integer.class);
		originY = new Property<Integer>(PROPERTY_ORIGIN_Y, oy, Integer.class);
		targetX = new Property<Integer>(PROPERTY_X, tx, Integer.class);
		targetY = new Property<Integer>(PROPERTY_Y, ty, Integer.class);
	}

	@Override
	public Layer getDefaultLayer() {
		return Layer.TEMPLATE;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
		g.translate(o.getX(), o.getY());

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

		int tx = targetX.getValue();
		int ox = originX.getValue();
		int ty = targetY.getValue();
		int oy = originY.getValue();

		// calculate the end point of the line based on range
		double dist = Math.sqrt((tx - ox) * (tx - ox) + (ty - oy) * (ty - oy));
		double endX = ox + range.getValue() * (tx - ox) / dist;
		double endY = oy + range.getValue() * (ty - oy) / dist;

		if (shadingVisible.getValue()) {
			// test for affected cells
			g.setColor(color.getValue());
			// this version uses intersections of the line with the rectangle formed by each cell
			// it doesn't work consistently for cells that have a corner on the line. could simply special-case those
			// cells but the below implementation *should* be more efficient
			//		int minX, maxX, minY, maxY;
			//		minX = (int)(originX < endX ? originX : endX-1);
			//		minY = (int)(originY < endY ? originY : endY-1);
			//		maxX = (int)(endX < originX ? originX : endX+1);
			//		maxY = (int)(endY < originY ? originY : endY+1);
			//		for (int x = minX; x < maxX; x++) {
			//			boolean seen = false;
			//			for (int y = minY; y < maxY; y++) {
			//				Rectangle2D r = new Rectangle2D.Double(x,y,1,1);
			//				if (r.intersectsLine(originX, originY, endX, endY)) {
			//					Point tl = canvas.getDisplayCoordinates(x, y);
			//					Point br = canvas.getDisplayCoordinates(x+1, y+1);
			//					g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
			//					seen = true;
			//				} else if (seen) {
			//					break;	// this cell is not included so no other cells in this column can be included either
			//				}
			//			}
			//		}
			//		System.out.println();
			if (tx == ox) {
				int minY = oy;
				int maxY = 1 + (int) endY;
				if (maxY < minY) {
					int t = minY;
					minY = maxY - 2;
					maxY = t;
				}
				Point tl = canvas.convertGridCoordsToDisplay(ox - 1, minY);
				Point br = canvas.convertGridCoordsToDisplay(ox + 1, maxY);
				g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
			} else if (tx > ox) {
				for (int x = 0; x <= endX - ox; x++) {
					Point range = getVerticalRange(x, tx, endX, endY);
					//System.out.println("x = "+x+", miny = "+range.x+", maxy = "+range.y+", endX = "+endX);
					for (int y = range.x; y <= range.y; y++) {
						Point tl = canvas.convertGridCoordsToDisplay(x + ox, y);
						Point br = canvas.convertGridCoordsToDisplay(x + ox + 1, y + 1);
						g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
					}
				}
			} else {
				// targetX < originX
				for (int x = 0; x >= endX - ox; x--) {
					// we mirror the relevant x coordinates to calculate the range as if it were the other case
					Point range = getVerticalRange(-x, ox * 2 - tx, ox * 2 - endX, endY);
					for (int y = range.x; y <= range.y; y++) {
						Point tl = canvas.convertGridCoordsToDisplay(x + ox - 1, y);
						Point br = canvas.convertGridCoordsToDisplay(x + ox, y + 1);
						g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
					}
				}
			}
		}
		g.setColor(darken(color.getValue()));
		Stroke oldStroke = g.getStroke();
		g.setStroke(getThickStroke());
		Point s = canvas.convertGridCoordsToDisplay(ox, oy);
		Point e = canvas.convertGridCoordsToDisplay(new Point2D.Double(endX, endY));
		g.drawLine(s.x, s.y, e.x, e.y);
		Point t = canvas.convertGridCoordsToDisplay(tx, ty);
		g.fillOval(t.x - 5, t.y - 5, 10, 10);
		t = canvas.convertGridCoordsToDisplay(ox, oy);
		g.fillOval(t.x - 5, t.y - 5, 10, 10);
		g.setStroke(oldStroke);
		g.setComposite(c);

		if (imageVisible.getValue() && image != null) {
//			int frame = image.getCurrentFrameIndex();
//
//			long startTime = System.nanoTime();
			if (transform == null) {
				transform = new AffineTransform();
				transform.setToRotation(e.x - s.x, e.y - s.y);
				// TODO should probably calculate width based on right - left
				Dimension size = canvas.getDisplayDimension(range.getValue(), 3);
				transform.scale(size.getWidth() / image.getSourceWidth(), size.getHeight() / image.getSourceHeight());
				transform.translate(0, -image.getSourceHeight() / 2);
				image.setTransform(transform);
			}
			g.drawImage(image.getImage(), s.x + (int) image.getTransformedXOffset(), s.y + (int) image.getTransformedYOffset(), null);

			// try transforming the line in two parts

//			long nanos = System.nanoTime() - startTime;
//			totalNanos += nanos;
//			totalFrames++;
//			double millis = (double) nanos / 1000000;
//			double avgMillis = (double) totalNanos / totalFrames / 1000000;
//			//logger.info("Image drawn for " + this + " in " + micros + "ms");
//			System.out.println(String.format("Image " + frame + " took %.3f ms, fps = %.1f. Average after %d: %.3f ms, fps = %.1f", millis, 1000d / millis, totalFrames, avgMillis, 1000d / avgMillis));
		}

		g.translate(-o.getX(), -o.getY());
	}

	@Override
	public String getIDString() {
		return "LineTemplate" + (label == null || label.getValue().length() == 0 ? "" : " (" + label + ")");
	}

//	private long totalNanos = 0;
//	private int totalFrames = 0;

	protected Point getVerticalRange(int x, int targetX, double endX, double endY) {
		int minY, maxY;	// the range of cells to paint for this column. note that for lines with negative gradiant minY will be larger than maxY
		int originX = this.originX.getValue();
		int originY = this.originY.getValue();
		int targetY = this.targetY.getValue();
		int deltaX = targetX - originX;
		int deltaY = targetY - originY;

		if (originY > targetY) {
			maxY = x * (deltaY) / (deltaX);
			if ((x > 0 || targetY == originY) && maxY * deltaX == x * deltaY) {
				// the smallest y position is an integer then include the previous cell
				// we don't do this for the first column unless the line is horizontal
				maxY++;
			}
			maxY += originY - 1;
			if (x + 1 + originX > endX) {
				//System.out.println("Using endY = "+endY);
				minY = (int) endY;	// if the next column is past endX then use endY as the end of the range to fill
			}
			else
				minY = (x + 1) * deltaY / deltaX + originY - 1;
		} else {
			minY = x * deltaY / deltaX;
			if ((x > 0 || targetY == originY) && minY * deltaX == x * deltaY) {
				// the smallest y position is an integer then include the previous cell
				// we don't do this for the first column unless the line is horizontal
				minY--;
			}
			minY += originY;
			if (x + 1 + originX > endX) {
				//System.out.println("Using endY = "+endY);
				maxY = (int) endY;	// if the next column is past endX then use endY as the end of the range to fill
			}
			else
				maxY = (x + 1) * deltaY / deltaX + originY;
		}
		return new Point(minY, maxY);
	}

	protected Stroke getThickStroke() {
		if (canvas.getColumnWidth() < 40) return new BasicStroke(3);
		return new BasicStroke(5);
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Line (" + getID() + ")";
		return "Line (" + label + ")";
	}

	@Override
	public Object getProperty(String property) {
		if (property.equals(PROPERTY_ORIGIN_LOCATION)) {
			return new Point(originX.getValue(), originY.getValue());
		} else if (property.equals(PROPERTY_TARGET_LOCATION)) {
			return new Point(targetX.getValue(), targetY.getValue());
		} else {
			return super.getProperty(property);
		}
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_ORIGIN_LOCATION)) {
			Point2D p = (Point2D) value;
			originX.setValue((int) Math.round(p.getX()));
			originY.setValue((int) Math.round(p.getY()));
		} else if (property.equals(PROPERTY_TARGET_LOCATION)) {
			Point2D p = (Point2D) value;
			targetX.setValue((int) Math.round(p.getX()));
			targetY.setValue((int) Math.round(p.getY()));
		} else if (property.equals(PROPERTY_IMAGE)) {
			URI uri = (URI) value;
			if (uri == null) {
				if (image != null) image.stop();
				image = null;
			} else {
				image = MediaManager.INSTANCE.getImageMedia(canvas, (URI) value);
			}
		} else if (property.equals(PROPERTY_IMAGE_PLAY)) {
			if (image != null) image.playOrPause();
		} else if (property.equals(PROPERTY_IMAGE_STOP)) {
			if (image != null) image.stop();
		} else {
			super.setProperty(property, value);
		}
		if (property.equals(PROPERTY_X)
				|| property.equals(PROPERTY_Y)
				|| property.equals(PROPERTY_ORIGIN_X)
				|| property.equals(PROPERTY_ORIGIN_Y)
				|| property.equals(PROPERTY_ORIGIN_LOCATION)
				|| property.equals(PROPERTY_TARGET_LOCATION)
				|| property.equals(PROPERTY_RANGE)) {
			// position of the line has changed so reset the transform
			transform = null;
		}
	}
}

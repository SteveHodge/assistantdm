package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import digital_table.server.MapCanvas.Order;
import digital_table.server.MeasurementLog;
import ui.CreatureStatus;
import ui.SimpleStatus;
import ui.Status;

public class Token extends Group {
	private static final long serialVersionUID = 1L;

//	public final static String PROPERTY_X = "x";	// int
//	public final static String PROPERTY_Y = "y";	// int
	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_IMAGE = "image";	// byte[]
	public final static String PROPERTY_REACH = "reach";	// int - in feet
	public final static String PROPERTY_SPACE = "space";	// int - in 1/2 foot units
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_SHOWREACH = "show_reach";	// boolean
	public final static String PROPERTY_REACHWEAPON = "reach_weapon";	// boolean
	public final static String PROPERTY_MAX_HPS = "max_hps";	// int
	public final static String PROPERTY_CURRENT_HPS = "current_hps";	// int
	public final static String PROPERTY_STATUS_TYPE = "status_type";	// StatusType
	public final static String PROPERTY_STATUS_DISPLAY = "status_display";	// StatusDisplay
	public final static String PROPERTY_WEB_LABEL = "web_label";	// this property does nothing on the real token, but TokenOverlay can set this to report changes back to the ui

	public enum StatusDisplay {
		NONE("Don't show"),
		LABEL("In label"),
		BACKGROUND("Background colour"),
		SPOT("Spot"),
		BORDER("Border colour");	//, FLOATING_LABEL, VERTICAL_BAR, HORIZONTAL_BAR

		@Override
		public String toString() {
			return description;
		}

		private StatusDisplay(String d) {
			description = d;
		}

		private String description;
	}

	public enum StatusType {
		EXACT("Exact HPs") {
			@Override
			public CreatureStatus getStatus(final int max, final int current) {
				return new CreatureStatus() {
					@Override
					public String toString() {
						return "" + current + "/" + max;
					}

					@Override
					public Color getColor() {
						return Color.BLACK;
					}
				};
			}
		},
		SIMPLE_STATUS("Basic status") {
			@Override
			public CreatureStatus getStatus(int m, int c) {
				return SimpleStatus.getStatus(m, c);
			}
		},
		STATUS("Detailed status") {
			@Override
			public CreatureStatus getStatus(int m, int c) {
				return Status.getStatus(m, c);
			}
		};

		public abstract CreatureStatus getStatus(int max, int current);

		@Override
		public String toString() {
			return description;
		}

		private StatusType(String d) {
			description = d;
		}

		private String description;
	}

	private Property<StatusDisplay> statusDisplay = new Property<StatusDisplay>(PROPERTY_STATUS_DISPLAY, StatusDisplay.NONE, StatusDisplay.class);
	private Property<StatusType> statusType = new Property<StatusType>(PROPERTY_STATUS_TYPE, StatusType.SIMPLE_STATUS, StatusType.class);
//	private Property<CreatureSize> size = new Property<CreatureSize>(PROPERTY_SIZE, CreatureSize.MEDIUM, CreatureSize.class);
	private Property<Integer> space = new Property<Integer>(PROPERTY_SPACE, 10, Integer.class);
	private Property<Integer> reach = new Property<Integer>(PROPERTY_REACH, 5, Integer.class);
	private Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.WHITE, Color.class);
	private Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	private Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, 0, Integer.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Integer r) {
			r = r % 4;
			if (rotations.getValue().equals(r)) return;
			cachedImage = null;
			super.setValue(r);
		}
	};
	private Property<Boolean> showReach = new Property<Boolean>(PROPERTY_SHOWREACH, false, Boolean.class);
	private Property<Boolean> reachWeapon = new Property<Boolean>(PROPERTY_REACHWEAPON, false, Boolean.class);
	private Property<String> label = new Property<String>(PROPERTY_LABEL, "", String.class);
	private Property<Integer> maxHPs = new Property<Integer>(PROPERTY_MAX_HPS, null, Integer.class);
	private Property<Integer> currentHPs = new Property<Integer>(PROPERTY_CURRENT_HPS, null, Integer.class);
	@SuppressWarnings("unused")
	private Property<String> webLabel = new Property<String>(PROPERTY_WEB_LABEL, false, "", String.class);

	private transient BufferedImage sourceImage = null;
	private transient Image cachedImage = null;
	private transient Dimension cachedSize;

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	long lastPaintTime = 0;

	@Override
	public MeasurementLog getPaintTiming() {
		MeasurementLog m = new MeasurementLog("Token (" + label + ")", id);
		m.total = lastPaintTime;
		return m;
	}

	@Override
	public void paint(Graphics2D g) {
		lastPaintTime = 0;
		long startTime = System.nanoTime();

		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
		g.translate(o.getX(), o.getY());

		int space = this.space.getValue();
		if (space < 10) space = 10;	// TODO need to be able to draw sub-Small tokens slightly smaller

		if (showReach.getValue()) paintReach(g);

		float arcWidth = canvas.getColumnWidth() * space / 30;
		float arcHeight = canvas.getRowHeight() * space / 30;
		int cells = space / 10;
		Point tl = canvas.convertGridCoordsToDisplay(location.getValue());
		Point br = canvas.convertGridCoordsToDisplay((int) getX() + cells, (int) getY() + cells);
		BasicStroke stroke = getThickStroke();
		float inset = stroke.getLineWidth() / 2;

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

		CreatureStatus status = getStatus();

		AffineTransform t = g.getTransform();
		g.rotate(Math.toRadians(rotations.getValue() * 90), (br.x + tl.x) / 2, (br.y + tl.y) / 2);

		// paint background:
		Shape s = new RoundRectangle2D.Float(tl.x + inset, tl.y + inset, br.x - tl.x - inset * 2, br.y - tl.y - inset * 2, arcWidth, arcHeight);
		if (statusDisplay.getValue() == StatusDisplay.BACKGROUND && status != null) {
			g.setColor(status.getColor());
		} else {
			g.setColor(color.getValue());
		}
		g.fill(s);

		// paint border:
		if (selected) {
			g.setColor(Color.BLUE);
		} else if (statusDisplay.getValue() == StatusDisplay.BORDER && status != null) {
			g.setColor(status.getColor());
		} else {
			g.setColor(Color.BLACK);
		}
		Stroke oldStroke = g.getStroke();
		g.setStroke(getThickStroke());
		g.draw(s);
		g.setStroke(oldStroke);

		//			g.setColor(Color.BLACK);
		//			Stroke oldStroke = g.getStroke();
		//			g.setStroke(getThickStroke());
		//			Shape s = new RoundRectangle2D.Float(tl.x+inset, tl.y+inset, br.x-tl.x-inset*2, br.y-tl.y-inset*2,arcWidth,arcHeight);
		//			g.draw(s);
		//			g.setStroke(oldStroke);
		//
		//			g.setColor(color);
		//			s = new RoundRectangle2D.Float(tl.x+2, tl.y+2, br.x-tl.x-inset*2, br.y-tl.y-inset*2,arcWidth,arcHeight);
		//			g.fill(s);

		Shape oldClip = g.getClip();
		RoundRectangle2D clip = new RoundRectangle2D.Float(tl.x + stroke.getLineWidth(), tl.y + stroke.getLineWidth(), br.x - tl.x - stroke.getLineWidth() * 2,
				br.y - tl.y - stroke.getLineWidth() * 2, arcWidth,
				arcHeight);
		g.clip(clip);

		int labelHeight = 0;
		if (getLabel().length() > 0) {
			Font f = g.getFont();
			float newSize = canvas.getRowHeight() / 4;
			if (newSize < 8.0f) newSize = 8.0f;
			g.setFont(f.deriveFont(newSize));
			FontMetrics metrics = g.getFontMetrics();
			Rectangle2D bounds = metrics.getStringBounds(getLabel(), g);
			labelHeight = (int) bounds.getHeight();
			double xPos = clip.getBounds2D().getCenterX() - bounds.getWidth() / 2;
			if (xPos < clip.getBounds2D().getX()) xPos = clip.getBounds2D().getX();
			double yPos = clip.getBounds2D().getMaxY() - metrics.getDescent();
			g.drawString(getLabel(), (float) xPos, (float) yPos);
		}

		if (sourceImage != null) {
			Rectangle2D bounds = clip.getBounds2D();
			bounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight() - labelHeight);
			if (bounds.getHeight() > 0f) {
				resizeImage(bounds);
				if (cachedImage != null) {
					g.drawImage(cachedImage, (int) (bounds.getX() + (bounds.getWidth() - cachedSize.width) / 2),
							(int) (bounds.getY() + (bounds.getHeight() - cachedSize.height) / 2), null);
				}
			}
		}

		g.setClip(oldClip);

		// paint status spot:
		if (statusDisplay.getValue() == StatusDisplay.SPOT && status != null
				&& statusType.getValue() != StatusType.EXACT) {
			g.setColor(status.getColor());
			int w = canvas.getColumnWidth() * cells / 6;
			int h = canvas.getRowHeight() * cells / 6;
			g.fillOval((int) (tl.x + inset + w / 2), (int) (tl.y + inset + h / 2), w, h);
		}

		g.setTransform(t);
		g.setComposite(c);
		g.translate(-o.getX(), -o.getY());

		lastPaintTime = (System.nanoTime() - startTime) / 1000;
	}

	// returns the absolute origin of this element's coordinate system. i.e. the absolute coordinates of this element if it's location were set to (0,0)
	public Point2D getElementOrigin() {
		return canvas.getElementOrigin(this);
	}

	private CreatureStatus getStatus() {
		if (maxHPs.getValue() == null || currentHPs.getValue() == null) return null;
		return statusType.getValue().getStatus(maxHPs.getValue(), currentHPs.getValue());
	}

	public String getStatusDescription() {
		StringBuilder s = new StringBuilder();

		if (statusType.getValue() == StatusType.EXACT) {
			if (maxHPs.getValue() != null || currentHPs.getValue() != null) {
				s.append("(");
				if (currentHPs.getValue() != null) {
					s.append(currentHPs.getValue());
					if (maxHPs.getValue() != null) s.append("/");
				} else {
					s.append("max ");
				}
				if (maxHPs.getValue() != null) {
					s.append(maxHPs.getValue());
				}
				s.append(" hps)");
			}
		} else {
			CreatureStatus status = getStatus();
			if (status != null) s.append("(").append(status).append(")");
		}

		return s.toString();
	}

	private String getLabel() {
		String s = "";

		if (label.getValue() != null && label.getValue().length() > 0) {
			s = label.getValue();
			if (statusDisplay.getValue() == StatusDisplay.LABEL) {
				s += " " + getStatusDescription();
			}
		}
		return s;
	}

	private void paintReach(Graphics2D g) {
		int reach = this.reach.getValue() / 5;
		int space = getSpace();
		Area area;
		if (!reachWeapon.getValue()) {
			area = getReach(reach, space);
		} else {
			area = getReach(reach * 2, space);
			area.subtract(getReach(reach, space));
		}
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() / 2));
		g.setColor(Color.RED);
		g.fill(area);
		g.setComposite(c);
	}

// returns the space is grid units
	int getSpace() {
		int space = this.space.getValue() / 10;
		if (space < 1) space = 1;
		return space;
	}

	private Area getReach(int reach, int space) {
		int x = (int) getX();
		int y = (int) getY();
		Area area = new Area();
		area.add(getRectangularArea(x - reach, y, x + reach + space, y + space));
		area.add(getRectangularArea(x, y - reach, x + space, y + space + reach));
		if (reach > 0) {
			area.add(getQuadrant(x, y, -1, -1, reach));
			area.add(getQuadrant(x + space, y, 1, -1, reach));
			area.add(getQuadrant(x, y + space, -1, 1, reach));
			area.add(getQuadrant(x + space, y + space, 1, 1, reach));
		}
		return area;
	}

	private Area getQuadrant(int x, int y, int xdir, int ydir, int radius) {
		Area area = new Area();

		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (isAffected(radius, i, j)) {
					int gridx = xdir * i + x;
					int gridy = ydir * j + y;
					area.add(getRectangularArea(gridx, gridy, gridx + xdir, gridy + ydir));
				}
			}
		}
		return area;
	}

	private boolean isAffected(int radius, int i, int j) {
		// measure distance from (0, 0) to each corner of this cell
		// if all four corners are within the radius then the cell is affected
		// note: only need to test the bottom right corner - if that is in the radius then the other corners must be
		int dist = i + 1 + j + 1 - (Math.min(i + 1, j + 1) - 1) / 2;	// the subtracted term is half the number of diagonals
		if (dist <= radius + 1) return true;
		if (radius == 2 && i == 1 && j == 1) return true;
		return false;
	}

// if the rectangle has any negative dimensions it will be modified to make those dimensions positive
	private Area getRectangularArea(int x1, int y1, int x2, int y2) {
		Point p1 = canvas.convertGridCoordsToDisplay(x1, y1, null);
		Point p2 = canvas.convertGridCoordsToDisplay(x2, y2, null);
		Rectangle rect = new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
		if (rect.width < 0) {
			rect.width = -rect.width;
			rect.x -= rect.width;
		}
		if (rect.height < 0) {
			rect.height = -rect.height;
			rect.y -= rect.height;
		}
		return new Area(rect);
	}

	private void resizeImage(Rectangle2D bounds) {
		Dimension b = new Dimension((int) bounds.getWidth(), (int) bounds.getHeight());
		int scaledWidth = (int) (sourceImage.getWidth() * b.getHeight() / sourceImage.getHeight());	// width if we scale to fit the height
		int scaledHeight = (int) (sourceImage.getHeight() * b.getWidth() / sourceImage.getWidth());	// // height if we scale to fit the width
		if (scaledWidth == 0 || scaledHeight == 0) return;
		//System.out.println("scaledWidth = "+scaledWidth+", scaledHeight = "+scaledHeight);
		if (scaledWidth <= bounds.getWidth()) {
			// scaledWidth fits so use (scaledWidth, bounds.getHeight())
			if (cachedImage == null || cachedSize.getWidth() > b.width || cachedSize.getHeight() > b.height || cachedSize.getHeight() < b.height - 2) {
				cachedImage = sourceImage.getScaledInstance(scaledWidth, b.height, Image.SCALE_SMOOTH);
				cachedSize = new Dimension(scaledWidth, b.height);
				//System.out.println("Resized to "+cachedSize+" to fit "+bounds);
			}
		} else {
			// use (bounds.getWidth(), scaledHeight)
			if (cachedImage == null || cachedSize.getWidth() > b.width || cachedSize.getWidth() < b.width - 2 || cachedSize.getHeight() > b.height) {
				cachedImage = sourceImage.getScaledInstance(b.width, scaledHeight, Image.SCALE_SMOOTH);
				cachedSize = new Dimension(b.width, scaledHeight);
				//System.out.println("Resized to "+cachedSize+" to fit "+bounds);
			}
		}
	}

	private BasicStroke getThickStroke() {
		if (canvas.getColumnWidth() < 40) return new BasicStroke(4);
		return new BasicStroke(6);
	}

//
//	protected void paintRaisedBevel(Graphics2D g, int x, int y, int width, int height, int outer, int inner)  {
//		Color oldColor = g.getColor();
//		int h = height;
//		int w = width;
//
//		g.translate(x, y);
//
//		int total = outer + inner;
//		g.setColor(oldColor.brighter().brighter());
//		for (int i = 0; i < outer; i++) {
//			g.drawLine(i, i, i, h-2-i);		// left
//			g.drawLine(1+i, i, w-2-i, i);	// top
//		}
//
//		g.setColor(oldColor.brighter());
//		for (int i = outer; i < total; i++) {
//			g.drawLine(i, i, i, h-2-i);		// left
//			g.drawLine(1+i, i, w-2-i, i);	// top
//		}
//
//		g.setColor(oldColor.darker().darker());
//		for (int i = 0; i < outer; i++) {
//			g.drawLine(i, h-1-i, w-1-i, h-1-i);
//			g.drawLine(w-1-i, i, w-1-i, h-2-i);
//		}
//
//		g.setColor(oldColor.darker());
//		for (int i = outer; i < total; i++) {
//			g.drawLine(i, h-1-i, w-1-i, h-1-i);
//			g.drawLine(w-1-i, i, w-1-i, h-2-i);
//		}
//
//		g.translate(-x, -y);
//		g.setColor(oldColor);
//	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Token (" + getID() + ")";
		return "Token (" + label + ")";
	}

	public void setImage(File f) {
		if (f != null) {
			try {
				sourceImage = ImageIO.read(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			sourceImage = null;
		}
		canvas.repaint();
	}

	public void setImage(byte[] bytes) {
		if (bytes != null) {
			try {
				ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
				sourceImage = ImageIO.read(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			sourceImage = null;
		}
		canvas.repaint();
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_IMAGE)) {
			setImage((byte[]) value);
		} else if (property.equals(PROPERTY_X)) {
			if (value instanceof Integer) {
				setX(((Integer) value).doubleValue());
			} else {
				setX((Double) value);
			}
		} else if (property.equals(PROPERTY_Y)) {
			if (value instanceof Integer) {
				setY(((Integer) value).doubleValue());
			} else {
				setY((Double) value);
			}
		} else {
			super.setProperty(property, value);
		}
	}
}

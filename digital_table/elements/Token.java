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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import digital_table.server.MapCanvas.Order;

public class Token extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_X = "x";	// int
	public final static String PROPERTY_Y = "y";	// int
	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_SIZE = "size";		// Size
	public final static String PROPERTY_LABEL = "label";
	public final static String PROPERTY_IMAGE = "image";	// byte[]
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_SHOWREACH = "show_reach";	// boolean
	public final static String PROPERTY_REACHWEAPON = "reach_weapon";	// boolean

	// TODO combine with gamesystem.Size
	public enum Size {
		FINE(1,0,"Fine"),
		DIMINUTIVE(2,0,"Diminutive"),
		TINY(5,0,"Tiny"),
		SMALL(10,5,"Small"),
		MEDIUM(10,5,"Medium"),
		LARGE_LONG(20,5,"Large (long)"),
		LARGE_TALL(20,10,"Large (tall)"),
		HUGE_LONG(30,10, "Huge (long)"),
		HUGE_TALL(30,15,"Huge (tall)"),
		GARGANTUAN_LONG(40,15, "Gargantuan (long)"),
		GARGANTUAN_TALL(40,20, "Gargantuan (tall)"),
		COLOSSAL_LONG(60,20, "Colossal (long)"),
		COLOSSAL_TALL(60,30, "Colossal (tall)");
		
		public String toString() {return description;}
		
		public static Size getSize(String d) {
			for (Size s : values()) {
				if (s.toString().equals(d)) return s;
			}
			return null;		// TODO should throw exception
		}
		
		public int getSpace() {return space;}	// space in 1/2ft units
		public double getSpaceFeet() {return (double)space/2;}
		public int getReach() {return reach;}
		
		Size(int space, int reach, String desc) {
			this.space = space;
			this.reach = reach;
			description = desc;
		}
		
		private final int space;	// in 1/2ft units
		private final int reach;	// in feet
		private String description;
	};

	Property<Size> size = new Property<Size>(PROPERTY_SIZE, Size.MEDIUM, Size.class);
	Property<Integer> x = new Property<Integer>(PROPERTY_X, 5, Integer.class);		// grid coordinate of left edge
	Property<Integer> y = new Property<Integer>(PROPERTY_Y, 8, Integer.class);		// grid coordinate of left edge
	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.WHITE, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, 0, Integer.class) {
		private static final long serialVersionUID = 1L;

		public void setValue(Integer r) {
			r = r % 4;
			if (rotations.getValue().equals(r)) return;
			cachedImage = null;
			super.setValue(r);
		}
	};
	Property<Boolean> showReach = new Property<Boolean>(PROPERTY_SHOWREACH, false, Boolean.class);
	Property<Boolean> reachWeapon = new Property<Boolean>(PROPERTY_REACHWEAPON, false, Boolean.class);
	Property<String> label = new Property<String>(PROPERTY_LABEL, "", String.class);

	protected transient BufferedImage sourceImage = null;
	protected transient Image cachedImage = null;
	protected transient Dimension cachedSize;

	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}
	
	public void paint(Graphics2D g) {
		if (canvas == null || !visible.getValue()) return;

		int space = size.getValue().getSpace();
		if (space < 10) space = 10;	// TODO need to be able to draw sub-Small tokens slightly smaller

		if (showReach.getValue()) paintReach(g);
		
		float arcWidth = canvas.getColumnWidth()*space/30;
		float arcHeight = canvas.getRowHeight()*space/30;
		int cells = space / 10;
		Point tl = canvas.getDisplayCoordinates(x.getValue(), y.getValue());
		Point br = canvas.getDisplayCoordinates(x.getValue()+cells, y.getValue()+cells);
		BasicStroke stroke = getThickStroke();
		float inset = stroke.getLineWidth()/2;

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));

		Shape s = new RoundRectangle2D.Float(tl.x+inset, tl.y+inset, br.x-tl.x-inset*2, br.y-tl.y-inset*2,arcWidth,arcHeight);
		g.setColor(color.getValue());
		g.fill(s);

		g.setColor(Color.BLACK);
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
		Shape clip = new RoundRectangle2D.Float(tl.x+stroke.getLineWidth(), tl.y+stroke.getLineWidth(), br.x-tl.x-stroke.getLineWidth()*2, br.y-tl.y-stroke.getLineWidth()*2,arcWidth,arcHeight);
		g.setClip(clip);

		int labelHeight = 0;
		if (label.getValue() != null && label.getValue().length() > 0) {
			Font f = g.getFont();
			float newSize = canvas.getRowHeight()/4;
			if (newSize < 8.0f) newSize = 8.0f;
			g.setFont(f.deriveFont(newSize));
			FontMetrics metrics = g.getFontMetrics();
			Rectangle2D bounds = metrics.getStringBounds(label.getValue(), g);
			labelHeight = (int)bounds.getHeight();
			double xPos = clip.getBounds2D().getCenterX() - bounds.getWidth()/2;
			if (xPos < clip.getBounds2D().getX()) xPos = clip.getBounds2D().getX();
			double yPos = clip.getBounds2D().getMaxY() - metrics.getDescent();
			g.drawString(label.getValue(), (float)xPos, (float)yPos);
		}
		
		if (sourceImage != null) {
			Rectangle2D bounds = clip.getBounds2D();
			bounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight() - labelHeight);
			if (bounds.getHeight() > 0f) {
				resizeImage(bounds);
				g.drawImage(cachedImage, (int)(bounds.getX()+(bounds.getWidth()-cachedSize.width)/2),
						(int)(bounds.getY()+(bounds.getHeight()-cachedSize.height)/2), null);
			}
		}

		g.setClip(oldClip);
		g.setComposite(c);
	}

	protected void paintReach(Graphics2D g) {
		int reach = size.getValue().getReach()/5;
		int space = size.getValue().getSpace()/10;
		if (space < 1) space = 1;
		Area area;
		if (!reachWeapon.getValue()) {
			area = getReach(reach, space);
		} else {
			area = getReach(reach*2, space);
			area.subtract(getReach(reach, space));
		}
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()/2));
		g.setColor(Color.RED);
		g.fill(area);
		g.setComposite(c);
	}

	protected Area getReach(int reach, int space) {
		int x = this.x.getValue();
		int y = this.y.getValue();
		Area area = new Area();
		area.add(getRectangularArea(x-reach, y, x+reach+space, y+space));
		area.add(getRectangularArea(x, y-reach, x+space, y+space+reach));
		if (reach > 0) {
			area.add(getQuadrant(x, y, -1, -1, reach));
			area.add(getQuadrant(x+space, y, 1, -1, reach));
			area.add(getQuadrant(x, y+space, -1, 1, reach));
			area.add(getQuadrant(x+space, y+space, 1, 1, reach));
		}
		return area;
	}
	
	protected Area getQuadrant(int x, int y, int xdir, int ydir, int radius) {
		Area area = new Area();

		boolean[][] affected = new boolean[radius][radius];

		// calculate the affected cells
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				// measure distance from (0, 0) to each corner of this cell
				// if all four corners are within the radius then the cell is affected
				// note: only need to test the bottom right corner - if that is in the radius then the other corners must be 
				int dist = i+1 + j+1 - (Math.min(i+1, j+1)-1)/2;	// the subtracted term is half the number of diagonals
				if (dist <= radius+1) affected[i][j] = true;
				if (radius == 2 && i == 1 && j == 1) affected[i][j] = true;
			}
		}
	
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (affected[i][j]) {
					int gridx = xdir*i+x;
					int gridy = ydir*j+y;
					area.add(getRectangularArea(gridx, gridy, gridx+xdir, gridy+ydir));
				}
			}
		}
		return area;
	}

	// if the rectangle has any negative dimensions it will be modified to make those dimensions positive
	protected Area getRectangularArea(int x1, int y1, int x2, int y2) {
		Point p1 = canvas.getDisplayCoordinates(x1, y1, null);
		Point p2 = canvas.getDisplayCoordinates(x2, y2, null);
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

	// TODO do scaling at the same time as rotate
	protected void resizeImage(Rectangle2D bounds) {
		AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
		Point p = new Point(sourceImage.getWidth(),sourceImage.getHeight());
		t.transform(p,p);	// transform to get new dimensions

		BufferedImage rotatedImage = new BufferedImage(Math.abs(p.x), Math.abs(p.y), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)rotatedImage.getGraphics();
		g2d.rotate(Math.toRadians(rotations.getValue()*90), rotatedImage.getWidth() / 2, rotatedImage.getHeight() / 2);
		g2d.translate((rotatedImage.getWidth() - sourceImage.getWidth()) / 2, (rotatedImage.getHeight() - sourceImage.getHeight()) / 2);
		g2d.drawImage(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
		g2d.dispose();

		Dimension b = new Dimension((int)bounds.getWidth(), (int)bounds.getHeight());
		int scaledWidth = (int)(rotatedImage.getWidth() * b.getHeight() / rotatedImage.getHeight());	// width if we scale to fit the height
		int scaledHeight = (int)(rotatedImage.getHeight() * b.getWidth() / rotatedImage.getWidth());	// // height if we scale to fit the width
		//System.out.println("scaledWidth = "+scaledWidth+", scaledHeight = "+scaledHeight);
		if (scaledWidth <= bounds.getWidth()) {
			// scaledWidth fits so use (scaledWidth, bounds.getHeight())
			if (cachedImage == null || cachedSize.getWidth() > b.width || cachedSize.getHeight() > b.height || cachedSize.getHeight() < b.height-2) {
				cachedImage = rotatedImage.getScaledInstance(scaledWidth, b.height, Image.SCALE_SMOOTH);
				cachedSize = new Dimension(scaledWidth, b.height);
				//System.out.println("Resized to "+cachedSize+" to fit "+bounds);
			}
		} else {
			// use (bounds.getWidth(), scaledHeight)
			if (cachedImage == null || cachedSize.getWidth() > b.width || cachedSize.getWidth() < b.width-2 || cachedSize.getHeight() > b.height) {
				cachedImage = rotatedImage.getScaledInstance(b.width, scaledHeight, Image.SCALE_SMOOTH);
				cachedSize = new Dimension(b.width, scaledHeight);
				//System.out.println("Resized to "+cachedSize+" to fit "+bounds);
			}
		}
	}
	
	protected BasicStroke getThickStroke() {
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

	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Token ("+getID()+")";
		return "Token ("+label+")";
	}
	
	public void setImage(File f) {
		try {
			sourceImage = ImageIO.read(f);
			canvas.repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setImage(byte[] bytes) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			sourceImage = ImageIO.read(stream);
			canvas.repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_IMAGE)) {
			setImage((byte[])value);
		} else {
			super.setProperty(property, value);
		}
	}
}

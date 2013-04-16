package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
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

	// TODO combine with gamesystem.Size
	public enum Size {
		FINE(1,0,"Fine"),
		DIMINUTIVE(2,0,"Diminutive"),
		TINY(5,0,"Tiny"),
		SMALL(10,5,"Small"),
		MEDIUM(10,5,"Medium"),
		LARGE_TALL(20,10,"Large (tall)"),
		LARGE_LONG(20,5,"Large (long)"),
		HUGE_TALL(30,15,"Huge (tall)"),
		HUGE_LONG(30,10, "Huge (long)"),
		GARGANTUAN_TALL(40,20, "Gargantuan (tall)"),
		GARGANTUAN_LONG(40,15, "Gargantuan (long)"),
		COLOSSAL_TALL(60,30, "Colossal (tall)"),
		COLOSSAL_LONG(60,20, "Colossal (long)");
		
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

	Size size = Size.LARGE_TALL;
	BufferedImage image;
	int x = 5;	// grid coordinate of left edge 
	int y = 8; 	// grid coordinate of top edge
	String label;
	Color color = Color.WHITE;
	float alpha = 1.0f;
	int rotations = 0;

	protected transient BufferedImage sourceImage = null;
	protected transient Image cachedImage = null;
	protected transient Dimension cachedSize;

	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}
	
	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		int space = size.getSpace();
		if (space < 10) space = 10;	// TODO need to be able to draw sub-Small tokens slightly smaller

		float arcWidth = canvas.getColumnWidth()*space/30;
		float arcHeight = canvas.getRowHeight()*space/30;
		int cells = space / 10;
		Point tl = canvas.getDisplayCoordinates(x, y);
		Point br = canvas.getDisplayCoordinates(x+cells, y+cells);
		BasicStroke stroke = getThickStroke();
		float inset = stroke.getLineWidth()/2;

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		Shape s = new RoundRectangle2D.Float(tl.x+inset, tl.y+inset, br.x-tl.x-inset*2, br.y-tl.y-inset*2,arcWidth,arcHeight);
		g.setColor(color);
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
		
		if (sourceImage != null) {
			Shape oldClip = g.getClip();
			Shape clip = new RoundRectangle2D.Float(tl.x+stroke.getLineWidth(), tl.y+stroke.getLineWidth(), br.x-tl.x-stroke.getLineWidth()*2, br.y-tl.y-stroke.getLineWidth()*2,arcWidth,arcHeight);
			g.setClip(clip);
			Rectangle2D bounds = clip.getBounds2D();
			resizeImage(bounds);
			g.drawImage(cachedImage, (int)(bounds.getX()+(bounds.getWidth()-cachedSize.width)/2),
					(int)(bounds.getY()+(bounds.getHeight()-cachedSize.height)/2), null);
			g.setClip(oldClip);
		}

		g.setComposite(c);
	}

	// TODO do scaling at the same time as rotate
	protected void resizeImage(Rectangle2D bounds) {
		AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations);
		Point p = new Point(sourceImage.getWidth(),sourceImage.getHeight());
		t.transform(p,p);	// transform to get new dimensions

		BufferedImage rotatedImage = new BufferedImage(Math.abs(p.x), Math.abs(p.y), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)rotatedImage.getGraphics();
		g2d.rotate(Math.toRadians(rotations*90), rotatedImage.getWidth() / 2, rotatedImage.getHeight() / 2);
		g2d.translate((rotatedImage.getWidth() - sourceImage.getWidth()) / 2, (rotatedImage.getHeight() - sourceImage.getHeight()) / 2);
		g2d.drawImage(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
		g2d.dispose();

		Dimension b = new Dimension((int)bounds.getWidth()-1, (int)bounds.getHeight()-1);
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
		if (label == null || label.length() == 0) return "Token ("+getID()+")";
		return "Token ("+label+")";
	}
	
	public void setImage(File f) {
		try {
			sourceImage = ImageIO.read(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setImage(byte[] bytes) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			sourceImage = ImageIO.read(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Object getProperty(String property) {
		if (property.equals(PROPERTY_X)) {
			return getX();
		} else if (property.equals(PROPERTY_Y)) {
			return getY();
		} else if (property.equals(PROPERTY_COLOR)) {
			return getColor();
		} else if (property.equals(PROPERTY_ALPHA)) {
			return getAlpha();
		} else if (property.equals(PROPERTY_LABEL)) {
			return getLabel();
		} else if (property.equals(PROPERTY_SIZE)) {
			return getSize();
		} else if (property.equals(PROPERTY_ROTATIONS)) {
			return getRotations();
		} else {
			// throw exception?
			return null;
		}
	}

	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_X)) {
			setX((Integer)value);
		} else if (property.equals(PROPERTY_Y)) {
			setY((Integer)value);
		} else if (property.equals(PROPERTY_COLOR)) {
			setColor((Color)value);
		} else if (property.equals(PROPERTY_ALPHA)) {
			setAlpha((Float)value);
		} else if (property.equals(PROPERTY_LABEL)) {
			setLabel((String)value);
		} else if (property.equals(PROPERTY_SIZE)) {
			setSize((Size)value);
		} else if (property.equals(PROPERTY_IMAGE)) {
			setImage((byte[])value);
		} else if (property.equals(PROPERTY_ROTATIONS)) {
			setRotations((Integer)value);
		} else {
			// throw exception?
		}
	}

	public String getLabel() {
		return label == null ? "" : label;
	}
	
	public void setLabel(String l) {
		String old = label;
		label = l;
		pcs.firePropertyChange(PROPERTY_LABEL, old, label);
	}

	public int getX() {
		return x;
	}
	
	public void setX(int newX) {
		if (x == newX) return;
		int old = x;
		x = newX;
		pcs.firePropertyChange(PROPERTY_X, old, x);
		if (canvas != null) canvas.repaint();
	}

	public int getY() {
		return y;
	}
	
	public void setY(int newY) {
		if (y == newY) return;
		int old = y;
		y = newY;
		pcs.firePropertyChange(PROPERTY_Y, old, y);
		if (canvas != null) canvas.repaint();
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color c) {
		if (color.equals(c)) return;
		Color old = color;
		color = c;
		pcs.firePropertyChange(PROPERTY_COLOR, old, color);
		if (canvas != null) canvas.repaint();
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

	public Size getSize() {
		return size;
	}
	
	public void setSize(Size newSize) {
		if (size == newSize) return;
		Size old = size;
		size = newSize;
		pcs.firePropertyChange(PROPERTY_SIZE, old, size);
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
		cachedImage = null;
		pcs.firePropertyChange(PROPERTY_ROTATIONS, old, rotations);
		if (canvas != null) canvas.repaint();
	}
}

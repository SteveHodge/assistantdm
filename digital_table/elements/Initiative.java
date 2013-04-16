package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.MapCanvas.Order;

public class Initiative extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double
	public final static String PROPERTY_TEXT = "text";	// String
	public final static String PROPERTY_COLOR = "color";	// Color
	public static final String PROPERTY_BACKGROUND_COLOR = "background_color";	// Color

	double x, y;	// position in grid coordinate-space
	int rotations = 0;
	float alpha = 1.0f;
	float fontSize = 0.333f;	// font size in grid coordinate-space
	Color color = Color.BLACK;
	Color backgroundColor = Color.WHITE;
	String text = "";
	
	public Order getDefaultOrder() {
		return Order.TOP;
	}

	public void paint(Graphics2D g) {
		if (!visible) return;

		Font f = g.getFont();
		float newSize = canvas.getDisplayCoordinates(new Point2D.Float(fontSize,0)).x;
		g.setFont(f.deriveFont(newSize));

		List<String[]> text = getTable();

		// get the maximum width of the first cell in each line
		FontMetrics metrics = g.getFontMetrics();
		int col1Width = 0, col2Width = 0;
		for (String[] line : text) {
			if (metrics.stringWidth(line[0]) > col1Width) col1Width = metrics.stringWidth(line[0]);
			if (metrics.stringWidth(line[1]) > col2Width) col2Width = metrics.stringWidth(line[1]);
		}

		if (col1Width + col2Width > 0 ) {
			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			
			Point p = canvas.getDisplayCoordinates(new Point2D.Double(x, y));
			int x0 = p.x + 5;
			int x1 = p.x + 5 + col1Width + 10;
			int y = p.y + 5 + metrics.getAscent();

			int w = col1Width + col2Width + 20;
			int h = metrics.getHeight() * text.size() + 10;

			AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations);
			Point size = new Point(w, h);
			t.transform(size,size);	// transform to get new dimensions
			int newWidth = Math.abs(size.x);
			int newHeight = Math.abs(size.y);
			t = g.getTransform();
			g.rotate(Math.toRadians(rotations*90), p.x + newWidth/2, p.y + newHeight/2);
			g.translate((newWidth - w)/2, (newHeight - h)/2);

			g.setColor(backgroundColor);
			g.fillRect(p.x, p.y, w, h);
			g.setColor(color);
			for (String[] line : text) {
				g.drawString(line[0], x0, y);
				g.drawString(line[1], x1, y);
				y += metrics.getHeight();
			}

			g.setTransform(t);
			g.setComposite(c);
			g.setFont(f);
		}
	}

	protected List<String[]> getTable() {
		String[] lines = text.split("\\r?\\n|\\r");
		List<String[]> output = new ArrayList<String[]>();

		String[] outLine = new String[2];
		for (String line : lines) {
			String[] parts = line.split("=");
			if (parts.length < 2) continue;

			if (parts[0].equals("round")) {
				outLine[0] = "Round";
				outLine[1] = parts[1];
				output.add(outLine);
				outLine = new String[2];
				
			} else if (parts[0].equals("lastindex")) {
				
			} else if (parts[0].startsWith("fixedname")) {
				outLine[0] = parts[1];
				
			} else if (parts[0].startsWith("init")) {
				outLine[1] = parts[1];
				output.add(outLine);
				outLine = new String[2];
			}
		}
		return output;
	}

	public String toString() {
		return "Initiative ("+getID()+")";
	}
	
	public Object getProperty(String property) {
		if (property.equals(PROPERTY_ALPHA)) {
			return getAlpha();
		} else if (property.equals(PROPERTY_X)) {
			return getX();
		} else if (property.equals(PROPERTY_Y)) {
			return getY();
		} else if (property.equals(PROPERTY_ROTATIONS)) {
			return getRotations();
		} else if (property.equals(PROPERTY_TEXT)) {
			return getText();
		} else if (property.equals(PROPERTY_COLOR)) {
			return getColor();
		} else if (property.equals(PROPERTY_BACKGROUND_COLOR)) {
			return getBackgroundColor();
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
		} else if (property.equals(PROPERTY_ROTATIONS)) {
			setRotations((Integer)value);
		} else if (property.equals(PROPERTY_TEXT)) {
			setText((String)value);
		} else if (property.equals(PROPERTY_COLOR)) {
			setColor((Color)value);
		} else if (property.equals(PROPERTY_BACKGROUND_COLOR)) {
			setBackgroundColor((Color)value);
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

	public int getRotations() {
		return rotations;
	}
	
	public void setRotations(int r) {
		r = r % 4;
		if (rotations == r) return;
		int old = rotations;
		rotations = r;
		pcs.firePropertyChange(PROPERTY_ROTATIONS, old, rotations);
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
	
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(Color c) {
		if (backgroundColor.equals(c)) return;
		Color old = backgroundColor;
		backgroundColor = c;
		pcs.firePropertyChange(PROPERTY_BACKGROUND_COLOR, old, backgroundColor);
		if (canvas != null) canvas.repaint();
	}

	public void setText(String t) {
		String old = text;
		text = t;
		pcs.firePropertyChange(PROPERTY_TEXT, old, text);
		if (canvas != null) canvas.repaint();
	}

	public String getText() {
		return text;
	}
}

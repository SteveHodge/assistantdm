package digital_table.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import digital_table.server.MapCanvas.Order;

public class Callibrate extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_SHOW_BACKGROUND = "show_background";	// boolean

	private int left = 1;
	private int top = 1;
	private int bottom = 37;
	private int right = 31;
	private int size = 16;

	private Property<Boolean> showBackground = new Property<Boolean>(PROPERTY_SHOW_BACKGROUND, false, Boolean.class);

	@Override
	public Order getDefaultOrder() {
		return Order.TOP;
	}

	@Override
	public void paint(Graphics2D g, Point2D offset) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		if (!showBackground.getValue()) {
			g.setColor(Color.WHITE);
			g.fill(g.getClip());
		}

		g.setColor(Color.RED);
		Point2D p = canvas.getDisplayCoordinates(left, top);
		g.fillOval((int) p.getX() - size / 2, (int) p.getY() - size / 2, size, size);
		p = canvas.getDisplayCoordinates(left, bottom);
		g.fillOval((int) p.getX() - size / 2, (int) p.getY() - size / 2, size, size);
		g.setColor(Color.BLUE);
		p = canvas.getDisplayCoordinates(right, top);
		g.fillOval((int) p.getX() - size / 2, (int) p.getY() - size / 2, size, size);
		p = canvas.getDisplayCoordinates(right, bottom);
		g.fillOval((int) p.getX() - size / 2, (int) p.getY() - size / 2, size, size);
	}

	@Override
	public String toString() {
		return "Callibrate";
	}
}

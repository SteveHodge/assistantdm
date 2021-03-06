package digital_table.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class Calibrate extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_SHOW_BACKGROUND = "show_background";	// boolean

	private int left = 1;
	private int top = 1;
	private int bottom = 37;
	private int right = 31;
	private int size = 40;

	private Property<Boolean> showBackground = new Property<>(PROPERTY_SHOW_BACKGROUND, false, Boolean.class);

	@Override
	public Layer getDefaultLayer() {
		return Layer.INFORMATION;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		if (!showBackground.getValue()) {
			g.setColor(Color.WHITE);
			g.fill(g.getClip());
		}

		Point tl = canvas.convertCanvasCoordsToDisplay(left + canvas.getRemote().getXOffset(), top + canvas.getRemote().getYOffset());
		Point br = canvas.convertCanvasCoordsToDisplay(right + canvas.getRemote().getXOffset(), bottom + canvas.getRemote().getYOffset());
		g.setColor(Color.RED);
		g.fillOval(tl.x - size / 2, tl.y - size / 2, size, size);
		g.fillOval(tl.x - size / 2, br.y - size / 2, size, size);
		g.setColor(Color.BLUE);
		g.fillOval(br.x - size / 2, tl.y - size / 2, size, size);
		g.fillOval(br.x - size / 2, br.y - size / 2, size, size);
	}

	@Override
	public String getIDString() {
		return "Calibrate";
	}

	@Override
	public String toString() {
		return "Calibrate";
	}
}

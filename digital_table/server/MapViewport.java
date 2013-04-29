package digital_table.server;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;



/*
 * A viewport onto a MapCanvas. This is similar to a JViewport, but multiple MapViewports can be attached to
 * a single MapCanvas.
 */

@SuppressWarnings("serial")
public class MapViewport extends JPanel implements RepaintListener {
	MapCanvas view;
	int viewx;	// coordinates in the MapCanvas of the top left corner of the displayed section 
	int viewy;

	public MapViewport(MapCanvas v, int x, int y) {
		setLayout(new BorderLayout());
		view = v;
		viewx = x;
		viewy = y;
		view.addRepaintListener(this);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		//Rectangle bounds = getBounds();
		//Rectangle clip = g.getClipBounds();
		//System.out.println("Clip = "+clip);
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform t = g2d.getTransform();
		g2d.translate(-viewx, -viewy);
		view.paint(g2d);
		g2d.setTransform(t);
	}
}

package digital_table.controller;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import digital_table.elements.MapElement;

public interface MapElementMouseListener {
	public void mousePressed(MouseEvent e, Point2D gridloc);
	public void mouseReleased(MouseEvent e, Point2D gridloc);
	public void mouseClicked(MouseEvent e, Point2D gridloc);
	public void mouseDragged(MouseEvent e, Point2D gridloc);
	public MapElement getCoordElement();
}

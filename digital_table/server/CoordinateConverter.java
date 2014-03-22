package digital_table.server;

import java.awt.geom.Point2D;

/* defines a set of methods that convert one coordinate space to another.
 * this is used to get grid dimensions and locations from the remote display coordinate system.
 * on the remote system the MapCanvas can be used to perform the conversion.
 * on the local system a proxy for the remote MapCanvas performs the conversion (for performance reasons).
 * the difference between the getGridDimension and getGridCoordinates methods is that getGridCoordinates applies
 * any offset to the coordinates.
 */
public interface CoordinateConverter {
	// returns the precise size in grid units of the rectangle located at (0,0) with the specified width and height
	// (which are in the coordinate system of the remote display). note that this may give imprecise results due to
	// round in the coordinate conversions
	// TODO should return a Dimension2D
	public Point2D convertDisplayCoordsToGrid(int width, int height);

	/**
	 * Get the precise (potentially fractional) grid coordinates of the pixel (x,y) where the pixel coordinates
	 * are in the coordinate system of the display.
	 * 
	 * @param x
	 *            the pixel's x coordinate
	 * @param y
	 *            the pixel's y coordinate
	 * @return a Point2D.Double containing the grid coordinates
	 */
	public Point2D convertDisplayCoordsToCanvas(int x, int y);

	public int getXOffset();

	public int getYOffset();
}

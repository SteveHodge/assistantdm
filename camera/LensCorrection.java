package camera;

import java.awt.geom.Point2D;

class LensCorrection implements PointRemapper {
	double A, B, C, D;
	double r;	// radius of circle
	double centreX, centreY;

	/**
	 * 
	 * @param a
	 *            correction parameter a
	 * @param b
	 *            correction parameter b
	 * @param c
	 *            correction parameter c
	 * @param width
	 *            width of source image
	 * @param height
	 *            height of source image
	 */
	LensCorrection(double a, double b, double c, int width, int height) {
		A = a;
		B = b;
		C = c;
		D = 1.0 - a - b - c;

		r = Math.min(width, height) / 2.0;
		centreX = (width - 1) / 2.0;	// not sure why the -1
		centreY = (height - 1) / 2.0;	// not sure why the -1
	}

	/***
	 * updates a coordinate in the corrected image to the corresponding source coordinate
	 * 
	 * @param p
	 *            the corrected point which will be updated
	 * @return
	 *         the point p
	 */
	@Override
	public Point2D.Double transform(Point2D.Double p) {
		// cartesian coordinates of the destination point (relative to the centre of the image)
		double deltaX = (p.x - centreX) / r;
		double deltaY = (p.y - centreY) / r;

		// distance or radius of dst image
		double dstR = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

		// distance or radius of src image (with formula)
		double srcR = (((A * dstR + B) * dstR + C) * dstR + D) * dstR;

		// comparing old and new distance to get factor
		double factor = Math.abs(dstR / srcR);

		// coordinates in source image
		p.x = centreX + ((p.x - centreX) * factor);
		p.y = centreY + ((p.y - centreY) * factor);

		return p;
	}
}

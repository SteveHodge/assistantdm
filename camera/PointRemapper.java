package camera;

import java.awt.geom.Point2D;

interface PointRemapper {
	Point2D.Double transform(Point2D.Double p);
}

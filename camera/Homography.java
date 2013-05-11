package camera;

import java.awt.geom.Point2D;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class Homography implements PointRemapper {
	private RealMatrix H;

	static Homography createHomographySVD(Point2D[] world, Point2D[] points) {
		Homography h = new Homography();
		h.H = getHomographySVD(world, points);
		return h;
	}

	static Homography createHomographyPI(Point2D[] world, Point2D[] points) {
		Homography h = new Homography();
		h.H = getHomographyPI(world, points);
		return h;
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
		RealVector s = new ArrayRealVector(new double[] { p.x, p.y, 1 }, false);
		RealVector t = H.operate(s);
		p.x = t.getEntry(0) / t.getEntry(2);
		p.y = t.getEntry(1) / t.getEntry(2);
		return p;
	}

	// uses SVD directly
	public static RealMatrix getHomographySVD(Point2D[] world, Point2D[] points) {
		double[][] coeffs = new double[8][8];
		double[] consts = new double[8];
		int j = 0;
		for (int i = 0; i < 4; i++) {
			double x = points[i].getX();
			double y = points[i].getY();
			double X = world[i].getX();
			double Y = world[i].getY();
			coeffs[j] = new double[] { x, y, 1, 0, 0, 0, -X * x, -X * y };
			consts[j++] = X;
			coeffs[j] = new double[] { 0, 0, 0, x, y, 1, -Y * x, -Y * y };
			consts[j++] = Y;
		}
		RealMatrix B = new Array2DRowRealMatrix(coeffs, false);
		RealVector D = new ArrayRealVector(consts, false);
		DecompositionSolver solver = new SingularValueDecomposition(B).getSolver();
		RealVector l = solver.solve(D);

		RealMatrix A = new Array2DRowRealMatrix(new double[][] {
				{ l.getEntry(0), l.getEntry(1), l.getEntry(2) },
				{ l.getEntry(3), l.getEntry(4), l.getEntry(5) },
				{ l.getEntry(6), l.getEntry(7), 1 }
		}, false);

		return A;
	}

	// uses the pseudo-inverse as with the example
	public static RealMatrix getHomographyPI(Point2D[] world, Point2D[] points) {
		double[][] coeffs = new double[8][8];
		double[] consts = new double[8];
		int j = 0;
		for (int i = 0; i < 4; i++) {
			double x = points[i].getX();
			double y = points[i].getY();
			double X = world[i].getX();
			double Y = world[i].getY();
			coeffs[j] = new double[] { x, y, 1, 0, 0, 0, -X * x, -X * y };
			consts[j++] = X;
			coeffs[j] = new double[] { 0, 0, 0, x, y, 1, -Y * x, -Y * y };
			consts[j++] = Y;
		}
		RealMatrix B = new Array2DRowRealMatrix(coeffs, false);
		RealVector D = new ArrayRealVector(consts, false);

		RealMatrix Bt = B.transpose();
		RealMatrix temp = Bt.multiply(B);
		RealMatrix invTemp = new SingularValueDecomposition(temp).getSolver().getInverse();
		RealVector l = invTemp.multiply(Bt).operate(D);

		RealMatrix A = new Array2DRowRealMatrix(new double[][] {
				{ l.getEntry(0), l.getEntry(1), l.getEntry(2) },
				{ l.getEntry(3), l.getEntry(4), l.getEntry(5) },
				{ l.getEntry(6), l.getEntry(7), 1 }
		}, false);

		return A;
	}

}

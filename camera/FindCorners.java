package camera;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import camera.AutoCalibrate.Region;

public class FindCorners {
	static String filename = "D:\\Work\\_assisstants\\calibrate\\20140411.png";
	static String regionsname = "D:\\Work\\_assisstants\\calibrate\\regions.png";
	static String outname = "D:\\Work\\_assisstants\\calibrate\\output.png";

	public static void main(String[] args) {
		BufferedImage source = null;

		try {
			source = ImageIO.read(new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		Stopwatch watch = new Stopwatch();

		watch.start();
		List<Region> regions = Region.findRegions(source);
		watch.stop();
		System.out.println("Total regions = " + regions.size());
		Region.filterRegions(regions);
		System.out.println("findRegions took " + watch.getElapsed() + "ms");
		Region.writeOutput(source, regions, regionsname);

		for (Region r : regions) {
			System.out.println(r);
		}

		Point[] points = AutoCalibrate.getPoints(regions);
		if (points != null) {
			for (Point p : points) {
				System.out.println(p);
			}
		}

		// calculate world-coordinates for the control points based on the distance from point 0 to point 1 (width)
		// and from point 0 to point 3 (height):
		double w = points[0].distance(points[1]);
		double h = points[0].distance(points[3]);
		double x = w * 2 / 36;
		double y = h / 30;
		final Point2D[] worldPoint = new Point2D.Double[4];
		worldPoint[0] = new Point2D.Double(x, y);
		worldPoint[1] = new Point2D.Double(x + w, y);
		worldPoint[2] = new Point2D.Double(x + w, y + h);
		worldPoint[3] = new Point2D.Double(x, y + h);

		watch.start();
		Homography H = Homography.createHomographySVD(points, worldPoint);
		watch.stop();
		System.out.println("createHomographySVD took " + watch.getElapsed() + "ms");

		int remappedWidth = (int) (w * 39 / 36);
		int remappedHeight = (int) (h * 32 / 30);

		List<PointRemapper> corrections = new ArrayList<>();
		corrections.add(H);

		BufferedImage image = null;
		long total = 0;
		for (int i = 0; i < 50; i++) {
			watch.start();
			image = PixelInterpolator.getImage(corrections, source, remappedWidth, remappedHeight);
			watch.stop();
			System.out.println("PixelInterpolator.getImage took " + watch.getElapsed() + "ms");
			total += watch.getElapsed();
		}
		System.out.println("Average = " + (total / 50.0));

		try {
			ImageIO.write(image, "png", new File(outname));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class Stopwatch {
		long startMillis;
		long elapsed;

		public Stopwatch() {
		}

		public void start() {
			reset();
		}

		public void reset() {
			startMillis = System.currentTimeMillis();
			elapsed = 0;
		}

		public void stop() {
			elapsed = System.currentTimeMillis() - startMillis;
		}

		public long getElapsed() {
			return elapsed;
		}
	}
}
package camera;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

public class AutoCalibrate {
	static String outputFilename = "callibrate.png";
	static String regionsFilename = "callibrateRegions.png";

	public static Point[] calibrate(BufferedImage source) {
		try {
			ImageIO.write(source, "png", new File(outputFilename));
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Region> regions = Region.findRegions(source);
		Region.writeOutput(source, regions, regionsFilename);
		return getPoints(regions);
	}

	public static Point[] getPoints(List<Region> regions) {
		Point[] points = null;

		int red = 0, blue = 0;
		for (Region r : regions) {
			if (r.getColour() == Region.COLOUR_BLUE) blue++;
			if (r.getColour() == Region.COLOUR_RED) red++;
		}
		if (blue != 2 || red != 2) {
			System.out.println("Incorrect numbers of regions found: " + red + " red, " + blue + " blue");
		} else {
			// calculate centre:
			double totalX = 0, totalY = 0;
			for (Region r : regions) {
				totalX += r.getCenterX();
				totalY += r.getCenterY();
			}
			final double centreX = totalX / 4;
			final double centreY = totalY / 4;

			// calculate angle from each region to the centre and sort from smallest to largest
//			for (Region r : regions) {
//				System.out.println(r);
//				double theta = Math.atan2(r.getCenterY() - centreY, r.getCenterX() - centreX);
//				System.out.println("angle = " + theta);
//			}
			Collections.sort(regions, (a, b) -> {
				double thetaA = Math.atan2(a.getCenterY() - centreY, a.getCenterX() - centreX);
				double thetaB = Math.atan2(b.getCenterY() - centreY, b.getCenterX() - centreX);
				if (thetaA < thetaB)
					return -1;
				else if (thetaA > thetaB)
					return 1;
				return 0;
			});

			points = new Point[4];
			int i = 0;
			for (Region r : regions) {
//				System.out.println(r);
//				double theta = Math.atan2(r.getCenterY() - centreY, r.getCenterX() - centreX);
//				System.out.println("angle = " + theta);
				points[i++] = new Point(r.getCenterX(), r.getCenterY());
			}
		}
		return points;
	}

	public static class Region {
		public static final int COLOUR_RED = 1;
		public static final int COLOUR_BLUE = 2;
		private int colour;
		private int pixels;
		private long totalX, totalY;
		private long totalR, totalG, totalB;
		private List<Point> points = new ArrayList<>();

		static class Segment {
			int colour;
			int startX, endX;
			int y;
			long totalR, totalG, totalB;
			Region region;

			@Override
			public String toString() {
				StringBuilder s = new StringBuilder();
				s.append("segment (").append(startX).append(" to ").append(endX);
				s.append(" on line ").append(y).append(" colour ");
				if (colour == COLOUR_RED) s.append("red");
				if (colour == COLOUR_BLUE) s.append("blue");
				s.append(")");
				return s.toString();
			}

			public String toShortString() {
				StringBuilder s = new StringBuilder();
				s.append("segment (").append(startX).append("-").append(endX);
				if (region != null) {
					s.append(" ").append(region.toShortString());
				}
				s.append(")");
				return s.toString();
			}
		}

		public static List<Region> findRegions(BufferedImage source) {
			List<Segment> previous = new ArrayList<>();
			List<Region> regions = new ArrayList<>();

			PixelIterator pixelIter = PixelIterator.createPixelIterator(source);

			int width = source.getWidth();
			int height = source.getHeight();

			for (int y = 0; y < height; y++) {
				List<Segment> segments = new ArrayList<>();

				// scan each line of the source image, recording segments of interest
				Segment segment = null;
				for (int x = 0; x < width; x++) {
					pixelIter.next();
					int r = pixelIter.getRed();
					int g = pixelIter.getGreen();
					int b = pixelIter.getBlue();
					int colour = getColour(r, g, b);
//					if (x >= 1990 && x <= 2018 && y >= 1588 && y <= 1615) {
//						System.out.printf("%03d,%03d,%03d   %d\n", r, g, b, colour);
//						if (x == 2018) System.out.println();
//					}

					if (colour == 0) {
						if (segment != null) {
							// finished the segment
							segment.endX = x - 1;
							segment = null;
						}
					} else if (colour != 0) {
						if (segment != null && segment.colour != colour) {
							// old segment done
							segment.endX = x - 1;
							segment = null;
						}
						if (segment == null) {
							segment = new Segment();
							segment.colour = colour;
							segment.startX = x;
							segment.y = y;
							segments.add(segment);
						}
						segment.totalR += r;
						segment.totalG += g;
						segment.totalB += b;
					}
				}
				if (segment != null) {
					// segment ends at the end of the line
					segment.endX = width - 1;
				}

//				System.out.println("\nSegments for " + (y - 1));
//				for (Segment s : previous) {
//					if (s.startX >= 2177)
//						System.out.println(s.toShortString());
//				}
//				System.out.println("\nSegments for " + y);

				// check each segment against the segments of the previous line
				for (Segment s : segments) {
					for (Segment p : previous) {
						//   if they overlap
						if (s.startX <= p.endX + 1 && s.endX >= p.startX - 1 && s.colour == p.colour) {
							if (s.region != null && s.region != p.region) {
//								System.out.println(s.toShortString() + " merging with " + p.region.toShortString());
								// if this segment has a region already then merge this region and the region from the previous line's segment
								// TODO probably makes sense to pick the winner and loser based on # pixels
								Region loser = p.region;
								regions.remove(loser);
								s.region.merge(loser);
								// fixup segments from previous line with merged region
								for (Segment t : previous) {
									if (t.region == loser) t.region = s.region;
								}
								// fixup segments in current line with merged region
								for (Segment t : segments) {
									if (t.region == loser) t.region = s.region;
								}

							} else if (s.region == null) {
//								System.out.println(s.toShortString() + " assigned to " + p.region.toShortString());

								// otherwise assign the region from the previous line's segment to this segment
								p.region.merge(s);
							} else {
//								System.out.println(s.toShortString() + " already matches " + p.region.hashCode());
							}
						}
					}
					if (s.region == null) {
						Region r = new Region();
						r.setColour(s.colour);
						r.merge(s);
						regions.add(r);
//						System.out.println(s.toShortString() + " assigned new region");
					}
				}

				previous = segments;
			}

			return regions;
		}

		public static void filterRegions(List<Region> regions) {
			// strip out small regions and regions with low density
			// TODO what we should do is rank the candidates and choose the best 2
			Iterator<Region> iter = regions.iterator();
			while (iter.hasNext()) {
				Region r = iter.next();
				if (r.pixels < 100) {
//					System.out.println("Excluding for < 100 pixels: " + r);
					iter.remove();
				} else {
					double centreX = (double) r.totalX / r.pixels;
					double centreY = (double) r.totalY / r.pixels;
					double maxDist = 0;
					for (Point p : r.points) {
						double dist = p.distance(centreX, centreY);
						if (dist > maxDist) maxDist = dist;
					}
					if ((1 / 0.5) * r.pixels < (Math.PI * maxDist * maxDist)) {
//						System.out.println("Excluding for low density: " + r);
						iter.remove();
					}
				}
			}
		}

		private String toShortString() {
			return super.toString() + " (" + pixels + " pixels)";
		}

		private void merge(Region r) {
			pixels += r.pixels;
			totalX += r.totalX;
			totalY += r.totalY;
			totalR += r.totalR;
			totalG += r.totalG;
			totalB += r.totalB;
			points.addAll(r.points);
		}

		private void merge(Segment s) {
			int count = s.endX - s.startX + 1;
			pixels += count;
			totalX += (s.startX + s.endX) * count / 2;
			totalY += s.y * count;
			totalR += s.totalR;
			totalG += s.totalG;
			totalB += s.totalB;
			s.region = this;
			for (int x = s.startX; x <= s.endX; x++) {
				points.add(new Point(x, s.y));
			}
		}

		private static int getColour(int r, int g, int b) {
			if (r > 90 && g < 20 && b < 20) return COLOUR_RED;
			if (r > 100 && g < 50 && b < 50) return COLOUR_RED;
			if (r > 120 && g < 70 && b < 70) return COLOUR_RED;
			if (r < 50 && g < 50 && b > 75) return COLOUR_BLUE;
			if (r < 30 && g < 40 && b > 50) return COLOUR_BLUE;
			return 0;
		}

		public int getColour() {
			return colour;
		}

		public void setColour(int colour) {
			this.colour = colour;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append(String.format("Region of %d pixels, colour %s, centered at (%d,%d)", pixels,
					(getColour() == Region.COLOUR_BLUE ? "blue" : "red"), getCenterX(), getCenterY()));
			if (pixels > 0) {
				s.append(", average colour: ");
				s.append(String.format("(%d,%d,%d)", totalR / pixels, totalG / pixels, totalB / pixels));
			}
			if (points.size() > 0) {
				double centreX = (double) totalX / pixels;
				double centreY = (double) totalY / pixels;
				int minX = (int) Math.ceil(centreX);
				int maxX = (int) Math.floor(centreX);
				int minY = (int) Math.ceil(centreY);
				int maxY = (int) Math.floor(centreY);
				double maxDist = 0;
				double totalDist = 0;
				for (Point p : points) {
					if (p.x < minX) minX = p.x;
					if (p.x > maxX) maxX = p.x;
					if (p.y < minY) minY = p.y;
					if (p.y > maxY) maxY = p.y;
					double dist = p.distance(centreX, centreY);
					if (dist > maxDist) maxDist = dist;
					totalDist += dist;
				}
				s.append(String.format("\n\tBounds = (%d,%d)-(%d,%d). Furtherest from centre = %.2f.", minX, minY, maxX, maxY, maxDist));
				s.append(String.format("\n\tAverage distance = %.2f. Density = %.2f", totalDist / pixels, pixels / (Math.PI * maxDist * maxDist)));
			}
			return s.toString();
		}

		public int getCenterX() {
			if (pixels == 0) return -1;
			return (int) (totalX / pixels);
		}

		public int getCenterY() {
			if (pixels == 0) return -1;
			return (int) (totalY / pixels);
		}

		public static void writeOutput(BufferedImage source, List<Region> regions, String filename) {
			BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < source.getHeight(); y++) {
				for (int x = 0; x < source.getWidth(); x++) {
					output.setRGB(x, y, 0xffffffff);
				}
			}
			for (Region r : regions) {
				for (Point p : r.points) {
					output.setRGB(p.x, p.y, 0xff000000 | source.getRGB(p.x, p.y));
				}
			}
			try {
				ImageIO.write(output, "png", new File(filename));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static abstract class PixelIterator {
		abstract void next();

		abstract int getRed();

		abstract int getBlue();

		abstract int getGreen();

		static PixelIterator createPixelIterator(BufferedImage source) {
			if (source.getType() == BufferedImage.TYPE_3BYTE_BGR
					|| source.getType() == BufferedImage.TYPE_4BYTE_ABGR
					|| source.getType() == BufferedImage.TYPE_4BYTE_ABGR_PRE) {
				return new BytePixelIterator(source);
			} else {
				System.out.println("Defaulting to GeneralPixelIterator for source image type = " + source.getType());
				return new GeneralPixelIterator(source);
			}
		}
	}

	static class GeneralPixelIterator extends PixelIterator {
		int[] pixels;
		int index = 0;
		int pixel;

		GeneralPixelIterator(BufferedImage source) {
			int width = source.getWidth();
			int height = source.getHeight();

			long startMillis = System.currentTimeMillis();
			pixels = source.getRGB(0, 0, width, height, null, 0, width);
			long elapsed = System.currentTimeMillis() - startMillis;
			System.out.println("GeneralPixelIterator source.getRGB() " + elapsed + "ms");
		}

		@Override
		void next() {
			pixel = pixels[index++];
		}

		@Override
		int getRed() {
			return (pixel >> 16) & 0xff;
		}

		@Override
		int getBlue() {
			return pixel & 0xff;
		}

		@Override
		int getGreen() {
			return (pixel >> 8) & 0xff;
		}

	}

	static class BytePixelIterator extends PixelIterator {
		byte[] pixels;
		int type;
		int index = 0;
		int r, g, b;

		BytePixelIterator(BufferedImage source) {
			pixels = ((DataBufferByte) source.getRaster().getDataBuffer()).getData();
			type = source.getType();
		}

		@Override
		void next() {
			if (type == BufferedImage.TYPE_4BYTE_ABGR || type == BufferedImage.TYPE_4BYTE_ABGR_PRE) {
				index++;	// skip alpha
			}
			b = pixels[index++] & 0xff;
			g = pixels[index++] & 0xff;
			r = pixels[index++] & 0xff;
		}

		@Override
		int getRed() {
			return r;
		}

		@Override
		int getBlue() {
			return b;
		}

		@Override
		int getGreen() {
			return g;
		}
	}
}
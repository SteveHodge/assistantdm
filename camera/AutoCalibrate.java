package camera;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

		int width = source.getWidth();
		int height = source.getHeight();
		int[] pixels = source.getRGB(0, 0, width, height, null, 0, width);

		BufferedImage output = new BufferedImage(width, source.getHeight(), BufferedImage.TYPE_INT_ARGB);

		List<Region> regions = new ArrayList<>();

//		float[] hsb = null;

		for (int i = 0, y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				//int pixel = pixels[y * width + x];
				int pixel = pixels[i++];
				if (output.getRGB(x, y) != 0) continue;	// already checked

				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				Region reg = Region.getRegion(r, g, b);
				if (reg != null) {
					reg.buildRegion(x, y, pixels, width, height, output);
					if (reg.pixels > 100) {
						output.setRGB(reg.getCenterX(), reg.getCenterY(), 0xff00ffff);
						regions.add(reg);
					}
				} else {
					output.setRGB(x, y, 0xffffffff);
				}

//				double r = ((pixel >> 16) & 0xFF) / 255.0;
//				double g = ((pixel >> 8) & 0xFF) / 255.0;
//				double b = (pixel & 0xFF) / 255.0;
//
//				double alpha = (2 * r - g - b) / 2;
//				double beta = Math.sqrt(3) / 2 * (g - b);
//				double hue = Math.atan2(beta, alpha);	// hue in radians
//				double chroma = Math.sqrt(alpha * alpha + beta * beta);
//				double intensity = (r + g + b) / 3;
//				double saturation = 1 - Math.min(r, Math.min(g, b)) / intensity;
//
//				// hue2 is hue modified to range of [0,1] to match HSB value
//				double hue2 = hue / (2 * Math.PI);
//				if (hue2 < 0) hue2 = 1 + hue2;
//
//				hsb = Color.RGBtoHSB((pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF, hsb);

//				output.setRGB(x, y, 0xffffffff);

//				if (x > 285 && x < 325 && y > 185 && y < 225) {
//					System.out.print(String.format("RGB: %d,%d,%d", (pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF));
//					System.out.print(String.format(" HSIC: %.2f,%.2f,%.2f,%.2f", hue2, saturation, intensity, chroma));
//					System.out.println(String.format(" HSB: %.2f,%.2f,%.2f", hsb[0], hsb[1], hsb[2]));
//				}
//				if (hue2 >= 0.62 && hue2 <= 0.70 && saturation > 0.3 && intensity > 0.15) {
//				if ((b > 0.8 && r < 0.5 && g < 0.8) || (r > 0.8 && g < 0.6 && b < 0.6)) {	// black bg, natural light
//				if ((b > 0.3 && r < 0.2 && g < 0.2) || (r > 0.4 && g < 0.3 && b < 0.3)) {	// black bg, natural light
//					System.out.print(String.format("(%04d,%04d)", x, y));
//					System.out.print(String.format(" RGB: %d,%d,%d", (pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF));
//					System.out.print(String.format(" HSIC: %.2f,%.2f,%.2f,%.2f", hue2, saturation, intensity, chroma));
//					System.out.println(String.format(" HSB: %.2f,%.2f,%.2f", hsb[0], hsb[1], hsb[2]));
//				output.setRGB(x, y, 0xff000000 | pixel);
//				}

//				if (g > 50 && g / Math.max(r, b) > 2) {
//					output.setRGB(x, y, 0xff000000);
//				} else {
//					output.setRGB(x, y, 0xffffffff);
//				}
			}
		}

		try {
			ImageIO.write(output, "png", new File(regionsFilename));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO this is fragile. what we should do is check there are the right numbers of regions and then sort them by angle from the average centre
		Region tl = null, tr = null, bl = null, br = null;

		for (Region r : regions) {
			if (r.colour == Region.COLOUR_BLUE) {
				if (tl == null) {
					tl = r;
				} else if (tr == null) {
					if (r.getCenterX() > tl.getCenterX()) {
						tr = r;
					} else {
						tr = tl;
						tl = r;
					}
				} else {
					System.out.println("Too many blue regions found!");
				}
			} else if (r.colour == Region.COLOUR_RED) {
				if (bl == null) {
					bl = r;
				} else if (br == null) {
					if (r.getCenterX() > bl.getCenterX()) {
						br = r;
					} else {
						br = bl;
						bl = r;
					}
				} else {
					System.out.println("Too many red regions found!");
				}
			}
		}

		Point[] points = new Point[4];
		points[0] = new Point(tl.getCenterX(), tl.getCenterY());
		points[1] = new Point(tr.getCenterX(), tr.getCenterY());
		points[2] = new Point(br.getCenterX(), br.getCenterY());
		points[3] = new Point(bl.getCenterX(), bl.getCenterY());
		return points;
	}

	public static class Region {
		public static final int COLOUR_RED = 1;
		public static final int COLOUR_BLUE = 2;
		public int colour;
		public int pixels;
		long totalX, totalY;
		long totalR, totalG, totalB;

//		static Region getRegion(int x, int y, int[] pixels, int width, int height, BufferedImage output) {
//		}

		public static Region getRegion(int r, int g, int b) {
			Region reg = null;
			if (isRed(r, g, b)) {
				reg = new Region();
				reg.colour = COLOUR_RED;
			} else if (isBlue(r, g, b)) {
				reg = new Region();
				reg.colour = COLOUR_BLUE;
			}
			return reg;
		}

		static boolean isRed(int r, int g, int b) {
			if (r > 100 && g < 30 && b < 30) return true;
			return false;
		}

		static boolean isBlue(int r, int g, int b) {
			if (r < 50 && g < 50 && b > 75) return true;
			return false;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append(String.format("Region of %d pixels, colour %s, centered at (%d,%d)", pixels,
					(colour == Region.COLOUR_BLUE ? "blue" : "red"), getCenterX(), getCenterY()));
			if (pixels > 0) {
				s.append(", average colour: ");
				s.append(String.format("(%d,%d,%d)", totalR / pixels, totalG / pixels, totalB / pixels));
			}
			return s.toString();
		}

		boolean isCorrectColour(int r, int g, int b) {
			if (colour == COLOUR_RED) return isRed(r, g, b);
			if (colour == COLOUR_BLUE) return isBlue(r, g, b);
			return false;
		}

		void addPixel(int x, int y, int r, int b, int g) {
			pixels++;
			totalX += x;
			totalY += y;
			totalR += r;
			totalB += b;
			totalG += g;
		}

		public int getCenterX() {
			if (pixels == 0) return -1;
			return (int) (totalX / pixels);
		}

		public int getCenterY() {
			if (pixels == 0) return -1;
			return (int) (totalY / pixels);
		}

		public void buildRegion(int x, int y, int[] pixels, int width, int height, BufferedImage checked) {
			if (x < 0 || y < 0 || x >= width || y >= height) return;	// out of bounds
			if (checked.getRGB(x, y) != 0) return;		// already checked this pixel

			int pixel = pixels[y * width + x];
			int r = (pixel >> 16) & 0xff;
			int g = (pixel >> 8) & 0xff;
			int b = pixel & 0xff;

			if (isCorrectColour(r, g, b)) {
				addPixel(x, y, r, g, b);
				checked.setRGB(x, y, 0xff000000 | pixel);
				buildRegion(x - 1, y - 1, pixels, width, height, checked);
				buildRegion(x, y - 1, pixels, width, height, checked);
				buildRegion(x + 1, y - 1, pixels, width, height, checked);
				buildRegion(x - 1, y, pixels, width, height, checked);
				buildRegion(x + 1, y, pixels, width, height, checked);
				buildRegion(x - 1, y + 1, pixels, width, height, checked);
				buildRegion(x, y + 1, pixels, width, height, checked);
				buildRegion(x + 1, y + 1, pixels, width, height, checked);
			} else {
				checked.setRGB(x, y, 0xffffffff);
			}
		}
	}
}
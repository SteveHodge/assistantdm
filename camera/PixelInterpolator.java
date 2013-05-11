package camera;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

class PixelInterpolator {
	private PixelInterpolator() {
	}

	public static BufferedImage getImage(List<PointRemapper> transforms, BufferedImage sourceImage, int width, int height) {
		int[] pixels = sourceImage.getRGB(0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null, 0, sourceImage.getWidth());
		BufferedImage dest = new BufferedImage(width, height, sourceImage.getType());
		int[] corrected = new int[width * height];
		int offset = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Point2D.Double p = new Point2D.Double(x, y);
				for (PointRemapper t : transforms) {
					t.transform(p);
				}

				// nearest neighbour filtering
//				int srcX = (int) (p.x + 0.5);
//				int srcY = (int) (p.y + 0.5);
//				if (srcX >= 0 && srcY >= 0 && srcX < sourceImage.getWidth() && srcY < sourceImage.getHeight()) {
//					corrected[y * width + x] = pixels[srcY * sourceImage.getWidth() + srcX];
//				}

				// bilinear filtering
				int srcX = (int) p.getX();	// x,y = source coordinates
				int srcY = (int) p.getY();
				double x_diff = p.getX() - srcX;	// fractional parts in source coordinates
				double y_diff = p.getY() - srcY;

				if (srcX < 0 || srcY < 0 || srcX >= sourceImage.getWidth() - 1 || srcY >= sourceImage.getHeight() - 1) {
					offset++;
					continue;
				}

				int index = (srcY * sourceImage.getWidth() + srcX);	// index into source array
				int a = pixels[index];
				int b = pixels[index + 1];
				int c = pixels[index + sourceImage.getWidth()];
				int d = pixels[index + sourceImage.getWidth() + 1];

//				System.out.print("("+x+","+y+") -> ("+srcX+","+srcY+"): ");
//				System.out.print

				// blue element
				// Yb = Ab(1-w)(1-h) + Bb(w)(1-h) + Cb(h)(1-w) + Db(wh)
				double blue = (a & 0xff) * (1 - x_diff) * (1 - y_diff) + (b & 0xff) * (x_diff) * (1 - y_diff) +
						(c & 0xff) * (y_diff) * (1 - x_diff) + (d & 0xff) * (x_diff * y_diff);

				// green element
				// Yg = Ag(1-w)(1-h) + Bg(w)(1-h) + Cg(h)(1-w) + Dg(wh)
				double green = ((a >> 8) & 0xff) * (1 - x_diff) * (1 - y_diff) + ((b >> 8) & 0xff) * (x_diff) * (1 - y_diff) +
						((c >> 8) & 0xff) * (y_diff) * (1 - x_diff) + ((d >> 8) & 0xff) * (x_diff * y_diff);

				// red element
				// Yr = Ar(1-w)(1-h) + Br(w)(1-h) + Cr(h)(1-w) + Dr(wh)
				double red = ((a >> 16) & 0xff) * (1 - x_diff) * (1 - y_diff) + ((b >> 16) & 0xff) * (x_diff) * (1 - y_diff) +
						((c >> 16) & 0xff) * (y_diff) * (1 - x_diff) + ((d >> 16) & 0xff) * (x_diff * y_diff);

				corrected[offset++] =
						0xff000000 | // hardcode alpha
						((((int) red) << 16) & 0xff0000) |
						((((int) green) << 8) & 0xff00) |
						((int) blue);

			}
		}

		dest.setRGB(0, 0, dest.getWidth(), dest.getHeight(), corrected, 0, dest.getWidth());
		return dest;
	}

}

package camera;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class PixelInterpolator {
	private PixelInterpolator() {
	}

	public static BufferedImage getImage(List<PointRemapper> transforms, BufferedImage sourceImage, int width, int height) {
		if (sourceImage.getType() == BufferedImage.TYPE_3BYTE_BGR) return getImageFromByteSourceParallel(transforms, sourceImage, width, height);

		System.out.println("Using slow PixelInterpolator for image type " + sourceImage.getType());
		int[] pixels = sourceImage.getRGB(0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null, 0, sourceImage.getWidth());

		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		byte[] corrected = ((DataBufferByte) dest.getRaster().getDataBuffer()).getData();
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

				corrected[offset++] = (byte) blue;
				corrected[offset++] = (byte) green;
				corrected[offset++] = (byte) red;
			}
		}

//		watch.start();
//		dest.setRGB(0, 0, dest.getWidth(), dest.getHeight(), corrected, 0, dest.getWidth());
//		watch.stop();
//		System.out.println("PixelInterpolator dest.setRGB took " + watch.getElapsed() + "ms");
		return dest;
	}

	public static BufferedImage getImageFromByteSource(List<PointRemapper> transforms, BufferedImage sourceImage, int width, int height) {
		if (sourceImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			System.err.println("PixelInterpolator.getImageFromByteSource() bad source image type: " + sourceImage.getType());
			return null;
		}

		byte[] pixels = ((DataBufferByte) sourceImage.getRaster().getDataBuffer()).getData();
		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		byte[] corrected = ((DataBufferByte) dest.getRaster().getDataBuffer()).getData();
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
//					int srcOff = 3 * (srcY * sourceImage.getWidth() + srcX);
//					corrected[offset++] = pixels[srcOff];
//					corrected[offset++] = pixels[srcOff + 1];
//					corrected[offset++] = pixels[srcOff + 2];
//				}

				// bilinear filtering
				int srcX = (int) p.getX();	// x,y = source coordinates
				int srcY = (int) p.getY();
				double x_diff = p.getX() - srcX;	// fractional parts in source coordinates
				double y_diff = p.getY() - srcY;

				if (srcX < 0 || srcY < 0 || srcX >= sourceImage.getWidth() - 1 || srcY >= sourceImage.getHeight() - 1) {
					offset += 3;
					continue;
				}

				// indexes for the 4 source pixels
				int a = 3 * (srcY * sourceImage.getWidth() + srcX);	// index into source array
				int b = a + 3;
				int c = a + 3 * sourceImage.getWidth();
				int d = c + 3;

				for (int i = 0; i < 3; i++) {
					double channel = (pixels[a] & 0xff) * (1 - x_diff) * (1 - y_diff)
							+ (pixels[b] & 0xff) * (x_diff) * (1 - y_diff)
							+ (pixels[c] & 0xff) * (y_diff) * (1 - x_diff)
							+ (pixels[d] & 0xff) * (x_diff * y_diff);
					corrected[offset++] = (byte) channel;
					a++;
					b++;
					c++;
					d++;
				}
			}
		}

		return dest;
	}

	@SuppressWarnings("serial")
	private static class BilinearInterpolate extends RecursiveAction {
		private final List<PointRemapper> transforms;
		private final byte[] pixels;	// source
		private final int srcWidth;		// source width
		private final int srcHeight;	// source height
		private final byte[] corrected;	// dest
		private final int width;	// dest width
		private final int height;	// dest height to calculate
		private final int y0;		// dest first row

		BilinearInterpolate(List<PointRemapper> transforms, byte[] pixels, int srcWidth, int srcHeight, byte[] corrected, int width, int height, int y0) {
			this.transforms = transforms;
			this.pixels = pixels;
			this.srcWidth = srcWidth;
			this.srcHeight = srcHeight;
			this.corrected = corrected;
			this.width = width;
			this.height = height;
			this.y0 = y0;
		}

		private void interpolate() {
			int offset = y0 * width * 3;

			for (int y = y0; y < y0 + height; y++) {
				for (int x = 0; x < width; x++) {
					Point2D.Double p = new Point2D.Double(x, y);
					for (PointRemapper t : transforms) {
						t.transform(p);
					}

					// nearest neighbour filtering
//					int srcX = (int) (p.x + 0.5);
//					int srcY = (int) (p.y + 0.5);
//					if (srcX >= 0 && srcY >= 0 && srcX < sourceImage.getWidth() && srcY < sourceImage.getHeight()) {
//						int srcOff = 3 * (srcY * sourceImage.getWidth() + srcX);
//						corrected[offset++] = pixels[srcOff];
//						corrected[offset++] = pixels[srcOff + 1];
//						corrected[offset++] = pixels[srcOff + 2];
//					}

					// bilinear filtering
					int srcX = (int) p.getX();	// x,y = source coordinates
					int srcY = (int) p.getY();
					double x_diff = p.getX() - srcX;	// fractional parts in source coordinates
					double y_diff = p.getY() - srcY;

					if (srcX < 0 || srcY < 0 || srcX >= srcWidth - 1 || srcY >= srcHeight - 1) {
						offset += 3;
						continue;
					}

					// indexes for the 4 source pixels
					int a = 3 * (srcY * srcWidth + srcX);	// index into source array
					int b = a + 3;
					int c = a + 3 * srcWidth;
					int d = c + 3;

					for (int i = 0; i < 3; i++) {
						double channel = (pixels[a] & 0xff) * (1 - x_diff) * (1 - y_diff)
								+ (pixels[b] & 0xff) * (x_diff) * (1 - y_diff)
								+ (pixels[c] & 0xff) * (y_diff) * (1 - x_diff)
								+ (pixels[d] & 0xff) * (x_diff * y_diff);
						corrected[offset++] = (byte) channel;
						a++;
						b++;
						c++;
						d++;
					}
				}
			}
		}

		@Override
		protected void compute() {
			if (height <= 20) {
				interpolate();
			} else {
				int split = height / 2;

				BilinearInterpolate top = new BilinearInterpolate(transforms, pixels, srcWidth, srcHeight, corrected, width, split, y0);
				BilinearInterpolate bottom = new BilinearInterpolate(transforms, pixels, srcWidth, srcHeight, corrected, width, height - split, y0 + split);
				invokeAll(top, bottom);
			}
		}
	}

	static private ForkJoinPool pool = new ForkJoinPool();

	public static BufferedImage getImageFromByteSourceParallel(List<PointRemapper> transforms, BufferedImage sourceImage, int width, int height) {
		if (sourceImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			System.err.println("PixelInterpolator.getImageFromByteSourceParallel() bad source image type: " + sourceImage.getType());
			return null;
		}

//		System.out.println("Parallel Pixel Interpolator");

		byte[] pixels = ((DataBufferByte) sourceImage.getRaster().getDataBuffer()).getData();
		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		byte[] corrected = ((DataBufferByte) dest.getRaster().getDataBuffer()).getData();

		BilinearInterpolate interpolate = new BilinearInterpolate(transforms, pixels, sourceImage.getWidth(), sourceImage.getHeight(), corrected, width, height, 0);
		pool.invoke(interpolate);
		return dest;
	}
}

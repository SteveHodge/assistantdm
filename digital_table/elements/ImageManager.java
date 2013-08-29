package digital_table.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Timer;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import digital_table.server.MapCanvas;

// TODO if the source image has been resized and the user subsequently wants a larger size then we should re-read the higher resolution source
// TODO don't like the rotated image handling here

abstract class ImageManager {
	protected BufferedImage sourceImage = null;
	BufferedImage rotatedImage = null;	// TODO make private
	protected AnimationFrame[] frames = null;	// TODO move to animated subclasses
	protected int index;	// TODO move to animated subclasses
	protected Timer timer = null;	// TODO move to animated subclasses
	protected MapCanvas canvas;
	protected double sourceGridWidth = 0;
	protected double sourceGridHeight = 0;

	private ImageManager() {
	}

	static ImageManager createImageManager(MapCanvas canvas, byte[] bytes) {
		ImageManager m = null;
		try {
			try {
				// see if this is a serialized Animation
				ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
				ObjectInputStream in = new ObjectInputStream(stream);
				Animation a = (Animation) in.readObject();
				in.close();
				m = new PNGSequenceIM(a);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				// not a serialized animation. perhaps it's an image file
				ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
				ImageInputStream iis = ImageIO.createImageInputStream(stream);
				ImageReader reader = ImageIO.getImageReaders(iis).next();
				reader.setInput(iis);
				if (reader.getFormatName().equals("gif")) {
					m = new GIFIM(reader);
				} else {
					m = new ImageIM(reader);
				}
			}

			m.canvas = canvas;
			m.canvas.repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return m;
	}

	BufferedImage getImage(int rotations) {
		if (sourceImage == null) {
			readImages();
		}

		if (rotatedImage == null) {
			createRotatedImage(rotations);
		}

		return rotatedImage;
	}

	BufferedImage getImage() {
		if (sourceImage == null) {
			readImages();
		}
		return sourceImage;
	}

	double getSourceGridWidth() {
		return sourceGridWidth;
	}

	double getSourceGridHeight() {
		return sourceGridHeight;
	}

	// rescales the image based on the canvas resolution (assumes source image resolution matches the remote screen)
	BufferedImage resizeImage(BufferedImage source) {
		// TODO ugly having this here
		if (sourceGridWidth == 0 || sourceGridHeight == 0) {
			Point2D gridSize = canvas.getRemoteGridCellCoords(source.getWidth(), source.getHeight());
			sourceGridWidth = gridSize.getX();
			sourceGridHeight = gridSize.getY();
		}

		int srcW = source.getWidth();
		int srcH = source.getHeight();

		Point2D gridSize = canvas.getRemoteGridCellCoords(srcW, srcH);
		Point size = canvas.getDisplayCoordinates(gridSize);
		int destW = size.x;
		int destH = size.y;

		if (srcW / destW >= 2 && srcH / destH >= 2) {
			System.out.println("Rescaling image from " + srcW + "x" + srcH + " to " + destW + "x" + destH);
//			long free = Runtime.getRuntime().freeMemory();
			BufferedImage scaled = new BufferedImage(destW, destH, source.getType());
			Graphics2D g = scaled.createGraphics();
			g.drawImage(source, 0, 0, destW, destH, null);

//			Runtime.getRuntime().gc();
//			long freed = Runtime.getRuntime().freeMemory() - free;
//			double freedMB = (double) freed / (1024 * 1024);
//			System.out.println(String.format("Freed %.2f MB", freedMB));
			return scaled;
		}
		return source;
	}

	protected abstract void readImages();

	protected void initImages() {
		index = 0;
		sourceImage = frames[index].getImage();

		if (sourceGridWidth == 0 || sourceGridHeight == 0) {
			Point2D gridSize = canvas.getRemoteGridCellCoords(sourceImage.getWidth(), sourceImage.getHeight());
			sourceGridWidth = gridSize.getX();
			sourceGridHeight = gridSize.getY();
		}

		if (frames.length > 1) {
			timer = new Timer(frames[0].getDelay(), new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					index++;
					if (index >= frames.length) index = 0;
					sourceImage = frames[index].getImage();
					rotatedImage = null;
					canvas.repaint();
					timer.setInitialDelay(frames[index].getDelay());
					timer.start();
				}
			});
			timer.setRepeats(false);
			timer.start();
		}
	}

	private void createRotatedImage(int rotations) {
		if (sourceImage != null) {
			AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations);
			Point p = new Point(sourceImage.getWidth(), sourceImage.getHeight());
			t.transform(p, p);	// transform to get new dimensions

			rotatedImage = new BufferedImage(Math.abs(p.x), Math.abs(p.y), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) rotatedImage.getGraphics();
			g2d.rotate(Math.toRadians(rotations * 90), rotatedImage.getWidth() / 2, rotatedImage.getHeight() / 2);
			g2d.translate((rotatedImage.getWidth() - sourceImage.getWidth()) / 2, (rotatedImage.getHeight() - sourceImage.getHeight()) / 2);
			g2d.drawImage(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
			g2d.dispose();

			// get the dimensions in grid-coordinate space of the remote display:
			// TODO setting sizes here means we lose any user set size which is not what we want - but we should swap the values if we've rotated 90 degrees
			// TODO strictly speaking we should calculate the bottom right corner and then use that to determine the size
//				Point2D size = canvas.getRemoteGridCellCoords(rotatedImage.getWidth(), rotatedImage.getHeight());
//				width.setValue(size.getX());
//				height.setValue(size.getY());
		}
	}

	private static class ImageIM extends ImageManager {
		private ImageReader reader;

		public ImageIM(ImageReader reader) {
			this.reader = reader;
		}

		@Override
		protected void readImages() {
			BufferedImage img = null;
			try {
				img = reader.read(reader.getMinIndex());
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (img == null) return;

			frames = new AnimationFrame[1];
			frames[0] = new AnimationFrame(img, 0);

			index = 0;
			sourceImage = frames[index].getImage();

			if (sourceGridWidth == 0 || sourceGridHeight == 0) {
				Point2D gridSize = canvas.getRemoteGridCellCoords(sourceImage.getWidth(), sourceImage.getHeight());
				sourceGridWidth = gridSize.getX();
				sourceGridHeight = gridSize.getY();
			}
		}
	};

	private static class PNGSequenceIM extends ImageManager {
		private Animation animation;

		public PNGSequenceIM(Animation a) {
			animation = a;
		}

		@Override
		protected void readImages() {
			frames = animation.getFrames(this, new File("media"));
			initImages();
		}

	};

	private static class GIFIM extends ImageManager {
		private ImageReader reader;

		public GIFIM(ImageReader reader) {
			this.reader = reader;
		}

		@Override
		protected void readImages() {
			try {
				frames = readGIF(reader);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			initImages();
		}

		private AnimationFrame[] readGIF(ImageReader reader) throws IOException {
			ArrayList<AnimationFrame> frames = new ArrayList<AnimationFrame>(2);

			int width = -1;
			int height = -1;

			IIOMetadata metadata = reader.getStreamMetadata();
			if (metadata != null) {
				IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

				NodeList globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

				if (globalScreenDescriptor != null && globalScreenDescriptor.getLength() > 0) {
					IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

					if (screenDescriptor != null) {
						width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
						height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
					}
				}
			}

			BufferedImage master = null;
			Graphics2D masterGraphics = null;
			int lastComplete = 0;	// index of the last frame that did not have RestorePrevious as the disposal method

			for (int frameIndex = 0;; frameIndex++) {
				BufferedImage image;
				try {
					image = reader.read(frameIndex);
				} catch (IndexOutOfBoundsException io) {
					break;
				}

				if (width == -1 || height == -1) {
					width = image.getWidth();
					height = image.getHeight();
				}

				IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
				IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
				int delay = Integer.valueOf(gce.getAttribute("delayTime"));
				String disposal = gce.getAttribute("disposalMethod");

				int x = 0;
				int y = 0;

				if (master == null) {
					master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					masterGraphics = master.createGraphics();
					masterGraphics.setBackground(new Color(0, 0, 0, 0));
				} else {
					NodeList children = root.getChildNodes();
					for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
						Node nodeItem = children.item(nodeIndex);
						if (nodeItem.getNodeName().equals("ImageDescriptor")) {
							NamedNodeMap map = nodeItem.getAttributes();
							x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
							y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
						}
					}
				}
				masterGraphics.drawImage(image, x, y, null);

				BufferedImage copy = new BufferedImage(master.getColorModel(), master.copyData(null), master.isAlphaPremultiplied(), null);
				frames.add(new AnimationFrame(copy, delay * 10));

				if (disposal.equals("restoreToPrevious")) {
					BufferedImage from = frames.get(lastComplete).getImage();
					master = new BufferedImage(from.getColorModel(), from.copyData(null), from.isAlphaPremultiplied(), null);
					masterGraphics = master.createGraphics();
					masterGraphics.setBackground(new Color(0, 0, 0, 0));
				} else {
					lastComplete = frameIndex;
					if (disposal.equals("restoreToBackgroundColor")) {
						masterGraphics.clearRect(x, y, image.getWidth(), image.getHeight());
					}
				}
			}
			reader.dispose();

			return frames.toArray(new AnimationFrame[frames.size()]);
		}
	};
}
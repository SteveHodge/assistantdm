package digital_table.server;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Timer;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO if the source image has been resized and the user subsequently wants a larger size then we should re-read the higher resolution source
// TODO don't like the rotated image handling here

public abstract class ImageMedia {
	MapCanvas canvas;
	Point2D sourceGridSize = null;		// stores the original size (in grid units) of the first image prescaleImage() processes. if left unset then it will be calculated when required from the presumably unscaled frames[0]

	BufferedImage[] frames = null;
	int index;

	AffineTransform transform = null;
	BufferedImage[] transformed;
	double tWidth, tHeight, tx, ty;		// TODO replace these with a Rectangle2D.Double

	private ImageMedia() {
	}

	static ImageMedia createImageMedia(MapCanvas canvas, byte[] bytes) {
		ImageMedia m = null;
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			ImageInputStream iis = ImageIO.createImageInputStream(stream);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				reader.setInput(iis);
				if (reader.getFormatName().equals("gif")) {
					m = new GIFMedia(reader);
				} else {
					m = new StaticImageMdia(reader);
				}

			} else {
				// it's not an image so assume it's an Animation xml file
				m = new ImageSequenceMedia(new ByteArrayInputStream(bytes));
			}

			m.canvas = canvas;
			m.canvas.repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return m;
	}

	public static Rectangle2D getBounds(AffineTransform xform, double width, double height) {
		// determine the bounds of the transformed image
		Point2D[] points = new Point2D[4];
		points[0] = new Point2D.Double(0, 0);
		points[1] = new Point2D.Double(width, 0);
		points[2] = new Point2D.Double(width, height);
		points[3] = new Point2D.Double(0, height);
		xform.transform(points, 0, points, 0, 4);
		Rectangle2D.Double bounds = new Rectangle2D.Double();
		bounds.x = points[0].getX();
		bounds.y = points[0].getY();
		bounds.width = points[0].getX();
		bounds.height = points[0].getY();
		for (int i = 1; i < 4; i++) {
			bounds.x = Math.min(bounds.x, points[i].getX());
			bounds.y = Math.min(bounds.y, points[i].getY());
			bounds.width = Math.max(bounds.width, points[i].getX());
			bounds.height = Math.max(bounds.height, points[i].getY());
		}
		bounds.width = bounds.width - bounds.x;
		bounds.height = bounds.height - bounds.y;
		return bounds;
	}

//	public static BufferedImage getTransformedImage(BufferedImage image, AffineTransform transform) {
//		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
//		Rectangle2D bounds = getBounds(transform, image.getWidth(), image.getHeight());
//		BufferedImage out = new BufferedImage((int) Math.ceil(bounds.getWidth()), (int) Math.ceil(bounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
//		//System.out.println("New image size: " + transformed[index].getWidth() + "x" + transformed[index].getHeight());
//		Graphics2D g = (Graphics2D) out.getGraphics();
//		g.drawImage(image, op, (int) -bounds.getX(), (int) -bounds.getY());
//		g.dispose();
//		return out;
//	}

	public void setTransform(AffineTransform xform) {
		if (transformed == null) readImages();		// ensures transformed is allocated

		if (xform == null) {
			transform = null;
			Arrays.fill(transformed, null);
			tx = 0;
			ty = 0;
			tWidth = frames[index].getWidth();
			tHeight = frames[index].getHeight();

		} else if (!xform.equals(transform)) {
			transform = xform;
			Arrays.fill(transformed, null);

			Rectangle2D bounds = getBounds(xform, frames[index].getWidth(), frames[index].getHeight());
			tx = bounds.getX();
			ty = bounds.getY();
			tWidth = bounds.getWidth();
			tHeight = bounds.getHeight();

//			// determine the bounds of the transformed image
//			Point2D[] points = new Point2D[4];
//			points[0] = new Point2D.Double(0, 0);
//			points[1] = new Point2D.Double(frames[index].getWidth(), 0);
//			points[2] = new Point2D.Double(frames[index].getWidth(), frames[index].getHeight());
//			points[3] = new Point2D.Double(0, frames[index].getHeight());
//			xform.transform(points, 0, points, 0, 4);
//			tx = points[0].getX();
//			ty = points[0].getY();
//			tWidth = points[0].getX();
//			tHeight = points[0].getY();
//			for (int i = 1; i < 4; i++) {
//				tx = Math.min(tx, points[i].getX());
//				ty = Math.min(ty, points[i].getY());
//				tWidth = Math.max(tWidth, points[i].getX());
//				tHeight = Math.max(tHeight, points[i].getY());
//			}
//			tWidth = tWidth - tx;
//			tHeight = tHeight - ty;
		}
	}

	static Set<String> logged = new HashSet<>();

	public void printMemoryUsage() {
		long srcBytes = 0;
		for (int i = 0; i < frames.length; i++) {
			DataBuffer buff = frames[i].getRaster().getDataBuffer();
			srcBytes += buff.getSize() * DataBuffer.getDataTypeSize(buff.getDataType()) / 8;
		}

		long cacheBytes = 0;
		if (transform != null) {
			for (int i = 0; i < transformed.length; i++) {
				if (transformed[i] != null && transformed[i].getRaster() != null) {
					DataBuffer buff = transformed[i].getRaster().getDataBuffer();
					cacheBytes += buff.getSize() * DataBuffer.getDataTypeSize(buff.getDataType()) / 8;
				}
			}
		}

		String out = String.format("Image %s frames: %d, total source size: %.3f MB, total cache size: %.3f MB",
				this.toString(), frames.length, (float) srcBytes / (1024 * 1024), (float) cacheBytes / (1024 * 1024));
		if (!logged.contains(out)) {
			System.out.println(out);
			logged.add(out);
		}
	}

	public BufferedImage getImage() {
		if (transform == null) return getSourceImage();
		if (Math.ceil(tWidth) == 0.0 || Math.ceil(tHeight) == 0.0) {
			System.err.println("Image has invalid width (" + tWidth + ") or height (" + tHeight + ")");
			return getSourceImage();
		}

		if (transformed[index] == null && frames[index] != null) {
//			Runtime rt = Runtime.getRuntime();
//			rt.gc();
//			rt.gc();
//			rt.gc();
//			long used = rt.totalMemory() - rt.freeMemory();
//			double usedMB = (double) used / (1024 * 1024);
//			System.out.println(String.format("Transforming image... used mem = %.3f MB ", usedMB));

			AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
			transformed[index] = new BufferedImage((int) Math.ceil(tWidth), (int) Math.ceil(tHeight), BufferedImage.TYPE_INT_ARGB);
			//System.out.println("New image size: " + transformed[index].getWidth() + "x" + transformed[index].getHeight());
			Graphics2D g = (Graphics2D) transformed[index].getGraphics();
			g.drawImage(frames[index], op, (int) -tx, (int) -ty);
			g.dispose();

//			rt.gc();
//			rt.gc();
//			rt.gc();
//			used = rt.totalMemory() - rt.freeMemory();
//			usedMB = (double) used / (1024 * 1024);
//			System.out.println(String.format("Transformed image... used mem = %.3f MB ", usedMB));
//			DataBuffer buff = transformed[index].getRaster().getDataBuffer();
//			int kbytes = buff.getSize() * DataBuffer.getDataTypeSize(buff.getDataType()) / (8 * 1024);
//			System.out.println("Transformed image data size (kB) = " + kbytes);
		}

		return transformed[index];
	}

	public double getTransformedXOffset() {
		return tx;
	}

	public double getTransformedYOffset() {
		return ty;
	}

	BufferedImage getSourceImage() {
		if (frames == null || frames[index] == null) {
			readImages();
		}
		return frames[index];
	}

	public int getSourceWidth() {
		BufferedImage img = getSourceImage();
		return img == null ? 0 : img.getWidth();
	}

	public int getSourceHeight() {
		BufferedImage img = getSourceImage();
		return img == null ? 0 : img.getHeight();
	}

	public double getSourceGridWidth() {
		if (sourceGridSize == null) {
			BufferedImage image = getSourceImage();
			if (image == null) return 0;
			if (sourceGridSize == null) {
				// sourceGridSize may have already been set as a consequence of getSourceImage()
				sourceGridSize = canvas.getRemote().convertDisplayCoordsToGrid(image.getWidth(), image.getHeight());
			}
		}
		return sourceGridSize.getX();
	}

	public double getSourceGridHeight() {
		if (sourceGridSize == null) {
			BufferedImage image = getSourceImage();
			if (image == null) return 0;
			if (sourceGridSize == null) {
				// sourceGridSize may have already been set as a consequence of getSourceImage()
				sourceGridSize = canvas.getRemote().convertDisplayCoordsToGrid(image.getWidth(), image.getHeight());
			}
		}
		return sourceGridSize.getY();
	}

	public int getCurrentFrameIndex() {
		return index;
	}

	public int getFrameCount() {
		if (frames == null) return 0;
		return frames.length;
	}

	public void stop() {
	}

	public void playOrPause() {
	}

// prescales the image based on the canvas resolution (assumes source image resolution matches the remote screen)
	BufferedImage prescaleImage(BufferedImage source) {
		int srcW = source.getWidth();
		int srcH = source.getHeight();

		Point2D gridSize = canvas.getRemote().convertDisplayCoordsToGrid(srcW, srcH);
		if (sourceGridSize == null) sourceGridSize = gridSize;
		Point size = canvas.convertCanvasCoordsToDisplay(gridSize);
		int destW = size.x;
		int destH = size.y;

		if (srcW / destW >= 2 && srcH / destH >= 2) {
			System.out.println("Rescaling image from " + srcW + "x" + srcH + " to " + destW + "x" + destH);
//			long free = Runtime.getRuntime().freeMemory();
			BufferedImage scaled = new BufferedImage(destW, destH, source.getType());
			Graphics2D g = scaled.createGraphics();
			g.drawImage(source, 0, 0, destW, destH, null);
			g.dispose();

//			Runtime.getRuntime().gc();
//			long freed = Runtime.getRuntime().freeMemory() - free;
//			double freedMB = (double) freed / (1024 * 1024);
//			System.out.println(String.format("Freed %.2f MB", freedMB));
			return scaled;
		}
		return source;
	}

	abstract void readImages();

	private static class StaticImageMdia extends ImageMedia {
		private ImageReader reader;

		public StaticImageMdia(ImageReader reader) {
			this.reader = reader;
		}

		@Override
		void readImages() {
			frames = new BufferedImage[1];
			transformed = new BufferedImage[1];
			index = 0;

			try {
				frames[0] = reader.read(reader.getMinIndex());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	private static abstract class AnimatedMedia extends ImageMedia {
		Timer timer = null;
		int loopFrame = -1;		// frame number to go to after the last frame. -1 means don't loop

		abstract int getDelay(int frame);

		@Override
		public int getCurrentFrameIndex() {
			return index;
		}

		@Override
		public void stop() {
			if (timer != null) {
				timer.stop();
				canvas.repaint();
				timer.setInitialDelay(getDelay(0));
			}
			index = 0;
		}

		@Override
		public void playOrPause() {
			if (timer == null) {
				initTimer();
			} else {
				if (timer.isRunning()) {
					timer.stop();
				} else {
					timer.start();
				}
			}
		}

		// using swing timers means that slow painting will cause slow animation, not just slow framerate
		// TODO consider switching to general timers
		void initTimer() {
			if (frames == null) return;

			index = 0;

			if (frames.length > 1) {
				timer = new Timer(getDelay(0), new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						index++;
						canvas.repaint();
						if (index >= frames.length) {
							if (loopFrame == -1) {
								index = 0;
								timer.stop();
								return;
							}
							index = loopFrame;
						}
						timer.setInitialDelay(getDelay(index));
						timer.start();
					}
				});
				timer.setRepeats(false);
				timer.start();
			}
		}
	}

// XXX perhaps better to parse the XML entirely rather than using the DOM as state
	private static class ImageSequenceMedia extends AnimatedMedia {
		private Element animationNode = null;

		private ImageSequenceMedia(InputStream xmlIS) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			InputStream xsdIS = getClass().getClassLoader().getResourceAsStream("animation.xsd");
			try {
				factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(xsdIS)));
				Document dom = factory.newDocumentBuilder().parse(xmlIS);

				NodeList nodes = dom.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node.getNodeName().equals("Animation")) {
						animationNode = (Element) node;
					}
				}
			} catch (SAXException | IOException | ParserConfigurationException e) {
				e.printStackTrace();
			}

			if (animationNode != null) {
				frames = new BufferedImage[getFrameCount()];
				transformed = new BufferedImage[getFrameCount()];
				if (animationNode.hasAttribute("loopframe")) {
					loopFrame = Integer.parseInt(animationNode.getAttribute("loopframe"));
				}
				if (animationNode.hasAttribute("gridwidth") && animationNode.hasAttribute("gridheight")) {
					double width = Double.parseDouble(animationNode.getAttribute("gridwidth"));
					double height = Double.parseDouble(animationNode.getAttribute("gridheight"));
					sourceGridSize = new Point2D.Double(width, height);
				}
			}
		}

		@Override
		int getDelay(int frame) {
			int framerate = 20;
			if (animationNode != null && animationNode.hasAttribute("framerate")) {
				framerate = Integer.parseInt(animationNode.getAttribute("framerate"));
			}
			int delay = 1000 / framerate;

			return delay;
		}

		@Override
		public int getFrameCount() {
			if (animationNode == null) return 0;

			NodeList nodes = animationNode.getChildNodes();
			int count = 0;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("Frame")) count++;
			}
			return count;
		}

		private URI getImageURI(int frame) {
			if (animationNode == null) return null;

			NodeList nodes = animationNode.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("Frame")) {
					if (--frame < 0) {
						URI uri = null;
						try {
							uri = new URI(((Element) node).getAttribute("uri"));
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
						return uri;
					}
				}
			}
			return null;
		}

		@Override
		void readImages() {
			for (int i = 0; i < frames.length; i++) {
				try {
					byte[] bytes = MediaManager.INSTANCE.getFile(getImageURI(i));
					ByteArrayInputStream in = new ByteArrayInputStream(bytes);
					BufferedImage image = ImageIO.read(in);

					image = prescaleImage(image);
					frames[i] = image;

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			initTimer();
		}
	};

	private static class GIFMedia extends AnimatedMedia {
		private ImageReader reader;
		private int[] delays;

		public GIFMedia(ImageReader reader) {
			this.reader = reader;
		}

		@Override
		int getDelay(int frame) {
			int delay = delays[frame];
			if (delay < 100) delay = 100;
			return delay;
		}

		@Override
		void readImages() {
			try {
				readGIF(reader);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			initTimer();
		}

		private void readGIF(ImageReader reader) throws IOException {
			ArrayList<BufferedImage> frames = new ArrayList<>();
			ArrayList<Integer> delays = new ArrayList<>();

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
				frames.add(copy);
				delays.add(delay * 10);

				if (disposal.equals("restoreToPrevious")) {
					BufferedImage from = frames.get(lastComplete);
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

			this.frames = frames.toArray(new BufferedImage[frames.size()]);
			transformed = new BufferedImage[frames.size()];
			this.delays = new int[delays.size()];
			for (int i = 0; i < delays.size(); i++) {
				this.delays[i] = delays.get(i);
			}
		}
	};
}
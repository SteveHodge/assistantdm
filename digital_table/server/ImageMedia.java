package digital_table.server;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

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
	BufferedImage sourceImage = null;
	public BufferedImage rotatedImage = null;	// TODO should not be public
	MapCanvas canvas;
	double sourceGridWidth = 0;
	double sourceGridHeight = 0;

	private ImageMedia() {
	}

	static ImageMedia createImageManager(MapCanvas canvas, byte[] bytes) {
		ImageMedia m = null;
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			ImageInputStream iis = ImageIO.createImageInputStream(stream);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				reader.setInput(iis);
				if (reader.getFormatName().equals("gif")) {
					m = new GIFIM(reader);
				} else {
					m = new ImageIM(reader);
				}

			} else {
				// it's not an image so assume it's an Animation xml file
				m = new ImageSequenceIM(new ByteArrayInputStream(bytes));
			}

			m.canvas = canvas;
			m.canvas.repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return m;
	}

	public BufferedImage getImage(int rotations) {
		if (sourceImage == null) {
			readImages();
		}

		if (rotatedImage == null) {
			createRotatedImage(rotations);
		}

		return rotatedImage;
	}

	public BufferedImage getImage() {
		if (sourceImage == null) {
			readImages();
		}
		return sourceImage;
	}

	public double getSourceGridWidth() {
		return sourceGridWidth;
	}

	public double getSourceGridHeight() {
		return sourceGridHeight;
	}

	// rescales the image based on the canvas resolution (assumes source image resolution matches the remote screen)
	BufferedImage resizeImage(BufferedImage source) {
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

	abstract void readImages();

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

	private static class ImageIM extends ImageMedia {
		private ImageReader reader;

		public ImageIM(ImageReader reader) {
			this.reader = reader;
		}

		@Override
		void readImages() {
			sourceImage = null;
			try {
				sourceImage = reader.read(reader.getMinIndex());
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (sourceImage == null) return;

			Point2D gridSize = canvas.getRemoteGridCellCoords(sourceImage.getWidth(), sourceImage.getHeight());
			sourceGridWidth = gridSize.getX();
			sourceGridHeight = gridSize.getY();
		}
	};

	private static abstract class AnimationIM extends ImageMedia {
		BufferedImage[] frames = null;
		int index;
		Timer timer = null;

		abstract int getDelay(int frame);

		void initImages() {
			if (frames == null) return;

			index = 0;
			sourceImage = frames[index];

			if (frames.length > 1) {
				timer = new Timer(getDelay(0), new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						index++;
						if (index >= frames.length) index = 0;
						sourceImage = frames[index];
						rotatedImage = null;
						canvas.repaint();
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
	private static class ImageSequenceIM extends AnimationIM {
		private Element animationNode = null;

		private ImageSequenceIM(InputStream xmlIS) {
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
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
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

		private int getFrameCount() {
			if (animationNode == null) return 0;

			NodeList nodes = animationNode.getChildNodes();
			int count = 0;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("Frame")) count++;
			}
			return count;
		}

		private String getImageFileName(int frame) {
			if (animationNode == null) return null;

			NodeList nodes = animationNode.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("Frame")) {
					if (--frame < 0) {
						return ((Element) node).getAttribute("filename");
					}
				}
			}
			return null;
		}

		@Override
		void readImages() {
			frames = new BufferedImage[getFrameCount()];

			for (int i = 0; i < frames.length; i++) {
				String filename = getImageFileName(i);

				try {
					// TODO more sophisticated filename parsing - detect if it has path or not
					File imgFile = new File(new File("media"), filename);
					System.out.println("Adding frame " + imgFile);
					BufferedImage image = ImageIO.read(imgFile);

					if (i == 0) {
						Point2D gridSize = canvas.getRemoteGridCellCoords(image.getWidth(), image.getHeight());
						sourceGridWidth = gridSize.getX();
						sourceGridHeight = gridSize.getY();
					}

					image = resizeImage(image);
					frames[i] = image;

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			initImages();
		}
	};

	private static class GIFIM extends AnimationIM {
		private ImageReader reader;
		private int[] delays;

		public GIFIM(ImageReader reader) {
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

			Point2D gridSize = canvas.getRemoteGridCellCoords(frames[0].getWidth(), frames[0].getHeight());
			sourceGridWidth = gridSize.getX();
			sourceGridHeight = gridSize.getY();

			initImages();
		}

		private void readGIF(ImageReader reader) throws IOException {
			ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
			ArrayList<Integer> delays = new ArrayList<Integer>();

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
			this.delays = new int[delays.size()];
			for (int i = 0; i < delays.size(); i++) {
				this.delays[i] = delays.get(i);
			}
		}
	};
}
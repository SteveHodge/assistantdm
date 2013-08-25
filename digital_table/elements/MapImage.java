package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Timer;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import digital_table.server.MapCanvas.Order;

// TODO cache scaled image for performance
// TODO should have some sort of persistent cache so we don't have to keep the image file bytes in memory and don't have to resend the image each time
// TODO create grid-aligned property

public class MapImage extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ALPHA = "alpha";	// float
	//	public final static String PROPERTY_FILENAME = "filename";	// String - read only
	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_WIDTH = "width";	// double
	public final static String PROPERTY_HEIGHT = "height";	// double
	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double
	public final static String PROPERTY_CLEARCELL = "clear";	// Point - when this property is set the specified cell will be cleared
	public final static String PROPERTY_UNCLEARCELL = "unclear";	// Point - when this property is set the specified cell will be shown again
	public final static String PROPERTY_IMAGE = "image";	// byte[]

	protected transient ImageManager image = null;
	byte[] bytes = null;	// used to store the raw bytes so we can be serialised

	// position in grid coordinate-space:
	Property<Double> x = new Property<Double>(PROPERTY_X, 0d, Double.class);
	Property<Double> y = new Property<Double>(PROPERTY_Y, 0d, Double.class);

	// scaled dimensions in grid coordinate-space:
	Property<Double> width = new Property<Double>(PROPERTY_WIDTH, 0d, Double.class);
	Property<Double> height = new Property<Double>(PROPERTY_HEIGHT, 0d, Double.class);

	Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, 0, Integer.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Integer r) {
			r = r % 4;
			if (rotations.getValue().equals(r)) return;
			getImageManager().rotatedImage = null;
			super.setValue(r);
		}
	};

	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<String> label;

	List<Point> cleared = new ArrayList<Point>();

	public MapImage(String label) {
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
	}

	public MapImage(byte[] b, String label) {
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
		bytes = b;
	}

	protected MapImage(int id, String label) {
		super(id);
		this.label = new Property<String>(PROPERTY_LABEL, false, label, String.class);
	}

	@Override
	public Order getDefaultOrder() {
		return Order.BOTTOM;
	}

	/* (non-Javadoc)
	 * @see server.MapRenderer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g, Point2D off) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;
		Point2D o = canvas.getDisplayCoordinates((int) off.getX(), (int) off.getY());
		g.translate(o.getX(), o.getY());

		Rectangle bounds = g.getClipBounds();
		//System.out.println("Clip = "+bounds);

		BufferedImage img = getImageManager().getImage();
		if (img != null) {
			Shape oldClip = g.getClip();
			// build the shape
			Area area = new Area(g.getClip());
			// using indexed loop instead of iterator to avoid concurrency issues
			for (int i = 0; i < cleared.size(); i++) {
				Point p = cleared.get(i);
				Point tl = canvas.getDisplayCoordinates(p.x, p.y);
				Point br = canvas.getDisplayCoordinates(p.x+1, p.y+1);
				area.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
			}
			g.setClip(area);

			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

			Point2D p = new Point2D.Double(width.getValue(),height.getValue());
			Point bottomRight = canvas.getDisplayCoordinates(p);
			Point offset = canvas.getDisplayCoordinates(new Point2D.Double(x.getValue(), y.getValue()));
			//System.out.println("Grid coordinates: ("+x+","+y+") x ("+p.getX()+","+p.getY()+")");
			//System.out.println("Display coordinates: "+offset+" x "+bottomRight);

			int left, right, top, bottom;
			left = (bounds.x - offset.x) * img.getWidth() / bottomRight.x;
			top = (bounds.y - offset.y) * img.getHeight() / bottomRight.y;
			right = (bounds.x + bounds.width - offset.x) * img.getWidth() / bottomRight.x;
			bottom = (bounds.y + bounds.height - offset.y) * img.getHeight() / bottomRight.y;

//			g.drawImage(img, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
//					left, top, right, bottom, new Color(255,255,255,0), null);
			g.drawImage(img, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
					left, top, right, bottom, null);

			g.setComposite(c);
			g.setClip(oldClip);
		}
		g.translate(-o.getX(), -o.getY());
	}

	public Dimension getImageSize() {
		BufferedImage img = getImageManager().getImage();
		return new Dimension(img.getWidth(), img.getHeight());
	}

	/**
	 * 
	 * @return array of the points defining the centres of the cubes
	 */
	public Point[] getCells() {
		Point[] ps = new Point[cleared.size()];
		for (int i = 0; i < ps.length; i++) {
			ps[i] = new Point(cleared.get(i));
		}
		return ps;
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Image ("+getID()+")";
		return "Image ("+label+")";
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_CLEARCELL)) {
			setCleared((Point)value, true);
		} else if (property.equals(PROPERTY_UNCLEARCELL)) {
			setCleared((Point)value, false);
		} else if (property.equals(PROPERTY_IMAGE)) {
			bytes = (byte[]) value;
			getImageManager().sourceImage = null;
			getImageManager().rotatedImage = null;
			canvas.repaint();

		} else {
			super.setProperty(property, value);
		}
	}

	public boolean isCleared(Point p) {
		return cleared.contains(p);
	}

	public void setCleared(Point p, boolean clear) {
		if (!clear) {
			cleared.remove(p);
			canvas.repaint();
		} else if (!cleared.contains(p)) {
			cleared.add(p);
			canvas.repaint();
		}
	}

	private ImageManager getImageManager() {
		if (image == null) image = new ImageManager();
		return image;
	}

	private class ImageManager {
		BufferedImage sourceImage = null;
		BufferedImage rotatedImage = null;
		AnimationFrame[] frames = null;
		int index;
		Timer timer = null;

		void readImages() {
			if (bytes == null) return;
			try {
				if (timer != null) timer.stop();
				Animation a = null;
				try {
					// see if this is a serialized Animation
					ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
					ObjectInputStream in = new ObjectInputStream(stream);
					a = (Animation) in.readObject();
					in.close();
					frames = a.getFrames(new File("C:\\Users\\Steve\\Documents\\Animations"));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (StreamCorruptedException e) {
					// not a serialized animation. perhaps it's an image file
					ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
					ImageInputStream iis = ImageIO.createImageInputStream(stream);
					ImageReader reader = ImageIO.getImageReaders(iis).next();
					reader.setInput(iis);
					if (reader.getFormatName().equals("gif")) {
						frames = readGIF(reader);
					} else {
						BufferedImage img = reader.read(reader.getMinIndex());
						frames = new AnimationFrame[1];
						frames[0] = new AnimationFrame(img, 0);
					}
				}

				initImages();

				// we could now drop the bytes array at the cost of no longer being serializable
				// TODO strictly speaking we should calculate the bottom right corner and then use that to determine the size
				if (width.getValue() == 0 || height.getValue() == 0) {
					Point2D size = canvas.getRemoteGridCellCoords(sourceImage.getWidth(), sourceImage.getHeight());
					width.setValue(size.getX());
					height.setValue(size.getY());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		BufferedImage getImage() {
			if (sourceImage == null) {
				readImages();
			}

			if (rotatedImage == null) {
				createRotatedImage();
			}

			return rotatedImage;
		}

		private void initImages() {
			index = 0;
			sourceImage = frames[index].getImage();
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

		protected void createRotatedImage() {
			if (sourceImage != null) {
				AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
				Point p = new Point(sourceImage.getWidth(), sourceImage.getHeight());
				t.transform(p, p);	// transform to get new dimensions

				rotatedImage = new BufferedImage(Math.abs(p.x), Math.abs(p.y), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = (Graphics2D) rotatedImage.getGraphics();
				g2d.rotate(Math.toRadians(rotations.getValue() * 90), rotatedImage.getWidth() / 2, rotatedImage.getHeight() / 2);
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
	}
}

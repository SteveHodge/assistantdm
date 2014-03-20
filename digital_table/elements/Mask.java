package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.ImageMedia;
import digital_table.server.MediaManager;

// TODO we don't detect changes in the underlying image - need to fix the architecture
// TODO if we want to allow different sized masks we'll need some architecture changes...
// could have multi-level cache of the mask:
// each set of masks of a particular size are combined into a cached mask of the same size
// if there are many masks of a particular size we could have several cached masks for that size
// each cached mask is then transformed and combined into the combined mask
// when a mask changes visibility only need to rebuild it's particular cache and the combined mask - less drawing than the current method

public class Mask extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ADD_MASK = "add_mask";		// uri - write only
	public static final String PROPERTY_SHOW_MASK = "show_mask";	// index of mask - write only
	public static final String PROPERTY_HIDE_MASK = "hide_mask";	// index of mask - write only

	private List<MaskImage> masks = new ArrayList<MaskImage>();

	private static class MaskImage {
//		URI uri;
		transient ImageMedia image;
		boolean visible = true;
	}

	private transient BufferedImage combinedMask = null;

	private transient MapImage image = null;

	void setImageElement(MapImage mapImage) {
		image = mapImage;
	}

	@Override
	public String toString() {
		return "Mask";
	}

	@Override
	public void paint(Graphics2D g, Point2D off) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;
		if (parent != image) return;
		if (image.getVisibility() != Visibility.VISIBLE) return;

		Point2D o = canvas.getDisplayCoordinates((int) off.getX(), (int) off.getY());
		g.translate(o.getX(), o.getY());

		Shape oldClip = g.getClip();

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getVisibility() == Visibility.FADED ? 0.5f : 1f));

//		System.out.println("Image position = " + o + ", size = " + displaySize);

		// build the shape
//		Area area = new Area(g.getClip());
		// using indexed loop instead of iterator to avoid concurrency issues
//		for (int i = 0; i < cleared.size(); i++) {
//			Point p = cleared.get(i);
//			Point tl = canvas.getDisplayCoordinates(p.x, p.y);
//			Point br = canvas.getDisplayCoordinates(p.x + 1, p.y + 1);
//			area.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
//		}
//		g.setClip(area);

		Point offset = canvas.getDisplayCoordinates(image.location.getValue());
		g.drawImage(getMaskImage(), offset.x, offset.y, null);

		g.setComposite(c);
		g.setClip(oldClip);
		g.translate(-o.getX(), -o.getY());
		//long micros = (System.nanoTime() - startTime) / 1000;
		//logger.info("Painting complete for " + this + " in " + micros + "ms");
		//System.out.println("Image painting took "+micros+"ms");
	}

	// mask images are combined and then transformed
	synchronized BufferedImage getMaskImage() {
		if (combinedMask == null) {
//		Runtime rt = Runtime.getRuntime();
//		rt.gc();
//		rt.gc();
//		rt.gc();
//		long used = rt.totalMemory() - rt.freeMemory();
//		double usedMB = (double) used / (1024 * 1024);
//		System.out.println(String.format("Building mask... used mem = %.3f MB ", usedMB));

			BufferedImage temp = null;
			Graphics2D tempG = null;
			for (int i = 0; i < masks.size(); i++) {
				MaskImage m = masks.get(i);
				if (m.visible) {
					if (temp == null) {
						temp = new BufferedImage(m.image.getSourceWidth(), m.image.getSourceHeight(), BufferedImage.TYPE_INT_ARGB);
						tempG = temp.createGraphics();
					}
					tempG.drawImage(m.image.getImage(), 0, 0, null);
				}
			}
			if (tempG != null) tempG.dispose();

			if (temp != null) {
				// update the image transform. this needs to be done on every repaint as the grid size may have changed
				AffineTransform transform = image.getTransform(temp.getWidth(), temp.getHeight());
				combinedMask = ImageMedia.getTransformedImage(temp, transform);
			} else {
				Dimension d = canvas.getDisplayDimension(image.width.getValue(), image.height.getValue());
				combinedMask = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
			}

//		rt.gc();
//		rt.gc();
//		rt.gc();
//		used = rt.totalMemory() - rt.freeMemory();
//		usedMB = (double) used / (1024 * 1024);
//		System.out.println(String.format("Built mask... used mem = %.3f MB ", usedMB));
		}
		return combinedMask;
	}

	private synchronized void clearMask() {
		combinedMask = null;
	}

	private void addMask(URI uri) {
		MaskImage m = new MaskImage();
//		m.uri = uri;
		m.image = MediaManager.INSTANCE.getImageMedia(canvas, uri);
		masks.add(m);
		clearMask();
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_ADD_MASK)) {
			addMask((URI) value);
		} else if (property.equals(PROPERTY_SHOW_MASK)) {
			masks.get((Integer) value).visible = true;
			clearMask();
			canvas.repaint();
		} else if (property.equals(PROPERTY_HIDE_MASK)) {
			masks.get((Integer) value).visible = false;
			clearMask();
			canvas.repaint();
		} else {
			super.setProperty(property, value);
		}
	}
}

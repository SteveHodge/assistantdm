package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.ImageMedia;
import digital_table.server.MediaManager;

public class MaskedImage extends MapImage {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ADD_MASK = "add_mask";		// uri
	public static final String PROPERTY_SHOW_MASK = "show_mask";	// index of mask
	public static final String PROPERTY_HIDE_MASK = "hide_mask";	// index of mask
	public static final String PROPERTY_MASK_VISIBILITY = "mask_visibility";	// Visibility

	private Property<Visibility> maskVisibility = new Property<Visibility>(PROPERTY_MASK_VISIBILITY, true, Visibility.VISIBLE, Visibility.class);

	private List<Mask> masks = new ArrayList<Mask>();

	private static class Mask {
//		URI uri;
		transient ImageMedia image;
		boolean visible = true;
	}

	private transient BufferedImage combinedMask = null;

	public MaskedImage(String label) {
		super(label);
	}

	@Override
	public void paint(Graphics2D g, Point2D off) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;
		if (image.getImage() == null) return;
		//long startTime = System.nanoTime();
		Point2D o = canvas.getDisplayCoordinates((int) off.getX(), (int) off.getY());
		g.translate(o.getX(), o.getY());

		Shape oldClip = g.getClip();

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

		Point offset = canvas.getDisplayCoordinates(new Point2D.Double(x.getValue(), y.getValue()));
		// update the image transform. this needs to be done on every repaint as the grid size may have changed
		AffineTransform transform = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
		double w, h;
		if (rotations.getValue() % 2 == 0) {
			w = width.getValue();
			h = height.getValue();
		} else {
			w = height.getValue();
			h = width.getValue();
		}
		Dimension displaySize = canvas.getDisplayDimension(w, h);
		transform.scale(displaySize.getWidth() / image.getSourceWidth(), displaySize.getHeight() / image.getSourceHeight());
		image.setTransform(transform);

		System.out.println("Mask visibililty = " + maskVisibility.getValue());

		// masks - build the combined mask image before drawing the underlying map image so we don't accidentally show it revealed
		// XXX if we get any flashes of the unmasked image then we'll need to combine these offscreen first
		if (combinedMask == null && getVisibility() == Visibility.VISIBLE && maskVisibility.getValue() != Visibility.HIDDEN) {
//			Runtime rt = Runtime.getRuntime();
//			rt.gc();
//			rt.gc();
//			rt.gc();
//			long used = rt.totalMemory() - rt.freeMemory();
//			double usedMB = (double) used / (1024 * 1024);
//			System.out.println(String.format("Building mask... used mem = %.3f MB ", usedMB));

			BufferedImage bgImg = image.getImage();
			combinedMask = new BufferedImage(bgImg.getWidth(), bgImg.getHeight(), bgImg.getType());
			Graphics2D maskG = combinedMask.createGraphics();
			for (int i = 0; i < masks.size(); i++) {
				Mask m = masks.get(i);
				if (m.visible) {
					m.image.setTransform(transform);
					maskG.drawImage(m.image.getImage(), 0, 0, null);
					m.image.setTransform(null);			// reset the transform to clear any cached images
				}
			}
			maskG.dispose();

//			rt.gc();
//			rt.gc();
//			rt.gc();
//			used = rt.totalMemory() - rt.freeMemory();
//			usedMB = (double) used / (1024 * 1024);
//			System.out.println(String.format("Built mask... used mem = %.3f MB ", usedMB));
		}

		g.drawImage(image.getImage(), offset.x, offset.y, null);

		if (getVisibility() == Visibility.VISIBLE && maskVisibility.getValue() != Visibility.HIDDEN) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (maskVisibility.getValue() == Visibility.FADED ? 0.5f : 1f)));

			// build the shape
			Area area = new Area(g.getClip());
			// using indexed loop instead of iterator to avoid concurrency issues
			for (int i = 0; i < cleared.size(); i++) {
				Point p = cleared.get(i);
				Point tl = canvas.getDisplayCoordinates(p.x, p.y);
				Point br = canvas.getDisplayCoordinates(p.x + 1, p.y + 1);
				area.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
			}
			g.setClip(area);

			g.drawImage(combinedMask, offset.x, offset.y, null);
		}

		// border
		if (border.getValue()) {
			g.setColor(Color.RED);
			g.drawRect(offset.x, offset.y, image.getImage().getWidth(), image.getImage().getHeight());
		}

		g.setComposite(c);
		g.setClip(oldClip);
		g.translate(-o.getX(), -o.getY());
		//long micros = (System.nanoTime() - startTime) / 1000;
		//logger.info("Painting complete for " + this + " in " + micros + "ms");
		//System.out.println("Image painting took "+micros+"ms");
	}

	private void addMask(URI uri) {
		Mask m = new Mask();
//		m.uri = uri;
		m.image = MediaManager.INSTANCE.getImageMedia(canvas, uri);
		masks.add(m);
		combinedMask = null;
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_ADD_MASK)) {
			addMask((URI) value);
		} else if (property.equals(PROPERTY_SHOW_MASK)) {
			masks.get((Integer) value).visible = true;
			combinedMask = null;
			canvas.repaint();
		} else if (property.equals(PROPERTY_HIDE_MASK)) {
			masks.get((Integer) value).visible = false;
			combinedMask = null;
			canvas.repaint();
		} else {
			super.setProperty(property, value);
		}
	}
}

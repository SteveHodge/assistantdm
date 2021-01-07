package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.ImageMedia;
import digital_table.server.MapCanvas.Order;
import digital_table.server.MeasurementLog;
import digital_table.server.MediaManager;

// TODO we don't detect changes in the underlying image - need to fix the architecture
// TODO (check this, looks like it might be done) should detect changes in image location - if the fractional part of the location changes then any cleared cells will be incorrectly aligned
// TODO if we want to allow different sized masks we'll need some architecture changes...
// could have multi-level cache of the mask:
// each set of masks of a particular size are combined into a cached mask of the same size
// if there are many masks of a particular size we could have several cached masks for that size
// each cached mask is then transformed and combined into the combined mask
// when a mask changes visibility only need to rebuild it's particular cache and the combined mask - less drawing than the current method

public class Mask extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ADD_MASK = "add_mask";		// uri - write only
	public static final String PROPERTY_SHOW_MASK = "show_mask";	// set specified mask/image to visible. index of mask - write only
	public static final String PROPERTY_HIDE_MASK = "hide_mask";	// set specified mask/image to hidden. index of mask - write only
	public final static String PROPERTY_CLEARCELL = "clear";		// Point - when this property is set the specified cell will be cleared
	public final static String PROPERTY_UNCLEARCELL = "unclear";	// Point - when this property is set the specified cell will be shown again
	public final static String PROPERTY_SET_TYPE_MASK = "mask_type";	// set specified mask/image to be a mask. index of mask - write only
	public final static String PROPERTY_SET_TYPE_IMAGE = "image_type";	// set specified mask/image to be an image. index of mask - write only
	public final static String PROPERTY_PROMOTE_MASK = "promote_mask";		// move specified mask/image up one in the list. index of mask - write only
	public final static String PROPERTY_DEMOTE_MASK = "demote_mask";		// move specified mask/image down one in the list. index of mask - write only
	public final static String PROPERTY_REMOVE_MASK = "remove_mask";	// remove the specified mask/image. index of mask - write only

	private List<MaskImage> masks = new ArrayList<>();

	private static class MaskImage {
//		URI uri;
		transient ImageMedia image;
		boolean visible = true;
		boolean isImage = false;	// false = mask, true = image
		int xOffset;	// offset from origin of base image
		int yOffset;	// offset from origin of base image
	}

	private transient MapImage image;
	private transient BufferedImage combinedMask;
	private transient BufferedImage combinedImage;
	private transient AffineTransform transform;
	private double xFraction, yFraction;	// fractional components of parent's location - used to detect when our cleared cells need redrawing due to parent movement

	private List<Point> cleared = new ArrayList<>();

	void setImageElement(MapImage mapImage) {
		image = mapImage;
	}

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	@Override
	public MeasurementLog getMemoryUsage() {
		List<MeasurementLog> logs = new ArrayList<>();
		if (combinedMask != null) {
			MeasurementLog m = new MeasurementLog("Combined Mask", 0);
			m.last = ImageMedia.getMemoryUsage(combinedMask);
			logs.add(m);
		}
		if (combinedImage != null) {
			MeasurementLog m = new MeasurementLog("Combined Image", 0);
			m.last = ImageMedia.getMemoryUsage(combinedImage);
			logs.add(m);
		}
		for (MaskImage mask : masks) {
			if (mask.image != null) {
				logs.add(mask.image.getMemoryUsage());
			}
		}
		String name = "";
		MeasurementLog m = new MeasurementLog("Mask" + name, id, logs.size());
		logs.toArray(m.components);
		m.updateTotal();
		return m;
	}

	@Override
	public String toString() {
		return "Mask";
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;
		if (parent != image) return;
		if (image.getVisibility() != Visibility.VISIBLE) return;

		Point2D o = canvas.convertGridCoordsToDisplay(image.translate(canvas.getElementOrigin(image)));
		g.translate(o.getX(), o.getY());

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getVisibility() == Visibility.FADED ? 0.5f : 1f));

		g.drawImage(getMaskImage(), 0, 0, null);

		g.setComposite(c);
		g.translate(-o.getX(), -o.getY());
	}

	@Override
	public String getIDString() {
		return "Mask";
	}

	synchronized BufferedImage getMaskImage() {
		combinedMask = getMaskImage(false, combinedMask);
		return combinedMask;
	}

	synchronized BufferedImage getCombinedImage() {
		combinedImage = getMaskImage(true, combinedImage);
		return combinedImage;
	}

	// mask images are combined and then transformed
	// if images == false then gets the combined mask
	// if images == true then gets the combined images
	private synchronized BufferedImage getMaskImage(boolean images, BufferedImage combinedMask) {
		// check if the parent image has moved relative to the grid
		// (i.e. the fractional component of the location has changed)
		// if so then we'll need to regenerate the mask if we have cleared cells
		Point2D imgLoc = image.location.getValue();
		if (imgLoc.getX() - (int) imgLoc.getX() != xFraction
				|| imgLoc.getY() - (int) imgLoc.getY() != yFraction) {
			xFraction = imgLoc.getX() - (int) imgLoc.getX();
			yFraction = imgLoc.getY() - (int) imgLoc.getY();
			if (cleared.size() > 0) combinedMask = null;
		}

		AffineTransform trans = null;
		BufferedImage temp = null;
		Graphics2D tempG = null;
		for (int i = 0; i < masks.size(); i++) {
			MaskImage m = masks.get(i);
			if (m.visible && m.isImage == images) {
				if (temp == null) {
					temp = new BufferedImage(image.image.getSourceWidth(), image.image.getSourceHeight(), BufferedImage.TYPE_INT_ARGB);
					tempG = temp.createGraphics();
					trans = image.getTransform(temp.getWidth(), temp.getHeight());
					if (combinedMask != null && trans.equals(transform)) return combinedMask;	// transform is unchanged and we already have a mask
					transform = trans;
				}
				tempG.drawImage(m.image.getImage(), m.xOffset, m.yOffset, null);
			}
		}
		if (tempG != null) tempG.dispose();

		if (temp != null) {
			AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
			Rectangle2D bounds = ImageMedia.getBounds(transform, temp.getWidth(), temp.getHeight());
			combinedMask = new BufferedImage((int) Math.ceil(bounds.getWidth()), (int) Math.ceil(bounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
			//System.out.println("New image size: " + transformed[index].getWidth() + "x" + transformed[index].getHeight());
			Graphics2D g = (Graphics2D) combinedMask.getGraphics();

			// clip any cleared cells
			Area area = new Area(new Rectangle2D.Double(0, 0, bounds.getWidth(), bounds.getHeight()));
			// cleared cell coordinates are relative to the parent image, get the offset
			// using indexed loop instead of iterator to avoid concurrency issues
			for (int i = 0; i < cleared.size(); i++) {
				Point p = cleared.get(i);
				Point2D cellCoord = new Point2D.Double(p.x - image.getX(), p.y - image.getY());
				Point tl = canvas.convertGridCoordsToDisplay(cellCoord);
				cellCoord.setLocation(cellCoord.getX() + 1, cellCoord.getY() + 1);
				Point br = canvas.convertGridCoordsToDisplay(cellCoord);
				area.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
			}
			g.setClip(area);

			g.drawImage(temp, op, (int) -bounds.getX(), (int) -bounds.getY());
			g.dispose();
		} else {
			Dimension d = canvas.getDisplayDimension(image.width.getValue(), image.height.getValue());
			combinedMask = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		}
		return combinedMask;
	}

	private void clearMask() {
		synchronized (this) {
			combinedMask = null;
			combinedImage = null;
		}
		canvas.repaint();
	}

	private void addMask(URI uri) {
		MaskImage m = new MaskImage();
//		m.uri = uri;

		ImageMedia srcImg = MediaManager.INSTANCE.getImageMedia(canvas, uri);
		BufferedImage src = srcImg.getImage();
		if (srcImg.getFrameCount() != 1) {
			m.image = srcImg;
		} else {
			// trim mask
			WritableRaster raster = src.getRaster();
			int[] row = null;
			int minX = raster.getWidth() * 4;
			int maxX = -1;
			int minY = raster.getHeight();
			int maxY = -1;
			for (int y = 0; y < raster.getHeight(); y++) {
				row = raster.getPixels(raster.getMinX(), y + raster.getMinY(), raster.getWidth(), 1, row);
				int rowMinX = raster.getWidth() * 4;
				int rowMaxX = -1;
				for (int x = 0; x < row.length; x += 4) {
					int val = row[x] << 24 | row[x + 1] << 16 | row[x + 2] << 8 | row[x + 3];
					if (val != 0xffffff00) {
						if (rowMaxX == -1) rowMinX = x;
						if (x > rowMaxX) rowMaxX = x;
						if (maxY == -1) minY = y;
						if (y > maxY) maxY = y;
					}
				}
				if (rowMinX < minX) minX = rowMinX;
				if (rowMaxX > maxX) maxX = rowMaxX;
			}
			int trimmedWidth = (maxX - minX) / 4 + 1;
			int trimmedHeight = maxY - minY + 1;
			m.xOffset = minX / 4;
			m.yOffset = minY;

			BufferedImage dest = new BufferedImage(trimmedWidth, trimmedHeight, src.getType());
			Graphics g = dest.getGraphics();
			g.drawImage(src, 0, 0, trimmedWidth, trimmedHeight, m.xOffset, m.yOffset, m.xOffset + trimmedWidth, m.yOffset + trimmedHeight, null);
			g.dispose();
			m.image = ImageMedia.createImageMedia(canvas, dest);
		}
		masks.add(m);
		clearMask();
	}

	/**
	 *
	 * @return array of the points defining the cells that have been removed from the mask
	 */
	public Point[] getCells() {
		Point[] ps = new Point[cleared.size()];
		for (int i = 0; i < ps.length; i++) {
			ps[i] = new Point(cleared.get(i));
		}
		return ps;
	}

	public boolean isCleared(Point p) {
		return cleared.contains(p);
	}

	public void setCleared(Point p, boolean clear) {
		if (!clear) {
			cleared.remove(p);
			clearMask();
		} else if (!cleared.contains(p)) {
			cleared.add(p);
			clearMask();
		}
	}

	// TODO index checking
	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_ADD_MASK)) {
			addMask((URI) value);
		} else if (property.equals(PROPERTY_SHOW_MASK)) {
			masks.get((Integer) value).visible = true;
			clearMask();
		} else if (property.equals(PROPERTY_HIDE_MASK)) {
			masks.get((Integer) value).visible = false;
			clearMask();
		} else if (property.equals(PROPERTY_CLEARCELL)) {
			setCleared((Point) value, true);
		} else if (property.equals(PROPERTY_UNCLEARCELL)) {
			setCleared((Point) value, false);
		} else if (property.equals(PROPERTY_SET_TYPE_IMAGE)) {
			masks.get((Integer) value).isImage = true;
			clearMask();
		} else if (property.equals(PROPERTY_SET_TYPE_MASK)) {
			masks.get((Integer) value).isImage = false;
			clearMask();
		} else if (property.equals(PROPERTY_REMOVE_MASK)) {
			int i = (Integer) value;
			if (i >= 0 && masks.size() > i) {
				masks.remove(i);
				clearMask();
			}
		} else if (property.equals(PROPERTY_PROMOTE_MASK)) {
			int i = (Integer) value;
			if (i > 0) {
				MaskImage m = masks.remove(i);
				masks.add(i - 1, m);
				clearMask();
			}
		} else if (property.equals(PROPERTY_DEMOTE_MASK)) {
			int i = (Integer) value;
			if (i < masks.size() - 1) {
				MaskImage m = masks.remove(i);
				masks.add(i + 1, m);
				clearMask();
			}
		} else {
			super.setProperty(property, value);
		}
	}
}

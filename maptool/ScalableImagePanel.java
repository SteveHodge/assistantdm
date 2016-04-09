package maptool;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

/*
 * A component that displays an automatically scaling image. Aspect ratio is always maintained
 * A loading image (generally a animated throber) can be supplied.
 * Scaling is performed so that something is displayed as quickly as possible. When a resize is necessary
 * two images are prepared - one scaled using SCALE_FAST and one scaled using SCALE_SMOOTH. If the fast
 * one finished first then it is displayed until the smooth one is done. I'm not sure this is worthwhile
 * in practice as the smooth scaler isn't much slower than the fast scaler.
 */
//TODO might be useful to have a mode where the previous scaled image is retained for quick restoration
public class ScalableImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	// the source image
	protected Image sourceImage = null;
	protected int sourceWidth = 0;
	protected int sourceHeight = 0;

	// the currently displayed image
	protected Image displayImage = null;
	protected int displayWidth = 0;
	protected int displayHeight = 0;

	// images in the process of being scaled. these are keep as null when no scaling is occurring
	protected Image fastScaled = null;
	protected Image smoothScaled = null;
	protected int newWidth = 0;
	protected int newHeight = 0;

	protected Image loadingImage = null;
	protected boolean loading = true;

	protected boolean allowEnlarge = true;

	protected float alpha = 1.0f;

	public ScalableImagePanel(Image img, Image loading) {
		loadingImage = loading;
		if (img != null) setImage(img);
	}

	public ScalableImagePanel(Image img) {
		if (img != null) setImage(img);
	}

	public void setAlpha(float a) {
		if (alpha == a) return;
		alpha = a;
		repaint();
	}

	public int getImageWidth() {
		return displayWidth;
	}

	public int getImageHeight() {
		return displayHeight;
	}

	public int getSourceImageWidth() {
		return sourceWidth;
	}

	public int getSourceImageHeight() {
		return sourceHeight;
	}

	public void scaleToFit(int availWidth, int availHeight) {
		//System.out.println("scaleImage: width(avail,display) = "+availWidth+", "+displayWidth
		//		+", height (avail,display) = "+availHeight+", "+displayHeight);
		if (displayWidth != availWidth || displayHeight != availHeight) {
			if ((availWidth == sourceWidth && availHeight >= sourceHeight)
					|| (availWidth >= sourceWidth && availHeight == sourceHeight)
					|| (!allowEnlarge && availWidth > sourceWidth && availHeight > sourceHeight)) {
				// either the source image exactly fits or the available space is larger but we're not
				// allowed to enlarge the image
				displayImage = sourceImage;
				displayWidth = sourceWidth;
				displayHeight = sourceHeight;
				if (fastScaled != null) {
					fastScaled.flush();
					fastScaled = null;
				}
				if (smoothScaled != null) {
					smoothScaled.flush();
					smoothScaled = null;
				}
				newWidth = sourceWidth;
				newHeight = sourceHeight;
				repaint();

			} else {
				Dimension scaledSize = getScaledSize(availWidth, availHeight);
				//System.out.println("Scaled size = "+scaledSize);
				//System.out.println("newWidth = "+newWidth + ", "+"newHeight = "+newHeight);
				if (scaledSize.width != newWidth || scaledSize.height != newHeight) {
					//System.out.println("Scaling to "+scaledSize);
					newWidth = scaledSize.width;
					newHeight = scaledSize.height;
					if (fastScaled != null) fastScaled.flush();
					fastScaled = sourceImage.getScaledInstance(scaledSize.width, scaledSize.height, Image.SCALE_FAST);
					prepareImage(fastScaled, this);	// start scaling the image
					if (smoothScaled != null) smoothScaled.flush();
					smoothScaled = sourceImage.getScaledInstance(scaledSize.width, scaledSize.height, Image.SCALE_SMOOTH);
					prepareImage(smoothScaled, this);	// start scaling the image
				}
			}
			setPreferredSize(new Dimension(displayWidth, displayHeight));
			revalidate();
		}
	}

	public double getScale() {
		if (displayWidth == 0) return 0;
		return ((double) displayWidth) / sourceWidth;
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		//System.out.println("Painting image: " + sourceImage + ": " + getSize());

		Graphics2D g = (Graphics2D) graphics;
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		if (isOpaque()) { //paint background
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		if (sourceImage == null) return;

		Insets insets = getInsets();
//		int availWidth = getWidth() - insets.right - insets.left;
//		int availHeight = getHeight() - insets.right - insets.left;
//		scaleImage(availWidth, availHeight);

		if (loadingImage != null && loading) {
			g.setColor(Color.GRAY);
			g.drawRect(insets.left, insets.top, newWidth, newHeight);
			int x = insets.left + (newWidth - loadingImage.getWidth(null)) / 2;
			int y = insets.top + (newHeight - loadingImage.getHeight(null)) / 2;
			g.drawImage(loadingImage, x, y, this);
		} else {
			int x = insets.left;
			int y = insets.top;

			if (newWidth > displayWidth) {
				// we're scaling the width up - for the moment we'll center the current image
				x += (newWidth - displayWidth) / 2;
			}
			if (newHeight > displayHeight) {
				// we're scaling the height up - for the moment we'll center the current image
				y += (newHeight - displayHeight) / 2;
			}

			Rectangle clipRect = new Rectangle();
			g.getClipBounds(clipRect);
			Rectangle imageRect = new Rectangle(x, y, displayWidth, displayHeight);
			if (imageRect.intersects(clipRect)) {
				g.drawImage(displayImage, x, y, this);
			}
		}
		g.setComposite(c);
	}

	public void scale(double s) {
		scaleToFit((int) (sourceWidth * s), (int) (sourceHeight * s));
	}

// TODO may need synchronisation in here
	@Override
	public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height) {
//			String image = "unknown";
//			//if (img == loadingImage) image = "loading";
//			if (img == displayImage) image = "display";
//			if (img == fastScaled) image = "fast";
//			if (img == smoothScaled) image = "smooth";
//			if (img == sourceImage) image = "source";
//			if (img != loadingImage && flags != 8) System.out.println("imageUpdate for '" + image + "' (" + img + "), flags = " + flags + ", at " + System.currentTimeMillis());

		if (img == sourceImage) {
			if ((flags & ImageObserver.WIDTH) != 0) {
				sourceWidth = width;
			}
			if ((flags & ImageObserver.HEIGHT) != 0) {
				sourceHeight = height;
			}
		}
		if ((flags & ImageObserver.ALLBITS) != 0 || (flags & ImageObserver.FRAMEBITS) != 0) {
			if (img == fastScaled && smoothScaled != null) {
				// the fastScaled image is done. if it is still relevant (i.e. smoothScaled is not null) then
				// we'll update the displayed image to this one.
				//System.out.println("Fast Scaling done "+System.currentTimeMillis());
				if (displayImage != sourceImage) displayImage.flush();
				displayImage = fastScaled;
				displayWidth = newWidth;
				displayHeight = newHeight;
				fastScaled = null;
				if (loading) loading = false;
				repaint();	// not sure if this is necessary
			}
			if (img == smoothScaled) {
				// the smoothScaled image is done. replace the displayed image and cleanup fastScaled is there is
				// one
				//System.out.println("Smooth Scaling done "+System.currentTimeMillis());
				if (displayImage != sourceImage) displayImage.flush();
				displayImage = smoothScaled;
				displayWidth = newWidth;
				displayHeight = newHeight;
				if (fastScaled != null) {
					fastScaled.flush();
					fastScaled = null;
				}
				smoothScaled = null;
				if (loading) loading = false;
				repaint();
			}
		}
		return super.imageUpdate(img, flags, x, y, width, height);
	}

	public void setAllowEnargements(boolean f) {
		allowEnlarge = f;
	}

// returns the current source Image, if any.
	public Image getImage() {
		return sourceImage;
	}

	public void setImage(Image img) {
		sourceImage = img;
		if (img == null) {
			sourceWidth = 0;
			sourceHeight = 0;
		} else {
			sourceWidth = img.getWidth(this);
			sourceHeight = img.getHeight(this);
		}
		displayImage = sourceImage;
		displayWidth = sourceWidth;
		displayHeight = sourceHeight;
		newWidth = sourceWidth;
		newHeight = sourceHeight;
		//	setMinimumSize(size);
		setPreferredSize(new Dimension(displayWidth, displayHeight));
		repaint();
	}

// returns the largest Dimension with the same aspect ratio as the source image
// that fits in availWidth x availHeight
	public Dimension getScaledSize(int availWidth, int availHeight) {
		Dimension scaledSize = new Dimension();
		int prefWidth = sourceWidth * availHeight / sourceHeight;
		if (prefWidth <= availWidth) {
			scaledSize.setSize(prefWidth, availHeight);
		} else {
			scaledSize.setSize(availWidth, sourceHeight * availWidth / sourceWidth);
		}
		if (scaledSize.width < 1) scaledSize.width = 1;
		if (scaledSize.height < 1) scaledSize.height = 1;
		return scaledSize;
	}

} // class ImagePanel

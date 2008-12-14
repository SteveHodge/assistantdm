package swing;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

//TODO this should probably be in a library
// Currently used by: CanonCDSDK, AssistantDM
//See also version in FFEGalaxy
public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	protected BufferedImage image = null;
	protected Image scaledImage = null;
	protected Dimension scaledSize = null;
	protected boolean allowEnlarge = true;

public ImagePanel(BufferedImage img) {
	if (img != null) setImage(img);
}

protected void paintComponent(Graphics g) {
	if (isOpaque()) { //paint background
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	if (image == null) return;

	Insets insets = getInsets();
	int availWidth = getWidth() - insets.right - insets.left;
	int availHeight = getHeight() - insets.right - insets.left;
	if (scaledSize.width != availWidth || scaledSize.height != availHeight) {
		if (!allowEnlarge && availWidth >= scaledSize.width && availHeight >= scaledSize.height) {
			scaledImage = image;
			scaledSize.setSize(image.getWidth(), image.getHeight());
		} else {
			scaledImage = getScaledImage(image, availWidth, availHeight);
		}
	}

	Rectangle clipRect = new Rectangle();
	g.getClipBounds(clipRect);
	Rectangle imageRect = new Rectangle(insets.left, insets.top, scaledSize.width, scaledSize.height);
	if (imageRect.intersects(clipRect)) {
		// TODO should we also check for width or height = -1 (meaning that the image isn't ready)?
		g.drawImage(scaledImage,imageRect.x,imageRect.y,null);
	}
}

public void setAllowEnargements(boolean f) {
	allowEnlarge = f;
}

public void setImage(BufferedImage img) {
	image = img;
	if (img != null) {
		scaledImage = image;
		scaledSize = new Dimension(image.getWidth(null), image.getHeight(null));
		//	setMinimumSize(size);
		setPreferredSize(scaledSize);
	}
	repaint();
}

public static Image getScaledImage(BufferedImage source, int w, int h) {
	int imgWidth = source.getWidth();
	int imgHeight = source.getHeight();
		
	Dimension scaledSize = new Dimension();
	int prefWidth = imgWidth*h/imgHeight;
	if (prefWidth <= w) {
		scaledSize.setSize(prefWidth, h);
	} else {
		scaledSize.setSize(w, imgHeight*w/imgWidth);
	}
	if (scaledSize.width < 1) scaledSize.width = 1;
	if (scaledSize.height < 1) scaledSize.height = 1;

	Image i = source.getScaledInstance(scaledSize.width,scaledSize.height,Image.SCALE_SMOOTH);
	return i;
}

} // class ImagePanel

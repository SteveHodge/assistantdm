package tilemapper;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

/* Represents a tile in a set: provides the image to use in each orientation,
 * and the edge definition.
 */

public class Tile {
	public String tileSet = null;
	public File hiresFile = null;	// full resolution file
	public File file = null;	// low resolution file if any, or full res file
	public BufferedImage image = null;
	boolean mirrored = false;
	Tile mirror = null;
	public int width = 6;
	int height = 6;
	Set<String> styles = new HashSet<String>();

	public Tile(int w, int h) {
		width = w;
		height = h;
	}

	public int getWidth(int orientation) {
		if (orientation % 2 == 0) return width;
		return height;
	}

	public int getHeight(int orientation) {
		if (orientation % 2 == 0) return height;
		return width;
	}

	// assumes unchanged orientation
	public void setHeight(int h) {
		height = h;
	}

	public void setWidth(int w) {
		width = w;
	}


	@Override
	public String toString() {
		String s = "tile "+file+" from "+tileSet+". ";
		s += "image: " + image.getWidth() + "x" + image.getHeight();
		return s;
	}

	public Tile getMirror() {
		if (mirror == null) {
			mirror = new Tile(width, height);
			mirror.tileSet = tileSet;
			mirror.file = file;
			mirror.image = flipImageHorizontal(image);
			mirror.mirror = this;
			mirror.mirrored = !mirrored;
		}
		return mirror;
	}

	// orient is the number of clockwise 90 degree rotations the tile should have been subjected to
	// will attempt to use the hi-res file if the requested dpi is higher than the current cached image
	public Image getTileImage(double dpi, int orient) {
		BufferedImage img = null;
		if (image == null || (dpi * width > image.getWidth() && hiresFile != null)) {
			// try to use the hires file
			try {
				img = ImageIO.read(hiresFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (img == null) img = image;

		if (img != null) {
			for (int i = 0; i < orient; i++) {
				img = rotateImage(img);	// should cache these
			}
			if ((int) (dpi * getWidth(orient)) == img.getWidth()) return img;
			return img.getScaledInstance((int) (dpi * getWidth(orient)), (int) (dpi * getHeight(orient)), Image.SCALE_SMOOTH);
		}
		return img;
	}

	// This implementation read the rows of the source image one at a time and writes those rows
	// as columns in the dest image. This works since the intermediate format of a row of data
	// from the source is exactly what we want for the destination column.
	public static BufferedImage rotateImage(BufferedImage src) {
		BufferedImage dest = new BufferedImage(src.getHeight(),src.getWidth(),src.getType());
		Raster srcR = src.getData();
		WritableRaster destR = dest.getRaster();
		Object data = null;
		for (int y = srcR.getMinY(); y < srcR.getHeight()+srcR.getMinY(); y++) {
			data = srcR.getDataElements(srcR.getMinX(), y, srcR.getWidth(), 1, data);
			destR.setDataElements(destR.getMinX()+destR.getWidth()-1-y, destR.getMinY(), 1, destR.getHeight(), data);
			//Naive implementation that copys pixels one at a time:
			//for (int x = srcR.getMinX(); x < srcR.getWidth()+srcR.getMinX(); x++) {
			//	data = srcR.getDataElements(x, y, data);
			//	destR.setDataElements(src.getHeight()-y-1, x, data);
			//}
		}
		return dest;
	}

	public static BufferedImage flipImageHorizontal(BufferedImage src) {
		BufferedImage dest = new BufferedImage(src.getWidth(),src.getHeight(),src.getType());
		Raster srcR = src.getData();
		WritableRaster destR = dest.getRaster();
		Object data = null;
		for (int y = 0; y < srcR.getHeight(); y++) {
			for (int x = 0; x < srcR.getWidth(); x++) {
				//System.out.println("("+x+","+y+") -> ("+(destR.getWidth()-x-1)+","+y+")");
				data = srcR.getDataElements(x+srcR.getMinX(), y+srcR.getMinY(), data);
				destR.setDataElements(destR.getWidth()-x-1+destR.getMinX(), y+destR.getMinY(), data);
			}
		}
		return dest;
	}

	// node should be the Tile node in the DOM to parse. tileset is the description of the set this Tile belongs to.
	// dir is the directory that contains the images. style is the default style for any edges that don't have a style specified
	public static Tile parseDOM(Element node, File dir, String tileset, Set<String> style, int width, int height) {
		Tile t = new Tile(width, height);
		t.tileSet = tileset;
		t.hiresFile = new File(dir, node.getAttribute("file"));
		t.file = findLoResFile(dir, node.getAttribute("file"));
		try {
			t.image = ImageIO.read(t.file);
		} catch (IOException ex) {
			System.out.println("Failed to load image from "+t.file.getAbsolutePath()+": "+ex.getMessage());
		}
		String widthStr = node.getAttribute("width");
		t.width = width;
		if (widthStr.length()>0) t.width = Integer.parseInt(widthStr);
		String heightStr = node.getAttribute("height");
		t.height = height;
		if (heightStr.length()>0) t.height = Integer.parseInt(heightStr);
		String mirrorStr = node.getAttribute("mirrored");
		if (mirrorStr.equals("true")) t.mirrored = true;
		String styles = node.getAttribute("style");
		if (styles.length() > 0) {
			for (String s : styles.split(";")) {
				if (!s.equals("")) t.styles.add(s);
			}
		} else if (style != null) {
			t.styles.addAll(style);
		}
		return t;
	}

	// Tries to locate a small version of the file first, and falls back on the large image is no small version is found
	static File findLoResFile(File dir, String largefile) {
		String file = largefile;
		String[] types = { "jpg", "png" };
		if (file.contains(".")) {
			file = file.substring(0, file.indexOf('.'));
		}
		for (String type : types) {
			File f = new File(dir, "Small/" + file + "." + type);
			if (f.exists()) return f;
		}
		File f = new File(dir, largefile);
		if (f.exists()) return f;
		return null;

	}

	public String getXML(String indent, Set<String> defaultStyle, int setWidth, int setHeight) {
		StringBuilder s = new StringBuilder();
		s.append(indent).append("<Tile file=\"").append(file.getName());
		if (width != setWidth) s.append("\" width=\"").append(width);
		if (height != setHeight) s.append("\" height=\"").append(height);
		if (!defaultStyle.equals(styles)) s.append("\" style=\"").append(styles);
		if (mirrored) s.append("\" mirrored=\"true");
		s.append("\"/>");
		return s.toString();
	}
}

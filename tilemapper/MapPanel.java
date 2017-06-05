package tilemapper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//TODO perhaps reposition view when minRow/minCol changes to prevent auto-scrolling
//TODO consider going back to using component for the tiles

/*
 * Selection:
 * A single tile can be selected.
 * Clicking on a tile selects the tile.
 * Clicking on the background unselects the selected tile (if any).
 * The selected tile can be deleted using deleteSelected().
 *
 * Dragging:
 * If the drag occurs on the selected tile then that tile is removed and a drag is set up to move it to a new location.
 * Otherwise dragging drags the map if it is in a containing JViewPort.
 */

@SuppressWarnings("serial")
public class MapPanel extends JPanel implements Scrollable, DragTarget {
	int gridSize = 16;	// size of grid to snap to
	int minCol = 0, maxCol = 9, minRow = 0, maxRow = 9;

	ArrayList<PlacedTile> tiles = new ArrayList<PlacedTile>();
	PlacedTile selected = null;
	DraggableTile dragTile;
	private ComponentDragger dragger;

	class PlacedTile {
		int x, y;
		Tile tile;
		Image image = null;
		int orientation;

		public PlacedTile(Tile t, int x, int y, int o) {
			tile = t;
			this.x = x;
			this.y = y;
			orientation = o;
		}

		public int getWidth() {
			return tile.getWidth(orientation);
		}

		public int getHeight() {
			return tile.getHeight(orientation);
		}

		public int getPixelX() {
			return (x-minCol)*gridSize;
		}

		public int getPixelY() {
			return (y-minRow)*gridSize;
		}
	}

	public MapPanel(ComponentDragger dragger) {
		this.dragger = dragger;
		Dimension dim = new Dimension((maxCol-minCol+1)*gridSize, (maxRow-minRow+1)*gridSize);
		setMinimumSize(dim);
		setPreferredSize(dim);
		setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		setBackground(Color.WHITE);
		setOpaque(true);
		setLayout(null);
		addMouseListener(mapMouseListener);
		addMouseMotionListener(mapMouseListener);
	}

	MouseInputAdapter mapMouseListener =
			new MouseInputAdapter() {
		// mouseClicked is used to detect tiles being selected
		@Override
		public void mouseClicked(MouseEvent e) {
			PlacedTile newselected = null;
			for (int i = tiles.size()-1; i >= 0; i--) {
				PlacedTile t = tiles.get(i);
				if (e.getX() >= t.getPixelX() && e.getY() >= t.getPixelY()
						&& e.getX() < t.getPixelX() + t.getWidth()*gridSize
						&& e.getY() < t.getPixelY() + t.getHeight()*gridSize) {
					newselected = t;
					break;
				}
			}
			setSelected(newselected);
		}

		// mouseDragged, mousePressed, mouseReleased are used to perform dragging of the map
		int xOff, yOff;
		Container c;
		boolean dragging = false;

		@Override
		public void mouseDragged(MouseEvent e) {
			c = MapPanel.this.getParent();
			if (c instanceof JViewport) {
				if (!dragging) {
					dragging = true;
					setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				}
				JViewport jv = (JViewport) c;
				Point p = jv.getViewPosition();
				int newX = p.x - (e.getX() - xOff);
				int newY = p.y - (e.getY() - yOff);

				int maxX = MapPanel.this.getWidth() - jv.getWidth();
				int maxY = MapPanel.this.getHeight() - jv.getHeight();
				if (newX < 0) newX = 0;
				if (newX > maxX) newX = maxX;
				if (newY < 0) newY = 0;
				if (newY > maxY) newY = maxY;

				jv.setViewPosition(new Point(newX, newY));
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			xOff = e.getX();
			yOff = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (dragging) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				dragging = false;
			}
		}
	};

	public void reset() {
		tiles = new ArrayList<PlacedTile>();
		repaint();
	}

	// TODO size of DraggableTile isn't quite right because it includes a border we're not painting
	// TODO this blocks drag onto the selected tile
	// TODO would be nice if rotation on selected tile also worked
	protected void setSelected(PlacedTile newselected) {
		if (newselected != selected) {
			if (selected != null) {
				// remove old dragTile
				remove(dragTile);
				dragger.unregisterSource(dragTile);
			}
			selected = newselected;
			if (selected != null) {
				// add new dragTile
				dragTile = new DraggableTile(selected.tile, gridSize) {
					@Override
					protected void paintComponent(Graphics graphics) {
					}

					@Override
					public Component getDragComponent() {
						// TODO this actually happens when the mouse is pressed instead of when the drag starts
						tiles.remove(selected);
						selected = null;
						MapPanel.this.repaint();
						return super.getDragComponent();
					}

					@Override
					public void dragFinished(boolean cancelled) {
						if (cancelled) {
							// drag was cancelled, put the tile back
							// TODO implement
						}
						MapPanel.this.remove(dragTile);
						dragger.unregisterSource(dragTile);
						super.dragFinished(cancelled);
					}
				};
				dragTile.setOrientation(selected.orientation);
				dragTile.setOpaque(false);
				dragger.registerSource(dragTile);
				add(dragTile);
				dragTile.setLocation(selected.getPixelX(), selected.getPixelY());
			}
			repaint();
		}
	}

	@Override
	public void setSize(Dimension newSize) {
		//System.out.println("setSize("+newSize+")");
		int newWidth = (int)Math.ceil(newSize.getWidth() / gridSize);
		int newHeight = (int)Math.ceil(newSize.getHeight() / gridSize);
		if (newWidth > (maxCol-minCol+1)) {
			maxCol = newWidth+minCol-1;
			repaint();
		}
		if (newHeight > (maxRow-minRow+1)) {
			maxRow = newHeight+minRow-1;
			repaint();
		}
		super.setSize(newSize);
		setPreferredSize(new Dimension((maxCol-minCol+1)*gridSize, (maxRow-minRow+1)*gridSize));
		//System.out.println("done setSize("+newSize+")");
	}

	public void deleteSelected() {
		System.out.println("deleteSelected " + selected);
		if (selected != null) {
			tiles.remove(selected);
			setSelected(null);
		}
	}

	// x, y in pixels
	public void addTile(Tile t, int x, int y, int o) {
		x = x / gridSize + minCol;
		y = y / gridSize + minRow;
		addTileGrid(t, x, y, o);
	}

	public void addTileGrid(Tile t, int x, int y, int o) {
		PlacedTile tile = new PlacedTile(t, x, y, o);
		tiles.add(tile);

		// check if resizing is necessary
		boolean resize = false;
		if (x < minCol) {
			minCol = x;
			resize = true;
		}
		if (y < minRow) {
			minRow = y;
			resize = true;
		}
		if (x+t.getWidth(o)-1 > maxCol) {
			maxCol = x+t.getWidth(o)-1;
			resize = true;
		}
		if (y+t.getHeight(o)-1 > maxRow) {
			maxRow = y+t.getHeight(o)-1;
			resize = true;
		}
		if (resize) {
			setPreferredSize(new Dimension((maxCol-minCol+1)*gridSize, (maxRow-minRow+1)*gridSize));
			revalidate();
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(new Color(208, 224, 255));
		for (int y = minRow; y <= maxRow; y ++) {
			for (int x = minCol; x <= maxCol; x ++) {
				if ((x+y) % 2 == 0) g.fillRect((x-minCol)*gridSize, (y-minRow)*gridSize, gridSize, gridSize);
			}
		}
		for (PlacedTile t : tiles) {
			if (t.image == null) {
				t.image = t.tile.getScaledImage(gridSize*t.getWidth(), t.orientation);
			}
			g.drawImage(t.image, t.getPixelX(), t.getPixelY(), null);
			if (selected == t) {
				g.setColor(Color.RED);
				g.drawRect(t.getPixelX(), t.getPixelY(), t.getWidth()*gridSize-1, t.getHeight()*gridSize-1);
				g.drawRect(t.getPixelX()+1, t.getPixelY()+1, t.getWidth()*gridSize-3, t.getHeight()*gridSize-3);
			}
		}
	}

	// TODO option to include grid
	// TODO shrink to fit
	public RenderedImage getImage() {
		if (tiles.size() < 1) return null;

		// first determine the extent of the map
		PlacedTile tt = tiles.get(0);
		int minx = tt.x;
		int miny = tt.y;
		int maxx = tt.x + tt.tile.getWidth(tt.orientation);
		int maxy = tt.y + tt.tile.getHeight(tt.orientation);
		for (PlacedTile t : tiles) {
			if (t.x < minx) minx = t.x;
			if (t.y < miny) miny = t.y;
			if (t.x + t.tile.getWidth(t.orientation) > maxx) maxx = t.x + t.tile.getWidth(t.orientation);
			if (t.y + t.tile.getHeight(t.orientation) > maxy) maxy = t.y + t.tile.getHeight(t.orientation);
		}
		// add a one grid square border (bottom and right border is added when creating image)
		minx--;
		miny--;

		BufferedImage img = null;
		Graphics g = null;
		for (PlacedTile t : tiles) {
			if (t.image == null) {
				t.image = t.tile.getScaledImage(gridSize*t.tile.getWidth(t.orientation), t.orientation);
			}
			if (img == null) {
				img = new BufferedImage((maxx-minx+1)*gridSize, (maxy-miny+1)*gridSize, BufferedImage.TYPE_INT_RGB);
				g = img.getGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, img.getWidth(), img.getHeight());
			}
			g.drawImage(t.image, (t.x-minx)*gridSize, (t.y-miny)*gridSize, null);
		}
		return img;
	}

	// DragTarget interface
	DraggableTile draggedTile = null;

	@Override
	public Component dragEntered(Component source) {
		if (source instanceof DraggableTile) {
			DraggableTile src = (DraggableTile)source;
			draggedTile = new DraggableTile(src.tile, gridSize) {
				@Override
				public void setLocation(int x, int y) {
					Point cLoc = SwingUtilities.convertPoint(getParent(),x,y,MapPanel.this);
					cLoc.x -= cLoc.x % gridSize +1;	// the +1 is to account for the border
					cLoc.y -= cLoc.y % gridSize +1;	// the +1 is to account for the border
					cLoc = SwingUtilities.convertPoint(MapPanel.this,cLoc.x,cLoc.y,getParent());
					super.setLocation(cLoc.x, cLoc.y);
				}
			};
			draggedTile.setOrientation(src.orientation);
			return draggedTile;
		}
		return null;
	}

	@Override
	public void dragExited(Component source) {
		if (source instanceof DraggableTile && draggedTile != null) {
			// reset the source component's orientation to match our drag component
			((DraggableTile)source).setOrientation(draggedTile.orientation);
		}
		draggedTile = null;
	}

	@Override
	public void dragCompleted(Point p) {
		if (draggedTile == null) return;
		// doesn't account for differences in scale - but drag should probably always be in destination scale anyway
		addTile(draggedTile.tile, p.x, p.y, draggedTile.orientation);
		draggedTile = null;
	}

	// TODO need to collate these by set and provide counts
	public void listTiles() {
		for (PlacedTile t : tiles) {
			System.out.println(t.tile);
		}
	}

	public void executeProcess(MapProcessor processor) {
		processor.ProcessMap(this);
		for (PlacedTile t : tiles) {
			if (t != null) processor.ProcessTile(t);
		}
	}

	public void parseDOM(Element node) {
		reset();
		minCol = 0;
		maxCol = 0;
		minRow = 0;
		maxRow = 0;

		NodeList nodes = node.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element)n;
			if (e.getNodeName() == "Tile") {
				String file = e.getAttribute("file");
				Tile t = TileManager.getTile(file);
				if (t != null) {
					if (e.getAttribute("mirrored").equals("true")) t = t.getMirror();
					int x = Integer.parseInt(e.getAttribute("x"));
					int y = Integer.parseInt(e.getAttribute("y"));
					int orient = Integer.parseInt(e.getAttribute("orientation"));
					System.out.println("found tile " + file + " @ (" + x + ", " + y + ") mirrored? " + e.getAttribute("mirrored"));
					addTileGrid(t, x, y, orient);
				} else {
					System.err.println("Unknown tile "+file);
				}
			}
		}
	}

	// Scrollable implementation:
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		return gridSize*6;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		return gridSize;
	}
}

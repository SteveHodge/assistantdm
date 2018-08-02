package tilemapper;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.border.BevelBorder;

//TODO resolve bugs with initial sizing of the two palettes and cases where resizing doesn't trigger resetting of the size

@SuppressWarnings("serial")
public class TilePalette extends JPanel implements MouseListener, Scrollable {
	ComponentDragger dragger;
	int gridSize = 16;
	boolean showEdges = false;
	JPanel topPalette;
	JPanel mainPalette;
	int cols = 5;
	Map<Tile,DraggableTile> tileCache = new HashMap<Tile,DraggableTile>();

	public TilePalette(ComponentDragger dragger) {
		TileManager.addPropertyChangeListener(e -> {
			tilesChanged();
		});

		this.dragger = dragger;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		topPalette = new PalettePanel();
		add(topPalette);

		JComponent line = (JComponent)Box.createVerticalStrut(14);
		line.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(5, 5, 5, 5),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		add(line);

		mainPalette = new PalettePanel();
		JScrollPane scroller = new JScrollPane(mainPalette, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setUnitIncrement(40);
		add(scroller);
		topPalette.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));	// default to avoid it taking half the area
		tilesChanged();
	}

	private void tilesChanged() {
		mainPalette.removeAll();
		for (Tile t : TileManager.getTiles()) {
			DraggableTile tile = tileCache.get(t);
			if (tile == null) {
				tile = new DraggableTile(t, gridSize);
				dragger.registerSource(tile);
				tile.addMouseListener(this);
				tileCache.put(t,tile);
			}
			mainPalette.add(tile);
		}
		mainPalette.revalidate();	// this is not necessary for initial layout but is necessary for subsequent changes
		repaint();
	}

	// Scrollable interface: we need this is lock our width to the parent scrollpane's width
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 100;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}

	// MouseListener interface:
	@Override
	public void mouseClicked(MouseEvent e) {
		topPalette.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));	// reset this to allow correct sizing
		topPalette.removeAll();
		DraggableTile tile = (DraggableTile)e.getSource();
		System.out.println("Selected " + tile.tile.file);
		for (int i = 0; i < 4; i++) {
			DraggableTile t = new DraggableTile(tile.tile,gridSize);
			t.setOrientation(i);
			dragger.registerSource(t);
			topPalette.add(t);
			// mirrored version:
			t = new DraggableTile(tile.tile.getMirror(),gridSize);
			t.setOrientation(4-i);
			dragger.registerSource(t);
			topPalette.add(t);
		}
		topPalette.revalidate();
		topPalette.repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
}

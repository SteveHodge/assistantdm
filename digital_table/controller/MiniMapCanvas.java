package digital_table.controller;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import digital_table.elements.MapElement;
import digital_table.server.CoordinateConverter;
import digital_table.server.MapCanvas;
import digital_table.server.RepaintListener;

// TODO move the canvas to the top level class and have it provide a factory for the panel

@SuppressWarnings("serial")
class MiniMapCanvas extends MapCanvas {
	public final static int DEFAULT_CELL_SIZE = 20;
	private final static int DEFAULT_ROWS = 38;
	private final static int DEFAULT_COLUMNS = 32;

	private int cellSize = DEFAULT_CELL_SIZE;
	private MiniMapPanel panel;
	private CoordinateConverter remote;

	private class MiniMapPanel extends JPanel implements RepaintListener {
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(DEFAULT_COLUMNS * DEFAULT_CELL_SIZE, DEFAULT_ROWS * DEFAULT_CELL_SIZE);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			MiniMapCanvas.this.paint((Graphics2D) g);
		}
	}

	MiniMapCanvas() {
		panel = new MiniMapPanel();
		addRepaintListener(panel);
	}

	JPanel getPanel() {
		return panel;
	}

	void setRemote(CoordinateConverter remote) {
		this.remote = remote;
	}

	@Override
	public CoordinateConverter getRemote() {
		return remote;
	}

	@Override
	public void addElement(MapElement e, MapElement parent) {
		super.addElement(e, parent);
		repaint();
	}

	@Override
	public boolean removeElement(int id) {
		if (super.removeElement(id)) {
			repaint();
			return true;
		}
		return false;
	}

	@Override
	public void changeParent(MapElement e, MapElement parent) {
		if (e.parent == parent) return;
		super.changeParent(e, parent);
	}

	@Override
	public void promoteElement(MapElement e) {
		super.promoteElement(e);
		repaint();
	}

	@Override
	public void demoteElement(MapElement e) {
		super.demoteElement(e);
		repaint();
	}

	public void reorganiseBefore(MapElement e1, MapElement e2) {
		super.reorganiseBefore(e1, e2);
		repaint();
	}

	@Override
	protected int getResolutionNumeratorX() {
		//return panel.getWidth();
		return cellSize;
	}

	@Override
	protected int getResolutionDenominatorX() {
		//return DEFAULT_COLUMNS;
		return 1;
	}

	@Override
	protected int getResolutionNumeratorY() {
		//return panel.getHeight();
		return cellSize;
	}

	@Override
	protected int getResolutionDenominatorY() {
		//return DEFAULT_ROWS;
		return 1;
	}

	void setCellSize(int size) {
		cellSize = size;
		repaint();
	}

	int getCellSize() {
		return cellSize;
	}
}

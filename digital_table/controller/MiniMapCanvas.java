package digital_table.controller;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import digital_table.elements.MapElement;
import digital_table.server.MapCanvas;
import digital_table.server.RepaintListener;

// TODO move the canvas to the top level class and have it provide a factory for the panel

@SuppressWarnings("serial")
class MiniMapCanvas extends MapCanvas {
	public final static int DEFAULT_CELL_SIZE = 20;

	private int rows = 38;
	private int columns = 32;

	private MiniMapPanel panel;

	private class MiniMapPanel extends JPanel implements RepaintListener {
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(columns * DEFAULT_CELL_SIZE, rows * DEFAULT_CELL_SIZE);
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

	@Override
	protected int getResolutionNumeratorX() {
		return panel.getWidth();
	}

	@Override
	protected int getResolutionDenominatorX() {
		return columns;
	}

	@Override
	protected int getResolutionNumeratorY() {
		return panel.getHeight();
	}

	@Override
	protected int getResolutionDenominatorY() {
		return rows;
	}
}

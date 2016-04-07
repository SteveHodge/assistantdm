package maptool;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class MapLayoutManager implements LayoutManager {
	ScalableImagePanel image;
	GridPanel grid;

	MapLayoutManager(ScalableImagePanel i, GridPanel g) {
		image = i;
		grid = g;
	}

	@Override
	public void addLayoutComponent(String s, Component c) {
	}

	@Override
	public void layoutContainer(Container c) {
		image.setSize(c.getSize());
		grid.setSize(c.getSize());
		image.setLocation(0, 0);
		image.setLocation(0, 0);
	}

	@Override
	public Dimension minimumLayoutSize(Container c) {
		Dimension d = new Dimension(image.getMinimumSize());
		Insets i = c.getInsets();
		d.width += i.left + i.right;
		d.height += i.top + i.bottom;
		return d;
	}

	@Override
	public Dimension preferredLayoutSize(Container c) {
		Dimension d = new Dimension(image.getPreferredSize());
		Insets i = c.getInsets();
		d.width += i.left + i.right;
		d.height += i.top + i.bottom;
		return d;
	}

	@Override
	public void removeLayoutComponent(Component c) {
	}

}

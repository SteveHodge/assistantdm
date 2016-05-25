package maptool;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class MapLayoutManager implements LayoutManager {
	ScalableImagePanel image;

	MapLayoutManager(ScalableImagePanel i) {
		image = i;
	}

	@Override
	public void addLayoutComponent(String s, Component c) {
	}

	@Override
	public void layoutContainer(Container parent) {
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			c.setSize(parent.getSize());
			c.setLocation(0, 0);
		}
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

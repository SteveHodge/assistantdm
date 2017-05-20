package tilemapper;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JPanel;

/*
 * This JPanel subclass always uses a FlowLayout for layout that has been modified
 * to return preferred sizes calculated to expect multiple rows (basically as the
 * layout itself will produce).
 * Note that getAlignOnBaseline is not considered in this size calculation.
 */
// TODO there is a still a problem with the panels not being quite big enough
@SuppressWarnings("serial")
public class PalettePanel extends JPanel {
	FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING,2,2) {
		@Override
		public Dimension minimumLayoutSize(Container target) {
			Dimension dim = getLayoutSize(target,true);
			//System.out.println("Minimum layout size = "+dim);
			return dim;
		}

		@Override
		public Dimension preferredLayoutSize(Container target) {
			Dimension dim = getLayoutSize(target,false);
			//System.out.println("Preferred layout size = "+dim);
			return dim;
		}

		public Dimension getLayoutSize(Container target, boolean min) {
			synchronized (target.getTreeLock()) {
				Dimension dim = new Dimension(0, 0);
				int nmembers = target.getComponentCount();
				Insets insets = target.getInsets();
				int maxwidth = target.getWidth() - (insets.left + insets.right + getHgap()*2);
				int x = 0;
				int rowh = 0;

				for (int i = 0 ; i < nmembers ; i++) {
					Component m = target.getComponent(i);
					if (m.isVisible()) {
						Dimension d;
						if (min) {
							d = m.getMinimumSize();
						} else {
							d = m.getPreferredSize();
						}
						m.setSize(d.width, d.height);

						if ((x == 0) || ((x + d.width) <= maxwidth)) {
							if (x > 0) {
								x += getHgap();
							}
							x += d.width;
							rowh = Math.max(rowh, d.height);
						} else {
							dim.width = Math.max(dim.width,x);
							if (dim.height > 0) dim.height += getVgap();
							dim.height += rowh;
							x = d.width;
							rowh = d.height;
						}
					}
				}
				dim.width = Math.max(dim.width,x);
				dim.height += rowh;

				dim.width += insets.left + insets.right + getHgap()*2;
				dim.height += insets.top + insets.bottom + getVgap()*2;
				return dim;
			}
		}
	};

	public PalettePanel() {
		setLayout(flowLayout);
	}

	@Override
	public Dimension getPreferredSize() {
		return flowLayout.preferredLayoutSize(this);
	}
}

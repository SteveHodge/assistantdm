package digital_table.elements;

import java.awt.BorderLayout;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class BrowserRemote extends Browser {
	private static final long serialVersionUID = 1L;

	transient protected JPanel panel = null;
	transient protected int onScreen;

	public BrowserRemote() {
		screen = new Property<Integer>(PROPERTY_SCREEN, 0, Integer.class) {
			private static final long serialVersionUID = 1L;

			@Override
			public void setValue(Integer s) {
				if (value == s) return;
				Integer old = value;
				value = s;
				checkScreenSetup();
				pcs.firePropertyChange(PROPERTY_SCREEN, old, value);
				//if (canvas != null) canvas.repaint();
			}

		};
		visible = new Property<Visibility>(PROPERTY_VISIBLE, false, Visibility.HIDDEN, Visibility.class) {
			private static final long serialVersionUID = 1L;

			@Override
			public void setValue(Visibility visible) {
				super.setValue(visible);
				checkScreenSetup();
			}
		};
	}

	@Override
	public void paint(Graphics2D g) {
		if (getVisibility() != Visibility.HIDDEN && panel != null) {
			panel.repaint();	// without this the underlying map will often get painted ontop of the panel. with this there can still be flickering
		}
	}

	public void checkScreenSetup() {
		if (visible.getValue() == Visibility.HIDDEN) {
			// make the panel not visible if it exists
			if (panel != null) {
				panel.setVisible(false);
			}
			return;
		}

		if (panel == null) {
			// create a new panel and add it to the screen
			panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(getComponent(), BorderLayout.CENTER);
			if (screenManager != null) {
				screenManager.addComponent(getID(), panel, screen.getValue());
				onScreen = screen.getValue();
			}
		} else {
			if (onScreen != screen.getValue() && screenManager != null) {
				// if the panel has changed screens then move it
				screenManager.removeComponent(getID(), panel);
				screenManager.addComponent(getID(), panel, screen.getValue());
				onScreen = screen.getValue();
				panel.revalidate();
			}
			// set it visible incase it is not visible
			panel.setVisible(true);
		}
	}
}

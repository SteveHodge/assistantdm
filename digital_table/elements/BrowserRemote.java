package digital_table.elements;

import java.awt.BorderLayout;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class BrowserRemote extends Browser {
	private static final long serialVersionUID = 1L;

	transient protected JPanel panel = null;
	transient protected int onScreen;

	public void paint(Graphics2D g) {
		if (visible && panel != null) {
			panel.repaint();	// without this the underlying map will often get painted ontop of the panel. with this there can still be flickering
		}
	}

	public void setScreen(int s) {
		if (screen == s) return;
		int old = screen;
		screen = s;
		checkScreenSetup();
		pcs.firePropertyChange(PROPERTY_SCREEN, old, screen);
		//if (canvas != null) canvas.repaint();
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		checkScreenSetup();
	}

	public void checkScreenSetup() {
		if (!visible) {
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
	        	screenManager.addComponent(getID(), panel, screen);
		        onScreen = screen;
	        }
		} else {
			if (onScreen != screen && screenManager != null) {
				// if the panel has changed screens then move it
				screenManager.removeComponent(getID(), panel);
				screenManager.addComponent(getID(), panel, screen);
		        onScreen = screen;
		        panel.revalidate();
			}
			// set it visible incase it is not visible
			panel.setVisible(true);
		}
	}
}

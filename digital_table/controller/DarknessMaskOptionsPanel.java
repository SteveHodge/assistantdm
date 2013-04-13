package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import digital_table.elements.DarknessMask;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class DarknessMaskOptionsPanel extends OptionsPanel {
	DarknessMask darkness;
	JPanel colorPanel;
	JSlider alphaSlider;
	
	boolean dragClear = true;	// when dragging if true then we clear cells, otherwise we reset cells 

	public DarknessMaskOptionsPanel(DarknessMask t, TableDisplay r) {
		super(r);
		darkness = t;
		darkness.addPropertyChangeListener(listener);

		colorPanel = createColorControl(darkness, DarknessMask.PROPERTY_COLOR);
		alphaSlider = createSliderControl(darkness, DarknessMask.PROPERTY_ALPHA, Mode.LOCAL);
		JCheckBox visibleCheck = createVisibilityControl(darkness);
		visibleCheck.setSelected(true);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);

	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(DarknessMask.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));
				
			} else if (e.getPropertyName().equals(DarknessMask.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());
				
			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	public DragMode getDragMode() {
		return DragMode.PAINT;
	}

	public void elementClicked(Point2D location, MouseEvent e, boolean dragging) {
		if (!dragging) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;
		}

		// get nearest grid intersection
		int x = (int)location.getX();
		int y = (int)location.getY();
		Point p = new Point(x, y);
		// TODO cleanup logic:
		if (!dragging) {
			setMasked(p, !darkness.isMasked(p));
		} else if (dragging && dragClear) {
			setMasked(p, false);
		} else if (dragging && !dragClear) {
			setMasked(p, true);
		}
	}
	
	protected void setMasked(Point p, boolean mask) {
		darkness.setMasked(p, mask);
		try {
			if (mask) {
				remote.setElementProperty(darkness.getID(), DarknessMask.PROPERTY_MASKCELL, p);
			} else {
				remote.setElementProperty(darkness.getID(), DarknessMask.PROPERTY_UNMASKCELL, p);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public Object getDragTarget(Point2D location) {
		// get nearest grid intersection
		// if the cell is already cleared then we are reseting, otherwise clearing
		Point p = new Point((int)location.getX(), (int)location.getY());
		dragClear = darkness.isMasked(p);
		return "MASK";
	}
}

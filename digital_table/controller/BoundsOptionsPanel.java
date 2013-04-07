package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import digital_table.elements.ScreenBounds;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class BoundsOptionsPanel extends OptionsPanel {
	ScreenBounds bounds;
	JPanel colorPanel;
	JSlider alphaSlider;

	public BoundsOptionsPanel(ScreenBounds b, TableDisplay r) {
		super(r);
		bounds = b;
		bounds.addPropertyChangeListener(listener);

		colorPanel = createColorControl(bounds, ScreenBounds.PROPERTY_COLOR);
		alphaSlider = createSliderControl(bounds, ScreenBounds.PROPERTY_ALPHA);

		JCheckBox visibleCheck = createVisibilityControl(bounds);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 1; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);

	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ScreenBounds.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));
				
			} else if (e.getPropertyName().equals(ScreenBounds.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());
				
			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	public boolean snapToGrid() {
		return true;
	}
}

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
import javax.swing.JTextField;

import digital_table.elements.ShapeableTemplate;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class ShapeableTemplateOptionsPanel extends OptionsPanel {
	ShapeableTemplate template;
	JPanel colorPanel;
	JTextField labelField;
	JTextField maximumField;
	JSlider alphaSlider;
	JLabel remaining = new JLabel();
	
	public ShapeableTemplateOptionsPanel(ShapeableTemplate t, TableDisplay r) {
		super(r);
		template = t;
		template.addPropertyChangeListener(listener);

		colorPanel = createColorControl(template, ShapeableTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(template, ShapeableTemplate.PROPERTY_ALPHA);
		maximumField = createIntegerControl(template, ShapeableTemplate.PROPERTY_MAXIMUM);
		labelField = this.createLocalStringControl(template, ShapeableTemplate.PROPERTY_LABEL);
		JCheckBox visibleCheck = createVisibilityControl(template);
		updateRemaining();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Maximum:"), c);
		c.gridy++; add(new JLabel("Remaining:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(maximumField, c);
		c.gridy++; add(remaining, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);

	}

	protected void updateRemaining() {
		int max = template.getMaximum();
		if (max == 0) {
			remaining.setText("");
		} else {
			remaining.setText("" + (max - template.getPlaced()));
		}
	}
	
	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));
				
			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());
				
			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_MAXIMUM)) {
				maximumField.setText(e.getNewValue().toString());
				updateRemaining();

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_PLACED)) {
				updateRemaining();

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	public boolean snapToGrid() {
		return true;
	}
}

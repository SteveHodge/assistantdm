package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class LineTemplateOptionsPanel extends OptionsPanel {
	LineTemplate template;
	JTextField xField;
	JTextField yField;
	JTextField rangeField;
	JTextField originXField;
	JTextField originYField;
	JPanel colorPanel;
	JTextField labelField;
	JSlider alphaSlider;

	public LineTemplateOptionsPanel(MapElement parent, TableDisplay r) {
		super(r);
		template = new LineTemplate(18, 14, 21, 7);
		sendElement(template, parent);
		template.setProperty(MapElement.PROPERTY_VISIBLE, true);
		template.addPropertyChangeListener(listener);

		xField = createIntegerControl(template, LineTemplate.PROPERTY_X);
		yField = createIntegerControl(template, LineTemplate.PROPERTY_Y);
		originXField = createIntegerControl(template, LineTemplate.PROPERTY_ORIGIN_X);
		originYField = createIntegerControl(template, LineTemplate.PROPERTY_ORIGIN_Y);
		rangeField = createIntegerControl(template, LineTemplate.PROPERTY_RANGE);
		colorPanel = createColorControl(template, LineTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(template, LineTemplate.PROPERTY_ALPHA);
		labelField = createStringControl(template, LineTemplate.PROPERTY_LABEL, Mode.LOCAL);
		JCheckBox visibleCheck = createVisibilityControl(template);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy = 1; add(new JLabel("Origin X:"), c);
		c.gridy = 2; add(new JLabel("Origin Y:"), c);
		c.gridy = 3; add(new JLabel("Target X:"), c);
		c.gridy = 4; add(new JLabel("Target Y:"), c);
		c.gridy = 5; add(new JLabel("Range:"), c);
		c.gridy = 6; add(new JLabel("Colour:"), c);
		c.gridy = 7; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy = 1; add(originXField, c);
		c.gridy = 2; add(originYField, c);
		c.gridy = 3; add(xField, c);
		c.gridy = 4; add(yField, c);
		c.gridy = 5; add(rangeField, c);
		c.gridy = 6; add(colorPanel, c);
		c.gridy = 7; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	@Override
	public LineTemplate getElement() {
		return template;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(LineTemplate.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_ORIGIN_X)) {
				originXField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_ORIGIN_Y)) {
				originYField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_RANGE)) {
				rangeField.setText(e.getNewValue().toString());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	@Override
	public MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	MapElementMouseListener mouseListener = new DefaultDragger() {
		@Override
		String getDragTarget(Point2D gridLocation) {

			if (gridLocation.distance((Integer)template.getProperty(LineTemplate.PROPERTY_ORIGIN_X),
					(Integer)template.getProperty(LineTemplate.PROPERTY_ORIGIN_Y)) < 2.0d
					&& gridLocation.distance((Integer)template.getProperty(LineTemplate.PROPERTY_ORIGIN_X),
							(Integer)template.getProperty(LineTemplate.PROPERTY_ORIGIN_Y))
							< gridLocation.distance((Integer)template.getProperty(LineTemplate.PROPERTY_X), (Integer)template.getProperty(LineTemplate.PROPERTY_Y))) {
				return LineTemplate.PROPERTY_ORIGIN_LOCATION;
			} else {
				return LineTemplate.PROPERTY_TARGET_LOCATION;
			}
		}
	};
}

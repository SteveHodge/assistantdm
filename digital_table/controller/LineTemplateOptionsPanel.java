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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;

@SuppressWarnings("serial")
class LineTemplateOptionsPanel extends OptionsPanel<LineTemplate> {
	private JTextField xField;
	private JTextField yField;
	private JTextField rangeField;
	private JTextField originXField;
	private JTextField originYField;
	private JPanel colorPanel;
	private JTextField labelField;
	private JSlider alphaSlider;
	private JCheckBox visibleCheck;

	LineTemplateOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new LineTemplate(18, 14, 21, 7);
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, true);
		element.addPropertyChangeListener(listener);

		xField = createIntegerControl(LineTemplate.PROPERTY_X);
		yField = createIntegerControl(LineTemplate.PROPERTY_Y);
		originXField = createIntegerControl(LineTemplate.PROPERTY_ORIGIN_X);
		originYField = createIntegerControl(LineTemplate.PROPERTY_ORIGIN_Y);
		rangeField = createIntegerControl(LineTemplate.PROPERTY_RANGE);
		colorPanel = createColorControl(LineTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(LineTemplate.PROPERTY_ALPHA);
		labelField = createStringControl(LineTemplate.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl();

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

	private PropertyChangeListener listener = new PropertyChangeListener() {
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
	MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	private MapElementMouseListener mouseListener = new DefaultDragger() {
		@Override
		String getDragTarget(Point2D gridLocation) {

			if (gridLocation.distance((Integer) element.getProperty(LineTemplate.PROPERTY_ORIGIN_X),
					(Integer) element.getProperty(LineTemplate.PROPERTY_ORIGIN_Y)) < 2.0d
					&& gridLocation.distance((Integer) element.getProperty(LineTemplate.PROPERTY_ORIGIN_X),
							(Integer) element.getProperty(LineTemplate.PROPERTY_ORIGIN_Y))
							< gridLocation.distance((Integer) element.getProperty(LineTemplate.PROPERTY_X), (Integer) element.getProperty(LineTemplate.PROPERTY_Y))) {
				return LineTemplate.PROPERTY_ORIGIN_LOCATION;
			} else {
				return LineTemplate.PROPERTY_TARGET_LOCATION;
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "LineTemplate";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected());
		return e;
	}

	@Override
	void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(LineTemplate.PROPERTY_LABEL, e, Mode.LOCAL);
		parseColorAttribute(LineTemplate.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(LineTemplate.PROPERTY_ALPHA, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_X, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_ORIGIN_X, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_ORIGIN_Y, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_RANGE, e, Mode.ALL);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);
	}
}

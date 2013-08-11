package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.MapElement;
import digital_table.elements.SpreadTemplate;


@SuppressWarnings("serial")
class SpreadTemplateOptionsPanel extends OptionsPanel {
	private SpreadTemplate template;
	private JTextField radiusField;
	private JTextField xField;
	private JTextField yField;
	private JComboBox directionCombo;
	private JComboBox typeCombo;
	private JPanel colorPanel;
	private JTextField labelField;
	private JSlider alphaSlider;
	private JCheckBox visibleCheck;

	SpreadTemplateOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		template = new SpreadTemplate(4, 10, 10);
		display.addElement(template, parent);
		template.setProperty(MapElement.PROPERTY_VISIBLE, true);
		template.addPropertyChangeListener(listener);

		radiusField = createIntegerControl(template, SpreadTemplate.PROPERTY_RADIUS);
		xField = createIntegerControl(template, SpreadTemplate.PROPERTY_X);
		yField = createIntegerControl(template, SpreadTemplate.PROPERTY_Y);
		colorPanel = createColorControl(template, SpreadTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(template, SpreadTemplate.PROPERTY_ALPHA);

		typeCombo = createComboControl(template, SpreadTemplate.PROPERTY_TYPE, SpreadTemplate.Type.values());
		typeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				directionCombo.setEnabled(typeCombo.getSelectedItem() == SpreadTemplate.Type.QUADRANT);
			}
		});

		directionCombo = createComboControl(template, SpreadTemplate.PROPERTY_DIRECTION, SpreadTemplate.Direction.values());
		directionCombo.setEnabled(template.getProperty(SpreadTemplate.PROPERTY_TYPE) == SpreadTemplate.Type.QUADRANT);

		labelField = createStringControl(template, SpreadTemplate.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl(template);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy = 1; add(new JLabel("Radius:"), c);
		c.gridy = 2; add(new JLabel("Column:"), c);
		c.gridy = 3; add(new JLabel("Row:"), c);
		c.gridy = 4; add(new JLabel("Colour:"), c);
		c.gridy = 5; add(new JLabel("Transparency:"), c);
		c.gridy = 6; add(new JLabel("Type:"), c);
		c.gridy = 7; add(new JLabel("Direction:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy = 1; add(radiusField, c);
		c.gridy = 2; add(xField, c);
		c.gridy = 3; add(yField, c);
		c.gridy = 4; add(colorPanel, c);
		c.gridy = 5; add(alphaSlider, c);
		c.gridy = 6; add(typeCombo, c);
		c.gridy = 7; add(directionCombo, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	@Override
	SpreadTemplate getElement() {
		return template;
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_DIRECTION)) {
				directionCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_RADIUS)) {
				radiusField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_TYPE)) {
				typeCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

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
			return "location";
		}

		@Override
		void setTargetLocation(Point2D p) {
			display.setProperty(template, SpreadTemplate.PROPERTY_X, (int) p.getX());
			display.setProperty(template, SpreadTemplate.PROPERTY_Y, (int) p.getY());
		}

		@Override
		Point2D getTargetLocation() {
			return new Point((Integer)template.getProperty(SpreadTemplate.PROPERTY_X),
					(Integer)template.getProperty(SpreadTemplate.PROPERTY_Y));
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "SpreadTemplate";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + SpreadTemplate.PROPERTY_VISIBLE, visibleCheck.isSelected());
		return e;
	}

	@Override
	void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseEnumAttribute(SpreadTemplate.PROPERTY_DIRECTION, SpreadTemplate.Direction.class, e, Mode.ALL);
		parseEnumAttribute(SpreadTemplate.PROPERTY_TYPE, SpreadTemplate.Type.class, e, Mode.ALL);
		parseStringAttribute(SpreadTemplate.PROPERTY_LABEL, e, Mode.LOCAL);
		parseColorAttribute(SpreadTemplate.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(SpreadTemplate.PROPERTY_ALPHA, e, Mode.ALL);
		parseIntegerAttribute(SpreadTemplate.PROPERTY_RADIUS, e, Mode.ALL);
		parseIntegerAttribute(SpreadTemplate.PROPERTY_X, e, Mode.ALL);
		parseIntegerAttribute(SpreadTemplate.PROPERTY_Y, e, Mode.ALL);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);
	}
}

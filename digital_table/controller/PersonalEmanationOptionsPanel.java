package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
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
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.PersonalEmanation;

@SuppressWarnings("serial")
class PersonalEmanationOptionsPanel extends OptionsPanel<PersonalEmanation> {
	private JTextField radiusField;
	private JTextField xField;
	private JTextField yField;
	private JPanel colorPanel;
	private JTextField spaceField;
	private JTextField labelField;
	private JSlider alphaSlider;
	private JCheckBox visibleCheck;

	PersonalEmanationOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		if (parent == null)
			element = new PersonalEmanation(2, r.getXOffset(), r.getYOffset());
		else
			element = new PersonalEmanation(2, 0, 0);
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		radiusField = createIntegerControl(PersonalEmanation.PROPERTY_RADIUS);
		xField = createIntegerControl(PersonalEmanation.PROPERTY_X);
		yField = createIntegerControl(PersonalEmanation.PROPERTY_Y);
		colorPanel = createColorControl(PersonalEmanation.PROPERTY_COLOR);
		alphaSlider = createSliderControl(PersonalEmanation.PROPERTY_ALPHA);
		spaceField = createIntegerControl(PersonalEmanation.PROPERTY_SPACE);
		labelField = createStringControl(PersonalEmanation.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl();

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Radius:"), c);
		c.gridy++; add(new JLabel("Column:"), c);
		c.gridy++; add(new JLabel("Row:"), c);
		c.gridy++; add(new JLabel("Space:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(radiusField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(spaceField, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
		add(new JPanel(), c);
		//@formatter:on
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_RADIUS)) {
				radiusField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_SPACE)) {
				spaceField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_DRAGGING)) {
				// ignore

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
			display.setProperty(element, PersonalEmanation.PROPERTY_X, (int) Math.round(p.getX()));
			display.setProperty(element, PersonalEmanation.PROPERTY_Y, (int) Math.round(p.getY()));
		}

		@Override
		Point2D getTargetLocation() {
			return new Point((Integer) element.getProperty(PersonalEmanation.PROPERTY_X),
					(Integer) element.getProperty(PersonalEmanation.PROPERTY_Y));
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "PersonalEmanation";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(PersonalEmanation.PROPERTY_LABEL, e, Mode.LOCAL);
		parseColorAttribute(PersonalEmanation.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(PersonalEmanation.PROPERTY_ALPHA, e, Mode.ALL);
		parseIntegerAttribute(PersonalEmanation.PROPERTY_X, e, Mode.ALL);
		parseIntegerAttribute(PersonalEmanation.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(PersonalEmanation.PROPERTY_RADIUS, e, Mode.ALL);
		parseIntegerAttribute(PersonalEmanation.PROPERTY_SPACE, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
	}
}

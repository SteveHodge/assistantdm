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

import digital_table.elements.MapElement;
import digital_table.elements.PersonalEmanation;
import digital_table.server.TableDisplay;


@SuppressWarnings("serial")
public class PersonalEmanationOptionsPanel extends OptionsPanel {
	PersonalEmanation template;
	JTextField radiusField;
	JTextField xField;
	JTextField yField;
	JPanel colorPanel;
	JTextField spaceField;
	JTextField labelField;
	JSlider alphaSlider;
	JCheckBox visibleCheck;

	public PersonalEmanationOptionsPanel(MapElement parent, TableDisplay r) {
		super(r);
		template = new PersonalEmanation(2, 0, 0);
		sendElement(template, parent);
		template.setProperty(MapElement.PROPERTY_VISIBLE, true);
		template.addPropertyChangeListener(listener);

		radiusField = createIntegerControl(template, PersonalEmanation.PROPERTY_RADIUS);
		xField = createIntegerControl(template, PersonalEmanation.PROPERTY_X);
		yField = createIntegerControl(template, PersonalEmanation.PROPERTY_Y);
		colorPanel = createColorControl(template, PersonalEmanation.PROPERTY_COLOR);
		alphaSlider = createSliderControl(template, PersonalEmanation.PROPERTY_ALPHA);
		spaceField = createIntegerControl(template, PersonalEmanation.PROPERTY_SPACE);
		labelField = createStringControl(template, PersonalEmanation.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl(template);

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

	@Override
	public PersonalEmanation getElement() {
		return template;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_ALPHA)) {
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
			return "location";
		}

		@Override
		void setTargetLocation(Point2D p) {
			setRemote(template.getID(), PersonalEmanation.PROPERTY_X, (int) p.getX());
			setRemote(template.getID(), PersonalEmanation.PROPERTY_Y, (int) p.getY());
			template.setProperty(PersonalEmanation.PROPERTY_X, (int) p.getX());
			template.setProperty(PersonalEmanation.PROPERTY_Y, (int) p.getY());
		}

		@Override
		Point2D getTargetLocation() {
			return new Point((Integer) template.getProperty(PersonalEmanation.PROPERTY_X),
					(Integer) template.getProperty(PersonalEmanation.PROPERTY_Y));
		}
	};

	// ---- XML serialisation methods ----
	public final static String XML_TAG = "PersonalEmanation";

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + PersonalEmanation.PROPERTY_VISIBLE, visibleCheck.isSelected());
		return e;
	}

	@Override
	public void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(PersonalEmanation.PROPERTY_LABEL, e, Mode.LOCAL);
		parseColorAttribute(PersonalEmanation.PROPERTY_COLOR, e, Mode.BOTH);
		parseFloatAttribute(PersonalEmanation.PROPERTY_ALPHA, e, Mode.BOTH);
		parseIntegerAttribute(PersonalEmanation.PROPERTY_X, e, Mode.BOTH);
		parseIntegerAttribute(PersonalEmanation.PROPERTY_Y, e, Mode.BOTH);
		parseIntegerAttribute(PersonalEmanation.PROPERTY_RADIUS, e, Mode.BOTH);
		parseIntegerAttribute(PersonalEmanation.PROPERTY_SPACE, e, Mode.BOTH);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);
	}
}

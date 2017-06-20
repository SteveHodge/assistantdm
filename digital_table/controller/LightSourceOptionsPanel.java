package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.LightSource;
import digital_table.elements.LightSource.Type;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;


@SuppressWarnings("serial")
class LightSourceOptionsPanel extends OptionsPanel<LightSource> {
	private JTextField radiusField;
	private JTextField xField;
	private JTextField yField;
	private JTextField labelField;
	private JComboBox<Type> typeCombo;
	private JCheckBox visibleCheck;
	private JCheckBox allCornersCheck;

	LightSourceOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		if (parent == null)
			element = new LightSource(4, r.getXOffset(), r.getYOffset());
		else
			element = new LightSource(4, 0, 0);
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		radiusField = createIntegerControl(LightSource.PROPERTY_RADIUS);
		xField = createIntegerControl(LightSource.PROPERTY_X);
		yField = createIntegerControl(LightSource.PROPERTY_Y);
		typeCombo = createComboControl(LightSource.PROPERTY_TYPE, LightSource.Type.values());
		labelField = createStringControl(LightSource.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl();
		allCornersCheck = createCheckBox(LightSource.PROPERTY_ALL_CORNERS, Mode.ALL, "Radiate from all corners of token");

		// @formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Type:"), c);
		c.gridy++; add(new JLabel("Radius:"), c);
		c.gridy++; add(new JLabel("Column:"), c);
		c.gridy++; add(new JLabel("Row:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(typeCombo, c);
		c.gridy++; add(radiusField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);

		c.gridx = 0; c.gridwidth = 2;
		c.gridy++; add(allCornersCheck, c);
		c.gridy++;
		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		add(new JPanel(), c);
		// @formatter:on
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_RADIUS)) {
				radiusField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_TYPE)) {
				typeCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_ALL_CORNERS)) {
				allCornersCheck.setSelected((Boolean) e.getNewValue());

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
			display.setProperty(element, LightSource.PROPERTY_X, (int) Math.round(p.getX()));
			display.setProperty(element, LightSource.PROPERTY_Y, (int) Math.round(p.getY()));
		}

		@Override
		Point2D getTargetLocation() {
			return new Point((Integer) element.getProperty(LightSource.PROPERTY_X),
					(Integer) element.getProperty(LightSource.PROPERTY_Y));
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "LightSource";

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

		parseStringAttribute(LightSource.PROPERTY_LABEL, e, Mode.LOCAL);
		parseIntegerAttribute(LightSource.PROPERTY_X, e, Mode.ALL);
		parseIntegerAttribute(LightSource.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(LightSource.PROPERTY_RADIUS, e, Mode.ALL);
		parseEnumAttribute(LightSource.PROPERTY_TYPE, LightSource.Type.class, e, Mode.ALL);
		parseBooleanAttribute(LightSource.PROPERTY_ALL_CORNERS, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
	}
}

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
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.SpreadTemplate;


@SuppressWarnings("serial")
class SpreadTemplateOptionsPanel extends OptionsPanel<SpreadTemplate> {
	private JTextField radiusField;
	private JTextField xField;
	private JTextField yField;
	private JComboBox<SpreadTemplate.Direction> directionCombo;
	private JComboBox<SpreadTemplate.Type> typeCombo;
	private JPanel colorPanel;
	private JTextField labelField;
	private JSlider alphaSlider;
	private JCheckBox visibleCheck;
	private ImageMediaOptionsPanel imagePanel;

	SpreadTemplateOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new SpreadTemplate(4, 10, 10);
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		radiusField = createIntegerControl(SpreadTemplate.PROPERTY_RADIUS);
		xField = createIntegerControl(SpreadTemplate.PROPERTY_X);
		yField = createIntegerControl(SpreadTemplate.PROPERTY_Y);
		colorPanel = createColorControl(SpreadTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(SpreadTemplate.PROPERTY_ALPHA);

		typeCombo = createComboControl(SpreadTemplate.PROPERTY_TYPE, SpreadTemplate.Type.values());
		typeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				directionCombo.setEnabled(typeCombo.getSelectedItem() == SpreadTemplate.Type.QUADRANT);
			}
		});

		directionCombo = createComboControl(SpreadTemplate.PROPERTY_DIRECTION, SpreadTemplate.Direction.values());
		directionCombo.setEnabled(element.getProperty(SpreadTemplate.PROPERTY_TYPE) == SpreadTemplate.Type.QUADRANT);

		labelField = createStringControl(SpreadTemplate.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl();

		imagePanel = new ImageMediaOptionsPanel();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JLabel("Radius:"), c);
		add(new JLabel("Column:"), c);
		add(new JLabel("Row:"), c);
		add(new JLabel("Colour:"), c);
		add(new JLabel("Transparency:"), c);
		add(new JLabel("Type:"), c);
		add(new JLabel("Direction:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(radiusField, c);
		add(xField, c);
		add(yField, c);
		add(colorPanel, c);
		add(alphaSlider, c);
		add(typeCombo, c);
		add(directionCombo, c);

		c.gridx = 0;
		c.gridwidth = 2;
		add(imagePanel, c);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		add(new JPanel(), c);
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_ALPHA)) {
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

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_IMAGE_VISIBLE)) {
				imagePanel.imageVisibleCheck.setSelected((Boolean) e.getNewValue());

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
			display.setProperty(element, SpreadTemplate.PROPERTY_X, (int) p.getX());
			display.setProperty(element, SpreadTemplate.PROPERTY_Y, (int) p.getY());
		}

		@Override
		Point2D getTargetLocation() {
			return new Point((Integer)element.getProperty(SpreadTemplate.PROPERTY_X),
					(Integer)element.getProperty(SpreadTemplate.PROPERTY_Y));
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "SpreadTemplate";
	private final static String FILE_ATTRIBUTE_NAME = "uri";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + SpreadTemplate.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		if (imagePanel.uri != null) e.setAttribute(FILE_ATTRIBUTE_NAME, imagePanel.uri.toASCIIString());
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseEnumAttribute(SpreadTemplate.PROPERTY_DIRECTION, SpreadTemplate.Direction.class, e, Mode.ALL);
		parseEnumAttribute(SpreadTemplate.PROPERTY_TYPE, SpreadTemplate.Type.class, e, Mode.ALL);
		parseStringAttribute(SpreadTemplate.PROPERTY_LABEL, e, Mode.LOCAL);
		parseColorAttribute(SpreadTemplate.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(SpreadTemplate.PROPERTY_ALPHA, e, Mode.ALL);
		parseIntegerAttribute(SpreadTemplate.PROPERTY_RADIUS, e, Mode.ALL);
		parseIntegerAttribute(SpreadTemplate.PROPERTY_X, e, Mode.ALL);
		parseIntegerAttribute(SpreadTemplate.PROPERTY_Y, e, Mode.ALL);
		parseBooleanAttribute(SpreadTemplate.PROPERTY_IMAGE_VISIBLE, e, Mode.ALL);
		parseVisibility(e, visibleCheck);

		if (e.hasAttribute(FILE_ATTRIBUTE_NAME)) {
			try {
				URI uri = new URI(e.getAttribute(FILE_ATTRIBUTE_NAME));
				imagePanel.setURI(uri);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
	}
}

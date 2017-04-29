package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import digital_table.elements.Label;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.Token;

@SuppressWarnings("serial")
class LabelOptionsPanel extends OptionsPanel<Label> {
	private JTextField xField;
	private JTextField yField;
	private JSlider alphaSlider;
	private JComboBox<String> rotationsCombo;
	private JPanel colorPanel;
	private JPanel bgColorPanel;
	private JCheckBox bgCheck;
	private JTextField fontSizeField;
	private JTextField textField;
	private JCheckBox visibleCheck;

	private boolean isFloating = false;

	LabelOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element= new Label();
		if (parent == null) {
			element.setProperty(Label.PROPERTY_X, (double) r.getXOffset());
			element.setProperty(Label.PROPERTY_Y, (double) r.getYOffset());
		}
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		xField = createDoubleControl(Label.PROPERTY_X);
		yField = createDoubleControl(Label.PROPERTY_Y);
		alphaSlider = createSliderControl(Label.PROPERTY_ALPHA);
		colorPanel = createColorControl(Label.PROPERTY_COLOR);
		bgColorPanel = createColorControl(Label.PROPERTY_BACKGROUND_COLOR);
		rotationsCombo = createRotationControl(Label.PROPERTY_ROTATIONS, Mode.ALL);
		visibleCheck = createVisibilityControl();
		bgCheck = createCheckBox(Label.PROPERTY_SOLID_BACKGROUND, Mode.ALL, "show background?");
		fontSizeField = createDoubleControl(Label.PROPERTY_FONT_SIZE);
		textField = createStringControl(Label.PROPERTY_TEXT);

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1; add(new JLabel("Label:"), c);
		c.gridy++; add(new JLabel("Left edge column:"), c);
		c.gridy++; add(new JLabel("Top edge Row:"), c);
		c.gridy++; add(new JLabel("Font size (in grid cells):"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Background:"), c);
		c.gridy++;
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(textField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(fontSizeField, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(bgColorPanel, c);
		c.gridy++; add(bgCheck, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		add(new JPanel(), c);
		//@formatter:on
	}

	void setFloating() {
		visibleCheck.setSelected(true);
		isFloating = true;
		textField.setEditable(false);
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(Label.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int) (100 * (Float) e.getNewValue()));

			} else if (e.getPropertyName().equals(Label.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Label.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Label.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer) e.getNewValue());

			} else if (e.getPropertyName().equals(Label.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(Label.PROPERTY_BACKGROUND_COLOR)) {
				bgColorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(Label.PROPERTY_SOLID_BACKGROUND)) {
				bgCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(Label.PROPERTY_FONT_SIZE)) {
				fontSizeField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Label.PROPERTY_TEXT)) {
				textField.setText(e.getNewValue().toString());

			} else {
				System.out.println(toString() + ": Unknown property: " + e.getPropertyName());
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
			display.setProperty(element, Label.PROPERTY_X, p.getX());
			display.setProperty(element, Label.PROPERTY_Y, p.getY());
		}

		@Override
		Point2D getTargetLocation() {
			return new Point2D.Double((Double) element.getProperty(Label.PROPERTY_X),
					(Double) element.getProperty(Label.PROPERTY_Y));
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Label";
	private final static String FLOATING_ATTRIBUTE_NAME = "floating";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		if (isFloating) setAttribute(e, FLOATING_ATTRIBUTE_NAME, isFloating);
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(Label.PROPERTY_TEXT, e, Mode.ALL);
		parseColorAttribute(Label.PROPERTY_COLOR, e, Mode.ALL);
		parseColorAttribute(Label.PROPERTY_BACKGROUND_COLOR, e, Mode.ALL);
		parseFloatAttribute(Label.PROPERTY_ALPHA, e, Mode.ALL);
		parseDoubleAttribute(Label.PROPERTY_X, e, Mode.ALL);
		parseDoubleAttribute(Label.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(Label.PROPERTY_ROTATIONS, e, Mode.ALL);
		parseDoubleAttribute(Label.PROPERTY_FONT_SIZE, e, Mode.ALL);
		parseBooleanAttribute(Label.PROPERTY_SOLID_BACKGROUND, e, Mode.ALL);

		if (e.hasAttribute(FLOATING_ATTRIBUTE_NAME)) {
			isFloating = Boolean.parseBoolean(e.getAttribute(FLOATING_ATTRIBUTE_NAME));
			if (isFloating && parent instanceof TokenOptionsPanel) {
				((TokenOptionsPanel) parent).floatingLabel = this;
				display.setProperty(parent.element, Token.PROPERTY_LABEL, "", Mode.REMOTE);
			}
		}

		parseVisibility(e, visibleCheck);
	}
}

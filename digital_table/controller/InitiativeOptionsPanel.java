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

import combat.CombatPanel;
import combat.InitiativeListener;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Initiative;
import digital_table.elements.Label;
import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;

@SuppressWarnings("serial")
class InitiativeOptionsPanel extends OptionsPanel<Initiative> {
	private JTextField xField;
	private JTextField yField;
	private JSlider alphaSlider;
	private JComboBox rotationsCombo;
	private JPanel colorPanel;
	private JPanel bgColorPanel;
	private JCheckBox visibleCheck;

	InitiativeOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new Initiative();
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		if (CombatPanel.getCombatPanel() != null) {
			CombatPanel.getCombatPanel().addInitiativeListener(new InitiativeListener() {
				@Override
				public void initiativeUpdated(String text) {
					display.setProperty(element, Label.PROPERTY_TEXT, text);
				}
			});
		}
		element.addPropertyChangeListener(listener);

		xField = createDoubleControl(Initiative.PROPERTY_X);
		yField = createDoubleControl(Initiative.PROPERTY_Y);
		alphaSlider = createSliderControl(Initiative.PROPERTY_ALPHA);
		colorPanel = createColorControl(Initiative.PROPERTY_COLOR);
		bgColorPanel = createColorControl(Initiative.PROPERTY_BACKGROUND_COLOR);
		rotationsCombo = createRotationControl(Initiative.PROPERTY_ROTATIONS, Mode.ALL);
		visibleCheck = createVisibilityControl();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1; add(new JLabel("Left edge column:"), c);
		c.gridy++; add(new JLabel("Top edge Row:"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Background:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(bgColorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Initiative.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(Initiative.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Initiative.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Initiative.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer)e.getNewValue());

			} else if (e.getPropertyName().equals(Initiative.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(Initiative.PROPERTY_BACKGROUND_COLOR)) {
				bgColorPanel.setBackground((Color)e.getNewValue());

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
			display.setProperty(element, Initiative.PROPERTY_X, p.getX());
			display.setProperty(element, Initiative.PROPERTY_Y, p.getY());
		}

		@Override
		Point2D getTargetLocation() {
			return new Point2D.Double((Double)element.getProperty(Initiative.PROPERTY_X),
					(Double)element.getProperty(Initiative.PROPERTY_Y));
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Initiative";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + LineTemplate.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseColorAttribute(Initiative.PROPERTY_COLOR, e, Mode.ALL);
		parseColorAttribute(Initiative.PROPERTY_BACKGROUND_COLOR, e, Mode.ALL);
		parseFloatAttribute(Initiative.PROPERTY_ALPHA, e, Mode.ALL);
		parseDoubleAttribute(Initiative.PROPERTY_X, e, Mode.ALL);
		parseDoubleAttribute(Initiative.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(Initiative.PROPERTY_ROTATIONS, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
	}
}


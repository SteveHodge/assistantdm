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

import digital_table.elements.Initiative;
import digital_table.elements.Label;
import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class InitiativeOptionsPanel extends OptionsPanel {
	private Initiative initiative;

	private JTextField xField;
	private JTextField yField;
	private JSlider alphaSlider;
	private JComboBox rotationsCombo;
	private JPanel colorPanel;
	private JPanel bgColorPanel;
	private JCheckBox visibleCheck;

	public InitiativeOptionsPanel(MapElement parent, TableDisplay r) {
		super(r);
		initiative = new Initiative();
		sendElement(initiative, parent);
		initiative.setProperty(MapElement.PROPERTY_VISIBLE, true);
		if (CombatPanel.getCombatPanel() != null) {
			CombatPanel.getCombatPanel().addInitiativeListener(new InitiativeListener() {
				@Override
				public void initiativeUpdated(String text) {
					initiative.setProperty(Label.PROPERTY_TEXT, text);
					setRemote(initiative.getID(), Label.PROPERTY_TEXT, text);
				}
			});
		}
		initiative.addPropertyChangeListener(listener);

		xField = createDoubleControl(initiative, Initiative.PROPERTY_X);
		yField = createDoubleControl(initiative, Initiative.PROPERTY_Y);
		alphaSlider = createSliderControl(initiative, Initiative.PROPERTY_ALPHA);
		colorPanel = createColorControl(initiative, Initiative.PROPERTY_COLOR);
		bgColorPanel = createColorControl(initiative, Initiative.PROPERTY_BACKGROUND_COLOR);
		rotationsCombo = createRotationControl(initiative, Initiative.PROPERTY_ROTATIONS, Mode.BOTH);
		visibleCheck = createVisibilityControl(initiative);

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

	@Override
	public Initiative getElement() {
		return initiative;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
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
			setRemote(initiative.getID(), Initiative.PROPERTY_X, p.getX());
			setRemote(initiative.getID(), Initiative.PROPERTY_Y, p.getY());
			initiative.setProperty(Initiative.PROPERTY_X, p.getX());
			initiative.setProperty(Initiative.PROPERTY_Y, p.getY());
		}

		@Override
		Point2D getTargetLocation() {
			return new Point2D.Double((Double)initiative.getProperty(Initiative.PROPERTY_X),
					(Double)initiative.getProperty(Initiative.PROPERTY_Y));
		}
	};

	// ---- XML serialisation methods ----
	public final static String XML_TAG = "Initiative";

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + LineTemplate.PROPERTY_VISIBLE, visibleCheck.isSelected());
		return e;
	}

	@Override
	public void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseColorAttribute(Initiative.PROPERTY_COLOR, e, Mode.BOTH);
		parseColorAttribute(Initiative.PROPERTY_BACKGROUND_COLOR, e, Mode.BOTH);
		parseFloatAttribute(Initiative.PROPERTY_ALPHA, e, Mode.BOTH);
		parseDoubleAttribute(Initiative.PROPERTY_X, e, Mode.BOTH);
		parseDoubleAttribute(Initiative.PROPERTY_Y, e, Mode.BOTH);
		parseIntegerAttribute(Initiative.PROPERTY_ROTATIONS, e, Mode.BOTH);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);
	}
}


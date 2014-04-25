package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.GridCoordinates;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;

@SuppressWarnings("serial")
public class GridCoordinatesOptionsPanel extends OptionsPanel<GridCoordinates> {
	private JTextField rulerRowField;
	private JTextField rulerColumnField;
	private JPanel colorPanel;
	private JPanel bgColorPanel;
	private JSlider alphaSlider;
	private JCheckBox visibleCheck;

	GridCoordinatesOptionsPanel(DisplayManager r) {
		super(r);
		element = new GridCoordinates();
		display.addElement(element, null);
		element.addPropertyChangeListener(listener);

		rulerRowField = createNullableIntegerControl(GridCoordinates.PROPERTY_RULER_ROW, Mode.REMOTE);
		rulerColumnField = createNullableIntegerControl(GridCoordinates.PROPERTY_RULER_COLUMN, Mode.REMOTE);

		// set local options
		element.setProperty(GridCoordinates.PROPERTY_RULER_COLUMN, 0);
		element.setProperty(GridCoordinates.PROPERTY_RULER_ROW, 0);

		colorPanel = createColorControl(GridCoordinates.PROPERTY_COLOR);
		bgColorPanel = createColorControl(GridCoordinates.PROPERTY_BACKGROUND_COLOR);
		alphaSlider = createSliderControl(GridCoordinates.PROPERTY_ALPHA);

		visibleCheck = createVisibilityControl();
		visibleCheck.setSelected(true);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		add(visibleCheck, c);
		add(new JLabel("Ruler Row:"), c);
		add(new JLabel("Ruler Column:"), c);
		add(new JLabel("Colour:"), c);
		add(new JLabel("Background:"), c);
		add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 1;
		add(rulerRowField, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(rulerColumnField, c);
		add(colorPanel, c);
		add(bgColorPanel, c);
		add(alphaSlider, c);

		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 6;
		add(new JSeparator(SwingConstants.HORIZONTAL), c);

		c.gridy = 10;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		add(new JPanel(), c);
	}

	private JTextField createNullableIntegerControl(final String property, final Mode mode) {
		final JTextField field = new JTextField(8);
		if (element.getProperty(property) != null) field.setText("" + element.getProperty(property));
		field.addActionListener(e -> {
			Integer newValue = null;
			if (field.getText().length() > 0) newValue = Integer.parseInt(field.getText());
			display.setProperty(element, property, newValue, mode);
		});
		return field;
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(GridCoordinates.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int) (100 * (Float) e.getNewValue()));

			} else if (e.getPropertyName().equals(GridCoordinates.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(GridCoordinates.PROPERTY_BACKGROUND_COLOR)) {
				bgColorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(GridCoordinates.PROPERTY_RULER_ROW)) {
				// don't care about local changes:
//				rulerRowField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(GridCoordinates.PROPERTY_RULER_COLUMN)) {
				// don't care about local changes:
//				rulerColumnField.setText(e.getNewValue().toString());

			} else {
				System.out.println("Unknown property: " + e.getPropertyName());
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "GridCoordinates";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		setAttribute(e, REMOTE_PREFIX + GridCoordinates.PROPERTY_RULER_ROW, rulerRowField.getText());
		setAttribute(e, REMOTE_PREFIX + GridCoordinates.PROPERTY_RULER_COLUMN, rulerColumnField.getText());
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseColorAttribute(GridCoordinates.PROPERTY_COLOR, e, Mode.ALL);
		parseColorAttribute(GridCoordinates.PROPERTY_BACKGROUND_COLOR, e, Mode.ALL);
		parseFloatAttribute(GridCoordinates.PROPERTY_ALPHA, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
		parseIntegerAttribute(GridCoordinates.PROPERTY_RULER_ROW, e, rulerRowField);
		parseIntegerAttribute(GridCoordinates.PROPERTY_RULER_COLUMN, e, rulerColumnField);
	}
}

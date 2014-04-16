package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
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
import digital_table.elements.Grid;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;

// TODO clean up nullable-Integer fields - maybe promote code to super

@SuppressWarnings("serial")
class GridOptionsPanel extends OptionsPanel<Grid> {
	private JTextField rulerRowField;
	private JTextField rulerColumnField;
	private JPanel colorPanel;
	private JPanel bgColorPanel;
	private JSlider alphaSlider;
	private JCheckBox visibleCheck;

	private JTextField xOffsetField;
	private JTextField yOffsetField;
	private MiniMapCanvas canvas;

	GridOptionsPanel(DisplayManager r, MiniMapCanvas mmc) {
		super(r);
		canvas = mmc;
		element = new Grid();
		display.addElement(element, null);
		element.addPropertyChangeListener(listener);

		rulerRowField = createNullableIntegerControl(Grid.PROPERTY_RULER_ROW, Mode.REMOTE);
		rulerColumnField = createNullableIntegerControl(Grid.PROPERTY_RULER_COLUMN, Mode.REMOTE);

		xOffsetField = new JTextField(8);
		xOffsetField.setText("0");
		xOffsetField.addActionListener(e -> {
			int xoffset = Integer.parseInt(xOffsetField.getText());
			int delta = xoffset - display.getXOffset();
			display.setRemoteOffset(xoffset, display.getYOffset());
			canvas.setOffset(canvas.getXOffset() + delta, canvas.getYOffset());
		});
		yOffsetField = new JTextField(8);
		yOffsetField.setText("0");
		yOffsetField.addActionListener(e -> {
			int yoffset = Integer.parseInt(yOffsetField.getText());
			int delta = yoffset - display.getYOffset();
			display.setRemoteOffset(display.getXOffset(), yoffset);
			canvas.setOffset(canvas.getXOffset(), canvas.getYOffset() + delta);
		});

		// set local options
		element.setProperty(Grid.PROPERTY_RULER_COLUMN, 0);
		element.setProperty(Grid.PROPERTY_RULER_ROW, 0);

		colorPanel = createColorControl(Grid.PROPERTY_COLOR);
		bgColorPanel = createColorControl(Grid.PROPERTY_BACKGROUND_COLOR);
		alphaSlider = createSliderControl(Grid.PROPERTY_ALPHA);

		visibleCheck = createVisibilityControl();
		visibleCheck.setSelected(true);

		JButton zoomInButton = new JButton("Zoom in");
		zoomInButton.addActionListener(e -> {
			int size = canvas.getCellSize();
			size = size + 10;
			canvas.setCellSize(size);
		});

		JButton zoomOutButton = new JButton("Zoom out");
		zoomOutButton.addActionListener(e -> {
			int size = canvas.getCellSize();
			size = size - 10;
			if (size < 10) size = 10;
			canvas.setCellSize(size);
		});

		JPanel buttons = new JPanel();
		buttons.add(zoomInButton);
		buttons.add(zoomOutButton);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		add(visibleCheck, c);
		add(new JLabel("Ruler Row:"), c);
		add(new JLabel("Ruler Column:"), c);
		add(new JLabel("Colour:"), c);
		add(new JLabel("Background:"), c);
		add(new JLabel("Transparency:"), c);
		c.gridy = 7;	// leave a gap for the separator
		add(new JLabel("X Offset:"), c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JLabel("Y Offset:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 1;
		add(rulerRowField, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(rulerColumnField, c);
		add(colorPanel, c);
		add(bgColorPanel, c);
		add(alphaSlider, c);
		c.gridy = 7;
		add(xOffsetField, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(yOffsetField, c);
		add(buttons, c);

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

	void setRemoteOffset(int x, int y) {
		display.setRemoteOffset(x, y);
		xOffsetField.setText(Integer.toString(x));
		yOffsetField.setText(Integer.toString(y));
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(Grid.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(Grid.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(Grid.PROPERTY_BACKGROUND_COLOR)) {
				bgColorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(Grid.PROPERTY_RULER_ROW)) {
				// don't care about local changes:
//				rulerRowField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Grid.PROPERTY_RULER_COLUMN)) {
				// don't care about local changes:
//				rulerColumnField.setText(e.getNewValue().toString());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Grid";
	private final static String X_OFFSET_ATTRIBUTE = "xoffset";
	private final static String Y_OFFSET_ATTRIBUTE = "yoffset";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		setAttribute(e, REMOTE_PREFIX + Grid.PROPERTY_RULER_ROW, rulerRowField.getText());
		setAttribute(e, REMOTE_PREFIX + Grid.PROPERTY_RULER_COLUMN, rulerColumnField.getText());
		setAttribute(e, X_OFFSET_ATTRIBUTE, Integer.toString(display.getXOffset()));
		setAttribute(e, Y_OFFSET_ATTRIBUTE, Integer.toString(display.getYOffset()));
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseColorAttribute(Grid.PROPERTY_COLOR, e, Mode.ALL);
		parseColorAttribute(Grid.PROPERTY_BACKGROUND_COLOR, e, Mode.ALL);
		parseFloatAttribute(Grid.PROPERTY_ALPHA, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
		parseIntegerAttribute(Grid.PROPERTY_RULER_ROW, e, rulerRowField);
		parseIntegerAttribute(Grid.PROPERTY_RULER_COLUMN, e, rulerColumnField);
		String xoff = e.getAttribute(X_OFFSET_ATTRIBUTE);
		String yoff = e.getAttribute(Y_OFFSET_ATTRIBUTE);
		int xoffset = 0;
		int yoffset = 0;
		if (xoff.length() > 0) xoffset = Integer.parseInt(xoff);
		if (yoff.length() > 0) yoffset = Integer.parseInt(yoff);
		setRemoteOffset(xoffset, yoffset);
		canvas.setOffset(xoffset, yoffset);
	}
}

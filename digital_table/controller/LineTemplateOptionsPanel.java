package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import digital_table.server.MediaManager;

@SuppressWarnings("serial")
class LineTemplateOptionsPanel extends OptionsPanel<LineTemplate> {
	private JTextField xField;
	private JTextField yField;
	private JTextField rangeField;
	private JTextField originXField;
	private JTextField originYField;
	private JPanel colorPanel;
	private JTextField labelField;
	private JSlider alphaSlider;
	private JCheckBox visibleCheck;
	private JCheckBox imageVisibleCheck;

	LineTemplateOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new LineTemplate(18, 14, 21, 7);
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		xField = createIntegerControl(LineTemplate.PROPERTY_X);
		yField = createIntegerControl(LineTemplate.PROPERTY_Y);
		originXField = createIntegerControl(LineTemplate.PROPERTY_ORIGIN_X);
		originYField = createIntegerControl(LineTemplate.PROPERTY_ORIGIN_Y);
		rangeField = createIntegerControl(LineTemplate.PROPERTY_RANGE);
		colorPanel = createColorControl(LineTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(LineTemplate.PROPERTY_ALPHA);
		labelField = createStringControl(LineTemplate.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl();
		imageVisibleCheck = createCheckBox(LineTemplate.PROPERTY_IMAGE_VISIBLE, Mode.ALL, "Image visible?");

		JButton imageButton = new JButton("Set Image");
		imageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				URI uri = MediaManager.INSTANCE.showFileChooser(LineTemplateOptionsPanel.this);
				if (uri != null) display.setMedia(element, LineTemplate.PROPERTY_IMAGE, uri);
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		add(visibleCheck, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JLabel("Origin X:"), c);
		add(new JLabel("Origin Y:"), c);
		add(new JLabel("Target X:"), c);
		add(new JLabel("Target Y:"), c);
		add(new JLabel("Range:"), c);
		add(new JLabel("Colour:"), c);
		add(new JLabel("Transparency:"), c);
		add(imageVisibleCheck, c);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridwidth = 2;
		add(new JPanel(), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.weighty = 0.0d;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 0;
		add(labelField, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(originXField, c);
		add(originYField, c);
		add(xField, c);
		add(yField, c);
		add(rangeField, c);
		add(colorPanel, c);
		add(alphaSlider, c);
		add(imageButton, c);
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_ORIGIN_X)) {
				originXField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_ORIGIN_Y)) {
				originYField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_RANGE)) {
				rangeField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_IMAGE_VISIBLE)) {
				imageVisibleCheck.setSelected((Boolean) e.getNewValue());

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

			if (gridLocation.distance((Integer) element.getProperty(LineTemplate.PROPERTY_ORIGIN_X),
					(Integer) element.getProperty(LineTemplate.PROPERTY_ORIGIN_Y)) < 2.0d
					&& gridLocation.distance((Integer) element.getProperty(LineTemplate.PROPERTY_ORIGIN_X),
							(Integer) element.getProperty(LineTemplate.PROPERTY_ORIGIN_Y))
							< gridLocation.distance((Integer) element.getProperty(LineTemplate.PROPERTY_X), (Integer) element.getProperty(LineTemplate.PROPERTY_Y))) {
				return LineTemplate.PROPERTY_ORIGIN_LOCATION;
			} else {
				return LineTemplate.PROPERTY_TARGET_LOCATION;
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "LineTemplate";

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

		parseStringAttribute(LineTemplate.PROPERTY_LABEL, e, Mode.LOCAL);
		parseColorAttribute(LineTemplate.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(LineTemplate.PROPERTY_ALPHA, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_X, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_ORIGIN_X, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_ORIGIN_Y, e, Mode.ALL);
		parseIntegerAttribute(LineTemplate.PROPERTY_RANGE, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
	}
}

package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
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
import digital_table.elements.ShapeableTemplate;

@SuppressWarnings("serial")
class ShapeableTemplateOptionsPanel extends OptionsPanel {
	private ShapeableTemplate template;
	private JPanel colorPanel;
	private JTextField labelField;
	private JTextField maximumField;
	private JSlider alphaSlider;
	private JLabel remaining = new JLabel();
	private JCheckBox visibleCheck;

	ShapeableTemplateOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		template = new ShapeableTemplate();
		display.addElement(template, parent);
		template.setProperty(MapElement.PROPERTY_VISIBLE, true);
		template.addPropertyChangeListener(listener);

		colorPanel = createColorControl(template, ShapeableTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(template, ShapeableTemplate.PROPERTY_ALPHA);
		maximumField = createIntegerControl(template, ShapeableTemplate.PROPERTY_MAXIMUM);
		labelField = createStringControl(template, ShapeableTemplate.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl(template);
		updateRemaining();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Maximum:"), c);
		c.gridy++; add(new JLabel("Remaining:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(maximumField, c);
		c.gridy++; add(remaining, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);

	}

	@Override
	ShapeableTemplate getElement() {
		return template;
	}

	private void updateRemaining() {
		int max = (Integer)template.getProperty(ShapeableTemplate.PROPERTY_MAXIMUM);
		if (max == 0) {
			remaining.setText("");
		} else {
			remaining.setText("" + (max - template.getPlaced()));
		}
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_MAXIMUM)) {
				maximumField.setText(e.getNewValue().toString());
				updateRemaining();

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_PLACED)) {
				updateRemaining();

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	@Override
	MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	private MapElementMouseListener mouseListener = new MapElementMouseListener() {
		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;

			// get nearest grid intersection
			int x = (int)(gridloc.getX() + 0.5d);
			int y = (int)(gridloc.getY() + 0.5d);
			Point p = new Point(x,y);
			if (template.contains(p)) {
				display.setProperty(template, ShapeableTemplate.PROPERTY_REMOVECUBE, p, Mode.REMOTE);
				template.removeCube(p);
			} else {
				display.setProperty(template, ShapeableTemplate.PROPERTY_ADDCUBE, p, Mode.REMOTE);
				template.addCube(p);
			}
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {}
		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {}
		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "ShapeableTemplate";
	private final static String CUBE_LIST_ATTRIBUTE = "cube_list";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected());

		// output the current list of points in an attribute (might be better to have a more
		// structured output but that will complicate general parsing of child elements).
		// points are output as a list of coordinates, one point at a time, x then y coordinate.
		Point[] points = template.getCubes();
		String attr = "";
		for (int i = 0; i < points.length; i++) {
			attr += points[i].x + "," + points[i].y + ",";
		}
		if (attr.length() > 0) {
			attr = attr.substring(0, attr.length() - 1);
			e.setAttribute(CUBE_LIST_ATTRIBUTE, attr);
		}

		return e;
	}

	@Override
	void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(ShapeableTemplate.PROPERTY_LABEL, e, Mode.LOCAL);
		parseColorAttribute(ShapeableTemplate.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(ShapeableTemplate.PROPERTY_ALPHA, e, Mode.ALL);
		parseIntegerAttribute(ShapeableTemplate.PROPERTY_MAXIMUM, e, Mode.ALL);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);

		if (e.hasAttribute(CUBE_LIST_ATTRIBUTE)) {
			String[] coords = e.getAttribute(CUBE_LIST_ATTRIBUTE).split("\\s*,\\s*");
			for (int i = 0; i < coords.length; i += 2) {
				Point p = new Point(Integer.parseInt(coords[i]), Integer.parseInt(coords[i + 1]));
				display.setProperty(template, ShapeableTemplate.PROPERTY_ADDCUBE, p, Mode.REMOTE);
				template.addCube(p);
			}
		}
	}
}

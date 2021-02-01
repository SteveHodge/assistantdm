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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Layer;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.ShapeableTemplate;

@SuppressWarnings("serial")
class ShapeableTemplateOptionsPanel extends OptionsPanel<ShapeableTemplate> {
	private JComboBox<Layer> layerCombo;
	private JPanel colorPanel;
	private JTextField labelField;
	private JTextField maximumField;
	private JSlider alphaSlider;
	private JLabel remaining = new JLabel();
	private JCheckBox visibleCheck;

	ShapeableTemplateOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new ShapeableTemplate();
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		layerCombo = createComboControl(MapElement.PROPERTY_LAYER, Layer.values());
		colorPanel = createColorControl(ShapeableTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(ShapeableTemplate.PROPERTY_ALPHA);
		maximumField = createIntegerControl(ShapeableTemplate.PROPERTY_MAXIMUM);
		labelField = createStringControl(ShapeableTemplate.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl();
		updateRemaining();

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(new JLabel("Layer"), c);
		c.gridy++; add(new JLabel("Label"), c);
		c.gridy++; add(new JLabel("Maximum:"), c);
		c.gridy++; add(new JLabel("Remaining:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.gridx = 2;
		c.gridy = 0;
		add(visibleCheck, c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(layerCombo, c);
		c.gridwidth = 2;
		c.gridy++; add(labelField, c);
		c.gridy++; add(maximumField, c);
		c.gridy++; add(remaining, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 3;
		add(new JPanel(), c);
		//@formatter:on
	}

	private void updateRemaining() {
		int max = (Integer) element.getProperty(ShapeableTemplate.PROPERTY_MAXIMUM);
		if (max == 0) {
			remaining.setText("");
		} else {
			remaining.setText("" + (max - element.getPlaced()));
		}
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_LAYER)) {
				layerCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_ALPHA)) {
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
			int x = (int) (Math.round(gridloc.getX()));
			int y = (int) (Math.round(gridloc.getY()));
			Point p = new Point(x,y);
			if (element.contains(p)) {
				display.setProperty(element, ShapeableTemplate.PROPERTY_REMOVECUBE, p, Mode.REMOTE);
				element.removeCube(p);
			} else {
				display.setProperty(element, ShapeableTemplate.PROPERTY_ADDCUBE, p, Mode.REMOTE);
				element.addCube(p);
			}
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {}
		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {}
		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {}

		@Override
		public MapElement getCoordElement() {
			return element;
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "ShapeableTemplate";
	private final static String CUBE_LIST_ATTRIBUTE = "cube_list";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		setCellListAttribute(e, CUBE_LIST_ATTRIBUTE, element.getCubes());
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(ShapeableTemplate.PROPERTY_LABEL, e, Mode.LOCAL);
		parseColorAttribute(ShapeableTemplate.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(ShapeableTemplate.PROPERTY_ALPHA, e, Mode.ALL);
		parseIntegerAttribute(ShapeableTemplate.PROPERTY_MAXIMUM, e, Mode.ALL);
		parseCellList(ShapeableTemplate.PROPERTY_ADDCUBE, e, CUBE_LIST_ATTRIBUTE, Mode.ALL);

		parseEnumAttribute(MapElement.PROPERTY_LAYER, Layer.class, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
	}
}

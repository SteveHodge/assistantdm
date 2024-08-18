package digital_table.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Drawing;
import digital_table.elements.Drawing.Fill;
import digital_table.elements.Drawing.Line;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Layer;
import digital_table.elements.MapElement.Visibility;

//TODO color chooser could use a proper recent list like the led controller has. Or have standard and recent colours in the ui

@SuppressWarnings("serial")
class DrawingOptionsPanel extends OptionsPanel<Drawing> {
	private JComboBox<Layer> layerCombo;
	private JPanel colorPanel;
	private JTextField labelField;
	private JSlider alphaSlider;
	private JCheckBox visibleCheck;
	private Point lineStart;	// stores the starting point of a line while dragging
	private boolean addingFill;	// indicates whether we're adding or removing fill while dragging
	private JRadioButton fillButton;
	private JRadioButton lineButton;
	private JDialog colorChooser;

	DrawingOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new Drawing();
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		layerCombo = createComboControl(MapElement.PROPERTY_LAYER, Layer.values());

		JColorChooser chooserPane = new JColorChooser();
		colorChooser = JColorChooser.createDialog(DrawingOptionsPanel.this, "Choose colour", true, chooserPane, e -> {
			colorPanel.setBackground(chooserPane.getColor());
		}, null);

		colorPanel = new JPanel();
		colorPanel.setBackground(Color.BLACK);
		colorPanel.setOpaque(true);
		colorPanel.setMinimumSize(new Dimension(50, 20));
		colorPanel.setPreferredSize(new Dimension(50, 20));
		colorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		colorPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				chooserPane.setColor(colorPanel.getBackground());
				colorChooser.setVisible(true);
			}
		});

		alphaSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
		alphaSlider.setValue(100);
//		alphaSlider.addChangeListener(e -> {
//			JSlider slider = (JSlider) e.getSource();
//			float alpha = slider.getValue() / 100f;
//		});

		labelField = createStringControl(Drawing.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl();

		fillButton = new JRadioButton("Fill");
		fillButton.setSelected(true);
		lineButton = new JRadioButton("Line");
		ButtonGroup group = new ButtonGroup();
		group.add(fillButton);
		group.add(lineButton);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(fillButton);
		buttonPanel.add(lineButton);

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(new JLabel("Layer:"), c);
		c.gridy++; add(new JLabel("Label:"), c);
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
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		add(buttonPanel, c);
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		add(createTipsPanel("In fill mode, dragging toggles squares"), c);
		//@formatter:on
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_LAYER)) {
				layerCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(Drawing.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	@Override
	MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	private Color getColor() {
		Color c = colorPanel.getBackground();
		c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255 * alphaSlider.getValue() / 100);
		return c;
	}

	private MapElementMouseListener mouseListener = new MapElementMouseListener() {
		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;

			if (fillButton.isSelected()) {
				int x = (int) (Math.floor(gridloc.getX()));
				int y = (int) (Math.floor(gridloc.getY()));
				Point p = new Point(x,y);
				if (element.containsFill(p)) {
					display.setProperty(element, Drawing.PROPERTY_REMOVE_FILL, p, Mode.REMOTE);
					element.removeFill(p);
				} else {
					Fill f = new Fill(x, y, getColor());
					display.setProperty(element, Drawing.PROPERTY_ADD_FILL, f, Mode.REMOTE);
					element.addFill(f);
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (lineButton.isSelected() && lineStart == null) {
				// get nearest grid intersection
				int x = (int) (Math.round(gridloc.getX()));
				int y = (int) (Math.round(gridloc.getY()));
				lineStart = new Point(x, y);
			}
			if (fillButton.isSelected()) {
				int x = (int) (Math.floor(gridloc.getX()));
				int y = (int) (Math.floor(gridloc.getY()));
				addingFill = !element.containsFill(new Point(x, y));
			}
		}

		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (lineButton.isSelected() && lineStart != null) {
				// get nearest grid intersection
				int x = (int) (Math.round(gridloc.getX()));
				int y = (int) (Math.round(gridloc.getY()));
				if (lineStart.x != x || lineStart.y != y) {
					Line l = new Line(lineStart.x, lineStart.y, x, y, getColor());
					if (element.containsLine(l)) {
						display.setProperty(element, Drawing.PROPERTY_REMOVE_LINE, l, Mode.REMOTE);
						element.removeLine(l);
					} else {
						display.setProperty(element, Drawing.PROPERTY_ADD_LINE, l, Mode.REMOTE);
						element.addLine(l);
					}
				}
				lineStart = null;
				element.clearTempLine();
			}
		}

		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {
			if (lineButton.isSelected() && lineStart != null) {
				int x = (int) (Math.round(gridloc.getX()));
				int y = (int) (Math.round(gridloc.getY()));
				element.setTempLine(lineStart, x, y, getColor());
			}
			if (fillButton.isSelected()) {
				int x = (int) (Math.floor(gridloc.getX()));
				int y = (int) (Math.floor(gridloc.getY()));
				Point p = new Point(x, y);
				if (addingFill && !element.containsFill(p)) {
					Fill f = new Fill(x, y, getColor());
					display.setProperty(element, Drawing.PROPERTY_ADD_FILL, f, Mode.REMOTE);
					element.addFill(f);
				} else if (!addingFill && element.containsFill(p)) {
					display.setProperty(element, Drawing.PROPERTY_REMOVE_FILL, p, Mode.REMOTE);
					element.removeFill(p);
				}
			}
		}

		@Override
		public MapElement getCoordElement() {
			return element;
		}
	};

// ---- XML serialisation methods ----
	final static String XML_TAG = "Drawing";
	private final static String FILL_LIST_ATTRIBUTE = "fill_list";
	private final static String LINE_LIST_ATTRIBUTE = "line_list";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		setFillListAttribute(e, FILL_LIST_ATTRIBUTE, element.getFills());
		setLineListAttribute(e, LINE_LIST_ATTRIBUTE, element.getLines());
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(Drawing.PROPERTY_LABEL, e, Mode.LOCAL);
		parseFillList(Drawing.PROPERTY_ADD_FILL, e, FILL_LIST_ATTRIBUTE, Mode.ALL);
		parseLineList(Drawing.PROPERTY_ADD_LINE, e, LINE_LIST_ATTRIBUTE, Mode.ALL);

		parseEnumAttribute(MapElement.PROPERTY_LAYER, Layer.class, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
	}

	void setLineListAttribute(Element e, String name, Line[] lines) {
		String attr = "";
		for (int i = 0; i < lines.length; i++) {
			String color = String.format("#%08x", lines[i].color.getRGB()).toUpperCase();
			attr += lines[i].x1 + "," + lines[i].y1 + "," + lines[i].x2 + "," + lines[i].y2 + "," + color + ";";
		}
		if (attr.length() > 0) {
			attr = attr.substring(0, attr.length() - 1);	// remove final semi-colon
			e.setAttribute(name, attr);
		}
	}

	void setFillListAttribute(Element e, String name, Fill[] fills) {
		// output the current list of points in an attribute (might be better to have a more
		// structured output but that will complicate general parsing of child elements).
		// fills are output as a list of coordinates and colours, one point at a time, x then y coordinate, then colour.
		// fills are separated by semi-colons, elements of each fill are separated by commas.
		String attr = "";
		for (int i = 0; i < fills.length; i++) {
			String color = String.format("#%08x", fills[i].color.getRGB()).toUpperCase();
			attr += fills[i].gridx + "," + fills[i].gridy + "," + color + ";";
		}
		if (attr.length() > 0) {
			attr = attr.substring(0, attr.length() - 1);	// remove final semi-colon
			e.setAttribute(name, attr);
		}
	}

	void parseFillList(String property, Element domElement, String attribute, Mode mode) {
		String attr = domElement.getAttribute(attribute);
		if (attr.length() > 0) {
			for (String fillStr : attr.split("\\s*;\\s*")) {
				String[] parts = fillStr.split("\\s*,\\s*");
				if (parts.length == 3) {
					Fill f = new Fill();
					f.gridx = Integer.parseInt(parts[0]);
					f.gridy = Integer.parseInt(parts[1]);
					f.color = new Color(Long.decode(parts[2]).intValue(), true);
					display.setProperty(element, property, f, mode);
				} else {
					System.err.println("Bad Drawing fill attribute: " + fillStr);
				}
			}
		}
	}

	void parseLineList(String property, Element domElement, String attribute, Mode mode) {
		String attr = domElement.getAttribute(attribute);
		if (attr.length() > 0) {
			for (String fillStr : attr.split("\\s*;\\s*")) {
				String[] parts = fillStr.split("\\s*,\\s*");
				if (parts.length == 5) {
					Line l = new Line();
					l.x1 = Integer.parseInt(parts[0]);
					l.y1 = Integer.parseInt(parts[1]);
					l.x2 = Integer.parseInt(parts[2]);
					l.y2 = Integer.parseInt(parts[3]);
					l.color = new Color(Long.decode(parts[4]).intValue(), true);
					display.setProperty(element, property, l, mode);
				} else {
					System.err.println("Bad Drawing line attribute: " + fillStr);
				}
			}
		}
	}
}

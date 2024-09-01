package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Group;
import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Layer;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.MapImage;
import digital_table.server.MediaManager;

@SuppressWarnings("serial")
class ImageOptionsPanel extends OptionsPanel<MapImage> {
	protected URI uri = null;

	private JComboBox<Layer> layerCombo;
	private JTextField xField;
	private JTextField yField;
	private JTextField widthField;
	private JTextField heightField;
	private JSlider alphaSlider;
	private JTextField labelField;
	private JComboBox<String> rotationsCombo;
	private JComboBox<String> dmVisCombo;
	private JCheckBox snapCheck;
	private JCheckBox positionLockCheck;
	JCheckBox visibleCheck;		// accessed by ControllerFrame
	private JCheckBox borderCheck;
	private JCheckBox aspectCheck;
	private JCheckBox backgroundCheck;
	private JCheckBox mirrorCheck;
	private JPanel colorPanel;

	private MaskOptionsPanel mask = null;

	ImageOptionsPanel(URI uri, MapElement parent, DisplayManager r, final ElementFactory<MaskOptionsPanel> maskFactory, final ElementFactory<POIOptionsPanel> poiFactory,
			final ElementFactory<WallsOptionsPanel> wallsFactory) {
		super(r);

		Element mapNode = getMapNode(uri);
		if (mapNode != null) {
			try {
				uri = new URI(mapNode.getAttribute("uri"));
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
		this.uri = uri;

		String label = MediaManager.INSTANCE.getFile(uri).getName();
		element = new MapImage(label);
		if (parent == null) {
			element.setProperty(Group.PROPERTY_X, (double) r.getXOffset());
			element.setProperty(Group.PROPERTY_Y, (double) r.getYOffset());
		}
		display.addElement(element, parent);
		display.setMedia(element, MapImage.PROPERTY_IMAGE, uri);
		// RemoteImageDisplays will set the size of the image incorrectly so we need to manually reset the size here
		display.setProperty(element, MapImage.PROPERTY_WIDTH, element.getProperty(MapImage.PROPERTY_WIDTH), Mode.REMOTE);
		display.setProperty(element, MapImage.PROPERTY_HEIGHT, element.getProperty(MapImage.PROPERTY_HEIGHT), Mode.REMOTE);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		layerCombo = createComboControl(MapElement.PROPERTY_LAYER, Layer.values());
		xField = createDoubleControl(Group.PROPERTY_X);
		yField = createDoubleControl(Group.PROPERTY_Y);
		widthField = createDoubleControl(MapImage.PROPERTY_WIDTH);
		heightField = createDoubleControl(MapImage.PROPERTY_HEIGHT);
		alphaSlider = createSliderControl(MapImage.PROPERTY_ALPHA);
		rotationsCombo = createRotationControl(MapImage.PROPERTY_ROTATIONS, Mode.ALL);
		labelField = createStringControl(MapImage.PROPERTY_LABEL, Mode.LOCAL);
		borderCheck = createCheckBox(MapImage.PROPERTY_SHOW_BORDER, Mode.LOCAL, "show border?");
		aspectCheck = createCheckBox(MapImage.PROPERTY_ASPECT_LOCKED, Mode.ALL, "aspect locked?");
		aspectCheck.setSelected(true);
		mirrorCheck = createCheckBox(MapImage.PROPERTY_MIRRORED, Mode.ALL, "mirrored?");
		backgroundCheck = createCheckBox(MapImage.PROPERTY_SHOW_BACKGROUND, Mode.ALL, "show background?");
		colorPanel = createColorControl(MapImage.PROPERTY_BACKGROUND_COLOR);
		snapCheck = new JCheckBox("snap to grid?");
		snapCheck.setSelected(true);
		positionLockCheck = new JCheckBox("lock position?");

		visibleCheck = new JCheckBox("visible?");
		visibleCheck.setSelected(false);
		visibleCheck.addItemListener(e -> {
			if (visibleCheck.isSelected()) {
				display.setProperty(element, MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE, Mode.REMOTE);
				if (dmVisCombo.getSelectedItem().equals("Hidden")) {
					display.setProperty(element, MapElement.PROPERTY_VISIBLE, Visibility.FADED, Mode.LOCAL);
				}
			} else {
				display.setProperty(element, MapElement.PROPERTY_VISIBLE, Visibility.HIDDEN, Mode.REMOTE);
				if (dmVisCombo.getSelectedItem().equals("Visible")) {
					display.setProperty(element, MapElement.PROPERTY_VISIBLE, Visibility.FADED, Mode.LOCAL);
				}
			}
		});

		dmVisCombo = new JComboBox<>(new String[] { "Visible", "Faded", "Hidden" });
		dmVisCombo.setSelectedItem("Visible");
		dmVisCombo.addActionListener(e -> {
			Object selected = dmVisCombo.getSelectedItem();
			Visibility v = Visibility.valueOf(selected.toString().toUpperCase());
			if (v != null) display.setProperty(element, MapElement.PROPERTY_VISIBLE, v, Mode.LOCAL);
		});

		JButton playButton = new JButton("Play");
		playButton.addActionListener(e -> display.setProperty(element, LineTemplate.PROPERTY_IMAGE_PLAY, null));

		JButton stopButton = new JButton("Stop");
		stopButton.addActionListener(e -> display.setProperty(element, LineTemplate.PROPERTY_IMAGE_STOP, null));

		JPanel imagePanel = new JPanel();
		imagePanel.add(playButton);
		imagePanel.add(stopButton);

		final JButton addMaskButton = new JButton("Add Mask");
		addMaskButton.addActionListener(e -> {
			if (mask == null) {
				mask = maskFactory.addElement(element);
			}
		});

		final JButton addPOIsButton = new JButton("Add POIs");
		addPOIsButton.addActionListener(e -> {
			poiFactory.addElement(element);
		});

		final JButton addWallsButton = new JButton("Add Walls");
		addWallsButton.addActionListener(e -> {
			wallsFactory.addElement(element);
		});

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JLabel("Layer: "), c);
		add(new JLabel("DM visibility: "), c);
		add(new JLabel("Local label:"), c);
		add(new JLabel("Left edge column:"), c);
		add(new JLabel("Top edge Row:"), c);
		add(new JLabel("Width:"), c);
		add(new JLabel("Height:"), c);
		add(new JLabel("Rotation:"), c);
		add(mirrorCheck, c);
		add(new JLabel("Transparency:"), c);
		add(backgroundCheck, c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		add(borderCheck, c);
		add(layerCombo, c);
		add(dmVisCombo, c);
		add(labelField, c);
		add(xField, c);
		add(yField, c);
		add(widthField, c);
		add(heightField, c);
		add(rotationsCombo, c);
		add(new JPanel(), c);
		add(alphaSlider, c);
		add(colorPanel, c);
		JPanel checks = new JPanel();
		checks.add(snapCheck);
		checks.add(aspectCheck);
		checks.add(positionLockCheck);
		add(checks, c);
		JPanel panel = new JPanel();
		panel.add(addMaskButton);
		panel.add(addWallsButton);
		panel.add(addPOIsButton);
		add(panel, c);
		add(imagePanel, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridx = 0;
		c.gridwidth = 2;
		add(createTipsPanel("Left drag: move image\nDouble left click: move to selected square\nRight click: toggle square"), c);
		//@formatter:on

		if (mapNode != null) parseMapNode(mapNode, maskFactory, poiFactory);
	}

	private Element getMapNode(URI uri) {
		if (uri.getPath().endsWith(".map")) {
			File file = MediaManager.INSTANCE.getFile(uri);
			System.out.println("Opening map " + file.getAbsolutePath());

			Document dom = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//InputStream is = p.getClass().getClassLoader().getResourceAsStream("party.xsd");
			//factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
			try {
				dom = factory.newDocumentBuilder().parse(file);
			} catch (SAXException | IOException | ParserConfigurationException ex) {
				ex.printStackTrace();
			}

			if (dom != null) {
				Element mapNode = dom.getDocumentElement();
				if (mapNode.getNodeName().equals("Map")) return mapNode;
			}
		}
		return null;
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				String s = e.getNewValue().toString();
				s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
				dmVisCombo.setSelectedItem(s);

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_LAYER)) {
				layerCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_LOCATION)) {
				Point2D loc = (Point2D) e.getNewValue();
				xField.setText(Double.toString(loc.getX()));
				yField.setText(Double.toString(loc.getY()));

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_WIDTH)) {
				widthField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_HEIGHT)) {
				heightField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer)e.getNewValue());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_MIRRORED)) {
				mirrorCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_SHOW_BORDER)) {
				borderCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_SHOW_BACKGROUND)) {
				backgroundCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_BACKGROUND_COLOR)) {
				colorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_ASPECT_LOCKED)) {
				aspectCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_DRAGGING)) {
				// ignore

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
		Point2D imageOffset = null;

		@Override
		String getDragTarget(Point2D gridLocation) {
			return "location";
		}

		@Override
		void setTargetLocation(Point2D p) {
			double x = p.getX();
			double y = p.getY();
			if (snapCheck.isSelected()) {
				x = Math.floor(x) + imageOffset.getX();
				y = Math.floor(y) + imageOffset.getY();
			}
			display.setProperty(element, Group.PROPERTY_X, x);
			display.setProperty(element, Group.PROPERTY_Y, y);
		}

		@Override
		Point2D getTargetLocation() {
			return new Point2D.Double((Double)element.getProperty(Group.PROPERTY_X),
					(Double)element.getProperty(Group.PROPERTY_Y));
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {
			super.mousePressed(e, gridloc);
			Point2D p = getTargetLocation();
			imageOffset = new Point2D.Double(p.getX() - Math.floor(p.getX()), p.getY() - Math.floor(p.getY()));
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
				// right-click: toggle cell
				Point p = gridCell(gridloc);
				boolean clear = !element.isCleared(p);
				element.setCleared(p, clear);
				if (clear) {
					display.setProperty(element, MapImage.PROPERTY_CLEARCELL, p, Mode.REMOTE);
				} else {
					display.setProperty(element, MapImage.PROPERTY_UNCLEARCELL, p, Mode.REMOTE);
				}

			} else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				// left double-click: move image to mouse
				setTargetLocation(gridCell(gridloc));
			}
		}

		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {
			if (!positionLockCheck.isSelected()) {
				super.mouseDragged(e, gridloc);
			}
		}
	};

	private void parseMapNode(Element e, ElementFactory<MaskOptionsPanel> maskFactory, ElementFactory<POIOptionsPanel> poiFactory) {
		display.setProperty(element, MapImage.PROPERTY_ASPECT_LOCKED, false, Mode.ALL);
		parseDoubleAttribute(MapImage.PROPERTY_WIDTH, e, Mode.ALL);
		parseDoubleAttribute(MapImage.PROPERTY_HEIGHT, e, Mode.ALL);

		if (e.hasAttribute(Group.PROPERTY_LOCATION)) {
//			try {
			String coords[] = e.getAttribute(Group.PROPERTY_LOCATION).split(",");
			Double x = Double.parseDouble(coords[0]);
			Double y = Double.parseDouble(coords[1]);
			Point2D value = new Point2D.Double(x, y);
			display.setProperty(element, Group.PROPERTY_LOCATION, value);
//			} catch (NumberFormatException e) {
//			}
		}

		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element el = (Element) nodes.item(i);
			if (el.getTagName().equals("MaskSet")) {
				if (mask == null) {
					mask = maskFactory.addElement(element);
				}
				mask.parseDOM(el, this);
			} else if (el.getTagName().equals("POIGroup")) {
				POIOptionsPanel poiGroup = poiFactory.addElement(element);
				poiGroup.parseDOM(el, this);
			}
		}
	}

// ---- XML serialisation methods ----
	final static String XML_TAG = "Image";
	final static String FILE_ATTRIBUTE_NAME = "uri";
	private final static String CLEARED_CELL_LIST_ATTRIBUTE = "cleared_cells";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		e.setAttribute(FILE_ATTRIBUTE_NAME, uri.toASCIIString());
		setCellListAttribute(e, CLEARED_CELL_LIST_ATTRIBUTE, element.getCells());

		Point2D location = (Point2D) element.getProperty(Group.PROPERTY_LOCATION);
		e.setAttribute(Group.PROPERTY_LOCATION, location.getX() + "," + location.getY());	// maybe should output X and Y separately
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(MapImage.PROPERTY_LABEL, e, Mode.LOCAL);
		parseFloatAttribute(MapImage.PROPERTY_ALPHA, e, Mode.ALL);
		parseDoubleAttribute(Group.PROPERTY_X, e, Mode.ALL);
		parseDoubleAttribute(Group.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(MapImage.PROPERTY_ROTATIONS, e, Mode.ALL);	// must be done before dimensions
		parseBooleanAttribute(MapImage.PROPERTY_ASPECT_LOCKED, e, Mode.ALL);	// must be done before dimensions
		parseDoubleAttribute(MapImage.PROPERTY_WIDTH, e, Mode.ALL);
		parseDoubleAttribute(MapImage.PROPERTY_HEIGHT, e, Mode.ALL);
		parseBooleanAttribute(MapImage.PROPERTY_SHOW_BORDER, e, Mode.LOCAL);
		parseBooleanAttribute(MapImage.PROPERTY_SHOW_BACKGROUND, e, Mode.ALL);
		parseBooleanAttribute(MapImage.PROPERTY_MIRRORED, e, Mode.ALL);
		parseColorAttribute(MapImage.PROPERTY_BACKGROUND_COLOR, e, Mode.ALL);
		parseCellList(MapImage.PROPERTY_CLEARCELL, e, CLEARED_CELL_LIST_ATTRIBUTE, Mode.ALL);

		if (e.hasAttribute(Group.PROPERTY_LOCATION)) {
//			try {
			String coords[] = e.getAttribute(Group.PROPERTY_LOCATION).split(",");
			Double x = Double.parseDouble(coords[0]);
			Double y = Double.parseDouble(coords[1]);
			Point2D value = new Point2D.Double(x, y);
			display.setProperty(element, Group.PROPERTY_LOCATION, value);
//			} catch (NumberFormatException e) {
//			}
		}

		parseEnumAttribute(MapElement.PROPERTY_LAYER, Layer.class, e, Mode.ALL);
		parseVisibility(e, visibleCheck);

		String attr = e.getAttribute(MapElement.PROPERTY_VISIBLE);
		if (attr.length() > 0) {
			try {
				Visibility v = Visibility.valueOf(attr);
				display.setProperty(element, MapElement.PROPERTY_VISIBLE, v, Mode.LOCAL);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			}
		}

	}
}

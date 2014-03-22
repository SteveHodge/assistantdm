package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Group;
import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.MapImage;
import digital_table.server.MediaManager;

@SuppressWarnings("serial")
class ImageOptionsPanel extends OptionsPanel<MapImage> {
	protected URI uri = null;

	private JTextField xField;
	private JTextField yField;
	private JTextField widthField;
	private JTextField heightField;
	private JSlider alphaSlider;
	private JTextField labelField;
	private JComboBox<String> rotationsCombo;
	private JCheckBox snapCheck;
	JCheckBox visibleCheck;		// accessed by ControllerFrame
	private JCheckBox borderCheck;
	private JCheckBox aspectCheck;

	private MaskOptionsPanel mask = null;

	ImageOptionsPanel(URI uri, MapElement parent, DisplayManager r, final ElementFactory<MaskOptionsPanel> maskFactory) {
		super(r);
		this.uri = uri;
		String label = MediaManager.INSTANCE.getName(uri);
		element = new MapImage(label);
		display.addElement(element, parent);
		display.setMedia(element, MapImage.PROPERTY_IMAGE, uri);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		xField = createDoubleControl(MapImage.PROPERTY_X);
		yField = createDoubleControl(MapImage.PROPERTY_Y);
		widthField = createDoubleControl(MapImage.PROPERTY_WIDTH);
		heightField = createDoubleControl(MapImage.PROPERTY_HEIGHT);
		alphaSlider = createSliderControl(MapImage.PROPERTY_ALPHA);
		rotationsCombo = createRotationControl(MapImage.PROPERTY_ROTATIONS, Mode.ALL);
		labelField = createStringControl(MapImage.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl();
		borderCheck = createCheckBox(MapImage.PROPERTY_SHOW_BORDER, Mode.LOCAL, "show border?");
		aspectCheck = createCheckBox(MapImage.PROPERTY_ASPECT_LOCKED, Mode.ALL, "aspect locked?");
		aspectCheck.setSelected(true);
		snapCheck = new JCheckBox("snap to grid?");
		snapCheck.setSelected(true);

		JButton playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				display.setProperty(element, LineTemplate.PROPERTY_IMAGE_PLAY, null);
			}
		});

		JButton stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				display.setProperty(element, LineTemplate.PROPERTY_IMAGE_STOP, null);
			}
		});

		JPanel imagePanel = new JPanel();
		imagePanel.add(playButton);
		imagePanel.add(stopButton);

		final JButton addMaskButton = new JButton("Add Mask");
		addMaskButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (mask == null) {
					mask = maskFactory.addElement(element);
				}
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JLabel("Local label:"), c);
		add(new JLabel("Left edge column:"), c);
		add(new JLabel("Top edge Row:"), c);
		add(new JLabel("Width:"), c);
		add(new JLabel("Height:"), c);
		add(new JLabel("Rotation:"), c);
		add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		add(borderCheck, c);
		add(labelField, c);
		add(xField, c);
		add(yField, c);
		add(widthField, c);
		add(heightField, c);
		add(rotationsCombo, c);
		add(alphaSlider, c);
		JPanel checks = new JPanel();
		checks.add(snapCheck);
		checks.add(aspectCheck);
		add(checks, c);
		JPanel panel = new JPanel();
		panel.add(addMaskButton);
		add(panel, c);
		add(imagePanel, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridx = 0;
		c.gridwidth = 2;
		add(new JPanel(), c);
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

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

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_SHOW_BORDER)) {
				borderCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_ASPECT_LOCKED)) {
				aspectCheck.setSelected((Boolean) e.getNewValue());

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
			double x = p.getX();
			double y = p.getY();
			if (snapCheck.isSelected()) {
				x = Math.floor(x);
				y = Math.floor(y);
			}
			display.setProperty(element, MapImage.PROPERTY_X, x);
			display.setProperty(element, MapImage.PROPERTY_Y, y);
		}

		@Override
		Point2D getTargetLocation() {
			return new Point2D.Double((Double)element.getProperty(MapImage.PROPERTY_X),
					(Double)element.getProperty(MapImage.PROPERTY_Y));
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;

			// get ancestor's relative position
			Point2D offset = new Point2D.Double();
			Group parent = element.getParent();
			while (parent != null) {
				offset = parent.translate(offset);
				parent = parent.getParent();
			}

			Point p = new Point((int) (gridloc.getX() - offset.getX()), (int) (gridloc.getY() - offset.getY()));
			boolean clear = !element.isCleared(p);
			element.setCleared(p, clear);
			if (clear) {
				display.setProperty(element, MapImage.PROPERTY_CLEARCELL, p, Mode.REMOTE);
			} else {
				display.setProperty(element, MapImage.PROPERTY_UNCLEARCELL, p, Mode.REMOTE);
			}
		}
	};

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

		// output the current list of points in an attribute (might be better to have a more
		// structured output but that will complicate general parsing of child elements).
		// points are output as a list of coordinates, one point at a time, x then y coordinate.
		Point[] points = element.getCells();
		String attr = "";
		for (int i = 0; i < points.length; i++) {
			attr += points[i].x + "," + points[i].y + ",";
		}
		if (attr.length() > 0) {
			attr = attr.substring(0, attr.length() - 1);
			e.setAttribute(CLEARED_CELL_LIST_ATTRIBUTE, attr);
		}

		Point2D location = (Point2D) element.getProperty(Group.PROPERTY_LOCATION);
		e.setAttribute(Group.PROPERTY_LOCATION, location.getX() + "," + location.getY());	// maybe should output X and Y separately
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(MapImage.PROPERTY_LABEL, e, Mode.LOCAL);
		parseFloatAttribute(MapImage.PROPERTY_ALPHA, e, Mode.ALL);
		parseDoubleAttribute(MapImage.PROPERTY_X, e, Mode.ALL);
		parseDoubleAttribute(MapImage.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(MapImage.PROPERTY_ROTATIONS, e, Mode.ALL);	// must be done before dimensions
		parseBooleanAttribute(MapImage.PROPERTY_ASPECT_LOCKED, e, Mode.ALL);	// must be done before dimensions
		parseDoubleAttribute(MapImage.PROPERTY_WIDTH, e, Mode.ALL);
		parseDoubleAttribute(MapImage.PROPERTY_HEIGHT, e, Mode.ALL);
		parseBooleanAttribute(MapImage.PROPERTY_SHOW_BORDER, e, Mode.LOCAL);

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

		if (e.hasAttribute(CLEARED_CELL_LIST_ATTRIBUTE)) {
			String[] coords = e.getAttribute(CLEARED_CELL_LIST_ATTRIBUTE).split("\\s*,\\s*");
			for (int i = 0; i < coords.length; i += 2) {
				Point p = new Point(Integer.parseInt(coords[i]), Integer.parseInt(coords[i + 1]));
				element.setCleared(p, true);
				display.setProperty(element, MapImage.PROPERTY_CLEARCELL, p, Mode.REMOTE);
			}
		}

		parseVisibility(e, visibleCheck);
	}
}

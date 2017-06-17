package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.MapImage;
import digital_table.elements.Walls;
import digital_table.server.MediaManager;

@SuppressWarnings("serial")
class WallsOptionsPanel extends OptionsPanel<Walls> {
	protected URI uri = null;

	private JPanel colorPanel;
	private JSlider alphaSlider;
	private JCheckBox visibleCheck;
	private JTextField xField;
	private JTextField yField;
	private JTextField widthField;
	private JTextField heightField;
	private JComboBox<String> rotationsCombo;
	private JCheckBox mirrorCheck;
	public static File lastDir = new File(".");	// last selected image - used to keep the current directory

	WallsOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new Walls();
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		display.addElement(element, parent);
		element.setProperty(Walls.PROPERTY_ALPHA, 0.5f);
		element.addPropertyChangeListener(listener);

		colorPanel = createColorControl(Walls.PROPERTY_COLOR);
		alphaSlider = createSliderControl(Walls.PROPERTY_ALPHA, Mode.LOCAL);
		visibleCheck = createVisibilityControl();
		visibleCheck.setSelected(false);

		JButton loadWallsButton = new JButton("Load Wall Layout");
		loadWallsButton.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (lastDir != null) chooser.setCurrentDirectory(lastDir);
				if (chooser.showOpenDialog(WallsOptionsPanel.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					loadLayout(f.toURI());
					lastDir = f;
				} else {
					System.out.println("Cancelled");
				}
			}
		});
		xField = createDoubleControl(Walls.PROPERTY_X);
		yField = createDoubleControl(Walls.PROPERTY_Y);
		widthField = createDoubleControl(Walls.PROPERTY_WIDTH);
		heightField = createDoubleControl(Walls.PROPERTY_HEIGHT);
		rotationsCombo = createRotationControl(Walls.PROPERTY_ROTATIONS, Mode.ALL);
		mirrorCheck = createCheckBox(Walls.PROPERTY_MIRRORED, Mode.ALL, "mirrored?");

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1; add(new JLabel("Colour:"), c);
		c.gridy = 2; add(new JLabel("Transparency:"), c);

		c.gridy = 4; add(new JLabel("Left edge column:"), c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JLabel("Top edge Row:"), c);
		add(new JLabel("Width:"), c);
		add(new JLabel("Height:"), c);
		add(new JLabel("Rotation:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;
		add(visibleCheck, c);
		add(colorPanel, c);
		add(alphaSlider, c);
		add(loadWallsButton, c);
		add(xField, c);
		add(yField, c);
		add(widthField, c);
		add(heightField, c);
		add(rotationsCombo, c);
		add(mirrorCheck, c);

		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		add(new JPanel(), c);

		//@formatter:on
	}

	private void loadLayout(URI uri) {
		try {
			File file = MediaManager.INSTANCE.getFile(uri);
			System.out.println("Opening wall layout " + file.getAbsolutePath());
			byte[] encoded = Files.readAllBytes(file.toPath());
			String contents = new String(encoded, StandardCharsets.UTF_8);
			display.setProperty(element, Walls.PROPERTY_WALL_LAYOUT, contents);
			this.uri = uri;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(Walls.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(Walls.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(Walls.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Walls.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Walls.PROPERTY_WIDTH)) {
				widthField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Walls.PROPERTY_HEIGHT)) {
				heightField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Walls.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer) e.getNewValue());

			} else if (e.getPropertyName().equals(Walls.PROPERTY_MIRRORED)) {
				mirrorCheck.setSelected((Boolean) e.getNewValue());

			} else {
				System.out.println("Unknown property changed: " + e.getPropertyName());
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Walls";
	final static String FILE_ATTRIBUTE_NAME = "uri";

//FIXME implement properly
	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);

		if (uri != null) e.setAttribute(FILE_ATTRIBUTE_NAME, uri.toASCIIString());

//		Point2D location = (Point2D) element.getProperty(Group.PROPERTY_LOCATION);
//		e.setAttribute(Group.PROPERTY_LOCATION, location.getX() + "," + location.getY());	// maybe should output X and Y separately
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseColorAttribute(Walls.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(Walls.PROPERTY_ALPHA, e, Mode.LOCAL);

		parseDoubleAttribute(Group.PROPERTY_X, e, Mode.ALL);
		parseDoubleAttribute(Group.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(MapImage.PROPERTY_ROTATIONS, e, Mode.ALL);	// must be done before dimensions
		parseDoubleAttribute(MapImage.PROPERTY_WIDTH, e, Mode.ALL);
		parseDoubleAttribute(MapImage.PROPERTY_HEIGHT, e, Mode.ALL);
		parseBooleanAttribute(MapImage.PROPERTY_MIRRORED, e, Mode.ALL);

		if (e.hasAttribute(FILE_ATTRIBUTE_NAME)) {
			try {
				loadLayout(new URI(e.getAttribute(FILE_ATTRIBUTE_NAME)));
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

//		if (e.hasAttribute(Group.PROPERTY_LOCATION)) {
////			try {
//			String coords[] = e.getAttribute(Group.PROPERTY_LOCATION).split(",");
//			Double x = Double.parseDouble(coords[0]);
//			Double y = Double.parseDouble(coords[1]);
//			Point2D value = new Point2D.Double(x, y);
//			display.setProperty(element, Group.PROPERTY_LOCATION, value);
////			} catch (NumberFormatException e) {
////			}
//		}

		parseVisibility(e, visibleCheck);

//		String attr = e.getAttribute(MapElement.PROPERTY_VISIBLE);
//		if (attr.length() > 0) {
//			try {
//				Visibility v = Visibility.valueOf(attr);
//				display.setProperty(element, MapElement.PROPERTY_VISIBLE, v, Mode.LOCAL);
//			} catch (IllegalArgumentException ex) {
//				ex.printStackTrace();
//			}
//		}
	}
}


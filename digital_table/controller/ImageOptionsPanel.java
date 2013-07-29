package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.elements.MapElement;
import digital_table.elements.MapImage;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class ImageOptionsPanel extends OptionsPanel {
	private MapImage image;

	private File file = null;

	private JTextField xField;
	private JTextField yField;
	private JTextField widthField;
	private JTextField heightField;
	private JSlider alphaSlider;
	private JTextField labelField;
	private JComboBox rotationsCombo;
	private JCheckBox snapCheck;
	private JCheckBox visibleCheck;

	public ImageOptionsPanel(File f, MapElement parent, TableDisplay r) {
		super(r);
		file = f;
		byte[] bytes = new byte[(int) file.length()];
		try {
			FileInputStream stream = new FileInputStream(file);
			stream.read(bytes);
		} catch (FileNotFoundException e) {
			// TODO handle exceptions correctly
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		image = new MapImage(bytes, file.getName());
		sendElement(image, parent);
		image.setProperty(MapElement.PROPERTY_VISIBLE, true);
		image.addPropertyChangeListener(listener);

		xField = createDoubleControl(image, MapImage.PROPERTY_X);
		yField = createDoubleControl(image, MapImage.PROPERTY_Y);
		widthField = createDoubleControl(image, MapImage.PROPERTY_WIDTH);
		heightField = createDoubleControl(image, MapImage.PROPERTY_HEIGHT);
		alphaSlider = createSliderControl(image, MapImage.PROPERTY_ALPHA);
		rotationsCombo = createRotationControl(image, MapImage.PROPERTY_ROTATIONS, Mode.BOTH);
		labelField = createStringControl(image, MapImage.PROPERTY_LABEL, Mode.LOCAL);
		visibleCheck = createVisibilityControl(image);

		snapCheck = new JCheckBox("snap to grid?");
		snapCheck.setSelected(true);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Left edge column:"), c);
		c.gridy++; add(new JLabel("Top edge Row:"), c);
		c.gridy++; add(new JLabel("Width:"), c);
		c.gridy++; add(new JLabel("Height:"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(widthField, c);
		c.gridy++; add(heightField, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(alphaSlider, c);
		c.gridy++; add(snapCheck, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	@Override
	public MapImage getElement() {
		return image;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapImage.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_WIDTH)) {
				widthField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_HEIGHT)) {
				heightField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer)e.getNewValue());

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
			double x = p.getX();
			double y = p.getY();
			if (snapCheck.isSelected()) {
				x = Math.floor(x);
				y = Math.floor(y);
			}
			setRemote(image.getID(), MapImage.PROPERTY_X, x);
			setRemote(image.getID(), MapImage.PROPERTY_Y, y);
			image.setProperty(MapImage.PROPERTY_X, x);
			image.setProperty(MapImage.PROPERTY_Y, y);
		}

		@Override
		Point2D getTargetLocation() {
			return new Point2D.Double((Double)image.getProperty(MapImage.PROPERTY_X),
					(Double)image.getProperty(MapImage.PROPERTY_Y));
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;

			Point p = new Point((int)gridloc.getX(), (int)gridloc.getY());
			boolean clear = !image.isCleared(p);
			image.setCleared(p, clear);
			if (clear) {
				setRemote(image.getID(), MapImage.PROPERTY_CLEARCELL, p);
			} else {
				setRemote(image.getID(), MapImage.PROPERTY_UNCLEARCELL, p);
			}
		}
	};

	// ---- XML serialisation methods ----
	public final static String XML_TAG = "Image";
	final static String FILE_ATTRIBUTE_NAME = "path";
	final static String CLEARED_CELL_LIST_ATTRIBUTE = "cleared_cells";

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected());
		e.setAttribute(FILE_ATTRIBUTE_NAME, file.getPath());

		// output the current list of points in an attribute (might be better to have a more
		// structured output but that will complicate general parsing of child elements).
		// points are output as a list of coordinates, one point at a time, x then y coordinate.
		Point[] points = image.getCells();
		String attr = "";
		for (int i = 0; i < points.length; i++) {
			attr += points[i].x + "," + points[i].y + ",";
		}
		if (attr.length() > 0) {
			attr = attr.substring(0, attr.length() - 1);
			e.setAttribute(CLEARED_CELL_LIST_ATTRIBUTE, attr);
		}

		return e;
	}

	@Override
	public void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(MapImage.PROPERTY_LABEL, e, Mode.LOCAL);
		parseFloatAttribute(MapImage.PROPERTY_ALPHA, e, Mode.BOTH);
		parseDoubleAttribute(MapImage.PROPERTY_X, e, Mode.BOTH);
		parseDoubleAttribute(MapImage.PROPERTY_Y, e, Mode.BOTH);
		parseDoubleAttribute(MapImage.PROPERTY_WIDTH, e, Mode.BOTH);
		parseDoubleAttribute(MapImage.PROPERTY_HEIGHT, e, Mode.BOTH);
		parseIntegerAttribute(MapImage.PROPERTY_ROTATIONS, e, Mode.BOTH);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);

		if (e.hasAttribute(CLEARED_CELL_LIST_ATTRIBUTE)) {
			String[] coords = e.getAttribute(CLEARED_CELL_LIST_ATTRIBUTE).split("\\s*,\\s*");
			for (int i = 0; i < coords.length; i += 2) {
				Point p = new Point(Integer.parseInt(coords[i]), Integer.parseInt(coords[i + 1]));
				image.setCleared(p, true);
				setRemote(image.getID(), MapImage.PROPERTY_CLEARCELL, p);
			}
		}
	}
}

package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.MapImage;
import digital_table.elements.Token;

@SuppressWarnings("serial")
class GroupOptionsPanel extends OptionsPanel<Group> {
	private JTextField labelField;
	private JCheckBox visibleCheck;
	private JTextField xField;
	private JTextField yField;

	GroupOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new Group();
		if (parent == null) {
			element.setProperty(Group.PROPERTY_X, (double) r.getXOffset());
			element.setProperty(Group.PROPERTY_Y, (double) r.getYOffset());
		}
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		xField = createDoubleControl(Group.PROPERTY_X);
		yField = createDoubleControl(Group.PROPERTY_Y);
		visibleCheck = createVisibilityControl();
		labelField = createStringControl(Token.PROPERTY_LABEL);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		add(visibleCheck, c);
		add(new JLabel("Left edge column:"), c);
		add(new JLabel("Top edge Row:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		add(labelField, c);
		add(xField, c);
		add(yField, c);

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

			} else if (e.getPropertyName().equals(Group.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_LOCATION)) {
				Point2D loc = (Point2D) e.getNewValue();
				xField.setText(Double.toString(loc.getX()));
				yField.setText(Double.toString(loc.getY()));

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_DRAGGING)) {
				// ignore

			} else {
				System.out.println(toString() + ": Unknown property: " + e.getPropertyName());
			}
		}
	};

	@Override
	MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	private MapElementMouseListener mouseListener = new DefaultDragger() {
		@Override
		protected String getDragTarget(Point2D gridLocation) {
			return Group.PROPERTY_LOCATION;
		}

		@Override
		void setTargetLocation(Point2D p) {
			Point gridP = new Point((int) Math.round(p.getX()), (int) Math.round(p.getY()));
			display.setProperty(element, Group.PROPERTY_LOCATION, gridP);
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Group";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAttribute(e, Group.PROPERTY_LABEL, element.getProperty(Group.PROPERTY_LABEL));
		Point2D location = (Point2D) element.getProperty(Group.PROPERTY_LOCATION);
		e.setAttribute(Group.PROPERTY_LOCATION, location.getX() + "," + location.getY());	// maybe should output X and Y separately
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(Group.PROPERTY_LABEL, e, Mode.LOCAL);

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

		parseVisibility(e, visibleCheck);
	}
}

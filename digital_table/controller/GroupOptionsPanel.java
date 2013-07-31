package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.Token;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class GroupOptionsPanel extends OptionsPanel {
	private Group group;

	private JTextField labelField;
	private JCheckBox visibleCheck;

	public GroupOptionsPanel(MapElement parent, TableDisplay r) {
		super(r);
		group = new Group();
		sendElement(group, parent);
		group.setProperty(MapElement.PROPERTY_VISIBLE, true);
		group.addPropertyChangeListener(listener);

		visibleCheck = createVisibilityControl(group);
		labelField = createStringControl(group, Token.PROPERTY_LABEL);

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		add(visibleCheck, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		add(new JPanel(), c);
		//@formatter:on
	}

	@Override
	public Group getElement() {
		return group;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Group.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());
			} else if (e.getPropertyName().equals(Group.PROPERTY_LOCATION)) {
			} else {
				System.out.println(toString() + ": Unknown property: " + e.getPropertyName());
			}
		}
	};

	@Override
	public MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	protected MapElementMouseListener mouseListener = new DefaultDragger() {
		@Override
		protected String getDragTarget(Point2D gridLocation) {
			return Group.PROPERTY_LOCATION;
		}
	};

	// ---- XML serialisation methods ----
	public final static String XML_TAG = "Group";

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAttribute(e, Group.PROPERTY_LABEL, group.getProperty(Group.PROPERTY_LABEL));
		Point2D location = (Point2D)group.getProperty(Group.PROPERTY_LOCATION);
		e.setAttribute(Group.PROPERTY_LOCATION, location.getX() + "," + location.getY());	// maybe should output X and Y separately
		setAttribute(e, REMOTE_PREFIX + Group.PROPERTY_VISIBLE, visibleCheck.isSelected());
		return e;
	}

	@Override
	public void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(Group.PROPERTY_LABEL, e, Mode.LOCAL);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);

		if (e.hasAttribute(Group.PROPERTY_LOCATION)) {
//			try {
			String coords[] = e.getAttribute(Group.PROPERTY_LOCATION).split(",");
			Double x = Double.parseDouble(coords[0]);
			Double y = Double.parseDouble(coords[1]);
			Point2D value = new Point2D.Double(x, y);
			group.setProperty(Group.PROPERTY_LOCATION, value);
			setRemote(group.getID(), Group.PROPERTY_LOCATION, value);
//			} catch (NumberFormatException e) {
//			}
		}
	}
}
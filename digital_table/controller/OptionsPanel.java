package digital_table.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.MapElement;

@SuppressWarnings("serial")
abstract class OptionsPanel extends JPanel {
	DisplayManager display;

	enum DragMode {
		NONE,	// no dragging
		MOVE,	// dragging will move the element or a subelement
		PAINT	// dragging will trigger a click event on each mouse move
	};

	OptionsPanel(DisplayManager r) {
		display = r;
	}

	abstract MapElement getElement();

	JTextField createIntegerControl(final MapElement element, final String property) {
		return createIntegerControl(element, property, Mode.ALL);
	}

	JTextField createIntegerControl(final MapElement element, final String property, final Mode mode) {
		final JTextField field = new JTextField(8);
		field.setText("" + element.getProperty(property));
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int newValue = Integer.parseInt(field.getText());
				display.setProperty(element, property, newValue, mode);
			}
		});
		return field;
	}

	JTextField createNullableIntegerControl(final MapElement element, final String property, final Mode mode) {
		final JTextField field = new JTextField(8);
		if (element.getProperty(property) != null) field.setText("" + element.getProperty(property));
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Integer newValue = null;
				if (field.getText().length() > 0) newValue = Integer.parseInt(field.getText());
				display.setProperty(element, property, newValue, mode);
			}
		});
		return field;
	}

	JTextField createDoubleControl(final MapElement element, final String property) {
		return createDoubleControl(element, property, Mode.ALL);
	}

	JTextField createDoubleControl(final MapElement element, final String property, final Mode mode) {
		final JTextField field = new JTextField(8);
		field.setText("" + element.getProperty(property));
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				double newRadius = Double.parseDouble(field.getText());
				display.setProperty(element, property, newRadius, mode);
			}
		});
		return field;
	}

	JPanel createColorControl(final MapElement element, final String property) {
		return createColorControl(element, property, Mode.ALL);
	}

	JPanel createColorControl(final MapElement element, final String property, final Mode mode) {
		final JPanel colorPanel = new JPanel();
		colorPanel.setBackground((Color) element.getProperty(property));
		colorPanel.setOpaque(true);
		colorPanel.setMinimumSize(new Dimension(50, 20));
		colorPanel.setPreferredSize(new Dimension(50, 20));
		colorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		colorPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color newColor = JColorChooser.showDialog(OptionsPanel.this, "Choose colour", (Color) element.getProperty(property));
				display.setProperty(element, property, newColor, mode);
				colorPanel.setBackground(newColor);
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		return colorPanel;
	}

	JSlider createSliderControl(final MapElement element, final String property) {
		return createSliderControl(element, property, Mode.ALL);
	}

	JSlider createSliderControl(final MapElement element, final String property, final Mode mode) {
		JSlider alphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		alphaSlider.setValue((int) (100 * (Float) element.getProperty(property)));
		alphaSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				float alpha = slider.getValue() / 100f;
				if (mode != Mode.REMOTE) element.setProperty(property, alpha);
				if (!slider.getValueIsAdjusting() && mode != Mode.LOCAL) {
					display.setProperty(element, property, alpha, Mode.REMOTE);	// send to remote because we've already done local
				}
			}
		});
		return alphaSlider;
	}

	JComboBox createComboControl(final MapElement element, final String property, Object[] values) {
		return createComboControl(element, property, Mode.ALL, values);
	}

	JComboBox createComboControl(final MapElement element, final String property, final Mode mode, Object[] values) {
		final JComboBox typeCombo = new JComboBox(values);
		typeCombo.setSelectedItem(element.getProperty(property));
		typeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				Object selected = combo.getSelectedItem();
				display.setProperty(element, property, selected, mode);
			}
		});
		return typeCombo;
	}

	JComboBox createComboControl(final MapElement element, final String property, final Mode mode, ComboBoxModel values) {
		final JComboBox typeCombo = new JComboBox(values);
		typeCombo.setSelectedItem(element.getProperty(property));
		typeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				Object selected = combo.getSelectedItem();
				display.setProperty(element, property, selected, mode);
			}
		});
		return typeCombo;
	}

	JTextField createStringControl(final MapElement element, final String property) {
		return createStringControl(element, property, Mode.ALL);
	}

	JTextField createStringControl(final MapElement element, final String property, final Mode mode) {
		final JTextField textField = new JTextField(30);
		textField.setText("" + element.getProperty(property));
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				display.setProperty(element, property, textField.getText(), mode);
			}
		});
		return textField;
	}

	JLabel createLabelControl(final MapElement element, final String property) {
		final JLabel label = new JLabel();
		label.setText("" + element.getProperty(property));
		return label;
	}

	// TODO use this for visibility - need to implement visibility properties
	JCheckBox createCheckBox(final MapElement element, final String property, final Mode mode, String label) {
		JCheckBox check = new JCheckBox(label);
		check.setSelected((Boolean) element.getProperty(property));
		check.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox check = (JCheckBox) e.getSource();
				display.setProperty(element, property, check.isSelected(), mode);
			}
		});
		return check;
	}

	// TODO convert users to use createCheckBox?
	// this control does not apply the changes to the local MapElement - it's intended for visibility
	JCheckBox createVisibilityControl(final MapElement element) {
		JCheckBox cb = createCheckBox(element, MapElement.PROPERTY_VISIBLE, Mode.REMOTE, "visible?");
		cb.setSelected(false);
		return cb;
	}

	// TODO convert users to use createCheckBox?
	// this control does not apply the changes to the local MapElement - it's intended for visibility
	JCheckBox createVisibilityControl(final MapElement element, String label) {
		JCheckBox cb = createCheckBox(element, MapElement.PROPERTY_VISIBLE, Mode.REMOTE, label);
		cb.setSelected(false);
		return cb;
	}

	final static String[] options = { "0", "90", "180", "270" };

	JComboBox createRotationControl(final MapElement element, final String property, final Mode mode) {
		JComboBox rotationsCombo = new JComboBox(options);
		rotationsCombo.setSelectedIndex((Integer) element.getProperty(property));
		rotationsCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				int index = combo.getSelectedIndex();
				display.setProperty(element, property, index, mode);
			}
		});
		return rotationsCombo;
	};

	MapElementMouseListener getMouseListener() {
		return null;
	}

	abstract class DefaultDragger implements MapElementMouseListener {
		boolean dragging = false;
		int button;
		Point2D offset;
		String target;

		abstract String getDragTarget(Point2D gridLocation);

		void setTargetLocation(Point2D p) {
			if (target == null) return;
			display.setProperty(getElement(), target, p, Mode.ALL);
		}

		Point2D getTargetLocation() {
			return (Point2D) getElement().getProperty(target);
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {
			button = e.getButton();
			target = getDragTarget(gridloc);
			if (target == null) return;
			Point2D targetLoc = getTargetLocation();
			offset = new Point2D.Double(gridloc.getX() - targetLoc.getX(), gridloc.getY() - targetLoc.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {
			if (dragging) {
				Point2D p = new Point2D.Double(gridloc.getX() - offset.getX(), gridloc.getY() - offset.getY());
				setTargetLocation(p);
				dragging = false;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {
			if (button == MouseEvent.BUTTON1 && target != null) {
				dragging = true;
			}
			if (dragging) {
				Point2D p = new Point2D.Double(gridloc.getX() - offset.getX(), gridloc.getY() - offset.getY());
				setTargetLocation(p);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
		}
	};

	// ---- XML serialisation methods ----
	static final String REMOTE_PREFIX = "remote_";	// prefix for properties intended for remote element

	Element getElement(Document doc) {
		return null;
	}

	void parseDOM(Element e) {
	}

	// sets attributes for all properties of the local MapElement
	void setAllAttributes(Element e) {
		for (String p : getElement().getProperties()) {
			setAttribute(e, p, getElement().getProperty(p));
		}
	}

	void setAttribute(Element e, String name, Object prop) {
		String value;
		if (prop instanceof Enum) {
			value = ((Enum<?>) prop).name();
		} else if (prop instanceof Color) {
			Color c = (Color) prop;
			value = String.format("#%06x", c.getRGB() & 0xffffff).toUpperCase();
		} else {
			value = prop == null ? "" : prop.toString();
		}
		e.setAttribute(name, value);
	}

	void parseBooleanAttribute(String property, Element domElement, JCheckBox check) {
		String domProp = REMOTE_PREFIX + property;
		if (domElement.hasAttribute(domProp)) {
			boolean value = Boolean.parseBoolean(domElement.getAttribute(domProp));
			display.setProperty(getElement(), property, value, Mode.REMOTE);
			check.setSelected(value);
		}
	}

	void parseBooleanAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
			boolean value = Boolean.parseBoolean(domElement.getAttribute(property));
			display.setProperty(getElement(), property, value, mode);
		}
	}

	void parseIntegerAttribute(String property, Element domElement, JTextComponent field) {
		String domProp = REMOTE_PREFIX + property;
		if (domElement.hasAttribute(domProp)) {
//			try {
			int value = Integer.parseInt(domElement.getAttribute(domProp));
			display.setProperty(getElement(), property, value, Mode.REMOTE);
			field.setText(Integer.toString(value));
//			} catch (NumberFormatException e) {
//			}
		}
	}

	void parseIntegerAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			int value = Integer.parseInt(domElement.getAttribute(property));
			display.setProperty(getElement(), property, value, mode);
//			} catch (NumberFormatException e) {
//			}
		}
	}

	void parseStringAttribute(String property, Element domElement, JTextComponent field) {
		String domProp = REMOTE_PREFIX + property;
		if (domElement.hasAttribute(domProp)) {
			String value = domElement.getAttribute(domProp);
			display.setProperty(getElement(), property, value, Mode.REMOTE);
			field.setText(value);
		} else {
			field.setText("");
		}
	}

	void parseStringAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
			String value = domElement.getAttribute(property);
			display.setProperty(getElement(), property, value, mode);
		}
	}

	void parseFloatAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			float value = Float.parseFloat(domElement.getAttribute(property));
			display.setProperty(getElement(), property, value, mode);
//			} catch (NumberFormatException e) {
//			}
		}
	}

	void parseDoubleAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			double value = Double.parseDouble(domElement.getAttribute(property));
			display.setProperty(getElement(), property, value, mode);
//			} catch (NumberFormatException e) {
//			}
		}
	}

	void parseColorAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			Color value = Color.decode(domElement.getAttribute(property));
			display.setProperty(getElement(), property, value, mode);
//			} catch (NumberFormatException e) {
//			}
		}
	}

	<T extends Enum<T>> void parseEnumAttribute(String property, Class<T> enumType, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			T value = Enum.valueOf(enumType, domElement.getAttribute(property));
			display.setProperty(getElement(), property, value, mode);
//			} catch (IllegalArgumentException e) {
//			}
		}
	}

}

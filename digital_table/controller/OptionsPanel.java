package digital_table.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.server.MediaManager;

@SuppressWarnings("serial")
abstract class OptionsPanel<E extends MapElement> extends JPanel {
	E element;
	DisplayManager display;

	enum DragMode {
		NONE,	// no dragging
		MOVE,	// dragging will move the element or a subelement
		PAINT	// dragging will trigger a click event on each mouse move
	};

	OptionsPanel(DisplayManager r) {
		display = r;
	}

	E getElement() {
		return element;
	}

	// ------ control creation factory methods ------
	//Grid has createNullableIntegerControl
	//Browser has createReadOnlyControl

	JTextField createIntegerControl(final String property) {
		final JTextField field = new JTextField(8);
		field.setText("" + element.getProperty(property));
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int newValue = Integer.parseInt(field.getText());
				display.setProperty(element, property, newValue, Mode.ALL);
			}
		});
		return field;
	}

	JTextField createDoubleControl(final String property) {
		final JTextField field = new JTextField(8);
		field.setText("" + element.getProperty(property));
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				double newRadius = Double.parseDouble(field.getText());
				display.setProperty(element, property, newRadius, Mode.ALL);
			}
		});
		return field;
	}

	JPanel createColorControl(final String property) {
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
				display.setProperty(element, property, newColor, Mode.ALL);
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

	JSlider createSliderControl(final String property) {
		return createSliderControl(property, Mode.ALL);
	}

//	JSlider createAlphaSlider(final JCheckBox visibilityCheck) {
//		JSlider alphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
//		alphaSlider.setValue((int) (100 * (Float) element.getProperty(Token.PROPERTY_ALPHA)));
//		alphaSlider.addChangeListener(new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent e) {
//				JSlider slider = (JSlider) e.getSource();
//				float alpha = slider.getValue() / 100f;
//				element.setProperty(Token.PROPERTY_ALPHA, alpha * (visibilityCheck.isSelected() ? 1.0f : 0.5f));
//				if (!slider.getValueIsAdjusting()) {
//					display.setProperty(element, Token.PROPERTY_ALPHA, alpha, Mode.REMOTE);	// send to remote because we've already done local
//				}
//			}
//		});
//		return alphaSlider;
//	}

	JSlider createSliderControl(final String property, final Mode mode) {
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

	JComboBox createComboControl(final String property, Object[] values) {
		final JComboBox typeCombo = new JComboBox(values);
		typeCombo.setSelectedItem(element.getProperty(property));
		typeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				Object selected = combo.getSelectedItem();
				display.setProperty(element, property, selected, Mode.ALL);
			}
		});
		return typeCombo;
	}

	JTextField createStringControl(final String property) {
		return createStringControl(property, Mode.ALL);
	}

	JTextField createStringControl(final String property, final Mode mode) {
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

	JCheckBox createCheckBox(final String property, final Mode mode, String label) {
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

	JCheckBox createVisibilityControl() {
		JCheckBox check = new JCheckBox("visible?");
		check.setSelected(false);
		check.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox check = (JCheckBox) e.getSource();
				display.setProperty(element, MapElement.PROPERTY_VISIBLE, check.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN, Mode.REMOTE);
				display.setProperty(element, MapElement.PROPERTY_VISIBLE, check.isSelected() ? Visibility.VISIBLE : Visibility.FADED, Mode.LOCAL);
			}
		});
		return check;
	}

	final static String[] options = { "0", "90", "180", "270" };

	JComboBox createRotationControl(final String property, final Mode mode) {
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

	class ImageMediaOptionsPanel extends JPanel {
		private JLabel imageLabel;
		JCheckBox imageVisibleCheck;
		URI uri = null;

		ImageMediaOptionsPanel() {
			imageVisibleCheck = createCheckBox(LineTemplate.PROPERTY_IMAGE_VISIBLE, Mode.ALL, "Image visible?");
			imageLabel = new JLabel();

			JButton imageButton = new JButton("Set Image");
			imageButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					URI u = MediaManager.INSTANCE.showFileChooser(ImageMediaOptionsPanel.this);
					if (u != null) setURI(u);
				}
			});

			JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setURI(null);
					display.setProperty(element, LineTemplate.PROPERTY_IMAGE_VISIBLE, false);
				}
			});

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

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			add(imageVisibleCheck, c);
			c.gridx = 1;
			add(imageLabel, c);

			JPanel imagePanel = new JPanel();
			imagePanel.add(imageButton);
			imagePanel.add(clearButton);
			imagePanel.add(playButton);
			imagePanel.add(stopButton);
			c.gridy = 1;
			add(imagePanel, c);
		}

		void setURI(URI uri) {
			this.uri = uri;
			if (uri == null) {
				imageLabel.setText("");
			} else {
				imageLabel.setText(uri.toASCIIString());
			}
			display.setMedia(element, LineTemplate.PROPERTY_IMAGE, uri);
		}
	}

	// ----- dragging support -----

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
			display.setProperty(element, target, p, Mode.ALL);
		}

		Point2D getTargetLocation() {
			return (Point2D) element.getProperty(target);
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

	abstract Element getElement(Document doc);

	abstract void parseDOM(Element e, OptionsPanel<?> parent);

	// sets attributes for all properties of the local MapElement
	void setAllAttributes(Element e) {
		for (String p : element.getProperties()) {
			setAttribute(e, p, element.getProperty(p));
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
			display.setProperty(element, property, value, Mode.REMOTE);
			check.setSelected(value);
		}
	}

	void parseBooleanAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
			boolean value = Boolean.parseBoolean(domElement.getAttribute(property));
			display.setProperty(element, property, value, mode);
		}
	}

	void parseIntegerAttribute(String property, Element domElement, JTextComponent field) {
		String domProp = REMOTE_PREFIX + property;
		if (domElement.hasAttribute(domProp)) {
//			try {
			int value = Integer.parseInt(domElement.getAttribute(domProp));
			display.setProperty(element, property, value, Mode.REMOTE);
			field.setText(Integer.toString(value));
//			} catch (NumberFormatException e) {
//			}
		}
	}

	void parseIntegerAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			int value = Integer.parseInt(domElement.getAttribute(property));
			display.setProperty(element, property, value, mode);
//			} catch (NumberFormatException e) {
//			}
		}
	}

	void parseStringAttribute(String property, Element domElement, JTextComponent field) {
		String domProp = REMOTE_PREFIX + property;
		if (domElement.hasAttribute(domProp)) {
			String value = domElement.getAttribute(domProp);
			display.setProperty(element, property, value, Mode.REMOTE);
			field.setText(value);
		} else {
			field.setText("");
		}
	}

	void parseStringAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
			String value = domElement.getAttribute(property);
			display.setProperty(element, property, value, mode);
		}
	}

	void parseFloatAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			float value = Float.parseFloat(domElement.getAttribute(property));
			display.setProperty(element, property, value, mode);
//			} catch (NumberFormatException e) {
//			}
		}
	}

	void parseDoubleAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			double value = Double.parseDouble(domElement.getAttribute(property));
			display.setProperty(element, property, value, mode);
//			} catch (NumberFormatException e) {
//			}
		}
	}

	void parseColorAttribute(String property, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			Color value = Color.decode(domElement.getAttribute(property));
			display.setProperty(element, property, value, mode);
//			} catch (NumberFormatException e) {
//			}
		}
	}

	<T extends Enum<T>> void parseEnumAttribute(String property, Class<T> enumType, Element domElement, Mode mode) {
		if (domElement.hasAttribute(property)) {
//			try {
			T value = Enum.valueOf(enumType, domElement.getAttribute(property));
			display.setProperty(element, property, value, mode);
//			} catch (IllegalArgumentException e) {
//			}
		}
	}

	void parseVisibility(Element domElement, JCheckBox visibleCheck) {
		String domProp = REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE;
		if (domElement.hasAttribute(domProp)) {
			Visibility v = Visibility.valueOf(domElement.getAttribute(domProp));
			display.setProperty(element, MapElement.PROPERTY_VISIBLE, v, Mode.REMOTE);
			visibleCheck.setSelected(v != Visibility.HIDDEN);
		}
	}

}

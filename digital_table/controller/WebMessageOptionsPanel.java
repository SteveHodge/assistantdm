package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import digital_table.elements.WebMessage;
import digital_table.elements.WebMessage.ColorMode;
import gamesystem.core.PropertyEvent;
import gamesystem.core.PropertyListener;
import party.Character;
import party.Party;
import party.PartyListener;
import util.ModuleListener;
import util.ModuleRegistry;
import webmonitor.WebsiteMessageListener;
import webmonitor.WebsiteMonitorModule;

@SuppressWarnings("serial")
class WebMessageOptionsPanel extends OptionsPanel<WebMessage> {
	private JComboBox<Layer> layerCombo;
	private JTextField xField;
	private JTextField yField;
	private JTextField widthField;
	private JTextField heightField;
	private JSlider alphaSlider;
	private JComboBox<String> rotationsCombo;
	private JPanel colorPanel;
	private JPanel bgColorPanel;
	private JCheckBox visibleCheck;
	private JTextField fontSizeField;
	private JComboBox<ColorMode> modeCombo;

	WebMessageOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new WebMessage();
		if (parent == null) {
			element.setProperty(WebMessage.PROPERTY_X, (double) r.getXOffset());
			element.setProperty(WebMessage.PROPERTY_Y, (double) r.getYOffset());
		}
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		layerCombo = createComboControl(MapElement.PROPERTY_LAYER, Layer.values());

		ModuleRegistry.addModuleListener(WebsiteMonitorModule.class, new ModuleListener<WebsiteMonitorModule>() {
			WebsiteMonitorModule web = null;

			WebsiteMessageListener webListener = new WebsiteMessageListener() {
				@Override
				public void addMessage(Character c, String message) {
					display.setProperty(element, WebMessage.PROPERTY_MESSAGE, message);
				}
			};

			@Override
			public void moduleRegistered(WebsiteMonitorModule module) {
				web = module;
				web.addMessageListener(webListener);
			}

			@Override
			public void moduleRemoved(WebsiteMonitorModule module) {
				web.removeMessageListener(webListener);
				web = null;
			}
		});

		ModuleRegistry.addModuleListener(Party.class, new ModuleListener<Party>() {
			Map<Character, PropertyListener> listeners = new HashMap<>();
			PartyListener partyListener = new PartyListener() {
				@Override
				public void characterAdded(Character c) {
					setupCharacter(c);
				}

				@Override
				public void characterRemoved(Character c) {
					PropertyListener l = listeners.remove(c);
					if (l != null)
						c.removePropertyListener(Character.PROPERTY_COLOR, l);
				}
			};

			@Override
			public void moduleRegistered(Party party) {
				for (Character c : party) {
					setupCharacter(c);
				}

				party.addPartyListener(partyListener);
			}

			@Override
			public void moduleRemoved(Party party) {
				Set<Character> toRemove = new HashSet<>(listeners.keySet());
				for (Character c : toRemove) {
					PropertyListener l = listeners.remove(c);
					if (l != null)
						c.removePropertyListener(Character.PROPERTY_COLOR, l);
					party.removePartyListener(partyListener);
				}
			}

			void setupCharacter(Character c) {
				PropertyListener l = new PropertyListener() {
					@Override
					public void propertyChanged(PropertyEvent event) {
						display.setProperty(element, WebMessage.PROPERTY_PREFIX_PREFIX + c.getName(), c.getColor());
					}
				};
				c.addPropertyListener("extra." + Character.PROPERTY_COLOR, l);
				listeners.put(c, l);
				display.setProperty(element, WebMessage.PROPERTY_PREFIX_PREFIX + c.getName(), c.getColor());
			}
		});

		element.addPropertyChangeListener(listener);

		xField = createDoubleControl(WebMessage.PROPERTY_X);
		yField = createDoubleControl(WebMessage.PROPERTY_Y);
		widthField = createDoubleControl(WebMessage.PROPERTY_WIDTH);
		heightField = createDoubleControl(WebMessage.PROPERTY_HEIGHT);
		alphaSlider = createSliderControl(WebMessage.PROPERTY_ALPHA);
		colorPanel = createColorControl(WebMessage.PROPERTY_COLOR);
		bgColorPanel = createColorControl(WebMessage.PROPERTY_BACKGROUND_COLOR);
		rotationsCombo = createRotationControl(WebMessage.PROPERTY_ROTATIONS, Mode.ALL);
		visibleCheck = createVisibilityControl();
		fontSizeField = createDoubleControl(WebMessage.PROPERTY_FONT_SIZE);
		modeCombo = createComboControl(WebMessage.PROPERTY_COLOR_MODE, WebMessage.ColorMode.values());

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 0.0d;
		c.weighty = 0.0d;
		c.fill = GridBagConstraints.NONE;
		add(new JLabel("Layer:"), c);
		add(new JLabel("Left edge column:"), c);
		add(new JLabel("Top edge Row:"), c);
		add(new JLabel("Width:"), c);
		add(new JLabel("Height:"), c);
		add(new JLabel("Font size (in grid cells):"), c);
		add(new JLabel("Rotation:"), c);
		add(new JLabel("Colour Mode:"), c);
		add(new JLabel("Colour:"), c);
		add(new JLabel("Background:"), c);
		add(new JLabel("Transparency:"), c);

		c.gridx = 2;
		c.gridy = 0;
		add(visibleCheck, c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;
		add(layerCombo, c);
		c.gridwidth = 2;
		add(xField, c);
		add(yField, c);
		add(widthField, c);
		add(heightField, c);
		add(fontSizeField, c);
		add(rotationsCombo, c);
		add(modeCombo, c);
		add(colorPanel, c);
		add(bgColorPanel, c);
		add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0;
		c.gridwidth = 3;
		add(new JPanel(), c);
		//@formatter:on
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_LAYER)) {
				layerCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int) (100 * (Float) e.getNewValue()));

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_WIDTH)) {
				widthField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_HEIGHT)) {
				heightField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer) e.getNewValue());

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_BACKGROUND_COLOR)) {
				bgColorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_FONT_SIZE)) {
				fontSizeField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(WebMessage.PROPERTY_COLOR_MODE)) {
				modeCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_DRAGGING)) {
				// ignore

			} else {
				System.out.println("Unknown property: " + e.getPropertyName());
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
			display.setProperty(element, WebMessage.PROPERTY_X, p.getX());
			display.setProperty(element, WebMessage.PROPERTY_Y, p.getY());
		}

		@Override
		Point2D getTargetLocation() {
			return new Point2D.Double((Double) element.getProperty(WebMessage.PROPERTY_X),
					(Double) element.getProperty(WebMessage.PROPERTY_Y));
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "WebMessage";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseColorAttribute(WebMessage.PROPERTY_COLOR, e, Mode.ALL);
		parseColorAttribute(WebMessage.PROPERTY_BACKGROUND_COLOR, e, Mode.ALL);
		parseFloatAttribute(WebMessage.PROPERTY_ALPHA, e, Mode.ALL);
		parseDoubleAttribute(WebMessage.PROPERTY_X, e, Mode.ALL);
		parseDoubleAttribute(WebMessage.PROPERTY_Y, e, Mode.ALL);
		parseDoubleAttribute(WebMessage.PROPERTY_WIDTH, e, Mode.ALL);
		parseDoubleAttribute(WebMessage.PROPERTY_HEIGHT, e, Mode.ALL);
		parseIntegerAttribute(WebMessage.PROPERTY_ROTATIONS, e, Mode.ALL);
		parseDoubleAttribute(WebMessage.PROPERTY_FONT_SIZE, e, Mode.ALL);
		parseEnumAttribute(WebMessage.PROPERTY_COLOR_MODE, WebMessage.ColorMode.class, e, Mode.ALL);
		parseEnumAttribute(MapElement.PROPERTY_LAYER, Layer.class, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
	}
}


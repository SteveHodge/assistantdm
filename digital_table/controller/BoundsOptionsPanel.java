package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.elements.MapElement;
import digital_table.elements.ScreenBounds;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class BoundsOptionsPanel extends OptionsPanel {
	ScreenBounds bounds;
	JPanel colorPanel;
	JSlider alphaSlider;
	JCheckBox visibleCheck;

	public BoundsOptionsPanel(MapElement parent, TableDisplay r) {
		super(r);
		bounds = new ScreenBounds();
		sendElement(bounds, parent);	// XXX should we send to remote?
		bounds.setProperty(MapElement.PROPERTY_VISIBLE, true);
		bounds.addPropertyChangeListener(listener);

		colorPanel = createColorControl(bounds, ScreenBounds.PROPERTY_COLOR);
		alphaSlider = createSliderControl(bounds, ScreenBounds.PROPERTY_ALPHA);

		visibleCheck = this.createCheckBox(bounds, MapElement.PROPERTY_VISIBLE, Mode.LOCAL, "local visible?");
		visibleCheck.setSelected(true);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 1; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	@Override
	public ScreenBounds getElement() {
		return bounds;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ScreenBounds.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(ScreenBounds.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(ScreenBounds.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected((Boolean) e.getNewValue());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	// ---- XML serialisation methods ----
	public final static String XML_TAG = "ScreenBounds";

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		return e;
	}

	@Override
	public void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseColorAttribute(ScreenBounds.PROPERTY_COLOR, e, Mode.LOCAL);
		parseFloatAttribute(ScreenBounds.PROPERTY_ALPHA, e, Mode.LOCAL);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, Mode.LOCAL);
	}
}

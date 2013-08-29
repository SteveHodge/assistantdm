package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Callibrate;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;

@SuppressWarnings("serial")
class CallibrateOptionsPanel extends OptionsPanel<Callibrate> {
	private JCheckBox visibleCheck;
	private JCheckBox showBGCheck;

	CallibrateOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new Callibrate();
		display.addElement(element, parent);
		element.addPropertyChangeListener(listener);

		showBGCheck = createCheckBox(Callibrate.PROPERTY_SHOW_BACKGROUND, Mode.ALL, "show background?");
		visibleCheck = new JCheckBox("visible?");
		visibleCheck.setSelected(false);
		visibleCheck.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox check = (JCheckBox) e.getSource();
				display.setProperty(element, MapElement.PROPERTY_VISIBLE, check.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN, Mode.ALL);
			}
		});

		// @formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(showBGCheck, c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 1;

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);
		// @formatter:on
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(Callibrate.PROPERTY_SHOW_BACKGROUND)) {
				showBGCheck.setSelected((Boolean) e.getNewValue());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Callibrate";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseBooleanAttribute(Callibrate.PROPERTY_SHOW_BACKGROUND, e, Mode.ALL);

		if (e.hasAttribute(MapElement.PROPERTY_VISIBLE)) {
			Visibility v = Visibility.valueOf(e.getAttribute(MapElement.PROPERTY_VISIBLE));
			display.setProperty(element, MapElement.PROPERTY_VISIBLE, v, Mode.ALL);
			visibleCheck.setSelected(v != Visibility.HIDDEN);
		}
	}
}

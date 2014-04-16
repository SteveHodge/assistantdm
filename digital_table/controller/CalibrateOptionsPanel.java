package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Calibrate;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;

@SuppressWarnings("serial")
class CalibrateOptionsPanel extends OptionsPanel<Calibrate> {
	private JCheckBox visibleCheck;
	private JCheckBox showBGCheck;

	CalibrateOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new Calibrate();
		display.addElement(element, parent);
		element.addPropertyChangeListener(listener);

		showBGCheck = createCheckBox(Calibrate.PROPERTY_SHOW_BACKGROUND, Mode.ALL, "show background?");

		visibleCheck = new JCheckBox("visible?");
		visibleCheck.setSelected(false);
		visibleCheck.addItemListener(e -> {
			JCheckBox check = (JCheckBox) e.getSource();
			display.setProperty(element, MapElement.PROPERTY_VISIBLE, check.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN, Mode.ALL);
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		add(visibleCheck, c);
		add(showBGCheck, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		add(new JPanel(), c);
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(Calibrate.PROPERTY_SHOW_BACKGROUND)) {
				showBGCheck.setSelected((Boolean) e.getNewValue());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Calibrate";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseBooleanAttribute(Calibrate.PROPERTY_SHOW_BACKGROUND, e, Mode.ALL);

		if (e.hasAttribute(MapElement.PROPERTY_VISIBLE)) {
			Visibility v = Visibility.valueOf(e.getAttribute(MapElement.PROPERTY_VISIBLE));
			display.setProperty(element, MapElement.PROPERTY_VISIBLE, v, Mode.ALL);
			visibleCheck.setSelected(v != Visibility.HIDDEN);
		}
	}
}

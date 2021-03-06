package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import camera.CameraModule;
import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.MapImage;

// TODO save latest image when saving?
// TODO can implement autoupdate now

@SuppressWarnings("serial")
class CameraOptionsPanel extends OptionsPanel<MapImage> {
	private JSlider alphaSlider;
	private CameraModule camera;
	private JCheckBox visibleCheck;

	CameraOptionsPanel(MapElement parent, DisplayManager r, CameraModule cam) {
		super(r);
		element = new MapImage("Camera");
		element.setProperty(MapImage.PROPERTY_ROTATIONS, 1);
		element.setProperty(MapImage.PROPERTY_WIDTH, 32.0d);
		element.setProperty(MapImage.PROPERTY_HEIGHT, 39.0d);
		element.setProperty(Group.PROPERTY_Y, -1.0d);
		display.addElement(element, parent);
		element.addPropertyChangeListener(listener);
		this.camera = cam;

		alphaSlider = createSliderControl(MapImage.PROPERTY_ALPHA);
		visibleCheck = new JCheckBox("visible?");
		visibleCheck.setSelected(false);
		visibleCheck.addItemListener(e -> {
			JCheckBox check = (JCheckBox) e.getSource();
			display.setProperty(element, MapElement.PROPERTY_VISIBLE, check.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN, Mode.ALL);
		});

		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(arg0 -> {
			byte[] bytes = camera.getLatestCorrectedJPEG();
			display.setProperty(element, MapImage.PROPERTY_IMAGE, bytes);
			display.setProperty(element, MapImage.PROPERTY_WIDTH, 32.0d);
			display.setProperty(element, MapImage.PROPERTY_HEIGHT, 39.0d);
		});

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		add(visibleCheck, c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0;
		c.gridy++; add(alphaSlider, c);
		c.gridy++; add(updateButton, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		add(new JPanel(), c);
		//@formatter:on
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int) (100 * (Float) e.getNewValue()));
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "CameraImage";

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

		parseFloatAttribute(MapImage.PROPERTY_ALPHA, e, Mode.ALL);
		parseVisibility(e, visibleCheck);
	}
}

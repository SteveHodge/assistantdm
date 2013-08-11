package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import camera.CameraPanel;
import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.MapElement;
import digital_table.elements.MapImage;

// TODO save latest image when saving?

class CameraOptionsPanel extends OptionsPanel {
	private static final long serialVersionUID = 1L;

	private MapImage image;
	private JSlider alphaSlider;
	private CameraPanel camera;
	private JCheckBox visibleCheck;

	CameraOptionsPanel(MapElement parent, DisplayManager r, CameraPanel cam) {
		super(r);
		image = new MapImage("Camera");
		image.setProperty(MapImage.PROPERTY_ROTATIONS, 1);
		image.setProperty(MapImage.PROPERTY_WIDTH, 32.0d);
		image.setProperty(MapImage.PROPERTY_HEIGHT, 39.0d);
		image.setProperty(MapImage.PROPERTY_Y, -1.0d);
		display.addElement(image, parent);
		image.addPropertyChangeListener(listener);
		this.camera = cam;

		alphaSlider = createSliderControl(image, MapImage.PROPERTY_ALPHA);
		visibleCheck = createCheckBox(image, MapElement.PROPERTY_VISIBLE, Mode.ALL, "visible?");

		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				byte[] bytes = camera.getLatestCorrectedImage();
				display.setProperty(image, MapImage.PROPERTY_IMAGE, bytes);
				display.setProperty(image, MapImage.PROPERTY_WIDTH, 32.0d);
				display.setProperty(image, MapImage.PROPERTY_HEIGHT, 39.0d);
			}
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

	@Override
	MapImage getElement() {
		return image;
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapImage.PROPERTY_ALPHA)) {
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
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected());
		return e;
	}

	@Override
	void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseFloatAttribute(MapImage.PROPERTY_ALPHA, e, Mode.ALL);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);
	}
}

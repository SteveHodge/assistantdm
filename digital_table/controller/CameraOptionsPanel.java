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
import digital_table.elements.MapElement;
import digital_table.elements.MapImage;
import digital_table.server.TableDisplay;

// TODO save latest image when saving?

public class CameraOptionsPanel extends OptionsPanel {
	private static final long serialVersionUID = 1L;

	private MapImage image;
	private JSlider alphaSlider;
	private CameraPanel camera;
	private JCheckBox visibleCheck;

	public CameraOptionsPanel(MapElement parent, TableDisplay r, CameraPanel cam) {
		super(r);
		image = new MapImage("Camera");
		image.setProperty(MapImage.PROPERTY_ROTATIONS, 1);
		image.setProperty(MapImage.PROPERTY_WIDTH, 32.0d);
		image.setProperty(MapImage.PROPERTY_HEIGHT, 39.0d);
		image.setProperty(MapImage.PROPERTY_Y, -1.0d);
		sendElement(image, parent);
		image.addPropertyChangeListener(listener);
		this.camera = cam;

		alphaSlider = createSliderControl(image, MapImage.PROPERTY_ALPHA);
		visibleCheck = createCheckBox(image, MapElement.PROPERTY_VISIBLE, Mode.BOTH, "visible?");

		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				byte[] bytes = camera.getLatestCorrectedImage();
				setRemote(image.getID(), MapImage.PROPERTY_IMAGE, bytes);
				setRemote(image.getID(), MapImage.PROPERTY_WIDTH, 32.0d);
				setRemote(image.getID(), MapImage.PROPERTY_HEIGHT, 39.0d);
				image.setProperty(MapImage.PROPERTY_IMAGE, bytes);
				image.setProperty(MapImage.PROPERTY_WIDTH, 32.0d);
				image.setProperty(MapImage.PROPERTY_HEIGHT, 39.0d);
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
	public MapImage getElement() {
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
	public final static String XML_TAG = "CameraImage";

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected());
		return e;
	}

	@Override
	public void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseFloatAttribute(MapImage.PROPERTY_ALPHA, e, Mode.BOTH);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);
	}
}

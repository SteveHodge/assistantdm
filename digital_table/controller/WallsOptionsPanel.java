package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.Token;
import digital_table.elements.Walls;
import digital_table.elements.Walls.WallLayout;
import digital_table.server.MediaManager;

@SuppressWarnings("serial")
class WallsOptionsPanel extends OptionsPanel<Walls> {
	protected URI uri = null;

	private JCheckBox visibleCheck;
	private JTextField labelField;
	private JCheckBox showWallsCheck;
	public static File lastDir = new File(".");	// last selected image - used to keep the current directory

	WallsOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new Walls();
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		display.addElement(element, parent);
		element.addPropertyChangeListener(listener);

		visibleCheck = createVisibilityControl();
		visibleCheck.setSelected(false);

		JButton loadWallsButton = new JButton("Load Wall Layout");
		loadWallsButton.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (lastDir != null) chooser.setCurrentDirectory(lastDir);
				if (chooser.showOpenDialog(WallsOptionsPanel.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					loadLayout(f.toURI());
					lastDir = f;
				} else {
					System.out.println("Cancelled");
				}
			}
		});
		labelField = createStringControl(Walls.PROPERTY_LABEL, Mode.LOCAL);
		showWallsCheck = createCheckBox(Walls.PROPERTY_SHOW_WALLS, Mode.LOCAL, "DM sees walls?");
		showWallsCheck.setSelected(true);

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		add(visibleCheck, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;
		add(labelField, c);
		add(loadWallsButton, c);
		add(showWallsCheck, c);

		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		add(new JPanel(), c);

		//@formatter:on
	}

	private void loadLayout(URI uri) {
		try {
			File file = MediaManager.INSTANCE.getFile(uri);
			System.out.println("Opening wall layout " + file.getAbsolutePath());

//			if (width.getValue() == 0 || height.getValue() == 0) {
//				System.out.println("Wall layout has no width or height");
//			}
//			System.out.println("Reading wall layout");
//			System.out.println("  Width = " + width.getValue());
//			System.out.println("  Height = " + height.getValue());
//			System.out.println("  X = " + x.getValue());
//			System.out.println("  Y = " + y.getValue());
//			System.out.println("  Rotations = " + rotations.getValue());
//			System.out.println("  Mirrored = " + mirrored.getValue());

			byte[] encoded = Files.readAllBytes(file.toPath());
			String contents = new String(encoded, StandardCharsets.UTF_8);
			WallLayout layout = WallLayout.parseXML(contents);
			display.setProperty(element, Walls.PROPERTY_WALL_LAYOUT, layout);
			this.uri = uri;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(Walls.PROPERTY_SHOW_WALLS)) {
				showWallsCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(Walls.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else {
				System.out.println("Unknown property changed: " + e.getPropertyName());
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Walls";
	final static String FILE_ATTRIBUTE_NAME = "uri";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);

		if (uri != null) e.setAttribute(FILE_ATTRIBUTE_NAME, uri.toASCIIString());
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(Token.PROPERTY_LABEL, e, Mode.ALL);
		parseBooleanAttribute(Walls.PROPERTY_SHOW_WALLS, e, Mode.LOCAL);

		if (e.hasAttribute(FILE_ATTRIBUTE_NAME)) {
			try {
				loadLayout(new URI(e.getAttribute(FILE_ATTRIBUTE_NAME)));
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		parseVisibility(e, visibleCheck);
	}
}


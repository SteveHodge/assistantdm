package digital_table.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Browser;
import digital_table.elements.BrowserLocal;
import digital_table.elements.BrowserRemote;
import digital_table.elements.MapElement;

@SuppressWarnings("serial")
class BrowserOptionsPanel extends OptionsPanel<Browser> {
	private JTextField urlField;
	private JLabel titleLabel;
	private JLabel rolloverLabel;
	private JComboBox rotationsCombo;
	private JComboBox screenCombo;
	private JCheckBox remoteVisibleCheck;
	private JCheckBox localVisibleCheck;

	private JFrame frame = null;
	private JPanel panel;
	private JTextField frameURLField;
	private JLabel statusLabel;
	private JProgressBar progressBar;

	BrowserOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		BrowserRemote remote = new BrowserRemote();
		element = new BrowserLocal(remote);
		element.addPropertyChangeListener(listener);
		display.addElement(remote, parent, element);

		urlField = createStringControl(Browser.PROPERTY_URL);
		titleLabel = createReadOnlyControl(Browser.PROPERTY_TITLE);
		rolloverLabel = createReadOnlyControl(Browser.PROPERTY_ROLLOVER);
		rotationsCombo = createRotationControl(Browser.PROPERTY_ROTATIONS, Mode.REMOTE);

		String[] screens = new String[DisplayConfig.screens.size()];
		for (int i = 0; i < screens.length; i++) {
			screens[i] = "" + i;
		}
		screenCombo = new JComboBox(screens);
		screenCombo.setSelectedIndex((Integer)element.getProperty(Browser.PROPERTY_SCREEN));
		screenCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox)e.getSource();
				int index = combo.getSelectedIndex();
				display.setProperty(element, Browser.PROPERTY_SCREEN, index, Mode.ALL);
			}
		});

		remoteVisibleCheck = this.createCheckBox(MapElement.PROPERTY_VISIBLE, Mode.ALL, "remote visible?");

		localVisibleCheck = new JCheckBox("local visible?");
		localVisibleCheck.setSelected(false);
		localVisibleCheck.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (localVisibleCheck.isSelected()) {
					if (frame == null) {
						frame = createFrame();
					}
					frame.setVisible(true);
				} else if (frame != null) {
					frame.setVisible(false);
				}
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(localVisibleCheck, c);
		c.gridy++; add(new JLabel("URL:"), c);
		c.gridy++; add(new JLabel("Title:"), c);
		c.gridy++; add(new JLabel("Rollover:"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Screen:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(remoteVisibleCheck, c);
		c.gridy++; add(urlField, c);
		c.gridy++; add(titleLabel, c);
		c.gridy++; add(rolloverLabel, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(screenCombo, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	private JLabel createReadOnlyControl(final String property) {
		final JLabel label = new JLabel();
		label.setText("" + element.getProperty(property));
		return label;
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame();
		panel = new JPanel(new BorderLayout());
		statusLabel = new JLabel();

		JButton goButton = new JButton("Go");
		frameURLField = new JTextField(element.getProperty(Browser.PROPERTY_URL).toString());

		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				display.setProperty(element, Browser.PROPERTY_URL, frameURLField.getText(), Mode.ALL);
			}
		};
		goButton.addActionListener(listener);
		frameURLField.addActionListener(listener);

		progressBar = new JProgressBar();

		frame.setPreferredSize(new Dimension(1024, 800));
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				localVisibleCheck.setSelected(false);
			}

			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowOpened(WindowEvent e) {}
		});

		progressBar.setPreferredSize(new Dimension(150, 18));
		progressBar.setStringPainted(true);

		JPanel topBar = new JPanel(new BorderLayout(5, 0));
		topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
		topBar.add(frameURLField, BorderLayout.CENTER);
		topBar.add(goButton, BorderLayout.EAST);

		JPanel statusBar = new JPanel(new BorderLayout(5, 0));
		statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
		statusBar.add(statusLabel, BorderLayout.CENTER);
		statusBar.add(progressBar, BorderLayout.EAST);

		panel.add(topBar, BorderLayout.NORTH);
		panel.add(element.getComponent(), BorderLayout.CENTER);
		panel.add(statusBar, BorderLayout.SOUTH);

		frame.setTitle((String)element.getProperty(Browser.PROPERTY_TITLE));
		statusLabel.setText((String)element.getProperty(Browser.PROPERTY_ROLLOVER));

		frame.getContentPane().add(panel);
		frame.pack();
		return frame;
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Browser.PROPERTY_URL)) {
				urlField.setText(e.getNewValue().toString());
				display.setProperty(element, Browser.PROPERTY_URL, e.getNewValue(), Mode.REMOTE);
				if (frame != null) frameURLField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Browser.PROPERTY_TITLE)) {
				String title = e.getNewValue() == null ? "" : e.getNewValue().toString();
				titleLabel.setText(title);
				if (frame != null) frame.setTitle(title);

			} else if (e.getPropertyName().equals(Browser.PROPERTY_ROLLOVER)) {
				String newRollover = e.getNewValue() == null ? "" : e.getNewValue().toString();
				rolloverLabel.setText(newRollover.substring(0,Math.min(newRollover.length(), 30)));
				if (frame != null) statusLabel.setText(newRollover);

			} else if (e.getPropertyName().equals(Browser.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer)e.getNewValue());

			} else if (e.getPropertyName().equals(Browser.PROPERTY_VISIBLE)) {
				localVisibleCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(Browser.PROPERTY_LABEL)) {

			} else if (e.getPropertyName().equals(Browser.PROPERTY_SCREEN)) {

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Browser";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, remoteVisibleCheck.isSelected());
		setAttribute(e, REMOTE_PREFIX + Browser.PROPERTY_ROTATIONS, rotationsCombo.getSelectedIndex());
		return e;
	}

	@Override
	void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(Browser.PROPERTY_URL, e, Mode.ALL);
		parseIntegerAttribute(Browser.PROPERTY_SCREEN, e, Mode.ALL);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, remoteVisibleCheck);
		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, Mode.LOCAL);

		parseIntegerAttribute(Browser.PROPERTY_ROTATIONS, e, Mode.LOCAL);
		if (e.hasAttribute(REMOTE_PREFIX + Browser.PROPERTY_ROTATIONS)) {
//			try {
			int value = Integer.parseInt(e.getAttribute(REMOTE_PREFIX + Browser.PROPERTY_ROTATIONS));
			display.setProperty(getElement(), Browser.PROPERTY_ROTATIONS, value, Mode.REMOTE);
			rotationsCombo.setSelectedIndex(value);
//			} catch (NumberFormatException e) {
//			}
		}
	}
}

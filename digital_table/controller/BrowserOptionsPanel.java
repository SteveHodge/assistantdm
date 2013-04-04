package digital_table.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import digital_table.server.TableDisplay;
import digital_table.elements.Browser;

@SuppressWarnings("serial")
public class BrowserOptionsPanel extends OptionsPanel {
	Browser browser;
	JTextField urlField;
	JLabel titleLabel;
	JLabel rolloverLabel;
	JComboBox rotationsCombo;
	JComboBox screenCombo;
	JCheckBox remoteVisibleCheck;
	JCheckBox localVisibleCheck;

    JFrame frame = null;
    JPanel panel;
    JLabel lblStatus;
    JProgressBar progressBar;

	protected BrowserOptionsPanel(Browser b, TableDisplay r) {
		super(r);
		browser = b;
		browser.addPropertyChangeListener(listener);
	
		urlField = createStringControl(browser, Browser.PROPERTY_URL);
		titleLabel = createLabelControl(browser, Browser.PROPERTY_TITLE);
		rolloverLabel = createLabelControl(browser, Browser.PROPERTY_ROLLOVER);
		
		String[] options = {"0","90","180","270"};
		rotationsCombo = new JComboBox(options);
		rotationsCombo.setSelectedIndex(browser.getRotations());
		rotationsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JComboBox combo = (JComboBox)e.getSource();
					int index = combo.getSelectedIndex();
					//browser.setRotations(index);
					remote.setElementProperty(browser.getID(), Browser.PROPERTY_ROTATIONS, index);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});

		String[] screens = {"0","1","2","3","4","5"};
		screenCombo = new JComboBox(screens);
		screenCombo.setSelectedIndex(browser.getScreen());
		screenCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JComboBox combo = (JComboBox)e.getSource();
					int index = combo.getSelectedIndex();
					//browser.setScreen(index);
					remote.setElementProperty(browser.getID(), Browser.PROPERTY_SCREEN, index);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});

		remoteVisibleCheck = createVisibilityControl(browser, "remote visible?");
		localVisibleCheck = new JCheckBox("local visible?");
		localVisibleCheck.setSelected(false);
		localVisibleCheck.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (localVisibleCheck.isSelected()) {
					if (frame == null) {
						frame = createFrame();
					}
			        frame.setVisible(true);
				} else if (frame != null) {
					frame.setVisible(false);
					// TODO dispose?
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
	
	protected JFrame createFrame() {
        JFrame frame = new JFrame();
        panel = new JPanel(new BorderLayout());
        lblStatus = new JLabel();

        progressBar = new JProgressBar();

        frame.setPreferredSize(new Dimension(1024, 800));
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        panel.add(browser.getComponent(), BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        frame.setTitle(browser.getTitle());
        lblStatus.setText(browser.getRollover());

        frame.getContentPane().add(panel);
        frame.pack();
        return frame;
    }

 	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Browser.PROPERTY_URL)) {
				urlField.setText(e.getNewValue().toString());
				try {
					remote.setElementProperty(browser.getID(), Browser.PROPERTY_URL, e.getNewValue());
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}

			} else if (e.getPropertyName().equals(Browser.PROPERTY_TITLE)) {
				String title = e.getNewValue() == null ? "" : e.getNewValue().toString();
				titleLabel.setText(title);
				if (frame != null) frame.setTitle(title);

			} else if (e.getPropertyName().equals(Browser.PROPERTY_ROLLOVER)) {
				String newRollover = e.getNewValue() == null ? "" : e.getNewValue().toString();
				rolloverLabel.setText(newRollover);
				if (frame != null) lblStatus.setText(newRollover);
				
			} else if (e.getPropertyName().equals(Browser.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer)e.getNewValue());

			} else if (e.getPropertyName().equals(Browser.PROPERTY_LABEL)) {

			} else if (e.getPropertyName().equals(Browser.PROPERTY_SCREEN)) {

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	public boolean snapToGrid() {
		return false;
	}
}
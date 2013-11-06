package monsters;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import party.DetailedMonster;
import swing.ImagePanel;

import combat.CombatPanel;

import digital_table.controller.ControllerFrame;
import digital_table.controller.TokenOptionsPanel;

@SuppressWarnings("serial")
public class AddMonsterDialog extends JDialog {
	private StatisticsBlock stats;

	private JTextField nameField;
	private JSpinner countSpinner;
	private JList monsterList;
	private DefaultListModel monsterListModel;
	private DetailedMonster selected;

	private ImagePanel imagePanel;
	private JButton prevImageButton;
	private JButton nextImageButton;

	private List<URL> imageURLs = new ArrayList<URL>();
	private int currentImageIndex = -1;	// TODO make per-token

	AddMonsterDialog(Window owner, final StatisticsBlock s) {
		super(owner, "Add new " + s.getName(), Dialog.ModalityType.MODELESS);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		stats = s;
		Collections.addAll(imageURLs, stats.getImageURLs());

		nameField = new JTextField(20);
		SpinnerModel model = new SpinnerNumberModel(1, 1, 100, 1);
		countSpinner = new JSpinner(model);
		countSpinner.addChangeListener(spinnerListener);
		// select image control

		monsterListModel = new DefaultListModel();
		selected = new DetailedMonster(stats);
		monsterListModel.addElement(selected);
		monsterList = new JList(monsterListModel);
		monsterList.setPreferredSize(new Dimension(200, 100));
		monsterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		monsterList.setSelectedIndex(0);
		monsterList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				updateFields();
			}
		});
		updateFields();
		JScrollPane scroller = new JScrollPane(monsterList);

		imagePanel = new ImagePanel(null);
		imagePanel.setPreferredSize(new Dimension(300, 300));

		JPanel buttonPanel = new JPanel();
		JButton imageButton = new JButton("Load Image");
		imageButton.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (TokenOptionsPanel.lastDir != null) chooser.setCurrentDirectory(TokenOptionsPanel.lastDir);
				if (chooser.showOpenDialog(AddMonsterDialog.this) == JFileChooser.APPROVE_OPTION) {
					try {
						File imageFile = chooser.getSelectedFile();
						imageURLs.add(imageFile.toURI().toURL());
						setSelectedImage(imageURLs.size() - 1);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Cancelled");
				}
			}
		});
		buttonPanel.add(imageButton);

		prevImageButton = new JButton("<");
		prevImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setSelectedImage(currentImageIndex - 1);
			}
		});
		buttonPanel.add(prevImageButton);

		nextImageButton = new JButton(">");
		nextImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setSelectedImage(currentImageIndex + 1);
			}
		});
		buttonPanel.add(nextImageButton);

		//@formatter:off
		JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0; main.add(new JLabel("Name:"), c);
		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.5d;
		c.gridx = 1; main.add(nameField, c);

		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		main.add(buttonPanel, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridy++;
		main.add(imagePanel, c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.5d; c.weighty = 0d;
		c.gridwidth = 1;
		c.gridy = 0;
		c.gridx = 2; main.add(new JLabel("Count:"), c);
		c.gridx = 3; main.add(countSpinner, c);

		c.fill = GridBagConstraints.BOTH; c.weightx = 1.0d; c.weighty = 1.0d;
		c.gridwidth = 2;
		c.gridheight = 2;
		c.gridx = 2; c.gridy = 1;
		main.add(scroller, c);
		// @formatter:on

		JButton addCombatButton = new JButton("Add To Combat");
		addCombatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < monsterListModel.getSize(); i++) {
					DetailedMonster m = (DetailedMonster) monsterListModel.get(i);
					CombatPanel.addMonster(m);
				}
			}
		});

		JButton addTokenButton = new JButton("Add Tokens");
		addTokenButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < monsterListModel.getSize(); i++) {
					DetailedMonster m = (DetailedMonster) monsterListModel.get(i);
					File f = null;
					if (currentImageIndex >= 0) {
						URL url = imageURLs.get(currentImageIndex);
						// TODO fix this. probably addMonster should take a URL for the image
						try {
							f = new File(url.toURI());
						} catch (URISyntaxException e) {
						}
					}
					ControllerFrame.addMonster(m, f);
				}
			}
		});

		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});

		JButton detailsButton = new JButton("Details");
		detailsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFrame frame = new MonsterFrame(stats);
				frame.setVisible(true);
			}
		});

		JPanel buttons = new JPanel();
		buttons.add(detailsButton);
		buttons.add(addCombatButton);
		buttons.add(addTokenButton);
		buttons.add(cancelButton);

		add(main, BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);
		pack();

		setSelectedImage(0);

		setVisible(true);
	}

	// handles out of range indexes
	private void setSelectedImage(int index) {
		if (index < 0) index = 0;
		if (index >= imageURLs.size()) index = imageURLs.size() - 1;
		currentImageIndex = index;
		BufferedImage image = null;
		if (currentImageIndex >= 0) {
			URL url = imageURLs.get(currentImageIndex);
			try {
				image = ImageIO.read(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		imagePanel.setImage(image);
		prevImageButton.setEnabled(currentImageIndex > 0);
		nextImageButton.setEnabled(currentImageIndex < imageURLs.size() - 1);
	}

	private void updateFields() {
		DetailedMonster m = (DetailedMonster) monsterList.getSelectedValue();
		if (m != selected) {
			if (selected != null) selected.setName(nameField.getText());
			selected = m;
		}
		if (selected != null) {
			nameField.setEnabled(true);
			nameField.setText(selected.getName());
		} else {
			nameField.setEnabled(false);
			nameField.setText("");
		}
	}

	final private ChangeListener spinnerListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			int newSize = (Integer) countSpinner.getValue();
			int oldSize = monsterListModel.getSize();
			monsterListModel.setSize(newSize);
			for (int i = oldSize; i < newSize; i++) {
				DetailedMonster m = new DetailedMonster(stats);
				m.setName(m.getName() + " " + (i + 1));
				monsterListModel.set(i, m);
			}
			DetailedMonster m = (DetailedMonster) monsterListModel.get(0);
			if (oldSize == 1 && newSize > 1 && !m.getName().endsWith(" 1")) {
				m.setName(m.getName() + " 1");
			} else if (newSize == 1 && m.getName().endsWith(" 1")) {
				m.setName(m.getName().substring(0, m.getName().length() - 2));
			}
			if (monsterList.getSelectedIndex() == 0) updateFields();
		}
	};
}

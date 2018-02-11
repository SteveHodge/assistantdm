package monsters;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import digital_table.controller.TokenOptionsPanel;
import gamesystem.Buff;
import gamesystem.BuffFactory;
import gamesystem.core.PropertyListener;
import gamesystem.core.Property;
import swing.ImagePanel;

@SuppressWarnings("serial")
class NamePanel extends DetailPanel {
	private Monster monster;

	private JTextField nameField;
	private JCheckBox augSummonCheck;
	private ImagePanel imagePanel;
	private JButton prevImageButton;
	private JButton nextImageButton;

	// XXX shared from AddMonsterDialog - this is messy
	private Map<StatisticsBlock, List<URL>> imageURLs;
	private Map<Monster, Integer> imageIndexes;

	private static Buff augmentedSummoning = BuffFactory.AUGMENTED_SUMMONING.getBuff();

	NamePanel(Map<StatisticsBlock, List<URL>> urls, Map<Monster, Integer> indexes) {
		imageURLs = urls;
		imageIndexes = indexes;

		nameField = new JTextField(20);
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updateName();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updateName();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updateName();
			}

		});

		augSummonCheck = new JCheckBox("Augmented Summoning");
		augSummonCheck.addItemListener(e -> {
			if (monster == null) return;
			if (augSummonCheck.isSelected()) {
				// if the buff has already been applied then we should not apply it again
				ListModel<Buff> buffs = monster.getBuffListModel();
				for (int i = 0; i < buffs.getSize(); i++) {
					Buff b = buffs.getElementAt(i);
					if (augmentedSummoning == b) return;
				}
				monster.addBuff(augmentedSummoning);
			} else {
				monster.removeBuff(augmentedSummoning.id);
			}
		});

		imagePanel = new ImagePanel(null);
		imagePanel.setPreferredSize(new Dimension(300, 300));

		JPanel buttonPanel = new JPanel();
		JButton imageButton = new JButton("Load Image");
		imageButton.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (TokenOptionsPanel.lastDir != null) chooser.setCurrentDirectory(TokenOptionsPanel.lastDir);
				if (chooser.showOpenDialog(NamePanel.this) == JFileChooser.APPROVE_OPTION) {
					try {
						File imageFile = chooser.getSelectedFile();
						List<URL> urls = imageURLs.get(monster.statisticsBlock);
						if (urls == null) {
							urls = new ArrayList<>();
							imageURLs.put(monster.statisticsBlock, urls);
						}
						urls.add(imageFile.toURI().toURL());
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
		prevImageButton.addActionListener(e -> setSelectedImage(getImageIndex() - 1));
		buttonPanel.add(prevImageButton);

		nextImageButton = new JButton(">");
		nextImageButton.addActionListener(e -> setSelectedImage(getImageIndex() + 1));
		buttonPanel.add(nextImageButton);

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0d;
		c.gridy = 0;
		c.gridx = 0;
		add(new JLabel("Name:"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5d;
		c.gridx = 1;
		add(nameField, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.weightx = 1.0d;
		add(augSummonCheck, c);

		c.gridy = 2;
		add(buttonPanel, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridy = 3;
		add(imagePanel, c);
	}

	private int getImageIndex() {
		Integer index = imageIndexes.get(monster);
		if (index == null) return -1;
		return index.intValue();
	}

	@Override
	void setMonster(Monster m) {
//		System.out.println("NamePanel.setMonster('" + m.getName() + "')");
		if (monster == m) return;
		if (monster != null) {
			//monster.setName(nameField.getText());	// should be unnecessary
			monster.removePropertyListener("name", listener);
		}
		monster = m;
		if (m != null) {
			nameField.setEnabled(true);
			nameField.setText(m.getName());
			monster.addPropertyListener("name", listener);
			if (!imageIndexes.containsKey(m)) {
				setSelectedImage(0);
			} else {
				setSelectedImage(imageIndexes.get(m));
			}

			augSummonCheck.setSelected(false);
			ListModel<Buff> buffs = monster.getBuffListModel();
			for (int i = 0; i < buffs.getSize(); i++) {
				Buff b = buffs.getElementAt(i);
				if (augmentedSummoning == b) augSummonCheck.setSelected(true);
			}
		}
	}

	// handles out of range indexes
	void setSelectedImage(int index) {
		if (monster == null) return;
		List<URL> urls = imageURLs.get(monster.statisticsBlock);
		if (index < 0) index = 0;
		if (index >= urls.size()) index = urls.size() - 1;
		imageIndexes.put(monster, index);
		BufferedImage image = null;
		if (getImageIndex() >= 0) {
			URL url = urls.get(getImageIndex());
			try {
				image = ImageIO.read(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		imagePanel.setImage(image);
		prevImageButton.setEnabled(getImageIndex() > 0);
		nextImageButton.setEnabled(getImageIndex() < urls.size() - 1);
	}

	private void updateName() {
		if (monster != null) {
			String newName = nameField.getText();
			if (!monster.getName().equals(newName)) monster.setName(newName);
		}
	}

	final private PropertyListener<Object> listener = new PropertyListener<Object>() {
		@Override
		public void propertyChanged(Property<Object> source, Object oldValue) {
			if (!nameField.getText().equals(source.getValue().toString())) {
				nameField.setText(source.getValue().toString());
			}
		}
	};
}
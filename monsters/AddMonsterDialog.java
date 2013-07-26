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

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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

import party.Monster;
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
	private Monster selected;
	private File imageFile = null;	// TODO make per-token
	private ImagePanel image = new ImagePanel(null);

	AddMonsterDialog(Window owner, StatisticsBlock stats) {
		super(owner, "Add new " + stats.getName(), Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.stats = stats;

		nameField = new JTextField(20);
		SpinnerModel model = new SpinnerNumberModel(1, 1, 100, 1);
		countSpinner = new JSpinner(model);
		countSpinner.addChangeListener(spinnerListener);
		// select image control

		monsterListModel = new DefaultListModel();
		selected = stats.createMonster();
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

		JButton imageButton = new JButton("Set Image");
		imageButton.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (TokenOptionsPanel.imageFile != null) chooser.setCurrentDirectory(TokenOptionsPanel.imageFile);
				if (chooser.showOpenDialog(AddMonsterDialog.this) == JFileChooser.APPROVE_OPTION) {
					imageFile = chooser.getSelectedFile();
					try {
						BufferedImage img = ImageIO.read(imageFile);
						image.setImage(img);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Cancelled");
				}
			}
		});

		//@formatter:off
		JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0; main.add(new JLabel("Name:"), c);
		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.5d;
		c.gridx = 1; main.add(nameField, c);

		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		main.add(imageButton, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridy++;
		main.add(image, c);

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
					Monster m = (Monster) monsterListModel.get(i);
					CombatPanel.addMonster(m);
				}
			}
		});

		JButton addTokenButton = new JButton("Add Tokens");
		addTokenButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < monsterListModel.getSize(); i++) {
					Monster m = (Monster) monsterListModel.get(i);
					ControllerFrame.addMonster(m, imageFile);
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

		JPanel buttons = new JPanel();
		buttons.add(addCombatButton);
		buttons.add(addTokenButton);
		buttons.add(cancelButton);

		add(main, BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	private void updateFields() {
		Monster m = (Monster) monsterList.getSelectedValue();
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
				Monster m = stats.createMonster();
				m.setName(m.getName() + " " + (i + 1));
				monsterListModel.set(i, m);
			}
			Monster m = (Monster) monsterListModel.get(0);
			if (oldSize == 1 && newSize > 1 && !m.getName().endsWith(" 1")) {
				m.setName(m.getName() + " 1");
			} else if (newSize == 1 && m.getName().endsWith(" 1")) {
				m.setName(m.getName().substring(0, m.getName().length() - 2));
			}
			if (monsterList.getSelectedIndex() == 0) updateFields();
		}
	};
}

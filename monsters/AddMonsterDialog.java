package monsters;

import gamesystem.Creature;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import monsters.StatisticsBlock.Field;

import combat.CombatPanel;

import digital_table.controller.ControllerFrame;

//TODO should keep a map of Field to DetailPanel so we can update them automatically and select the right one more easily

@SuppressWarnings("serial")
public class AddMonsterDialog extends JDialog {
	private static final String BLANK_PANEL = "BLANK";

	private StatisticsBlock stats;

	private JSpinner countSpinner;
	private JList monsterList;
	private MonsterListModel monsterListModel;
	private Monster selected;

	private StatsBlockPanel statsPanel;

	private CardLayout detailLayout;
	private JPanel detailPanel;
	private Map<Field, DetailPanel> detailPanels = new HashMap<Field, DetailPanel>();

	private List<URL> imageURLs = new ArrayList<URL>();
	private Map<Monster, Integer> imageIndexes = new HashMap<Monster, Integer>();

	AddMonsterDialog(Window owner, final StatisticsBlock s) {
		super(owner, "Add new " + s.getName(), Dialog.ModalityType.MODELESS);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		stats = s;
		Collections.addAll(imageURLs, stats.getImageURLs());

		NamePanel namePanel = new NamePanel(imageURLs, imageIndexes);
		detailPanels.put(Field.NAME, namePanel);
		detailPanels.put(Field.ABILITIES, new AbilitiesPanel());
		detailPanels.put(Field.HITDICE, new HitPointsPanel());
		for (Field f : Field.getStandardOrder()) {
			// TODO implement parsing these remaining fields:
			if (f == Field.SIZE_TYPE
					|| f == Field.AC
					|| f == Field.SPACE_REACH
					|| f == Field.SPECIAL_QUALITIES
					|| f == Field.FEATS) continue;

			if (!detailPanels.containsKey(f)) {
				detailPanels.put(f, new DefaultDetailPanel(f));
			}
		}

		SpinnerModel model = new SpinnerNumberModel(1, 1, 100, 1);
		countSpinner = new JSpinner(model);
		countSpinner.addChangeListener(spinnerListener);
		// select image control

		monsterListModel = new MonsterListModel();
		selected = StatsBlockCreatureView.getMonster(stats);
		monsterListModel.addMonster(selected);
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

		statsPanel = new StatsBlockPanel(selected);
		statsPanel.setSelectionForeground(monsterList.getSelectionForeground());
		statsPanel.setSelectionBackground(monsterList.getSelectionBackground());
		statsPanel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				updateFields();
			}
		});

		detailLayout = new CardLayout();
		detailPanel = new JPanel(detailLayout);
		for (Field f : detailPanels.keySet()) {
			detailPanel.add(detailPanels.get(f), f.name());
		}
		detailPanel.add(new JPanel(), BLANK_PANEL);
		detailLayout.show(detailPanel, BLANK_PANEL);

		//@formatter:off
		JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE; c.weightx = 0d; c.weighty = 0d;
		c.gridy = 0;
		c.gridx = 0; main.add(new JLabel("Count:"), c);
		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.5d;
		c.gridx = 1; main.add(countSpinner, c);

		c.fill = GridBagConstraints.BOTH; c.weightx = 0.5d; c.weighty = 1.0d;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 1;
		main.add(new JScrollPane(monsterList), c);

		c.fill = GridBagConstraints.NONE; c.weightx = 0d; c.weighty = 0d;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.gridx = 2; c.gridy = 0;
		main.add(statsPanel, c);

		c.gridx = 3;
		c.fill = GridBagConstraints.BOTH; c.weightx = 1.0d; c.weighty = 1.0d;
		main.add(detailPanel, c);

		// @formatter:on

		JButton addCombatButton = new JButton("Add To Combat");
		addCombatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < monsterListModel.getSize(); i++) {
					Creature m = monsterListModel.getElementAt(i);
					CombatPanel.addMonster(m);
				}
			}
		});

		JButton addTokenButton = new JButton("Add Tokens");
		addTokenButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < monsterListModel.getSize(); i++) {
					Creature m = monsterListModel.getElementAt(i);
					Integer imgIndex = imageIndexes.get(m);
					File f = null;
					if (imgIndex != null && imgIndex.intValue() >= 0) {
						URL url = imageURLs.get(imgIndex.intValue());
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

		for (DetailPanel p : detailPanels.values()) {
			p.setMonster(selected);
		}
		updateFields();
		namePanel.setSelectedImage(0);

		setVisible(true);
	}

	private void updateFields() {
		Monster m = (Monster) monsterList.getSelectedValue();

		if (m != selected) {
			selected = m;
			statsPanel.setCreature(selected);
			for (DetailPanel p : detailPanels.values()) {
				p.setMonster(selected);
			}
		}

		if (selected != null) {
			if (detailPanels.containsKey(statsPanel.getSelectedField())) {
				detailLayout.show(detailPanel, statsPanel.getSelectedField().name());
			} else {
				detailLayout.show(detailPanel, BLANK_PANEL);
			}
		} else {
			detailLayout.show(detailPanel, BLANK_PANEL);
		}
	}

	// TODO name generation needs to be smarter now that individuals can be renamed
	final private ChangeListener spinnerListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			int newSize = (Integer) countSpinner.getValue();
			int oldSize = monsterListModel.getSize();

			// remove extra monsters if the size has got smaller:
			for (int i = oldSize; i > newSize; i--) {
				monsterListModel.removeElementAt(i - 1);
			}

			// add any extra monsters if the size has got larger:
			for (int i = oldSize; i < newSize; i++) {
				Monster m = StatsBlockCreatureView.getMonster(stats);
				m.setName(m.getName() + " " + (i + 1));
				monsterListModel.addMonster(m);
			}

			// fixup the name of the first monster if necessary
			Creature m = monsterListModel.getElementAt(0);
			if (oldSize == 1 && newSize > 1 && !m.getName().endsWith(" 1")) {
				m.setName(m.getName() + " 1");
			} else if (newSize == 1 && m.getName().endsWith(" 1")) {
				m.setName(m.getName().substring(0, m.getName().length() - 2));
			}
		}
	};

	private class MonsterListModel extends AbstractListModel {
		private List<Monster> monsters = new ArrayList<Monster>();

		@Override
		public Creature getElementAt(int i) {
			return monsters.get(i);
		}

		public void removeElementAt(int i) {
			Creature m = monsters.remove(i);
			m.removePropertyChangeListener(listener);
			fireIntervalRemoved(this, i, i);
		}

		public void addMonster(Monster m) {
			m.addPropertyChangeListener(listener);
			monsters.add(m);
			fireIntervalAdded(this, monsters.size() - 1, monsters.size() - 1);
		}

		@Override
		public int getSize() {
			return monsters.size();
		}

		private PropertyChangeListener listener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(Creature.PROPERTY_NAME)) {
					int idx = monsters.indexOf(evt.getSource());
					if (idx != -1) {
						fireContentsChanged(this, idx, idx);
					}
				}
			}
		};
	}
}
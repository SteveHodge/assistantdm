package ui;

// TODO replace select all JButtons with radio buttons?
// TODO add days counter?

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import gamesystem.Creature;
import gamesystem.HPs;
import party.Character;
import party.Party;

@SuppressWarnings("serial")
public class RestDialog extends JDialog {
	public RestDialog(JFrame parent, Party party) {
		super(SwingUtilities.getWindowAncestor(parent), "Resting");

		List<CharacterRow> rows = new ArrayList<>();

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 2, 1, 2);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		add(new JLabel("Name"), c);
		add(new JLabel("HP"), c);
		add(new JLabel("No Rest"), c);
		add(new JLabel("<html>Rest<br>8 hr<br>1/lvl</html>"), c);
		add(new JLabel("<html>Rest<br>24 hr<br>2/lvl</html>"), c);
		add(new JLabel("<html>LTC<br>8 hr<br>2/lvl</html>"), c);
		add(new JLabel("<html>LTC<br>24 hr<br>4/lvl</html>"), c);
		add(new JLabel("<html>Reset<br>sanity<br>session</html>"), c);

		c.gridy++;
		c.gridx = 2;
		JButton select = new JButton("All");
		select.addActionListener(e -> {
			for (CharacterRow r : rows) {
				r.noneButton.setSelected(true);
			}
		});
		add(select, c);
		select = new JButton("All");
		select.addActionListener(e -> {
			for (CharacterRow r : rows) {
				r.oneButton.setSelected(true);
			}
		});
		c.gridx = GridBagConstraints.RELATIVE;
		add(select, c);
		select = new JButton("All");
		select.addActionListener(e -> {
			for (CharacterRow r : rows) {
				r.twoButton.setSelected(true);
			}
		});
		add(select, c);
		select = new JButton("All");
		select.addActionListener(e -> {
			for (CharacterRow r : rows) {
				r.twoLTCButton.setSelected(true);
			}
		});
		add(select, c);
		select = new JButton("All");
		select.addActionListener(e -> {
			for (CharacterRow r : rows) {
				r.fourLTCButton.setSelected(true);
			}
		});
		add(select, c);
		JCheckBox all = new JCheckBox();
		all.setSelected(true);
		all.addActionListener(e -> {
			for (CharacterRow r : rows) {
				r.sanityCheck.setSelected(all.isSelected());
			}
		});
		add(all, c);

		c.gridwidth = 8;
		c.gridy++;
		add(new JSeparator(SwingConstants.HORIZONTAL), c);
		c.gridwidth = 1;

		for (Character chr : party) {
			c.gridy++;
			CharacterRow row = new CharacterRow(chr);
			rows.add(row);
			add(row.nameLabel, c);
			add(row.hpLabel, c);
			add(row.noneButton, c);
			add(row.oneButton, c);
			add(row.twoButton, c);
			add(row.twoLTCButton, c);
			add(row.fourLTCButton, c);
			add(row.sanityCheck, c);
		}

		c.gridy++;
		JButton healAll = new JButton("Heal All");
		healAll.addActionListener(e -> {
			for (CharacterRow r : rows) {
				HPs hps = r.character.getHPStatistic();
				hps.applyHealing(Math.max(hps.getWounds(), hps.getNonLethal()));
			}
		});
		add(healAll, c);

		c.gridwidth = 7;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> {
			for (CharacterRow r : rows) {
				HPs hps = r.character.getHPStatistic();
				int level = r.character.getLevel();
				if (r.oneButton.isSelected()) {
					hps.applyHealing(level);
				} else if (r.twoButton.isSelected() || r.twoLTCButton.isSelected()) {
					hps.applyHealing(level * 2);
				} else if (r.fourLTCButton.isSelected()) {
					hps.applyHealing(level * 4);
				}
				if (r.sanityCheck.isSelected()) {
					r.character.getSanity().startSession();
				}
			}
		});
		add(apply, c);

		pack();
		setLocationRelativeTo(SwingUtilities.getWindowAncestor(parent));
	}

	class CharacterRow {
		Character character;
		JLabel nameLabel;
		JLabel hpLabel;
		ButtonGroup group = new ButtonGroup();
		JRadioButton noneButton = new JRadioButton();
		JRadioButton oneButton = new JRadioButton();
		JRadioButton twoButton = new JRadioButton();
		JRadioButton twoLTCButton = new JRadioButton();
		JRadioButton fourLTCButton = new JRadioButton();
		JCheckBox sanityCheck = new JCheckBox();

		CharacterRow(Character c) {
			character = c;
			nameLabel = new JLabel(c.getName());
			hpLabel = new JLabel(c.getHPStatistic().getShortSummary());
			noneButton.setSelected(true);
			group.add(noneButton);
			group.add(oneButton);
			group.add(twoButton);
			group.add(twoLTCButton);
			group.add(fourLTCButton);
			sanityCheck.setSelected(true);

			c.getHPStatistic().addPropertyListener((source, oldValue) -> {
				hpLabel.setText(c.getHPStatistic().getShortSummary());
			});

			c.addPropertyChangeListener(Creature.PROPERTY_NAME, e -> {
				nameLabel.setText(c.getName());
			});
		}
	}
}

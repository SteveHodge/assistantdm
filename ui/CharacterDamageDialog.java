package ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import gamesystem.AbilityScore;
import gamesystem.HPs;
import gamesystem.Sanity;
import party.Character;

@SuppressWarnings("serial")
public class CharacterDamageDialog extends JDialog {
	Character character;
	HPPanel hpPanel;
	SanityPanel sanityPanel;

	public CharacterDamageDialog(JComponent parent, String title, Character chr) {
		super(SwingUtilities.getWindowAncestor(parent), title + " - " + chr.getName());

		character = chr;

		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		hpPanel = new HPPanel();
		add(hpPanel);

		sanityPanel = new SanityPanel();
		add(sanityPanel);
		add(sanityPanel);

		hpPanel.updateSummary();
		sanityPanel.updateSummary();
		pack();	// not needed as updateSummary packs
		setLocationRelativeTo(SwingUtilities.getWindowAncestor(parent));
	}

	public static void openDialog(JComponent parent, String title, Character chr) {
		CharacterDamageDialog dialog = new CharacterDamageDialog(parent, title, chr);
		dialog.setVisible(true);
		dialog.hpPanel.dmgField.grabFocus();
	}

	class SanityPanel extends JPanel {
		Sanity sanity;
		JFormattedTextField dmgField;
		JFormattedTextField healField;
		JLabel maxLabel = new JLabel();
		JLabel startingLabel = new JLabel();
		JLabel currentLabel = new JLabel();
		JLabel recentLabel = new JLabel();
		JLabel messageLabel = new JLabel();

		public SanityPanel() {
			sanity = character.getSanity();
			setBorder(BorderFactory.createTitledBorder("Sanity"));

			setLayout(new GridBagLayout());

			dmgField = new JFormattedTextField(0);
			dmgField.setColumns(3);
			dmgField.addPropertyChangeListener(e -> updateSummary());
			healField = new JFormattedTextField(0);
			healField.addPropertyChangeListener(e -> updateSummary());
			healField.setColumns(3);

			JButton apply = new JButton("Apply");
			apply.addActionListener(e -> {
				int dmg = (Integer) dmgField.getValue();
				if (dmg > 0) sanity.applyDamage(dmg);
				dmgField.setValue(0);

				int heal = (Integer) healField.getValue();
				if (heal > 0) sanity.applyHealing(heal);
				healField.setValue(0);

				updateSummary();
			});

			JButton healAll = new JButton("Heal All");
			healAll.addActionListener(e -> {
				int healing = sanity.getStartingSanityProperty().getValue() - sanity.getValue();
				if (healing > 0) sanity.applyHealing(healing);
				dmgField.setValue(0);
				healField.setValue(0);
				updateSummary();
			});

			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(1, 2, 1, 2);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.0;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = GridBagConstraints.RELATIVE;
			c.gridy = 0;
			add(new JLabel("Max Sanity:"), c);
			add(maxLabel, c);
			add(new JLabel("Starting Sanity:"), c);
			add(startingLabel, c);

			c.gridy = 1;
			add(new JLabel("Current Sanity:"), c);
			add(currentLabel, c);
			add(new JLabel("Lost Recently:"), c);
			add(recentLabel, c);

			c.gridy = 2;
			add(new JLabel("Damage:"), c);
			add(dmgField, c);
			c.gridwidth = 2;
			add(apply, c);

			c.gridy = 3;
			c.gridwidth = 1;
			add(new JLabel("Healing:"), c);
			add(healField, c);
			c.gridwidth = 2;
			add(healAll, c);

			c.gridy = 4;
			c.gridwidth = 4;
			add(messageLabel, c);
		}

		void updateSummary() {
			int dmg = (Integer) dmgField.getValue();
			int starting = sanity.getStartingSanityProperty().getValue();
			int current = sanity.getValue() - dmg + (Integer) healField.getValue();
			if (current > starting) current = starting;
			int session = sanity.getSessionStartingSanity() - current;

			maxLabel.setText(Integer.toString(sanity.getMaximumSanityProperty().getValue()));
			startingLabel.setText(Integer.toString(starting));
			currentLabel.setText(Integer.toString(current));
			recentLabel.setText(Integer.toString(session));

			if (current <= -10) {
				messageLabel.setText("Permanent insanity triggered");
			} else if (session > sanity.getMaximumSanityProperty().getValue() / 5) {
				messageLabel.setText("Indefinite insanity triggered");
			} else if (dmg >= character.getAbilityScore(AbilityScore.Type.WISDOM) / 2) {
				messageLabel.setText("Pass sanity check or temporary insanity");
			} else {
				messageLabel.setText(" ");
			}
		}
	}

	class HPPanel extends JPanel {
		HPs hps;
		JFormattedTextField dmgField;
		JFormattedTextField healField;
		JCheckBox nonLethal = new JCheckBox("Non-lethal");
		JLabel summary;

		public HPPanel() {
			hps = character.getHPStatistic();

			setBorder(BorderFactory.createTitledBorder("Hit Points"));

			setLayout(new GridBagLayout());

			summary = new JLabel();
			dmgField = new JFormattedTextField(0) {
				@Override
				protected void processFocusEvent(FocusEvent e) {
					super.processFocusEvent(e);
					if (e.isTemporary())
						return;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							selectAll();
						}
					});
				}
			};
			dmgField.setColumns(3);
			dmgField.addPropertyChangeListener(e -> updateSummary());
			healField = new JFormattedTextField(0);
			healField.addPropertyChangeListener(e -> updateSummary());
			healField.setColumns(3);
			nonLethal.addChangeListener(e -> updateSummary());

			JButton apply = new JButton("Apply");
			apply.addActionListener(e -> {
				int dmg = (Integer) dmgField.getValue();
				if (nonLethal.isSelected()) {
					hps.applyNonLethal(dmg);
				} else {
					hps.applyDamage(dmg);
				}
				dmgField.setValue(0);

				int heal = (Integer) healField.getValue();
				hps.applyHealing(heal);
				healField.setValue(0);
			});

			JButton healAll = new JButton("Heal All");
			healAll.addActionListener(e -> {
				hps.applyHealing(Math.max(hps.getWounds(), hps.getNonLethal()));
				dmgField.setValue(0);
				healField.setValue(0);
				updateSummary();
			});

			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(1, 2, 1, 2);
			c.gridx = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.0;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridy = 0;
			add(new JLabel("New Hit Points:"), c);
			c.gridy++;
			add(new JLabel("Damage:"), c);
			c.gridy++;
			add(new JLabel("Healing:"), c);

			c.gridx = 1;
			c.gridy = 0;
			add(summary, c);
			c.gridy++;
			add(dmgField, c);
			c.gridy++;
			add(healField, c);

			c.gridx = 2;
			c.gridy = 0;
			add(apply, c);
			c.gridy++;
			add(nonLethal, c);
			c.gridy++;
			add(healAll, c);
		}

		void updateSummary() {
			int current = hps.getHPs();
			int newDmg = 0;
			int newNL = hps.getNonLethal();
			if (!nonLethal.isSelected()) {
				newDmg = (Integer) dmgField.getValue();
				current -= newDmg;
			} else {
				newNL += (Integer) dmgField.getValue();
			}
			newNL -= (Integer) healField.getValue();
			if (newNL < 0) newNL = 0;
			current += Math.min((Integer) healField.getValue(), Math.max(0, hps.getWounds() - (hps.getTemporaryHPs() - newDmg)));	// this should account for the effects of healing including temporary hitpoints and damage done simultaneously

			StringBuilder text = new StringBuilder();
			text.append("<html><body>").append(current);
			if (newNL > 0) text.append(" (").append(newNL).append(" NL)");
			text.append(" / ").append(hps.getMaximumHitPoints()).append("</body></html>");
			summary.setText(text.toString());
			pack();
		}
	}
}

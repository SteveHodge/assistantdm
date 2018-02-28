package ui;

// TODO allow switching characters?

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
public class CharacterDamagePanel extends JPanel {
	HPPanel hpPanel;

	public CharacterDamagePanel(Character chr) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		hpPanel = new HPPanel(chr.getHPStatistic());
		add(hpPanel);

		SanityPanel sanityPanel = new SanityPanel(chr.getSanity(), chr.getAbilityStatistic(AbilityScore.Type.WISDOM));
		add(sanityPanel);

		hpPanel.updateSummary();
		sanityPanel.updateSummary();
	}

	public static void openDialog(JComponent parent, String title, Character chr) {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title + " - " + chr.getName());
		CharacterDamagePanel panel = new CharacterDamagePanel(chr);
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(parent));
		dialog.setVisible(true);
		panel.hpPanel.dmgField.grabFocus();
	}

	static class SanityPanel extends JPanel {
		Sanity sanity;
		AbilityScore wisdom;
		JFormattedTextField dmgField;
		JFormattedTextField healField;
		JLabel maxLabel = new JLabel();
		JLabel startingLabel = new JLabel();
		JLabel currentLabel = new JLabel();
		JLabel recentLabel = new JLabel();
		JLabel messageLabel = new JLabel();

		public SanityPanel(Sanity s, AbilityScore wis) {
			sanity = s;
			wisdom = wis;
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
			} else if (dmg >= wisdom.getValue() / 2) {
				messageLabel.setText("Pass sanity check or temporary insanity");
			} else {
				messageLabel.setText(" ");
			}
		}
	}

	public static class HPPanel extends JPanel {
		HPs hps;
		JFormattedTextField dmgField;
		JFormattedTextField healField;
		JCheckBox nonLethal = new JCheckBox("Non-lethal");
		JLabel summary;

		public HPPanel(HPs hps) {
			this.hps = hps;

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
			text.append(" / ").append(hps.getMaxHPStat().getValue()).append("</body></html>");
			summary.setText(text.toString());
			revalidate();
		}
	}
}

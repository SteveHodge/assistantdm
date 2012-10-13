package ui;

import gamesystem.Buff;
import gamesystem.BuffFactory;
import gamesystem.Dice;
import gamesystem.Modifier;
import gamesystem.Buff.Effect;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import party.Character;

@SuppressWarnings("serial")
public class CharacterBuffsPanel extends JPanel implements ItemListener {
	Character character;

	protected class BuffCheckBox extends JCheckBox {
		BuffFactory buffFactory;
		Buff buff;

		BuffCheckBox(BuffFactory bf) {
			super(bf.name);
			buffFactory = bf;
			setToolTipText(bf.getDescription());
		}
	}

	public CharacterBuffsPanel(Character c) {
		character = c;
		
		setBorder(new TitledBorder("Buffs / Penalties"));
		setLayout(new GridLayout(0,3));
		for (BuffFactory bf : BuffFactory.buffs) {
			JCheckBox cb = new BuffCheckBox(bf);
			cb.addItemListener(this);
			add(cb);
		}
	}

	// Note: this class assumes that the Effects in a Buff don't change.
	protected class BuffOptionPanel extends JPanel {
		Buff buff;
		SpinnerNumberModel clModel;
		JLabel[] effectLabels;
		SpinnerNumberModel[] diceModels;

		BuffOptionPanel(Buff b) {
			buff = b;
			buff.setCasterLevel(1);

			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			JPanel clPanel = new JPanel();
			clPanel.add(new JLabel("Caster Level: "));
			clModel = new SpinnerNumberModel(1, 1, 20, 1);
			JSpinner clSpinner = new JSpinner(clModel);
			clPanel.add(clSpinner);
			add(clPanel);

			JPanel effectsPanel = new JPanel();
			effectsPanel.setLayout(new GridBagLayout());
			effectLabels = new JLabel[buff.effects.size()];
			diceModels = new SpinnerNumberModel[buff.effects.size()];
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.LINE_START;
			c.insets = new Insets(2,5,2,5);
			int i = 0;
			for (Effect e : buff.effects) {
				JLabel l = new JLabel(e.toString());
				c.gridy = i; c.gridx = 0;
				effectsPanel.add(l, c);
				c.fill = GridBagConstraints.NONE;

				final Dice d = e.getDice();
				if (d != null) {
					int roll = d.roll();
					buff.setRoll(d, roll);
					diceModels[i] = new SpinnerNumberModel(roll, d.getMinimum(), d.getMaximum(), 1);
					diceModels[i].addChangeListener(new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							buff.setRoll(d, ((SpinnerNumberModel)e.getSource()).getNumber().intValue());
							updateEffects();
						}
					});
					JSpinner s = new JSpinner(diceModels[i]);
					c.gridx = 1;
					effectsPanel.add(new JLabel("Roll: "), c);
					c.gridx = 2;
					effectsPanel.add(s, c);
				}
				if (e.requiresCasterLevel()) {
					effectLabels[i] = new JLabel();
					c.gridx = 3;
					effectsPanel.add(effectLabels[i], c);
				}
				i++;
			}
			add(effectsPanel);

			clModel.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					buff.setCasterLevel(clModel.getNumber().intValue());
					updateEffects();
				}
			});

			updateEffects();
		}
		
		protected void updateEffects() {
			int i = 0;
			for (Effect e : buff.effects) {
				if (e.requiresCasterLevel()) {
					StringBuilder s = new StringBuilder("(");
					Modifier m = e.getModifier(buff);
					if (m.getModifier() >= 0) s.append("+");
					s.append(m.getModifier()).append(")");
					effectLabels[i].setText(s.toString());
				}
				i++;
			}
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (!(e.getSource() instanceof BuffCheckBox)) return;
		final BuffCheckBox cb = (BuffCheckBox)e.getSource();
		if (e.getStateChange() == ItemEvent.SELECTED) {
			// need to invoke this code later since it involves a modal dialog
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					cb.buff = cb.buffFactory.getBuff();
					if (cb.buff.requiresCasterLevel()) {
						// TODO add cancel button?
						final JDialog dialog = new JDialog((JDialog)null, "Enter caster level...", true);
						dialog.add(new BuffOptionPanel(cb.buff));

						JButton ok = new JButton("Ok");
						ok.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								// TODO confirm editing of clSpinner?
								dialog.setVisible(false);
							}
						});
						JPanel buttonPanel = new JPanel();
						buttonPanel.add(ok);
						dialog.add(buttonPanel, BorderLayout.PAGE_END);

						dialog.pack();
						dialog.setVisible(true);
					}
					cb.buff.applyBuff(character);
				}
			});
		} else {
			cb.buff.removeBuff(character);
			cb.buff = null;
		}
	}
}

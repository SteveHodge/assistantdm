package ui;

import gamesystem.Buff;
import gamesystem.BuffFactory;
import gamesystem.Modifier;
import gamesystem.Buff.Effect;
import gamesystem.dice.Dice;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import party.Character;
import swing.JListWithToolTips;
import swing.ListModelWithToolTips;

// TODO have caster level selected with the max/empower check boxes?

@SuppressWarnings("serial")
public class CharacterBuffsPanel extends CharacterSubPanel {
	JCheckBox empCheckBox;
	JCheckBox maxCheckBox;

	public CharacterBuffsPanel(Character c) {
		super(c);
		setLayout(new GridLayout(0,2));

		BuffFactory[] availableBuffs = Arrays.copyOf(BuffFactory.buffs, BuffFactory.buffs.length);
		Arrays.sort(availableBuffs, new Comparator<BuffFactory>() {
			public int compare(BuffFactory a, BuffFactory b) {
				return a.name.compareTo(b.name);
			}
		});
		BuffListModel bfModel = new BuffListModel(availableBuffs);
		final JListWithToolTips buffs = new JListWithToolTips(bfModel);
		buffs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buffs.setVisibleRowCount(20);

		final JListWithToolTips applied = new JListWithToolTips(character.buffs);
		applied.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		applied.setVisibleRowCount(8);

		JButton apply = new JButton("Apply");
		apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// need to invoke this code later since it involves a modal dialog
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						BuffFactory bf = (BuffFactory)buffs.getSelectedValue();
						Buff buff = applyBuff(bf);
						character.buffs.addElement(buff);
					}
				});
			}
		});

		JButton remove = new JButton("Remove");
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] buffs = applied.getSelectedValues();
				for (Object b : buffs) {
					((Buff)b).removeBuff(character);
					character.buffs.removeElement(b);
				}
			}
		});

		JScrollPane scroller = new JScrollPane(buffs);
		//scroller.setPreferredSize(preferredSize);
		add(scroller);

		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
		empCheckBox = new JCheckBox("Empowered");
		right.add(empCheckBox);
		maxCheckBox = new JCheckBox("Maximized");
		right.add(maxCheckBox);

		JPanel buttons = new JPanel();
		buttons.add(apply);
		buttons.add(remove);
		right.add(buttons);

		scroller = new JScrollPane(applied);
		scroller.setBorder(new TitledBorder("Currently Applied:"));
		right.add(scroller);
		add(right);
	}

	protected Buff applyBuff(BuffFactory bf) {
		Buff buff = bf.getBuff();
		buff.maximized = maxCheckBox.isSelected();
		buff.empowered = empCheckBox.isSelected();
		if (buff.requiresCasterLevel()) {
			// TODO add cancel button?
			Window parentWindow = SwingUtilities.getWindowAncestor(this);
			final JDialog dialog = new JDialog(parentWindow, "Enter caster level...", Dialog.DEFAULT_MODALITY_TYPE);
			dialog.add(new BuffOptionPanel(buff));

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
			dialog.setLocationRelativeTo(parentWindow);
			dialog.setVisible(true);
		}
		buff.applyBuff(character);
		return buff;
	}

	public static class BuffListModel extends DefaultListModel implements ListModelWithToolTips {
		public BuffListModel() {
			super();
		}

		public BuffListModel(BuffFactory[] buffs) {
			super();
			for (BuffFactory bf : buffs) {
				addElement(bf);
			}
		}

		public String getToolTipAt(int index) {
			if (index < 0) return null;
			Object o = get(index);
			if (o instanceof BuffFactory) {
				return ((BuffFactory)o).getDescription();
			} else if (o instanceof Buff) {
				return ((Buff)o).getDescription();
			}
			return null;
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
				if (d != null && !buff.maximized || buff.empowered) {
					// if the buff is maximized but not empowered then we don't need to roll

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

}

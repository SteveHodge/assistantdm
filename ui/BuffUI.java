package ui;

import gamesystem.Buff;
import gamesystem.BuffFactory;
import gamesystem.EffectorDefinition;
import gamesystem.Modifier;
import gamesystem.Spell;
import gamesystem.dice.Dice;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import swing.JListWithToolTips;
import swing.ListModelWithToolTips;

public class BuffUI {
	@SuppressWarnings("serial")
	public static class BuffListModel extends DefaultListModel<BuffFactory> implements ListModelWithToolTips<BuffFactory> {
		SortedMap<BuffFactory, Boolean> source = new TreeMap<>();	// state of all buffs in the list

		public BuffListModel() {
			super();
		}

		public BuffListModel(BuffFactory[] buffs) {
			super();
			for (BuffFactory bf : buffs) {
				source.put(bf, true);
				addElement(bf);
			}
		}

		@Override
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

		public void filter(Predicate<BuffFactory> pred) {
			int i = 0;
			for (BuffFactory bf : source.keySet()) {
				boolean current = source.get(bf);
				boolean newVal = pred.test(bf);
				if (newVal) {
					if (!current) add(i, bf);
					i++;
				} else if (current) {
					removeElement(bf);
				}
				source.put(bf, newVal);
			}
		}
	}

	private JPanel buffOptionsPanel;

	// controls used in the options panel
	private Buff buff;
	private SpinnerNumberModel clModel;
	private JSpinner clSpinner;
	private JCheckBox empCheckBox;
	private JCheckBox maxCheckBox;
	private JPanel clPanel;
	private JListWithToolTips<BuffFactory> buffs;

	// controls used in the descriptions of the effects of the buff
	private JLabel[] effectLabels;

	public BuffUI() {
		buffOptionsPanel = new JPanel();
		buffOptionsPanel.setLayout(new BoxLayout(buffOptionsPanel, BoxLayout.PAGE_AXIS));

		clModel = new SpinnerNumberModel(1, 1, 20, 1);
		clModel.addChangeListener(e -> {
			if (buff != null) {
				buff.setCasterLevel(clModel.getNumber().intValue());
				updateEffects(buff);
			}
		});
		clSpinner = new JSpinner(clModel);

		empCheckBox = new JCheckBox("Empowered");
		empCheckBox.addItemListener(e -> {
			buff.empowered = empCheckBox.isSelected();
			if (buff.maximized) {
				updateBuffOptions();	// need to update all options as dice spinners need to be re-enabled
			} else {
				updateEffects(buff);
			}
		});

		maxCheckBox = new JCheckBox("Maximized");
		maxCheckBox.addItemListener(e -> {
			buff.maximized = maxCheckBox.isSelected();
			updateBuffOptions();
		});

		clPanel = new JPanel();
		clPanel.add(new JLabel("Caster Level: "));
		clPanel.add(clSpinner);
		clPanel.add(empCheckBox);
		clPanel.add(maxCheckBox);
	}

	public JListWithToolTips<BuffFactory> getBuffList() {
		return getBuffList(false);
	}

	public JListWithToolTips<BuffFactory> getBuffList(boolean allSpells) {
		List<BuffFactory> buffsList = new ArrayList<>();
		for (Spell s : Spell.spells) {
			if (allSpells && s.buffFactories.size() == 0) {
				BuffFactory e = new BuffFactory(s.name);
				e.source = s;
				buffsList.add(e);
			} else {
				for (BuffFactory f : s.buffFactories.values()) {
					buffsList.add(f);
				}
			}
		}
		BuffFactory[] availableBuffs = buffsList.toArray(new BuffFactory[buffsList.size()]);
		Arrays.sort(availableBuffs, (a, b) -> a.name.compareTo(b.name));
		BuffListModel bfModel = new BuffListModel(availableBuffs);
		buffs = new JListWithToolTips<>(bfModel);
		buffs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buffs.setVisibleRowCount(20);
		buffs.addListSelectionListener(e -> {
			BuffFactory b = buffs.getSelectedValue();
			if (b != null) {
				buff = b.getBuff();
				buff.setCasterLevel(clModel.getNumber().intValue());
				buff.empowered = empCheckBox.isSelected();
				buff.maximized = maxCheckBox.isSelected();
				updateBuffOptions();
			} else {
				buff = null;
				buffOptionsPanel.removeAll();
			}
		});
		return buffs;
	}

	public Buff getBuff() {
		return buff;
	}

	private void updateBuffOptions() {
		buffOptionsPanel.removeAll();

		if (buff.requiresCasterLevel()) {
			buffOptionsPanel.add(clPanel);
		}
		empCheckBox.setVisible(false);
		maxCheckBox.setVisible(false);

		JPanel effectsPanel = new JPanel();
		effectsPanel.setLayout(new GridBagLayout());
		effectLabels = new JLabel[buff.effects.size()];
		SpinnerNumberModel[] diceModels = new SpinnerNumberModel[buff.effects.size()];
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(2, 5, 2, 5);
		c.gridy = 0;
		int i = 0;
		for (EffectorDefinition.Effect e : buff.effects) {
			JLabel l = new JLabel(e.toString().trim());
			c.gridx = 0;
			c.gridwidth = 3;
			effectsPanel.add(l, c);
			c.gridwidth = 1;
			c.fill = GridBagConstraints.NONE;

			final Dice d = e.getDice();
			if (d != null) {
				empCheckBox.setVisible(true);
				maxCheckBox.setVisible(true);
				if ((!buff.maximized || buff.empowered)) {
					// if the buff is maximized but not empowered then we don't need to roll

					int roll = d.roll();
					buff.setRoll(d, roll);
					diceModels[i] = new SpinnerNumberModel(roll, d.getMinimum(), d.getMaximum(), 1);
					diceModels[i].addChangeListener(evt -> {
						buff.setRoll(d, ((SpinnerNumberModel) evt.getSource()).getNumber().intValue());
						updateEffects(buff);
					});
					JSpinner s = new JSpinner(diceModels[i]);
					c.gridy++;
					effectsPanel.add(new JLabel("Roll: "), c);
					c.gridx = 1;
					effectsPanel.add(s, c);
				}
			}
			if (e.requiresCasterLevel()) {
				effectLabels[i] = new JLabel();
				c.gridx = 2;
				if (d == null) c.gridy++;
				effectsPanel.add(effectLabels[i], c);
			}
			i++;
			c.gridy++;
		}
		buffOptionsPanel.add(effectsPanel);

		updateEffects(buff);
		buffOptionsPanel.revalidate();
	}

	private void updateEffects(Buff buff) {
		int i = 0;
		for (EffectorDefinition.Effect e : buff.effects) {
			if (e instanceof EffectorDefinition.ModifierEffect) {
				if (e.requiresCasterLevel()) {
					StringBuilder s = new StringBuilder("Total: ");
					Modifier m = ((EffectorDefinition.ModifierEffect) e).getModifier(buff);
					if (m.getModifier() >= 0) s.append("+");
					s.append(m.getModifier());
					effectLabels[i].setText(s.toString());
				}
				i++;
			}
		}
	}

	public JPanel getOptionsPanel() {
		return buffOptionsPanel;
	}

	public void setCasterLevel(int level) {
		clModel.setValue(level);
	}
}

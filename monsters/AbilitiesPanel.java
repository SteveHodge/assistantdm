package monsters;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gamesystem.AbilityScore;
import gamesystem.Creature;
import gamesystem.core.PropertyListener;


@SuppressWarnings("serial")
class AbilitiesPanel extends DetailPanel {
	private JSpinner spinners[] = new JSpinner[AbilityScore.Type.values().length];
	private JLabel modLabel[] = new JLabel[AbilityScore.Type.values().length];
	private JLabel totalLabel[] = new JLabel[AbilityScore.Type.values().length];
	private JLabel abilityModLabel[] = new JLabel[AbilityScore.Type.values().length];
	private Creature monster;

	AbilitiesPanel() {
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(1, 3, 1, 3);

		add(new JLabel("Ability"), c);
		add(new JLabel("Base"), c);
		add(new JLabel("Mods"), c);
		add(new JLabel("Total"), c);
		add(new JLabel("(Mod)"), c);

		for (int i = 0; i < AbilityScore.Type.values().length; i++) {
			c.gridy = i + 1;
			AbilityScore.Type t = AbilityScore.Type.values()[i];
			JLabel label = new JLabel(t.toString());
			add(label, c);
			SpinnerNumberModel m = new SpinnerNumberModel(0, 0, 100, 1);
			spinners[i] = new JSpinner(m);
			spinners[i].addChangeListener(spinnerListener);
			add(spinners[i], c);
			modLabel[i] = new JLabel();
			add(modLabel[i], c);
			totalLabel[i] = new JLabel();
			add(totalLabel[i], c);
			abilityModLabel[i] = new JLabel();
			add(abilityModLabel[i], c);
		}
	}

	@Override
	void setMonster(Monster m) {
		if (monster == m) return;

		if (monster != null) {
			for (AbilityScore.Type t : AbilityScore.Type.values()) {
				AbilityScore s = monster.getAbilityStatistic(t);
				if (s != null) {
					s.removePropertyListener(listener);
				}
			}
		}

		monster = m;

		if (monster != null) {
			for (AbilityScore.Type t : AbilityScore.Type.values()) {
				AbilityScore s = monster.getAbilityStatistic(t);
				if (s != null) {
					s.addPropertyListener(listener);
				}
			}
		}

		update();
	}

	private void update() {
		if (monster != null) {
			for (int i = 0; i < AbilityScore.Type.values().length; i++) {
				AbilityScore s = monster.getAbilityStatistic(AbilityScore.Type.values()[i]);
				if (s != null) {
					spinners[i].setEnabled(true);
					spinners[i].setValue(s.getBaseValue());

					int modifiers = s.getModifiersTotal();
					String t = "";
					if (modifiers != 0) {
						if (modifiers > 0) t = "+";
						t += Integer.toString(modifiers);
					}
					modLabel[i].setText(t);

					t = "= ";
					t += Integer.toString(s.getValue());
					totalLabel[i].setText(t);

					t = Integer.toString(s.getModifierValue());
					if (s.getModifierValue() >= 0) t = "+" + t;
					abilityModLabel[i].setText("(" + t + ")");
				} else {
					spinners[i].setEnabled(false);
					spinners[i].setValue(0);
					modLabel[i].setText("");
					totalLabel[i].setText("-");
					abilityModLabel[i].setText("(0)");
				}
			}
		}
	}

	final private PropertyListener<Integer> listener = (source, old) -> update();

	final private ChangeListener spinnerListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			// figure out which stat has changed
			for (int i = 0; i < spinners.length; i++) {
				if (spinners[i] == e.getSource()) {
					AbilityScore s = monster.getAbilityStatistic(AbilityScore.Type.values()[i]);
					if (s != null) {
						// should never be null
						int newVal = (Integer) spinners[i].getValue();
						if (s.getBaseValue() != newVal) s.setBaseValue(newVal);
					}
					return;
				}
			}
		}
	};
}
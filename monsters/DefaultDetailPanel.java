package monsters;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import gamesystem.SavingThrow;
import monsters.EncounterDialog.MonsterData;
import monsters.Monster.MonsterAttackForm;
import monsters.Monster.MonsterAttackRoutine;
import monsters.StatisticsBlock.Field;

@SuppressWarnings("serial")
public class DefaultDetailPanel extends DetailPanel {
	private Monster monster;	// TODO remove eventually - should be able to just use the view
	private MonsterData monsterData;
	private StatsBlockCreatureView view;
	private Field field;

	private JTextArea textArea;
	private JLabel statLabel;
	private Color defaultBG;

	DefaultDetailPanel(Field f) {
		this(f, false);
	}

	DefaultDetailPanel(Field f, boolean readOnly) {
		field = f;
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		textArea = new JTextArea(5, 40);
		defaultBG = textArea.getBackground();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);

		JButton apply = new JButton("Apply");
		apply.addActionListener(evt -> {
			try {
				view.setField(field, textArea.getText());
				textArea.setBackground(defaultBG);
			} catch (Exception e) {
				textArea.setBackground(Color.RED);
			}
		});

		statLabel = new JLabel("");

		c.insets = new Insets(2, 4, 2, 4);
		c.gridy = -1;	// will be incremented by the first field
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHWEST;

		if (hasCalculation()) {
			c.gridx = 0;
			c.gridy++;
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0;
			add(new JLabel("Calculated:"), c);

			c.gridx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(statLabel, c);
		}

		if (!readOnly) {
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			add(new JLabel("Edit: "), c);

			c.gridy++;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
			add(scrollPane, c);

			c.gridy++;
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0;
			c.weighty = 0;
			c.anchor = GridBagConstraints.NORTHEAST;
			add(apply, c);
		} else {
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
			add(new JPanel(), c);
		}
	}

	@Override
	void setMonster(Monster m, MonsterData d) {
		if (monster == m) return;

		if (monster != null) {
			view.removePropertyChangeListener(listener);
		}

		monster = m;
		monsterData = d;

		if (monster != null) {
			view = StatsBlockCreatureView.getView(monster);
			view.addPropertyChangeListener(listener);
			// add listener
		}

		update();
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(field.name())) update();
		}
	};

	private void update() {
		if (monster != null) {
			String val = view.getField(field);
			if (val.startsWith("<b>")) val = val.substring(3, val.length() - 4);	// TODO remove when no longer required
			textArea.setText(val);

			String summary = getCalculationSummary();
			if (summary != null)
				statLabel.setText("<html>" + summary + "</html>");
		} else {
			textArea.setText("");
		}
	}

	private boolean hasCalculation() {
		return field == Field.INITIATIVE
				|| field == Field.AC
				|| field == Field.SAVES
				|| field == Field.BASE_ATTACK_GRAPPLE
				|| field == Field.SIZE_TYPE
				|| field == Field.ATTACK
				|| field == Field.FULL_ATTACK;
	}

	private String getCalculationSummary() {
		if (field == Field.INITIATIVE) {
			return monster.getInitiativeStatistic().getSummary();
		} else if (field == Field.AC) {
			return monster.getACStatistic().getSummary();
//		} else if (field == Field.ABILITIES) {
//			for (AbilityScore.Type t : AbilityScore.Type.values()) {
//				AbilityScore score = creature.getAbilityStatistic(t);
//			}
		} else if (field == Field.SAVES) {
			StringBuilder b = new StringBuilder();
			for (SavingThrow.Type t : SavingThrow.Type.values()) {
				SavingThrow save = monster.getSavingThrowStatistic(t);
				b.append(t.toString()).append(": ");
				if (save != null) {
					b.append(save.getSummary());
				} else {
					b.append("-");
				}
				b.append("<br/>");
			}
			return b.toString();
//		} else if (field == Field.HITDICE) {
//			return creature.getHPStatistic().getSummary();
//			creature.getHPStatistic().getMaxHPStat()
		} else if (field == Field.SIZE_TYPE) {
			return monster.getSizeStatistic().getSummary();
		} else if (field == Field.ATTACK) {
			StringBuilder b = new StringBuilder();
			for (MonsterAttackRoutine r : monster.attackList) {
				for (MonsterAttackForm f : r.attackForms) {
					b.append(f.description).append(": ");
					if (f.attack != null)
						b.append(f.attack.getSummary());
					else
						b.append("ERROR getting attack");
					b.append("<br/>");
				}
			}
			return b.toString();
		} else if (field == Field.FULL_ATTACK) {
			// TODO should filter out repeated attack forms
			StringBuilder b = new StringBuilder();
			for (MonsterAttackRoutine r : monster.fullAttackList) {
				for (MonsterAttackForm f : r.attackForms) {
					b.append(f.description).append(": ");
					if (f.attack != null)
						b.append(f.attack.getSummary());
					else
						b.append("ERROR getting attack");
					b.append("<br/>");
				}
			}
			return b.toString();
		} else if (field == Field.BASE_ATTACK_GRAPPLE) {
			StringBuilder b = new StringBuilder();
			b.append("BAB: ").append(monster.getBAB().toString()).append("<br/>");
			b.append("Grapple: ").append(monster.getGrappleModifier().getSummary());
			return b.toString();
		}
		return null;
	}
}

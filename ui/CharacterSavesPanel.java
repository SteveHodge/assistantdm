package ui;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import gamesystem.AbilityScore;
import gamesystem.Creature;
import gamesystem.SavingThrow;
import gamesystem.core.PropertyListener;
import gamesystem.core.Property;
import party.Character;
import swing.NullableIntegerFieldFactory;

//TODO update to use the saving throw's ability modifier rather than looking at the ability score's modifier directly
//TODO change to listen to the SavingThrow itself instead of the character
//TODO cleanup stuff surround change to enum for save types
@SuppressWarnings("serial")
class CharacterSavesPanel extends CharacterSubPanel {
	private SavingThrow[] stats = new SavingThrow[3];
	private JLabel[] modLabels = new JLabel[3];
	private JLabel[] totalLabels = new JLabel[3];
	private JLabel[] baseLabels = new JLabel[3];
	private JFormattedTextField overrideFields[] = new JFormattedTextField[3];
	private JFormattedTextField miscSaveFields[] = new JFormattedTextField[3];

	CharacterSavesPanel(Character c) {
		super(c);
		for (int i = 0; i < 3; i++) {
			stats[i] = (SavingThrow) character.getStatistic(Creature.STATISTIC_SAVING_THROW[i]);
		}
		summary = getSummary();

		JPanel inner = new JPanel();
		GroupLayout layout = new GroupLayout(inner);
		inner.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel[] titleLabels = new JLabel[6];
		titleLabels[0] = new JLabel();
		titleLabels[1] = new JLabel("Base");
		titleLabels[2] = new JLabel("<html><body>Base<br>Override</body></html>");
		titleLabels[3] = new JLabel("Misc");
		titleLabels[4] = new JLabel("Mod");
		titleLabels[5] = new JLabel("Total");
		JLabel[] saveLabels = new JLabel[3];

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(titleLabels[0])
				.addComponent(titleLabels[1])
				.addComponent(titleLabels[2])
				.addComponent(titleLabels[3])
				.addComponent(titleLabels[4])
				.addComponent(titleLabels[5])
				);
		GroupLayout.ParallelGroup[] hGroups = new GroupLayout.ParallelGroup[6];
		for (int i = 0; i < 6; i++) {
			hGroups[i] = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			hGroups[i].addComponent(titleLabels[i]);
		}
		for (int type=0; type<3; type++) {
			saveLabels[type] = new JLabel(stats[type].getDescription());
			baseLabels[type] = new JLabel(Integer.toString(stats[type].getCalculatedBase()));
			overrideFields[type] = NullableIntegerFieldFactory.createNullableIntegerField();
			if (stats[type].getBaseOverride() != -1) {
				overrideFields[type].setValue(stats[type].getBaseOverride());
			} else {
				overrideFields[type].setText("");
			}
			overrideFields[type].setColumns(3);
			overrideFields[type].addPropertyChangeListener("value", new OverrideFieldPropertyListener(type));
			miscSaveFields[type] = new JFormattedTextField();
			miscSaveFields[type].setValue(character.getSavingThrowMisc(SavingThrow.Type.values()[type]));
			miscSaveFields[type].setColumns(3);
			miscSaveFields[type].addPropertyChangeListener("value", new MiscFieldPropertyListener(type));
			modLabels[type] = new JLabel("" + character.getAbilityStatistic(SavingThrow.Type.values()[type].getAbilityType()).getModifierValue());
			totalLabels[type] = new JLabel(""+stats[type].getValue()+(stats[type].hasConditionalModifier()?"*":""));
			vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(saveLabels[type])
					.addComponent(baseLabels[type])
					.addComponent(overrideFields[type])
					.addComponent(miscSaveFields[type])
					.addComponent(modLabels[type])
					.addComponent(totalLabels[type])
					);
			hGroups[0].addComponent(saveLabels[type]);
			hGroups[1].addComponent(baseLabels[type]);
			hGroups[2].addComponent(overrideFields[type], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
			hGroups[3].addComponent(miscSaveFields[type], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
			hGroups[4].addComponent(modLabels[type]);
			hGroups[5].addComponent(totalLabels[type]);
		}

		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		for (int i = 0; i < 6; i++) {
			hGroup.addGroup(hGroups[i]);
		}
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!SwingUtilities.isRightMouseButton(e)) return;
				int y = e.getPoint().y;
				for (int i = 0; i < 3; i++) {
					Rectangle bounds = overrideFields[i].getBounds();
					if (y >= bounds.y && y <= bounds.y + bounds.height) {
						String title = stats[i].getDescription();
						String statName = Creature.STATISTIC_SAVING_THROW[i];
						StatisticInfoDialog dialog = new StatisticInfoDialog(CharacterSavesPanel.this, title, character, statName);
						dialog.setVisible(true);
						break;
					}
				}
			}
		});

		character.addPropertyListener("ability_scores", new PropertyListener<Integer>() {
			@Override
			public void propertyChanged(Property<Integer> source, Integer oldValue) {
				if (source instanceof AbilityScore) {
					AbilityScore.Type type = ((AbilityScore) source).getType();
					for (int i = 0; i < 3; i++) {
						if (type.equals(SavingThrow.Type.values()[i].getAbilityType())) {
							//System.out.println("Ability "+prop+" modified for save "+SavingThrow.Type.values()[i].getAbilityType().toString());
							modLabels[i].setText("" + character.getAbilityStatistic(SavingThrow.Type.values()[i].getAbilityType()).getModifierValue());
						}
					}
				}
			}
		});

		character.addPropertyListener("saving_throws", new PropertyListener<Integer>() {
			@Override
			public void propertyChanged(Property<Integer> source, Integer oldValue) {
				for (int i = 0; i < 3; i++) {
					if (stats[i] == source) {
						baseLabels[i].setText(Integer.toString(stats[i].getCalculatedBase()));
						if (stats[i].getBaseOverride() == -1) {
							overrideFields[i].setText("");
						} else {
							overrideFields[i].setValue(stats[i].getBaseOverride());
						}
						totalLabels[i].setText(""+stats[i].getValue()+(stats[i].hasConditionalModifier()?"*":""));
					}
				}
				updateToolTips();
				updateSummaries(getSummary());
			}
		});

		updateToolTips();

		add(inner);
	}

	private void updateToolTips() {
		for (int i = 0; i < totalLabels.length; i++) {
			StringBuilder text = new StringBuilder();
			text.append("<html><body>");
			SavingThrow stat = (SavingThrow)character.getStatistic(Creature.STATISTIC_SAVING_THROW[i]);
			text.append(stat.getSummary());
			text.append("</body></html>");
			totalLabels[i].setToolTipText(text.toString());
		}
	}

	private String getSummary() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			if (i > 0) s.append("   ");
			SavingThrow.Type type = SavingThrow.Type.values()[i];
			s.append(type).append(" ");
			if (character.getSavingThrowStatistic(type).getValue() >= 0) s.append("+");
			s.append(character.getSavingThrowStatistic(type).getValue());
		}
		return s.toString();
	}


	private class OverrideFieldPropertyListener implements PropertyChangeListener {
		int type;

		public OverrideFieldPropertyListener(int t) {
			type = t;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("value")) {
				if (overrideFields[type].getValue() == null || "".equals(overrideFields[type].getText())) {
					stats[type].clearBaseOverride();
				} else {
					int total = (Integer) overrideFields[type].getValue();
					if (total == stats[type].getCalculatedBase()) {
						stats[type].clearBaseOverride();
						overrideFields[type].setText("");
					} else {
						stats[type].setBaseOverride(total);
					}
				}
			}
		}
	}

	private class MiscFieldPropertyListener implements PropertyChangeListener {
		int type;

		public MiscFieldPropertyListener(int t) {
			type = t;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("value")) {
				int total = (Integer)miscSaveFields[type].getValue();
				character.setSavingThrowMisc(SavingThrow.Type.values()[type], total);
			}
		}
	}
}

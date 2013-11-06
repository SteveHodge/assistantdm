package ui;

import gamesystem.SavingThrow;

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

import party.Character;
import party.Creature;

//TODO update to use the saving throw's ability modifier rather than looking at the ability score's modifier directly
//TODO change to listen to the SavingThrow itself instead of the character
//TODO cleanup stuff surround change to enum for save types
//TODO review for change to enum SavingThrow.Type
@SuppressWarnings("serial")
class CharacterSavesPanel extends CharacterSubPanel implements PropertyChangeListener {
	private JLabel[] modLabels = new JLabel[3];
	private JLabel[] totalLabels = new JLabel[3];
	private JFormattedTextField baseSaveFields[] = new JFormattedTextField[3];
	private JFormattedTextField miscSaveFields[] = new JFormattedTextField[3];

	CharacterSavesPanel(Character c) {
		super(c);
		summary = getSummary();

		JPanel inner = new JPanel();
		GroupLayout layout = new GroupLayout(inner);
		inner.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel[] titleLabels = new JLabel[5];
		titleLabels[0] = new JLabel();
		titleLabels[1] = new JLabel("Base");
		titleLabels[2] = new JLabel("Misc");
		titleLabels[3] = new JLabel("Mod");
		titleLabels[4] = new JLabel("Total");
		JLabel[] saveLabels = new JLabel[3];

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(titleLabels[0])
				.addComponent(titleLabels[1])
				.addComponent(titleLabels[2])
				.addComponent(titleLabels[3])
				.addComponent(titleLabels[4])
				);
		GroupLayout.ParallelGroup[] hGroups = new GroupLayout.ParallelGroup[5];
		for (int i=0; i<5; i++) {
			hGroups[i] = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			hGroups[i].addComponent(titleLabels[i]);
		}
		for (int type=0; type<3; type++) {
			saveLabels[type] = new JLabel(SavingThrow.Type.values()[type].toString());
			baseSaveFields[type] = new JFormattedTextField();
			baseSaveFields[type].setValue(new Integer(character.getSavingThrowBase(SavingThrow.Type.values()[type])));
			baseSaveFields[type].setColumns(3);
			baseSaveFields[type].addPropertyChangeListener("value", new BaseFieldPropertyListener(type));
			miscSaveFields[type] = new JFormattedTextField();
			miscSaveFields[type].setValue(new Integer(character.getSavingThrowMisc(SavingThrow.Type.values()[type])));
			miscSaveFields[type].setColumns(3);
			miscSaveFields[type].addPropertyChangeListener("value", new MiscFieldPropertyListener(type));
			modLabels[type] = new JLabel(""+character.getAbilityModifierValue(SavingThrow.Type.values()[type].getAbilityType()));
			SavingThrow stat = (SavingThrow)character.getStatistic(Creature.STATISTIC_SAVING_THROW[type]);
			totalLabels[type] = new JLabel(""+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
			vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(saveLabels[type])
					.addComponent(baseSaveFields[type])
					.addComponent(miscSaveFields[type])
					.addComponent(modLabels[type])
					.addComponent(totalLabels[type])
					);
			hGroups[0].addComponent(saveLabels[type]);
			hGroups[1].addComponent(baseSaveFields[type], GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
			hGroups[2].addComponent(miscSaveFields[type], GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
			hGroups[3].addComponent(modLabels[type]);
			hGroups[4].addComponent(totalLabels[type]);
		}

		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		for (int i=0; i<5; i++) hGroup.addGroup(hGroups[i]);
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!SwingUtilities.isRightMouseButton(e)) return;
				int y = e.getPoint().y;
				for (int i = 0; i < 3; i++) {
					Rectangle bounds = baseSaveFields[i].getBounds();
					if (y >= bounds.y && y <= bounds.y + bounds.height) {
						String title = SavingThrow.Type.values()[i].toString();
						String statName = Creature.STATISTIC_SAVING_THROW[i];
						StatisticInfoDialog dialog = new StatisticInfoDialog(CharacterSavesPanel.this, title, character, statName);
						dialog.setVisible(true);
						break;
					}
				}
			}
		});

		character.addPropertyChangeListener(this);
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
			s.append(SavingThrow.Type.values()[i]).append(" ");
			if (character.getSavingThrow(SavingThrow.Type.values()[i]) >= 0) s.append("+");
			s.append(character.getSavingThrow(SavingThrow.Type.values()[i]));
		}
		return s.toString();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (prop.startsWith(Creature.PROPERTY_ABILITY_PREFIX)) {
			prop = prop.substring(Creature.PROPERTY_ABILITY_PREFIX.length());
			for (int i = 0; i < 3; i++) {
				if (prop.equals(SavingThrow.Type.values()[i].getAbilityType().toString())) {
					//System.out.println("Ability "+prop+" modified for save "+SavingThrow.Type.values()[i].getAbilityType().toString());
					modLabels[i].setText(""+character.getAbilityModifierValue(SavingThrow.Type.values()[i].getAbilityType()));
				}
			}
		} else if (prop.startsWith(Creature.PROPERTY_SAVE_PREFIX)) {
			prop = prop.substring(Creature.PROPERTY_SAVE_PREFIX.length());
			for (int i = 0; i < 3; i++) {
				if (prop.equals(SavingThrow.Type.values()[i].toString())) {
					baseSaveFields[i].setValue(new Integer(character.getSavingThrowBase(SavingThrow.Type.values()[i])));
					SavingThrow stat = (SavingThrow)character.getStatistic(Creature.STATISTIC_SAVING_THROW[i]);
					totalLabels[i].setText(""+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
				}
			}
			updateToolTips();
			updateSummaries(getSummary());
		}
	}

	private class BaseFieldPropertyListener implements PropertyChangeListener {
		int type;

		public BaseFieldPropertyListener(int t) {
			type = t;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("value")) {
				int total = (Integer)baseSaveFields[type].getValue();
				character.setSavingThrowBase(SavingThrow.Type.values()[type], total);
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

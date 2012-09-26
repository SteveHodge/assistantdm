package ui;

import gamesystem.Modifier;
import gamesystem.SavingThrow;
import gamesystem.Statistic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import party.Creature;
import party.Character;

//TODO update to use the saving throw's ability modifier rather than looking at the ability score's modifier directly
//TODO change to listen to the SavingThrow itself instead of the character 
@SuppressWarnings("serial")
public class CharacterSavesPanel extends JPanel implements PropertyChangeListener {
	protected Character character;
	protected JLabel[] modLabels = new JLabel[3];
	protected JLabel[] totalLabels = new JLabel[3];
	protected JFormattedTextField baseSaveFields[] = new JFormattedTextField[3];
	protected JFormattedTextField miscSaveFields[] = new JFormattedTextField[3];

	public CharacterSavesPanel(Character c) {
		character = c;

		setBorder(new TitledBorder("Saving Throws"));

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
			saveLabels[type] = new JLabel(SavingThrow.getSavingThrowName(type));
			baseSaveFields[type] = new JFormattedTextField();
			baseSaveFields[type].setValue(new Integer(character.getSavingThrowBase(type)));
			baseSaveFields[type].setColumns(3);
			baseSaveFields[type].addPropertyChangeListener("value", new BaseFieldPropertyListener(type));
			miscSaveFields[type] = new JFormattedTextField();
			miscSaveFields[type].setValue(new Integer(character.getSavingThrowMisc(type)));
			miscSaveFields[type].setColumns(3);
			miscSaveFields[type].addPropertyChangeListener("value", new MiscFieldPropertyListener(type));
			modLabels[type] = new JLabel(""+character.getAbilityModifier(SavingThrow.getSaveAbility(type)));
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

		character.addPropertyChangeListener(this);
		updateToolTips();

		add(inner);
	}

	protected void updateToolTips() {
		for (int i = 0; i < totalLabels.length; i++) {
			StringBuilder text = new StringBuilder();
			text.append("<html><body>");
			SavingThrow stat = (SavingThrow)character.getStatistic(Creature.STATISTIC_SAVING_THROW[i]);
			text.append(stat.getBaseValue()).append(" base<br/>");
			Map<Modifier, Boolean> mods = stat.getModifiers();
			text.append(Statistic.getModifiersHTML(mods));
			text.append(stat.getValue()).append(" total");
			String conds = Statistic.getModifiersHTML(mods, true);
			if (conds.length() > 0) text.append("<br/><br/>").append(conds);			
			text.append("</body></html>");
			totalLabels[i].setToolTipText(text.toString());
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
//		if (prop.startsWith(Creature.PROPERTY_ABILITY_PREFIX)) {
//			prop = prop.substring(Creature.PROPERTY_ABILITY_PREFIX.length());
//			for (int i = 0; i < 3; i++) {
//				if (prop.equals(AbilityScore.getAbilityName(SavingThrow.getSaveAbility(i)))) {
//					//System.out.println("Ability "+prop+" modified for save "+Creature.getSavingThrowName(i));
//					modLabels[i].setText(""+character.getAbilityModifier(SavingThrow.getSaveAbility(i)));
//					totalLabels[i].setText(""+character.getSavingThrow(i));
//				}
//			}
//		} else 
		if (prop.startsWith(Creature.PROPERTY_SAVE_PREFIX)) {
			prop = prop.substring(Creature.PROPERTY_SAVE_PREFIX.length());
			for (int i = 0; i < 3; i++) {
				if (prop.equals(SavingThrow.getSavingThrowName(i))) {
					baseSaveFields[i].setValue(new Integer(character.getSavingThrowBase(i)));
					SavingThrow stat = (SavingThrow)character.getStatistic(Creature.STATISTIC_SAVING_THROW[i]);
					totalLabels[i].setText(""+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
				}
			}
			updateToolTips();
		}
	}

	protected class BaseFieldPropertyListener implements PropertyChangeListener {
		int type;

		public BaseFieldPropertyListener(int t) {
			type = t;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("value")) {
				int total = (Integer)baseSaveFields[type].getValue();
				character.setSavingThrowBase(type, total);
			}
		}
	}

	protected class MiscFieldPropertyListener implements PropertyChangeListener {
		int type;

		public MiscFieldPropertyListener(int t) {
			type = t;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("value")) {
				int total = (Integer)miscSaveFields[type].getValue();
				character.setSavingThrowMisc(type, total);
			}
		}
	}
}

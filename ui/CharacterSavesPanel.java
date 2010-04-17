package ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import party.Creature;
import party.Character;

@SuppressWarnings("serial")
public class CharacterSavesPanel extends JPanel implements PropertyChangeListener {
	protected Character character;
	protected JLabel[] modLabels = new JLabel[4];
	protected JLabel[] totalLabels = new JLabel[4];
	protected JFormattedTextField baseSaveFields[] = new JFormattedTextField[3];

	public CharacterSavesPanel(Character c) {
		character = c;

		setBorder(new TitledBorder("Saving Throws"));

		JPanel inner = new JPanel(); 
		GroupLayout layout = new GroupLayout(inner);
		inner.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel[] titleLabels = new JLabel[4];
		titleLabels[0] = new JLabel();
		titleLabels[1] = new JLabel("Base");
		titleLabels[2] = new JLabel("Mod");
		titleLabels[3] = new JLabel("Total");
		JLabel[] saveLabels = new JLabel[4];

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(titleLabels[0])
				.addComponent(titleLabels[1])
				.addComponent(titleLabels[2])
				.addComponent(titleLabels[3])
			);
		GroupLayout.ParallelGroup[] hGroups = new GroupLayout.ParallelGroup[4];
		for (int i=0; i<4; i++) {
			hGroups[i] = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			hGroups[i].addComponent(titleLabels[i]);
		}
		for (int type=0; type<3; type++) {
			saveLabels[type] = new JLabel(Creature.getSavingThrowName(type));
			baseSaveFields[type] = new JFormattedTextField();
			baseSaveFields[type].setValue(new Integer(character.getSavingThrowBase(type)));
			baseSaveFields[type].setColumns(3);
			baseSaveFields[type].addPropertyChangeListener("value", new BaseFieldPropertyListener(type));
			modLabels[type] = new JLabel(""+character.getAbilityModifier(Creature.getSaveAbility(type)));
			totalLabels[type] = new JLabel(""+character.getSavingThrow(type));
			vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(saveLabels[type])
					.addComponent(baseSaveFields[type])
					.addComponent(modLabels[type])
					.addComponent(totalLabels[type])
				);
			hGroups[0].addComponent(saveLabels[type]);
			hGroups[1].addComponent(baseSaveFields[type], GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
			hGroups[2].addComponent(modLabels[type]);
			hGroups[3].addComponent(totalLabels[type]);
		}

		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		for (int i=0; i<4; i++) hGroup.addGroup(hGroups[i]);
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		character.addPropertyChangeListener(this);

		add(inner);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (prop.startsWith(Creature.PROPERTY_ABILITY_PREFIX)) {
			prop = prop.substring(Creature.PROPERTY_ABILITY_PREFIX.length());
			for (int i = 0; i < 3; i++) {
				if (prop.equals(Creature.getAbilityName(Creature.getSaveAbility(i)))) {
					//System.out.println("Ability "+prop+" modified for save "+Creature.getSavingThrowName(i));
					modLabels[i].setText(""+character.getAbilityModifier(Creature.getSaveAbility(i)));
					totalLabels[i].setText(""+character.getSavingThrow(i));
				}
			}
		} else if (prop.startsWith(Creature.PROPERTY_SAVE_PREFIX)) {
			prop = prop.substring(Creature.PROPERTY_SAVE_PREFIX.length());
			for (int i = 0; i < 3; i++) {
				if (prop.equals(Creature.getSavingThrowName(i))) {
					baseSaveFields[i].setValue(new Integer(character.getSavingThrowBase(i)));
					totalLabels[i].setText(""+character.getSavingThrow(i));
				}
			}
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
}

package ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import party.Creature;
import party.Character;

@SuppressWarnings("serial")
public class CharacterInitiativePanel extends JPanel implements PropertyChangeListener {
	protected Character character;
	protected JLabel dexLabel;
	protected BoundIntegerField baseInit;
	protected JLabel totLabel;

	public CharacterInitiativePanel(Character c) {
		character = c;

		setBorder(new TitledBorder("Initiative"));

		dexLabel = new JLabel("Dex Mod: "+character.getAbilityModifier(Creature.ABILITY_DEXTERITY));
		add(dexLabel);

		add(new JLabel("Base:"));

		baseInit = new BoundIntegerField(character, Creature.PROPERTY_INITIATIVE, 3);
		add(baseInit);

		totLabel = new JLabel("Total: "+character.getInitiativeModifier());
		add(totLabel);

		// update labels when character changes
		character.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+Creature.getAbilityName(Creature.ABILITY_DEXTERITY))) {
			dexLabel.setText("Dex Mod: "+character.getAbilityModifier(Creature.ABILITY_DEXTERITY));
		} else if (arg0.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
			totLabel.setText("Total: "+character.getInitiativeModifier());
		}
	}
}

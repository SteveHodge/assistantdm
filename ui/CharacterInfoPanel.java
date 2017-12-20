package ui;

import java.awt.GridLayout;

import javax.swing.JLabel;

import gamesystem.Size;
import party.Character;

// TODO space should be double control showing value in feet (instead of integer showing value in 6-inch units)

@SuppressWarnings("serial")
public class CharacterInfoPanel extends CharacterSubPanel {
	public CharacterInfoPanel(Character c) {
		super(c);

		setLayout(new GridLayout(0,4));

		add(new JLabel("Player: "));
		add(new BoundTextField(character, Character.PROPERTY_PLAYER, 30));

		add(new JLabel("Campaign: "));
		add(new BoundTextField(character, Character.PROPERTY_CAMPAIGN, 30));

		add(new JLabel("Region: "));
		add(new BoundTextField(character, Character.PROPERTY_REGION, 30));

		add(new JLabel("Race: "));
		add(new BoundTextField(character, Character.PROPERTY_RACE, 30));

		add(new JLabel("Gender: "));
		add(new BoundTextField(character, Character.PROPERTY_GENDER, 30));

		add(new JLabel("Alignment: "));
		add(new BoundTextField(character, Character.PROPERTY_ALIGNMENT, 30));

		add(new JLabel("Deity: "));
		add(new BoundTextField(character, Character.PROPERTY_DEITY, 30));

		add(new JLabel("Space: "));
		Size size = character.getSizeStatistic();
		add(new BoundIntegerField(size.getSpace(), 30), c);

		add(new JLabel("Reach: "));
		// FIXME replace with BoundIntegerField once space is a property
		add(new BoundIntegerField(size.getReach(), 30), c);

		add(new JLabel("Type: "));
		add(new BoundTextField(character, Character.PROPERTY_TYPE, 30));

		add(new JLabel("Age: "));
		add(new BoundTextField(character, Character.PROPERTY_AGE, 30));

		add(new JLabel("Height: "));
		add(new BoundTextField(character, Character.PROPERTY_HEIGHT, 30));

		add(new JLabel("Weight: "));
		add(new BoundTextField(character, Character.PROPERTY_WEIGHT, 30));

		add(new JLabel("Eye Colour: "));
		add(new BoundTextField(character, Character.PROPERTY_EYE_COLOUR, 30));

		add(new JLabel("Hair Colour: "));
		add(new BoundTextField(character, Character.PROPERTY_HAIR_COLOUR, 30));

		add(new JLabel("Speed: "));
		add(new BoundTextField(character, Character.PROPERTY_SPEED, 30));

		add(new JLabel("Damage Reduction: "));
		add(new BoundTextField(character, Character.PROPERTY_DAMAGE_REDUCTION, 30));

		add(new JLabel("Spell Resistance: "));
		add(new BoundTextField(character, Character.PROPERTY_SPELL_RESISTANCE, 30));

		add(new JLabel("Arcane Spell Failure: "));
		add(new BoundTextField(character, Character.PROPERTY_ARCANE_SPELL_FAILURE, 30));

		add(new JLabel("Action Points: "));
		add(new BoundTextField(character, Character.PROPERTY_ACTION_POINTS, 30));
	}
}

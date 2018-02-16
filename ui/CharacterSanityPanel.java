package ui;

import javax.swing.JLabel;
import javax.swing.JTextField;

import gamesystem.Sanity;
import gamesystem.core.ValueProperty;
import party.Character;

@SuppressWarnings("serial")
class CharacterSanityPanel extends CharacterSubPanel {
	private JLabel maxLabel;
	private JTextField currentField;
	private JLabel startingLabel;
	private JTextField knowledgeField;
	private Sanity sanity;

	CharacterSanityPanel(Character c) {
		super(c);
		sanity = c.getSanity();
		summary = sanity.getValue().toString();

		maxLabel = PropertyFields.createIntegerPropertyLabel(sanity.getMaximumSanityProperty(), "Maximum: ");
		add(maxLabel);

		startingLabel = PropertyFields.createIntegerPropertyLabel(sanity.getStartingSanityProperty(), "Starting: ");
		add(startingLabel);

		add(new JLabel("Current:"));

		currentField = PropertyFields.createSettableIntegerField(sanity, 3);
		add(currentField);

		add(new JLabel("Knowledge Skill:"));

		ValueProperty<Integer> knowledge = sanity.getKnowledgeSkillProperty();
		knowledgeField = PropertyFields.createSettableIntegerField(knowledge, 3);
		add(knowledgeField);

		updateToolTip();
	}

	private void updateToolTip() {
		//XXX implement
/*		StringBuilder text = new StringBuilder();
		text.append("<html><body>");
		InitiativeModifier stat = character.getInitiativeStatistic();
		text.append(stat.getSummary());
		text.append("</body></html>");
		startingLabel.setToolTipText(text.toString());*/
	}
}

package ui;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import gamesystem.Sanity;
import gamesystem.core.ValueProperty;
import party.Character;

@SuppressWarnings("serial")
class CharacterSanityPanel extends CharacterSubPanel {
	private JLabel maxLabel;
	private JFormattedTextField currentField;
	private JLabel startingLabel;
	private JFormattedTextField knowledgeField;
	private Sanity sanity;

	CharacterSanityPanel(Character c) {
		super(c);
		sanity = c.getSanity();
		summary = sanity.getValue().toString();

		maxLabel = new JLabel("Maximum: " + sanity.getMaximumSanityProperty().getValue());
		sanity.getMaximumSanityProperty().addPropertyListener((source, oldValue) -> {
			maxLabel.setText("Maximum: " + source.getValue());
		});
		add(maxLabel);

		startingLabel = new JLabel("Starting: " + sanity.getStartingSanityProperty().getValue());
		sanity.getStartingSanityProperty().addPropertyListener((source, oldValue) -> {
			startingLabel.setText("Starting: " + source.getValue());
		});
		add(startingLabel);

		add(new JLabel("Current:"));

		currentField = new JFormattedTextField(sanity.getValue());
		currentField.addPropertyChangeListener("value", evt -> {
			if (evt.getPropertyName().equals("value")) {
				sanity.setBaseValue((Integer) currentField.getValue());
			}
		});
		sanity.addPropertyListener((source, oldValue) -> {
			currentField.setValue(source.getValue());
		});
		currentField.setColumns(3);
		add(currentField);

		add(new JLabel("Knowledge Skill:"));

		ValueProperty<Integer> knowledge = sanity.getKnowledgeSkillProperty();
		knowledgeField = new JFormattedTextField(knowledge.getValue());
		knowledgeField.addPropertyChangeListener("value", evt -> {
			if (evt.getPropertyName().equals("value")) {
				knowledge.setBaseValue((Integer) knowledgeField.getValue());
			}
		});
		knowledge.addPropertyListener((source, oldValue) -> {
			knowledgeField.setValue(source.getValue());
		});
		knowledgeField.setColumns(3);
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

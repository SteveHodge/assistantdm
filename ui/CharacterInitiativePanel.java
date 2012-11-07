package ui;

import gamesystem.AbilityScore;
import gamesystem.InitiativeModifier;
import gamesystem.Modifier;
import gamesystem.Statistic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.JLabel;

import party.Creature;
import party.Character;

@SuppressWarnings("serial")
public class CharacterInitiativePanel extends CharacterSubPanel implements PropertyChangeListener {
	protected JLabel dexLabel;
	protected BoundIntegerField baseInit;
	protected JLabel totLabel;

	public CharacterInitiativePanel(Character c) {
		super(c);
		summary = (character.getInitiativeModifier() >= 0 ? "+" : "") + character.getInitiativeModifier();

		dexLabel = new JLabel("Dex Mod: "+character.getAbilityModifier(AbilityScore.ABILITY_DEXTERITY));
		add(dexLabel);

		add(new JLabel("Base:"));

		baseInit = new BoundIntegerField(character, Creature.PROPERTY_INITIATIVE, 3);
		add(baseInit);

		InitiativeModifier stat = (InitiativeModifier)character.getStatistic(Creature.STATISTIC_INITIATIVE);
		totLabel = new JLabel("Total: "+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
		add(totLabel);

		updateToolTip();

		// update labels when character changes
		character.addPropertyChangeListener(this);
	}

	protected void updateToolTip() {
		StringBuilder text = new StringBuilder();
		text.append("<html><body>");
		InitiativeModifier stat = (InitiativeModifier)character.getStatistic(Creature.STATISTIC_INITIATIVE);
		text.append(stat.getBaseValue()).append(" base<br/>");
		Map<Modifier, Boolean> mods = stat.getModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(stat.getValue()).append(" total");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		totLabel.setToolTipText(text.toString());
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+AbilityScore.getAbilityName(AbilityScore.ABILITY_DEXTERITY))) {
			dexLabel.setText("Dex Mod: "+character.getAbilityModifier(AbilityScore.ABILITY_DEXTERITY));
		} else if (arg0.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
			InitiativeModifier stat = (InitiativeModifier)character.getStatistic(Creature.STATISTIC_INITIATIVE);
			totLabel.setText("Total: "+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
			updateToolTip();
			updateSummaries((character.getInitiativeModifier() >= 0 ? "+" : "") + character.getInitiativeModifier());
		}
	}
}

package ui;

import gamesystem.AbilityScore;
import gamesystem.InitiativeModifier;
import gamesystem.Modifier;
import gamesystem.Statistic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

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

		dexLabel = new JLabel("Dex Mod: "+character.getAbilityModifier(AbilityScore.ABILITY_DEXTERITY));
		add(dexLabel);

		add(new JLabel("Base:"));

		baseInit = new BoundIntegerField(character, Creature.PROPERTY_INITIATIVE, 3);
		add(baseInit);

		totLabel = new JLabel("Total: "+character.getInitiativeModifier());
		add(totLabel);

		updateToolTip();

		// update labels when character changes
		character.addPropertyChangeListener(this);
	}

	protected void updateToolTip() {
		StringBuilder text = new StringBuilder();
		text.append("<html><body>");
		InitiativeModifier stat = (InitiativeModifier)character.getStatistic(Creature.STATISTIC_INITIATIVE);
		text.append("Base = "+stat.getBaseValue()).append("<br/>");
		Map<Modifier, Boolean> mods = stat.getModifiers();
		for (Modifier m : mods.keySet()) {
			if (!mods.get(m)) text.append("<s>");
			text.append(m);
			if (!mods.get(m)) text.append("</s>");
			text.append("<br/>");
		}
		text.append("Total = "+character.getInitiativeModifier());
		text.append("</body></html>");
		totLabel.setToolTipText(text.toString());
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+AbilityScore.getAbilityName(AbilityScore.ABILITY_DEXTERITY))) {
			dexLabel.setText("Dex Mod: "+character.getAbilityModifier(AbilityScore.ABILITY_DEXTERITY));
		} else if (arg0.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
			totLabel.setText("Total: "+character.getInitiativeModifier());
			updateToolTip();
		}
	}
}

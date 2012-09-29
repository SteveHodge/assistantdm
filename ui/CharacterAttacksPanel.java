package ui;

import gamesystem.AbilityScore;
import gamesystem.Attacks;
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
public class CharacterAttacksPanel extends JPanel implements PropertyChangeListener {
	protected Character character;
	protected JLabel strLabel;
	protected BoundIntegerField BAB;
	protected JLabel totLabel;

	public CharacterAttacksPanel(Character c) {
		character = c;

		setBorder(new TitledBorder("Attacks"));

		add(new JLabel("BAB:"));

		BAB = new BoundIntegerField(character, Creature.PROPERTY_BAB, 3);
		add(BAB);

		strLabel = new JLabel("Str Mod: "+character.getAbilityModifier(AbilityScore.ABILITY_STRENGTH));
		add(strLabel);

		Attacks stat = (Attacks)character.getStatistic(Creature.STATISTIC_ATTACKS);
		totLabel = new JLabel("Total: "+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
		add(totLabel);

		updateToolTip();

		// update labels when character changes
		character.addPropertyChangeListener(this);
	}

	protected void updateToolTip() {
		StringBuilder text = new StringBuilder();
		text.append("<html><body>");
		Attacks stat = (Attacks)character.getStatistic(Creature.STATISTIC_ATTACKS);
		text.append(stat.getBAB()).append(" base attack bonus<br/>");
		Map<Modifier, Boolean> mods = stat.getModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(stat.getValue()).append(" total");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		totLabel.setToolTipText(text.toString());
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+AbilityScore.getAbilityName(AbilityScore.ABILITY_STRENGTH))) {
			strLabel.setText("Str Mod: "+character.getAbilityModifier(AbilityScore.ABILITY_STRENGTH));
		} else if (arg0.getPropertyName().equals(Creature.PROPERTY_BAB)) {
			Attacks stat = (Attacks)character.getStatistic(Creature.STATISTIC_ATTACKS);
			totLabel.setText("Total: "+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
			updateToolTip();
		}
	}

}

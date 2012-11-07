package ui;

import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Modifier;
import gamesystem.Statistic;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.JLabel;

import party.Creature;
import party.Character;

@SuppressWarnings("serial")
public class CharacterAttacksPanel extends CharacterSubPanel implements PropertyChangeListener {
	protected BoundIntegerField BAB;
	protected JLabel strLabel = new JLabel();
	protected JLabel dexLabel = new JLabel();
	protected JLabel meleeLabel = new JLabel();
	protected JLabel rangedLabel = new JLabel();

	public CharacterAttacksPanel(Character chr) {
		super(chr);
		summary = getSummary();

		setLayout(new GridBagLayout());

		BAB = new BoundIntegerField(character, Creature.PROPERTY_BAB, 3);
		updateLabels();

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = 0; c.gridy = 0;
		c.gridheight = 2;
		add(new JLabel("BAB:"),c);

		c.gridx = 1;
		add(BAB,c);

		c.gridx = 2; c.gridheight = 1; c.anchor = GridBagConstraints.LINE_END;
		add(new JLabel("Str Mod: "),c);

		c.gridx = 3;
		add(strLabel,c);

		c.gridx = 4;
		add(new JLabel("Melee Attack: "),c);

		c.gridx = 5;
		add(meleeLabel,c);

		c.gridx = 2; c.gridy = 1;
		add(new JLabel("Dex Mod: "),c);

		c.gridx = 3;
		add(dexLabel,c);

		c.gridx = 4;
		add(new JLabel("Ranged Attack: "),c);

		c.gridx = 5;
		add(rangedLabel,c);

		updateToolTip();

		// update labels when character changes
		character.addPropertyChangeListener(this);
	}

	protected void updateLabels() {
		Attacks stat = (Attacks)character.getStatistic(Creature.STATISTIC_ATTACKS);
		strLabel.setText(""+character.getAbilityModifier(AbilityScore.ABILITY_STRENGTH));
		meleeLabel.setText(stat.getValue()+(stat.hasConditionalModifier()?"*":""));
		dexLabel.setText(""+character.getAbilityModifier(AbilityScore.ABILITY_DEXTERITY));
		rangedLabel.setText(stat.getRangedValue()+(stat.hasConditionalModifier()?"*":""));
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
		meleeLabel.setToolTipText(text.toString());
	}

	protected String getSummary() {
		StringBuilder s = new StringBuilder();
		s.append("Melee ");
		Attacks stat = (Attacks)character.getStatistic(Creature.STATISTIC_ATTACKS);
		int v = stat.getValue();
		if (v >= 0) s.append("+");
		s.append(v);
		int b = stat.getBAB() - 5;
		while (b >= 1) {
			s.append("/");
			v -= 5;
			if (v >= 0) s.append("+");
			s.append(v);
			b -= 5;
		}

		s.append("   Ranged ");
		v = stat.getRangedValue();
		if (v >= 0) s.append("+");
		s.append(v);
		b = stat.getBAB() - 5;
		while (b >= 1) {
			s.append("/");
			v -= 5;
			if (v >= 0) s.append("+");
			s.append(v);
			b -= 5;
		}

		return s.toString();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+AbilityScore.getAbilityName(AbilityScore.ABILITY_STRENGTH))
				|| e.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+AbilityScore.getAbilityName(AbilityScore.ABILITY_DEXTERITY))
				|| e.getPropertyName().equals(Creature.PROPERTY_BAB)
				) {
			updateLabels();
			updateToolTip();
			updateSummaries(getSummary());
		}
	}

}

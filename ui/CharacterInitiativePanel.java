package ui;

import gamesystem.AbilityScore;
import gamesystem.InitiativeModifier;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import party.Character;
import party.Creature;

@SuppressWarnings("serial")
class CharacterInitiativePanel extends CharacterSubPanel implements PropertyChangeListener {
	private JLabel dexLabel;
	private BoundIntegerField baseInit;
	private JLabel totLabel;

	CharacterInitiativePanel(Character c) {
		super(c);
		summary = (character.getInitiativeModifier() >= 0 ? "+" : "") + character.getInitiativeModifier();

		dexLabel = new JLabel("Dex Mod: "+character.getAbilityModifierValue(AbilityScore.Type.DEXTERITY));
		add(dexLabel);

		add(new JLabel("Base:"));

		baseInit = new BoundIntegerField(character, Creature.PROPERTY_INITIATIVE, 3);
		add(baseInit);

		InitiativeModifier stat = (InitiativeModifier) character.getStatistic(Creature.STATISTIC_INITIATIVE);
		totLabel = new JLabel("Total: "+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
		add(totLabel);

		addMouseListener(rightClickListener);
		totLabel.addMouseListener(rightClickListener);

		updateToolTip();

		// update labels when character changes
		character.addPropertyChangeListener(this);
	}

	private MouseListener rightClickListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (!SwingUtilities.isRightMouseButton(e)) return;
			StatisticInfoDialog dialog = new StatisticInfoDialog(CharacterInitiativePanel.this, "Initiative", character, Creature.STATISTIC_INITIATIVE);
			dialog.setVisible(true);
		}
	};

	private void updateToolTip() {
		StringBuilder text = new StringBuilder();
		text.append("<html><body>");
		InitiativeModifier stat = (InitiativeModifier)character.getStatistic(Creature.STATISTIC_INITIATIVE);
		text.append(stat.getSummary());
		text.append("</body></html>");
		totLabel.setToolTipText(text.toString());
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+AbilityScore.Type.DEXTERITY)) {
			dexLabel.setText("Dex Mod: "+character.getAbilityModifierValue(AbilityScore.Type.DEXTERITY));
		} else if (arg0.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
			InitiativeModifier stat = (InitiativeModifier)character.getStatistic(Creature.STATISTIC_INITIATIVE);
			totLabel.setText("Total: "+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
			updateToolTip();
			updateSummaries((character.getInitiativeModifier() >= 0 ? "+" : "") + character.getInitiativeModifier());
		}
	}
}

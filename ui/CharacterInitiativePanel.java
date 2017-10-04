package ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import gamesystem.AbilityScore;
import gamesystem.Creature;
import gamesystem.InitiativeModifier;
import party.Character;

@SuppressWarnings("serial")
class CharacterInitiativePanel extends CharacterSubPanel {
	private JLabel dexLabel;
	private BoundIntegerField baseInit;
	private JLabel totLabel;

	CharacterInitiativePanel(Character c) {
		super(c);
		InitiativeModifier init = character.getInitiativeStatistic();
		summary = (init.getValue() >= 0 ? "+" : "") + init.getValue();

		dexLabel = new JLabel("Dex Mod: "+character.getAbilityModifierValue(AbilityScore.Type.DEXTERITY));
		add(dexLabel);

		add(new JLabel("Base:"));

		baseInit = new BoundIntegerField(init, 3);
		add(baseInit);

		totLabel = new JLabel("Total: " + init.getValue() + (init.hasConditionalModifier() ? "*" : ""));
		add(totLabel);

		addMouseListener(rightClickListener);
		totLabel.addMouseListener(rightClickListener);

		updateToolTip();

		// update labels when character changes
		character.addPropertyListener(init, (source, old) -> {
			totLabel.setText("Total: " + init.getValue() + (init.hasConditionalModifier() ? "*" : ""));
			updateToolTip();
			updateSummaries((init.getValue() >= 0 ? "+" : "") + init.getValue());
		});
		character.addPropertyListener(character.getAbilityStatistic(AbilityScore.Type.DEXTERITY), (source, old) -> {
			dexLabel.setText("Dex Mod: " + character.getAbilityModifierValue(AbilityScore.Type.DEXTERITY));
		});
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
		InitiativeModifier stat = character.getInitiativeStatistic();
		text.append(stat.getSummary());
		text.append("</body></html>");
		totLabel.setToolTipText(text.toString());
	}
}

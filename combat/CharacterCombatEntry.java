package combat;

import java.awt.GridBagConstraints;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gamesystem.AC;
import gamesystem.Creature;
import gamesystem.InitiativeModifier;
import gamesystem.Modifier;
import gamesystem.Sanity;
import gamesystem.Statistic;
import gamesystem.core.Property;
import gamesystem.core.PropertyListener;
import party.Character;
import ui.CharacterDamageDialog;

@SuppressWarnings("serial")
public class CharacterCombatEntry extends CombatEntry {
	JLabel sanityCurrent;
	JLabel sanitySession;
	Sanity sanity;

	CharacterCombatEntry(Character character) {
		super(character);
		sanity = character.getSanity();

		blank = false;

		AC ac = (AC) character.getStatistic(Creature.STATISTIC_AC);
		acComp = new JLabel(""+ac.getValue()+(ac.hasConditionalModifier()?"*":""));
		touchACComp = new JLabel(""+ac.getTouchAC().getValue()+(ac.getTouchAC().hasConditionalModifier()?"*":""));
		flatFootedACComp = new JLabel(""+ac.getFlatFootedAC().getValue()+(ac.getFlatFootedAC().hasConditionalModifier()?"*":""));

		character.addPropertyListener("ac", new PropertyListener<Integer>() {
			@Override
			public void propertyChanged(Property<Integer> source, Integer oldValue) {
				AC ac = creature.getACStatistic();
				((JLabel) acComp).setText("" + ac.getValue() + (ac.hasConditionalModifier() ? "*" : ""));
				((JLabel) touchACComp).setText("" + ac.getTouchAC().getValue() + (ac.getTouchAC().hasConditionalModifier() ? "*" : ""));
				((JLabel) flatFootedACComp).setText("" + ac.getFlatFootedAC().getValue() + (ac.getFlatFootedAC().hasConditionalModifier() ? "*" : ""));
				updateACToolTips();
			}
		});
		character.addPropertyListener("initiative", new PropertyListener<Integer>() {
			@Override
			public void propertyChanged(Property<Integer> source, Integer oldValue) {
				InitiativeModifier stat = creature.getInitiativeStatistic();
				((JLabel) modifierComp).setText("" + stat.getValue() + (stat.hasConditionalModifier() ? "*" : ""));
				updateInitToolTip();
			}
		});

		createPanel();

		InitiativeModifier stat = (InitiativeModifier) character.getStatistic(Creature.STATISTIC_INITIATIVE);
		((JLabel)modifierComp).setText(""+stat.getValue()+(stat.hasConditionalModifier()?"*":""));

		updateInitToolTip();
		updateACToolTips();
		//setToolTipText("AC breakdown"); // the text is irrelevant as we override the JToolTip (see below). this just forces the tip to appear
	}

/*	public JToolTip createToolTip() {
		JToolTip tip = new JToolTip() {
			public String getTipText() {
				if (creature instanceof Character) {
					Character c = (Character)creature;
					// get the AC components
					String components = "<html>Base AC: 10<br>";
					for (int i = 0; i < AC.AC_MAX_INDEX; i++) {
						int v = c.getACComponent(i);
						if (v != 0) {
							components += AC.getACComponentName(i) + ": " + v + "<br>";
						}
					}
					return components+"</html>";
				}
				return null;
			}
		};
        tip.setComponent(this);
        return tip;
	}*/

	private void updateInitToolTip() {
		InitiativeModifier stat = creature.getInitiativeStatistic();

		StringBuilder text = new StringBuilder();
		text.append("<html><body>");
		text.append(stat.getBaseValue()).append(" base<br/>");
		Map<Modifier, Boolean> mods = stat.getModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(stat.getValue()).append(" total");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		modifierComp.setToolTipText(text.toString());
	}

	private void updateACToolTips() {
		AC ac = creature.getACStatistic();

		Map<Modifier, Boolean> mods = ac.getModifiers();
		StringBuilder text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(ac.getValue()).append(" total");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		acLabel.setToolTipText(text.toString());
		acComp.setToolTipText(text.toString());

		mods = ac.getTouchAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(ac.getTouchAC().getValue()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		touchACLabel.setToolTipText(text.toString());
		touchACComp.setToolTipText(text.toString());

		mods = ac.getFlatFootedAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(ac.getFlatFootedAC().getValue()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		flatFootedACLabel.setToolTipText(text.toString());
		flatFootedACComp.setToolTipText(text.toString());
	}

	@Override
	void createButtons() {
		apply = new JButton("Dmg/Heal");
		apply.addActionListener(e -> {
			CharacterDamageDialog.openDialog(this, "Damage and healing", (Character) creature);
		});
		healAll = null;
	}

	@Override
	void layoutHPComponents(GridBagConstraints c) {
		c.fill = GridBagConstraints.NONE;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 0.0;
		add(new JLabel("HP: "), c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		currentHPs = new JLabel(creature.getHPStatistic().getShortSummary());
		add(currentHPs, c);
		c.weightx = 0.0;
		add(new JLabel("Sanity: "), c);
		c.weightx = 1.0;
		sanityCurrent = new JLabel(Integer.toString(creature.getSanity().getValue()));
		add(sanityCurrent, c);
		c.weightx = 0.0;
		c.gridwidth = 2;
		add(new JLabel("Session loss:"), c);
		c.weightx = 1.0;
		c.gridwidth = 1;
		sanitySession = new JLabel(Integer.toString(creature.getSanity().getSessionStartingSanity() - creature.getSanity().getValue()));
		add(sanitySession, c);
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		if (apply != null) add(apply, c);
		c.fill = GridBagConstraints.NONE;

		sanity.addPropertyListener((source, oldValue) -> {
			sanityCurrent.setText(sanity.getValue().toString());
			sanitySession.setText(Integer.toString(sanity.getSessionStartingSanity() - sanity.getValue()));
		});
	}

	@Override
	JComponent createNameSection() {
		onlyDM.setSelected(true); // we assume an non-editable source is a character that should be visible
		int mod = creature.getInitiativeStatistic().getValue();
		modifierComp = new JLabel(""+mod);
		total.setText("= "+mod);
		return new JLabel(creature.getName());
	}

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement("CharacterEntry");
		e.setAttribute("name", creature.getName());
		e.setAttribute("roll", Integer.toString(getRoll()));
		e.setAttribute("tieBreak", Integer.toString(getTieBreak()));
		e.setAttribute("creatureID", Integer.toString(creature.getID()));
		return e;
	}
}

package combat;

import gamesystem.AC;
import gamesystem.InitiativeModifier;
import gamesystem.Modifier;
import gamesystem.Statistic;

import java.beans.PropertyChangeEvent;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import party.Character;
import party.Creature;

@SuppressWarnings("serial")
public class CharacterCombatEntry extends CombatEntry {
	public CharacterCombatEntry(Creature creature) {
		this.creature = creature;

		blank = false;

		AC ac = (AC)((Character)creature).getStatistic(Creature.STATISTIC_AC);
		acComp = new JLabel(""+ac.getValue()+(ac.hasConditionalModifier()?"*":""));
		touchACComp = new JLabel(""+ac.getTouchAC().getValue()+(ac.getTouchAC().hasConditionalModifier()?"*":""));
		flatFootedACComp = new JLabel(""+ac.getFlatFootedAC().getValue()+(ac.getFlatFootedAC().hasConditionalModifier()?"*":""));

		creature.addPropertyChangeListener(this);
		createPanel();

		InitiativeModifier stat = (InitiativeModifier)((Character)creature).getStatistic(Creature.STATISTIC_INITIATIVE);
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

	protected void updateInitToolTip() {
		if (!(creature instanceof Character)) return;
		InitiativeModifier stat = (InitiativeModifier)((Character)creature).getStatistic(Creature.STATISTIC_INITIATIVE);

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

	protected void updateACToolTips() {
		if (!(creature instanceof Character)) return;
		Character character = (Character)creature; 
		AC ac = (AC)character.getStatistic(Creature.STATISTIC_AC);

		Map<Modifier, Boolean> mods = ac.getModifiers();
		StringBuilder text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getAC()).append(" total");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		acLabel.setToolTipText(text.toString());
		acComp.setToolTipText(text.toString());

		mods = ac.getTouchAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getTouchAC()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		touchACLabel.setToolTipText(text.toString());
		touchACComp.setToolTipText(text.toString());

		mods = ac.getFlatFootedAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getFlatFootedAC()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		flatFootedACLabel.setToolTipText(text.toString());
		flatFootedACComp.setToolTipText(text.toString());
	}

	protected JComponent createNameSection() {
		onlyDM.setSelected(true); // we assume an non-editable source is a character that should be visible
		int mod = creature.getInitiativeModifier();
		modifierComp = new JLabel(""+mod);
		total.setText("= "+mod);
		return new JLabel(creature.getName());
	}

	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == creature) {
			// update the relevant fields
			if (evt.getPropertyName().equals(Creature.PROPERTY_AC)) {
				AC ac = (AC)((Character)creature).getStatistic(Creature.STATISTIC_AC);
				((JLabel)acComp).setText(""+ac.getValue()+(ac.hasConditionalModifier()?"*":""));
				((JLabel)touchACComp).setText(""+ac.getTouchAC().getValue()+(ac.getTouchAC().hasConditionalModifier()?"*":""));
				((JLabel)flatFootedACComp).setText(""+ac.getFlatFootedAC().getValue()+(ac.getFlatFootedAC().hasConditionalModifier()?"*":""));
				updateACToolTips();
			} else if (evt.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
				InitiativeModifier stat = (InitiativeModifier)((Character)creature).getStatistic(Creature.STATISTIC_INITIATIVE);
				((JLabel)modifierComp).setText(""+stat.getValue()+(stat.hasConditionalModifier()?"*":""));
				updateInitToolTip();
			}
		}
	}

	public String getXML(String indent, String nextIndent) {
		StringBuilder b = new StringBuilder();
		b.append(indent).append("<CharacterEntry name=\"").append(creature.getName());
		b.append("\" roll=\"").append(getRoll());
		b.append("\" tieBreak=\"").append(getTieBreak());
		b.append("\"/>").append(System.getProperty("line.separator"));
		return b.toString();
	}
}

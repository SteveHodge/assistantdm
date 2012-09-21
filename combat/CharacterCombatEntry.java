package combat;

import gamesystem.AC;

import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolTip;

import party.Character;
import party.Creature;

@SuppressWarnings("serial")
public class CharacterCombatEntry extends CombatEntry {
	public CharacterCombatEntry(Creature creature) {
		this.creature = creature;

		blank = false;

		acComp = new JLabel(""+creature.getAC());
		touchACComp = new JLabel(""+creature.getTouchAC());
		flatFootedACComp = new JLabel(""+creature.getFlatFootedAC());

		creature.addPropertyChangeListener(this);
		createPanel();
		setToolTipText("AC breakdown"); // the text is irrelevant as we override the JToolTip (see below). this just forces the tip to appear
	}

	public JToolTip createToolTip() {
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
				((JLabel)acComp).setText(""+creature.getAC());
				((JLabel)touchACComp).setText(""+creature.getTouchAC());
				((JLabel)flatFootedACComp).setText(""+creature.getFlatFootedAC());
			} else if (evt.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
				((JLabel)modifierComp).setText("" + creature.getInitiativeModifier());
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

import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.Character;
import party.Creature;
import party.Party;
import party.Skill;
import party.XP;
import party.Character.XPHistoryItem;

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

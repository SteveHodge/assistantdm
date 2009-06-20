import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;

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
}

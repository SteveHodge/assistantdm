import java.awt.GridBagConstraints;
import java.beans.PropertyChangeEvent;

import javax.swing.JLabel;

import party.Character;

@SuppressWarnings("serial")
public class CharacterInitiativeEntry extends InitiativeEntry {
	protected JLabel nameLabel;

	Character character = null;

	public CharacterInitiativeEntry(Character c) {
		super(false);
		character = c;
		blank = false;
		createPanel();
	}

	protected void addNameSection(GridBagConstraints c) {
		onlyDM.setSelected(true);
		Integer mod = new Integer(character.getInitiativeModifier());
		modifierField.setValue(mod);
		total.setText("= "+mod);
		nameLabel = new JLabel(character.getName());
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		add(nameLabel, c);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == modifierField) {
			character.setInitiativeModifier((Integer)modifierField.getValue());
		}
		total.setText("= "+getTotal());
		fireChange();
	}

	public int getModifier() {
		return character.getInitiativeModifier();
	}

	public String getName() {
		return character.getName();
	}
}

package ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import party.Character;
import party.Party;

// TODO would be nice to set dropdowns to likely correct options

@SuppressWarnings("serial")
public class UpdateCharacterDialog extends JDialog implements ActionListener {
	boolean returnOk = false;
	Map<Character,JComboBox> dropdownMap;

	protected class CharacterEntry {
		Character character;

		public CharacterEntry(Character c) {
			character = c;
		}

		public String toString() {
			return character.getName();
		}
	}

	public UpdateCharacterDialog(JFrame frame, Party oldparty, Party newparty) {
		super(frame, "Select characters to update", true);
		dropdownMap = new HashMap<Character,JComboBox>();

		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new GridLayout(newparty.size()+1,2));
		selectPanel.add(new JLabel("Incomming character"));
		selectPanel.add(new JLabel("Character to update"));
		Object[] options = new Object[oldparty.size()+1];
		for (int i=0; i<oldparty.size(); i++) {
			options[i+1] = new CharacterEntry(oldparty.get(i));
		}
		for (int i=0; i<newparty.size(); i++) {
			Character c = newparty.get(i);
			selectPanel.add(new JLabel(c.getName()));
			JComboBox combo = new JComboBox(options);
			dropdownMap.put(c, combo);
			selectPanel.add(combo);
		}
		JPanel buttons = new JPanel();
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttons.add(okButton);
		buttons.add(cancelButton);
		add(selectPanel);
		add(buttons,"South");
		pack();
	}

	public boolean isCancelled() {
		return !returnOk;
	}

	public Character getSelectedCharacter(Character c) {
		JComboBox combo = dropdownMap.get(c);
		if (combo == null) return null;
		Object selected = combo.getSelectedItem();
		if (selected != null && selected instanceof CharacterEntry) {
			return ((CharacterEntry)selected).character;
		}
		return null;
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("Ok")) {
			returnOk = true;
		}

		setVisible(false);
	}
}

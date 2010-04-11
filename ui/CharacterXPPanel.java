package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import party.Character;
import party.Creature;

// TODO better adhoc change dialog
// TODO better history dialog
@SuppressWarnings("serial")
public class CharacterXPPanel extends JPanel implements PropertyChangeListener, ActionListener {
	protected Character character;
	JLabel xpLabel;
	JButton adhocButton, historyButton, levelButton;

	public CharacterXPPanel(Character c) {
		character = c;

		setBorder(new TitledBorder("Levels and Experience"));
		add(new JLabel("Level: "));
		add(new BoundIntegerField(character, Creature.PROPERTY_LEVEL, 2));
		xpLabel = new JLabel(String.format("XP: %,d / %,d", character.getXP(), character.getRequiredXP()));
		add(xpLabel);
		levelButton = new JButton("Level Up");
		levelButton.addActionListener(this);
		levelButton.setEnabled(character.getXP() >= character.getRequiredXP());
		add(levelButton);
		adhocButton = new JButton("Adhoc Change");
		adhocButton.addActionListener(this);
		add(adhocButton);
		historyButton = new JButton("History");
		historyButton.addActionListener(this);
		add(historyButton);

		// update fields when character changes
		character.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Creature.PROPERTY_LEVEL)
				|| e.getPropertyName().equals(Creature.PROPERTY_XP)) {
			xpLabel.setText(String.format("XP: %,d / %,d", character.getXP(), character.getRequiredXP()));
			levelButton.setEnabled(character.getXP() >= character.getRequiredXP());
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == levelButton) {
			character.setLevel(character.getLevel()+1);

		} else if (e.getSource() == adhocButton) {
			String s = (String)JOptionPane.showInputDialog(this,"Enter the required xp change:","Make adhoc XP change",JOptionPane.PLAIN_MESSAGE);
			if ((s != null) && (s.length() > 0)) {
				try {
					int xp = Integer.parseInt(s);
					character.addXPAdhocChange(xp);
				} catch (NumberFormatException ex) {
					return;	// do nothing
				}
			}

		} else if (e.getSource() == historyButton) {
			JTextArea area = new JTextArea(character.getXPHistory());
			JScrollPane scroll = new JScrollPane(area);
			
			JFrame popup = new JFrame(character.getName()+" XP History");
			popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			popup.getContentPane().add(scroll);
			popup.pack();
			popup.setVisible(true);
		}
	}
}

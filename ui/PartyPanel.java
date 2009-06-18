package ui;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import party.Character;
import party.Party;

// TODO better layout of characters
// TODO would be nice to be able to pop characters out to windows and pop them back in to tabs

@SuppressWarnings("serial")
public class PartyPanel extends JPanel {
	Party party;

	public PartyPanel(Party party) {
		this.party = party;

		JTabbedPane tabbedPane = new JTabbedPane();

		for (Character c : party) {
			tabbedPane.addTab(c.getName(), null, createCharacterPanel(c), c.getName());
			//JFrame window = new JFrame(c.getName());
			//window.add(createCharacterPanel(c));
			//window.pack();
			//window.setVisible(true);
		}
		add(tabbedPane);
	}

	public JPanel createCharacterPanel(Character c) {
		JPanel panel = new JPanel();

		Box leftPanel = new Box(BoxLayout.PAGE_AXIS);
		leftPanel.add(new CharacterAbilityPanel(c));
		//leftPanel.add(Box.createRigidArea(new Dimension(0,5)));
		leftPanel.add(new CharacterSavesPanel(c));
		//leftPanel.add(Box.createRigidArea(new Dimension(0,5)));
		leftPanel.add(new CharacterInitiativePanel(c));

		Box rightPanel = new Box(BoxLayout.PAGE_AXIS);
		rightPanel.add(new CharacterHitPointPanel(c));
		rightPanel.add(new CharacterACPanel(c));
		rightPanel.add(new CharacterSkillsPanel(c));

		panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
		panel.add(leftPanel);
		panel.add(rightPanel);
		panel.setPreferredSize(new Dimension(1000,600));
		return panel;
	}

	public JPanel oldcreateCharacterPanel(Character c) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints a = new GridBagConstraints();

		a.insets = new Insets(2, 3, 2, 3);
		a.fill = GridBagConstraints.BOTH;
		a.weightx = 1.0; a.weighty = 1.0;
		a.gridx = 0; a.gridy = 0; a.gridheight = 2;
		panel.add(new CharacterAbilityPanel(c),a);
		a.gridx = 0; a.gridy = 2; a.gridheight = 1;
		a.weightx = 1.0; a.weighty = 0.0;
		panel.add(new CharacterSavesPanel(c),a);
		a.gridx = 0; a.gridy = 3; a.gridheight = 1;
		panel.add(new CharacterInitiativePanel(c),a);

		a.gridx = 1; a.gridy = 0; a.gridheight = 1;
		a.weightx = 1.0; a.weighty = 0.0;
		panel.add(new CharacterHitPointPanel(c),a);
		a.gridx = 1; a.gridy = 1; a.gridheight = 1;
		panel.add(new CharacterACPanel(c),a);
		a.gridx = 1; a.gridy = 2; a.gridheight = 2;
		a.weightx = 1.0; a.weighty = 1.0;
		panel.add(new CharacterSkillsPanel(c),a);

		panel.setPreferredSize(new Dimension(1000,600));
		return panel;
	}
}

package ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.VerticalLayout;

import party.Character;
import party.Party;
import party.PartyListener;
import swing.JSubSection;

// TODO better layout of characters - rearrangeable layout would be nicest
// TODO would be nice to be able to pop characters out to windows and pop them back in to tabs
// TODO might be worth caching tabs of removed characters and reusing them if they are re-added (currently just create a new tab)

@SuppressWarnings("serial")
public class PartyPanel extends JPanel implements PartyListener {
	Party party;
	JTabbedPane tabbedPane;
	Map<Character,JComponent> tabs;

	public PartyPanel(Party party) {
		setLayout(new BorderLayout());

		this.party = party;
		this.party.addPartyListener(this);

		tabbedPane = new JTabbedPane();
		tabs = new HashMap<Character,JComponent>();
		for (Character c : party) {
			characterAdded(c);
		}
		add(tabbedPane, BorderLayout.CENTER);
	}

	public void characterAdded(Character c) {
		JComponent tab = createCharacterPanel(c);
		tabs.put(c, tab);
		tabbedPane.addTab(c.getName(), null, tab, c.getName());
	}

	public void characterRemoved(Character c) {
		JComponent tab = tabs.get(c);
		if (tab != null) {
			int i = tabbedPane.indexOfComponent(tab);
			if (i > -1) tabbedPane.removeTabAt(i);
			tabs.remove(tab);
		}
	}

	public JComponent createCharacterPanel(final Character c) {
		JPanel leftPanel = new JPanel();
		leftPanel.setMinimumSize(new Dimension(400,300));
		leftPanel.setLayout(new VerticalLayout());
		
		JXTaskPane actionsPane = new JXTaskPane("Actions");
		JButton saveHTML = new JButton("Save HTML");
		saveHTML.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				c.saveCharacterSheet();
			}
		});
		actionsPane.add(saveHTML);
		leftPanel.add(actionsPane);

		CharacterSubPanel p = new CharacterInfoPanel(c);
		JSubSection sub = new JSubSection("General Details", p);
		sub.setCollapsed(true);
		leftPanel.add(sub);

		p = new CharacterXPPanel(c);
		sub = new JSubSection("Levels and Experience", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		leftPanel.add(sub);

		p = new CharacterHitPointPanel(c);
		sub = new JSubSection("Hit Points", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		leftPanel.add(sub);

		p = new CharacterAbilityPanel(c);
		sub = new JSubSection("Ability Scores", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		leftPanel.add(sub);

		p = new CharacterSavesPanel(c);
		sub = new JSubSection("Saving Throws", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		leftPanel.add(sub);

		sub = new JSubSection("Buffs / Penalties", new CharacterBuffsPanel(c));
		leftPanel.add(sub);

		JPanel rightPanel = new JPanel();
		rightPanel.setMinimumSize(new Dimension(300,300));
		rightPanel.setLayout(new VerticalLayout());
	
		p = new CharacterInitiativePanel(c);
		sub = new JSubSection("Initiative", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		rightPanel.add(sub);

		p = new CharacterAttacksPanel(c);
		sub = new JSubSection("Attacks", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		rightPanel.add(sub);

		p = new CharacterACPanel(c);
		sub = new JSubSection("Armor Class", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		rightPanel.add(sub);

		sub = new JSubSection("Skills", new CharacterSkillsPanel(c));
		rightPanel.add(sub);

		JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		panel.setPreferredSize(new Dimension(1000,600));
		panel.setDividerLocation(0.5);
		return panel;
	}

	public class SummaryDisplay {
		JSubSection subSection;
		String title;

		SummaryDisplay(JSubSection sub) {
			subSection = sub;
			title = sub.getTitle();
		}

		public void updateSummary(String summary) {
			subSection.setCollapsedTitle(title + ":   " + summary);
		}
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

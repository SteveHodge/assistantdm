package ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import party.Character;
import party.CharacterSheetView;
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
		tabs = new HashMap<>();
		for (Character c : party) {
			characterAdded(c);
		}
		add(tabbedPane, BorderLayout.CENTER);
	}

	@Override
	public void characterAdded(Character c) {
		JComponent tab = createCharacterPanel(c);
		tabs.put(c, tab);
		tabbedPane.addTab(c.getName(), null, tab, c.getName());
	}

	@Override
	public void characterRemoved(Character c) {
		JComponent tab = tabs.get(c);
		if (tab != null) {
			int i = tabbedPane.indexOfComponent(tab);
			if (i > -1) tabbedPane.removeTabAt(i);
			tabs.remove(tab);
		}
	}

	/* Ideally want to have a split pane with each side containing a scroll pane containing the sections. The split pane should determine the width of the sections
	 * and the scroll pane would accommodate the height. The problem is that the scroll panes allow the subsections to have their preferred width which is too wide.
	 * The current alternative is to have the split pane inside a scroll pane. This means the split pane dictates the sections' width, as desired. The height is
	 * unconstrained, which is fine as the scroll pane can accommodate that but unfortunately the split pane does not adjust its height when one of the children
	 * changes height. This can result in the bottom sections not fitting in the fixed height of the split pane.
	 * Either need to have a scroll pane subclass that restricts it's child to it's own width, or need a split pane that expands it's height to match it's children.
	 * For now we're nesting the split pane inside a scroll pane and padding the split pane with extra height.
	 */
	public JComponent createCharacterPanel(final Character c) {
		Dimension padding = new Dimension(0, 1);

		final JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		final JScrollPane scroller = new JScrollPane(panel);

		final JPanel leftPanel = new JPanel();
		leftPanel.setMinimumSize(new Dimension(450,300));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));

		JPanel actionsPane = new JPanel();
		actionsPane.add(new JLabel("Actions: "));
		JCheckBox autoSaveCheck = new JCheckBox("Auto-update server");
		autoSaveCheck.setSelected(c.isAutoSaving());	// TODO we don't monitor this value, just set it once. if there is another way to set it we'll need to monitor
		autoSaveCheck.addItemListener(e -> {
			c.setAutoSave(e.getStateChange() == ItemEvent.SELECTED);
		});
		actionsPane.add(autoSaveCheck);
		JButton saveHTML = new JButton("Save HTML");
		saveHTML.addActionListener(e -> {
			CharacterSheetView view = new CharacterSheetView(c, false);
			view.saveCharacterSheet();
		});
		actionsPane.add(saveHTML);
		JButton debug = new JButton("Debug");
		actionsPane.add(debug);
		JButton healButton = new JButton("Damage/Heal");
		healButton.addActionListener(e -> {
			CharacterDamageDialog.openDialog(this, "Damage and healing", c);
		});
		actionsPane.add(healButton);
		actionsPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, actionsPane.getMinimumSize().height));
		leftPanel.add(actionsPane);

		CharacterSubPanel p = new CharacterInfoPanel(c);
		JSubSection sub = new JSubSection("General Details", p);
		sub.setCollapsed(true);
		leftPanel.add(sub);

		leftPanel.add(Box.createRigidArea(padding));

		p = new CharacterXPPanel(c);
		sub = new JSubSection("Levels and Experience", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		leftPanel.add(sub);

		leftPanel.add(Box.createRigidArea(padding));

		p = new CharacterClassesPanel(c);
		sub = new JSubSection("Classes", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		sub.setCollapsed(true);
		leftPanel.add(sub);

		leftPanel.add(Box.createRigidArea(padding));

		p = new CharacterHitPointPanel(c);
		sub = new JSubSection("Hit Points", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		leftPanel.add(sub);

		leftPanel.add(Box.createRigidArea(padding));

		p = new CharacterAbilityPanel(c);
		sub = new JSubSection("Ability Scores", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		leftPanel.add(sub);

		leftPanel.add(Box.createRigidArea(padding));

		p = new CharacterSavesPanel(c);
		sub = new JSubSection("Saving Throws", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		leftPanel.add(sub);

		leftPanel.add(Box.createRigidArea(padding));

		sub = new JSubSection("Buffs / Penalties", new CharacterBuffsPanel(c));
		leftPanel.add(sub);

		leftPanel.add(Box.createRigidArea(padding));

		sub = new JSubSection("Feats", new CharacterFeatsPanel(c));
		leftPanel.add(sub);

		leftPanel.add(Box.createVerticalGlue());

		final JPanel rightPanel = new JPanel();

		rightPanel.setMinimumSize(new Dimension(300,300));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));

		p = new CharacterSanityPanel(c);
		sub = new JSubSection("Sanity", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		rightPanel.add(sub);

		rightPanel.add(Box.createRigidArea(padding));

		p = new CharacterInitiativePanel(c);
		sub = new JSubSection("Initiative", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		rightPanel.add(sub);

		rightPanel.add(Box.createRigidArea(padding));

		p = new CharacterAttacksPanel(c);
		sub = new JSubSection("Attacks", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		rightPanel.add(sub);

		rightPanel.add(Box.createRigidArea(padding));

		p = new CharacterACPanel(c);
		sub = new JSubSection("Armor Class", p);
		p.addSummaryDisplay(new SummaryDisplay(sub));
		rightPanel.add(sub);

		rightPanel.add(Box.createRigidArea(padding));

		sub = new JSubSection("Skills", new CharacterSkillsPanel(c));
		rightPanel.add(sub);

		rightPanel.add(Box.createVerticalGlue());

		panel.setLeftComponent(leftPanel);
		panel.setRightComponent(rightPanel);
		panel.setDividerLocation(500);
		panel.setPreferredSize(new Dimension(1000, panel.getPreferredSize().height + 600));

		scroller.setPreferredSize(new Dimension(1000, 600));

//		leftPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
//		rightPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));

		debug.addActionListener(e -> {
//			System.out.println("Left PreferredSize = "+leftPanel.getPreferredSize());
//			System.out.println("Right PreferredSize = "+rightPanel.getPreferredSize());
//			System.out.println("PreferredSize = "+panel.getPreferredSize());
//			System.out.println("Size = "+panel.getSize());
//			StatisticDescription[] targets = c.getStatistics();
//			for (StatisticDescription t : targets) {
//				System.out.println(t.target + ": " + t.name);
//			}
			c.debugDumpStructure();
		});

		return scroller;
	}

	public class SummaryDisplay {
		JSubSection subSection;

		SummaryDisplay(JSubSection sub) {
			subSection = sub;
		}

		public void updateSummary(String summary) {
			subSection.setInfoText(summary);
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

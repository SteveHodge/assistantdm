package ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import gamesystem.CasterLevels.CasterClass;
import gamesystem.CharacterClass;
import gamesystem.Spell;
import gamesystem.Spell.SpellListModel;

@SuppressWarnings("serial")
public class SpellsPanel extends JPanel {
	CasterClass casterClass;

	private SpellsPanel(CasterClass casterClass) {
		this.casterClass = casterClass;
	}

	static public class ScribePanel extends SpellsPanel {
		JLabel levelLabel = new JLabel();
		JLabel abilityLabel = new JLabel();
		JLabel maxLevelLabel = new JLabel();
		SpellListModel spellsModel;
		SpellbookModel spellbookModel;
		JComboBox<String> levelFilter;
		int[] slotData;

		DefaultListCellRenderer spellListRenderer = new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Spell spell = (Spell) value;
				int level = spellsModel.getLevel(spell);
				return super.getListCellRendererComponent(list, level + " " + spell.name, index, isSelected, cellHasFocus);
			}
		};

		class SpellbookModel extends AbstractListModel<Spell> {
			List<Spell> spells = new ArrayList<>();

			public void addSpell(Spell s) {
				if (spells.contains(s)) return;
				spells.add(s);
				Collections.sort(spells, spellsModel.getComparator());
				int idx = spells.indexOf(s);
				fireIntervalAdded(this, idx, idx);
			}

			public void removeSpell(Spell s) {
				int idx = spells.indexOf(s);
				if (idx != -1) {
					spells.remove(idx);
					fireIntervalRemoved(this, idx, idx);
				}
			}

			@Override
			public Spell getElementAt(int index) {
				return spells.get(index);
			}

			@Override
			public int getSize() {
				return spells.size();
			}
		}

		public ScribePanel(CasterClass cc) {
			super(cc);
			casterClass.addPropertyListener((e) -> update());

			slotData = cc.getSpellsArray();
			spellsModel = new Spell.SpellListModel(CharacterClass.WIZARD);
			spellbookModel = new SpellbookModel();

			JList<Spell> spellList = new JList<>(spellsModel);
			spellList.setCellRenderer(spellListRenderer);
			spellList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			spellList.setLayoutOrientation(JList.VERTICAL);
			spellList.setVisibleRowCount(-1);
			JScrollPane listScroller = new JScrollPane(spellList);
			listScroller.setPreferredSize(new Dimension(250, 400));

			JList<Spell> spellbook = new JList<>(spellbookModel);
			spellbook.setCellRenderer(spellListRenderer);
			spellbook.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			spellbook.setLayoutOrientation(JList.VERTICAL);
			spellbook.setVisibleRowCount(-1);
			JScrollPane bookScroller = new JScrollPane(spellbook);
			bookScroller.setPreferredSize(new Dimension(250, 400));

			levelFilter = new JComboBox<>();
			levelFilter.addActionListener(e -> updateSpellList());

			update();
			levelFilter.setSelectedItem("All");

			JButton scribeButton = new JButton("Scribe");
			scribeButton.addActionListener(e -> {
				List<Spell> selected = spellList.getSelectedValuesList();
				for (Spell s : selected) {
					spellsModel.removeSpell(s);
					spellbookModel.addSpell(s);
				}
			});
			JButton eraseButton = new JButton("Erase");
			eraseButton.addActionListener(e -> {
				List<Spell> selected = spellbook.getSelectedValuesList();
				for (Spell s : selected) {
					spellbookModel.removeSpell(s);
					spellsModel.addSpell(s);
				}
			});

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 2, 4, 2);
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			add(new JLabel("Available Spells"), c);
			add(listScroller, c);
			JPanel panel = new JPanel();
			panel.add(scribeButton);
			panel.add(new JLabel("Show level: "));
			panel.add(levelFilter);
			add(panel, c);

			c.gridx = 1;
			c.gridheight = 1;
			add(new JLabel("In Spellbook"), c);
			add(bookScroller, c);
			add(eraseButton, c);
		}

		void update() {
			levelLabel.setText("Level = " + casterClass.getCasterLevel());
			abilityLabel.setText("Ability = " + casterClass.getAbilityScore());

			if (casterClass.getCharacterClass() != CharacterClass.WIZARD)
				throw new IllegalArgumentException("Scribe Spells panel configured for class which can't learn spells: " + casterClass.getCharacterClass());
			maxLevelLabel.setText("Max Spell Level = " + casterClass.getMaxSpellLevel());

			// TODO rebuilding the filter list like this is not ideal. use a model that filters visible options
			int maxLevel = casterClass.getMaxSpellLevel();
			String filter = (String) levelFilter.getSelectedItem();
			levelFilter.removeAllItems();
			levelFilter.addItem("All");
			for (int i = spellsModel.getMinimumLevel(); i <= maxLevel; i++)
				levelFilter.addItem(Integer.toString(i));
			levelFilter.setSelectedItem(filter);

			updateSpellList();
		}

		void updateSpellList() {
			int minLevel = 0;
			int maxLevel = casterClass.getMaxSpellLevel();
			String filter = (String) levelFilter.getSelectedItem();

			if (filter != null && !filter.equals("All")) {
				minLevel = Integer.parseInt(filter);
				maxLevel = minLevel;
			}

			spellsModel.filter(minLevel, maxLevel);
		}
	}

	static public class PreparePanel extends ScribePanel {
		public PreparePanel(CasterClass cc) {
			super(cc);
		}

		@Override
		void update() {
			levelLabel.setText("Level = " + casterClass.getCasterLevel());
			abilityLabel.setText("Ability = " + casterClass.getAbilityScore());
			maxLevelLabel.setText("Max Spell Level = " + casterClass.getMaxSpellLevel());
		}
	}

	static public class LearnPanel extends ScribePanel {
		public LearnPanel(CasterClass cc) {
			super(cc);
		}

		@Override
		void update() {
			levelLabel.setText("Level = " + casterClass.getCasterLevel());
			abilityLabel.setText("Ability = " + casterClass.getAbilityScore());
			maxLevelLabel.setText("Max Spell Level = " + casterClass.getMaxSpellLevel());
		}
	}
}

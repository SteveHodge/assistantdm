package ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import gamesystem.Spell;

// TODO buttons could be disabled when no selection has been made
// TODO could have level filter option to only show levels with available slots
// TODO level filter should probably apply to both lists (does for scribe panel already)

@SuppressWarnings("serial")
public class SpellsPanel extends JPanel {
	CasterClass casterClass;
	Map<Spell, Integer> spellLevel = new HashMap<>();	// used to store the level of the spell for the specified class

	private SpellsPanel(CasterClass casterClass) {
		this.casterClass = casterClass;
	}

	int getLevel(Spell s) {
		if (spellLevel.containsKey(s))
			return spellLevel.get(s);

		String classAbbr = Spell.classMap.get(casterClass.getCharacterClass());
		if (classAbbr == null) return -1;
		if (s.level == null || s.level.length() == 0) return -1;
		String[] bits = s.level.split("\\s*,\\s*");
		for (int i = 0; i < bits.length; i++) {
			String classStr = bits[i].replaceAll("\\r\\n|\\r|\\n", " ").trim();
			if (classStr.contains(classAbbr)) {
				String levelStr = classStr.substring(classStr.indexOf(" ") + 1);	// XXX this is a bit fragile
				int l = Integer.parseInt(levelStr);
				spellLevel.put(s, l);
				return l;
			}
		}
		return -1;
	}

	Comparator<Spell> spellOrderComparator = new Comparator<Spell>() {
		@Override
		public int compare(Spell a, Spell b) {
			int alevel = getLevel(a);
			int blevel = getLevel(b);
			if (alevel == blevel) {
				return a.name.compareTo(b.name);
			} else {
				return alevel - blevel;
			}
		}
	};

	DefaultListCellRenderer spellListRenderer = new DefaultListCellRenderer() {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Spell spell = (Spell) value;
			int level = getLevel(spell);
			return super.getListCellRendererComponent(list, level + " " + spell.name, index, isSelected, cellHasFocus);
		}
	};

	class LevelFilterCombo extends JComboBox<String> {
		SpellListModel spellsModel1;
		SpellListModel spellsModel2;

		LevelFilterCombo(SpellListModel model) {
			this(model, null);
		}

		LevelFilterCombo(SpellListModel model1, SpellListModel model2) {
			spellsModel1 = model1;
			spellsModel2 = model2;
			casterClass.addPropertyListener((e) -> updateOptions());
			addActionListener(e -> updateSpellList());
			updateOptions();
		}

		void updateSpellList() {
			int minLevel = 0;
			int maxLevel = casterClass.getMaxSpellLevel();
			String filter = (String) getSelectedItem();

			if (filter != null && !filter.equals("All")) {
				minLevel = Integer.parseInt(filter);
				maxLevel = minLevel;
			}

			spellsModel1.filter(minLevel, maxLevel);
			if (spellsModel2 != null) spellsModel2.filter(minLevel, maxLevel);
		}

		void updateOptions() {
			// TODO rebuilding the filter list like this is not ideal. use a model that filters visible options
			int maxLevel = casterClass.getMaxSpellLevel();
			String filter = (String) getSelectedItem();
			removeAllItems();
			addItem("All");
			for (int i = spellsModel1.getMinimumLevel(); i <= maxLevel; i++)
				addItem(Integer.toString(i));
			if (filter != null)
				setSelectedItem(filter);
			else
				setSelectedItem("All");
		}
	}

	static class ScribePanel extends SpellsPanel {
		AllSpellsListModel spellsModel;
		SpellbookModel spellbookModel;
		LevelFilterCombo levelFilter;

		public ScribePanel(CasterClass cc) {
			super(cc);

			spellsModel = new AllSpellsListModel();
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

			levelFilter = new LevelFilterCombo(spellsModel, spellbookModel);

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
	}

	static class PreparePanel extends SpellsPanel {
		SpellListModel spellListModel;
		SpellSlotListModel spellSlotModel;
		LevelFilterCombo levelFilter;

		public PreparePanel(CasterClass cc, String[] domains) {
			this(cc, null, domains);
		}

		public PreparePanel(CasterClass cc) {
			this(cc, null, null);
		}

		public PreparePanel(CasterClass cc, SpellListModel sourceModel) {
			this(cc, sourceModel, null);
		}

		// shouldn't have both sourceModel and domains
		private PreparePanel(CasterClass cc, SpellListModel sourceModel, String[] domains) {
			super(cc);

			if (sourceModel != null) {
				spellListModel = sourceModel;
				spellSlotModel = new SpellSlotListModel(spellListModel, true);
			} else if (domains != null && domains.length > 0) {
				spellListModel = new AllSpellsListModel(domains);
				spellSlotModel = new SpellSlotListModel(spellListModel, false, true);
			} else {
				spellListModel = new AllSpellsListModel();
				spellSlotModel = new SpellSlotListModel(spellListModel, true);
			}

			JList<Spell> spellList = new JList<>(spellListModel);
			spellList.setCellRenderer(spellListRenderer);
			spellList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			spellList.setLayoutOrientation(JList.VERTICAL);
			spellList.setVisibleRowCount(-1);
			JScrollPane listScroller = new JScrollPane(spellList);
			listScroller.setPreferredSize(new Dimension(250, 400));

			JList<SpellSlot> knownList = new JList<>(spellSlotModel);
			knownList.setCellRenderer(spellSlotRenderer);
			knownList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			knownList.setLayoutOrientation(JList.VERTICAL);
			knownList.setVisibleRowCount(-1);
			JScrollPane bookScroller = new JScrollPane(knownList);
			bookScroller.setPreferredSize(new Dimension(250, 400));

			JButton prepareButton = new JButton("Prepare");
			prepareButton.addActionListener(e -> {
				List<Spell> selected = spellList.getSelectedValuesList();
				for (Spell s : selected) {
					spellSlotModel.addSpell(s, true);
				}
			});
			JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(e -> {
				List<SpellSlot> selected = knownList.getSelectedValuesList();
				for (SpellSlot s : selected) {
					spellSlotModel.removeSpell(s);
				}
				knownList.clearSelection();
			});

			levelFilter = new LevelFilterCombo(spellListModel);

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 2, 4, 2);
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			add(new JLabel("Available Spells"), c);
			add(listScroller, c);
			JPanel panel = new JPanel();
			panel.add(prepareButton);
			panel.add(new JLabel("Show level: "));
			panel.add(levelFilter);
			add(panel, c);

			c.gridx = 1;
			c.gridheight = 1;
			add(new JLabel("Known"), c);
			add(bookScroller, c);
			add(clearButton, c);
		}
	}

	static class LearnPanel extends SpellsPanel {
		AllSpellsListModel spellsModel;
		SpellSlotListModel spellSlotModel;
		LevelFilterCombo levelFilter;

		public LearnPanel(CasterClass cc) {
			super(cc);

			spellsModel = new AllSpellsListModel();
			spellSlotModel = new SpellSlotListModel(spellsModel);

			JList<Spell> spellList = new JList<>(spellsModel);
			spellList.setCellRenderer(spellListRenderer);
			spellList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			spellList.setLayoutOrientation(JList.VERTICAL);
			spellList.setVisibleRowCount(-1);
			JScrollPane listScroller = new JScrollPane(spellList);
			listScroller.setPreferredSize(new Dimension(250, 400));

			JList<SpellSlot> knownList = new JList<>(spellSlotModel);
			knownList.setCellRenderer(spellSlotRenderer);
			knownList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			knownList.setLayoutOrientation(JList.VERTICAL);
			knownList.setVisibleRowCount(-1);
			JScrollPane bookScroller = new JScrollPane(knownList);
			bookScroller.setPreferredSize(new Dimension(250, 400));

			JButton learnButton = new JButton("Learn");
			learnButton.addActionListener(e -> {
				List<Spell> selected = spellList.getSelectedValuesList();
				for (Spell s : selected) {
					spellsModel.removeSpell(s);
					spellSlotModel.addSpell(s, false);
				}
			});
			JButton unlearnButton = new JButton("Unlearn");
			unlearnButton.addActionListener(e -> {
				List<SpellSlot> selected = knownList.getSelectedValuesList();
				for (SpellSlot s : selected) {
					Spell spell = spellSlotModel.removeSpell(s);
					if (spell != null)
						spellsModel.addSpell(spell);
				}
				knownList.clearSelection();
			});

			levelFilter = new LevelFilterCombo(spellsModel);

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 2, 4, 2);
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			add(new JLabel("Available Spells"), c);
			add(listScroller, c);
			JPanel panel = new JPanel();
			panel.add(learnButton);
			panel.add(new JLabel("Show level: "));
			panel.add(levelFilter);
			add(panel, c);

			c.gridx = 1;
			c.gridheight = 1;
			add(new JLabel("Known"), c);
			add(bookScroller, c);
			add(unlearnButton, c);
		}
	}

	abstract class SpellListModel extends AbstractListModel<Spell> {
		List<Spell> filtered = new ArrayList<>();			// the list of currently visible spells
		int filterMinLevel = 0;				// current filter setting
		int filterMaxLevel = 9;

		abstract void addSpell(Spell s);

		abstract void removeSpell(Spell s);

		abstract void updateFiltered();		// add all spells that are visible to the filtered list

		abstract int getMinimumLevel();

		void filter(int min, int max) {
			if (filterMinLevel == min && filterMaxLevel == max) return;
			filterMinLevel = min;
			filterMaxLevel = max;
			int oldSize = filtered.size();
			filtered.clear();
			updateFiltered();
			int newSize = filtered.size();
			if (newSize > oldSize)
				fireIntervalAdded(this, oldSize, newSize - 1);
			if (newSize < oldSize)
				fireIntervalRemoved(this, newSize, oldSize - 1);
			fireContentsChanged(this, 0, newSize - 1);
		}

		@Override
		public Spell getElementAt(int i) {
			return filtered.get(i);
		}

		@Override
		public int getSize() {
			return filtered.size();
		}
	}

	// Provides a ListModel backed by a Collection of Spells (defaults to the set of known spells)
	class AllSpellsListModel extends SpellListModel {
		List<Spell> spellList;
		Set<Spell> removed = new HashSet<>();				// spells that have been removed from the list
		int minimumLevel = 999;			// minimum level spell in spellList

		// create a ListModel of the spells from the supplied collection that are available to the specified class
		public AllSpellsListModel(Collection<Spell> spellSet) {
			String classAbbr = Spell.classMap.get(casterClass.getCharacterClass());
			spellList = spellSet.stream()
					.filter(s -> {
						if (classAbbr == null) return true;
						if (s.level == null || s.level.length() == 0) return false;
						String[] bits = s.level.split("\\s*,\\s*");
						for (int i = 0; i < bits.length; i++) {
							String classStr = bits[i].replaceAll("\\r\\n|\\r|\\n", " ").trim();
							if (classStr.contains(classAbbr)) {
								String levelStr = classStr.substring(classStr.indexOf(" ") + 1);	// XXX this is a bit fragile
								int l = Integer.parseInt(levelStr);
								if (l < minimumLevel) minimumLevel = l;
								if (l > filterMaxLevel) filterMaxLevel = l;
								spellLevel.put(s, l);
								return true;
							}
						}
						return false;
					})
					.sorted(spellOrderComparator)
					.collect(Collectors.toList());
			filterMinLevel = minimumLevel;
			filtered.addAll(spellList);
		}

		public AllSpellsListModel() {
			this(Spell.spells);
		}

		public AllSpellsListModel(String[] domains) {
			spellList = Spell.spells.stream()
					.filter(s -> {
						if (s.level == null || s.level.length() == 0) return false;
						String[] bits = s.level.split("\\s*,\\s*");
						for (int i = 0; i < bits.length; i++) {
							String classStr = bits[i].replaceAll("\\r\\n|\\r|\\n", " ").trim();
							for (String domain : domains) {
								if (classStr.contains(domain)) {
									String levelStr = classStr.substring(classStr.indexOf(" ") + 1);	// XXX this is a bit fragile
									int l = Integer.parseInt(levelStr);
									if (l < minimumLevel) minimumLevel = l;
									if (l > filterMaxLevel) filterMaxLevel = l;
									spellLevel.put(s, l);
									return true;
								}
							}
						}
						return false;
					})
					.sorted(spellOrderComparator)
					.collect(Collectors.toList());
			filterMinLevel = minimumLevel;
			filtered.addAll(spellList);
		}

		@Override
		void updateFiltered() {
			for (Spell s : spellList) {
				int level = spellLevel.get(s);
				if (level >= filterMinLevel && level <= filterMaxLevel && !removed.contains(s))
					filtered.add(s);
			}
		}

		@Override
		void removeSpell(Spell s) {
			int index = filtered.indexOf(s);
			filtered.remove(s);
			removed.add(s);
			if (index != -1)
				fireIntervalRemoved(this, index, index);
		}

		@Override
		void addSpell(Spell s) {
			boolean refresh = false;
			if (!spellList.contains(s)) {
				spellList.add(s);
				Collections.sort(spellList, spellOrderComparator);
				refresh = true;
			}
			refresh = refresh | removed.remove(s);
			if (refresh)
				filter(filterMinLevel, filterMaxLevel);	// refilter the list
		}

		// returns the minimum level of spells in this class (should typically be 0 or 1)
		@Override
		int getMinimumLevel() {
			return minimumLevel;
		}
	}

	class SpellbookModel extends SpellListModel {
		List<Spell> spells = new ArrayList<>();

		@Override
		void addSpell(Spell s) {
			if (spells.contains(s)) return;
			spells.add(s);
			Collections.sort(spells, spellOrderComparator);

			int level = spellLevel.get(s);
			if (level >= filterMinLevel && level <= filterMaxLevel) {
				filtered.add(s);
				Collections.sort(filtered, spellOrderComparator);
				int idx = filtered.indexOf(s);
				fireIntervalAdded(this, idx, idx);
			}
		}

		@Override
		void removeSpell(Spell s) {
			spells.remove(s);
			int idx = filtered.indexOf(s);
			if (idx != -1) {
				filtered.remove(idx);
				fireIntervalRemoved(this, idx, idx);
			}
		}

		@Override
		void updateFiltered() {
			for (Spell s : spells) {
				int level = spellLevel.get(s);
				if (level >= filterMinLevel && level <= filterMaxLevel)
					filtered.add(s);
			}
		}

		@Override
		int getMinimumLevel() {
			return 0;
		}
	}

	class SpellSlot {
		int slotLevel = -1;		// level of the slot
		Spell spell;		// may be null for an empty slot
		int spellLevel = -1;		// level of the spell for the current class

	}

	Comparator<SpellSlot> spellSlotComparator = new Comparator<SpellSlot>() {
		@Override
		public int compare(SpellSlot a, SpellSlot b) {
			if (a.slotLevel != b.slotLevel)
				return a.slotLevel - b.slotLevel;
			if (b.spell == null && a.spell != null) return -1;
			if (a.spell == null && b.spell != null) return 1;
			if (a.spell == null && b.spell == null) return 0;
			return a.spell.name.compareTo(b.spell.name);
		}
	};

	DefaultListCellRenderer spellSlotRenderer = new DefaultListCellRenderer() {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			SpellSlot slot = (SpellSlot) value;
			String text = Integer.toString(slot.slotLevel);
			if (slot.spell != null) {
				text += " " + slot.spell.name;
				if (slot.spellLevel != slot.slotLevel)
					text += " (" + slot.spellLevel + ")";
			}
			return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
		}
	};

	class SpellSlotListModel extends AbstractListModel<SpellSlot> {
		Map<Integer, List<SpellSlot>> slots = new HashMap<>();		// list of slots at each level
		SpellListModel sourceList;
		boolean addBonus = false;
		boolean singleSlot = false;

		// sourceList is the model that provided spells for this list. if a change in the casterClass causes slots to be deleted then any spells in those
		// slots will be added to the sourceList, if it is not null
		// if singleSlot is true then only creates a single slot for each available spell level (used for domain spells)
		public SpellSlotListModel(SpellListModel sourceList, boolean addBonus, boolean singleSlot) {
			this.sourceList = sourceList;
			this.addBonus = addBonus;
			this.singleSlot = singleSlot;

			casterClass.addPropertyListener(e -> update());

			update();
		}

		public SpellSlotListModel(SpellListModel sourceList, boolean addBonus) {
			this(sourceList, false, false);
		}

		public SpellSlotListModel(SpellListModel sourceList) {
			this(sourceList, false);
		}

		public Spell removeSpell(SpellSlot s) {
			List<SpellSlot> l = slots.get(s.slotLevel);
			if (l == null || !l.contains(s)) return null;

			int index = 0;
			for (int i = 0; i < s.slotLevel; i++) {
				l = slots.get(i);
				if (l != null) index += l.size();
			}

			Spell spell = s.spell;
			s.spell = null;
			s.spellLevel = -1;
			Collections.sort(l, spellSlotComparator);
			fireContentsChanged(this, index, index + l.size() - 1);
			return spell;
		}

		public void addSpell(Spell s, boolean allowHigherSlot) {
			int spellLevel = getLevel(s);
			if (spellLevel == -1) return;
			// put the spell in the first empty slot it can fit in
			int index = 0;	// needed to fire change event
			for (int i = 0; i < spellLevel; i++) {
				List<SpellSlot> l = slots.get(i);
				if (l != null) index += l.size();
			}
			int limit = spellLevel;
			if (allowHigherSlot) limit = 9;
			for (int i = spellLevel; i <= limit; i++) {
				List<SpellSlot> l = slots.get(i);
				if (l == null) continue;
				for (SpellSlot slot : l) {
					if (slot.spell == null) {
						slot.spell = s;
						slot.spellLevel = spellLevel;
						Collections.sort(l, spellSlotComparator);
						fireContentsChanged(this, index, index + l.size() - 1);
						return;
					}
				}
				index += l.size();
			}
		}

		void update() {
			int[] slotCounts = casterClass.getSpellsArray(addBonus);
			Map<Integer, List<SpellSlot>> newSlots = new HashMap<>();
			for (int i = 0; i < slotCounts.length; i++) {
				if (singleSlot && slotCounts[i] > 0) slotCounts[i] = 1;
				if (singleSlot && i == 0) slotCounts[i] = 0;	// disable level 0 for domain spells // TODO bit of a hack; find a better way
				List<SpellSlot> list = new ArrayList<>();
				List<SpellSlot> oldList = slots.get(i);
				int firstNew = 0;
				if (oldList != null) {
					firstNew = oldList.size();
					// copy as many slots as possible
					for (int j = 0; j < slotCounts[i] && j < oldList.size(); j++) {
						list.add(oldList.get(j));
					}
					if (sourceList != null) {
						// return any spells in deleted slots to the source model
						for (int j = slotCounts[i]; j < oldList.size(); j++) {
							SpellSlot s = oldList.get(j);
							if (s.spell != null) {
								sourceList.addSpell(s.spell);
							}
						}
					}
				}
				for (int j = firstNew; j < slotCounts[i]; j++) {
					SpellSlot s = new SpellSlot();
					s.slotLevel = i;
					list.add(s);
				}
				if (list.size() > 0)
					newSlots.put(i, list);
			}
			int oldSize = getSize();
			slots = newSlots;
			int newSize = getSize();
			if (oldSize < newSize) {
				fireIntervalAdded(this, oldSize, newSize - 1);
			} else if (oldSize > newSize) {
				fireIntervalRemoved(this, oldSize - 1, newSize);
			}
			fireContentsChanged(this, 0, Math.min(oldSize, newSize) - 1);
		}

		@Override
		public SpellSlot getElementAt(int index) {
			for (int i = 0; i <= 9; i++) {
				List<SpellSlot> l = slots.get(i);
				if (l == null) continue;
				if (l.size() > index)
					return l.get(index);
				index -= l.size();
			}
			return null;
		}

		@Override
		public int getSize() {
			int size = 0;
			for (List<SpellSlot> l : slots.values())
				size += l.size();
			return size;
		}

	}
}

package ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import gamesystem.CasterLevels.CasterClass;
import gamesystem.CharacterClass;
import gamesystem.Spell;

// TODO could have level filter option to only show levels with available slots
// TODO level filter should probably apply to both lists (does for scribe panel already)

@SuppressWarnings("serial")
public class SpellsPanel extends JPanel {
	CasterClass casterClass;
	Map<Spell, Integer> spellLevel = new HashMap<>();	// used to store the level of the spell for the specified class
	JLabel spellDetailsLabel;
	JScrollPane spellDetailScroller;

	private SpellsPanel(CasterClass casterClass) {
		this.casterClass = casterClass;
		spellDetailsLabel = new JLabel();
		spellDetailScroller = new JScrollPane(spellDetailsLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		spellDetailScroller.setPreferredSize(new Dimension(450, 400));
	}

	JsonValue getSpellsJsonBuilder() {
		return null;
	}

	String getJsonTabName() {
		return "tab_" + casterClass.getCharacterClass().toString().toLowerCase();
	}

	JScrollPane setupSpellList(JList<?> list, ListCellRenderer<Object> renderer) {
		list.setCellRenderer(renderer);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		JScrollPane leftScroller = new JScrollPane(list);
		leftScroller.setPreferredSize(new Dimension(250, 400));
		return leftScroller;
	}

	void setupListSelection(JList<?> leftList, JButton leftButton, JList<?> rightList, JButton rightButton) {
		leftButton.setEnabled(leftList.getSelectedIndices().length > 0);
		leftList.addListSelectionListener(e -> {
			int[] selections = leftList.getSelectedIndices();
			if (selections.length > 0) {
				rightList.clearSelection();
				leftButton.setEnabled(true);
				if (selections.length == 1) {
					Object value = leftList.getSelectedValue();
					if (value instanceof Spell) {
						spellDetailsLabel.setText(((Spell) value).getHTML());
					} else if (value instanceof SpellSlot) {
						Spell s = ((SpellSlot) value).spell;
						if (s != null)
							spellDetailsLabel.setText(s.getHTML());
					}
				}
			} else {
				leftButton.setEnabled(false);
			}
		});

		rightButton.setEnabled(rightList.getSelectedIndices().length > 0);
		rightList.addListSelectionListener(e -> {
			int[] selections = rightList.getSelectedIndices();
			if (selections.length > 0) {
				leftList.clearSelection();
				rightButton.setEnabled(true);
				if (selections.length == 1) {
					Object value = rightList.getSelectedValue();
					if (value instanceof Spell) {
						spellDetailsLabel.setText(((Spell) value).getHTML());
					} else if (value instanceof SpellSlot) {
						Spell s = ((SpellSlot) value).spell;
						if (s != null)
							spellDetailsLabel.setText(s.getHTML());
					}
				}
			} else {
				rightButton.setEnabled(false);
			}
		});
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

	void loadJson(JsonArray spells) {
		System.out.println("Parsing spells for " + getJsonTabName());
	}

	Map<String, Integer> metaFeats;
	{
		metaFeats = new HashMap<>();
		metaFeats.put("Empowered", 2);
		metaFeats.put("Enlarged", 1);
		metaFeats.put("Extended", 1);
		metaFeats.put("Heightened", 0);
		metaFeats.put("Maximized", 3);
		metaFeats.put("Quickened", 4);
		metaFeats.put("Silent", 1);
		metaFeats.put("Stilled", 1);
		metaFeats.put("Widened", 3);
	};

	static interface AddSpellAction {
		void addSpell(Spell spell, int lvlAdj, String prefix);
	}

	void loadJson(JsonArray spells, SpellListModel spellList, AddSpellAction action) {
		System.out.println("Parsing spells for " + getJsonTabName());
		spells.forEach(s -> {
			JsonObject spellJson = s.asJsonObject();
			String name = spellJson.getString("description");
			if (!name.startsWith(spellJson.getString("level") + " ")) {
				System.out.println("Spell description '" + name + "' does not match level " + spellJson.getString("level"));
			}
			name = name.substring(name.indexOf(' ') + 1);

			Spell spell = spellList.getSpell(name);
			// check for meta feats
			List<String> metas = new ArrayList<>();
			boolean done = false;
			int levelAdj = 0;
			boolean heightened = false;
			while (spell == null && !done) {
				// try cutting words off the front to see if we can find the spell
				String[] pieces = name.split(" ", 2);
				if (metaFeats.containsKey(pieces[0])) {
					int l = metaFeats.get(pieces[0]);
					if (l == 0) heightened = true;
					levelAdj += l;
					metas.add(pieces[0]);
					name = pieces[1];
					spell = spellList.getSpell(name);
				} else {
					// did not recognise the first word as a meta feat, so assume we're done
					done = true;
				}
			}

			if (spell != null) {
				int levelAdjust = 0;
				String prefix = "";
				if (metas.size() > 0) {
					int finalLevel = Integer.parseInt(spellJson.getString("level"));
					int spellLevel = getLevel(spell);
					int heightenedBy = 0;
					if (heightened) {
						heightenedBy = finalLevel - levelAdj - spellLevel;
					}
					System.out.println("Spell = '" + spell.name + "' (" + spellLevel + ") Meta feats = " + String.join(", ", metas) + ": total level adjust = " + (levelAdj + heightenedBy)
							+ (heightenedBy > 0 ? " heightened " + heightenedBy : ""));
					if (finalLevel != spellLevel + levelAdj + heightenedBy || heightenedBy < 0) {
						System.out.println("Could not calculate adjusted spell level");
					}
					prefix = String.join(" ", metas);
					levelAdjust = levelAdj + heightenedBy;
				}
				action.addSpell(spell, levelAdjust, prefix);
			} else {
				System.out.println("Could not find spell '" + name + "'");
			}
		});
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

	SpellListCellRenderer spellListRenderer = new SpellListCellRenderer();

	class SpellListCellRenderer extends DefaultListCellRenderer {
		int levelAdjustment = 0;
		String namePrefix = "";

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Spell spell = (Spell) value;
			int level = getLevel(spell) + levelAdjustment;
			return super.getListCellRendererComponent(list, level + " " + namePrefix + spell.name, index, isSelected, cellHasFocus);
		}

		void setMetaFeats(int levelAdjust, String prefix) {
			levelAdjustment = levelAdjust;
			namePrefix = prefix;
		}
	}

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
			JScrollPane listScroller = setupSpellList(spellList, spellListRenderer);

			JList<Spell> spellbook = new JList<>(spellbookModel);
			JScrollPane bookScroller = setupSpellList(spellbook, spellListRenderer);

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

			setupListSelection(spellList, scribeButton, spellbook, eraseButton);

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

			c.gridx = 2;
			c.gridheight = 3;
			add(spellDetailScroller, c);
		}

		@Override
		void loadJson(JsonArray spells) {
			super.loadJson(spells, spellsModel, (spell, lvl, prefix) -> {
				spellsModel.removeSpell(spell);
				spellbookModel.addSpell(spell);
			});
		}

		@Override
		String getJsonTabName() {
			if (casterClass.getCharacterClass() == CharacterClass.WIZARD)
				return "tab_spellbook";
			return super.getJsonTabName();
		}
	}

	static interface MetaFeatControl {
		public int getLevelAdjustment();

		public String getAdjective();
	}

	static class MetaFeatCheck extends JCheckBox implements MetaFeatControl {
		int levelAdjustment;
		String adjective;

		MetaFeatCheck(String name, int level, String adjective, ActionListener updateListener) {
			super(name);
			levelAdjustment = level;
			this.adjective = adjective;
			addActionListener(updateListener);
		}

		@Override
		public int getLevelAdjustment() {
			return isSelected() ? levelAdjustment : 0;
		}

		@Override
		public String getAdjective() {
			return isSelected() ? adjective : "";
		}
	}

	static class MetaFeatCombo extends JComboBox<Integer> implements MetaFeatControl {
		String adjective;

		MetaFeatCombo(String adjective, ActionListener updateListener) {
			this.adjective = adjective;
			addActionListener(updateListener);
		}

		@Override
		public int getLevelAdjustment() {
			Integer value = (Integer) getSelectedItem();
			return value == null ? 0 : value;
		}

		@Override
		public String getAdjective() {
			Integer value = (Integer) getSelectedItem();
			return value != null && value.intValue() > 0 ? adjective : "";
		}

	}

	static class PreparePanel extends SpellsPanel {
		SpellListModel spellListModel;
		SpellSlotListModel spellSlotModel;
		LevelFilterCombo levelFilter;
		SpellListCellRenderer spellRenderer = new SpellListCellRenderer();
		String[] domains;

		MetaFeatCheck empowerCheck;
		MetaFeatCheck enlargeCheck;
		MetaFeatCheck extendCheck;
		MetaFeatCheck maximizeCheck;
		MetaFeatCheck quickenCheck;
		MetaFeatCheck silentCheck;
		MetaFeatCheck stillCheck;
		MetaFeatCheck widenCheck;
		JLabel heightenLabel = new JLabel("Heighten:");			// as selection
		MetaFeatCombo heightenCombo;
		int maxSpellLevel = 0;
		int levelAdjustment = 0;
		String spellPrefix = "";

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
			this.domains = domains;

			if (sourceModel != null) {
				spellListModel = sourceModel;
				spellSlotModel = new SpellSlotListModel(spellListModel);
			} else if (domains != null && domains.length > 0) {
				spellListModel = new AllSpellsListModel(domains);
				spellSlotModel = new SpellSlotListModel(spellListModel, true);
			} else {
				spellListModel = new AllSpellsListModel();
				spellSlotModel = new SpellSlotListModel(spellListModel);
			}

			JList<Spell> spellList = new JList<>(spellListModel);
			JScrollPane leftScroller = setupSpellList(spellList, spellRenderer);

			JList<SpellSlot> preparedList = new JList<>(spellSlotModel);
			JScrollPane rightScroller = setupSpellList(preparedList, spellSlotRenderer);

			JButton prepareButton = new JButton("Prepare");
			prepareButton.addActionListener(e -> {
				List<Spell> selected = spellList.getSelectedValuesList();
				for (Spell s : selected) {
					spellSlotModel.addSpell(s, levelAdjustment, spellPrefix);
				}
			});
			JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(e -> {
				List<SpellSlot> selected = preparedList.getSelectedValuesList();
				for (SpellSlot s : selected) {
					spellSlotModel.removeSpell(s);
				}
				preparedList.clearSelection();
			});

			setupListSelection(spellList, prepareButton, preparedList, clearButton);

			levelFilter = new LevelFilterCombo(spellListModel);

			List<MetaFeatControl> metaControls = new ArrayList<>();
			ActionListener metaUpdate = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int levelAdjust = 0;
					String prefix = "";
					for (MetaFeatControl c : metaControls) {
						levelAdjust += c.getLevelAdjustment();
						String pre = c.getAdjective();
						if (pre.length() > 0)
							prefix += pre + " ";
					}
					if (!prefix.equals(spellPrefix) || levelAdjustment != levelAdjust) {
						spellPrefix = prefix;
						levelAdjustment = levelAdjust;
						spellRenderer.setMetaFeats(levelAdjustment, spellPrefix);
						spellListModel.setLevelAdjustment(levelAdjustment);
					}
				}
			};

			empowerCheck = new MetaFeatCheck("Empower", 2, "Empowered", metaUpdate);
			enlargeCheck = new MetaFeatCheck("Enlarge", 1, "Enlarged", metaUpdate);
			extendCheck = new MetaFeatCheck("Extend", 1, "Extended", metaUpdate);
			maximizeCheck = new MetaFeatCheck("Maximize", 3, "Maximized", metaUpdate);
			quickenCheck = new MetaFeatCheck("Quicken", 4, "Quickened", metaUpdate);
			silentCheck = new MetaFeatCheck("Silent", 1, "Silent", metaUpdate);
			stillCheck = new MetaFeatCheck("Still", 1, "Stilled", metaUpdate);
			widenCheck = new MetaFeatCheck("Widen", 3, "Widened", metaUpdate);

			heightenCombo = new MetaFeatCombo("Heightened", metaUpdate);
			Collections.addAll(metaControls, new MetaFeatControl[] { empowerCheck, enlargeCheck, extendCheck, maximizeCheck, quickenCheck, silentCheck, stillCheck, widenCheck, heightenCombo });

			heightenCombo.setVisible(false);
			heightenLabel.setVisible(false);
			casterClass.addPropertyListener(e -> {
				int maxLevel = casterClass.getSpellsArray(false).length - 1;
				if (maxLevel != maxSpellLevel) {
					maxSpellLevel = maxLevel;
					if (maxLevel >= 1) {
						heightenCombo.removeAllItems();
						for (int i = 0; i <= maxLevel; i++) {
							heightenCombo.addItem(i);
						}
						heightenCombo.setSelectedItem(0);
						heightenLabel.setVisible(true);
						heightenCombo.setVisible(true);
					} else {
						heightenLabel.setVisible(false);
						heightenCombo.setVisible(false);
					}
				}
			});

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 2, 4, 2);
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridheight = 1;
			add(new JLabel("Show level:"), c);
			add(levelFilter, c);
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(10, 30));
			add(panel, c);
			add(new JLabel("Metamagic Feats:"), c);
			add(empowerCheck, c);
			add(enlargeCheck, c);
			add(extendCheck, c);
			add(maximizeCheck, c);
			add(quickenCheck, c);
			add(silentCheck, c);
			add(stillCheck, c);
			add(widenCheck, c);
			add(heightenLabel, c);
			add(heightenCombo, c);

			c.gridx = 1;
			add(new JLabel("Available Spells"), c);
			c.gridheight = 13;
			add(leftScroller, c);
			c.gridheight = 1;
			add(prepareButton, c);

			c.gridx = 2;
			add(new JLabel("Known"), c);
			c.gridheight = 13;
			add(rightScroller, c);
			c.gridheight = 1;
			add(clearButton, c);

			c.gridx = 3;
			c.gridheight = 15;
			add(spellDetailScroller, c);
		}

		@Override
		String getJsonTabName() {
			if (domains != null) return "tab_domain";
			return super.getJsonTabName();
		}

		@Override
		JsonValue getSpellsJsonBuilder() {
			JsonArrayBuilder builder = Json.createArrayBuilder();

			for (int level : spellSlotModel.slots.keySet()) {
				for (SpellSlot slot : spellSlotModel.slots.get(level)) {
					if (slot.spell != null) {
						String description = slot.slotLevel + " " + slot.spell.name;
						if (slot.spellLevel != slot.slotLevel)
							description += " (" + slot.spellLevel + ")";
						JsonObjectBuilder obj = Json.createObjectBuilder()
								.add("level", "" + slot.slotLevel)
								.add("locked", "false")
								.add("description", description);
						builder.add(obj);
					}
				}
			}

			return builder.build();
		}

		@Override
		void loadJson(JsonArray spells) {
			super.loadJson(spells, spellListModel, (spell, lvl, prefix) -> {
				spellSlotModel.addSpell(spell, lvl, prefix);
			});
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
			JScrollPane listScroller = setupSpellList(spellList, spellListRenderer);

			JList<SpellSlot> knownList = new JList<>(spellSlotModel);
			JScrollPane knownScroller = setupSpellList(knownList, spellSlotRenderer);

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

			setupListSelection(spellList, learnButton, knownList, unlearnButton);

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
			add(knownScroller, c);
			add(unlearnButton, c);

			c.gridx = 2;
			c.gridheight = 3;
			add(spellDetailScroller, c);
		}

		@Override
		JsonValue getSpellsJsonBuilder() {
			JsonArrayBuilder builder = Json.createArrayBuilder();

			for (int level : spellSlotModel.slots.keySet()) {
				for (SpellSlot slot : spellSlotModel.slots.get(level)) {
					if (slot.spell != null) {
						JsonObjectBuilder obj = Json.createObjectBuilder()
								.add("level", "" + slot.slotLevel)
								.add("locked", "false")
								.add("description", slot.spellLevel + " " + slot.spell.name);
						builder.add(obj);
					}
				}
			}

			return builder.build();
		}

		@Override
		void loadJson(JsonArray spells) {
			super.loadJson(spells, spellsModel, (spell, lvl, prefix) -> {
				spellsModel.removeSpell(spell);
				spellSlotModel.addSpell(spell, false);
			});
		}
	}

	abstract class SpellListModel extends AbstractListModel<Spell> {
		List<Spell> filtered = new ArrayList<>();			// the list of currently visible spells
		int filterMinLevel = 0;				// current filter setting
		int filterMaxLevel = 9;
		int levelAdjustment = 0;	// added to the level of each spell (for metamagic feats)

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

		void setLevelAdjustment(int adj) {
			levelAdjustment = adj;
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

		public Spell getSpell(String name) {
			name = name.replace('’', '\'');
			for (Spell s : filtered) {
				if (s.name.replace('’', '\'').equals(name)) return s;
			}
			return null;
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
								try {
									int l = Integer.parseInt(levelStr);
									if (l < minimumLevel) minimumLevel = l;
									if (l > filterMaxLevel) filterMaxLevel = l;
									spellLevel.put(s, l);
									return true;
								} catch (NumberFormatException e) {
									System.err.println("Error parsing spell '" + s.name + "': can't parse level '" + levelStr + "'");
									return false;
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
				int level = spellLevel.get(s) + levelAdjustment;
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
				int level = spellLevel.get(s) + levelAdjustment;
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
		Spell spell;			// may be null for an empty slot
		int spellLevel = -1;	// level of the spell for the current class
		String prefix = "";		// metamagic prefix

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
				if (slot.prefix != null && slot.prefix.length() > 0)
					text += " " + slot.prefix;
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
		boolean domain = false;

		// sourceList is the model that provided spells for this list. if a change in the casterClass causes slots to be deleted then any spells in those
		// slots will be added to the sourceList, if it is not null
		// if domain is true then only creates a single slot for each available spell level and no bonus slots are added
		public SpellSlotListModel(SpellListModel sourceList, boolean domain) {
			this.sourceList = sourceList;
			this.domain = domain;

			casterClass.addPropertyListener(e -> update());

			update();
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
			if (l != null) {
				Collections.sort(l, spellSlotComparator);
				fireContentsChanged(this, index, index + l.size() - 1);
			} else {
				fireContentsChanged(this, index, index);
			}
			return spell;
		}

		public void addSpell(Spell s, int levelAdj, String prefix) {
			addSpell(s, true, levelAdj, prefix);
		}

		public void addSpell(Spell s, boolean allowHigherSlot) {
			addSpell(s, allowHigherSlot, 0, "");
		}

		void addSpell(Spell s, boolean allowHigherSlot, int levelAdj, String prefix) {
			int spellLevel = getLevel(s) + levelAdj;
			if (spellLevel == -1) return;
			// put the spell in the first empty slot it can fit in
			int index = 0;	// needed to fire change event
			for (int i = 0; i < spellLevel; i++) {
				List<SpellSlot> l = slots.get(i);
				if (l != null) index += l.size();
			}
			int limit = spellLevel;			// highest slot we can use
			if (allowHigherSlot) limit = 9;
			for (int i = spellLevel; i <= limit; i++) {
				List<SpellSlot> l = slots.get(i);
				if (l == null) continue;
				for (SpellSlot slot : l) {
					if (slot.spell == null) {
						slot.spell = s;
						slot.spellLevel = spellLevel;
						if (slot.prefix != null)
							slot.prefix = prefix;
						Collections.sort(l, spellSlotComparator);
						fireContentsChanged(this, index, index + l.size() - 1);
						return;
					}
				}
				index += l.size();
			}
		}

		void update() {
			int[] slotCounts = casterClass.getSpellsArray(!domain);
			Map<Integer, List<SpellSlot>> newSlots = new HashMap<>();
			for (int i = 0; i < slotCounts.length; i++) {
				if (domain && slotCounts[i] > 0) slotCounts[i] = 1;
				if (domain && i == 0) slotCounts[i] = 0;	// disable level 0 for domain spells // TODO bit of a hack; find a better way
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

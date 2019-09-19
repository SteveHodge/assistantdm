package gamesystem;

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

import org.w3c.dom.Element;

public class Spell {
	public static Set<Spell> spells = new HashSet<>();

	public String name;
	public String school;
	public String level;
	public String components;
	public String castingTime;
	public String range;
	public String effect;
	public String duration;
	public String savingThrow;
	public String spellResistance;
	public String description;
	public String material;
	public String focus;
	public String xpCost;

	public Map<String, BuffFactory> buffFactories = new HashMap<>();

	public Element domNode;

	public static Map<CharacterClass, String> classMap = new HashMap<>();
	{
		classMap.put(CharacterClass.BARD, "Brd");
		classMap.put(CharacterClass.CLERIC, "Clr");
		classMap.put(CharacterClass.DRUID, "Drd");
		classMap.put(CharacterClass.PALADIN, "Pal");
		classMap.put(CharacterClass.RANGER, "Rgr");
		classMap.put(CharacterClass.SORCERER, "Sor");
		classMap.put(CharacterClass.WIZARD, "Wiz");
	}

	@SuppressWarnings("serial")
	public static class SpellListModel extends AbstractListModel<Spell> {
		List<Spell> spellList;
		Map<Spell, Integer> spellLevel = new HashMap<>();	// used to store the level of the spell for the specified class
		List<Spell> filtered = new ArrayList<>();			// the list of currently visible spells
		Set<Spell> removed = new HashSet<>();				// spells that have been removed from the list
		int minimumLevel = 999;			// minimum level spell in spellList
		int filterMinLevel;				// current filter setting
		int filterMaxLevel;

		// create a ListModel of the spells from the supplied collection that are available to the specified class
		public SpellListModel(CharacterClass cls, Collection<Spell> spellSet) {
			String classAbbr = classMap.get(cls);
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
					.sorted(getComparator())
					.collect(Collectors.toList());
			filterMinLevel = minimumLevel;
			filtered.addAll(spellList);
		}

		public SpellListModel(CharacterClass cls) {
			this(cls, spells);
		}

		public Comparator<Spell> getComparator() {
			return (a, b) -> {
				int alevel = getLevel(a);
				int blevel = getLevel(b);
				if (alevel == blevel) {
					return a.name.compareTo(b.name);
				} else {
					return alevel - blevel;
				}
			};
		}

		public void filter(int min, int max) {
			filterMinLevel = min;
			filterMaxLevel = max;
			int oldSize = filtered.size();
			filtered.clear();
			for (Spell s : spellList) {
				int level = spellLevel.get(s);
				if (level >= filterMinLevel && level <= filterMaxLevel && !removed.contains(s))
					filtered.add(s);
			}
			int newSize = filtered.size();
			if (newSize > oldSize)
				fireIntervalAdded(this, oldSize, newSize - 1);
			if (newSize < oldSize)
				fireIntervalRemoved(this, newSize, oldSize - 1);
			fireContentsChanged(this, 0, newSize - 1);
		}

		public void removeSpell(Spell s) {
			int index = filtered.indexOf(s);
			filtered.remove(s);
			removed.add(s);
			if (index != -1)
				fireIntervalRemoved(this, index, index);
		}

		public void addSpell(Spell s) {
			boolean refresh = false;
			if (!spellList.contains(s)) {
				spellList.add(s);
				Collections.sort(spellList, getComparator());
				refresh = true;
			}
			refresh = refresh | removed.remove(s);
			if (refresh)
				filter(filterMinLevel, filterMaxLevel);	// refilter the list
		}

		public int getLevel(Spell s) {
			if (spellLevel.containsKey(s))
				return spellLevel.get(s);
			return -1;
		}

		// returns the minimum level of spells in this class (should typically be 0 or 1)
		public int getMinimumLevel() {
			return minimumLevel;
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
}

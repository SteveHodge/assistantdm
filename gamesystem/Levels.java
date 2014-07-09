package gamesystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO this is statistic for the listener stuff but it doesn't really need modifiers so perhaps refactor

// TODO should XP be here too or separate?
// TODO support monsters?

public class Levels extends Statistic implements HitDice {
	int level = 1;
	List<CharacterClass> classes = new ArrayList<>();	// this can have more entries than the current level (if classes have been removed they are remembered)

	public Levels() {
		super("Level");
	}

	public int getLevel() {
		return level;
	}

	@Override
	public int getHitDiceCount() {
		return level;
	}

	public void setLevel(int l) {
		int old = level;
		level = l;
		pcs.firePropertyChange("value", old, level);
	}

	public CharacterClass getClass(int level) {
		if (level < 1 || level > classes.size()) return null;
		return classes.get(level - 1);
	}

	public void setClass(int lvl, CharacterClass cls) {
		if (lvl < 1 || lvl > level) throw new IllegalArgumentException();	// TODO better exception

		// add extra levels to class list
		if (lvl > classes.size()) {
			for (int i = classes.size(); i < lvl; i++) {
				classes.add(null);
			}
		}
		classes.set(lvl - 1, cls);

		pcs.firePropertyChange("value", null, level);	// TODO fire more appropriate event
	}

	// TODO use streams API if possible
	Map<CharacterClass, Integer> getClassLevels() {
		Map<CharacterClass, Integer> classLvl = new HashMap<>();
		for (int i = 0; i < level && i < classes.size(); i++) {
			CharacterClass c = classes.get(i);
			if (c != null) {
				if (classLvl.containsKey(c)) {
					classLvl.put(c, classLvl.get(c) + 1);
				} else {
					classLvl.put(c, 1);
				}
			}
		}
		return classLvl;
	}

	// TODO use streams API
	int getBaseSave(SavingThrow.Type type) {
		Map<CharacterClass, Integer> classLvl = getClassLevels();

		int save = 0;
		for (CharacterClass c : classLvl.keySet()) {
			save += c.getBaseSave(type, classLvl.get(c));
		}
		return save;
	}

	// TODO use streams API
	int getBAB() {
		Map<CharacterClass, Integer> classLvl = getClassLevels();

		int bab = 0;
		for (CharacterClass c : classLvl.keySet()) {
			bab += c.getBAB(classLvl.get(c));
		}
		return bab;
	}

}

package gamesystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO this is statistic for the listener stuff but it doesn't really need modifiers so perhaps refactor

// TODO should XP be here too or separate?
// TODO support monsters?

public class Levels extends Statistic {
	int level = 0;
	List<CharacterClass> classes = new ArrayList<>();	// this can have more entries than the current level (if classes have been removed they are remembered)

	public Levels() {
		super("Level");
	}

	public int getLevel() {
		return level;
	}

	public int getHitDiceCount() {
		return level;
	}

	public void setLevel(int l) {
		int old = level;
		level = l;
		pcs.firePropertyChange("value", old, level);
	}

	public CharacterClass getClass(int level) {
		if (level < 1 || level > this.level) throw new IllegalArgumentException("Level " + level + " does not exist [1.." + this.level + "] is valid");
		if (level > classes.size()) return null;
		return classes.get(level - 1);
	}

	public void setClass(int lvl, CharacterClass cls) {
		if (lvl < 1 || lvl > level) throw new IllegalArgumentException("Level " + level + " does not exist [1.." + this.level + "] is valid");	// TODO better exception

		// add extra levels to class list
		if (lvl > classes.size()) {
			for (int i = classes.size(); i < lvl; i++) {
				classes.add(null);
			}
//		} else {
//			CharacterClass old = classes.get(lvl - 1);
//			if (old == cls) return;	// no change, don't need to do anything further
//			if (old != null) {
//				// old class features will need to rebuilt as the number of levels of that class has dropped
//				// TODO implement
//			}
		}
		classes.set(lvl - 1, cls);
		// get the levelup actions for the class in question
//		Set<?> actions = cls.getActions(lvl);

		pcs.firePropertyChange("value", null, level);	// TODO fire more appropriate event
	}

	public int getClassLevel(CharacterClass cls) {
		int lvl = 0;
		for (int i = 0; i < level && i < classes.size(); i++) {
			CharacterClass c = classes.get(i);
			if (c == cls) lvl++;
		}
		return lvl;
	}

	// TODO use streams API if possible
	public Map<CharacterClass, Integer> getClassLevels() {
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
	public int getBaseSave(SavingThrow.Type type) {
		Map<CharacterClass, Integer> classLvl = getClassLevels();

		int save = 0;
		for (CharacterClass c : classLvl.keySet()) {
			save += c.getBaseSave(type, classLvl.get(c));
		}
		return save;
	}

	// TODO use streams API
	public int getBAB() {
		Map<CharacterClass, Integer> classLvl = getClassLevels();

		int bab = 0;
		for (CharacterClass c : classLvl.keySet()) {
			bab += c.getBAB(classLvl.get(c));
		}
		return bab;
	}

	public HitDice getHitDice() {
		HitDice hd = null;
		Map<CharacterClass, Integer> classLvl = getClassLevels();
		for (CharacterClass c : classLvl.keySet()) {
			if (hd == null) {
				hd = new HitDice(classLvl.get(c), c.getHitDiceType());
			} else {
				hd.add(classLvl.get(c), c.getHitDiceType());
			}
		}
		return hd;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		//b.append("level ").append(level).append(": ");
		Map<CharacterClass, Integer> classLvl = getClassLevels();
		for (CharacterClass c : classLvl.keySet()) {
			b.append(c).append(" ").append(classLvl.get(c));
		}
		return b.toString();
	}
}

package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/*
 * Skills is a compound Statistic - it represents all possible skills for a creature. Modifiers can be added to this class,
 * they will apply to all skills.
 * 
 * Unlike other modifiers, this class fires property change events for each skill name (not just "value").
 * "value" property changes denote changes to global skills modifiers (or ability modifiers). All skills should be considered updated
 */
// TODO need a way of retrieving a specific skill so it can be modified individually (or special case code for applying buffs to individual skills)
// TODO reimplement misc as Modifier
public class Skills extends Statistic {
	public Map<SkillType,Skill> skills = new HashMap<SkillType,Skill>();	// TODO public for Character.getXML. change when no longer required
	EnumMap<AbilityScore.Type,Modifier> abilityMods = new EnumMap<AbilityScore.Type,Modifier>(AbilityScore.Type.class);

	PropertyChangeListener abilityListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			// for ability modifier changes. sends event to indicate all skills need updating 
			pcs.firePropertyChange("value", null, null);
		}
	};

	public Skills(Collection<AbilityScore> abilities) {
		super("Skills");
		for (AbilityScore a : abilities) {
			abilityMods.put(a.type, a.getModifier());
			a.getModifier().addPropertyChangeListener(abilityListener);
		}
	}

	public void setRanks(SkillType s, float r) {
		Skill skill = skills.get(s);
		if (skill == null) {
			skill = new Skill(s,abilityMods.get(s.ability));
			skills.put(s,skill);
		}
		if (skill.ranks != r) {
			//int oldValue = getValue(s);
			skill.ranks = r;
			int newValue = getValue(s);
			pcs.firePropertyChange(skill.name, null, newValue);
		}
	}

	public float getRanks(SkillType s) {
		Skill skill = skills.get(s);
		if (skill == null) {
			return 0f;
		} else {
			return skill.ranks;
		}
	}

	public void setMisc(SkillType s, int m) {
		Skill skill = skills.get(s);
		if (skill == null) {
			skill = new Skill(s,abilityMods.get(s.ability));
			skills.put(s,skill);
		}
		if (skill.misc != m) {
			//int oldValue = getValue(s);
			skill.misc = m;
			int newValue = getValue(s);
			pcs.firePropertyChange(skill.name, null, newValue);
		}
	}

	public int getMisc(SkillType s) {
		Skill skill = skills.get(s);
		if (skill == null) {
			return 0;
		} else {
			return skill.misc;
		}
	}

	public int getValue(SkillType s) {
		Skill skill = skills.get(s);
		Set<Modifier> mods = new HashSet<Modifier>(modifiers);
		if (skill == null) {
			mods.add(abilityMods.get(s.ability));
			return super.getModifiersTotal(mods, null);
		} else {
			mods.addAll(skill.modifiers);
			return (int)skill.ranks + getModifiersTotal(mods, null) + skill.misc;
		}
	}

	// returns true if this has an active conditional modifier
	public boolean hasConditionalModifier(SkillType s) {
		Map<Modifier,Boolean> mods = getModifiers(s);
		for (Modifier m : mods.keySet()) {
			if (mods.get(m) && m.getCondition() != null) return true;
		}
		return false;
	}

	public Map<Modifier,Boolean> getModifiers(SkillType s) {
		Skill skill = skills.get(s);
		Set<Modifier> mods = new HashSet<Modifier>(modifiers);
		if (skill == null) {
			mods.add(abilityMods.get(s.ability));
		} else {
			mods.addAll(skill.modifiers);
		}
		return getModifiers(mods);
	}

	public void addModifier(SkillType s, Modifier m) {
		Skill skill = skills.get(s);
		if (skill == null) {
			skill = new Skill(s,abilityMods.get(s.ability));
			skills.put(s,skill);
		}

		//int oldValue = getValue(s);
		m.addPropertyChangeListener(listener);
		skill.modifiers.add(m);
		int newValue = getValue(s);
		pcs.firePropertyChange(s.name, null, newValue);
	}

	public void removeModifier(SkillType s, Modifier m) {
		Skill skill = skills.get(s);
		if (skill == null) {
			skill = new Skill(s,abilityMods.get(s.ability));
			skills.put(s,skill);
		}

		//int oldValue = getValue(s);
		skill.modifiers.remove(m);
		m.removePropertyChangeListener(listener);
		int newValue = getValue(s);
		pcs.firePropertyChange(s.name, null, newValue);
	}

	// returns all skills with ranks > 0
	public Set<SkillType> getTrainedSkills() {
		Set<SkillType> trained = new HashSet<SkillType>();
		for (SkillType type : skills.keySet()) {
			Skill s = skills.get(type);
			if (s.ranks > 0f) trained.add(type);
		}
		return trained;
	}

	public Element getElement(Document doc) {
		Element e = doc.createElement(name);

		ArrayList<SkillType> set = new ArrayList<SkillType>(skills.keySet());
		Collections.sort(set, new Comparator<SkillType>() {
			public int compare(SkillType o1, SkillType o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (SkillType s : set) {
			Element se = doc.createElement("Skill");
			se.setAttribute("type", s.name);
			Skill skill = skills.get(s);
			se.setAttribute("ranks", ""+skill.ranks);
			if (skill.misc != 0) se.setAttribute("misc", ""+skill.misc);
			e.appendChild(se);
		}
		return e;
	}

	public void parseDOM(Element e) {
		if (!e.getTagName().equals("Skills")) return;

		NodeList skills = e.getChildNodes();
		for (int j=0; j<skills.getLength(); j++) {
			if (!skills.item(j).getNodeName().equals("Skill")) continue;
			Element s = (Element)skills.item(j);
			String ranks = s.getAttribute("ranks");
			String type = s.getAttribute("type");
			SkillType skill = SkillType.getSkill(type);
			setRanks(skill, Float.parseFloat(ranks));
			String misc = s.getAttribute("misc");
			if (misc != "") setMisc(skill, Integer.parseInt(misc));
		}
	}
}

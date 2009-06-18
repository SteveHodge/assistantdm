package party;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xml.XML;
import xml.XMLUtils;


public class Character extends Creature implements XML {
	protected String name;
	protected int initModifier = 0;
	protected int[] saves = new int[3];
	protected int[] abilities = new int[6];
	protected int[] tempAbilities = new int[6];
	protected Map<String,Integer> skills = new HashMap<String,Integer>();
	protected int hps, wounds, nonLethal;
	protected int[] ac = new int[AC_MAX_INDEX];

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	// properties
	public final static String PROPERTY_NAME = "Name";	// not currently sent to listeners
	public final static String PROPERTY_MAXHPS = "Hit Points";
	public final static String PROPERTY_WOUNDS = "Wounds";
	public final static String PROPERTY_NONLETHAL = "Non Lethal Damage";
	public final static String PROPERTY_INITIATIVE = "Initiative";
	public final static String PROPERTY_ABILITY_PREFIX = "Ability: ";
	public final static String PROPERTY_ABILITY_OVERRIDE_PREFIX = "Temporary Ability: ";	// not currently sent to listeners
	public final static String PROPERTY_SAVE_PREFIX = "Save: ";
	public final static String PROPERTY_AC = "AC";
	public final static String PROPERTY_AC_COMPONENT_PREFIX = "AC: ";	// not currently sent to listeners
	public final static String PROPERTY_SKILL_PREFIX = "Skill: ";

	public Character(String n) {
		name = n;
		for (int i=0; i<6; i++) tempAbilities[i] = -1;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(property, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public int getAbilityScore(int type) {
		if (tempAbilities[type] != -1) return tempAbilities[type];
		return abilities[type];
	}

	public int getBaseAbilityScore(int type) {
		return abilities[type];
	}

	public int getAbilityModifier(int type) {
		return getModifier(getAbilityScore(type));
	}

	protected void fireAbilityChange(int type, int old, int value) {
        pcs.firePropertyChange(PROPERTY_ABILITY_PREFIX+ability_names[type], old, value);
        int modDelta = Creature.getModifier(value) - Creature.getModifier(old);
        if (modDelta != 0) {
        	// skills
        	// TODO this is inefficient
        	for (String skill : Skill.skill_ability_map.keySet()) {
        		int ability = Skill.skill_ability_map.get(skill);
        		if (ability == type) {
        	        pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+skill, getSkill(skill)-modDelta, getSkill(skill));
        		}
        	}

        	// saves
        	for (int i = 0; i < 3; i++) {
        		int stat = Creature.getSaveAbility(i);
        		if (stat == type) {
        			pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(i),
        					getSavingThrow(i)-modDelta, getSavingThrow(i)
        				);
        		}
        	}

        	// initiative
        	if (type == Creature.ABILITY_DEXTERITY) {
        		pcs.firePropertyChange(PROPERTY_INITIATIVE, getInitiativeModifier()-modDelta,
        				getInitiativeModifier()
        			);
        	}

        	// TODO other properties
        }
	}

	public void setAbilityScore(int type, int value) {
		int old = abilities[type];
		abilities[type] = value;
		fireAbilityChange(type,old,value);
	}

	public void setTemporaryAbility(int type, int value) {
		int old = tempAbilities[type];
		if (old == -1) old = abilities[type];
		if (value == abilities[type] || value == -1) {
			// reseting to normal score
			tempAbilities[type] = -1;
			fireAbilityChange(type,old,abilities[type]);
		} else {
			tempAbilities[type] = value;
			fireAbilityChange(type,old,value);
		}
	}

	public Set<String> getSkillNames() {
		return new HashSet<String>(skills.keySet());
	}

	public int getInitiativeModifier() {
		return initModifier+getAbilityModifier(Creature.ABILITY_DEXTERITY);
	}

	public int getBaseInitiative() {
		return initModifier;
	}

	public void setInitiativeModifier(int i) {
		int old = initModifier;
		initModifier = i;
		pcs.firePropertyChange(PROPERTY_INITIATIVE, old, i);
	}

	public int getSavingThrow(int save) {
		return saves[save]+getAbilityModifier(Creature.getSaveAbility(save));
	}

	public int getBaseSavingThrow(int save) {
		return saves[save];
	}

	public void setSavingThrow(int save, int total) {
		int old = saves[save];
		saves[save] = total;
		pcs.firePropertyChange(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(save), old, total);
	}

	public int getSkill(String skill) {
		int ranks = 0;
		if (skills.get(skill) != null) ranks = skills.get(skill);
		int ability = Skill.getAbilityForSkill(skill);
		if (ability == -1) return ranks;
		return ranks+getAbilityModifier(ability);
	}

	public int getSkillRanks(String skill) {
		Integer ranks = skills.get(skill);
		if (ranks == null) return 0;
		return ranks;
	}

	public void setSkill(String skill, int total) {
		int old = 0;
		if (skills.containsKey(skill)) old = skills.get(skill);
		skills.put(skill,total);
        pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+skill, old, total);
	}

	public void deleteSkill(String skill) {
		if (skills.containsKey(skill)) {
			int old = skills.get(skill);
			skills.remove(skill);
	        pcs.firePropertyChange(PROPERTY_SKILL_PREFIX+skill, old, 0);
		}
	}

	public String getName() {
		return name;
	}

	public int getMaximumHitPoints() {
		return hps;
	}

	public void setMaximumHitPoints(int hp) {
		int old = hps;
		hps = hp;
        pcs.firePropertyChange(PROPERTY_MAXHPS, old, hp);
	}		

	public int getWounds() {
		return wounds;
	}

	public void setWounds(int i) {
		int old = wounds;
		wounds = i;
		pcs.firePropertyChange(PROPERTY_WOUNDS, old, i);
	}

	public int getNonLethal() {
		return nonLethal;
	}

	public void setNonLethal(int i) {
		int old = nonLethal;
		nonLethal = i;
		pcs.firePropertyChange(PROPERTY_NONLETHAL, old, i);
	}

	public int getHPs() {
		return hps - wounds - nonLethal;
	}

	public void setACComponent(int type, int value) {
		int oldValue = ac[type];
		ac[type] = value;
		pcs.firePropertyChange(PROPERTY_AC, oldValue, value);
	}

	public int getACComponent(int type) {
		return ac[type];
	}

	public int getAC() {
		int totAC = 10;
		for (int i = 0; i < AC_MAX_INDEX; i++) {
			totAC += ac[i];
		}
		return totAC;
	}

	public int getFlatFootedAC() {
		if (ac[AC_DEX] < 1) return getAC();
		return getAC() - ac[AC_DEX];
	}

	public int getTouchAC() {
		return getAC() - ac[AC_ARMOR] - ac[AC_SHIELD] - ac[AC_NATURAL];
	}

	public void setAC(int ac) {}
	public void setTouchAC(int ac) {}
	public void setFlatFootedAC(int ac) {}
	public void setName(String name) {}

	public static Character parseDOM(Node node) {
		if (!node.getNodeName().equals("Character")) return null;
		Character c = new Character(XMLUtils.getAttribute(node, "name"));

		NodeList nodes = node.getChildNodes();
		if (nodes != null) {
			for (int i=0; i<nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
				Element e = (Element)nodes.item(i);
				String tag = e.getTagName();

				if (tag.equals("HitPoints")) {
					c.setMaximumHitPoints(Integer.parseInt(e.getAttribute("maximum")));
					if (e.hasAttribute("wounds")) c.setWounds(Integer.parseInt(e.getAttribute("wounds")));
					if (e.hasAttribute("non-lethal")) c.setNonLethal(Integer.parseInt(e.getAttribute("non-lethal")));

				} else if (tag.equals("Initiative")) {
					String value = e.getAttribute("value");
					if (value != null) c.setInitiativeModifier(Integer.parseInt(value));

				} else if (tag.equals("AbilityScores")) {
					NodeList abilities = e.getChildNodes();
					if (abilities != null) {
						for (int j=0; j<abilities.getLength(); j++) {
							if (!abilities.item(j).getNodeName().equals("AbilityScore")) continue;
							Element s = (Element)abilities.item(j);
							String value = s.getAttribute("value");
							String type = s.getAttribute("type");
							for (int k=0; k<ability_names.length; k++) {
								if (ability_names[k].equals(type)) {
									c.setAbilityScore(k, Integer.parseInt(value));
								}
							}
						}
					}

				} else if (tag.equals("SavingThrows")) {
					NodeList saves = e.getChildNodes();
					if (saves != null) {
						for (int j=0; j<saves.getLength(); j++) {
							if (!saves.item(j).getNodeName().equals("Save")) continue;
							Element s = (Element)saves.item(j);
							String value = s.getAttribute("value");
							String type = s.getAttribute("type");
							for (int k=0; k<save_names.length; k++) {
								if (save_names[k].equals(type)) {
									c.setSavingThrow(k, Integer.parseInt(value));
								}
							}
						}
					}

				} else if (tag.equals("Skills")) {
					NodeList skills = e.getChildNodes();
					if (skills != null) {
						for (int j=0; j<skills.getLength(); j++) {
							if (!skills.item(j).getNodeName().equals("Skill")) continue;
							Element s = (Element)skills.item(j);
							String value = s.getAttribute("value");
							String type = s.getAttribute("type");
							c.setSkill(type, Integer.parseInt(value));
						}
					}

				} else if (tag.equals("AC")) {
					NodeList acs = e.getChildNodes();
					if (acs != null) {
						for (int j=0; j<acs.getLength(); j++) {
							if (!acs.item(j).getNodeName().equals("ACComponent")) continue;
							Element s = (Element)acs.item(j);
							String value = s.getAttribute("value");
							String type = s.getAttribute("type");
							for (int k=0; k<ac_names.length; k++) {
								if (ac_names[k].equals(type)) {
									c.setACComponent(k, Integer.parseInt(value));
								}
							}
						}
					}
				}
			}
		}

		return c;
	}

	public String getXML() {
		return getXML("","    ");
	}

	public String getXML(String indent, String nextIndent) {
		StringBuilder b = new StringBuilder();
		String nl = System.getProperty("line.separator");
		String i2 = indent + nextIndent;
		String i3 = i2 + nextIndent;
		b.append(indent).append("<Character name=\"").append(getName()).append("\">").append(nl);
		b.append(i2).append("<AbilityScores>").append(nl);
		for (int i=0; i<ability_names.length; i++) {
			b.append(i3).append("<AbilityScore type=\"").append(ability_names[i]);
			b.append("\" value=\"").append(getBaseAbilityScore(i)).append("\"/>").append(nl);
		}
		b.append(i2).append("</AbilityScores>").append(nl);
		b.append(i2).append("<HitPoints maximum=\"").append(getMaximumHitPoints()).append("\"");
		if (getWounds() != 0) b.append(" wounds=\"").append(getWounds()).append("\"");
		if (getNonLethal() != 0) b.append(" non-lethal=\"").append(getNonLethal()).append("\"");
		b.append("/>").append(nl);
		b.append(i2).append("<Initiative value=\"").append(getBaseInitiative()).append("\"/>").append(nl);
		b.append(i2).append("<SavingThrows>").append(nl);
		for (int i=0; i<save_names.length; i++) {
			b.append(i3).append("<Save type=\"").append(save_names[i]);
			b.append("\" value=\"").append(getBaseSavingThrow(i)).append("\"/>").append(nl);
		}
		b.append(i2).append("</SavingThrows>").append(nl);
		b.append(i2).append("<Skills>").append(nl);
		for (String s : skills.keySet()) {
			if (getSkill(s) != 0) {
				b.append(i3).append("<Skill type=\"").append(s);
				b.append("\" value=\"").append(getSkillRanks(s)).append("\"/>").append(nl);
			}
		}
		b.append(i2).append("</Skills>").append(nl);
		b.append(i2).append("<AC>").append(nl);
		for (int i=0; i<ac_names.length; i++) {
			if (getACComponent(i) != 0) {
				b.append(i3).append("<ACComponent type=\"").append(ac_names[i]);
				b.append("\" value=\"").append(getACComponent(i)).append("\"/>").append(nl);
			}
		}
		b.append(i2).append("</AC>").append(nl);
		b.append(indent).append("</Character>").append(nl);
		return b.toString();
	}

	// Prints the differences between this and inChar
	public void showDiffs(Character inChar) {
		if (!name.equals(inChar.name)) {
			// new attribute
			System.out.println(PROPERTY_NAME+"|"+name+"|"+inChar.name);
		}
		if (hps != inChar.hps) {
			System.out.println(PROPERTY_MAXHPS+"|"+hps+"|"+inChar.hps);
		}
		if (wounds != inChar.wounds) {
			System.out.println(PROPERTY_WOUNDS+"|"+wounds+"|"+inChar.wounds);
		}
		if (nonLethal != inChar.nonLethal) {
			System.out.println(PROPERTY_NONLETHAL+"|"+nonLethal+"|"+inChar.nonLethal);
		}
		if (initModifier != inChar.initModifier) {
			System.out.println(PROPERTY_INITIATIVE+"|"+initModifier+"|"+inChar.initModifier);
		}
		for (int i=0; i<6; i++) {
			if (abilities[i] != inChar.abilities[i]) {
				System.out.println(PROPERTY_ABILITY_PREFIX+ability_names[i]+"|"+abilities[i]+"|"+inChar.abilities[i]);
			}
		}
		for (int i=0; i<6; i++) {
			if (tempAbilities[i] != inChar.tempAbilities[i]) {
				// new attribute
				System.out.println(PROPERTY_ABILITY_OVERRIDE_PREFIX+getAbilityName(i)+"|"+tempAbilities[i]+"|"+inChar.tempAbilities[i]);
			}
		}
		for (int i=0; i<3; i++) {
			if (saves[i] != inChar.saves[i]) {
				System.out.println(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(i)+"|"+saves[i]+"|"+inChar.saves[i]);
			}
		}
		for (int i=0; i<AC_MAX_INDEX; i++) {
			if (ac[i] != inChar.ac[i]) {
				// new attribute
				System.out.println(PROPERTY_AC_COMPONENT_PREFIX+Creature.getACComponentName(i)+"|"+ac[i]+"|"+inChar.ac[i]);
			}
		}
		HashSet<String> allSkills = new HashSet<String>(skills.keySet());
		allSkills.addAll(inChar.skills.keySet());
		for (String skill : allSkills) {
			if (getSkillRanks(skill) != inChar.getSkillRanks(skill)) {
				System.out.println(PROPERTY_SKILL_PREFIX+skill+"|"+getSkillRanks(skill)+"|"+inChar.getSkillRanks(skill));
			}
		}
	}

	// Returns a list of the properties that differ between this and inChar
	// The list is ordered by the type of property
	public List<String> getDifferences(Character inChar) {
		List<String> diffs = new ArrayList<String>();

		if (!name.equals(inChar.name)) diffs.add(PROPERTY_NAME);
		if (hps != inChar.hps) diffs.add(PROPERTY_MAXHPS);
		if (wounds != inChar.wounds) diffs.add(PROPERTY_WOUNDS);
		if (nonLethal != inChar.nonLethal) diffs.add(PROPERTY_NONLETHAL);
		if (initModifier != inChar.initModifier) diffs.add(PROPERTY_INITIATIVE);
		for (int i=0; i<6; i++) {
			if (abilities[i] != inChar.abilities[i]) {
				diffs.add(PROPERTY_ABILITY_PREFIX+getAbilityName(i));
			}
		}
		for (int i=0; i<6; i++) {
			if (tempAbilities[i] != inChar.tempAbilities[i]) {
				diffs.add(PROPERTY_ABILITY_OVERRIDE_PREFIX+getAbilityName(i));
			}
		}
		for (int i=0; i<3; i++) {
			if (saves[i] != inChar.saves[i]) {
				diffs.add(PROPERTY_SAVE_PREFIX+Creature.getSavingThrowName(i));
			}
		}
		for (int i=0; i<AC_MAX_INDEX; i++) {
			if (ac[i] != inChar.ac[i]) {
				diffs.add(PROPERTY_AC_COMPONENT_PREFIX+Creature.getACComponentName(i));
			}
		}
		HashSet<String> allSkills = new HashSet<String>(skills.keySet());
		allSkills.addAll(inChar.skills.keySet());
		for (String skill : allSkills) {
			if (getSkillRanks(skill) != inChar.getSkillRanks(skill)) {
				diffs.add(PROPERTY_SKILL_PREFIX+skill);
			}
		}
		return diffs;
	}

	public Object getProperty(String prop) {
		if (prop.equals(PROPERTY_NAME)) return name;
		if (prop.equals(PROPERTY_MAXHPS)) return hps;
		if (prop.equals(PROPERTY_WOUNDS)) return wounds;
		if (prop.equals(PROPERTY_NONLETHAL)) return nonLethal;
		if (prop.equals(PROPERTY_INITIATIVE)) return initModifier;
		
		if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(Creature.getAbilityName(i))) return abilities[i];
			}
		}

		if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(Creature.getAbilityName(i))) return tempAbilities[i];
			}
		}

		if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(Creature.getSavingThrowName(i))) return saves[i];
			}
		}

		if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (int i=0; i<Creature.AC_MAX_INDEX; i++) {
				if (comp.equals(Creature.getACComponentName(i))) return ac[i];
			}
		}

		if (prop.startsWith(PROPERTY_SKILL_PREFIX)) {
			String skill = prop.substring(PROPERTY_SKILL_PREFIX.length());
			Integer ranks = skills.get(skill);
			if (ranks != null) return ranks;
		}

		return null;
	}

	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_NAME)) setName((String)value);
		if (prop.equals(PROPERTY_MAXHPS)) setMaximumHitPoints((Integer)value);
		if (prop.equals(PROPERTY_WOUNDS)) setWounds((Integer)value);
		if (prop.equals(PROPERTY_NONLETHAL)) setNonLethal((Integer)value);
		if (prop.equals(PROPERTY_INITIATIVE)) setInitiativeModifier((Integer)value);
		
		if (prop.startsWith(PROPERTY_ABILITY_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(Creature.getAbilityName(i))) setAbilityScore(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_ABILITY_OVERRIDE_PREFIX)) {
			String ability = prop.substring(PROPERTY_ABILITY_OVERRIDE_PREFIX.length());
			for (int i=0; i<6; i++) {
				if (ability.equals(Creature.getAbilityName(i))) setTemporaryAbility(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_SAVE_PREFIX)) {
			String save = prop.substring(PROPERTY_SAVE_PREFIX.length());
			for (int i=0; i<3; i++) {
				if (save.equals(Creature.getSavingThrowName(i))) setSavingThrow(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_AC_COMPONENT_PREFIX)) {
			String comp = prop.substring(PROPERTY_AC_COMPONENT_PREFIX.length());
			for (int i=0; i<Creature.AC_MAX_INDEX; i++) {
				if (comp.equals(Creature.getACComponentName(i))) setACComponent(i, (Integer)value);
			}
		}

		if (prop.startsWith(PROPERTY_SKILL_PREFIX)) {
			String skill = prop.substring(PROPERTY_SKILL_PREFIX.length());
			if (value == null) {
				deleteSkill(skill);
			} else {
				setSkill(skill, (Integer)value);
			}
		}
	}
}

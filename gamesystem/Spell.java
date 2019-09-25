package gamesystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	public String getHTML() {
		StringBuilder s = new StringBuilder();
		s.append("<html><body style=\"width:300px;\">");
		s.append("<h1>").append(name).append("</h1>");
		if (school != null && school.length() > 0) s.append(school).append("<br/>");
		if (level != null && level.length() > 0) s.append("<b>Level:</b>").append(level).append("<br/>");
		if (components != null && components.length() > 0) s.append("<b>Components:</b>").append(components).append("<br/>");
		if (castingTime != null && castingTime.length() > 0) s.append("<b>Casting Time:</b>").append(castingTime).append("<br/>");
		if (range != null && range.length() > 0) s.append("<b>Range:</b>").append(range).append("<br/>");
		if (effect != null && effect.length() > 0) s.append("<b>Effect:</b>").append(effect).append("<br/>");
		if (duration != null && duration.length() > 0) s.append("<b>Duration:</b>").append(duration).append("<br/>");
		if (savingThrow != null && savingThrow.length() > 0) s.append("<b>Saving Throw:</b>").append(savingThrow).append("<br/>");
		if (spellResistance != null && spellResistance.length() > 0) s.append("<b>Spell Resistance:</b>").append(spellResistance).append("<br/>");
		if (description != null && description.length() > 0) s.append("<p>").append(description).append("</p>");
		if (material != null && material.length() > 0) s.append("<p><i>Material Component:</i><br/>").append(material).append("</p>");
		if (focus != null && focus.length() > 0) s.append("<p><i>Focus:</i><br/>").append(focus).append("</p>");
		if (xpCost != null && xpCost.length() > 0) s.append("<p><i>XP Cost:</i><br/>").append(xpCost).append("</p>");
		s.append("</body></html>");
		return s.toString();
	}
}

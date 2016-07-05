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
}

package gamesystem;

import java.util.HashMap;
import java.util.Map;

public class Spell {
	String name;
	String school;
	String level;
	String components;
	String castingTime;
	String range;
	String effect;
	String duration;
	String savingThrow;
	String spellResistance;
	String description;
	String material;
	String focus;
	String xpCost;
	Map<String, BuffFactory> buffFactories = new HashMap<>();
}

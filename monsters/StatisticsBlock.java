package monsters;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gamesystem.AbilityScore;
import gamesystem.AbilityScore.Type;
import gamesystem.CharacterClass;
import gamesystem.ImmutableModifier;
import gamesystem.Modifier;
import gamesystem.MonsterType;
import gamesystem.SavingThrow;
import gamesystem.SizeCategory;
import gamesystem.dice.HDDice;
import monsters.StatisticsBlock.AttackRoutine.Attack;
import util.LocalEntityResolver;

/**
 * Represents a parsed statistics block from a source (usual an HTML table). The values stored in the
 * StatisticsBlock are accessed via keys (Field values). The raw values are strings which may contain
 * any text. Specific getX() methods attempt to further parse concrete values from these strings.
 *
 * This is an immutable type.
 *
 * @author Steve
 *
 */

public class StatisticsBlock {
	public enum Field {
		NAME("Name:"),
		URL("URL:"),

		SIZE_TYPE("Size/Type:", "size-type"),
		CLASS_LEVELS("Class Levels:"),
		HITDICE("Hit Dice:", "hitdice"),
		INITIATIVE("Initiative:", "initiative"),
		SPEED("Speed:", "speed"),
		AC("Armor Class:", "ac"),
		BASE_ATTACK_GRAPPLE("Base Attack/Grapple:", "bab-grapple"),
		ATTACK("Attack:", "attack"),
		FULL_ATTACK("Full Attack:", "full-attack"),
		SPACE_REACH("Space/Reach:", "space-reach"),
		SPECIAL_ATTACKS("Special Attacks:", "special-attacks"),
		SPECIAL_QUALITIES("Special Qualities:", "special-qualities"),
		SAVES("Saves:", "saves"),
		ABILITIES("Abilities:", "abilities"),
		SKILLS("Skills:", "skills"),
		FEATS("Feats:", "feats"),
		ENVIRONMENT("Environment:", "environment"),
		ORGANIZATION("Organization:", "ogranization"),
		CR("Challenge Rating:", "cr"),
		TREASURE("Treasure:", "treasure"),
		ALIGNMENT("Alignment:", "alignment"),
		ADVANCEMENT("Advancement:", "advancement"),
		LEVEL_ADJUSTMENT("Level Adjustment:", "level-adjustment");
//		'Type:' - from the dragon pages

		@Override
		public String toString() {return label;}

		public static Field fromLabel(String p) {
			// TODO more efficient implementation
			for (Field prop : Field.values()) {
				if (prop.label.equals(p)) return prop;
			}
			return null;
		}

		public static Field fromTagName(String t) {
			// TODO more efficient implementation
			for (Field prop : Field.values()) {
				if (prop.tag != null && prop.tag.equals(t)) return prop;
			}
			return null;
		}

		public static Field[] getStandardOrder() {
			return Arrays.copyOf(standardOrder, standardOrder.length);
		}

		private static final Field[] standardOrder = {
				SIZE_TYPE,
				HITDICE,
				INITIATIVE,
				SPEED,
				AC,
				BASE_ATTACK_GRAPPLE,
				ATTACK,
				FULL_ATTACK,
				SPACE_REACH,
				SPECIAL_ATTACKS,
				SPECIAL_QUALITIES,
				SAVES,
				ABILITIES,
				SKILLS,
				FEATS,
				ENVIRONMENT,
				ORGANIZATION,
				CR,
				TREASURE,
				ALIGNMENT,
				ADVANCEMENT,
				LEVEL_ADJUSTMENT
		};

		private Field(String l) {
			label = l;
		}

		private Field(String l, String t) {
			label = l;
			tag = t;
		}

		private String label;
		private String tag;
	}

	static final String STATBLOCKCLASS = "statBlock";
	static final String IMAGECLASS = "monsterImage";

	private Source source;
	private Map<Field, String> fields = new HashMap<>();
	private URL[] images = null;

	public String get(Field key) {
		return fields.get(key);
	}

	public String getName() {
		return get(Field.NAME);
	}

	URL getURL() throws MalformedURLException {
		return new URL(get(Field.URL));
	}

	// type of the creature
	// field has format "<Size> <Type> [(Subtypes)]"
	// returns null if the field has the incorrect format or if the type is unknown
	MonsterType getType() {
		String sizeType = get(Field.SIZE_TYPE);
		if (sizeType == null || sizeType.indexOf(' ') < 1) return null;
		sizeType = sizeType.substring(sizeType.indexOf(' ')+1);
		if (sizeType.indexOf('(') > 1) {
			sizeType = sizeType.substring(0, sizeType.indexOf('(')).trim();
		}
		return MonsterType.getMonsterType(sizeType);
	}

	// Returns the original type as specified in the "Augmented..." subtype, if any
	MonsterType getAugmentedType() {
		for (String subtype : getSubtypes()) {
			if (subtype.startsWith("Augmented ")) {
				return MonsterType.getMonsterType(subtype.substring(10));
			}
		}
		return null;
	}

	// always returns a valid list (which may be empty)
	List<String> getSubtypes() {
		List<String> subtypes = new ArrayList<>();

		String sizeType = get(Field.SIZE_TYPE);
		if (sizeType == null || sizeType.indexOf('(') < 0) return subtypes;
		sizeType = sizeType.substring(sizeType.indexOf('(') + 1);
		if (sizeType.indexOf(')') >= 0) {
			sizeType = sizeType.substring(0, sizeType.indexOf(')'));
		}

		for (String subtype : sizeType.split("\\s*,\\s*")) {
			subtypes.add(subtype);
		}
		return subtypes;
	}

	/**
	 * Parses the SIZE_TYPE field value and returns the size category (which is the first word of the field).
	 * If SIZE_TYPE can't be parsed then null is returned
	 *
	 * @return the SizeCategory of the creature or null
	 */
	public SizeCategory getSize() {
		String sizeType = get(Field.SIZE_TYPE);
		if (sizeType == null || sizeType.indexOf(' ') < 1) return null;
		return SizeCategory.getSize(sizeType.substring(0, sizeType.indexOf(' ')));
	}

	/**
	 * Parses the SPACE_REACH field value and returns the space taken by the creature in 6" units.
	 * The SPACE_REACH value should be of the form "X ft./...". X should be an integer or "2 1/2" or "2½" or "1/2" or "½".
	 * If SPACE_REACH has no value or it can't be parsed to extract the space value then -1 is returned
	 *
	 * @return the space taken by the creature in 6" units or -1 if the SPACE_REACH field can't be parsed
	 */
	public int getSpace() {
		String space = get(Field.SPACE_REACH);
		if (space == null || space.indexOf(" ft./") < 1) return -1;
		space = space.substring(0, space.indexOf(" ft./"));
		if (space.equals("1/2") || space.equals("½")) return 1;
		if (space.equals("2 1/2") || space.equals("2½")) return 5;
		int s = -1;
		try {
			s = Integer.parseInt(space) * 2;
		} catch (NumberFormatException e) {
			// will default to -1 if we can't parse
		}
		return s;
	}

	/**
	 * Parses the SPACE_REACH field value and returns the normal reach of the creature in feet.
	 * The SPACE_REACH value should be of the form ".../X ft....". X should be an integer.
	 * Any additional information (e.g. "20 ft. with tentacles") is ignored.
	 * If SPACE_REACH has no value or it can't be parsed to extract the reach value then -1 is returned
	 *
	 * @return the reach of the creature in feet or -1 if the SPACE_REACH field can't be parsed
	 */
	public int getReach() {
		String reach = get(Field.SPACE_REACH);
		if (reach == null || reach.indexOf(" ft./") < 1) return -1;
		reach = reach.substring(reach.indexOf(" ft./") + 5);
		if (reach.indexOf(" ft.") < 1) return -1;
		reach = reach.substring(0, reach.indexOf(" ft."));
		int s = -1;
		try {
			s = Integer.parseInt(reach);
		} catch (NumberFormatException e) {
			// will default to -1 if we can't parse
		}
		return s;
	}

	// format of field is:
	// Str 25, Dex 10, Con —, Int 1, Wis 11, Cha 1
	// returns -1 for a missing ability
	// TODO should throw exceptions for invalid formats (or at least return -1)
	static int parseAbilityScore(String field, Type ability) {
		String[] abilities = field.split("\\s*,\\s*");
		String a = abilities[ability.ordinal()].substring(abilities[ability.ordinal()].indexOf(' ')+1);
		if (a.equals("-") || a.equals("—") || a.equals("Ø")) return -1;
		if (a.endsWith("*")) a = a.substring(0, a.length() - 1);	// strip any asterisk
		return Integer.parseInt(a);
	}

	public int getAbilityScore(Type ability) {
		return parseAbilityScore(get(Field.ABILITIES), ability);
	}

	// format of field is:
	// Fort +2, Ref +6, Will +1
	// returns Integer.MIN_VALUE for a missing save
	// TODO should throw exceptions for invalid formats (or at least return -1)
	static int parseSavingThrow(String field, SavingThrow.Type save) {
		String[] saves = field.split("\\s*,\\s*");
		String s = saves[save.ordinal()].substring(saves[save.ordinal()].indexOf(' ') + 1);
		if (s.contains(" ")) s = s.substring(0, s.indexOf(' '));	// strip any conditional version
		if (s.endsWith("*")) s = s.substring(0, s.length() - 1);	// strip any asterisk
		if (s.equals("—")) return Integer.MIN_VALUE;
		return parseModifier(s);
	}

	public int getSavingThrow(SavingThrow.Type save) {
		return parseSavingThrow(get(Field.SAVES), save);
	}

//	private CR getCR() {
//		String s = get(Field.CR);
//		if (s.equals("¼")) s = "1/4";
//		if (s.equals("½")) s = "1/2";
//		try {
//			return new CR(s);
//		} catch (NumberFormatException e) {
//			// failed to parse, generally this means there was a note in the CR field
//			// we could try to parse the initial number, but for now we'll just return null
//			//System.out.println("Failed to parse '"+s+"' as CR");
//			return null;
//		}
//	}

	Source getSource() {
		return source;
	}

	// TODO should return a copy
	URL[] getImageURLs() {
		return images;
	}

	// for completeness
	static int parseInitiativeModifier(String field) {
		return parseModifier(field);
	}

	public int getInitiativeModifier() {
		String init = get(Field.INITIATIVE);
		if (init == null) {
			System.out.println("WARN: "+getName()+" has no initiative");
			return 0;
		}
		if (init.contains("(")) init = init.substring(0, init.indexOf("(") - 1);
		return parseModifier(init);
	}

	static int parseBAB(String field) {
		return parseModifier(field.substring(0, field.indexOf('/')));
	}

	// returns the parsed BAB.
	public int getBAB() {
		return parseBAB(get(Field.BASE_ATTACK_GRAPPLE));
	}

	// returns the parsed grapple modifier
	// TODO perhaps better to return Integer and use null as guard value?
	public int getGrapple() {
		String field = get(Field.BASE_ATTACK_GRAPPLE);
		field = field.substring(field.indexOf('/') + 1);
		if (field.contains(" (")) field = field.substring(0, field.indexOf(" ("));
		if (field.contains(",")) field = field.substring(0, field.indexOf(","));
		if (field.equals("—")) return Integer.MIN_VALUE;
		return parseModifier(field);
	}

	public List<Modifier> getGrappleModifiers() {
		String field = get(Field.BASE_ATTACK_GRAPPLE);
		field = field.substring(field.indexOf('/') + 1);	// get the grapple part of the field
		String[] variants = field.split(",(?![^()]*+\\))");	// split on commas that aren't in parentheses

		List<Modifier> mods = new ArrayList<>();
		for (int i = 0; i < variants.length; i++) {
			if (variants[i].contains("(")) {
				String variant = variants[i].trim();
				String condition = null;
				if (variant.indexOf(" ") + 1 < variant.indexOf("(")) {
					condition = variant.substring(variant.indexOf(" ") + 1, variant.indexOf("(")).trim();
				}
				String[] modStrs = variant.substring(variant.indexOf("(") + 1, variant.indexOf(")")).split(",");
				for (String mod : modStrs) {
					mod = mod.trim();
					int val = parseModifier(mod.substring(0, mod.indexOf(" ")));
					String type = mod.substring(mod.indexOf(" ") + 1).trim();
					ImmutableModifier m = new ImmutableModifier(val, type, null, condition);
					mods.add(m);
				}
			}
		}
		return mods;
	}

	// parse class levels:
	// pattern is "<name> <level>[, <name level]*"
	Map<CharacterClass, Integer> parseClassLevels(String classLevelStr) {
		Map<CharacterClass, Integer> classLevels = new HashMap<>();

		if (classLevelStr != null && !classLevelStr.equals("—")) {
			for (String classLevel : classLevelStr.split("\\s*,\\s+")) {
				String[] pieces = classLevel.split("\\s+");
				CharacterClass charClass = CharacterClass.getCharacterClass(pieces[0]);
				int level = Integer.parseInt(pieces[1]);
				classLevels.put(charClass, level);
			}
		}
		return classLevels;
	}

	public Map<CharacterClass, Integer> getClassLevels() {
		return parseClassLevels(get(Field.CLASS_LEVELS));
	}

	// parse hitdice:
	// pattern for a dice roll is "#d#[+#]"
	// multiple dice rolls can be separated by " plus "
	// hitdice section ends with " (# hp)"
	// first number may be "½ "
	List<HDDice> parseHitDice(String hd) {
		hd = hd.substring(0, hd.indexOf(" ("));
		return HDDice.parseList(hd);
	}

	public List<HDDice> getHitDice() {
		String hd = get(Field.HITDICE);
		if (hd == null || hd.indexOf(" (") < 0) {
			System.out.println("WARN: "+getName()+" has no default hp ending hitdice");
			return null;
		}
		return parseHitDice(hd);
	}

	// parse default hitpoints:
	// pattern is "<hitdice> (# hp)"
	public int getDefaultHPs() {
		int hp = 0;
		String hps = get(Field.HITDICE);
		if (hps != null && hps.indexOf(" (") > 0 && hps.indexOf(" hp)") > 0) {
			hps = hps.substring(hps.indexOf(" (")+2,hps.indexOf(" hp)"));
			//System.out.println(block.get("Name:")+"HPs: "+hps);
			try {
				hp = Integer.parseInt(hps);
			} catch (NumberFormatException e) {
				System.out.println(getName()+": "+e);
			}

		} else {
			System.out.println("WARN: "+getName()+" has no default hp entry");
		}
		return hp;
	}

	// parse armor class:
	// pattern is "# (components), touch #, flat-footed #"
	// returns an array of integers: full, touch, and flat-footed ac totals respectively
	// if multiple ac versions appear (separated by " or ") then the first one is parsed and returned
	public int[] getACs() {
		String acProp = get(Field.AC);
		if (acProp == null) {
			System.out.println("WARN: " + getName() + " has no AC");
			return new int[3];
		}
		return parseACs(acProp);
	}

	// this version parses the first of any alternate ACs
	static int[] parseACs(String acProp) {
		String[] acStrs = acProp.split("\\s+or\\s+");
		if (acStrs[0].endsWith(",")) acStrs[0] = acStrs[0].substring(0, acStrs[0].length() - 1);	// strip any trailing ','

		int[] acs = new int[3];

		int i = acStrs[0].indexOf(", touch ");
		if (i == -1) {
			throw new IllegalArgumentException("Could not locate ', touch ' in '" + acStrs[0] + "'");
		}
		int j = acStrs[0].indexOf(", flat-footed ");
		if (j == -1) {
			throw new IllegalArgumentException("Could not locate ', flat-footed ' in '" + acStrs[0] + "'");
		}
		String fullAC = acStrs[0].substring(0, i);
		if (fullAC.indexOf(" (") > -1) {
			fullAC = fullAC.substring(0, fullAC.indexOf(" ("));
		}
		String touchAC = acStrs[0].substring(i + 8, j);
		String ffAC = acStrs[0].substring(j + 14);

		acs[0] = Integer.parseInt(fullAC);
		acs[1] = Integer.parseInt(touchAC);
		acs[2] = Integer.parseInt(ffAC);

		return acs;
	}

	private static final Map<String, String> acComponentTypes = new HashMap<>();
	{
		acComponentTypes.put("dex", AbilityScore.Type.DEXTERITY.name());
		acComponentTypes.put("wis", AbilityScore.Type.WISDOM.name());
		acComponentTypes.put("armor", Modifier.StandardType.ARMOR.toString());
		acComponentTypes.put("deflection", Modifier.StandardType.DEFLECTION.toString());
		acComponentTypes.put("dodge", Modifier.StandardType.DODGE.toString());
		acComponentTypes.put("natural", Modifier.StandardType.NATURAL_ARMOR.toString());
		acComponentTypes.put("profane", Modifier.StandardType.PROFANE.toString());
		acComponentTypes.put("shield", Modifier.StandardType.SHIELD.toString());
		acComponentTypes.put("size", Modifier.StandardType.SIZE.toString());
	}

	// returns the components for the first full ac section found
	// format for the ac line is:
	// <fullac>, touch <touchac>, flat-footed <flatfootedac>
	// each of <fullac>, <touchac>, and <flatfootedac> can have multiple versions separated by " or "
	// <touchac> and <flatfootedac> are simple integers
	// each version of <fullac> has the format "<value>[ (<component>[, component]*)]"
	// <value> is a simple integer
	// <component> has the format "<modifier> <type>[ (<description)]"
	// <modifier> is a simple integer (optionally having '+' before non-negative values)
	// <type> is a string which should be one of the standard Modifier types
	// <description> is a string
	public Set<Modifier> getACModifiers() {
		String acProp = get(Field.AC);
		if (acProp == null) {
			System.out.println("WARN: " + getName() + " has no AC");
			return null;
		}

		return parseACModifiers(acProp);
	}

	private static int parseModifier(String valueStr) {
		if (valueStr.startsWith("+")) valueStr = valueStr.substring(1);
		valueStr = valueStr.replace('–', '-');	// replace non-standard minus signs
		return Integer.parseInt(valueStr);
	}

	static Set<Modifier> parseACModifiers(String acProp) {
		String[] acStrs = acProp.split("\\s+or\\s+");
		if (acStrs[0].endsWith(",")) acStrs[0] = acStrs[0].substring(0, acStrs[0].length() - 1);	// strip any trailing ','

		Set<Modifier> components = new HashSet<>();

		int i = acStrs[0].indexOf(", touch ");
		if (i == -1) {
			throw new IllegalArgumentException("Couldn't locate ', touch ' in '" + acStrs[0] + "'");
		}

		String fullAC = acStrs[0].substring(0, i);
		if (fullAC.indexOf(" (") > -1) {
			try {
				String componentStr = fullAC.substring(fullAC.indexOf(" (") + 2, fullAC.lastIndexOf(')'));
				String[] componentsStr = componentStr.split("\\s*,\\s*");
				for (String component : componentsStr) {
					int mod = parseModifier(component.substring(0, component.indexOf(' ')));
					String type = component.substring(component.indexOf(' ') + 1);
					String desc = null;
					if (type.indexOf(" (") >= 0) {
						desc = type.substring(type.indexOf(" (") + 2, type.lastIndexOf(')'));
						type = type.substring(0, type.indexOf(" ("));
					}
					if (acComponentTypes.containsKey(type.toLowerCase())) {
						type = acComponentTypes.get(type.toLowerCase());
					}
					ImmutableModifier m = new ImmutableModifier(mod, type, desc);
					components.add(m);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Couldn't parse AC: '" + acStrs[0] + "'", e);
			}
		}
		return components;
	}

	// <desc> '+|-'<bonus>[/'+|-'<bonus>...] [(<modifiers>) ]'melee|ranged'[ 'touch'] (<damage>)
	static final private Pattern stdAttackPattern = Pattern.compile("(.*)\\s+([+-–]\\d+(\\/[+-]\\d+)*)\\s+(\\((.*)\\)\\s+)?(melee|ranged)(\\stouch)?\\s\\((.*)\\)");
	static final private Pattern autoHitPattern = Pattern.compile("(.*)\\s+\\((.*)\\)");
	static final private Pattern routineSep = Pattern.compile(",?\\s+(and|plus)\\s+");

	public List<AttackRoutine> getAttacks(boolean full) {
		String prop = get(full ? Field.FULL_ATTACK : Field.ATTACK);
		return parseAttacks(prop, new CreatureDetails() {
			@Override
			public int getDexterity() {
				return getAbilityScore(AbilityScore.Type.DEXTERITY);
			}

			@Override
			public int getStrength() {
				return getAbilityScore(AbilityScore.Type.STRENGTH);
			}

			@Override
			public String getFeats() {
				return get(Field.FEATS);
			}

			@Override
			public String getSpecialQualities() {
				return get(Field.SPECIAL_QUALITIES);
			}

			@Override
			public int getBAB() {
				return StatisticsBlock.this.getBAB();
			}

			@Override
			public SizeCategory getSize() {
				return StatisticsBlock.this.getSize();
			}
		});
	}

	static List<AttackRoutine> parseAttacks(String prop, CreatureDetails creature) {
		List<AttackRoutine> attackRoutines = new ArrayList<>();

		prop = prop.replaceAll("\\(([^\\)]*)\\s+or\\s+", "($1 _OR_ ");	// temporarily translate any " or " found inside parentheses (i.e. inside a damage section)
		prop = prop.replaceAll("\\(([^\\)]*)\\s+and\\s+", "($1 _AND_ ");	// temporarily translate any " and " found inside parentheses (i.e. inside a damage section)
		prop = prop.replaceAll("\\(([^\\)]*)\\s+plus\\s+", "($1 _PLUS_ ");	// temporarily translate any " plus " found inside parentheses (i.e. inside a damage section)
		prop = prop.replaceAll("\\(([^\\)]*)\\s+plus\\s+", "($1 _PLUS_ ");	// TODO really should just test these repeatedly
		prop = prop.replaceAll("\\(([^\\)]*)\\s+plus\\s+", "($1 _PLUS_ ");

		for (String attackStr : prop.split("[;,]*\\s+or\\s+")) {
			AttackRoutine attackRoutine = new AttackRoutine(creature);

			Matcher m = routineSep.matcher(attackStr);
			List<String> attackForms = new ArrayList<>();
			int routineStart = 0;
			String lastSep = "plus ";	// so first attack will be marked as primary
			while (m.find()) {
				attackForms.add(lastSep + attackStr.substring(routineStart, m.start()));
				lastSep = m.group(1) + " ";
				routineStart = m.end();
			}
			attackForms.add(lastSep + attackStr.substring(routineStart));

			for (String value : attackForms) {
				String separator = value.substring(0, value.indexOf(' '));
				value = value.substring(value.indexOf(' ') + 1);

				// {desc} {bonus}[/{bonus}...] ('melee'|'ranged') '('{damage}')'
				// {desc} '('{damage}')'
				// '—'
				String cleanValue = value.replaceAll("\\*", "");	// strip out any '*' in the attack
				//System.out.println("'" + value + "':");

				if (value.equals("—")) continue;

				Attack attack = attackRoutine.new Attack();
				if (separator.equals("plus")) attack.primary = true;

				Matcher matcher = stdAttackPattern.matcher(cleanValue);
				if (matcher.find()) {
					// groups are:
					// 1: description
					// 2: attack bonuses
					// 3: last attack bonus
					// 4: modifiers to attack (including parentheses)
					// 5: modifiers to attack
					// 6: attack type (melee or ranged)
					// 7: ' touch' if touch attack
					// 8: damage description
//					System.out.println(cleanValue);
//					for (int i = 0; i <= matcher.groupCount(); i++) {
//						System.out.println("" + i + ": " + matcher.group(i));
//					}

					attack.setDescription(matcher.group(1));
					String[] bonuses = matcher.group(2).split("\\/");
					attack.attackBonuses = new int[bonuses.length];
					for (int i = 0; i < bonuses.length; i++) {
						attack.attackBonuses[i] = parseModifier(bonuses[i]);
					}
					attack.setModifiers(matcher.group(5), false);
					attack.ranged = matcher.group(6).equals("ranged");
					attack.setTouch(matcher.group(7) != null);
					attack.setDamage(matcher.group(8).replace("_OR_", "or").replace("_AND_", "and").replace("_PLUS_", "plus"));

				} else {
					matcher = autoHitPattern.matcher(cleanValue);
					if (matcher.find()) {
						attack.description = matcher.group(1);
						attack.automatic = true;
						attack.setDamage(matcher.group(2).replace("_OR_", "or").replace("_AND_", "and").replace("_PLUS_", "plus"));
					}
				}

				attackRoutine.addAttack(attack);
			}

			if (attackRoutine != null) {
				attackRoutines.add(attackRoutine);
			}
		}
		return attackRoutines;
	}

	static final Pattern modPattern = Pattern.compile("[+-](\\d+)\\s+(.*)");
	// [<num> ]['+'<bonus>]<desc>[(<modifiers>)] though note the enhancement bonus is parse in calculateAttackBonus
	static final Pattern descPattern = Pattern.compile("((\\d+)\\s+)?([^\\(]*)(\\((.*)\\))?");
	static final Pattern wfPattern = Pattern.compile("weapon focus \\(([^\\)]+)\\)");
	static final Pattern dmgPattern = Pattern.compile("(\\d+)d(\\d+)([+-]\\d+)?(\\/(\\d+)[-]20)?(\\/[x×](\\d+))?");
	static final Pattern byWeapPattern = Pattern.compile("by weapon( ([+-]\\d+))?");
	static final Pattern wsPattern = Pattern.compile("weapon specialization \\(([^\\)]+)\\)");

	static final Set<String> naturalWeapons = new HashSet<>();
	{
		naturalWeapons.add("bite");
		naturalWeapons.add("claw");
		naturalWeapons.add("pincers");
		naturalWeapons.add("talon");
		naturalWeapons.add("gore");
		naturalWeapons.add("slam");
		naturalWeapons.add("slap");
		naturalWeapons.add("sting");
		naturalWeapons.add("tentacle");
		naturalWeapons.add("wings");	// avoid matching 'throwing axe'
		naturalWeapons.add("hooves");
		naturalWeapons.add("tail");
		naturalWeapons.add("horn");
		naturalWeapons.add("stamp");
		naturalWeapons.add("quill");
		naturalWeapons.add("arm");
		naturalWeapons.add("ram");
		naturalWeapons.add("spikes");	// avoid matching 'spiked'
		naturalWeapons.add("snake");
		naturalWeapons.add("head butt");
		naturalWeapons.add("tendril");
	}

	static final Map<String, String> plurals = new HashMap<>();
	{
		plurals.put("hoof", "hooves");
	}

	// information required by the attack parsing classes.
	static interface CreatureDetails {
		int getDexterity();

		int getStrength();

		String getFeats();	// comma separated

		String getSpecialQualities();	// comma separated

		int getBAB();

		SizeCategory getSize();
	}

	static class MonsterDetails implements CreatureDetails {
		private Monster creature;

		public MonsterDetails(Monster m) {
			creature = m;
		}

		@Override
		public int getDexterity() {
			return creature.getAbilityStatistic(AbilityScore.Type.DEXTERITY).getValue();
		}

		@Override
		public int getStrength() {
			return creature.getAbilityStatistic(AbilityScore.Type.STRENGTH).getValue();
		}

		@Override
		public String getFeats() {
			return (String) creature.getPropertyValue(Field.FEATS.name());
		}

		@Override
		public String getSpecialQualities() {
			return (String) creature.getPropertyValue(Field.SPECIAL_QUALITIES.name());
		}

		@Override
		public int getBAB() {
			return creature.getBAB().getValue();
		}

		@Override
		public SizeCategory getSize() {
			return creature.getSizeStatistic().getSize();
		}
	};

	static class AttackRoutine {
		CreatureDetails creature;

		class Attack {
			String description;
			int number = 1;
			int[] attackBonuses;
			Map<String, Integer> modifiers = new HashMap<>();	// attack modifiers
			boolean ranged;
			boolean touch;
			boolean automatic = false;
			boolean primary = false;
			boolean manufactured = true;	// evenutally change this to link to the weapon details
			boolean non_light = false;	// valid only for secondary manufactured weapons - set in AttackRoutine.addAttack

			// calculated in calculateAttackBonus():
			boolean weaponFinesseApplies = false;	// true if the creature has weapon finesse and it's being applied to this attack
			boolean weaponFocusApplies = false;
			int enhancementBonus = 0;
			boolean masterwork = false;

			String damage;	// first part of the damage description - this is the "regular" damage part (the full specifier including bonus and critical)
			String damageExtra = "";	// and extra damage (this part is unaffected by changes to the creature's stats)
			boolean damageParsed = false;	// true if 'damage' string was successfully parsed
			Map<String, Integer> damageModifiers = new HashMap<>();	// damage modifiers
			int damageBonus = 0;
			int strMultiplier;
			String damageDice;	// string description of the regular damage dice (if any)
			String damageCritical;	// critical description for regular damage
			int strLimit = Integer.MAX_VALUE;
			boolean weaponSpecApplies = false;
			boolean byWeapon = false;

			void setDamage(String d) {
				damage = d.replace("–", "-");

				// get the first part of the damage string
				int idx = damage.indexOf(" plus ");
				if (idx == -1 || (damage.indexOf(" or ") >= 0 && damage.indexOf(" or ") < idx)) {
					idx = damage.indexOf(" or ");
				}
				if (idx >= 0) {
					damageExtra = damage.substring(idx);
					damage = damage.substring(0, idx);
				}

				// try to match the string
				Matcher byWeapMatcher = byWeapPattern.matcher(damage);
				Matcher dmgMatcher = dmgPattern.matcher(damage);
				// groups:
				// 1: num dice
				// 2: dice type
				// 3: bonus
				// 4: full crit range
				// 5: crit range low value
				// 6: full crit multiplier
				// 7: crit multiplier value
				if (!touch && dmgMatcher.find()) {
					String type = null;
					if (dmgMatcher.end() < damage.length() - 1) {
						type = damage.substring(dmgMatcher.end());
					}
					// if type is empty or " nonlethal" then this is regular damage
					if (type == null || type.equals(" nonlethal")) {
						// regular damage - process groups
						damageParsed = true;
						damageDice = dmgMatcher.group(1) + "d" + dmgMatcher.group(2);
						damageCritical = dmgMatcher.group(4);
						if (damageCritical != null) {
							if (dmgMatcher.group(6) != null) damageCritical += dmgMatcher.group(6);
						} else {
							damageCritical = dmgMatcher.group(6);
						}
						if (dmgMatcher.group(3) != null) damageBonus = parseModifier(dmgMatcher.group(3));
						// TODO remaining damage components

						if (type != null) damageExtra = type + damageExtra;

					} else {
						// typed damage - no regular damage
						damageExtra = damage + damageExtra;
						damage = "";
					}

				} else if (byWeapMatcher.find()) {
					String bonus = byWeapMatcher.group(2);
					if (bonus != null) {
						if (bonus.startsWith("+")) bonus = bonus.substring(1);
						damageBonus = Integer.parseInt(bonus);
						damageParsed = true;
						byWeapon = true;
					}

				} else {
					// irregular damage
					// TODO handle "0" and "1" as real damage
					damageExtra = damage + damageExtra;
					damage = "";
				}

				// if we managed to parse the damage descriptor we can now try to figure out the bonuses
				if (damageParsed && !automatic) {
					calculateAttackBonus();	// TODO need enhancement bonus

					int strMod = AbilityScore.getModifier(creature.getStrength());

					String feats = creature.getFeats();
					if (feats == null) feats = "";
					feats = feats.toLowerCase();

					Set<String> weaponSpecs = new HashSet<>();
					Matcher matcher = wsPattern.matcher(feats);
					while (matcher.find()) {
						weaponSpecs.add(matcher.group(1));
//						System.out.println(block.getName() + ": weapon focus = " + weaponFocus);
					}

					// default strength bonus:
					// there are exceptions: some secondary natural attacks get full bonus or even 1.5,
					// most ranged projectile weapons get no bonus
					strMultiplier = 1;		// half bonus for secondary attacks
					if (primary) {
						if (attacks.size() == 1 && number == 1)
							strMultiplier = 3;	// 1.5x bonus for single primary attack with no secondary attack
						else
							strMultiplier = 2;	// 1x bonus for primary attacks with secondary attacks
					}

					int mods = enhancementBonus;

					// check for weapon focus
					for (String weaponSpec : weaponSpecs) {
						if (description.toLowerCase().contains(weaponSpec)
//								|| plurals.containsKey(weaponFocus) && desc.contains(plurals.get(weaponFocus))
								) {
//								System.out.println(block.getName() + ": weapon focus (" + desc + ")");
							mods += 2;
							weaponSpecApplies = true;
						}
					}

					// apply any modifers
					for (String m : damageModifiers.keySet()) {
						mods += damageModifiers.get(m);
					}

					// apply any strength limit specified for a ranged weapon
					if (ranged && strLimit < strMod) {
						strMod = strLimit;
					}

					int supplied = damageBonus - mods;
					int calculated = strMod * strMultiplier / 2;
					if (strMod < 0) calculated = strMod;	// full penalty always applies (except for some ranged weapons)

					if (supplied != calculated) {
						// try to reconcile the differences
						if (strMod > 0) {
							// melee - try the other possible multipliers
							for (int m = 0; m < 4; m++) {
								if (supplied == strMod * m / 2) {
									// found matching bonus, assume it's correct
//									System.err.println(block.getName() + " " + a + ": overriding strength bonus to " + ((float) m / 2) + "x (" + (strMod * m / 2) + ")"
//											+ " was " + ((float) calculated / strMod) + " (" + calculated + ")");
									strMultiplier = m;
								}
							}
						} else if (strMod < 0 && ranged && supplied == 0) {
							// probably a projectile weapon that is not subject to str penalties
							strMultiplier = 0;
						}
					}

					calculated = strMod * strMultiplier / 2;
					if (strMod < 0 && strMultiplier > 0) calculated = strMod;	// full penalty always applies (except for some ranged weapons)

					if (supplied != calculated) {
						// still no match
						System.err.println("Could not calculate damage bonus for " + this);
						System.err.println("supplied = " + supplied + ", calculated = " + calculated + ", mods = " + mods);
					}
				}
			}

			String getFullDescription() {
				StringBuilder b = new StringBuilder();
				b.append(number).append(" '").append(description).append("' ");
				if (automatic)
					b.append("<automatic>");
				else {
					if (primary)
						b.append("<primary");
					else
						b.append("<secondary");
					if (manufactured)
						b.append(" manufactured>");
					else
						b.append(" natural>");
					if (ranged)
						b.append(" ranged");
					else
						b.append(" melee");
					if (touch) b.append(" touch");
					b.append(" ").append(attackBonuses[0]);
					for (int i = 1; i < attackBonuses.length; i++) {
						b.append("/").append(attackBonuses[i]);
					}
				}
				b.append(" ('").append(damage).append("')");

				return b.toString();
			}

			void setDescription(String desc) {
				Matcher m = descPattern.matcher(desc);
				if (m.matches()) {
					// group:
					// 1: number including space
					// 2: number
					// 3: description
					// 4: modifiers including parentheses
					// 5: modifiers
					if (m.group(2) != null) number = Integer.parseInt(m.group(2));
					description = m.group(3);
					if (m.group(5) != null) setModifiers(m.group(5), true);	// modifiers in the description apply to attack and damage
				} else {
					// shouldn't happen
					System.err.println("Failed to match '" + desc + "'");
					description = desc;
				}

				// check if this is a natural weapon - eventually will look for manufactured weapons instead
				for (String type : naturalWeapons) {
					if (desc.toLowerCase().contains(type)) {
						manufactured = false;
					}
				}
			}

			void setTouch(boolean touch) {
				this.touch = touch;
				if (touch) manufactured = false;
			}

			void setModifiers(String modifierStr, boolean dmgAlso) {
				if (modifierStr == null) return;
				for (String modStr : modifierStr.split("\\s*,\\s*")) {
					Matcher matcher = modPattern.matcher(modStr);
					if (matcher.matches()) {
						int mod = parseModifier(matcher.group(1));
						if (matcher.group(2).equals("Str bonus")) {
							// strength limit. should only apply to compound bows but we don't check that yet
							strLimit = mod;
						} else {
							modifiers.put(matcher.group(2), mod);
							if (dmgAlso) {
								damageModifiers.put(matcher.group(2), mod);
							}
						}
					} else if (modStr.equals("secondary")) {
						primary = false;
					} else {
//						System.out.println("unmatched modifier " + modStr);
					}
				}
			}

			Map<String, Integer> getModifiers() {
				return modifiers;
			}

			int calculateAttackBonus() {
				String feats = creature.getFeats();
				if (feats == null) feats = "";
				feats = feats.toLowerCase();

				String special_qualities = creature.getSpecialQualities();
				if (special_qualities == null) special_qualities = "";
				special_qualities = special_qualities.toLowerCase();

				Set<String> weaponFocuses = new HashSet<>();
				Matcher matcher = wfPattern.matcher(feats);
				while (matcher.find()) {
					weaponFocuses.add(matcher.group(1));
//					System.out.println(block.getName() + ": weapon focus = " + weaponFocus);
				}

				int bab = creature.getBAB();
				int sizeMod = creature.getSize().getSizeModifier();
				int dexMod = AbilityScore.getModifier(creature.getDexterity());
				int str = creature.getStrength();
				int strMod = AbilityScore.getModifier(str);
				if (str == -1) strMod = dexMod;		// no strength so use dex

				int atkBonus = bab + strMod + sizeMod;
				String desc = description.toLowerCase();

				// weapon finesse
				if (feats.contains("weapon finesse")) {
					// assume weapon finesse always applies. we'll later revise the assumption if the calculated
					// attack bonus doesn't match
					weaponFinesseApplies = true;
				}

				// weapon finese applied or ranged
				if ((weaponFinesseApplies && strMod < dexMod) || ranged) {
					atkBonus = atkBonus - strMod + dexMod;
				}

				// enhancement bonus
				if (desc.contains("+") && (desc.indexOf('(') == -1 || desc.indexOf('(') > desc.indexOf('+'))) {
					// assume magic weapon - maybe should assume a plus anywhere in the description is a magic weapon
					enhancementBonus = Integer.parseInt(desc.substring(desc.indexOf('+') + 1, desc.indexOf(' ', desc.indexOf('+'))));
//						System.out.println(block.getName() + ": weapon bonus = " + weapBonus + " (" + desc + ")");
					atkBonus += enhancementBonus;
				} else if (desc.toLowerCase().contains("masterwork")) {
//						System.out.println(block.getName() + ": masterwork weapon (" + desc + ")");
					atkBonus++;
					masterwork = true;
				}

				// check for weapon focus
				for (String weaponFocus : weaponFocuses) {
					if (desc.contains(weaponFocus)
							|| plurals.containsKey(weaponFocus) && desc.contains(plurals.get(weaponFocus))) {
//							System.out.println(block.getName() + ": weapon focus (" + desc + ")");
						atkBonus++;
						weaponFocusApplies = true;
					}
				}

				// specified modifiers
				Map<String, Integer> modifiers = getModifiers();
				for (String type : modifiers.keySet()) {
					atkBonus += modifiers.get(type);
				}

				if (manufactured && has_mfg_primary && has_mfg_secondary) {
					// two weapon fighting - note that this treats manufactured secondary as a natural weapon (as with hybrid werebear and satyr)
					if (!special_qualities.contains("enhanced multiweapon fighting")) {
						atkBonus -= 2;	// minimum cost for twf
					}
					if (!feats.contains("two-weapon fighting") && !feats.contains("multiweapon fighting")) {
						atkBonus -= primary ? 2 : 6;
					}
					// TODO assuming off-hand weapon is light
					if (has_non_light_secondary) {
						atkBonus -= 2;
					}

				} else {
					// natural secondary attack
					if (!primary) {
						if (feats.contains("improved multiattack")) {
						} else if (feats.contains("multiattack")) {
							atkBonus -= 2;	// assumes multiattack always applies - should only apply to natural attacks
						} else {
							atkBonus -= 5;
						}
					}
				}

				if (attackBonuses[0] == atkBonus - dexMod + strMod && weaponFinesseApplies) {
					weaponFinesseApplies = false;
					atkBonus = atkBonus - dexMod + strMod;
				}

				return atkBonus;
			}

			@Override
			public String toString() {
				return toString(false);
			}

			String toString(boolean first) {
				StringBuilder s = new StringBuilder();
				if (number > 1) s.append(number).append(" ");
				s.append(description);

				if (!automatic) {
					if (ranged && strLimit < Integer.MAX_VALUE) {
						s.append("(+").append(strLimit).append(" Str bonus)");
					}

					// modifiers that apply to attack and damage
					if (damageModifiers.size() > 0) {
						s.append("(");
						for (String m : damageModifiers.keySet()) {
							if (s.charAt(s.length() - 1) != '(') s.append(", ");
							int val = damageModifiers.get(m);
							if (val > 0) s.append("+");
							s.append(val).append(" ").append(m);
						}
						s.append(")");
					}

					s.append(" ");
					for (int i = 0; i < attackBonuses.length; i++) {
						if (i > 0) s.append("/");
						if (attackBonuses[i] >= 0) s.append("+");
						s.append(attackBonuses[i]);
					}

					if (first && !primary) s.append(" (secondary)");

					// modifiers that only apply to attack
					Set<String> mods = new HashSet<>(modifiers.keySet());
					mods.removeAll(damageModifiers.keySet());
					if (mods.size() > 0) {
						s.append(" (");
						for (String m : mods) {
							if (s.charAt(s.length() - 1) != '(') s.append(", ");
							int val = modifiers.get(m);
							if (val > 0) s.append("+");
							s.append(val).append(" ").append(m);
						}
						s.append(")");
					}

					if (ranged)
						s.append(" ranged");
					else
						s.append(" melee");
					if (touch) s.append(" touch");
				}

				s.append(" (").append(damage);
				if (damageExtra.length() > 0) s.append(damageExtra);
				s.append(")");
				return s.toString();
			}
		}

		List<Attack> attacks = new ArrayList<>();
		boolean has_mfg_primary = false;
		boolean has_mfg_secondary = false;	// has at least one manufactured secondary attack
		boolean has_non_light_secondary = false;	// has at least one non-light manufactured secondary attack

		AttackRoutine(CreatureDetails c) {
			creature = c;
		}

		private void addAttack(Attack a) {
			if (a.manufactured && !a.automatic) {
				if (a.primary) {
					has_mfg_primary = true;
				} else {
					has_mfg_secondary = true;
					String desc = a.description.toLowerCase();
					if (desc.contains("morningstar") || desc.contains("rock")	// temporary hack (for athach)
							|| desc.contains("longbow")	// temporary hack for xill
							|| desc.contains("not light")	// hack for darktentacles
							|| desc.contains("flail")	// deathbringer
							|| desc.contains("shield")	// skullcrusher ogre
							) {
						// TODO move this test into Attack somewhere
						has_non_light_secondary = true;
						a.non_light = true;
					}
				}
			}
			attacks.add(a);
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for (int j = 0; j < attacks.size(); j++) {
				Attack a = attacks.get(j);
				if (j > 0) b.append(a.primary?" plus ":" and ");
				b.append(a.toString(j == 0));
			}
			return b.toString();
		}

		String getDescription() {
			StringBuilder attackList = new StringBuilder();
			for (int j = 0; j < attacks.size(); j++) {
				Attack a = attacks.get(j);
				if (a.primary) attackList.append("<primary> ");
				if (!a.automatic) {
					if (a.manufactured) {
						attackList.append("<manufactured> ");
					} else {
						attackList.append("<natural> ");
					}
				} else {
					attackList.append("<automatic> ");
				}
				attackList.append(a).append("\n");
			}
			return attackList.toString();
		}
	}

// TODO should download the URL directly rather than converting to a file. should add Source argument
	static List<StatisticsBlock> parseURL(URL url) {
		try {
			// first remove any fragment from the URL:
			URL u = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
			return parseFile(null, new File(u.toURI()));
		} catch (URISyntaxException | MalformedURLException ex) {
			ex.printStackTrace();
		}
		return null;
	}

// TODO remove this version - source should always be required
	static List<StatisticsBlock> parseFile(File file) {
		return parseFile(null, file);
	}

	static List<StatisticsBlock> parseFile(Source source, File file) {
		if (file.getName().endsWith(".xml")) {
			return parseXMLFile(source, file);
		} else {
			return parseHTMLFile(source, file);
		}
	}

	private static List<StatisticsBlock> parseXMLFile(Source source, File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document dom;
		List<StatisticsBlock> blocks = new ArrayList<>();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new LocalEntityResolver());
			dom = builder.parse(file);

			NodeList monsters = dom.getElementsByTagName("monster");
			for (int i = 0; i < monsters.getLength(); i++) {
				Element monster = (Element) monsters.item(i);

				String name = monster.getAttribute("name");
				URL url;
				if (source != null) {
					url = new File(source.getLocation() + "\\" + file.getName()).toURI().toURL();
				} else {
					url = file.toURI().toURL();
				}

				// find the statsblocks
				List<StatisticsBlock> statsBlocks = new ArrayList<>();
				NodeList children = monster.getElementsByTagName("statsblock");
				for (int j = 0; j < children.getLength(); j++) {
					Map<String, Integer> tags = new HashMap<>();	// counts of how many of each tag we've seen. For detecting multiple columns
					int maxTags = 0;	// highest number of one type of tag we've seen. Ultimately this is the number of columns in this statsblock

					NodeList rows = ((Element) children.item(j)).getChildNodes();
					for (int k = 0; k < rows.getLength(); k++) {
						if (rows.item(k).getNodeType() == Node.ELEMENT_NODE) {
							Element row = (Element) rows.item(k);
							String tag = row.getTagName();

							int index = 0;
							if (tags.containsKey(tag)) index = tags.get(tag);
							index++;
							tags.put(tag, index);

							StatisticsBlock block;
							if (index > maxTags) {
								// add a block
								block = new StatisticsBlock();
								block.fields.put(Field.NAME, name);
								block.fields.put(Field.URL, url.toString());
								statsBlocks.add(block);
								maxTags = index;
							} else {
								block = statsBlocks.get(index - 1);
							}

							if (tag.equals("form")) {
								block.fields.put(Field.NAME, name + " (" + row.getTextContent().trim() + ")");
							} else {
								Field f = Field.fromTagName(tag);
								if (f != null) block.fields.put(f, row.getTextContent().trim());
							}
						}
					}
				}

				for (StatisticsBlock block : statsBlocks) {
					block.source = source;
					blocks.add(block);
				}
			}

		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}

		for (StatisticsBlock s : blocks) {
			s.images = new URL[0];
		}

		return blocks;
	}

	private static List<StatisticsBlock> parseHTMLFile(Source source, File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document dom;
		List<StatisticsBlock> blocks = new ArrayList<>();
		List<URL> images = new ArrayList<>();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new LocalEntityResolver());
			dom = builder.parse(file);

			NodeList htmlBodies = dom.getElementsByTagName("body");
			if (htmlBodies.getLength() != 1) {
				System.out.println("Expected exactly one body tag, found "+htmlBodies.getLength());
				return blocks;
			}

			NodeList children = ((Element)htmlBodies.item(0)).getChildNodes();
			String name = "";
			URL url = null;
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element child = (Element)children.item(i);
					if (child.getTagName().equals("h1")) {
						name = child.getTextContent();
						if (source != null) {
							url = new File(source.getLocation() + "\\" + file.getName()).toURI().toURL();
						} else {
							url = file.toURI().toURL();
						}
						//System.out.println("h1 name = "+name);

					} else if (child.getTagName().equals("h2")) {
						// h2 tag. if this has an child anchor then this is a new monster name
						NodeList anchors = child.getElementsByTagName("a");
						if (anchors.getLength() > 0) {
							name = child.getTextContent();
							Element a = (Element)anchors.item(0);
							if (source != null) {
								url = new File(source.getLocation() + "\\" + file.getName()).toURI().toURL();
								url = new URL(url, "#" + a.getAttribute("name"));
							} else {
								url = new URL(file.toURI().toURL(), "#" + a.getAttribute("name"));
							}
							//System.out.println("h2 name = "+name);
						}

					} else if (child.getTagName().equals("table")) {
						String classString = child.getAttribute("class");
						if (classString != null && classString.contains(STATBLOCKCLASS)) {
							for (StatisticsBlock block : parseHTMLStatBlock(child, name, url.toString())) {
								block.source = source;
								blocks.add(block);
							}
						}
					} else if (child.getTagName().equals("a")) {
						String classString = child.getAttribute("class");
						if (classString != null && classString.contains(IMAGECLASS)) {
							String href = child.getAttribute("href");
							URL u;
							if (source != null) {
								u = new File(source.getLocation() + "\\" + file.getName()).toURI().toURL();
								u = new URL(u, href);
							} else {
								u = new URL(file.toURI().toURL(), href);
							}
							images.add(u);
						}
					}
				}
			}


		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}

		for (StatisticsBlock s : blocks) {
			s.images = images.toArray(new URL[images.size()]);
		}

		return blocks;
	}

	private static List<StatisticsBlock> parseHTMLStatBlock(Element table, String defaultName, String url) {
		//System.out.println("Found stat block");
		List<StatisticsBlock> statsBlock = new ArrayList<>();

		// fetch the rows...
		NodeList rows = table.getElementsByTagName("tr");
		if (rows.getLength() > 24) {
			// note this check is not precise - some block have fewer than 22 rows so even 22 or 23 could mean extra rows
			System.out.println("WARN: extra rows found in "+url);
		}
		for (int j = 0; j < rows.getLength(); j++) {
			Element row = (Element)rows.item(j);
			// row children...
			//System.out.println("Found row "+j);
			NodeList children = row.getChildNodes();
			String stat = "";
			int col = 0;
			for (int k = 0; k < children.getLength(); k++) {
				Node node = children.item(k);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element)node;
					if (el.getTagName().equals("th") || el.getTagName().equals("td")) {
						//System.out.println("Found data: "+el.getTextContent().trim());
						if (col == 0) {
							stat = el.getTextContent().trim();
						} else {
							while (col > statsBlock.size()) {
								StatisticsBlock block = new StatisticsBlock();
								block.fields.put(Field.NAME, defaultName);
								block.fields.put(Field.URL, url);
								statsBlock.add(block);
								//System.out.println("Added block for "+col);
							}
							StatisticsBlock block = statsBlock.get(col-1);
							if (stat.equals("")) {
								block.fields.put(Field.NAME, el.getTextContent().trim());
								//System.out.println("Set name to "+block.properties.get(Field.NAME));
							} else {
								Field p = Field.fromLabel(stat);
								if (p != null) block.fields.put(p, el.getTextContent().trim());
								//System.out.println(""+col+": "+stat+" = "+el.getTextContent());
							}
						}
						col++;
					} else {
						//System.out.println("Found unknown element: "+el.getTagName());
					}
				} else {
					//System.out.println("Found unknown child of row: "+node.getNodeName()+", contains '"+node.getNodeValue()+"'");
				}
			}
		}
		return statsBlock;
	}

	public String getHTML() {
		StringBuilder s = new StringBuilder();
		s.append("<html><table><tr><td></td><td>").append(getName()).append("</td></tr>");
		for (Field p : Field.getStandardOrder()) {
			s.append("<tr><td>").append(p).append("</td><td>").append(get(p)).append("</td></tr>");
		}
		s.append("</table></html>");
		return s.toString();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		String nl = System.getProperty("line.separator");
		s.append(getName()).append(nl);
		for (Field p : Field.getStandardOrder()) {
			s.append(p).append(" ").append(get(p)).append(nl);
		}
		return s.toString();
	}
}

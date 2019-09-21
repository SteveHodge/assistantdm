package gamesystem;

import java.util.ArrayList;
import java.util.List;

import gamesystem.core.AbstractProperty;
import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;

// the value of this property is 0. if this becomes a statistic then the value would be the total caster level modifier

public class CasterLevels extends AbstractProperty<Integer> {

	// the value of this property is the caster level of the particular class
	public class CasterClass extends AbstractProperty<Integer> {
		CharacterClass casterClass;	// one of BARD, CLERIC, DRUID, PALADIN, RANGER, SORCERER, WIZARD
		int casterLevel;
		int ability;	// relevant ability score (charisma for BARD, SORCERER, wisdom for CLERIC, DRUID, PALADIN, RANGER, intelligence for WIZARD
		List<String> domains;	// for CLERIC only

		public CasterClass(CharacterClass cls) {
			super(CasterLevels.this.name + "." + cls.toString(), CasterLevels.this.parent);
			casterClass = cls;
		}

		@Override
		public Integer getValue() {
			return getCasterLevel();
		}

		public int getCasterLevel() {
			return casterLevel;
		}

		public int getAbilityScore() {
			return ability;
		}

		public CharacterClass getCharacterClass() {
			return casterClass;
		}

		public void setCasterLevel(int l) {
			if (l == casterLevel) return;
			int old = casterLevel;
			casterLevel = l;
			fireEvent(createEvent(PropertyEvent.VALUE_CHANGED, old));
		}

		public void setAbilityScore(int a) {
			if (ability == a) return;
			ability = a;
			fireEvent(createEvent(PropertyEvent.VALUE_CHANGED, casterLevel));
		}

		// returns the slots per spell level (cleric, druid, paladin, ranger, wizard) or the spells known per level (sorcerer, bard)
		// TODO modify for ability score
		public int[] getSpellsArray() {
			int[] array;

			if (casterLevel == 0) return new int[0];
			switch (casterClass) {
			case BARD:
				array = bardKnown[casterLevel - 1];
				break;
			case CLERIC:
				array = clericSlots[casterLevel - 1];
				break;
			case DRUID:
				array = clericSlots[casterLevel - 1];
				break;
			case PALADIN:
				array = paladinSlots[casterLevel - 1];
				break;
			case RANGER:
				array = paladinSlots[casterLevel - 1];
				break;
			case SORCERER:
				array = sorcererKnown[casterLevel - 1];
				break;
			case WIZARD:
				array = wizardSlots[casterLevel - 1];
				break;
			default:
				return null;
			}

			return array.clone();
		}

		public int getMaxSpellLevel() {
			if (casterLevel < 1 || ability < 10) return -1;
			int spellLevel = getSpellsArray().length - 1;
			if (ability - 10 < spellLevel)
				return ability - 10;
			return spellLevel;
		}

	}

	public List<CasterClass> classes;
	public List<String> feats;	// empower spell, enlarge spell, extend spell, maximize spell, quicken spell, silent spell, still spell, widen spell, heighten spell

	public CasterLevels(PropertyCollection parent) {
		super("caster_levels", parent);
		classes = new ArrayList<CasterClass>();
		feats = new ArrayList<String>();
	}

	@Override
	public Integer getValue() {
		return 0;
	}

	// TODO move these to the class definitions
// slots:
	public static final int[][] clericSlots = {
			{ 3, 1 },
			{ 4, 2 },
			{ 4, 2, 1 },
			{ 5, 3, 2 },
			{ 5, 3, 2, 1 },
			{ 5, 3, 3, 2 },
			{ 6, 4, 3, 2, 1 },
			{ 6, 4, 3, 3, 2 },
			{ 6, 4, 4, 3, 2, 1 },
			{ 6, 4, 4, 3, 3, 2 },
			{ 6, 5, 4, 4, 3, 2, 1 },
			{ 6, 5, 4, 4, 3, 3, 2 },
			{ 6, 5, 5, 4, 4, 3, 2, 1 },
			{ 6, 5, 5, 4, 4, 3, 3, 2 },
			{ 6, 5, 5, 5, 4, 4, 3, 2, 1 },
			{ 6, 5, 5, 5, 4, 4, 3, 3, 2 },
			{ 6, 5, 5, 5, 5, 4, 4, 3, 2, 1 },
			{ 6, 5, 5, 5, 5, 4, 4, 3, 3, 2 },
			{ 6, 5, 5, 5, 5, 5, 4, 4, 3, 3 },
			{ 6, 5, 5, 5, 5, 5, 4, 4, 4, 4 }
	};

	public static final int[][] paladinSlots = {
			{ 0 },
			{ 0 },
			{ 0 },
			{ 0, 0 },
			{ 0, 0 },
			{ 0, 1 },
			{ 0, 1 },
			{ 0, 1, 0 },
			{ 0, 1, 0 },
			{ 0, 1, 1 },
			{ 0, 1, 1, 0 },
			{ 0, 1, 1, 1 },
			{ 0, 1, 1, 1 },
			{ 0, 2, 1, 1, 0 },
			{ 0, 2, 1, 1, 1 },
			{ 0, 2, 2, 1, 1 },
			{ 0, 2, 2, 2, 1 },
			{ 0, 3, 2, 2, 1 },
			{ 0, 3, 3, 3, 2 },
			{ 0, 3, 3, 3, 3 }
	};
	public static final int[][] sorcererPerDay = {
			{ 5, 3 },
			{ 6, 4 },
			{ 6, 5 },
			{ 6, 6, 3 },
			{ 6, 6, 4 },
			{ 6, 6, 5, 3 },
			{ 6, 6, 6, 4 },
			{ 6, 6, 6, 5, 3 },
			{ 6, 6, 6, 6, 4 },
			{ 6, 6, 6, 6, 5, 3 },
			{ 6, 6, 6, 6, 6, 4 },
			{ 6, 6, 6, 6, 6, 5, 3 },
			{ 6, 6, 6, 6, 6, 6, 4 },
			{ 6, 6, 6, 6, 6, 6, 5, 3 },
			{ 6, 6, 6, 6, 6, 6, 6, 4 },
			{ 6, 6, 6, 6, 6, 6, 6, 5, 3 },
			{ 6, 6, 6, 6, 6, 6, 6, 6, 4 },
			{ 6, 6, 6, 6, 6, 6, 6, 6, 5, 3 },
			{ 6, 6, 6, 6, 6, 6, 6, 6, 6, 4 },
			{ 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 }
	};

	public static final int[][] sorcererKnown = {
			{ 4, 2 },
			{ 5, 2 },
			{ 5, 3 },
			{ 6, 3, 1 },
			{ 6, 4, 2 },
			{ 7, 4, 2, 1 },
			{ 7, 5, 3, 2 },
			{ 8, 5, 3, 2, 1 },
			{ 8, 5, 4, 3, 2 },
			{ 9, 5, 4, 3, 2, 1 },
			{ 9, 5, 5, 4, 3, 2 },
			{ 9, 5, 5, 4, 3, 2, 1 },
			{ 9, 5, 5, 4, 4, 3, 2 },
			{ 9, 5, 5, 4, 4, 3, 2, 1 },
			{ 9, 5, 5, 4, 4, 4, 3, 2 },
			{ 9, 5, 5, 4, 4, 4, 3, 2, 1 },
			{ 9, 5, 5, 4, 4, 4, 3, 3, 2 },
			{ 9, 5, 5, 4, 4, 4, 3, 3, 2, 1 },
			{ 9, 5, 5, 4, 4, 4, 3, 3, 3, 2 },
			{ 9, 5, 5, 4, 4, 4, 3, 3, 3, 3 }
	};

	public static final int[][] bardPerDay = {
			{ 2 },
			{ 3, 0 },
			{ 3, 1 },
			{ 3, 2, 0 },
			{ 3, 3, 1 },
			{ 3, 3, 2 },
			{ 3, 3, 2, 0 },
			{ 3, 3, 3, 1 },
			{ 3, 3, 3, 2 },
			{ 3, 3, 3, 2, 0 },
			{ 3, 3, 3, 3, 1 },
			{ 3, 3, 3, 3, 2 },
			{ 3, 3, 3, 3, 2, 0 },
			{ 4, 3, 3, 3, 3, 1 },
			{ 4, 4, 3, 3, 3, 2 },
			{ 4, 4, 4, 3, 3, 2, 0 },
			{ 4, 4, 4, 4, 3, 3, 1 },
			{ 4, 4, 4, 4, 4, 3, 2 },
			{ 4, 4, 4, 4, 4, 4, 3 },
			{ 4, 4, 4, 4, 4, 4, 4 }
	};

	public static final int[][] bardKnown = {
			{ 4 },
			{ 5, 2 },
			{ 6, 3 },
			{ 6, 3, 2 },
			{ 6, 4, 3 },
			{ 6, 4, 3 },
			{ 6, 4, 4, 2 },
			{ 6, 4, 4, 3 },
			{ 6, 4, 4, 3 },
			{ 6, 4, 4, 4, 2 },
			{ 6, 4, 4, 4, 3 },
			{ 6, 4, 4, 4, 3 },
			{ 6, 4, 4, 4, 4, 2 },
			{ 6, 4, 4, 4, 4, 3 },
			{ 6, 4, 4, 4, 4, 3 },
			{ 6, 5, 4, 4, 4, 4, 2 },
			{ 6, 5, 5, 4, 4, 4, 3 },
			{ 6, 5, 5, 5, 4, 4, 3 },
			{ 6, 5, 5, 5, 5, 4, 4 },
			{ 6, 5, 5, 5, 5, 5, 4 }
	};

	public static final int[][] wizardSlots = {
			{ 3, 1 },
			{ 4, 2 },
			{ 4, 2, 1 },
			{ 4, 3, 2 },
			{ 4, 3, 2, 1 },
			{ 4, 3, 3, 2 },
			{ 4, 4, 3, 2, 1 },
			{ 4, 4, 3, 3, 2 },
			{ 4, 4, 4, 3, 2, 1 },
			{ 4, 4, 4, 3, 3, 2 },
			{ 4, 4, 4, 4, 3, 2, 1 },
			{ 4, 4, 4, 4, 3, 3, 2 },
			{ 4, 4, 4, 4, 4, 3, 2, 1 },
			{ 4, 4, 4, 4, 4, 3, 3, 2 },
			{ 4, 4, 4, 4, 4, 4, 3, 2, 1 },
			{ 4, 4, 4, 4, 4, 4, 3, 3, 2 },
			{ 4, 4, 4, 4, 4, 4, 4, 3, 2, 1 },
			{ 4, 4, 4, 4, 4, 4, 4, 3, 3, 2 },
			{ 4, 4, 4, 4, 4, 4, 4, 4, 3, 3 },
			{ 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 }
	};

// domain spells:
/*	'Air': ['Obscuring Mist','Wind Wall','Gaseous Form','Air Walk','Control Winds','Chain Lightning','Control Weather','Whirlwind','Elemental Swarm (Air only)'],
	'Animal': ['Calm Animals','Hold Animal','Dominate Animal',"Summon Nature's Ally IV (Animals only)",'Commune with Nature','Antilife Shell','Animal Shapes',"Summon Nature's Ally VIII (Animals only)",'Shapechange'],
	'Chaos': ['Protection from Law','Shatter','Magic Circle against Law','Chaos Hammer','Dispel Law','Animate Objects','Word of Chaos','Cloak of Chaos','Summon Monster IX (Chaos only)'],
	'Death': ['Cause Fear','Death Knell','Animate Dead','Death Ward','Slay Living','Create Undead','Destruction','Create Greater Undead','Wail of the Banshee'],
	'Destruction': ['Inflict Light Wounds','Shatter','Contagion','Inflict Critical Wounds','Inflict Light Wounds, Mass','Harm','Disintegrate','Earthquake','Implosion'],
	'Earth': ['Magic Stone','Soften Earth and Stone','Stone Shape','Spike Stones','Wall of Stone','Stoneskin','Earthquake','Iron Body','Elemental Swarm (Earth only)'],
	'Evil': ['Protection from Good','Desecrate','Magic Circle against Good','Unholy Blight','Dispel Good','Create Undead','Blasphemy','Unholy Aura','Summon Monster IX (Evil only)'],
	'Fire': ['Burning Hands','Produce Flame','Resist Energy (Cold or fire only)','Wall of Fire','Fire Shield','Fire Seeds','Fire Storm','Incendiary Cloud','Elemental Swarm (Fire only)'],
	'Good': ['Protection from Evil','Aid','Magic Circle against Evil','Holy Smite','Dispel Evil','Blade Barrier','Holy Word','Holy Aura','Summon Monster IX (Good only)'],
	'Healing': ['Cure Light Wounds','Cure Moderate Wounds','Cure Serious Wounds','Cure Critical Wounds','Cure Light Wounds, Mass','Heal','Regenerate','Cure Critical Wounds, Mass','Heal, Mass'],
	'Knowledge': ['Detect Secret Doors','Detect Thoughts','Clairaudience/Clairvoyance','Divination','True Seeing','Find the Path','Legend Lore','Discern Location','Foresight'],
	'Law': ['Protection from Chaos','Calm Emotions','Magic Circle against Chaos',"Order's Wrath",'Dispel Chaos','Hold Monster','Dictum','Shield of Law','Summon Monster IX (Law only)'],
	'Luck': ['Entropic Shield','Aid','Protection from Energy','Freedom of Movement','Break Enchantment','Mislead','Spell Turning','Moment of Prescience','Miracle'],
	'Magic': ["Nystul's Magic Aura",'Identify','Dispel Magic','Imbue with Spell Ability','Spell Resistance','Antimagic Field','Spell Turning','Protection from Spells',"Mordenkainen's Disjunction"],
	'Plant': ['Entangle','Barkskin','Plant Growth','Command Plants','Wall of Thorns','Repel Wood','Animate Plants','Control Plants','Shambler'],
	'Protection': ['Sanctuary','Shield Other','Protection from Energy','Spell Immunity','Spell Resistance','Antimagic Field','Repulsion','Mind Blank','Prismatic Sphere'],
	'Strength': ['Enlarge Person',"Bull's Strength",'Magic Vestment','Spell Immunity','Righteous Might','Stoneskin',"Bigby's Grasping Hand","Bigby's Clenched Fist","Bigby's Crushing Hand"],
	'Sun': ['Endure Elements','Heat Metal','Searing Light','Fire Shield','Flame Strike','Fire Seeds','Sunbeam','Sunburst','Prismatic Sphere'],
	'Travel': ['Longstrider','Locate Object','Fly','Dimension Door','Teleport','Find the Path','Teleport, Greater','Phase Door','Astral Projection'],
	'Trickery': ['Disguise Self','Invisibility','Nondetection','Confusion','False Vision','Mislead','Screen','Polymorph Any Object','Time Stop'],
	'War': ['Magic Weapon','Spiritual Weapon','Magic Vestment','Divine Power','Flame Strike','Blade Barrier','Power Word Blind','Power Word Stun','Power Word Kill'],
	'Water': ['Obscuring Mist','Fog Cloud','Water Breathing','Control Water','Ice Storm','Cone of Cold','Acid Fog','Horrid Wilting','Elemental Swarm (Water only)']
 */

}

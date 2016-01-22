package party;

import gamesystem.AC;
import gamesystem.CharacterClass.ClassOption;
import gamesystem.Creature;
import gamesystem.CreatureProcessor;
import gamesystem.Feat;
import gamesystem.HitDiceProperty;
import gamesystem.Levels;
import gamesystem.Modifier;
import gamesystem.SavingThrow;
import gamesystem.XMLOutputHelper;
import monsters.Monster.MonsterAttackRoutine;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class XMLOutputCharacterProcessor extends XMLOutputHelper implements CreatureProcessor {
	protected Character character;

	public XMLOutputCharacterProcessor(Document d) {
		super(d);
	}

	@Override
	public void processCreature(Creature c) {
		if (!(c instanceof Character)) return;
		character = (Character) c;

		if (doc == null) return;

		creatureEl = doc.createElement("Character");
		creatureEl.setAttribute("name", c.getName());
		setAttributeFromProperty(c, creatureEl, "player", Character.PROPERTY_PLAYER);
		setAttributeFromProperty(c, creatureEl, "region", Character.PROPERTY_REGION);
		setAttributeFromProperty(c, creatureEl, "race", Character.PROPERTY_RACE);
		setAttributeFromProperty(c, creatureEl, "gender", Character.PROPERTY_GENDER);
		setAttributeFromProperty(c, creatureEl, "alignment", Character.PROPERTY_ALIGNMENT);
		setAttributeFromProperty(c, creatureEl, "deity", Character.PROPERTY_DEITY);
		setAttributeFromProperty(c, creatureEl, "type", Character.PROPERTY_TYPE);
		setAttributeFromProperty(c, creatureEl, "age", Character.PROPERTY_AGE);
		setAttributeFromProperty(c, creatureEl, "height", Character.PROPERTY_HEIGHT);
		setAttributeFromProperty(c, creatureEl, "weight", Character.PROPERTY_WEIGHT);
		setAttributeFromProperty(c, creatureEl, "eye-colour", Character.PROPERTY_EYE_COLOUR);
		setAttributeFromProperty(c, creatureEl, "hair-colour", Character.PROPERTY_HAIR_COLOUR);
		setAttributeFromProperty(c, creatureEl, "speed", Character.PROPERTY_SPEED);
		setAttributeFromProperty(c, creatureEl, "damage-reduction", Character.PROPERTY_DAMAGE_REDUCTION);
		setAttributeFromProperty(c, creatureEl, "spell-resistance", Character.PROPERTY_SPELL_RESISTANCE);
		setAttributeFromProperty(c, creatureEl, "arcane-spell-failure", Character.PROPERTY_ARCANE_SPELL_FAILURE);
		setAttributeFromProperty(c, creatureEl, "action-points", Character.PROPERTY_ACTION_POINTS);
		setAttributeFromProperty(c, creatureEl, "campaign", Character.PROPERTY_CAMPAIGN);
	}

	@Override
	public void processSavingThrow(SavingThrow s) {
		getSavingThrowElement(s);
	}

	@Override
	public void processFeat(Feat feat) {
		if (!feat.bonus) super.processFeat(feat);
	}

	@Override
	public void processLevel(Levels level) {
		levelEl = getLevelElement(level);
		levelEl.setAttribute("xp", "" + character.xp);

		Node first = levelEl.getFirstChild();
		for (ClassOption opt : character.classOptions.values()) {
			if (opt.selection != null && opt.selection != "") {
				Element e = doc.createElement("ClassOption");
				e.setAttribute("id", opt.id);
				e.setAttribute("selection", opt.selection);
				levelEl.insertBefore(e, first);
			}
		}
	}

	protected Element getSavingThrowElement(SavingThrow s) {
		if (creatureEl == null) return null;
		if (savesEl == null) {
			savesEl = doc.createElement("SavingThrows");
			creatureEl.appendChild(savesEl);
		}
		SavingThrow.Type t = s.getType();

		Element saveEl = doc.createElement("Save");
		saveEl.setAttribute("type", t.toString());
		if (s.getBaseOverride() != -1) saveEl.setAttribute("base", "" + s.getBaseOverride());

		if (character.saveMisc.containsKey(t)) {
			saveEl.setAttribute("misc", "" + character.saveMisc.get(t).getModifier());
		}
		savesEl.appendChild(saveEl);
		return saveEl;
	}

	@Override
	public void processAC(AC ac) {
		Element e = getACElement(ac);

		for (Modifier m : character.acMods.values()) {
			if (m.getModifier() != 0) {
				Element comp = doc.createElement("ACComponent");
				comp.setAttribute("type", m.getType());
				comp.setAttribute("value", "" + m.getModifier());
				e.appendChild(comp);
			}
		}
		creatureEl.appendChild(e);
	}

	@Override
	public void processMonsterAttackForm(MonsterAttackRoutine a) {
	}

	@Override
	public void processMonsterFullAttackForm(MonsterAttackRoutine a) {
	}

	@Override
	public void processHitdice(HitDiceProperty hitDice) {
	}
}

package party;

import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Attacks.AttackForm;
import gamesystem.CharacterClass;
import gamesystem.HPs;
import gamesystem.InitiativeModifier;
import gamesystem.Levels;
import gamesystem.SavingThrow;
import gamesystem.SkillType;
import gamesystem.Skills;
import gamesystem.XP.XPChangeAdhoc;
import gamesystem.XP.XPChangeChallenges;
import gamesystem.XP.XPChangeLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import party.Character.ACComponentType;
import util.Updater;

public class CharacterSheetView {
	private Character character;
	private boolean autosave = false;

	public CharacterSheetView(Character c, boolean auto) {
		character = c;
		autosave = auto;

		character.addPropertyChangeListener(e -> {
			if (autosave) {
				System.out.println("Autosave trigger by change to " + e.getPropertyName());
				saveCharacterSheet();
			}
		});
	}

	public void saveCharacterSheet() {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/assistantdm/static/CharacterSheetTemplate.xsl\"");
			doc.appendChild(pi);
			CharacterSheetProcessor processor = new CharacterSheetProcessor(doc);
			character.executeProcess(processor);
			doc.appendChild(processor.getElement());
			doc.setXmlStandalone(true);
			Updater.updateDocument(doc, character.getName());
			//System.out.println("Saved character sheet "+name);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getModifierString(int mod) {
		if (mod >= 0) return "+" + mod;
		return Integer.toString(mod);
	}

	private static void setACComponent(Document doc, Element e, String type, int mod) {
		if (mod != 0) {
			Element comp = doc.createElement("ACComponent");
			comp.setAttribute("type", type);
			comp.setAttribute("value", "" + getModifierString(mod));
			e.appendChild(comp);
		}
	}

	// TODO this should probably include all the same data as the regular save as well as additional stuff
	class CharacterSheetProcessor extends XMLOutputCharacterProcessor {
		public CharacterSheetProcessor(Document doc) {
			super(doc);
		}

		@Override
		public void processLevel(Levels level) {
			// process class levels
			Map<String,Integer> classes = new HashMap<>();
			int defined = 0;
			for (int i = 1; i <= level.getLevel(); i++) {
				CharacterClass cls = level.getClass(i);
				if (cls != null) {
					defined++;
					if (classes.containsKey(cls.toString())) {
						classes.put(cls.toString(), classes.get(cls.toString()) + 1);
					} else {
						classes.put(cls.toString(), 1);
					}
				}
			}
			String[] ordered = classes.keySet().toArray(new String[0]);
			String classStr = "";
			String lvlStr = "";
			if (ordered.length > 0) {
				Arrays.sort(ordered, (k1, k2) -> classes.get(k2) - classes.get(k1));
				classStr = String.join("/", ordered);

				for (String c : ordered) {
					if (lvlStr.length() > 0) lvlStr += "/";
					lvlStr += Integer.toString(classes.get(c));
				}

				if (defined < level.getLevel()) {
					classStr += "/?";
					lvlStr += "/" + (level.getLevel() - defined);
				}
			} else {
				lvlStr = Integer.toString(level.getLevel());
			}

			Element levelEl = doc.createElement("Level");
			levelEl.setAttribute("level", "" + lvlStr);
			levelEl.setAttribute("class", classStr);
			creatureEl.appendChild(levelEl);
		}

		// XP output is not required:
		@Override
		public void processXPChange(XPChangeAdhoc xp) {
		}

		@Override
		public void processXPChange(XPChangeChallenges xp) {
		}

		@Override
		public void processXPChange(XPChangeLevel xp) {
		}

		@Override
		public void processAbilityScore(AbilityScore s) {
			Element e1 = getAbilityScoreElement(s);
			e1.removeAttribute("value");

			e1.setAttribute("total", "" + s.getRegularValue());		// this is the base value plus modifiers to the ability score
			e1.setAttribute("modifier", getModifierString(AbilityScore.getModifier(s.getRegularValue())));
			e1.setAttribute("info", s.getSummary());

			if (s.getOverride() != -1) {
				e1.setAttribute("temp-modifier", getModifierString(AbilityScore.getModifier(s.getOverride())));
			}
		}

		@Override
		public void processAC(AC ac) {
			if (creatureEl == null) return;

			Element e = doc.createElement("AC");
			e.setAttribute("total", "" + ac.getValue());
			e.setAttribute("flat-footed", "" + ac.getFlatFootedAC().getValue());
			e.setAttribute("touch", "" + ac.getTouchAC().getValue());
			e.setAttribute("armor-check-penalty", "" + ac.getArmorCheckPenalty().getModifier());
			setACComponent(doc, e, ACComponentType.SIZE.toString(), ac.getModifiersTotal(ACComponentType.SIZE.toString()));
			setACComponent(doc, e, ACComponentType.NATURAL.toString(), ac.getModifiersTotal(ACComponentType.NATURAL.toString()));
			setACComponent(doc, e, ACComponentType.DEFLECTION.toString(), ac.getModifiersTotal(ACComponentType.DEFLECTION.toString()));
			int value = ac.getModifiersTotal(ACComponentType.OTHER.toString());
			value += ac.getModifiersTotal(ACComponentType.DODGE.toString());
			setACComponent(doc, e, ACComponentType.OTHER.toString(), value);
			setACComponent(doc, e, "Dexterity", ac.getModifiersTotal("Dexterity"));
			setACComponent(doc, e, "Armor", ac.getArmor().getValue());
			setACComponent(doc, e, "Shield", ac.getShield().getValue());
			creatureEl.appendChild(e);
		}

		private Set<String> meleeExcl = new HashSet<>(Arrays.asList(new String[] { AbilityScore.Type.STRENGTH.toString(), "Size" }));
		private Set<String> rangedExcl = new HashSet<>(Arrays.asList(new String[] { AbilityScore.Type.DEXTERITY.toString(), "Size" }));

		@Override
		public void processAttacks(Attacks attacks) {
			Element e = getAttacksElement(attacks);
			if (attacks.getBABOverride() == -1) {
				e.setAttribute("temp", "");
			} else {
				e.setAttribute("temp", getModifierString(attacks.getBABOverride()));
			}
			e.setAttribute("base", getModifierString(attacks.getBAB()));
			e.setAttribute("size-modifier", "+0");			// TODO implement
			e.setAttribute("normal-attacks", attacks.getAttacksDescription(attacks.getCalculatedBAB(), attacks.getCalculatedBAB()));		// attack description for non-overridden BAB
			e.setAttribute("attacks", attacks.getAttacksDescription(attacks.getBAB()));		// attack description based on current BAB

			Element e1 = doc.createElement("Attack");
			e1.setAttribute("type", "Grapple");
			e1.setAttribute("total", getModifierString(attacks.getGrappleValue()));
			e1.setAttribute("misc", "+0");				// TODO implement
			e.appendChild(e1);
			e1 = doc.createElement("Attack");
			e1.setAttribute("type", "Melee");
			e1.setAttribute("total", getModifierString(attacks.getValue()));
			e1.setAttribute("misc", getModifierString(attacks.getModifiersTotal(meleeExcl)));
			e1.setAttribute("temp-modifier", "");				// TODO implement
			e1.setAttribute("attacks", attacks.getAttacksDescription(attacks.getValue()));
			e1.setAttribute("info", attacks.getSummary());
			e.appendChild(e1);
			e1 = doc.createElement("Attack");
			e1.setAttribute("type", "Ranged");
			e1.setAttribute("total", getModifierString(attacks.getRangedValue()));
			e1.setAttribute("misc", getModifierString(attacks.getRangedModifiersTotal(rangedExcl)));
			e1.setAttribute("temp-modifier", "");				// TODO implement
			e1.setAttribute("attacks", attacks.getAttacksDescription(attacks.getRangedValue()));
			e1.setAttribute("info", attacks.getRangedSummary());
			e.appendChild(e1);
			creatureEl.appendChild(e);
		}

		@Override
		protected Element getAttackFormElement(AttackForm a) {
			Element e = super.getAttackFormElement(a);
			e.setAttribute("attacks", a.getAttacksDescription());
			e.setAttribute("info", a.getSummary());
			e.setAttribute("damage_info", a.getDamageSummary());
			return e;
		}

		@Override
		public void processHPs(HPs hps) {
			Element e = getHPsElement(hps);
			e.setAttribute("info", hps.getSummary());
			e.setAttribute("current", Integer.toString(hps.getHPs()));
		}

		@Override
		public void processSavingThrow(SavingThrow s) {
			Element saveEl = getSavingThrowElement(s);
			saveEl.setAttribute("type", s.getName());
			saveEl.setAttribute("base", getModifierString(s.getBaseValue()));
			saveEl.setAttribute("total", getModifierString(s.getValue()));
			saveEl.setAttribute("info", s.getSummary());
			int temp = 0;
			if (character.saveMisc.get(s.getType()) != null) temp = character.saveMisc.get(s.getType()).getModifier();
			if (temp != 0) saveEl.setAttribute("misc", getModifierString(temp));	// the misc/temp modifier applied through the ui
			int misc = s.getValue() - s.getBaseValue() - character.getAbilityStatistic(s.getType().getAbilityType()).getModifierValue() - temp;
			if (misc != 0) saveEl.setAttribute("mods", getModifierString(misc));	// mods is the total combined modifiers other than the misc/temp modifier and the ability modifier
		}

		@Override
		public void processSkills(Skills skills) {
			if (creatureEl == null) return;

			Element e = doc.createElement("Skills");
			ArrayList<SkillType> set = new ArrayList<>(character.getSkills());
			Collections.sort(set, (o1, o2) -> o1.getName().compareTo(o2.getName()));
			for (SkillType s : set) {
				Element se = doc.createElement("Skill");
				se.setAttribute("type", s.getName());
				if (character.skills.getRanks(s) == (int) character.skills.getRanks(s)) {
					se.setAttribute("ranks", Integer.toString((int) character.skills.getRanks(s)));
				} else {
					se.setAttribute("ranks", "" + character.skills.getRanks(s));
				}
				// cross-class="true"
				se.setAttribute("untrained", Boolean.toString(!s.isTrainedOnly()));
				se.setAttribute("ability", s.getAbility().getAbbreviation());
				se.setAttribute("ability-modifier", "" + character.getAbilityStatistic(s.getAbility()).getModifierValue());
				se.setAttribute("total", getModifierString(character.skills.getValue(s)));
				se.setAttribute("info", character.skills.getSummary(s));

				int miscMod = character.skills.getMisc(s);
				Set<String> excl = new HashSet<>();
				excl.add(s.getAbility().toString());
				miscMod += character.skills.getModifiersTotal(s, excl);

				if (miscMod != 0) se.setAttribute("misc", Integer.toString(miscMod));

				e.appendChild(se);
			}
			creatureEl.appendChild(e);
		}

		@Override
		public void processInitiative(InitiativeModifier initiative) {
			Element e = getInitiativeElement(initiative);
			e.setAttribute("total", getModifierString(initiative.getValue()));
			e.setAttribute("misc", getModifierString(initiative.getValue() - character.getAbilityStatistic(AbilityScore.Type.DEXTERITY).getModifierValue()));	// assumes only 1 dex modifier that will always apply
			e.setAttribute("info", initiative.getSummary());
			e.removeAttribute("value");
		}
	}
}

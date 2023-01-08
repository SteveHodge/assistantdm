package monsters;

import java.net.MalformedURLException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gamesystem.AC;
import gamesystem.Creature;
import gamesystem.CreatureProcessor;
import gamesystem.HitDiceProperty;
import gamesystem.Levels;
import gamesystem.Modifier;
import gamesystem.NegativeLevels;
import gamesystem.SavingThrow;
import gamesystem.XMLOutputHelper;
import monsters.Monster.MonsterAttackRoutine;

public class XMLOutputMonsterProcessor extends XMLOutputHelper implements CreatureProcessor {
//	private Monster monster;

	public XMLOutputMonsterProcessor(Document d) {
		super(d);
	}

	@Override
	public void processCreature(Creature c) {
		if (!(c instanceof Monster)) return;
//		monster = (Monster) c;

		if (doc == null) return;

		creatureEl = doc.createElement("Monster");
		creatureEl.setAttribute("name", c.getName());
		creatureEl.setAttribute("id", Integer.toString(c.getID()));
	}

	@Override
	public void processProperty(String property, Object value) {
		if (value == null) return;

		// TODO ignore values that match the statsblock (if there is one) default?
		Element propEl = doc.createElement("Property");
		propEl.setAttribute("name", property);
		propEl.setAttribute("value", value.toString());
		creatureEl.appendChild(propEl);
	}

	@Override
	public void processStatisticsBlock(StatisticsBlock blk) {
		try {
			Element propEl = doc.createElement("StatisticsBlock");
			propEl.setAttribute("url", blk.getURL().toString());
			propEl.setAttribute("name", blk.getName());
			creatureEl.appendChild(propEl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processAC(AC ac) {
		Element e = getACElement(ac);

		for (Modifier m : ac.getModifiers().keySet()) {
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
	public void processSavingThrow(SavingThrow s) {
		if (creatureEl == null) return;
		if (savesEl == null) {
			savesEl = doc.createElement("SavingThrows");
			creatureEl.appendChild(savesEl);
		}
		SavingThrow.Type t = s.getType();

		Element saveEl = doc.createElement("Save");
		saveEl.setAttribute("type", t.toString());
		saveEl.setAttribute("base", "" + s.getBaseValue());

		savesEl.appendChild(saveEl);
	}

	@Override
	public void processLevel(Levels level, NegativeLevels negLevels) {
	}

	@Override
	public void processMonsterAttackForm(MonsterAttackRoutine a) {
		if (creatureEl == null) return;

		Element e = doc.createElement("AttackForm");
		e.setTextContent(a.toString());
		attacksEl.appendChild(e);
	}

	@Override
	public void processMonsterFullAttackForm(MonsterAttackRoutine a) {
		if (creatureEl == null) return;

		Element e = doc.createElement("FullAttackForm");
		e.setTextContent(a.toString());
		attacksEl.appendChild(e);
	}

	@Override
	public void processHitdice(HitDiceProperty hitDice) {
		if (creatureEl == null) return;

		Element e = doc.createElement("HitDice");
		e.setAttribute("dice", hitDice.toString());
		creatureEl.appendChild(e);
	}

}

package monsters;

import gamesystem.AC;
import gamesystem.Creature;
import gamesystem.CreatureProcessor;
import gamesystem.Level;
import gamesystem.SavingThrow;
import gamesystem.XMLOutputHelper;

import java.net.MalformedURLException;

import monsters.Monster.MonsterAttackRoutine;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

		// TODO hitdice
	}

	@Override
	public void processProperty(String property, Object value) {
		if (value == null) return;

		// TODO ignore values that match the statsblock (if there is one) default?

		if (property.equals(StatsBlockCreatureView.PROPERTY_STATS_BLOCK)) {
			StatisticsBlock blk = (StatisticsBlock) value;
			try {
				Element propEl = doc.createElement("StatisticsBlock");
				propEl.setAttribute("url", blk.getURL().toString());
				propEl.setAttribute("name", blk.getName());
				creatureEl.appendChild(propEl);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
			Element propEl = doc.createElement("Property");
			propEl.setAttribute("name", property);
			propEl.setAttribute("value", value.toString());
			creatureEl.appendChild(propEl);
		}
	}

	@Override
	public void processAC(AC ac) {
		creatureEl.appendChild(getACElement(ac));
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
	public void processLevel(Level level) {
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

}

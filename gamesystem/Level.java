package gamesystem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//TODO this is statistic for the listener stuff but it doesn't really need modifiers so perhaps refactor

// TODO class levels
// TODO should XP be here too or separate?
// TODO support monsters?

public class Level extends Statistic implements HitDice {
	int level = 1;

	public Level() {
		super("Level");
	}

	public int getLevel() {
		return level;
	}

	@Override
	public int getHitDiceCount() {
		return level;
	}

	public void setLevel(int l) {
		level = l;
	}

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement("Level");
		e.setAttribute("level", ""+level);
		return e;
	}

	// TODO notify listeners?
	public void parseDOM(Element e) {
		if (!e.getTagName().equals("Level")) return;
		level = Integer.parseInt(e.getAttribute("level"));
	}
}

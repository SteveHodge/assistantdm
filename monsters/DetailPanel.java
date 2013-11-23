package monsters;

import javax.swing.JPanel;

import party.DetailedMonster;

@SuppressWarnings("serial")
abstract class DetailPanel extends JPanel {
	abstract void setMonster(DetailedMonster m);
}
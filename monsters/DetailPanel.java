package monsters;

import javax.swing.JPanel;

import monsters.EncounterDialog.MonsterData;

@SuppressWarnings("serial")
abstract class DetailPanel extends JPanel {
	abstract void setMonster(Monster m, MonsterData d);
}
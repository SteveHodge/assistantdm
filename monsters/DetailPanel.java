package monsters;

import javax.swing.JPanel;


@SuppressWarnings("serial")
abstract class DetailPanel extends JPanel {
	abstract void setMonster(Monster m);
}
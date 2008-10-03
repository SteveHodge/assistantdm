package magicgenerator;
import java.util.Random;

public class TableRoller implements TableRowChooser {
	public static java.util.Random rand = new Random();

	public static int roll() {
		return rand.nextInt(100)+1;
	}

	public TableRow chooseRow(Table t, int category) {
		return t.getTableRow(category, roll());
	}
}

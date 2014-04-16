package magicgenerator;
import java.util.Random;

public class TableRoller implements TableRowChooser {
	public static java.util.Random rand = new Random();

	public static int roll() {
		return rand.nextInt(100)+1;
	}

	@Override
	public TableRow chooseRow(Item i, Table t) {
		return t.getTableRow(i.category, roll());
	}
}

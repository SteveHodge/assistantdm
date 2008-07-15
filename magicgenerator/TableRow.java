package magicgenerator;

import java.util.ArrayList;
import java.util.List;

// a row in a Table
class TableRow {
	int number;
	int chances[] = new int[3];
	List<Instruction> instructions = new ArrayList<Instruction>();

	public String toString() {
		String s = "  Row ("+number+") chances = ";
		for (int i=0; i<3; i++) {
			s += chances[i];
			if (i<2) s += ", ";
		}
		s += "\n";
		for (Instruction i : instructions) {
			s += "    "+i+"\n";
		}
		return s;
	}
}

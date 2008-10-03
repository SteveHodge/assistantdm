package magicgenerator;

import java.util.ArrayList;
import java.util.List;

// a row in a Table
public class TableRow {
	int number;
	int chances[] = new int[3];
	List<Instruction> instructions = new ArrayList<Instruction>();

	public int getChance(int cat) {
		// TODO check argument
		return chances[cat];
	}

	public String getInstructionString() {
		String s = "";
		for (Instruction i : instructions) {
			s += i.toString();
		}
		return s;
	}

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

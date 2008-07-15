package magicgenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// encapsulates the Fields of an item and the instructions required to generate it
class Procedure {
	String name;
	List<Instruction> instructions = new ArrayList<Instruction>();

	public String toString() {
		String s = "Table "+name+"\n";
		for (Instruction i : instructions) {
			s += "  " + i + "\n";
		}
		return s;
	}

	public void execute(Item item) {
		//System.out.println("Running "+name);
		for (Instruction i : instructions) {
			i.execute(item);
		}
	}

	// str contains the procedure definition line
	// procedure "<name>"
	// lines following must be either valid single instructions
	// an empty line terminates the procedure
	public static Procedure parseProcedure(NameSpace ns, String proc, BufferedReader in) throws IOException {
		Procedure p = new Procedure();
		p.name = proc.substring(proc.indexOf('"')+1, proc.lastIndexOf('"'));
		String str;
		while ((str = in.readLine()) != null) {
			if (str.equals("")) break;
			else {
				Instruction i = Instruction.parseInstruction(ns, str);
				p.instructions.add(i);
			}
		}
		return p;
	}
}

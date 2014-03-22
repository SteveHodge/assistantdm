package magicgenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// a table used to generate Items
public class Table {
	public String name;
	public String comment;
	List<TableRow> rows = new ArrayList<>();

	@Override
	public String toString() {
		String s = "Table "+name+" ("+comment+")";
		for (TableRow r : rows) {
			s += "\n"+r;
		}
		return s;
	}

	public int getRowCount() {
		return rows.size();
	}

	public TableRow getRow(int rownum) {
		return rows.get(rownum);
	}

	public TableRow getTableRow(int category, int roll) {
		int sum = 0;
		for (TableRow row : rows) {
			sum += row.chances[category];
			if (sum >= roll) {
				//System.out.println("Roll = "+roll+" less than "+sum+" (total for row "+row.number+")");
				return row;
			}
		}
		return null;
	}

	// str contains the table definition line
	// table "<name>" "<description>"
	public static Table parseTable(NameSpace ns, String tab, BufferedReader in) throws IOException {
		String nameEtc = tab.substring(tab.indexOf('"')+1, tab.lastIndexOf('"'));
		Table t = new Table();
		t.name = nameEtc.substring(0, nameEtc.indexOf('"'));
		t.comment = nameEtc.substring(nameEtc.lastIndexOf('"')+1);
		String str;
		while ((str = in.readLine()) != null) {
			if (str.equals("")) break;
			// 0:01-60,01-05,:roll("7-4"),modifier=+1,effective_modifier=+1
			String[] pieces = str.split(":");
			TableRow r = new TableRow();
			r.number = Integer.parseInt(pieces[0]);
			String[] chances = pieces[1].split(",");
			if (chances.length > 1) {
				for (int i = 0; i < chances.length; i++) {
					r.chances[i] = parseChance(chances[i]);
				}
			} else {
				int c = parseChance(chances[0]);
				for (int i=0; i<3; i++) r.chances[i] = c;
			}

			// add instructions, if any
			if (pieces.length>2) {
				r.instructions = Instruction.parseInstructionLine(ns, pieces[2]);
			}
			t.rows.add(r);
		}
		return t;
	}

	static int parseChance(String s) {
		if (s.equals("-")) {
			return 0;
		} else if (s.contains("-")) {
			int h = Integer.parseInt(s.substring(s.indexOf('-')+1));
			int l = Integer.parseInt(s.substring(0,s.indexOf('-')));
			return h - l + 1;
		} else {
			return 1;	// one number
		}
	}
}

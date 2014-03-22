package magicgenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//TODO doesn't handle double weapons
//TODO doesn't handle duplicated specials
//TODO this is going to have to be thread safe - generator() at least
//TODO tables need readable comments for user selection

public class Generator implements NameSpace {
	Map<String, Procedure> procedures = new HashMap<>();
	Map<String, Table> tables = new HashMap<>();

	public Generator(String filename) {
		try {
			loadScript(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void loadScript(String filename) throws IOException {
		//System.out.println("loading "+filename);
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String str;
		while ((str = in.readLine()) != null) {
			if (str.startsWith("procedure ")) {
				Procedure p = Procedure.parseProcedure(this, str, in);
				procedures.put(p.name, p);
				//System.out.println("Procedure: '"+p.name+"'");
			} else if (str.startsWith("table ")) {
				Table t = Table.parseTable(this, str, in);
				tables.put(t.name,t);
				//System.out.println("Table: '"+t.name+"'");
			} else if (str.startsWith("include ")) {
				String f = str.substring(str.indexOf('"')+1,str.lastIndexOf('"'));
				loadScript(f);
			}
		}
		in.close();
	}

	public Item generate(int category, String procName) {
		return generate(category, procName, new TableRoller());
	}

	public Item generate(int category, String procName, TableRowChooser tabChooser) {
		Procedure p = procedures.get(procName);
		if (p == null) throw new IllegalArgumentException("Unknown procedure "+procName);
		Item item = new Item(category);
		p.execute(item, tabChooser);

		while(true) {
			Field f = item.getField("invalid");
			if (f == null) break;
			Object o = item.getValue(f);
			if (o == null || !(o instanceof Integer)) break;
			if (((Integer)o).intValue() != 1) break;
			String name = (String)item.getValue("rerun");
//			System.out.println("! Invalid item: "+item.getFullDescription());
//			System.out.println("! Rerunning procedure "+name);
			p = procedures.get(name);
			item = new Item(item.category);
			p.execute(item, tabChooser);
		}

		return item;
	}

	@Override
	public Procedure getProcedure(String name) {
		return procedures.get(name);
	}

	@Override
	public Table getTable(String name) {
		return tables.get(name);
	}
}

package magicgenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//TODO doesn't handle double weapons
//TODO doesn't handle duplicated specials 

public class Generator implements NameSpace {
	Map<String,Procedure> procedures = new HashMap<String,Procedure>();
	Map<String,Table> tables = new HashMap<String,Table>();

	public Generator() {
        BufferedReader in;
		try {
			in = new BufferedReader(new FileReader("magic_tables.txt"));
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
	        	}
	        }
	        in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Item generate(int category, String procName) {
		Procedure p = procedures.get(procName);
		if (p == null) throw new IllegalArgumentException("Unknown procedure "+procName);
		Item item = new Item(category);
		p.execute(item);

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
			p.execute(item);
		}

		return item;
	}

	public Procedure getProcedure(String name) {
		return procedures.get(name);
	}

	public Table getTable(String name) {
		return tables.get(name);
	}
}

package magicgenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// an instruction in a procedure
abstract class Instruction {
	NameSpace namespace;

	public Instruction (NameSpace ns) {
		namespace = ns;
	}

	public abstract void execute(Item item);

	// parse a series of instructions separated by pipe symbols ('|')
	// it never returns null - if there are no instructions then an empty list will be returned
	static public List<Instruction> parseInstructionLine(NameSpace ns, String str) {
		// TODO this will fall over if separators are not used sensibly
		ArrayList<Instruction> list = new ArrayList<Instruction>();
		boolean inString = false;	// true when we are scanning a string
		int level = 0;	// >1 when we are inside a pair of parentheses
		int start = -1;	// index of the last separator before the current instruction
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (inString) {
				if (c == '"') {
					inString = false;
				}
				// ignore all other characters
			} else {
				if (c == '"') {
					inString = true;
				} else if (c == '(') {
					level++;
				} else if (c == ')') {
					level--;
				} else if (c == '|' && level == 0) {
					// end of instruction
					list.add(parseInstruction(ns, str.substring(start+1,i)));
					start = i;
				}
			}
		}
		if (inString) throw new IllegalArgumentException("Unterminated string in '"+str+"'");
		if (level > 0) throw new IllegalArgumentException("Unmatched parenthesis in '"+str+"'");
		list.add(parseInstruction(ns, str.substring(start+1)));
		return list;
	}

	static class Roll extends Instruction {
		String tableName;
		Set<Integer> ignore = new HashSet<Integer>();

		protected Roll(NameSpace ns) {
			super(ns);
		}

		public void execute(Item item) {
			Table t = namespace.getTable(tableName);
			if (t == null) throw new IllegalArgumentException("Table "+tableName+" not found");
			int roll;
			TableRow row;
			do {
				roll = Roller.roll();
				row = t.getTableRow(item.category, roll);
				//System.out.println("Rolled "+roll+" on '"+t.name+"' giving row "+row.number);
			} while (ignore.contains(row.number));
			for (Instruction i : row.instructions) {
				i.execute(item);
			}
		}

		// expects one of:
		// roll(<table_name>)
		// roll(<table_name>,ignore(<ignore_list>)
		public static Instruction parseInstruction(NameSpace ns, String str) {
			int end;
			if (str.contains("\",")) {
				end = str.indexOf("\",");
			} else {
				end = str.indexOf("\")");
			}
			Roll r = new Roll(ns);
			r.tableName = str.substring(str.indexOf("(\"")+2, end);
			if (str.contains("ignore(")) {
				String ignore[] = str.substring(str.indexOf("ignore(")+7,str.indexOf(')')).split(",");
				for (String i : ignore) r.ignore.add(Integer.parseInt(i));
			}
			return r;
		}

		public String toString() {
			if (ignore.size() == 0) {
				return "Roll("+tableName+")";
			} else {
				String s = new String();
				for (int i : ignore) {
					if (s.equals("")) s += i;
					else s += ","+i;
				}
				return "Roll("+tableName+","+s+")";
			}
		}
	}

	static public Instruction parseInstruction(NameSpace ns, String str) {
		try {
			if (str.startsWith("roll(")) {
				return Roll.parseInstruction(ns, str);
			} if (str.startsWith("validate(")) {
				return Validate.parseInstruction(ns, str);
			} if (str.startsWith("repeat(")) {
				return Repeat.parseInstruction(ns, str);
			} if (str.startsWith("run(")) {
				return RunProcedure.parseInstruction(ns, str);
			} if (str.startsWith("field(")) {
				return AddField.parseInstruction(ns, str);
			} if (str.startsWith("add_special(")) {
				return AddSpecial.parseInstruction(ns, str);
			} else {
				return SetValue.parseInstruction(ns, str);
			}
		} catch (Exception e) {
			System.err.println("Failed to parse instruction:");
			System.err.println(str);
			e.printStackTrace();
		}
		return null;
	}

	static class SetValue extends Instruction {
		public final static int OPERATION_SET = 0;
		public final static int OPERATION_ADD = 1;
		public final static int OPERATION_LIST = 2;
		public final static int OPERATION_PREPEND = 3;

		String fieldName;
		int operation = OPERATION_SET;
		String value;

		protected SetValue(NameSpace ns) {
			super(ns);
		}

		public void execute(Item item) {
			Field f = item.getField(fieldName);
			Object v = item.getValue(fieldName);

			String val = value;
			if (val.startsWith("@")) {
				// replace val with the current value of the field
				val = item.getValue(val.substring(1)).toString();
			}
			if (val.startsWith("\"")) {
				val = value.substring(1, value.length()-1);
			}

			if (f.type == Field.TYPE_NUMBER) {
				if (value.contains(".") || v instanceof Float) {
					float valFloat = Float.parseFloat(value);
					if (operation == OPERATION_ADD) {
						v = new Float(Float.parseFloat(v.toString()) + valFloat);
					} else {
						// OPERATION_SET
						v = new Float(valFloat);					
					}
				} else {
					int valInt = Integer.parseInt(value);
					if (operation == OPERATION_ADD) {
						v = new Integer(((Integer)v).intValue() + valInt);
					} else {
						// OPERATION_SET
						v = new Integer(valInt);					
					}
				}
				item.setValue(f,v);					
			} else {
				// TYPE_STRING
				if (operation == OPERATION_ADD || operation == OPERATION_LIST || operation == OPERATION_PREPEND) {
					if (((String)v).length() == 0) {
						item.setValue(f,val);					
					} else {
						if (operation == OPERATION_ADD) {
							item.setValue(f,new String(v + val));
						} else if (operation == OPERATION_LIST && val.length() > 0) {
							item.setValue(f,new String(v + ", " + val));
						} else if (operation == OPERATION_PREPEND) {
							item.setValue(f,new String(val + v));
						}
					}
				} else {
					// OPERATION_SET
					item.setValue(f,val);					
				}
			}
		}

		// expects one of:
		// <field_name>=<value>
		// <field_name>+<value>
		public static Instruction parseInstruction(NameSpace ns, String str) {
			SetValue s = new SetValue(ns);
			int end = str.indexOf('=');
			int len = 1;
			if (str.contains("+=")) {
				end = str.indexOf("+=");
				s.operation = SetValue.OPERATION_ADD;
				len = 2;
			} else if (str.contains("&=")) {
				end = str.indexOf("&=");
				s.operation = SetValue.OPERATION_LIST;
				len = 2;
			} else if (str.contains("^=")) {
				end = str.indexOf("^=");
				s.operation = SetValue.OPERATION_PREPEND;
				len = 2;
			}
			s.fieldName = str.substring(0, end);
			s.value = str.substring(end+len);
			return s;
		}

		public String toString() {
			String op = " = ";
			if (operation == OPERATION_ADD) op = " += ";
			return "SetValue: " + fieldName + op + value;
		}
	}

	// The 'validate(<factor>)' instruction does several things for armor, shields, and weapons:
	// 1. if the modifier is 0 then it just returns (as this indicates a specific item).
	// 2. it checks if the effective_modifier is 10 or less:
	//    if it is not then a 'invalid' number Field is added with value = 1 and a 'rerun'
	//    text Field is added and set to the name of the procedure to regenerate this type of item.
	// 3. it fixes the item's cost by adding effective_modifier*effective_modifier*1000*factor to the cost.
	// 4. it generates the final item name for the item.
	static class Validate extends Instruction {
		int multiplier = 1;
		String procName;

		protected Validate(NameSpace ns) {
			super(ns);
		}

		public static Instruction parseInstruction(NameSpace ns, String str) {
			Validate i = new Validate(ns);
			String args = str.substring(str.indexOf('(')+1,str.indexOf(')'));
			if (args.contains(",")) {
				i.procName = args.substring(0,args.indexOf(','));
				i.multiplier=Integer.parseInt(args.substring(args.indexOf(',')+1));
			} else {
				i.procName = args;
			}
			i.procName = i.procName.substring(1, i.procName.length()-1);	// strip quotes
			return i;
		}

		@SuppressWarnings("unchecked")
		public void execute(Item item) {
			int mod = (Integer)item.getValue("modifier");
			if (mod == 0) return;

			int effMod = (Integer)item.getValue("effective_modifier");
			// check effective modifier
			if (effMod > 10) {
				item.setValue(new Field("invalid",Field.TYPE_NUMBER), 1);
				item.setValue(new Field("rerun",Field.TYPE_TEXT), procName);
				return;
			}

			// fix cost
			Object c = item.getValue("cost");
			if (c instanceof Integer) {
				int cost = (Integer)item.getValue("cost");
				cost += effMod*effMod*1000*multiplier;
				c = new Integer(cost);
			} else {
				float cost = (Float)item.getValue("cost");
				cost += effMod*effMod*1000*multiplier;
				c = new Float(cost);
			}
			item.setValue("cost",c);

			// fix name
			String desc = (String)item.getValue("item");
			desc += " +"+mod;
			Field f = item.getField("specials_map");
			if (f != null) {
				Map<Integer,AddSpecial> map = (Map<Integer,AddSpecial>)item.getValue(f);
				if (map != null) {
					for (AddSpecial sp : map.values()) {
						desc += ", "+sp.specialName;
					}
				}
			}

			item.setValue("item", desc);
		}
	}

	static class Repeat extends Instruction {
		List<Instruction> instructions = new ArrayList<Instruction>();
		String timesString;

		protected Repeat(NameSpace ns) {
			super(ns);
		}

		public void execute(Item item) {
			int times = 0;
			if (timesString.startsWith("@")) {
				// replace val with the current value of the field
				times = ((Integer)item.getValue(timesString.substring(1))).intValue();
			} else {
				times = Integer.parseInt(timesString);
			}

			for (int i=0; i<times; i++) {
				for (Instruction inst : instructions) {
					inst.execute(item);
				}
			}
		}

		// expects one of:
		//repeat(@<field_name>,<instruction_list>)
		//repeat(<number>,<instruction_list>)
		public static Instruction parseInstruction(NameSpace ns, String str) {
			Repeat r = new Repeat(ns);
			r.timesString = str.substring(str.indexOf('(')+1, str.indexOf(','));
			r.instructions = Instruction.parseInstructionLine(ns, str.substring(str.indexOf(',')+1, str.lastIndexOf(')')));
			return r;
		}
	}

	static class RunProcedure extends Instruction {
		String procedureName;

		protected RunProcedure(NameSpace ns) {
			super(ns);
		}

		public void execute(Item item) {
			Procedure p = namespace.getProcedure(procedureName);
			if (p == null) throw new IllegalArgumentException("Procedure '"+procedureName+"' not found");
			p.execute(item);
		}

		// expects:
		//run(<procedure>)
		public static Instruction parseInstruction(NameSpace ns, String str) {
			RunProcedure r = new RunProcedure(ns);
			r.procedureName = str.substring(str.indexOf('"')+1, str.lastIndexOf('"'));
			return r;
		}
	}

	static class AddField extends Instruction {
		Field field;

		protected AddField(NameSpace ns) {
			super(ns);
		}

		public void execute(Item item) {
			item.addField(field);
		}

		// expects a valid fied description (see Field constructor)
		public static Instruction parseInstruction(NameSpace ns, String str) {
			AddField f = new AddField(ns);
			f.field = new Field(str);
			return f;
		}
	}

	//TODO this can't handle multiple cases of Bane - need to have a type that allows dups and
	// also need to store special properly in the map (not just store this instruction)
	static class AddSpecial extends Instruction {
		int type;
		int level = 0;
		String tableName;
		String specialName;
		List<Instruction> instructions = new ArrayList<Instruction>();

		protected AddSpecial(NameSpace ns) {
			super(ns);
		}

		@SuppressWarnings("unchecked")
		public void execute(Item item) {
//			System.out.println("AddSpecial("+specialName+")");
			// first get the map if it exists otherwise create it
			Map<Integer,AddSpecial> map;
			Field f = item.getField("specials_map");
			if (f == null) {
				f = new Field("specials_map",Field.TYPE_OBJECT);
				map = new HashMap<Integer,AddSpecial>();
				item.setValue(f, map);
			} else {
				map = (Map<Integer,AddSpecial>)item.getValue(f);
			}

			boolean add = false;
			if (!map.containsKey(type)) {
				// no current special of this type - add this one
				add = true;

			} else {
				if (tableName == null) {
					// add this special if it's level is greater than the existing one
					AddSpecial existing = map.get(type);
					if (level > existing.level) {
						add = true;
					}
				} else {
					// reroll on the specified table
					Table t = namespace.getTable(tableName);
					if (t == null) throw new IllegalArgumentException("Table "+tableName+" not found");
					int roll = Roller.roll();
					TableRow row = t.getTableRow(item.category, roll);
					//System.out.println("Rolled "+roll+" on '"+t.name+"' giving row "+row.number);
					for (Instruction i : row.instructions) {
						i.execute(item);
					}
				}
			}

			if (add) {
				// run the instructions since we're adding this special
				for (Instruction inst : instructions) {
					inst.execute(item);
				}

				if (specialName.startsWith("@")) {
					// replace specialName with the current value of the field
					specialName = item.getValue(specialName.substring(1)).toString();
				}

				map.put(type, this);
			}
		}

		// expects:
		// add_special(<type>,<level/table>,<name>,<instructions>)
		// <type> is a number indicating the class of special. add_special won't add two specials of
		// the same type.
		// <level/table> is either a number indicating the level of the special or the name of the
		// table to roll on if a special with the same <type> is already in the item. if the level is
		// specified then the old special is replaced if the new special's level is higher.
		// <name> is the name of the special. current commas are no allowed
		// <instructions> are the instructions to run if the special is added
		public static Instruction parseInstruction(NameSpace ns, String str) {
			AddSpecial s = new AddSpecial(ns);

			String args = str.substring(str.indexOf('(')+1, str.lastIndexOf(')'));
			int comma = args.indexOf(',');
			s.type = Integer.parseInt(args.substring(0, comma));

			args = args.substring(comma+1);
			comma = args.indexOf(',');
			String l = args.substring(0, comma);
			// determine if l is a number and set level or tableName accordingly
			try {
				s.level = Integer.parseInt(l);
			} catch (NumberFormatException e) {
				s.tableName = l.substring(l.indexOf('"')+1, l.lastIndexOf('"'));
			}

			args = args.substring(comma+1);
			comma = args.indexOf(',');
			s.specialName = args.substring(0, comma);
			if (s.specialName.contains("\"")) {
				s.specialName = s.specialName.substring(args.indexOf('"')+1, s.specialName.lastIndexOf('"'));
			}

			// remainder are instructions
			s.instructions = Instruction.parseInstructionLine(ns, args.substring(comma+1));

//			System.out.println("Parsed add_special:");
//			System.out.println("  type = "+s.type);
//			System.out.println("  level = "+s.level);
//			System.out.println("  table = "+s.tableName);
//			System.out.println("  name = "+s.specialName);
//			System.out.println("  instructions:");
//			for (Instruction i : s.instructions) {
//				System.out.println("    "+i);
//			}
			return s;
		}
	}
}

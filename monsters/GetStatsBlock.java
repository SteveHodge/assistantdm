package monsters;
import gamesystem.AbilityScore;
import gamesystem.SizeCategory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class GetStatsBlock {
	static String baseName = "D:\\Programming\\Workspace\\AssistantDM\\html\\monsters\\";

	public static void main(String[] args) {
		List<StatisticsBlock> blocks = new ArrayList<StatisticsBlock>();
		Source[] sources = new Source[] {
				new Source("Monster Manual","monster_manual"),
				new Source("Monster Manual II", "monster_manual_ii"),
				new Source("Monster Manual III", "monster_manual_iii"),
				new Source("Ptolus","ptolus")
		};

		int blockCount = 0;
		int fileCount = 0;
		for (Source source : sources) {
			File dir = new File(baseName+source.getLocation());
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					//if (name.equals("HalfGolem.htm")) return true;
					if (name.toLowerCase().endsWith("html")) return true;
					if (name.toLowerCase().endsWith("htm")) return true;
					return false;
				}
			});

			for (File file : files) {
				//				System.out.println("File "+file.getName());
				List<StatisticsBlock> fileBlocks = StatisticsBlock.parseFile(source, file);
				//				for (StatisticsBlock b : fileBlocks) {
				//					System.out.println("\t"+b.getName());
				//				}
				blocks.addAll(fileBlocks);
			}

			System.out.println(source.getName()+": "+files.length+" files, "+(blocks.size()-blockCount)+" stats blocks");
			blockCount = blocks.size();
			fileCount += files.length;
		}
		System.out.println("Total: "+fileCount+" files, "+blockCount+" stats blocks");

		// check for unique names:
		HashSet<String> names = new HashSet<String>();
		for (StatisticsBlock block : blocks) {
			if (names.contains(block.getName())) {
				System.out.println("Found duplicate name: " + block.getName() + " in " + block.get(StatisticsBlock.Property.URL));
				System.out.println(block);
			} else {
				names.add(block.getName());
			}
		}

		//		checkBABGrapple(blocks);

		//		HashSet<String> subtypes = new HashSet<String>();
		//		for (StatisticsBlock block : blocks) {
		//			String[] subs = block.getSubtypes();
		//			if (subs != null) {
		//				//System.out.println(block.getName()+": "+block.get(StatisticsBlock.Property.SIZE_TYPE));
		//				for (String s : subs) {
		//					subtypes.add(s);
		//					//System.out.println("'"+s+"'");
		//					if (s.equals("Guardinal") || s.equals("Eladrin") || s.equals("Tanar'ri") || s.equals("Baatezu")) {
		//						//System.out.println(block.getName()+" ("+block.get(StatisticsBlock.Property.URL)+"): "+block.get(StatisticsBlock.Property.SIZE_TYPE));
		//						//System.out.println("SA: "+block.get(StatisticsBlock.Property.SPECIAL_ATTACKS));
		//						//System.out.println("SQ: "+block.get(StatisticsBlock.Property.SPECIAL_QUALITIES));
		//						//System.out.println();
		//					}
		//				}
		//			}
		//		}
		//		for (String value : subtypes) {
		//			System.out.println("'"+value+"'");
		//		}

		// get unique values for specified property
		//		for (String value : getUniqueValues(blocks, StatisticsBlock.Property.ENVIRONMENT)) {
		//			System.out.println("'"+value+"'");
		//		}
		//
		//		for (StatisticsBlock block : blocks) {
		//			String env = block.get(StatisticsBlock.Property.ENVIRONMENT);
		//			if (env == null) System.out.println("Null environment for " + block.getName());
		//			else if (env.contains("hill,")) System.out.println(block.getName());
		//		}

		// check size, space, reach combos
//		Set<String> sizes = new HashSet<String>();
//		Set<String> types = new HashSet<String>();
//		Set<String> subtypes = new HashSet<String>();
//		for (String value : getUniqueValues(blocks, StatisticsBlock.Property.SIZE_TYPE)) {
//			String size = value.substring(0, value.indexOf(' '));
//			String type = value.substring(value.indexOf(' ') + 1);
//			String subtype = "";
//			if (type.indexOf('(') > -1) {
//				subtype = type.substring(type.indexOf('(') + 1, type.indexOf(')'));
//				type = type.substring(0, type.indexOf('(') - 1);
//			}
//			String[] subs = subtype.split(",");
//			sizes.add(size);
//			types.add(type);
//			for (String s : subs) {
//				subtypes.add(s.trim());
//			}
//		}
//		List<String> sorted = new ArrayList<String>(sizes);
//		Collections.sort(sorted);
//		System.out.println("Sizes:");
//		for (String value : sorted) {
//			System.out.println("'" + value + "'");
//		}
//
//		sorted = new ArrayList<String>(types);
//		Collections.sort(sorted);
//		System.out.println("Types:");
//		for (String value : sorted) {
//			System.out.println("'" + value + "'");
//		}
//
//		sorted = new ArrayList<String>(subtypes);
//		Collections.sort(sorted);
//		System.out.println("Subtypes:");
//		for (String value : sorted) {
//			System.out.println("'" + value + "'");
//		}

		Map<String, StatisticsBlock> unique = new HashMap<String, StatisticsBlock>();
		Map<String, Integer> count = new HashMap<String, Integer>();
		Map<String, StatisticsBlock> allReach = new HashMap<String, StatisticsBlock>();
		for (StatisticsBlock block : blocks) {
			String space = block.get(StatisticsBlock.Property.SPACE_REACH);
			if (space == null) {
				System.out.println("null space/reach in " + block.getName());
				continue;
			}
			allReach.put(space, block);
			if (space.indexOf('(') > -1) {
				space = space.substring(0, space.indexOf('(') - 1);
			}
			SizeCategory size = block.getSize();
			space = size + ": " + space;
			unique.put(space, block);
			if (count.containsKey(space)) {
				int c = count.get(space);
				c++;
				count.put(space, c);
			} else {
				count.put(space, 1);
			}
		}

		System.out.println();
		List<String> sorted = new ArrayList<String>(unique.keySet());
		Collections.sort(sorted);
		for (String s : sorted) {
			StatisticsBlock b = unique.get(s);
			System.out.print(s + ": " + count.get(s) + " (" + b.getName() + " - " + b.getSource().getName() + ")");
			System.out.println(" space = " + b.getSpace() + ", reach = " + b.getReach());
		}

		System.out.println("\nAll Space/Reach variants:");
		sorted = new ArrayList<String>(allReach.keySet());
		Collections.sort(sorted);
		for (String s : sorted) {
			System.out.println(s + ": " + allReach.get(s).getName() + " - " + allReach.get(s).getSource().getName());
		}

		//		for (StatisticsBlock block : blocks) {
		//			try {
		//				if (block.get(StatisticsBlock.Property.HITDICE) != null) {
		//					if (!block.get(StatisticsBlock.Property.HITDICE).equals(block.getHitDice().toString() + " (" + block.getDefaultHPs() + " hp)")) {
		//						System.out.print(block.getName() + ": Hit dice: '" + block.get(StatisticsBlock.Property.HITDICE) + "' <> '");
		//						System.out.println(block.getHitDice().toString() + " (" + block.getDefaultHPs() + " hp)'");
		//					}
		//					//System.out.println(block.getName() + ": " + block.getHitDice()  + " (" + block.getDefaultHPs() + " hp)" + ": " + block.getHitDice().roll());
		////					if (block.getName().equals("Elder Xorn")) {
		////						System.out.println(block.getName() + ": " + block.getHitDice()  + " (" + block.getDefaultHPs() + " hp)" + ":");
		////						doTrails(100000,block.getHitDice());
		////					}
		//				}
		//			} catch (Exception e) {
		//				System.err.println("Exception processing "+block.getName());
		//				e.printStackTrace();
		//			}
		//			System.out.println(block.getInitiativeModifier()+" - " + block.getName());
		//		}
	}

	public static void checkBABGrapple(List<StatisticsBlock> blocks) {
		HashMap<String,Integer> sizeMod = new HashMap<String,Integer>();
		sizeMod.put("Colossal", -8);
		sizeMod.put("Gargantuan", -4);
		sizeMod.put("Huge", -2);
		sizeMod.put("Large", -1);
		sizeMod.put("Medium", 0);
		sizeMod.put("Small", +1);
		sizeMod.put("Tiny", +2);
		sizeMod.put("Diminutive", +4);
		sizeMod.put("Fine", +8);

		HashMap<String,Integer> grappleMod = new HashMap<String,Integer>();
		grappleMod.put("Colossal", 16);
		grappleMod.put("Gargantuan", 12);
		grappleMod.put("Huge", 8);
		grappleMod.put("Large", 4);
		grappleMod.put("Medium", 0);
		grappleMod.put("Small", -4);
		grappleMod.put("Tiny", -8);
		grappleMod.put("Diminutive", -12);
		grappleMod.put("Fine", -16);

		HashMap<String,Integer> typeMult = new HashMap<String,Integer>();
		typeMult.put("Aberration",3);
		typeMult.put("Animal",3);
		typeMult.put("Construct",3);
		typeMult.put("Dragon",4);
		typeMult.put("Elemental",3);
		typeMult.put("Fey",2);
		typeMult.put("Giant",3);
		typeMult.put("Humanoid",3);
		typeMult.put("Magical Beast",4);
		typeMult.put("Monstrous Humanoid",4);
		typeMult.put("Ooze",3);
		typeMult.put("Outsider",4);
		typeMult.put("Plant",3);
		typeMult.put("Undead",2);
		typeMult.put("Vermin",3);

		for (StatisticsBlock block : blocks) {
			if (block.get(StatisticsBlock.Property.SIZE_TYPE) == null || block.get(StatisticsBlock.Property.SIZE_TYPE) == "") {
				System.out.println("No size/type: "+block.getName());
			}
			try {
				// BAB depends on HitDice and type:
				int hd = block.getHitDice().getNumber();
				String type = block.getType();
				if (!typeMult.containsKey(type)) {
					System.out.println("unknown type in "+block.getName()+": "+type);
				}
				SizeCategory size = block.getSize();
				if (size == null) {
					System.out.println("unknown size in " + block.getName() + ": " + block.get(StatisticsBlock.Property.SIZE_TYPE));
				}
				int strMod = 0;
				int str = block.getAbilityScore(AbilityScore.Type.STRENGTH);
				if (str > -1) strMod = AbilityScore.getModifier(str);

				int bab = hd*typeMult.get(type)/4;
				int attack = bab + sizeMod.get(size) + strMod;
				int grapple = bab + grappleMod.get(size) + strMod;

				StringBuilder property = new StringBuilder();
				if (bab>-1) property.append("+");
				property.append(bab+"/");
				if (block.get(StatisticsBlock.Property.SIZE_TYPE).toLowerCase().contains("incorporeal")) {
					property.append("—");
				} else {
					if (grapple>-1) property.append("+");
					property.append(grapple);
				}

				String existing = block.get(StatisticsBlock.Property.BASE_ATTACK_GRAPPLE);
				if (existing == null || !existing.equals(property.toString())) {
					System.out.println(block.get(StatisticsBlock.Property.URL) + " - " + block.getName() + ": BAB = " + bab + ", Grapple = " + grapple + ", Attack = " + attack);
					System.out.println("Attack: " + block.get(StatisticsBlock.Property.ATTACK));
					String feats = block.get(StatisticsBlock.Property.FEATS);
					if (feats != null && feats.toLowerCase().contains("weapon")) {
						System.out.println("Feats: "+feats);
					}
					String spAtt = block.get(StatisticsBlock.Property.SPECIAL_ATTACKS);
					if (spAtt != null && spAtt.toLowerCase().contains("grab")) {
						System.out.println("Special Attacks: "+spAtt);
					}
					if (existing != null && existing.length() > 0) System.out.println("Existing: "+existing);
					System.out.println(property);
					System.out.println();
				}

			} catch (Exception e) {
				System.err.println("Exception processing "+block.getName()+": "+e);
				//e.printStackTrace();
			}
		}
	}

	// get unique values for specified property
	public static Set<String> getUniqueValues(List<StatisticsBlock> blocks, StatisticsBlock.Property property) {
		HashSet<String> values = new HashSet<String>();
		for (StatisticsBlock block : blocks) {
			String value = block.get(property);
			values.add(value);
		}
		return values;
	}

	public static void printIndex(List<StatisticsBlock> blocks) {
		// Generate index:
		for (StatisticsBlock block : blocks) {
			String size = "", type = "";
			String sizeAndType = block.get(StatisticsBlock.Property.SIZE_TYPE);
			if (sizeAndType != null) {
				size = sizeAndType.split(" ")[0];
				type = sizeAndType.substring(size.length()+1);
			}
			String cr = block.get(StatisticsBlock.Property.CR);
			if (cr.equals("½")) cr = "1/2";
			if (cr.equals("¼")) cr = "1/4";

			System.out.println("<Monster name=\""+block.getName()
					+ "\" url=\"" + block.get(StatisticsBlock.Property.URL)
					+"\" size=\""+size
					+"\" type=\""+type
					+ "\" environment=\"" + block.get(StatisticsBlock.Property.ENVIRONMENT)
					+"\" cr=\""+cr
					+"\"/>");
		}
	}

	public static void doTrails(int number, HitDice hd) {
		int min = Integer.MAX_VALUE;
		int max = 0;
		int total = 0;
		int[] results = new int[number];

		for (int i = 0; i < number; i++) {
			int roll = hd.roll();
			results[i] = roll;
			total += roll;
			if (roll < min) min = roll;
			if (roll > max) max = roll;
		}

		System.out.println("Testing "+hd+" "+number+" times");
		System.out.println("Minimum result = "+min);
		System.out.println("Maximum result = "+max);
		System.out.println("Average result = "+(float)total/number);

		int[] freq = new int[max-min+1];
		for (int i = 0; i < number; i++) {
			freq[results[i]-min]++;
		}
		for (int i = 0; i < freq.length; i++) {
			System.out.println("  "+(i+min)+": "+freq[i]);
		}
	}
}


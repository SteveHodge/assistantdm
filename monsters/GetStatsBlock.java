package monsters;
import gamesystem.AbilityScore;
import gamesystem.CharacterClass;
import gamesystem.Creature;
import gamesystem.HitDice;
import gamesystem.Modifier;
import gamesystem.MonsterType;
import gamesystem.SaveProgression;
import gamesystem.SavingThrow;
import gamesystem.SizeCategory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monsters.Monster.MonsterAttackForm;
import monsters.Monster.MonsterAttackRoutine;
import monsters.StatisticsBlock.AttackRoutine;
import monsters.StatisticsBlock.AttackRoutine.Attack;
import monsters.StatisticsBlock.Field;


public class GetStatsBlock {
	static String baseName = "D:\\Programming\\git\\assistantdm\\html\\monsters\\";

	public static void main(String[] args) {
		List<StatisticsBlock> blocks = new ArrayList<>();
		Source[] sources = new Source[] {
				new Source("Monster Manual", "monster_manual"),
//				new Source("Monster Manual II", "monster_manual_ii"),
//				new Source("Monster Manual III", "monster_manual_iii"),
//				new Source("Ptolus", "ptolus"),
//				new Source("Cthulhu", "cthulhu")
		};

		int blockCount = 0;
		int fileCount = 0;
		for (Source source : sources) {
			File dir = new File(baseName+source.getLocation());
			System.out.println(dir);
			File[] files = dir.listFiles((d, name) -> {
				//if (name.equals("HalfGolem.htm")) return true;
				if (name.equals("All.html")) return false;
				if (name.toLowerCase().endsWith("html")) return true;
				if (name.toLowerCase().endsWith("htm")) return true;
				if (name.toLowerCase().endsWith("xml")) return true;
				return false;
			});

			for (File file : files) {
				try {
					//				System.out.println("File "+file.getName());
					List<StatisticsBlock> fileBlocks = StatisticsBlock.parseFile(source, file);
					//				for (StatisticsBlock b : fileBlocks) {
					//					System.out.println("\t"+b.getName());
					//				}
					blocks.addAll(fileBlocks);
				} catch (Exception e) {
					System.err.println("Exception processing " + file + ":");
					e.printStackTrace();
				}
			}

			System.out.println(source.getName()+": "+files.length+" files, "+(blocks.size()-blockCount)+" stats blocks");
			blockCount = blocks.size();
			fileCount += files.length;
		}
		System.out.println("Total: "+fileCount+" files, "+blockCount+" stats blocks");

		// check for unique names:
		HashSet<String> names = new HashSet<>();
		for (StatisticsBlock block : blocks) {
			if (names.contains(block.getName())) {
				System.out.println("Found duplicate name: " + block.getName() + " in " + block.get(StatisticsBlock.Field.URL));
				System.out.println(block);
			} else {
				names.add(block.getName());
			}
		}

		for (StatisticsBlock block : blocks) {
			try {
				validateBlock(block);
			} catch (Exception e) {
				try {
					System.err.println("Exception processing '" + block.getName() + "' from " + block.getURL());
				} catch (MalformedURLException e1) {
					System.err.println("Exception processing '" + block.getName() + "' " + e1.getMessage());
				}
				e.printStackTrace();
			}
		}

		// get unique values for specified property
//		List<String> sorted = new ArrayList<>(getUniqueValues(blocks, StatisticsBlock.Property.FULL_ATTACK));
//		Collections.sort(sorted);
//		for (String value : sorted) {
//			System.out.println("'" + value + "'");
//		}

		//		for (StatisticsBlock block : blocks) {
		//			String env = block.get(StatisticsBlock.Property.ENVIRONMENT);
		//			if (env == null) System.out.println("Null environment for " + block.getName());
		//			else if (env.contains("hill,")) System.out.println(block.getName());
		//		}

		// check size, space, reach combos
//		Set<String> sizes = new HashSet<>();
//		Set<String> types = new HashSet<>();
//		Set<String> subtypes = new HashSet<>();
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
//		List<String> sorted = new ArrayList<>(sizes);
//		Collections.sort(sorted);
//		System.out.println("Sizes:");
//		for (String value : sorted) {
//			System.out.println("'" + value + "'");
//		}
//
//		sorted = new ArrayList<>(types);
//		Collections.sort(sorted);
//		System.out.println("Types:");
//		for (String value : sorted) {
//			System.out.println("'" + value + "'");
//		}
//
//		sorted = new ArrayList<>(subtypes);
//		Collections.sort(sorted);
//		System.out.println("Subtypes:");
//		for (String value : sorted) {
//			System.out.println("'" + value + "'");
//		}

		// list space/reach combinations for each size:
//		Map<String, StatisticsBlock> unique = new HashMap<>();
//		Map<String, Integer> count = new HashMap<>();
//		Map<String, StatisticsBlock> allReach = new HashMap<>();
//		for (StatisticsBlock block : blocks) {
//			String space = block.get(StatisticsBlock.Property.SPACE_REACH);
//			if (space == null) {
//				System.out.println("null space/reach in " + block.getName());
//				continue;
//			}
//			allReach.put(space, block);
//			if (space.indexOf('(') > -1) {
//				space = space.substring(0, space.indexOf('(') - 1);
//			}
//			SizeCategory size = block.getSize();
//			space = size + ": " + space;
//			unique.put(space, block);
//			if (count.containsKey(space)) {
//				int c = count.get(space);
//				c++;
//				count.put(space, c);
//			} else {
//				count.put(space, 1);
//			}
//		}
//
//		System.out.println();
//		List<String> sorted = new ArrayList<>(unique.keySet());
//		Collections.sort(sorted);
//		for (String s : sorted) {
//			StatisticsBlock b = unique.get(s);
//			System.out.print(s + ": " + count.get(s) + " (" + b.getName() + " - " + b.getSource().getName() + ")");
//			System.out.println(" space = " + b.getSpace() + ", reach = " + b.getReach());
//		}
//
//		System.out.println("\nAll Space/Reach variants:");
//		sorted = new ArrayList<>(allReach.keySet());
//		Collections.sort(sorted);
//		for (String s : sorted) {
//			System.out.println(s + ": " + allReach.get(s).getName() + " - " + allReach.get(s).getSource().getName());
//		}

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

		// validate saves and get unique values:
		// checkSaves(blocks);

		// get unique hitdice:
//		Map<String, StatisticsBlock> uniqueValues = new HashMap<>();
//		for (StatisticsBlock block : blocks) {
//			try {
//				if (block.get(StatisticsBlock.Property.HITDICE) != null) {
//					HitDice hitdice = block.getHitDice();
//					uniqueValues.put(hitdice.toString(), block);
//				}
//			} catch (Exception e) {
//				System.err.println("Exception processing " + block.getName());
//				e.printStackTrace();
//			}
//		}
//		List<String> sorted = new ArrayList<>(uniqueValues.keySet());
//		Collections.sort(sorted);
//		for (String s : sorted) {
//			System.out.println(s + ": " + uniqueValues.get(s).getName());
//		}

//		checkBABGrapple(blocks);

		// check supplied default hitpoints matches the average of the hitdice
//		for (StatisticsBlock block : blocks) {
//			try {
//				HitDice dice = block.getHitDice();
//				if ((int) dice.getMeanRoll() != block.getDefaultHPs()) {
//					System.out.println("supplied = " + block.getDefaultHPs() + ", calculated = " + (int) dice.getMeanRoll() + ": " + getNameURL(block));
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}

//		checkBuiltAttacks(blocks, false);
//		checkBuiltAttacks(blocks, true);

//		checkAllAttacks(blocks, false);
//		checkAllAttacks(blocks, true);

		// check statistic
//		Field f = Field.BASE_ATTACK_GRAPPLE;
//		for (StatisticsBlock block : blocks) {
//			Monster monster = StatsBlockCreatureView.getMonster(block);
//			StatsBlockCreatureView view = StatsBlockCreatureView.getView(monster);
//			if (!view.getField(f).equals(block.get(f))) {
//				System.out.println(getNameURL(block) + ":");
//				System.out.println("  calculated: " + view.getField(Field.BASE_ATTACK_GRAPPLE));
//				System.out.println("  supplied:   " + block.get(Field.BASE_ATTACK_GRAPPLE));
//			}
//		}

//		getAllACComponents(blocks);
	}

	// checks the supplied block for consistency with the rules. primarily compares values found in the block with values from a Monster instance created
	// from the block (at least for those values that are built/calculated from scratch in the Monster instance).
	private static void validateBlock(StatisticsBlock block) {
		// FIXME handle templates, creatures with character classes
		// Values that are checked:
		// BAB, grapple
		// TODO attack bonuses
		// TODO number of feats
		// saves
		// TODO number of skills
		// TODO hit dice bonuses
		// TODO size/reach

		if (block.getClassLevels().size() == 0) {
			//System.err.println(block.getName() + " skipped due to no class levels");
			return;
		}

		@SuppressWarnings("serial")
		class Messages extends ArrayList<String> {
			void checkValues(String field, int parsed, int calculated) {
				if (parsed != calculated) {
					add(field + ": " + parsed + " != calculated value of " + calculated);
				}
			}

			void checkValues(String field, int parsed, int calculated, String msg) {
				if (parsed != calculated) {
					add(field + ": " + parsed + " != calculated value of " + calculated + ": " + msg);
				}
			}

			void checkSave(SavingThrow.Type type, Monster m) {
				int parsed = block.getSavingThrow(type);
				SavingThrow save = m.getSavingThrowStatistic(type);
				if (save.getValue() == parsed) return;
				int fast = m.getSaveUsingProgression(type, SaveProgression.FAST);
				int slow = m.getSaveUsingProgression(type, SaveProgression.SLOW);
				SaveProgression found = null;
				if (fast == parsed) {
					found = SaveProgression.FAST;
				} else if (slow == parsed) {
					found = SaveProgression.SLOW;
				}
				if (found == null) {
					add(type.name() + " Save: " + parsed + " != value of " + save.getValue() + " or calculated values of " + fast + " (fast) or " + slow + " (slow)");
					if (m.race != null && m.level != null) {
						add(type.name() + " Save: racial save bonus = " + m.race.getBaseSave(type) + ", level save bonus = " + m.level.getBaseSave(type) + ", calculation = " + save.getSummary());
						//add(type.name() + " Save: base = " + m.hitDice.getBaseSave(type) + ", override = " + save.getBaseOverride());
					}
				} else if (found != m.getSaveProgression(type)) {
					add(type.name() + " Save: using " + found + " progression rather than " + m.getSaveProgression(type) + " as is usual for " + m.race.getType());
				}
			}
		}
		;

		Messages messages = new Messages();
		Monster m = StatsBlockCreatureView.createMonster(block);

		messages.checkValues("BAB", block.getBAB(), m.getBAB().getValue(), m.getBAB().toString());
		messages.checkValues("Grapple", block.getGrapple(), m.race.hasSubtype("Incorporeal") || m.race.hasSubtype("Swarm") ? Integer.MIN_VALUE : m.getAttacksStatistic().getGrappleValue());	// TODO handle types/subtypes with no grapple (incorporal, swarms), racial bonuses
		messages.checkSave(SavingThrow.Type.FORTITUDE, m);
		messages.checkSave(SavingThrow.Type.REFLEX, m);
		messages.checkSave(SavingThrow.Type.WILL, m);

		if (messages.size() == 0) {
			System.out.println(block.getName() + " OK");
		} else {
			System.out.println(block.getName());
			for (String s : messages) {
				System.out.println("  " + s);
			}
			System.out.println("  Feats: " + block.get(Field.FEATS));
			System.out.println("  Subtypes: " + String.join(", ", m.race.subtypes));
			System.out.println("  Classes: " + m.level);
		}
	}

	static void getAllACComponents(List<StatisticsBlock> blocks) {
		Map<String, StatisticsBlock> examples = new HashMap<>();
		Set<String> types = new HashSet<>();

		for (StatisticsBlock block : blocks) {
			Set<Modifier> comps = block.getACModifiers();
			for (Modifier m : comps) {
				types.add(m.getType());
				examples.put(m.getType(), block);
			}
		}

		List<String> sorted = new ArrayList<>(types);
		Collections.sort(sorted);
		System.out.println("--- AC Components ---");
		for (String n : sorted) {
			System.out.println(n + " - " + getNameURL(examples.get(n)));
		}
	}

	static void checkBuiltAttacks(List<StatisticsBlock> blocks, boolean full) {
		// check that attacks are parsed correctly:
		for (StatisticsBlock block : blocks) {
			try {
				String value = block.get(full ? Field.FULL_ATTACK : Field.ATTACK);

				Monster monster = StatsBlockCreatureView.createMonster(block);
				StringBuilder b = new StringBuilder();
				StringBuilder parsedStr = new StringBuilder();
				List<MonsterAttackRoutine> attackRoutines = full ? monster.fullAttackList : monster.attackList;
				List<AttackRoutine> parsed = block.getAttacks(full);
				for (int i = 0; i < attackRoutines.size(); i++) {
					if (i > 0) {
						b.append("; or ");
						parsedStr.append("; or ");
					}
					b.append(attackRoutines.get(i));
					parsedStr.append(parsed.get(i).getDescription());
				}

				String cleanValue = value.replace(";", "").replace(",", "").replace("*", "").replace("–", "-").replaceAll("\\s+", " ");
				String cleanBuilt = b.toString().replace(";", "").replace("–", "-").replace(",", "");
				if (cleanBuilt.length() == 0) cleanBuilt = "—";
				if (cleanValue.equals(cleanBuilt.toString())) {
//					System.out.println("OK: " + getNameURL(block));
				} else {
					System.out.println();
					System.out.println(getNameURL(block) + ": ");
					System.out.println("HTML  : " + value);
//					System.out.println("clean: " + cleanValue);

					System.out.println("built : " + b.toString());
//					System.out.println("built: " + cleanBuilt);
//					System.out.println("parsed: " + parsedStr);

					for (MonsterAttackRoutine r : full ? monster.fullAttackList : monster.attackList) {
						for (MonsterAttackForm f : r.attackForms) {
							StringBuilder atk = new StringBuilder();
							if (f.automatic) {
								atk.append("(automatic) ");
								atk.append(f).append(": ");
							} else {
								atk.append(f.attack.natural ? "(natural) " : "(manufactured) ");
								atk.append(f).append(": ");
								for (Modifier m : f.attack.getModifierSet()) {
									atk.append(m).append(", ");
								}
								if (block.getBAB() > 0) atk.append("+");
								atk.append(block.getBAB()).append(" BAB");
								atk.append(". Damage: ");
								for (Modifier m : f.attack.getDamageModifiersSet()) {
									atk.append(m).append(", ");
								}
							}
//							atk.append(" (str mult = ").append(f.attack.strMultiplier).append(")");
							System.out.println(atk);
						}
					}
					System.out.println();
				}
			} catch (Exception e) {
				System.err.println(getNameURL(block) + ": ");
				e.printStackTrace();
			}
		}
	}

	static void checkDamage(List<StatisticsBlock> blocks) {
		final Pattern wsPattern = Pattern.compile("weapon specialization \\(([^\\)]+)\\)");
//		final Pattern strLimitPattern = Pattern.compile("\\(\\+(\\d+) Str bonus\\)");

		for (StatisticsBlock block : blocks) {
			int str = block.getAbilityScore(AbilityScore.Type.STRENGTH);

			String feats = block.get(Field.FEATS);
			if (feats == null) feats = "";
			feats = feats.toLowerCase();

			Set<String> weaponSpecs = new HashSet<>();
			Matcher matcher = wsPattern.matcher(feats);
			while (matcher.find()) {
				weaponSpecs.add(matcher.group(1));
//				System.out.println(block.getName() + ": weapon focus = " + weaponFocus);
			}

			try {
				boolean ok = true;

				StringBuilder b = new StringBuilder();
				List<AttackRoutine> attackRoutines = block.getAttacks(true);
				for (int i = 0; i < attackRoutines.size(); i++) {
					if (i > 0) b.append("; or ");
					AttackRoutine attackRoutine = attackRoutines.get(i);

					for (int j = 0; j < attackRoutine.attacks.size(); j++) {
						Attack a = attackRoutine.attacks.get(j);
						if (j > 0) b.append(" and ");
						b.append(a);

						if (a.automatic) {
//							System.out.println(a.getFullDescription() + " " + getNameURL(block));
							continue;
						}

						int strMod = AbilityScore.getModifier(str);

						int atkBonus = a.calculateAttackBonus();
						if (a.attackBonuses[0] != atkBonus) {
							b.append(" [calculated ").append(atkBonus);
							Map<String, Integer> modifiers = a.getModifiers();
							for (String type : modifiers.keySet()) {
								b.append(", ").append(modifiers.get(type)).append(" ").append(type);
							}
//								b.append(" (").append(desc).append(" ").append(bonus).append(")");
//							if (attackRoutine.has_mfg_secondary) b.append(" mfgd 2nd");
//							if (attackRoutine.has_non_light_secondary) b.append(" non-light 2nd");
							b.append("]");

							ok = false;
						}

						if (a.damageParsed) {
							// default strength bonus:
							// there are exceptions: some secondary natural attacks get full bonus or even 1.5,
							// most ranged projectile weapons get no bonus
							int strMult = 1;		// half bonus for secondary attacks
							if (a.primary) {
								if (attackRoutine.attacks.size() == 1 && a.number == 1)
									strMult = 3;	// 1.5x bonus for single primary attack with no secondary attack
								else
									strMult = 2;	// 1x bonus for primary attacks with secondary attacks
							}

							int mods = a.enhancementBonus;

							// check for weapon focus
							for (String weaponSpec : weaponSpecs) {
								if (a.description.toLowerCase().contains(weaponSpec)
//										|| plurals.containsKey(weaponFocus) && desc.contains(plurals.get(weaponFocus))
										) {
//										System.out.println(block.getName() + ": weapon focus (" + desc + ")");
									mods += 2;
								}
							}

							// apply any modifers
							for (String m : a.damageModifiers.keySet()) {
								mods += a.damageModifiers.get(m);
							}

							// apply any strength limit specified for a ranged weapon
							if (a.ranged && a.strLimit < strMod) {
								strMod = a.strLimit;
							}

							int supplied = a.damageBonus - mods;
							int calculated = strMod * strMult / 2;
							if (strMod < 0) calculated = strMod;	// full penalty always applies (except for some ranged weapons)

							if (supplied != calculated) {
								// try to reconcile the differences
								if (strMod > 0) {
									// melee - try the other possible multipliers
									for (int m = 0; m < 4; m++) {
										if (supplied == strMod * m / 2) {
											// found matching bonus, assume it's correct
//											System.err.println(block.getName() + " " + a + ": overriding strength bonus to " + ((float) m / 2) + "x (" + (strMod * m / 2) + ")"
//													+ " was " + ((float) calculated / strMod) + " (" + calculated + ")");
											strMult = m;
										}
									}
								} else if (strMod < 0 && a.ranged && supplied == 0) {
									// probably a projectile weapon that is not subject to str penalties
									strMult = 0;
								}
							}

							calculated = strMod * strMult / 2;
							if (strMod < 0 && strMult > 0) calculated = strMod;	// full penalty always applies (except for some ranged weapons)

							if (supplied != calculated) {
								// still no match
								ok = false;
								b.append(" [damage calculated str bonus = ");
								b.append(calculated + mods);
								b.append("]");
							}
						}
					}
//					if (!ok) {
//						System.out.println();
//						System.out.println(getNameURL(block));
//						for (Attack a : attackRoutine.attacks) {
//							System.out.println(a + ": " + (a.manufactured ? "manufactured" : "natural"));
//						}
//						System.out.print(attackList);
//						System.out.println("Total manufactured = " + manufactured + ", total natural = " + natural);
//						System.out.println();
//					}
				}
				if (!ok) {
					System.out.println();
					System.out.println(getNameURL(block) + ": ");
					int sizeMod = block.getSize().getSizeModifier();
					int dexMod = AbilityScore.getModifier(block.getAbilityScore(AbilityScore.Type.DEXTERITY));
					int strMod = AbilityScore.getModifier(str);
					if (str == -1) strMod = dexMod;		// no strength so use dex
					System.out.println("BAB = " + block.getBAB() + ", Str = " + strMod + ", Dex = " + dexMod + ", Size = " + sizeMod);
					System.out.println("Feats = " + block.get(Field.FEATS));
					System.out.println("Attacks = " + b);
					ok = true;
				}

			} catch (Exception e) {
				System.err.println(getNameURL(block) + ": ");
				e.printStackTrace();
			}
		}

//		List<String> sorted = new ArrayList<>(naturalAttacks);
//		Collections.sort(sorted);
//		System.out.println("--- Natural Attacks ---");
//		for (String n : sorted) {
//			System.out.println(n);
//		}
//		sorted = new ArrayList<>(mfgAttacks);
//		Collections.sort(sorted);
//		System.out.println("--- Manufactured Attacks ---");
//		for (String n : sorted) {
//			System.out.println(n);
//		}
//		System.out.println();
	}

	static void printUniqueSubtypes(List<StatisticsBlock> blocks) {
		// list distinct subtypes:
		HashSet<String> subtypes = new HashSet<>();
		for (StatisticsBlock block : blocks) {
			List<String> subs = block.getSubtypes();
			//System.out.println(block.getName()+": "+block.get(Property.SIZE_TYPE));
			for (String s : subs) {
				subtypes.add(s);
				//System.out.println("'"+s+"'");
				if (s.equals("Guardinal") || s.equals("Eladrin") || s.equals("Tanar'ri") || s.equals("Baatezu")) {
					//System.out.println(block.getName()+" ("+block.get(StatisticsBlock.Property.URL)+"): "+block.get(StatisticsBlock.Property.SIZE_TYPE));
					//System.out.println("SA: "+block.get(StatisticsBlock.Property.SPECIAL_ATTACKS));
					//System.out.println("SQ: "+block.get(StatisticsBlock.Property.SPECIAL_QUALITIES));
					//System.out.println();
				}
			}
		}
		List<String> sorted = new ArrayList<>(subtypes);
		Collections.sort(sorted);
		for (String value : sorted) {
			System.out.println("'" + value + "'");
		}
	}

	static void checkAllAttacks(List<StatisticsBlock> blocks, boolean full) {
		// check that attacks are parsed correctly:
		for (StatisticsBlock block : blocks) {
			try {
				String value = block.get(full ? Field.FULL_ATTACK : Field.ATTACK);

				StringBuilder b = new StringBuilder();
				List<AttackRoutine> attackRoutines = block.getAttacks(full);
				for (int i = 0; i < attackRoutines.size(); i++) {
					if (i > 0) b.append("; or ");
					b.append(attackRoutines.get(i));
				}

				String cleanValue = value.replace(";", "").replace(",", "").replace("*", "").replace("–", "-").replaceAll("\\s+", " ");
				String cleanBuilt = b.toString().replace(";", "").replace("–", "-").replace(",", "");
				if (cleanBuilt.length() == 0) cleanBuilt = "—";
				if (!cleanValue.equals(cleanBuilt.toString())) {
					System.out.println(getNameURL(block) + ": ");
					System.out.println("HTML : " + value);
					System.out.println("clean: " + cleanValue);
//					System.out.println("built: " + b.toString());
					System.out.println("built: " + cleanBuilt);
					System.out.println();
				}
			} catch (Exception e) {
				System.err.println(getNameURL(block) + ": ");
				e.printStackTrace();
			}
		}
	}



// test parse all blocks
	public static void testParse(List<StatisticsBlock> blocks) {
		for (StatisticsBlock block : blocks) {
			try {
				Creature m = StatsBlockCreatureView.createMonster(block);
				int[] acs = block.getACs();
				if (acs[0] != m.getAC()) {
					System.out.println(getNameURL(block) + "\n calculated ac (" + m.getAC() + ") does not match supplied ac (" + acs[0] + ")");
				}
				if (acs[1] != m.getTouchAC()) {
					System.out.println(getNameURL(block) + "\n calculated touch ac (" + m.getTouchAC() + ") does not match supplied touch ac (" + acs[1] + ")");
				}
				if (acs[2] != m.getFlatFootedAC()) {
					if (!block.get(StatisticsBlock.Field.SPECIAL_QUALITIES).contains("uncanny dodge") || acs[2] != m.getAC()) {
						System.out.println(getNameURL(block) + "\n calculated flat-footed ac (" + m.getFlatFootedAC() + ") does not match supplied flat-footed ac (" + acs[2] + ")");
					}
				} else {
					if (block.get(StatisticsBlock.Field.SPECIAL_QUALITIES).contains("uncanny dodge") && m.getAC() != m.getFlatFootedAC()) {
						System.out.println(getNameURL(block) + "\n calculated flat-footed ac (" + m.getFlatFootedAC() + ") does not match supplied ac with uncanny dodge (" + m.getAC() + ")");
					}
				}
//				System.out.println(m.getName() + " ok");
			} catch (Exception e) {
				System.out.println(getNameURL(block));
//				System.out.println(e.getClass() + ": " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
	}

// validate AC
	public static void getACDetails(List<StatisticsBlock> blocks) {
		Map<String, StatisticsBlock> uniqueComponents = new HashMap<>();
		for (StatisticsBlock block : blocks) {
			String ac = block.get(Field.AC);
			if (ac == null) {
				System.out.println("WARN: " + getNameURL(block) + " has no AC");
				continue;
			}

			int i = ac.indexOf(", touch ");
			if (i == -1) {
				System.out.println("WARN: " + getNameURL(block) + " couldn't locate ', touch '");
				continue;
			}
			int j = ac.indexOf(", flat-footed ");
			if (j == -1) {
				System.out.println("WARN: " + getNameURL(block) + " couldn't locate ', flat-footed '");
				continue;
			}

			String fullACSection = ac.substring(0, i);
			String[] fullACs = fullACSection.split("\\s+or\\s+");
			for (String fullAC : fullACs) {
				if (fullAC.indexOf(" (") > -1) {
					try {
						String componentStr = fullAC.substring(fullAC.indexOf(" (") + 2, fullAC.lastIndexOf(')'));
						fullAC = fullAC.substring(0, fullAC.indexOf(" ("));
						String[] components = componentStr.split("\\s*,\\s*");
						for (String component : components) {
//							String value = component.substring(0, component.indexOf(' '));
							String type = component.substring(component.indexOf(' ') + 1);
							if (type.indexOf(" (") >= 0) {
//								String desc = type.substring(type.indexOf(" (") + 2, type.lastIndexOf(')'));
								type = type.substring(0, type.indexOf(" ("));
							}
							uniqueComponents.put(type, block);
						}
					} catch (Exception e) {
						System.err.println(getNameURL(block) + ": " + ac);
						e.printStackTrace();
					}
				} else {
					System.out.println("WARN: " + getNameURL(block) + " couldn't components");
				}
			}
//			String touchAC = ac.substring(i + 8, j);
//			String ffAC = ac.substring(j + 14);
		}
		List<String> sorted = new ArrayList<>(uniqueComponents.keySet());
		Collections.sort(sorted);
		for (String comp : sorted) {
			System.out.println(comp + ": " + getNameURL(uniqueComponents.get(comp)));
		}
	}

	public static String getNameURL(StatisticsBlock block) {
		String s = block.getName();
		try {
			s += " (" + block.getURL() + ")";
		} catch (MalformedURLException e1) {
			s += " (<malformed URL>)";
		}
		return s;
	}

// validate saves and get unique values:
	public static void checkSaves(List<StatisticsBlock> blocks) {
		Map<SavingThrow.Type, Set<String>> saves = new HashMap<>();
		for (SavingThrow.Type save : SavingThrow.Type.values()) {
			saves.put(save, new HashSet<String>());
		}
		List<StatisticsBlock> exceptions = new ArrayList<>();

		for (StatisticsBlock block : blocks) {
			String value = block.get(StatisticsBlock.Field.SAVES);
			try {
				for (SavingThrow.Type save : SavingThrow.Type.values()) {
					String[] savesTxt = value.split("\\s*,\\s*");
					String s = savesTxt[save.ordinal()].substring(savesTxt[save.ordinal()].indexOf(' ') + 1);
					saves.get(save).add(s);
					if (s.startsWith("+")) s = s.substring(1);
					if (s.contains(" ")) s = s.substring(0, s.indexOf(' '));
					if (s.endsWith("*")) s = s.substring(0, s.length() - 1);
					s = s.replace('–', '-');
					if (s.equals("—")) continue;
					try {
						Integer.parseInt(s);
					} catch (NumberFormatException ex) {
						System.out.println("Failed to parse '" + s + "'");
						exceptions.add(block);
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("Exception parsing " + getNameURL(block));
				e.printStackTrace();
			}
		}
		for (SavingThrow.Type save : SavingThrow.Type.values()) {
			List<String> values = new ArrayList<>(saves.get(save));
			Collections.sort(values);
			System.out.println("Values for " + save);
			for (String value : values) {
				System.out.println("'" + value + "'");
			}
		}
		for (StatisticsBlock block : exceptions) {
			System.out.println(getNameURL(block));
			System.out.println(": " + block.get(Field.SAVES));
		}
	}

	public static void checkBABGrapple(List<StatisticsBlock> blocks) {
		for (StatisticsBlock block : blocks) {
			if (block.get(Field.SIZE_TYPE) == null || block.get(Field.SIZE_TYPE) == "") {
				System.out.println("No size/type: "+block.getName());
				continue;
			}

			try {
				MonsterType type = block.getType();
				if (type == null) {
					System.out.println("unknown type in " + getNameURL(block) + ": " + type);
					continue;
				}

				List<String> subtypes = block.getSubtypes();

				SizeCategory size = block.getSize();
				if (size == null) {
					System.out.println("unknown size in " + getNameURL(block) + ": " + block.get(StatisticsBlock.Field.SIZE_TYPE));
					continue;
				}

				int bab = 0;
				StringBuilder babStr = new StringBuilder();

				Map<CharacterClass, Integer> classes = block.getClassLevels();
				Set<HitDice> hitdice = block.getHitDice().getComponents();

				// figure out the base monster hitdice by removing all the class level hitdice
				// also add the class derived bab
				for (CharacterClass cls : classes.keySet()) {
					int level = classes.get(cls);

					bab += cls.getBAB(level);
					if (babStr.length() > 0) babStr.append(" + ");
					babStr.append(cls.getBAB(level)).append(" (").append(cls).append(" ").append(level).append(")");

					HitDice classHD = null;
					for (HitDice d : hitdice) {
						if (d.getNumber(0) == level && d.getType(0) == cls.getHitDiceType()) {
							classHD = d;
							break;
						}
					}
					if (classHD != null) {
						hitdice.remove(classHD);
					} else {
						System.out.println("WARN: could not for hitdice for level-" + level + " " + cls + ": " + getNameURL(block));
						System.out.println("  expected " + level + "d" + cls.getHitDiceType() + ", only found " + block.getHitDice());
					}
				}
				// hitdice should be either empty or just have a single component representing the base hitdice
				if (hitdice.size() > 1) {
					// probably this is a templated creature
					// TODO
					System.out.println("WARN: extra hitdice found in " + getNameURL(block));
					for (HitDice d : hitdice) {
						System.out.println("  " + d);
					}
				} else if (hitdice.size() == 1) {
					HitDice hd = hitdice.iterator().next();

					MonsterType babType = type;
					// check that the type is correct
					if (hd.getType(0) == type.getHitDiceType()) {
						// type is correct
					} else if (block.getAugmentedType() != null && hd.getType(0) == block.getAugmentedType().getHitDiceType()) {
						// augmented creature using original hitdice type. use the original BAB progression
						babType = block.getAugmentedType();
					} else {
						System.out.println("WARN: creature has incorrect hitdice for type: " + getNameURL(block));
						System.out.println("  Should be d" + type.getHitDiceType() + " but found d" + hd.getType(0));
					}

					int num = hd.getNumber(0);
					if (num < 1) num = 1;
					bab += babType.getBAB(num);
					if (babStr.length() > 0) babStr.append(" + ");
					babStr.append(babType.getBAB(num)).append(" (").append(babType).append(" ").append(hd.getNumber(0)).append(")");
				}

				// TODO augmented creatures could be calculated using the wrong type - really need to check both and use whichever matches
				// TODO 3rd posibility for augmented creatures is that they use new type's hitdice but original type's BAB (e.g. deathknight)

				int strMod = 0;
				int str = block.getAbilityScore(AbilityScore.Type.STRENGTH);
				if (str > -1) strMod = AbilityScore.getModifier(str);

				int attack = bab + size.getSizeModifier() + strMod;
				int grapple = bab + size.getGrappleModifier() + strMod;
				if (block.get(Field.FEATS) != null && block.get(Field.FEATS).contains("Improved Grapple"))
					grapple += 4;

				String existing = block.get(Field.BASE_ATTACK_GRAPPLE).replace("–", "-");
				Pattern p = Pattern.compile("[+-]\\d+\\/[+-]\\d+(\\s+\\((.*)\\))?((,.*))?");
				Matcher m = p.matcher(existing);
				// no match if grapple value is '-'
				// group 1 is the modifiers on normal grapple including the parentheses
				// group 2 is the modifiers on normal grapple excluding the parentheses
				// group 3 is any alternate grapple
				if (m.matches() && m.group(2) != null) {
					for (String bonus : m.group(2).split(",\\s*")) {
						if (bonus.contains("Dex")) {
							int dexMod = AbilityScore.getModifier(block.getAbilityScore(AbilityScore.Type.DEXTERITY));

							// if a dex modifier is specified but str mod is not then use the higher of the two
							// if both are specified then use both
							if (m.group(2).contains(" Str")) {
								grapple += dexMod;
							} else if (strMod < dexMod) {
								grapple -= strMod;
								grapple += dexMod;
							}
						} else {
							String value = bonus.substring(0, bonus.indexOf(' '));
							if (value.startsWith("+")) value = value.substring(1);
							grapple += Integer.parseInt(value);
						}
					}
				}

				// rebuilt BAB/Grapple property:
				StringBuilder property = new StringBuilder();
				if (bab > -1) property.append("+");
				property.append(bab + "/");
				if (subtypes.contains("Incorporeal") || subtypes.contains("Swarm")) {
					property.append("—");
				} else {
					if (grapple>-1) property.append("+");
					property.append(grapple);
				}
				if (m.matches() && m.group(2) != null) property.append(" (").append(m.group(2)).append(")");
				if (m.matches() && m.group(3) != null) property.append(m.group(3));

				if (existing == null || !existing.equals(property.toString())) {
					System.out.println(block.get(Field.URL) + " - " + block.getName() + ": BAB = " + bab + ", Grapple = " + grapple + ", Attack = " + attack);
					System.out.println(block.get(Field.SIZE_TYPE) + ": " + block.getType());
					System.out.println("Attack: " + block.get(Field.ATTACK));
					String feats = block.get(Field.FEATS);
					if (feats != null && feats.toLowerCase().contains("weapon")) {
						System.out.println("Feats: "+feats);
					}
					String spAtt = block.get(Field.SPECIAL_ATTACKS);
					if (spAtt != null && spAtt.toLowerCase().contains("grab")) {
						System.out.println("Special Attacks: "+spAtt);
					}
					if (existing != null && existing.length() > 0) System.out.println("Existing: "+existing);
					System.out.println("Calculated: " + property);
					System.out.println("BAB: " + bab + " = " + babStr);
					System.out.println("Grapple: " + grapple + " = " + bab + " (BAB) + " + size.getGrappleModifier() + " (size) + " + strMod + " str");
					System.out.println();
				}

			} catch (Exception e) {
				System.err.println("Exception processing " + block.getName() + ": ");
				e.printStackTrace();
			}
		}
	}

// get unique values for specified property
	public static Set<String> getUniqueValues(List<StatisticsBlock> blocks, StatisticsBlock.Field property) {
		HashSet<String> values = new HashSet<>();
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
			String sizeAndType = block.get(StatisticsBlock.Field.SIZE_TYPE);
			if (sizeAndType != null) {
				size = sizeAndType.split(" ")[0];
				type = sizeAndType.substring(size.length()+1);
			}
			String cr = block.get(StatisticsBlock.Field.CR);
			if (cr.equals("½")) cr = "1/2";
			if (cr.equals("¼")) cr = "1/4";

			System.out.println("<Monster name=\""+block.getName()
					+ "\" url=\"" + block.get(StatisticsBlock.Field.URL)
					+"\" size=\""+size
					+"\" type=\""+type
					+ "\" environment=\"" + block.get(StatisticsBlock.Field.ENVIRONMENT)
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



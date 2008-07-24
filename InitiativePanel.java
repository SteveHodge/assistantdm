import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLayeredPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import party.Character;
import party.Party;


@SuppressWarnings("serial")
public class InitiativePanel extends JLayeredPane implements ChangeListener, MouseMotionListener, MouseListener {
	private static final double DRAG_START_DISTANCE = 3;
	List<InitiativeEntry> list = new ArrayList<InitiativeEntry>();
	String lastOutput = "";
	Party party;
	InitiativeEntry blankInit = null;

	int nextTop = 0;		// y position of next entry
	int maxWidth = 0;		// width of widest child 

	private InitiativeEntry dragEntry;
	private Dimension dragEntrySize;	// size of drag entry
	private int yoffset;	// offset of mouse from top of dragged component 
	private Point origin;	// mouse position when button is pressed - used to determine if we should start dragging
	private boolean dragging = false;
	private int gapTop;		// position of the top of the gap in the list. the height of the gap will be dragEntrySize.height

	public InitiativePanel(Party p) {
		setLayout(null);
		addMouseListener(this);
		addMouseMotionListener(this);

		party = p;
		if (party != null) {
			for (Character c : party) {
				addEntry(new InitiativeEntry(c));
			}
		}
		blankInit = new InitiativeEntry();
		addEntry(blankInit);
		reorderList();
	}

	protected void addEntry(InitiativeEntry e) {
		list.add(e);
		e.addChangeListener(this);
		add(e);
	}

	public void stateChanged(ChangeEvent e) {
		if (dragging) return;	// don't process changes while dragging
		InitiativeEntry changed = (InitiativeEntry)e.getSource();
		boolean reorder = false;

		// check if we need to add a new entry:
		if (changed == blankInit && !changed.isBlank()) {
//			System.out.println("Adding new entry");
			blankInit = new InitiativeEntry();
			addEntry(blankInit);
			reorder = true;
		}

		// check if reorder is necessary
		int index = list.indexOf(e.getSource());
		if (!reorder && index >0) {
			InitiativeEntry prev = list.get(index-1);
			if (compareInitiatives(changed,prev) < 0) {
				//System.out.println("Changed entry is > prev");
				reorder = true;
			}
		}
		if (!reorder && index < list.size()-1) {
			InitiativeEntry next = list.get(index+1);
			if (compareInitiatives(changed,next) > 0) {
				//System.out.println("Changed entry is < next");
				reorder = true;
			}
		}
		if (reorder) reorderList();

		writeHTML();
	}

	// compares InitiativeEntrys for initiative order, i.e. highest total first, ties
	// broken by modifier and then tiebreak
	public static int compareInitiatives(InitiativeEntry ie1, InitiativeEntry ie2) {
		if (ie2.isBlank()) {
			if (ie1.isBlank()) return 0;
			else return -1;
		}
		if (ie1.isBlank()) return 1;
		return compareInitiatives(ie1.getTotal(), ie1.getModifier(), ie1.getTieBreak(),
				ie2.getTotal(), ie2.getModifier(), ie2.getTieBreak());
	}

	// compares InitiativeEntrys for initiative order, i.e. highest total first, ties
	// broken by modifier and then tiebreak
	protected static int compareInitiatives(int total1, int mod1, int tie1, int total2, int mod2, int tie2) {
		if (total1 != total2) return total2 - total1;
		// totals the same, next check is modifiers
		if (mod1 != mod2) return mod2 - mod1;
		// totals and modifiers are the same, next check is tie break
		return tie2 - tie1;
	}

	private void reorderList() {
		//		System.out.println("Reordering list");
		Collections.sort(list,new Comparator<InitiativeEntry>() {
			public int compare(InitiativeEntry ie1, InitiativeEntry ie2) {
				return compareInitiatives(ie1,ie2);
			}
		});

		nextTop = getInsets().top;
		maxWidth = 0;
		for (InitiativeEntry e : list) {
			Dimension size = e.getPreferredSize();
			if (size.width > maxWidth) maxWidth = size.width;
		}
		for (InitiativeEntry e : list) {
			Dimension size = e.getPreferredSize();
			e.setBounds(getInsets().left, nextTop, maxWidth, size.height);
			nextTop += size.height;
		}
		setPreferredSize(new Dimension(maxWidth,nextTop));
		revalidate();
	}

	private void writeHTML() {
		String output = "round=1\n";
		int i = 0;
		for (InitiativeEntry e : list) {
			if (!e.isDMOnly()) {
				i++;
				//System.out.println(e.getName() + " - " +e.getTotal());
				output += "fixedname"+i+"="+e.getName()+"\n";
				output += "init"+i+"="+e.getTotal()+"\n";
			}
		}
		output = "lastindex="+i+"\n"+output;
		if (!output.equals(lastOutput)) {
			//System.out.println(output);
			lastOutput = output;
			try {
				FileWriter file = new FileWriter("M:\\webcam\\initiative.txt");
				file.write(output);
				file.close();
			} catch (IOException e1) {
				System.out.println("Exception writing initiative file: "+e1);
			}
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (dragEntry != null) {
			int y = e.getY() - yoffset;
			if (!dragging && e.getPoint().distance(origin) > DRAG_START_DISTANCE) {
				// We've moved the minimum drag distance, set up drag
				dragging = true;
				setLayer(dragEntry,DRAG_LAYER);
			}
			if (dragging) {
				dragEntry.setBounds(0, y, dragEntrySize.width, dragEntrySize.height);
				// 1. find entry under mouse
				// TODO: scanning the entries like this is pretty inefficient - could track the indexes instead
				Rectangle bounds = null;
				for (InitiativeEntry ie : list) {
					if (ie == dragEntry) continue;
					bounds = ie.getBounds(bounds);
					if (bounds.contains(e.getPoint())) {
						if (e.getY() <= bounds.getCenterY() && e.getY() < gapTop) {
							// 2a. if we are in the top half of the entry and the gap is below
							// 3a.   then gapTop = entry.y and entry.y += dragEntrySize.height
							gapTop = bounds.y;
							bounds.y += dragEntrySize.height;
							ie.setBounds(bounds);
						} else if (e.getY() > bounds.getCenterY() && e.getY() > gapTop) {
							// 2b. else if we are in the bottom half of the entry and the gap is above
							// 3b.   then entryTop = gapTop and gapTop += entry.height
							bounds.y = gapTop;
							gapTop += bounds.height;
							ie.setBounds(bounds);
						}
					}
				}
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		Component comp = getComponentAt(e.getPoint());
		if (comp instanceof InitiativeEntry) {
			dragEntry = (InitiativeEntry)comp;
			dragEntrySize = dragEntry.getSize(dragEntrySize);
			yoffset = e.getY() - dragEntry.getY();
			origin = e.getPoint();
			gapTop = comp.getY();
		    e.consume();
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (dragging) {
			setLayer(dragEntry,DEFAULT_LAYER);
			dragEntry.setBounds(0, gapTop, dragEntrySize.width, dragEntrySize.height);

			// fix up roll so that it remains in this position
			// first sort the list by y-position
			Collections.sort(list,new Comparator<InitiativeEntry>() {
				public int compare(InitiativeEntry o1, InitiativeEntry o2) {
					return o1.getY() - o2.getY();
				}
			});

			// next check the new prev and next entries against the dragEntry
			int index = list.indexOf(dragEntry);
			System.out.println("Index of dragged entry = "+index);
			if (index == 0 && list.size() > 1) {
				// case: entry is now first
				InitiativeEntry next = list.get(index+1);
				if (compareInitiatives(dragEntry, next) > 0) {
					// entry needs correction...
					dragEntry.setRoll(next.getTotal()+1 - dragEntry.getModifier());
				}
			} else if (index >= list.size()-2 && list.size() > 2) {
				// case: entry is now last or second to last (before only the blank entry)
				InitiativeEntry prev = list.get(list.size()-3);
				if (compareInitiatives(dragEntry, prev) < 0) {
					// entry needs correction...
					dragEntry.setRoll(prev.getTotal()-1 - dragEntry.getModifier());
					reorderList();	// need this to make sure the blank entry stays at the bottom
				}
			} else if (index > 0 && index < list.size()-2) {
				// case: entry is between two other legitimate entries
				InitiativeEntry next = list.get(index+1);
				InitiativeEntry prev = list.get(index-1);
				if (prev.getTotal() - next.getTotal() >= 2) {
					// there's a gap: if the current total doesn't fit in the gap
					// then put the dragged entry in the middle of the gap
					if (compareInitiatives(dragEntry, next) >= 0
							|| compareInitiatives(prev, dragEntry) >= 0) {
						int target = (prev.getTotal() + next.getTotal()) / 2;
						dragEntry.setRoll(target - dragEntry.getModifier());
					}
				} else if (prev.getTotal() - next.getTotal() == 1) {
					// There is a gap of one. The situation is this:
					// prev, Total = X, Modifier/Tiebreak = A
					// dragEntry, Total = X or X-1, Modifier/Tiebreak = C
					// next, Total = X-1, Modifier/Tiebreak = B
					// If we set dragEntry total to X then the following two cases can occur:
					// 1. A > C - this is ok, we're done
					// 2. A < C - this will require moving prev up
					// If we set dragEntry total to X-1 then the following two cases can occur:
					// 3. C > B - this is ok, we're done
					// 4. C < B - this will require moving next down
					// So we test 1 and 3 and take the first to succeed
					// If neither succeeds then B > C > A. Test 2 and 4 and use the option that
					// results in the least amount of movement. In practice it's easiest to set
					// dragEntry total to X-1 and then test moving next down or prev and dragEntry up.
					// TODO: write the code
					if (compareInitiatives(prev.getTotal(),prev.getModifier(),prev.getTieBreak(),
							prev.getTotal(),dragEntry.getModifier(),dragEntry.getTieBreak()) < 0) {
						// with dragEntry.total = X, A > C - do it for real and we're done
						dragEntry.setRoll(prev.getTotal() - dragEntry.getModifier());
						System.out.println("Setting dragged entry total to "+prev.getTotal()+" works");
					} else if (compareInitiatives(next.getTotal(),dragEntry.getModifier(),dragEntry.getTieBreak(),
							next.getTotal(),next.getModifier(),next.getTieBreak()) < 0) {
						// with dragEntry total = X-1, C > B - do it for real and we're done
						dragEntry.setRoll(next.getTotal() - dragEntry.getModifier());
						System.out.println("Setting dragged entry total to "+next.getTotal()+" works");
					} else {
						// neither X or X-1 just work. So we have too try each option in each direction
						System.out.println("Neither X or X-1 just work");
						// We'll arbitrarily set dragEntry's total to X-1
						dragEntry.setRoll(next.getTotal() - dragEntry.getModifier());
						System.out.println("Setting dragged entry total to "+next.getTotal()+" and testing moves");
						int bDown = testMove(index+1,-1);
						int acUp = testMove(index,1);
						acUp--; // above counts moving dragEntry which shouldn't really count
						System.out.println("AC up = "+acUp+", B Down = "+bDown);
						if (bDown <= acUp) {
							System.out.println("Moving B down");
							moveEntries(index+1,-1);
						} else {
							System.out.println("Moving A & C up");
							moveEntries(index,1);
						}
					}

				} else if (prev.getTotal() == next.getTotal()) {
					// There is no gap at all. The situation is this:
					// prev, Total = X, Modifier/Tiebreak = A
					// dragEntry, Total = X, Modifier/Tiebreak = C
					// next, Total = X, Modifier/Tiebreak = B
					// Note that A > B is always true. There are three cases:
					// 1. A > C > B - this is ok, we're done
					// 2. C > A > B - move prev up or dragEntry and next down
					// 3. A > B > C - move next down or dragEntry and prev up
					// If 2 or 3 apply then we need to test both options for moving and choose
					// the one that results in the least amount of movement.
					// TODO: There is one special case: if all the modifiers are equal then we could manipulate the tiebreak scores.
					dragEntry.setRoll(prev.getTotal() - dragEntry.getModifier());
					int a_c = compareInitiatives(prev, dragEntry);
					int c_b = compareInitiatives(dragEntry, next);
					if (a_c <= 0 && c_b <= 0) {
						// A > C > B
						System.out.println("A>C>B - done");
					} else if (a_c <= 0 && c_b > 0) {
						// A > B > C
						System.out.println("A>B>C - move B down or A & C up");
						int bDown = testMove(index+1,-1);
						int acUp = testMove(index,1);
						acUp--; // above counts moving dragEntry which shouldn't really count
						System.out.println("AC up = "+acUp+", B Down = "+bDown);
						if (bDown <= acUp) {
							System.out.println("Moving B down");
							moveEntries(index+1,-1);
						} else {
							System.out.println("Moving A & C up");
							moveEntries(index,1);
						}
					} else if (a_c > 0 && c_b <= 0) {
						// C > A > B
						System.out.println("C>A>B - move A up or B & C down");
						int bcDown = testMove(index,-1);
						bcDown--; // above counts moving dragEntry which shouldn't really count
						int aUp = testMove(index-1,1);
						System.out.println("A up = "+aUp+", B&C Down = "+bcDown);
						if (bcDown <= aUp) {
							System.out.println("Moving B & C down");
							moveEntries(index,-1);
						} else {
							System.out.println("Moving A up");
							moveEntries(index-1,1);
						}
					} else {
						System.err.println("Impossible: a_c = "+a_c+", c_b = "+c_b);
					}
				}

/*				} else {
					// there may not be enough room. check what happens if we set the target to the same
					// total as the next and previous
					boolean prevNudgeNext = false;
					boolean prevNudgePrev = false;
					boolean nextNudgeNext = false;
					boolean nextNudgePrev = false;
					System.out.println("dragEntry,next = " + compareInitiatives(dragEntry, next));
					System.out.println("dragEntry,prev = " + compareInitiatives(dragEntry, prev));
					// 1. try setting the total to the total of the prev
					dragEntry.setRoll(prev.getTotal() - dragEntry.getModifier());
					System.out.println("Changed dragEntry total to "+dragEntry.getTotal());
					if (compareInitiatives(prev, dragEntry) < 0) {
						System.out.println("prev and dragEntry are in correct order");
					} else {
						// prev <= dragEntry - would require nudge of prev up
						prevNudgePrev = true;
					}
					if (compareInitiatives(dragEntry, next) < 0) {
						System.out.println("dragEntry and next are in correct order");
					} else {
						// prev > dragEntry <= next - would require nudge of next down
						prevNudgeNext = true;
					}
					if (!prevNudgeNext && !prevNudgePrev) {
						System.out.println("Order is correct - leave");
					} else {
						// 2. try setting the total to the total of the next
						dragEntry.setRoll(next.getTotal() - dragEntry.getModifier());
						System.out.println("Changed dragEntry total to "+dragEntry.getTotal());
						if (compareInitiatives(dragEntry, next) < 0) {
							System.out.println("dragEntry and next are in correct order");
						} else {
							// dragEntry <= next - would require nudge of next down
							nextNudgeNext = true;
						}
						if (compareInitiatives(prev, dragEntry) < 0) {
							System.out.println("prev and dragEntry are in correct order");
						} else {
							// prev <= dragEntry > next - would require nudge of prev up
							nextNudgePrev = true;
						}
						if (!nextNudgeNext && !nextNudgePrev) {
							System.out.println("Order is correct - leave");
						} else {
							// TODO: 3. if all three have the same modifiers we could manipulate tiebreaks
							// some sort of manipulation is required:
							// note because prev >= next it is not possible for prev < dragEntry < next
							// so (prevNudgeNext && prevNudgePrev) is always false
							// and (nextNudgeNext && nextNudgePrev) is always false
							if ((prevNudgeNext && prevNudgePrev) || (nextNudgeNext && nextNudgePrev)) {
								System.err.println("Assertion failure:");
								System.err.println("Setting total to previous:");
								if (prevNudgeNext) System.err.println(" requires nudge of next");
								if (prevNudgePrev) System.err.println(" requires nudge of prev");
								System.err.println("Setting total to next:");
								if (nextNudgeNext) System.err.println(" requires nudge of next");
								if (nextNudgePrev) System.err.println(" requires nudge of prev");
							}
							// TODO choose best option and perform necessary nudges
							// if both options force us to nudge prev then do that otherwise nudge next
							if (prevNudgePrev && nextNudgePrev) {
								System.out.println("Nudging previous entry(s) up");
								// nudging prev up. first set dragEntry total to prev's current total
								dragEntry.setRoll(prev.getTotal() - dragEntry.getModifier());
								// nudge the previous entries up..
								int i = index-1;
								while (i >= 0) {
									InitiativeEntry a = list.get(i);
									a.adjustRoll(1);
									System.out.println("Index: " + i + ", adjusted to "+a);
									if (i == 0) break;
									InitiativeEntry b = list.get(i-1);
									System.out.println("Quit if less than: "+b);
									System.out.println(" = " + compareInitiatives(b,a));
									if (compareInitiatives(b,a) < 0) break;	// done
									i--;
								}
							} else {
								System.out.println("Nudging next entry(s) down");
								// if only one of nextNudgeNext and prevNudgeNext is set then we
								// need to choose the total that works for that, if they are both
								// set then we choose to use prev's total
								if (nextNudgeNext && !prevNudgeNext) {
									System.out.println("Setting to next");
									dragEntry.setRoll(next.getTotal() - dragEntry.getModifier());
								} else {
									dragEntry.setRoll(prev.getTotal() - dragEntry.getModifier());
								}
								// nudge next
								int i = index+1;
								while (i < list.size()-2) {
									InitiativeEntry a = list.get(i);
									a.adjustRoll(-1);
									System.out.println("Index: " + i + ", adjusted to "+a);
									InitiativeEntry b = list.get(i+1);
									System.out.println("Quit if greater than: "+b);
									System.out.println(" = " + compareInitiatives(a,b));
									if (compareInitiatives(a,b) < 0) break;	// done
									i++;
								}
							}
						}
					}
					
 					// if necessary, nudge the next entries down, ignoring the blank
				}
*/
			}

			reorderList();
			dragging = false;
			dragEntry = null;
		}
	}

	// returns the number of entries moved
	// start is the index of the entry to start from
	// dir is the direction: -1 is down (total will be lower), +1 is up (total will be higher)
	protected int testMove(int start, int dir) {
//		System.out.println("testMove(start="+start+", ignore="+ignore+", dir="+dir+")");
//		for (int i=0; i<list.size(); i++) {
//			System.out.println("  "+i+": "+list.get(i));
//		}
		int i = start;
		int count = 1;
		while (i > 0 && i < list.size()-1) {
			InitiativeEntry current = list.get(i);
			InitiativeEntry next = list.get(i-dir);
			if (!current.isBlank() && !next.isBlank()) {
				// two real entries
				int comp = compareInitiatives(current.getTotal()+dir,current.getModifier(),current.getTieBreak(),
						next.getTotal(),next.getModifier(),next.getTieBreak());
				System.out.println("("+(current.getTotal()+dir)+") "+current+"\nvs. ("+next.getTotal()+") "+next+"\n = "+comp+" ("+(comp*dir)+")");
				if (dir * comp < 0) {
					count++;
					//System.out.println("No room, need to move");
				} else {
					//System.out.println("There is room, finished");
					break;
				}
			}
			i -= dir;
		}
		//System.out.println("Count to move = "+count);
		return count;
	}

	// start is the index of the entry to start from
	// dir is the direction: -1 is down (total will be lower), +1 is up (total will be higher)
	protected void moveEntries(int start, int dir) {
		int i = start;
		while (i >= 0 && i <= list.size()-1) {
			InitiativeEntry current = list.get(i);
			if (!current.isBlank()) {
				current.adjustRoll(dir);
			}
			if (i == 0 || i == list.size()-1) break;
			// only proceed if there is a next element
			InitiativeEntry next = list.get(i-dir);
			if (!next.isBlank()) {
				// two real entries
				int comp = compareInitiatives(current,next);
				//System.out.println("("+current.getTotal()+") "+current+"\nvs. ("+next.getTotal()+") "+next+"\n = "+comp+" ("+(comp*dir)+")");
				if (dir * comp >= 0) {
					//System.out.println("There is room, finished");
					break;
				}
			}
			i -= dir;
		}
	}
}

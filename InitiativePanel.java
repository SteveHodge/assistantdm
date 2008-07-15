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
		if (changed == blankInit && !changed.blank) {
//			System.out.println("Adding new entry");
			blankInit = new InitiativeEntry();
			addEntry(blankInit);
			reorder = true;
		}

		// check if reorder is necessary
		int index = list.indexOf(e.getSource());
		if (!reorder && index >0) {
			InitiativeEntry prev = list.get(index-1);
			if (initComp.compare(changed,prev) < 0) {
				//System.out.println("Changed entry is > prev");
				reorder = true;
			}
		}
		if (!reorder && index < list.size()-1) {
			InitiativeEntry next = list.get(index+1);
			if (initComp.compare(changed,next) > 0) {
				//System.out.println("Changed entry is < next");
				reorder = true;
			}
		}
		if (reorder) reorderList();

		writeHTML();
	}

	// compares InitiativeEntrys for initiative order, i.e. highest total first, ties
	// broken by modifier and then tiebreak
	static Comparator<InitiativeEntry> initComp = new Comparator<InitiativeEntry>() {
		public int compare(InitiativeEntry ie1, InitiativeEntry ie2) {
			if (ie2.blank) {
				if (ie1.blank) return 0;
				else return -1;
			}
			if (ie1.blank) return 1;
			if (ie1.getTotal() != ie2.getTotal()) return ie2.getTotal() - ie1.getTotal();
			// totals the same, next check is modifiers
			if (ie1.getModifier() != ie2.getModifier()) return ie2.getModifier() - ie1.getModifier();
			// totals and modifiers are the same, next check is tie break
			int oTie = (Integer)ie1.tiebreakField.getValue();
			int tie = (Integer)ie2.tiebreakField.getValue();
			return tie - oTie;
		}
	};

	private void reorderList() {
		//		System.out.println("Reordering list");
		Collections.sort(list,initComp);

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
				if (initComp.compare(dragEntry, next) > 0) {
					// entry needs correction...
					dragEntry.setRoll(next.getTotal()+1 - dragEntry.getModifier());
				}
			} else if (index >= list.size()-2 && list.size() > 2) {
				// case: entry is now last or second to last (before only the blank entry)
				InitiativeEntry prev = list.get(list.size()-3);
				if (initComp.compare(dragEntry, prev) < 0) {
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
					if (initComp.compare(dragEntry, next) >= 0
							|| initComp.compare(prev, dragEntry) >= 0) {
						int target = (prev.getTotal() + next.getTotal()) / 2;
						dragEntry.setRoll(target - dragEntry.getModifier());
					}
				} else {
					// there may not be enough room. check what happens if we set the target to the same
					// total as the next and previous
					boolean prevNudgeNext = false;
					boolean prevNudgePrev = false;
					boolean nextNudgeNext = false;
					boolean nextNudgePrev = false;
					System.out.println("dragEntry,next = " + initComp.compare(dragEntry, next));
					System.out.println("dragEntry,prev = " + initComp.compare(dragEntry, prev));
					// 1. try setting the total to the total of the prev
					dragEntry.setRoll(prev.getTotal() - dragEntry.getModifier());
					System.out.println("Changed dragEntry total to "+dragEntry.getTotal());
					if (initComp.compare(prev, dragEntry) < 0) {
						System.out.println("prev and dragEntry are in correct order");
					} else {
						// prev <= dragEntry - would require nudge of prev up
						prevNudgePrev = true;
					}
					if (initComp.compare(dragEntry, next) < 0) {
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
						if (initComp.compare(dragEntry, next) < 0) {
							System.out.println("dragEntry and next are in correct order");
						} else {
							// dragEntry <= next - would require nudge of next down
							nextNudgeNext = true;
						}
						if (initComp.compare(prev, dragEntry) < 0) {
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
									System.out.println(" = " + initComp.compare(b,a));
									if (initComp.compare(b,a) < 0) break;	// done
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
									System.out.println(" = " + initComp.compare(a,b));
									if (initComp.compare(a,b) < 0) break;	// done
									i++;
								}
							}
						}
					}
					
 					// if necessary, nudge the next entries down, ignoring the blank
				}
			}

			reorderList();
			dragging = false;
			dragEntry = null;
		}
	}
}

package combat;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.Character;
import party.Party;
import party.PartyListener;
import swing.ReorderableListModel;


public class InitiativeListModel implements ReorderableListModel<CombatEntry>, ActionListener, ChangeListener, PartyListener {
	private Party party;
	@SuppressWarnings("serial")
	private List<CombatEntry> list = new ArrayList<CombatEntry>() {
		@Override
		public void sort(Comparator<? super CombatEntry> arg0) {
			//Thread.dumpStack();
			super.sort(arg0);
		}
	};

	private EventListenerList listenerList = new EventListenerList();

	private CombatEntry blankInit = null;	// there is always a blank entry at the end of the list. if this is edited then we'll add a new blank entry
	private CombatEntry selected = null;	// the selected entry, if any
	private Border selectedBorder = null;	// the original border of the selected entry
	private JPanel detailPanel;

	private boolean noSort = false;		// set to true to block sorting while we reorder entries

	InitiativeListModel(Party p, JPanel detailPanel) {
		party = p;
		p.addPartyListener(this);
		if (party != null) {
			for (Character c : party) {
				addEntry(new CharacterCombatEntry(c));
			}
		}
		this.detailPanel = detailPanel;
		blankInit = new MonsterCombatEntry();
		addEntry(blankInit);
		sort();
	}

	@Override
	public void characterAdded(Character c) {
		addEntry(new CharacterCombatEntry(c));
		sort();
	}

	@Override
	public void characterRemoved(Character c) {
		CombatEntry toRemove = null;
		for (CombatEntry e : list) {
			if (e.getSource() == c) {
				toRemove = e;
				break;
			}
		}
		if (toRemove != null) removeEntry(toRemove);
	}

	@Override
	public int indexOf(Component item) {
		return list.indexOf(item);
	}

	// XXX use of noSort flag to prevent sorts is a bit hackish, but probably the simpilest solution
	@Override
	public void moveTo(Component item, int index) {
		if (!list.remove(item)) throw new NoSuchElementException();
		noSort = true;	// disable sort while we reorganise (because adjustRoll() in moveEntries() will cause a sort otherwise
		CombatEntry dragged = (CombatEntry) item;
		list.add(index,dragged);

		// fix up roll so that it remains in this position
		// first sort the list by y-position
		Collections.sort(list,(o1, o2) -> o1.getY() - o2.getY());

		// Store the required order and details so we can check everything worked
		String reqOrder = "", preDetails = "";
		for (CombatEntry e : list) {
			reqOrder += e.getCreatureName()+",";
			preDetails += e + "\n";
		}

		// next check the new prev and next entries against the dragEntry
		//System.out.println("Index of dragged entry = "+index);
		if (index == 0 && list.size() > 1) {
			// case: entry is now first
			CombatEntry next = list.get(index+1);
			if (CombatEntry.compareInitiatives(dragged, next) > 0) {
				// entry needs correction...
				dragged.setRoll(next.getTotal()+1 - dragged.getModifier());
			}
		} else if (index >= list.size()-2 && list.size() > 2) {
			// case: entry is now last or second to last (before only the blank entry)
			CombatEntry prev = list.get(list.size()-3);
			if (CombatEntry.compareInitiatives(dragged, prev) < 0) {
				// entry needs correction...
				dragged.setRoll(prev.getTotal()-1 - dragged.getModifier());
			}
		} else if (index > 0 && index < list.size()-2) {
			// case: entry is between two other legitimate entries
			CombatEntry next = list.get(index+1);
			CombatEntry prev = list.get(index-1);
			if (prev.getTotal() - next.getTotal() >= 2) {
				// there's a gap: if the current total doesn't fit in the gap
				// then put the dragged entry in the middle of the gap
				if (CombatEntry.compareInitiatives(dragged, next) >= 0
						|| CombatEntry.compareInitiatives(prev, dragged) >= 0) {
					int target = (prev.getTotal() + next.getTotal()) / 2;
					dragged.setRoll(target - dragged.getModifier());
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
				if (CombatEntry.compareInitiatives(prev.getTotal(),prev.getModifier(),prev.getTieBreak(),
						prev.getTotal(),dragged.getModifier(),dragged.getTieBreak()) < 0) {
					// with dragEntry.total = X, A > C - do it for real and we're done
					dragged.setRoll(prev.getTotal() - dragged.getModifier());
					//System.out.println("Setting dragged entry total to "+prev.getTotal()+" works");
				} else if (CombatEntry.compareInitiatives(next.getTotal(),dragged.getModifier(),dragged.getTieBreak(),
						next.getTotal(),next.getModifier(),next.getTieBreak()) < 0) {
					// with dragEntry total = X-1, C > B - do it for real and we're done
					dragged.setRoll(next.getTotal() - dragged.getModifier());
					//System.out.println("Setting dragged entry total to "+next.getTotal()+" works");
				} else {
					// neither X or X-1 just work. So we have too try each option in each direction
					//System.out.println("Neither X or X-1 just work");
					// We'll arbitrarily set dragEntry's total to X-1
					dragged.setRoll(next.getTotal() - dragged.getModifier());
					//System.out.println("Setting dragged entry total to "+next.getTotal()+" and testing moves");
					int bDown = testMove(index+1,-1);
					int acUp = testMove(index,1);
					acUp--; // above counts moving dragEntry which shouldn't really count
					//System.out.println("AC up = "+acUp+", B Down = "+bDown);
					if (bDown <= acUp) {
						//System.out.println("Moving B down");
						moveEntries(index+1,-1);
					} else {
						//System.out.println("Moving A & C up");
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
				// TODO There is one special case: if all the modifiers are equal then we could manipulate the tiebreak scores.
				dragged.setRoll(prev.getTotal() - dragged.getModifier());
				int a_c = CombatEntry.compareInitiatives(prev, dragged);
				int c_b = CombatEntry.compareInitiatives(dragged, next);
				if (a_c <= 0 && c_b <= 0) {
					// A > C > B
					//System.out.println("A>C>B - done");
				} else if (a_c <= 0 && c_b > 0) {
					// A > B > C
					//System.out.println("A>B>C - move B down or A & C up");
					int bDown = testMove(index+1,-1);
					int acUp = testMove(index,1);
					acUp--; // above counts moving dragEntry which shouldn't really count
					//System.out.println("AC up = "+acUp+", B Down = "+bDown);
					if (bDown <= acUp) {
						//System.out.println("Moving B down");
						moveEntries(index+1,-1);
					} else {
						//System.out.println("Moving A & C up");
						moveEntries(index,1);
					}
				} else if (a_c > 0 && c_b <= 0) {
					// C > A > B
					//System.out.println("C>A>B - move A up or B & C down");
					int bcDown = testMove(index,-1);
					bcDown--; // above counts moving dragEntry which shouldn't really count
					int aUp = testMove(index-1,1);
					//System.out.println("A up = "+aUp+", B&C Down = "+bcDown);
					if (bcDown <= aUp) {
						//System.out.println("Moving B & C down");
						moveEntries(index,-1);
					} else {
						//System.out.println("Moving A up");
						moveEntries(index-1,1);
					}
				} else {
					System.err.println("Impossible: a_c = "+a_c+", c_b = "+c_b);
				}
			}
		}

		// Store the required order and details so we can check everything worked
		String postOrder = "", postDetails = "";
		for (CombatEntry e : list) {
			postOrder += e.getCreatureName()+",";
			postDetails += e + "\n";
		}
		if (!reqOrder.equals(postOrder)) {
			System.err.println("Initiative reorder failed. Required order: "+reqOrder);
			System.err.println("Final order: "+postOrder);
			System.err.println("Initial state: "+preDetails);
			System.err.println("Final state: "+postDetails);
		}

		noSort = false;	// re-enable sort
		fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,0,list.size()-1);
	}

	@Override
	public void sort() {
		if (noSort) return;
		Collections.sort(list,(ie1, ie2) -> CombatEntry.compareInitiatives(ie1,ie2));
		fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,0,list.size()-1);
	}

	@Override
	public void sort(Comparator<Object> c) {
		if (noSort) return;
		Collections.sort(list,c);
		fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,0,list.size()-1);
	}

	@Override
	public CombatEntry getElementAt(int index) {
		return list.get(index);
	}

	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listenerList.add(ListDataListener.class,l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(ListDataListener.class,l);
	}

	private void fireListDataEvent(int type, int index0, int index1) {
		ListDataEvent e = null;
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ListDataListener.class) {
				if (e == null) e = new ListDataEvent(this, type, index0, index1);
				switch(type) {
				case ListDataEvent.CONTENTS_CHANGED:
					((ListDataListener)listeners[i+1]).contentsChanged(e);
					break;
				case ListDataEvent.INTERVAL_ADDED:
					((ListDataListener)listeners[i+1]).intervalAdded(e);
					break;
				case ListDataEvent.INTERVAL_REMOVED:
					((ListDataListener)listeners[i+1]).intervalRemoved(e);
					break;
				}
			}
		}
	}

	public void addEntry(CombatEntry e) {
		list.add(e);
		e.addChangeListener(this);
		e.addActionListener(this);
		e.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (selected != null) {
					selected.setBorder(selectedBorder);
					selected.updateDetails(detailPanel, false);
				}
				if (ev.getSource() instanceof CombatEntry) {
					if (ev.getSource() != blankInit) {
						selected = (CombatEntry) ev.getSource();
						selectedBorder = selected.getBorder();
						selected.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED), selectedBorder));
						selected.updateDetails(detailPanel, true);
					}
				}
			}
		});
		if (e.isBlank()) {
			fireListDataEvent(ListDataEvent.INTERVAL_ADDED,list.size()-1,list.size()-1);
		} else {
			sort();
		}
	}

	private void removeEntry(CombatEntry e) {
		e.removeChangeListener(this);
		e.removeActionListener(this);
		int pos = list.indexOf(e);
		//System.out.println("remove entry "+e+" at "+pos);
		list.remove(e);
		fireListDataEvent(ListDataEvent.INTERVAL_REMOVED,pos,pos);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		CombatEntry changed = (CombatEntry)e.getSource();
		boolean reorder = false;

		// check if we need to add a new entry:
		if (changed == blankInit && !changed.isBlank()) {
//			System.out.println("Adding new entry");
			blankInit = new MonsterCombatEntry();
			addEntry(blankInit);
			reorder = true;
		}

		// check if reorder is necessary
		int index = list.indexOf(e.getSource());
		if (!reorder && index >0) {
			CombatEntry prev = list.get(index-1);
			if (CombatEntry.compareInitiatives(changed,prev) < 0) {
				//System.out.println("Changed entry is > prev");
				reorder = true;
			}
		}
		if (!reorder && index < list.size()-1) {
			CombatEntry next = list.get(index+1);
			if (CombatEntry.compareInitiatives(changed,next) > 0) {
				//System.out.println("Changed entry is < next");
				reorder = true;
			}
		}

		if (reorder) {
			sort();
		} else {
			// firing this in case a name or other property that is used elsewhere has changed.
			fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,0,list.size()-1);
		}
	}

	String getJSON(String indent) {
		String output = indent + "[\n";
		boolean first = true;
		for (CombatEntry e : list) {
			if (!e.isDMOnly()) {
				if (first) {
					first = false;
				} else {
					output += ",\n";
				}
				output += indent + "\t{\"name\": \"" + e.getCreatureName();
				output += "\", \"initiative\": " + e.getTotal() + "}";
			}
		}
		output += indent + "]\n";
		return output;
	}

	// returns the number of entries moved
	// start is the index of the entry to start from
	// dir is the direction: -1 is down (total will be lower), +1 is up (total will be higher)
	private int testMove(int start, int dir) {
//		System.out.println("testMove(start="+start+", ignore="+ignore+", dir="+dir+")");
//		for (int i=0; i<list.size(); i++) {
//			System.out.println("  "+i+": "+list.get(i));
//		}
		int i = start;
		int count = 1;
		while (i > 0 && i < list.size()-1) {
			CombatEntry current = list.get(i);
			CombatEntry next = list.get(i-dir);
			if (!current.isBlank() && !next.isBlank()) {
				// two real entries
				int comp = CombatEntry.compareInitiatives(current.getTotal()+dir,current.getModifier(),current.getTieBreak(),
						next.getTotal(),next.getModifier(),next.getTieBreak());
				//System.out.println("("+(current.getTotal()+dir)+") "+current+"\nvs. ("+next.getTotal()+") "+next+"\n = "+comp+" ("+(comp*dir)+")");
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
	private void moveEntries(int start, int dir) {
		//System.out.println("Moving entries from "+start+" in "+dir);
		int i = start;
		while (i >= 0 && i <= list.size()-1) {
			CombatEntry current = list.get(i);
			//System.out.println("Moving "+i+": "+current);
			if (!current.isBlank()) {
				current.adjustRoll(dir);
			}
			if (i == 0 || i == list.size()-1) break;
			// only proceed if there is a next element
			CombatEntry next = list.get(i-dir);
			//System.out.println("Next: "+next);
			if (!next.isBlank()) {
				// two real entries
				int comp = CombatEntry.compareInitiatives(current,next);
				//System.out.println("("+current.getTotal()+") "+current+"\nvs. ("+next.getTotal()+") "+next+"\n = "+comp+" ("+(comp*dir)+")");
				if (dir * comp >= 0) {
					//System.out.println("There is room, finished");
					break;
				}
			}
			i -= dir;
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("delete")) {
			CombatEntry e = (CombatEntry)arg0.getSource();
			removeEntry(e);
		}
	}

	// removes all created (non-character) initiative entries and reset rolls
	void reset() {
		// first check out all the entries and determine what to do with them
		// we need to do it this way because both changing the rolls and removing entries
		// will alter the list or list order
		ArrayList<CombatEntry> toRemove = new ArrayList<>();
		ArrayList<CombatEntry> toReset = new ArrayList<>();

		for (CombatEntry e : list) {
			if (e.getSource() instanceof Character) {
				toReset.add(e);
			} else if (e != blankInit) {
				toRemove.add(e);
			}
		}

		// remove entries to remove
		for (CombatEntry e : toRemove) {
			removeEntry(e);
		}

		// reset entries' rolls to 0
		for (CombatEntry e : toReset) {
			e.setRoll(0);
		}
	}

	// TODO should probably just match characters up on ids
	public void parseDOM(Element el) {
		if (!el.getNodeName().equals("InitiativeList")) return;
		reset();
		NodeList nodes = el.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element)nodes.item(i);
			String tag = e.getTagName();
			if (tag.equals("CharacterEntry")) {
				String name = e.getAttribute("name");
				CombatEntry entry = null;
				for (CombatEntry ce : list) {
					if (ce instanceof CharacterCombatEntry && name.equals(ce.getCreatureName())) {
						entry = ce;
						break;
					}
				}
				if (entry != null) {
					entry.setRoll(Integer.parseInt(e.getAttribute("roll")));
					entry.setTieBreak(Integer.parseInt(e.getAttribute("tieBreak")));
					String idStr = e.getAttribute("creatureID");
					if (idStr.length() > 0) {
						entry.creature.setID(Integer.parseInt(idStr));
					}
				}
			} else if (tag.equals("MonsterEntry")) {
				MonsterCombatEntry m = MonsterCombatEntry.parseDOM(e);
				addEntry(m);
			}
		}
	}

	Element getElement(Document doc) {
		Element el = doc.createElement("InitiativeList");
		for(CombatEntry e : list) {
			if (e != blankInit) el.appendChild(e.getElement(doc));
		}
		return el;
	}
}


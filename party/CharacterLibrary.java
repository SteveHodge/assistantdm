package party;

import gamesystem.XP;
import gamesystem.XP.Challenge;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Character.XPHistoryItem;
import swing.TableModelWithToolTips;

public class CharacterLibrary {
	final static Format format = new SimpleDateFormat("yyyy-MM-dd");

	public static Set<Character> characters = new HashSet<>();

	public static void add(Character c) {
		characters.add(c);
	}

	public static TableModel getXPHistoryTableModel() {
		return new PartyXPTableModel();
	}

	protected static Comparator<Character> charComparator = new Comparator<Character>() {
		@Override
		public int compare(Character c1, Character c2) {
			// if both characters have no xp history then they are treated as equal
			// if one character has no xp history then it is considered greater then the other
			if (c1.getXPHistoryCount() == 0) {
				return c2.getXPHistoryCount() == 0 ? 0 : -1;
			} else {
				if (c2.getXPHistoryCount() == 0) return 1;
			}
			XPHistoryItem x1 = c1.getXPHistory(0);
			XPHistoryItem x2 = c2.getXPHistory(0);
			// null date is considered earlier than any date
			if (x1.getDate() == null) {
				return (x2.getDate() == null ? 0 : -1);
			} else {
				if (x2.getDate() == null) return 1;
			}
			return x1.getDate().compareTo(x2.getDate());
		}
	};

	// TODO still issues with order here
	public static List<PartyXPItem> getXPHistory() {
		// try to combine history from each character into a single narrative
		List<PartyXPItem> items = null;
		List<Character> chars = new ArrayList<>(characters);
		Collections.sort(chars, charComparator);
		for (Character c : chars) {
			System.out.println("Examining "+c);
			if (items == null) {
				// first character
				items = new ArrayList<>();
				for (int i = 0; i < c.getXPHistoryCount(); i++) {
					items.add(new PartyXPItem(c, c.getXPHistory(i)));
				}
			} else {
				// subsequent characters
				int lastMatch = 0;
				for (int i = 0; i < c.getXPHistoryCount(); i++) {
					XPHistoryItem xpItem = c.getXPHistory(i);
					PartyXPItem ci = new PartyXPItem(c, xpItem);
					boolean found = false;
					int datePos = lastMatch;
					for (int j = lastMatch; j < items.size(); j++) {
						PartyXPItem pi = items.get(j);
						if (ci.equals(pi) && !pi.changes.containsKey(c)) {
							lastMatch = j;
							pi.changes.put(c, xpItem);
							found = true;
							break;
						} else if (pi.date == null || (ci.date != null && !ci.date.before(pi.date))) {	// added null tests
							datePos = j;
						}
					}
					if (!found) {
						items.add(datePos+1, ci);
					}
				}
			}
		}

		return items;
	}

	public static class PartyXPItem {
		Class<?> type;
		String comment;
		Date date;
		int partyCount;
		List<Challenge> challenges;
		Map<Character, XPHistoryItem> changes = new HashMap<>();

		PartyXPItem(Character c, XPHistoryItem ci) {
			type = ci.xpChange.getClass();
			date = ci.getDate();
			challenges = ci.getChallenges();
			partyCount = ci.getPartyCount();
			if (type == XP.XPChangeLevel.class) {
				comment = "Level Up";
			} else if (type == XP.XPChangeAdhoc.class) {
				comment = "Adhoc Change";
			} else {
				comment = ci.getComment();
			}
			changes.put(c,ci);
		}

		// assumes challenges is never null
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PartyXPItem)) return false;
			PartyXPItem o = (PartyXPItem)obj;
			if (!o.type.equals(type)) return false;
			if (o.date != null) {
				if (date == null || !o.date.equals(date)) return false;
			} else if (date != null) {
				return false;
			}
			if (o.comment != null) {
				if (comment == null || !o.comment.equals(comment)) return false;
			} else if (comment != null) {
				return false;
			}
			if (o.partyCount != partyCount) return false;
			if (o.challenges.size() != challenges.size()) return false;
			for (int i = 0; i < challenges.size(); i++) {
				if (!o.challenges.get(i).equals(challenges.get(i))) return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(type);
			b.append("\t").append(format.format(date));
			if (comment != null) b.append("\t").append(comment);
			b.append("\t").append(partyCount);
			for (Challenge c : challenges) {
				b.append(System.getProperty("line.separator"));
				b.append("\t\t").append(c);
			}
			b.append(System.getProperty("line.separator"));
			b.append("\t").append("Applies to: ");
			boolean first = true;
			for (Character c : changes.keySet()) {
				if (first) first = false;
				else b.append(", ");
				b.append(c);
			}
			return b.toString();
		}
	}

	@SuppressWarnings("serial")
	protected static class PartyXPTableModel extends AbstractTableModel implements TableModelWithToolTips {
		List<PartyXPItem> history;
		List<Character> chars;

		PartyXPTableModel() {
			history =  getXPHistory();
			chars = new ArrayList<>(characters);
			Collections.sort(chars, charComparator);
		}

		@Override
		public int getColumnCount() {
			return chars.size()*2+2;
		}

		@Override
		public int getRowCount() {
			return history.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			PartyXPItem item = history.get(row);
			if (col == 0) {
				if (item.date == null) return null;
				return format.format(item.date);

			} else if (col == 1) {
				String comment = item.comment;
				for (Challenge c : item.challenges) {
					if (comment == null || comment.length() == 0) {
						comment = c.toString();
					} else {
						comment += ", " + c;
					}
				}
				return comment;

			} else if (col > 1 && col < chars.size()+2) {
				Character c = chars.get(col-2);
				if (item.changes.containsKey(c)) {
					XPHistoryItem i = item.changes.get(c);
					if (item.type == XP.XPChangeLevel.class) {
						return i.getComment();
					} else {
						return i.getXP();
					}
				}

			} else {
				Character c = chars.get(col-2-chars.size());
				// TODO need to backtrack if there is no entry for this character
				while (row >= 0) {
					item = history.get(row);
					if (item.changes.containsKey(c)) {
						XPHistoryItem i = item.changes.get(c);
						return i.getTotal();
					}
					row--;
				}
			}
			return null;
		}

		@Override
		public String getToolTipAt(int row, int col) {
			int index = col-2;
			if (index >= chars.size()) index -= chars.size();
			if (col > 1) {
				PartyXPItem item = history.get(row);
				Character c = chars.get(index);
				if (item.changes.containsKey(c)) {
					XPHistoryItem i = item.changes.get(c);
					if (item.type == XP.XPChangeLevel.class) {
						return "From level "+i.getOldLevel()+" to "+i.getLevel();
					} else if (item.type == XP.XPChangeAdhoc.class) {
						String tip = i.getComment();
						if (tip != null && tip.length() == 0) return null;
						return tip;
					}
				}
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col < 2) return String.class;
			return Integer.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Date";
			if (column == 1) return "Comment";
			if (column < chars.size()+2) return chars.get(column-2).getName();
			return chars.get(column-2-chars.size()).getName();
		}
	}
}

package gamesystem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/* This class contains static methods and classes used to calculate and record xp.
 * Classes are:
 * CR - a challenge rating which may be an integer (should be > 0) or a fraction in the form of 1/X.
 * Challenge - a record of a group of one or more identical CRs, with a comment. E.g. 3 Goblins or CR 1/3
 * XPChange - base class for records of xp related change
 * XPChangeAdhoc - a record of an adhoc change to xp
 * XPChangeLevel - a record of a change from one level to another
 * XPChangeChallenges - a record of xp earned by defeating a group of Challenges. contains all info needed to recalculate the xp earned
 */
public class XP {
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static class Challenge {
		public CR cr;
		public int number;
		public String comment = "";

		@Override
		public String toString() {
			if (number == 1) return comment + " CR " + cr;
			return comment + " " + number + " x CR " + cr;
		}

		public String getXML(String indent, String nextIndent) {
			StringBuilder b = new StringBuilder();
			String nl = System.getProperty("line.separator");
			b.append(indent).append("<XPChallenge cr=\"").append(cr);
			b.append("\" number=\"").append(number);
			b.append("\">").append(comment).append("</XPChallenge>").append(nl);
			return b.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Challenge)) return false;
			Challenge o = (Challenge)obj;
			if (o.number != number) return false;
			if (!o.comment.equals(comment)) return false;
			if (!o.cr.equals(cr)) return false;
			return true;
		}
	}

	public abstract static class XPChange {
		String comment = "";
		Date date;

		public XPChange(String c, Date d) {
			comment = c;
			date = d;
		}

		public abstract int getXP();	// returns the change in xp - some classes will always return 0 (eg. XPChangeLevel)

		public Date getDate() {return date;}

		public String getComment() {return comment;}

		@Override
		public String toString() {
			if (comment != null && comment.length() > 0) {
				if (date != null) {
					return dateFormat.format(date)+" - "+comment;
				} else {
					return comment;
				}
			} else {
				if (date != null) {
					return dateFormat.format(date);
				}
			}
			return "";
		}

		public abstract void executeProcess(CreatureProcessor processor);
	}

	public static class XPChangeChallenges extends XPChange {
		public int xp;	// xp earned from challenges
		public int level;	// level when meeting challenges
		public int partyCount;	// number of party members meeting challenges
		public int penalty;	// % penalty applied to xp
		public List<Challenge> challenges = new ArrayList<>();

		public XPChangeChallenges(String c, Date d) {
			super(c,d);
		}

		@Override
		public int getXP() {
			return xp;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			String nl = System.getProperty("line.separator");
			b.append("XP = ").append(xp).append(" (earned at level ");
			b.append(level).append(" in party of ").append(partyCount);
			if (penalty != 0) b.append(" with a penalty of ").append(penalty).append("%");
			b.append(").").append(nl);
			String com = super.toString();
			if (com != "") {
				b.append("Comment: ").append(com).append(nl);
			}
			b.append("Challenges: ").append(nl);
			for (Challenge c : challenges) {
				b.append("\t").append(c).append(nl);
			}
			return b.toString();
		}

		@Override
		public void executeProcess(CreatureProcessor processor) {
			processor.processXPChange(this);
		}
	}

	public static class XPChangeAdhoc extends XPChange {
		int xp;

		public XPChangeAdhoc(int xp, String c, Date d) {
			super(c,d);
			this.xp = xp;
		}

		@Override
		public int getXP() {
			return xp;
		}

		@Override
		public String toString() {
			String txt = "Adhoc change of "+xp;
			String c = super.toString();
			if (c != "") txt += " ("+c+")";
			return txt;
		}

		@Override
		public void executeProcess(CreatureProcessor processor) {
			processor.processXPChange(this);
		}
	}

	public static class XPChangeLevel extends XPChange {
		int oldLevel;
		int newLevel;

		public XPChangeLevel(int o, int n, String c, Date d) {
			super(c,d);
			oldLevel = o;
			newLevel = n;
		}

		@Override
		public int getXP() {
			return 0;
		}

		@Override
		public String toString() {
			String txt = "Changed level from "+oldLevel+" to "+newLevel;
			String c = super.toString();
			if (c != "") txt += " ("+c+")";
			return txt;
		}

		public int getOldLevel() {
			return oldLevel;
		}

		public int getNewLevel() {
			return newLevel;
		}

		@Override
		public void executeProcess(CreatureProcessor processor) {
			processor.processXPChange(this);
		}
	}

	public static int getXP(int level, int members, int penalty, Collection<Challenge> challenges) {
		int xp = 0;
		for (Challenge c : challenges) {
			if (c.cr == null || c.cr.cr == 0 || c.number == 0) continue;
			int award = getAward(level, c.cr.cr) * c.number;
			if (c.cr.inverted) {
				award = Math.round(getAward(level,1) * c.number / c.cr.cr);
			}
			award = Math.round((float)award / members);
			xp += award;
		}
		if (penalty != 0) xp = (xp * (100-penalty) / 100);
		return xp;
	}
/*
	public static int getXP(int level, int members, Set<CR> crs) {
		int xp = 0;
		for (CR cr : crs) {
			int award = getAward(level, cr.cr);
			if (cr.inverted) {
				award = Math.round(getAward(level,1) / cr.cr);
			}
			award = Math.round((float)award / members);
			xp += award;
		}
		return xp;
	}
 */
	/* this array holds the base xp values. in most cases a character of level 'L'
	 * defeating an opponent of CR 'C' will receive xp = baseXP[C-L+7]*L/2
	 */
	private static int baseXP[] = {50,75,100,150,200,300,400,600,900,1200,1800,2400,3600,4800,7200};

	// expects cr >= 1. if cr < 1 then xp = getAward(level,1) * cr
	public static int getAward(int level, int cr) {
		if (level < 3) level = 3;
		int i = cr - level + 7;
		if (i < 0 || i >= baseXP.length) return 0;	// cr too high or too low for this level character
		int xp = (baseXP[i] * level + 1) / 2;	// the plus 1 ensures correct rounding
		if (cr == 1 && xp > 300) xp = 300;	// can't get more than 300 for a cr 1
		if (level == 4 && cr >= 5 && cr % 2 == 1) {
			// level 4 for cr 5, 7, 9, 11 is different
			xp = 1600 * (1 << (cr-5)/2);
		}
		return xp;
	}

	public static int getXPRequired(int l) {
		int total = 0;
		for (int i = 1; i < l; i++) {
			total += i*1000;
		}
		return total;
	}

}

package party;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.Character.XPChangeChallenges;


public class XP {
/*	public static void main(String[] args) {
		System.out.print("\t  ");
		for (int cr = 1; cr <= 20; cr++) {
			System.out.print("\t"+cr);
		}
		System.out.println();
		for (int level = 1; level <= 20; level++) {
			System.out.print("\t"+level+": ");
			for (int cr = 1; cr <= 20; cr++) {
				System.out.print("\t"+getAward(level,cr));
			}
			System.out.println();
		}
*/
/*		HashSet<CR> crs = new HashSet<CR>();
		crs.add(new CR(1));
		crs.add(new CR(2));
		crs.add(new CR(2,true));
		System.out.println("First = "+getXP(1,4,crs));
		crs.clear();
		crs.add(new CR(2,true));
		System.out.println("Second = "+(getXP(1,5,crs)*13));
		crs.clear();
		crs.add(new CR(1));
		crs.add(new CR(1));
		crs.add(new CR(1));
		crs.add(new CR(1));
		crs.add(new CR(1));
		crs.add(new CR(2));
		crs.add(new CR(3));
		System.out.println("Third = "+(getXP(1,5,crs)));
	}
		*/

	public static class CR {
		public int cr;
		public boolean inverted = false;	// true if cr should be inverted, i.e. actual cr = 1 / cr

		public CR(int cr) {this.cr = cr;}

		public CR(int cr, boolean inv) {this.cr = cr; inverted = inv;}

		public CR(String text) {
			try {
				cr = Integer.parseInt(text);
				return;
			} catch (NumberFormatException e) {
				if (text.startsWith("1/")) {
					inverted = true;
					cr = Integer.parseInt(text.substring(2));
					return;
				}
			}
			throw new NumberFormatException();
		}

		public String toString() {
			if (inverted) return "1/"+cr;
			return ""+cr;
		}
	}

	public static class Challenge {
		public CR cr;
		public int number;
		public String comment;

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

		public static Challenge parseDOM(Element e) {
			if (!e.getNodeName().equals("XPChallenge")) return null;
			Challenge c = new Challenge();
			c.cr = new CR(e.getAttribute("cr"));
			c.number = Integer.parseInt(e.getAttribute("number"));
			c.comment = e.getTextContent();
			return c;
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
		if (penalty != 0) xp = (int)(xp * (100-penalty) / 100);
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

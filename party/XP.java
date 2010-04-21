package party;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


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

	public abstract static class XPChange {
		String comment;
		Date date;

		public XPChange(String c, Date d) {
			comment = c;
			date = d;
		}

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

		public abstract String getXML(String indent, String nextIndent);
	}

	public static class XPChangeChallenges extends XPChange {
		int xp;	// xp earned from challenges
		int level;	// level when meeting challenges
		int partyCount;	// number of party members meeting challenges
		int penalty;	// % penalty applied to xp
		List<Challenge> challenges = new ArrayList<Challenge>();

		public XPChangeChallenges(String c, Date d) {
			super(c,d);
		}

		public String getXML(String indent, String nextIndent) {
			StringBuilder b = new StringBuilder();
			String nl = System.getProperty("line.separator");
			b.append(indent).append("<XPAward xp=\"").append(xp);
			b.append("\" level=\"").append(level);
			b.append("\" party=\"").append(partyCount);
			b.append("\" penalty=\"").append(penalty);
			if (date != null) {
				b.append("\" date=\"").append(dateFormat.format(date));
			}
			b.append("\">").append(nl);
			if (comment != null && comment.length() > 0) {
				b.append(indent+nextIndent).append("<Comment>").append(comment).append("</Comment>").append(nl);
			}
			for (Challenge c : challenges) {
				b.append(c.getXML(indent+nextIndent,nextIndent));
			}
			b.append(indent).append("</XPAward>").append(nl);
			return b.toString();
		}
	
		public static XPChangeChallenges parseDOM(Element e) {
			if (!e.getNodeName().equals("XPAward")) return null;
			Date d = null;
			try {
				d = dateFormat.parse(e.getAttribute("date"));
			} catch (ParseException e1) {
			}
			XPChangeChallenges c = new XPChangeChallenges(null,d);
			c.xp = Integer.parseInt(e.getAttribute("xp"));
			c.level = Integer.parseInt(e.getAttribute("level"));
			c.partyCount = Integer.parseInt(e.getAttribute("party"));
			if (e.hasAttribute("penalty")) c.penalty = Integer.parseInt(e.getAttribute("penalty"));
	
			NodeList nodes = e.getChildNodes();
			if (nodes != null) {
				for (int i=0; i<nodes.getLength(); i++) {
					if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
					Element child = (Element)nodes.item(i);
					String tag = child.getTagName();
	
					if (tag.equals("XPChallenge")) {
						Challenge chal = Challenge.parseDOM(child);
						c.challenges.add(chal);
					} else if (tag.equals("Comment")) {
						c.comment = child.getTextContent();
					}
				}
			}
			return c;
		}
	
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
	}

	public static class XPChangeAdhoc extends XPChange {
		int xp;
	
		public XPChangeAdhoc(int xp, String c, Date d) {
			super(c,d);
			this.xp = xp;
		}
	
		public String getXML(String indent, String nextIndent) {
			String xml =indent+"<XPChange xp=\""+xp+"\"";
			if (date != null) {
				xml += " date=\""+dateFormat.format(date)+"\"";
			}
			if (comment != null && comment.length() > 0) xml += ">"+comment+"</XPChange>";
			else xml += "/>";
			return xml+System.getProperty("line.separator");
		}
	
		public static XPChangeAdhoc parseDOM(Element e) {
			if (!e.getNodeName().equals("XPChange")) return null;
			String comment = e.getTextContent();
			Date d = null;
			try {
				d = dateFormat.parse(e.getAttribute("date"));
			} catch (ParseException e1) {
			}
			XPChangeAdhoc c = new XPChangeAdhoc(Integer.parseInt(e.getAttribute("xp")),comment,d);
			return c;
		}
	
		public String toString() {
			String txt = "Adhoc change of "+xp;
			String c = super.toString();
			if (c != "") txt += " ("+c+")";
			return txt;
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
	
		public String getXML(String indent, String nextIndent) {
			String xml =indent+"<XPLevelChange old=\""+oldLevel+"\" new=\""+newLevel+"\"";
			if (date != null) {
				xml += " date=\""+dateFormat.format(date)+"\"";
			}
			if (comment != null && comment.length() > 0) xml += ">"+comment+"</XPLevelChange>";
			else xml += "/>";
			return xml+System.getProperty("line.separator");
		}

		public static XPChangeLevel parseDOM(Element e) {
			if (!e.getNodeName().equals("XPLevelChange")) return null;
			String comment = e.getTextContent();
			Date d = null;
			try {
				d = dateFormat.parse(e.getAttribute("date"));
			} catch (ParseException e1) {
			}
			XPChangeLevel c = new XPChangeLevel(
					Integer.parseInt(e.getAttribute("old")),
					Integer.parseInt(e.getAttribute("new")),
					comment, d
			);
			return c;
		}

		public String toString() {
			String txt = "Changed level from "+oldLevel+" to "+newLevel;
			String c = super.toString();
			if (c != "") txt += " ("+c+")";
			return txt;
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

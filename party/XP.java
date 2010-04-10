package party;
import java.util.HashSet;
import java.util.Set;


public class XP {
	public static void main(String[] args) {
/*		System.out.print("\t  ");
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
		}*/

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
		*/
	}

	public static class CR {
		public int cr;
		public boolean inverted = false;	// true if cr should be inverted, i.e. actual cr = 1 / cr
		public CR(int cr) {this.cr = cr;}
		public CR(int cr, boolean inv) {this.cr = cr; inverted = inv;}
	}

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
			xp = 1600;	// xp for cr 5 - it doubles for each increase of 2 in the cr
			switch (cr) {
			// intentional drop through here...
			case 11: xp += xp;
			case 9: xp += xp;
			case 7: xp += xp;
			}
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

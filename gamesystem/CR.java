package gamesystem;

public class CR {
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

	@Override
	public String toString() {
		if (inverted) return "1/"+cr;
		return ""+cr;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CR)) return false;
		CR o = (CR)obj;
		return o.cr == cr && o.inverted == inverted;
	}
}
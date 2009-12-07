import java.awt.Color;


/*
Untouched	100%	dark green
Lightly Wounded	80% to 100%	light green
Wounded	60% to 80%		dark yellow
Heavily Wounded	40% to 60%	orangy yellow
Severely Wounded	20% to 40%	orange
Critical	1 to 20%	red
Disabled	0		grey
Dying	-10 to -1	black
*/

public class Status {
	public static final int STATUS_UNTOUCHED = 0;
	public static final int STATUS_LIGHT = 1;
	public static final int STATUS_WOUNDED = 2;
	public static final int STATUS_HEAVY = 3;
	public static final int STATUS_SEVERE = 4;
	public static final int STATUS_CRITICAL = 5;
	public static final int STATUS_DISABLED = 6;
	public static final int STATUS_DYING = 7;
	public static final int STATUS_DEAD = 8;

	public static final String descriptions[] = {"Untouched","Lightly Wounded","Wounded","Heavily Wounded",
		"Severely Wounded", "Critical", "Disabled", "Dying", "Dead"};
	public static final Color colours[] = {
		new Color(255, 255, 255),
		new Color(0, 127, 0),
		new Color(0, 255, 0),
		new Color(255, 255, 0),
		new Color(255, 127, 0),
		new Color(255, 0, 0),
		new Color(200, 200, 200),
		new Color(120, 120, 120),
		new Color(0, 0, 0)
	};

	public static int getStatus(int maxHPs, int hps) {
		if (hps >= maxHPs) return STATUS_UNTOUCHED;
		if (hps <= -10) return STATUS_DEAD;
		if (hps >= -9 && hps < 0) return STATUS_DYING;
		if (hps == 0) return STATUS_DISABLED;
		if (10*hps < 2*maxHPs) return STATUS_CRITICAL;
		if (10*hps < 4*maxHPs) return STATUS_SEVERE;
		if (10*hps < 6*maxHPs) return STATUS_HEAVY;
		if (10*hps < 8*maxHPs) return STATUS_WOUNDED;
		//if (hps < maxHPs)
		return STATUS_LIGHT;
	}
}

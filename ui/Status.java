package ui;

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

public enum Status implements CreatureStatus {
	UNTOUCHED(new Color(0, 204, 51), "Untouched"),
	LIGHT(new Color(153, 204, 0), "Lightly Wounded"),
	WOUNDED(new Color(204, 255, 51), "Wounded"),
	HEAVY(new Color(255, 255, 0), "Heavily Wounded"),
	SEVERE(new Color(255, 204, 51), "Severely Wounded"),
	CRITICAL(new Color(255, 0, 0), "Critical"),
	DISABLED(new Color(200, 200, 200), "Disabled"),
	DYING(new Color(120, 120, 120), "Dying"),
	DEAD(new Color(0, 0, 0), "Dead");

	@Override
	public String toString() {
		return description;
	}

	@Override
	public Color getColor() {
		return color;
	}

	public static Status getStatus(int maxHPs, int hps) {
		if (hps >= maxHPs) return UNTOUCHED;
		if (hps <= -10) return DEAD;
		if (hps >= -9 && hps < 0) return DYING;
		if (hps == 0) return DISABLED;
		if (10 * hps < 2 * maxHPs) return CRITICAL;
		if (10 * hps < 4 * maxHPs) return SEVERE;
		if (10 * hps < 6 * maxHPs) return HEAVY;
		if (10 * hps < 8 * maxHPs) return WOUNDED;
		//if (hps < maxHPs)
		return LIGHT;
	}

	private Status(Color c, String d) {
		color = c;
		description = d;
	}

	private Color color;
	private String description;
}

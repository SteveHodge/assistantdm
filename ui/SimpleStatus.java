package ui;

import java.awt.Color;

public enum SimpleStatus implements CreatureStatus {
	HEALTHY(new Color(0, 204, 51), "Healthy"),
	BLOODIED(new Color(255, 0, 0), "Bloodied"),
	DISABLED(new Color(200, 200, 200), "Disabled"),
	DYING(new Color(120, 120, 120), "Dying"),
	DEAD(new Color(0, 0, 0), "Dead");

	@Override
	public String toString() {
		return description;
	}

	public Color getColor() {
		return color;
	}

	public static SimpleStatus getStatus(int maxHPs, int hps) {
		if (hps > maxHPs / 2) return HEALTHY;
		if (hps <= -10) return DEAD;
		if (hps >= -9 && hps < 0) return DYING;
		if (hps == 0) return DISABLED;
		return BLOODIED;
	}

	private SimpleStatus(Color c, String d) {
		color = c;
		description = d;
	}

	private Color color;
	private String description;
}

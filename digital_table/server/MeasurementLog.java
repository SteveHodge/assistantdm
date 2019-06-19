package digital_table.server;

import java.io.Serializable;


public class MeasurementLog implements Serializable {
	private static final long serialVersionUID = 1L;

	public String name;
	public int id;
	public long last;
	public long worst;
	public long average;
	public MeasurementLog[] components;

	public MeasurementLog(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public MeasurementLog(String name, int id, int compCount) {
		this.name = name;
		this.id = id;
		if (compCount > 0) components = new MeasurementLog[compCount];
	}

	public void updateTotal() {
		last = 0;
		if (components != null) {
			for (int i = 0; i < components.length; i++) {
				if (components[i] != null) {
					last += components[i].last;
					average += components[i].average;
				}
			}
		}
	}
}

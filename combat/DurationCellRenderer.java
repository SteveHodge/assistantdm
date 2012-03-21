package combat;

import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class DurationCellRenderer extends DefaultTableCellRenderer {
	public DurationCellRenderer() {
		super();
		setHorizontalAlignment(RIGHT);
	}

	public void setValue(Object value) {
		if (value instanceof Integer) {
			int duration = ((Integer)value).intValue();
			if (duration >= 900) {
				setText((duration / 600)+" Hours");
			} else if (duration >= 20) {
				setText((duration / 10)+" Minutes");
			} else {
				setText(""+duration+" Rounds");
			}
			setToolTipText(""+duration+" Rounds");
		} else {
			super.setValue(value);
		}
	}
}

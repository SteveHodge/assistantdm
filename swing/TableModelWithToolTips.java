package swing;

import javax.swing.table.TableModel;

public interface TableModelWithToolTips extends TableModel {
	public String getToolTipAt(int row, int col);
}

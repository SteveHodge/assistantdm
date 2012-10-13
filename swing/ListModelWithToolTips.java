package swing;

import javax.swing.ListModel;

public interface ListModelWithToolTips extends ListModel {
	public String getToolTipAt(int index);
}

package swing;

import javax.swing.ListModel;

public interface ListModelWithToolTips<T> extends ListModel<T> {
	public String getToolTipAt(int index);
}

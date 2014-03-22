package swing;

import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListModel;

@SuppressWarnings("serial")
public class JListWithToolTips<T> extends JList<T> {
	public JListWithToolTips() {
		super();
	}

	public JListWithToolTips(ListModel<T> dataModel) {
		super(dataModel);
	}

	public JListWithToolTips(T[] listData) {
		super(listData);
	}

	public JListWithToolTips(Vector<T> listData) {
		super(listData);
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		String tip = null;
		int index = locationToIndex(e.getPoint());

		ListModel<T> m = getModel();
		if (m instanceof ListModelWithToolTips) {
			tip = ((ListModelWithToolTips<T>) m).getToolTipAt(index);
		}

		if (tip == null) {
			tip = super.getToolTipText(e);
		}
		return tip;
	}
}

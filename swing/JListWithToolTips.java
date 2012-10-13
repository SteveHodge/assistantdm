package swing;

import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListModel;

@SuppressWarnings("serial")
public class JListWithToolTips extends JList {
	public JListWithToolTips() {
		super();
	}

	public JListWithToolTips(ListModel dataModel) {
		super(dataModel);
	}

	public JListWithToolTips(Object[] listData) {
		super(listData);
	}

	public JListWithToolTips(Vector<?> listData) {
		super(listData);
	}

	public String getToolTipText(MouseEvent e) {
		String tip = null;
		int index = locationToIndex(e.getPoint());

		ListModel m = getModel();
		if (m instanceof ListModelWithToolTips) {
			tip = ((ListModelWithToolTips)m).getToolTipAt(index);
		}

		if (tip == null) {
			tip = super.getToolTipText(e);
		}
		return tip;
	}
}

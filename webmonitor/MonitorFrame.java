package webmonitor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class MonitorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	JTextArea messageArea;
	SubscriberModel subModel;
	JScrollPane subScroller;

	MonitorFrame(SubscriberModel model) {
		super("Webpage Monitor");

		subModel = model;
		JTable subTable = new JTable(subModel);
		subTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		subTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		subTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		subTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		subTable.getColumnModel().getColumn(4).setPreferredWidth(400);
//		subTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		subScroller = new JScrollPane(subTable);
		subScroller.setPreferredSize(new Dimension(800, 150));
		subTable.setFillsViewportHeight(true);


		messageArea = new JTextArea(10, 150);
		JScrollPane scroller = new JScrollPane(messageArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		GridBagConstraints c = new GridBagConstraints();
		setLayout(new GridBagLayout());

		c.gridx = 0;
		add(subScroller, c);

		add(scroller, c);

		pack();
	}

	void addMessage(String text) {
		messageArea.append(text + "\n\n");

		// scroll to bottom
		messageArea.revalidate();
		SwingUtilities.invokeLater(() -> {
			messageArea.scrollRectToVisible(new Rectangle(0, messageArea.getHeight() - 1, 1, 1));
		});
	}
}

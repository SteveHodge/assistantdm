package webmonitor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class MonitorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	JTextArea messageArea;
	SubscriberModel subModel;

	MonitorFrame(SubscriberModel model) {
		super("Webpage Monitor");

		subModel = model;
		JTable subTable = new JTable(subModel);
		JScrollPane tableScroll = new JScrollPane(subTable);

		messageArea = new JTextArea(10, 150);
		JScrollPane scroller = new JScrollPane(messageArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		GridBagConstraints c = new GridBagConstraints();
		setLayout(new GridBagLayout());

		c.gridx = 0;
		add(tableScroll, c);

		add(scroller, c);

		pack();
	}

	void addMessage(String text) {
		messageArea.append(text + "\n\n");
	}
}

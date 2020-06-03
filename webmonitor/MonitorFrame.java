package webmonitor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import gamesystem.SavingThrow;
import party.Character;
import webmonitor.SubscriberModel.Subscriber;

public class MonitorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	JTextArea messageArea;
	SubscriberModel subModel;
	JScrollPane subScroller;
	WebsiteMonitor monitor;

	enum RequestType {
		INITIAITVE("Initiative", "initiative", "initiative", "initiative"),
		SANITY("Sanity", "sanity", "sanity", "sanity", "1d100"),
		FORT_SAVE(SavingThrow.Type.FORTITUDE.toString() + " Save", "saving_throws.fortitude", "save", SavingThrow.Type.FORTITUDE.toString().toLowerCase()), 
		REFLEX_SAVE(SavingThrow.Type.REFLEX.toString() + " Save", "saving_throws.reflex", "save", SavingThrow.Type.REFLEX.toString().toLowerCase()),
		WILL_SAVE(SavingThrow.Type.WILL.toString() + " Save", "saving_throws.will", "save", SavingThrow.Type.WILL.toString().toLowerCase());

		@Override
		public String toString() {
			return description;
		}

		public String getStatisticName() {
			return statistic;
		}

		public String getRollType() {
			return rollType;
		}

		public String getTitle() {
			return title;
		}

		public String getRoll(Character c) {
			if (roll != null) return roll;
			int mod = (int) c.getProperty(statistic).getValue();
			String dice = "1d20" + (mod > 0 ? "+" : "") + mod;
			return dice;
		}

		RequestType(String description, String stat, String type, String title, String roll) {
			this(description, stat, type, title);
			this.roll = roll;
		}

		RequestType(String description, String stat, String type, String title) {
			this.description = description;
			this.statistic = stat;
			this.rollType = type;
			this.title = title;
		}

		String description;
		String statistic;
		String roll;
		String rollType;
		String title;
	};

	MonitorFrame(SubscriberModel model, WebsiteMonitor monitor) {
		super("Webpage Monitor");

		this.monitor = monitor;
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

		JComboBox<RequestType> rollTypeBox = new JComboBox<>(RequestType.values());

		JButton requestButton = new JButton("Request Roll");
		requestButton.addActionListener(e -> {
			Set<Subscriber> subs = new HashSet<>();
			int[] selected = subTable.getSelectedRows();
			for (int i = 0; i < selected.length; i++) {
				subs.add(subModel.getSubscriber(selected[i]));
			}

			RequestType type = (RequestType) rollTypeBox.getSelectedItem();

			monitor.requestRoll(subs, type);
		});

		GridBagConstraints c = new GridBagConstraints();
		setLayout(new GridBagLayout());

		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;
		add(subScroller, c);

		c.gridx = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		add(rollTypeBox, c);

		c.gridy = 1;
		add(requestButton, c);

		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
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

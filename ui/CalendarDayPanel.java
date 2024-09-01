package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import gamesystem.Calendar;
import gamesystem.Calendar.Comment;
import gamesystem.Calendar.Event;
import gamesystem.Calendar.Month;

@SuppressWarnings("serial")
public class CalendarDayPanel extends JPanel {
	JLabel dateLabel;
	JTextArea eventArea;
	JTextArea commentArea;
	JTextArea customEventArea;
	JTextArea monthArea;
	JButton currentButton;
	int currentYear;
	Month currentMonth;
	int currentDay;
	Event currentEvent;
	Comment currentComment;

	public CalendarDayPanel()
	{
		setBorder(BorderFactory.createLineBorder(Color.BLUE));
		setPreferredSize(new Dimension(600, 200));

		currentButton = new JButton("Make Current");
		dateLabel = new JLabel();
		monthArea = new JTextArea();
		monthArea.setEditable(false);
		monthArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Month Info"));
		eventArea = new JTextArea();
		eventArea.setEditable(false);
		eventArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Annual Events"));
		customEventArea = new JTextArea();
		customEventArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Custom Events"));
		commentArea = new JTextArea();
		commentArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Notes"));

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(dateLabel, c);

		c.gridy++;
		c.gridwidth = 2;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(commentArea), c);

		c.gridy++;
		add(new JScrollPane(customEventArea), c);

		c.gridy++;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JScrollPane(eventArea), c);

		c.gridy++;
		add(new JScrollPane(monthArea), c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		add(currentButton, c);
	}

	public void setDate(int day, Month month, int year) {
		Calendar.setCustomEvent(currentMonth, currentDay, customEventArea.getText());
		Calendar.setComment(currentYear, currentMonth, currentDay, commentArea.getText());

		dateLabel.setText("" + day + " " + month.name + " " + year);
		currentYear = year;
		currentMonth = month;
		currentDay = day;


		currentEvent = Calendar.getCustomEvent(month, day);
		if (currentEvent == null)
			customEventArea.setText(null);
		else
			customEventArea.setText(currentEvent.name);

		currentComment = Calendar.getComment(year, month, day);
		if (currentComment == null)
			commentArea.setText(null);
		else
			commentArea.setText(currentComment.comment);

		List<Event> events = Calendar.getEvents(month, day);
		String eventText = "";
		for (Event event : events) {
			eventText += event.name + "\n";
			eventText += event.description.getTextContent() + "\n";
		}
		eventArea.setText(eventText);

		String monthText = month.name + " (" + month.season + ")\n";
		monthText += month.description.getTextContent();
//		System.out.println(monthText);
		monthArea.setText(monthText);
		revalidate();
	}
}

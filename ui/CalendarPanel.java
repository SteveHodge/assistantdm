package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;

import gamesystem.Calendar;
import gamesystem.Calendar.Month;
import gamesystem.Calendar.Weekday;

// FIXME doesn't handle start month that spans the end of a year

@SuppressWarnings("serial")
public class CalendarPanel extends JPanel {
	public static JFrame MakeCalendarFrame() {
		CalendarDayPanel dp = new CalendarDayPanel();
		int month = Calendar.currentMonth - 1;
		if (month < 1) month = 1;
		if (month > 9) month = 9;	// hack to avoid issue with wrapping years
		CalendarPanel cp = new CalendarPanel(Calendar.currentYear, month);

		dp.currentButton.addActionListener(e -> {
			Calendar.currentYear = dp.currentYear;
			Calendar.currentMonth = dp.currentMonth.number;
			Calendar.currentDay = dp.currentDay;
			cp.setCalendar(cp.currentYear, cp.currentMonth);
		});

		cp.addCalendarListener(e -> {
			dp.setDate(e.getDay(), e.getMonth(), e.getYear());
		});

		dp.setDate(Calendar.currentDay, Calendar.months[Calendar.currentMonth - 1], Calendar.currentYear);

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(cp);
		p.add(new JSeparator(JSeparator.VERTICAL));
		p.add(dp);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(p);
		frame.pack();
		frame.setSize(1100, 700);
		frame.setLocationRelativeTo(null);
		return frame;
	}

	Font dayFont;
	Font weekdayFont;
	Font monthFont;
	MouseAdapter dayListener;
	List<JPanel> panels = new ArrayList<>();
	List<JLabel> labels = new ArrayList<>();
	int currentYear;
	int currentMonth;

	private EventListenerList listenerList = new EventListenerList();

	public CalendarPanel(int year, int month) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		dayFont = getFont().deriveFont(15f);
		weekdayFont = dayFont.deriveFont(Font.BOLD);
		monthFont = weekdayFont.deriveFont(20f);

		dayListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getSource() instanceof DayLabel) {
					DayLabel day = (DayLabel)e.getSource();
					fireCalendarEvent(day.day, day.month, currentYear);
				}
			}
		};

		JComboBox<Month> monthCombo = new JComboBox<>(Calendar.months);
		monthCombo.setSelectedIndex(month - 1);
		monthCombo.addActionListener(e -> {
			setCalendar(currentYear, ((Month) monthCombo.getSelectedItem()).number);
		});

		JSpinner spinner = new JSpinner(new SpinnerNumberModel(year, 0, 1000, 1));
		spinner.addChangeListener(e -> {
			setCalendar((int) (spinner.getModel().getValue()), currentMonth);
		});

		JPanel p = new JPanel();
		p.add(monthCombo);
		p.add(spinner);
		add(p);

		for (int i = 0; i < 3; i++) {
			add(new JSeparator(JSeparator.HORIZONTAL));
			JLabel l = createLabel("", monthFont);
			l.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(l);
			labels.add(l);
			JPanel m = new JPanel();
			add(m);
			panels.add(m);
		}

		setCalendar(year, month);
	}

	void setCalendar(int year, int month) {
		currentYear = year;
		currentMonth = month;

		for (int i = 0; i < 3; i++) {
			JLabel l = labels.get(i);
			l.setText(Calendar.months[month + i - 1].name + " " + year);
			JPanel m = panels.get(i);
			m.removeAll();
			setMonthPanel(m, year, month + i);
		}
		revalidate();
	}

	void setMonthPanel(JPanel p, int year, int month) {
		int firstWeekday = Calendar.firstWeekday.number - 1;
		// add any days for the months before firstMonth
		for (int i = 0; i < month - 1; i++) {
			firstWeekday = (firstWeekday + Calendar.months[i].days) % Calendar.weekdays.length;
		}

		p.setLayout(new GridLayout(0, Calendar.weekdays.length));

		// add weekday header
		for (Weekday w : Calendar.weekdays) {
			p.add(createLabel(w.abbreviation, weekdayFont));
		}

		// first week, skip to the first day
		for (int j = 0; j < firstWeekday; j++)
			p.add(new JPanel());

		// output the month
		for (int j = 1; j <= Calendar.months[month - 1].days; j++) {
			DayLabel l = new DayLabel(Calendar.months[month - 1], j, dayFont);
			if (Calendar.hasEvent(Calendar.months[month - 1], j)) {
				l.setForeground(Color.RED);
			}
			if (year == Calendar.currentYear && month == Calendar.currentMonth && j == Calendar.currentDay) {
				l.setBorder(BorderFactory.createCompoundBorder(l.getBorder(), BorderFactory.createLineBorder(Color.RED, 2)));
			}
			l.addMouseListener(dayListener);
			p.add(l);
		}
	}

	class DayLabel extends JLabel
	{
		Month month;
		int day;

		DayLabel(Month month, int day, Font f) {
			this.month = month;
			this.day = day;
			setText("" + day);
			setFont(f);
			setHorizontalAlignment(JLabel.CENTER);
			setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		}
	}

	JLabel createLabel(String text, Font f) {
		JLabel l = new JLabel(text);
		l.setFont(f);
		l.setHorizontalAlignment(JLabel.CENTER);
		l.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		return l;
	}

	public void addCalendarListener(CalendarListener listener) {
		listenerList.add(CalendarListener.class, listener);
	}

	public void removeCalendarListener(CalendarListener listener) {
		listenerList.remove(CalendarListener.class, listener);
	}

	void fireCalendarEvent(int d, Month m, int y) {
		CalendarEvent e = null;
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == CalendarListener.class) {
				if (e == null) e = new CalendarEvent(this, d, m, y);
				((CalendarListener) listeners[i + 1]).daySelected(e);
			}
		}

	}

	public static class CalendarEvent extends EventObject {
		private int day;
		private Month month;
		private int year;

		public CalendarEvent(Object source, int day, Month month, int year) {
			super(source);
			this.day = day;
			this.month = month;
			this.year = year;
		}

		public int getDay() {
			return day;
		}

		public Month getMonth() {
			return month;
		}

		public int getYear() {
			return year;
		}
	}

	public static interface CalendarListener extends EventListener {
		void daySelected(CalendarEvent e);
	}

	String getTextCalendar(int year, int month) {
		StringBuilder s = new StringBuilder();
		int currentDay = Calendar.firstWeekday.number - 1;
		// add any days for the months before firstMonth
		for (int i = 0; i < month - 1; i++) {
			currentDay = (currentDay + Calendar.months[i].days) % Calendar.weekdays.length;
		}

		for (int i = 0; i < 3; i++) {
//			int rows = (currentDay > 0 ? 1 : 0)	// how many rows we need: 1 if we've got a partial first row
//					+ ((Calendar.months[month - 1].days - currentDay) / Calendar.weekdays.length)	// plus one per complete week for the remaining days
//					+ ((Calendar.months[month - 1].days - currentDay) % Calendar.weekdays.length == 0 ? 0 : 1);	// plus one if there are any remaining days for the last row
//			s.append("rows = ").append(rows).append("\n");

			s.append(Calendar.months[month - 1].name + " " + year).append("\n");
			// print out weekday header
			for (Weekday w : Calendar.weekdays) {
				s.append(" ").append(w.abbreviation).append(" ");
			}
			s.append("\n");
			// first week, skip to the first day
			for (int j = 0; j < currentDay; j++)
				s.append("   ");
			// output the month
			for (int j = 1; j <= Calendar.months[month - 1].days; j++) {
				if (j < 10)
					s.append(" ");
				s.append(j).append(" ");
				if (++currentDay == 7) {
					s.append("\n");
					currentDay = 0;
				}
			}
			if (currentDay != 0)
				s.append("\n");
			s.append("\n");

			month++;
			if (month > Calendar.months.length) {
				year++;
				month = 1;
			}
		}
		return s.toString();
	}
}

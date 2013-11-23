package monsters;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.View;

import monsters.StatisticsBlock.Field;
import party.DetailedMonster;

@SuppressWarnings("serial")
class StatsBlockPanel extends JPanel {
	private DetailedMonster creature;
	private JLabel[] labels;
	private JLabel[] fields;
	private int selected = -1;
	private Color selectedBackground = Color.BLUE;
	private Color selectedForeground = Color.WHITE;
	private Color oldBackground, oldForeground;

	private EventListenerList listenerList = new EventListenerList();

	StatsBlockPanel(DetailedMonster m) {
		setLayout(new GridBagLayout());

		int i = 0;
		boolean stripe = false;
		labels = new JLabel[Field.getStandardOrder().length + 2];
		labels[i++] = createLabel("Name:", false);
		labels[i++] = createLabel("Creature:", true);
		for (Field p : Field.getStandardOrder()) {
			labels[i++] = createLabel(p.toString(), stripe);
			stripe = !stripe;
		}

		stripe = false;
		fields = new JLabel[labels.length];
		for (i = 0; i < labels.length; i++) {
			fields[i] = createLabel("", stripe);
			stripe = !stripe;
		}

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		for (JLabel l : labels) {
			add(l, c);
		}

		c.gridx = 1;
		c.weightx = 1.0d;
		for (JLabel l : fields) {
			add(l, c);
		}

		setCreature(m);
		setSelected(0);
	}

	void setCreature(DetailedMonster m) {
		if (creature != null) {
			creature.removePropertyChangeListener(listener);
		}

		creature = m;
		creature.addPropertyChangeListener(listener);

		updateFields();

		if (selected != -1) setSelected(selected);	// set the selected field again
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			// TODO should just update the altered fields
			updateFields();
		}
	};

	private void updateFields() {
		if (creature == null) {
			for (int i = 0; i < fields.length; i++) {
				fields[i].setText("");
				fields[i].setPreferredSize(labels[i].getPreferredSize());
				fields[i].setMinimumSize(labels[i].getMinimumSize());
			}
			return;
		}

		int i = 0;
		fields[i++].setText(creature.getName());
		fields[i++].setText(creature.stats.getName());
		for (Field p : Field.getStandardOrder()) {
			fields[i].setText("<html><body>" + creature.getField(p) + "</body></html>");

			// determine the required size to fit the wrapped html text
			View view = (View) fields[i].getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
			view.setSize(200, 0);
			float h = view.getPreferredSpan(View.Y_AXIS);
			Dimension d = new Dimension(200, (int) Math.ceil(h));

			fields[i].setPreferredSize(d);
			fields[i].setMinimumSize(d);

			i++;
		}
	}

	void setSelectionForeground(Color c) {
		selectedForeground = c;
		if (selected != -1) {
			labels[selected].setForeground(c);
			fields[selected].setForeground(c);
		}
	}

	void setSelectionBackground(Color c) {
		selectedBackground = c;
		if (selected != -1) {
			labels[selected].setBackground(c);
			fields[selected].setBackground(c);
		}
	}

	private JLabel createLabel(String text, boolean striped) {
		JLabel label = new JLabel(text);
		label.addMouseListener(selectListener);
		label.setOpaque(true);
		if (striped) {
			label.setBackground(label.getBackground().brighter());
		}
		return label;
	}

	private void setSelected(int row) {
		if (row < 0 || row >= labels.length) throw new IllegalArgumentException(Integer.toString(row));

		int old = selected;
		if (old != -1) {
			labels[old].setForeground(oldForeground);
			labels[old].setBackground(oldBackground);
			fields[old].setForeground(oldForeground);
			fields[old].setBackground(oldBackground);
		}

		selected = row;
		oldForeground = labels[selected].getForeground();
		oldBackground = labels[selected].getBackground();
		labels[selected].setForeground(selectedForeground);
		labels[selected].setBackground(selectedBackground);
		fields[selected].setForeground(selectedForeground);
		fields[selected].setBackground(selectedBackground);

		fireListSelectionEvent(old, selected);
	}

	private MouseListener selectListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			for (int i = 0; i < labels.length; i++) {
				if (labels[i] == e.getSource() || fields[i] == e.getSource()) {
					setSelected(i);
				}
			}
		}
	};

	public void addListSelectionListener(ListSelectionListener listSelectionListener) {
		listenerList.add(ListSelectionListener.class, listSelectionListener);
	}

	public void removeListSelectionListener(ListSelectionListener listSelectionListener) {
		listenerList.remove(ListSelectionListener.class, listSelectionListener);
	}

	private void fireListSelectionEvent(int oldRow, int newRow) {
		Object[] listeners = listenerList.getListenerList();
		if (oldRow == -1) oldRow = newRow;
		if (newRow == -1) newRow = oldRow;
		if (oldRow > newRow) {
			int temp = oldRow;
			oldRow = newRow;
			newRow = temp;
		}
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListSelectionListener.class) {
				ListSelectionEvent event = new ListSelectionEvent(this, oldRow, newRow, false);
				((ListSelectionListener) listeners[i + 1]).valueChanged(event);
			}
		}
	}

	public Field getSelectedField() {
		if (selected == -1) return null;
		if (selected < 2) return Field.NAME;
		return Field.getStandardOrder()[selected - 2];
	}
}

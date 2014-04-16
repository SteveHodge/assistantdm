package monsters;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import monsters.StatisticsBlock.Field;

@SuppressWarnings("serial")
public class DefaultDetailPanel extends DetailPanel {
	private Monster creature;	// TODO remove eventually - should be able to just use the view
	private StatsBlockCreatureView view;
	private Field field;

	private JTextArea textArea;
	private Color defaultBG;

	DefaultDetailPanel(Field f) {
		field = f;
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		textArea = new JTextArea(5, 40);
		defaultBG = textArea.getBackground();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(textArea);
		c.gridx = 0;
		add(scrollPane, c);

		JButton apply = new JButton("Apply");
		apply.addActionListener(evt -> {
			try {
				view.setField(field, textArea.getText());
				textArea.setBackground(defaultBG);
			} catch (Exception e) {
				textArea.setBackground(Color.RED);
			}
		});
		add(apply, c);
	}

	@Override
	void setMonster(Monster m) {
		if (creature == m) return;

		if (creature != null) {
			view.removePropertyChangeListener(listener);
		}

		creature = m;

		if (creature != null) {
			view = StatsBlockCreatureView.getView(creature);
			view.addPropertyChangeListener(listener);
			// add listener
		}

		update();
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(field.name())) update();
		}
	};

	private void update() {
		if (creature != null) {
			String val = view.getField(field);
			if (val.startsWith("<b>")) val = val.substring(3, val.length() - 4);	// TODO remove when no longer required
			textArea.setText(val);
		} else {
			textArea.setText("");
		}
	}
}

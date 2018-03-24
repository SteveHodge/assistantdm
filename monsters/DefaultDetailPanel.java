package monsters;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import monsters.StatisticsBlock.Field;

@SuppressWarnings("serial")
public class DefaultDetailPanel extends DetailPanel {
	private Monster creature;	// TODO remove eventually - should be able to just use the view
	private StatsBlockCreatureView view;
	private Field field;

	private JTextArea textArea;
	private JLabel sourceLabel;
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

		JButton apply = new JButton("Apply");
		apply.addActionListener(evt -> {
			try {
				view.setField(field, textArea.getText());
				textArea.setBackground(defaultBG);
			} catch (Exception e) {
				textArea.setBackground(Color.RED);
			}
		});

		sourceLabel = new JLabel("");
		sourceLabel.setMaximumSize(new Dimension(200, 100));
		sourceLabel.setPreferredSize(new Dimension(200, 40));

		c.insets = new Insets(2, 4, 2, 4);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		add(new JLabel("Source:"), c);

		c.gridx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		add(sourceLabel, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		c.weighty = 0;
		add(new JLabel("Edit: "), c);

		c.gridy++;
		c.weighty = 1.0;
		add(scrollPane, c);

		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.NORTHEAST;
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
			StatisticsBlock blk = creature.statisticsBlock;
			if (blk != null) sourceLabel.setText("<html>" + blk.get(field) + "</html>");		// XXX we're assuming this never changes

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

package ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import party.Character;
import party.Creature;
import party.XP;

// TODO better history dialog
// FIXME: changing the level directly doesn't apply the comments or date
@SuppressWarnings("serial")
public class CharacterXPPanel extends JPanel implements PropertyChangeListener, ActionListener {
	protected Character character;
	JLabel xpLabel;
	JLabel percentage;
	JButton adhocButton, historyButton, levelButton;
	JTextField commentsField;
	JFormattedTextField adhocField;
	JFormattedTextField dateField;

	public CharacterXPPanel(Character chr) {
		character = chr;

		xpLabel = new JLabel(String.format("%,d / %,d", character.getXP(), character.getRequiredXP()));
		percentage = new JLabel(String.format("%.2f%%",	((float)character.getXP()-XP.getXPRequired(character.getLevel()))/(character.getLevel()*10)));
		historyButton = new JButton("History");
		historyButton.addActionListener(this);
		levelButton = new JButton("Level Up");
		levelButton.addActionListener(this);
		levelButton.setEnabled(character.getXP() >= character.getRequiredXP());
		adhocButton = new JButton("Apply");
		adhocButton.addActionListener(this);
		commentsField = new JTextField(70);
		adhocField = new JFormattedTextField();
		adhocField.setValue(new Integer(0));
		dateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
		dateField.setValue(new Date());
		dateField.setToolTipText("Expects date as 'yyyy-mm-dd'");

		setBorder(new TitledBorder("Levels and Experience"));
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 3, 1, 3);
		c.gridx = 0; c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		add(new JLabel("Level: "),c);

		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		BoundIntegerField f = new BoundIntegerField(character, Creature.PROPERTY_LEVEL, 2);
		f.setMinimumSize(new Dimension(40,f.getMinimumSize().height));
		add(f,c);

		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		add(new JLabel("XP:"),c);

		c.gridx = 3;
		c.weightx = 1.0;
		add(xpLabel,c);

		c.gridx = 4;
		c.weightx = 0.0;
		add(percentage,c);

		c.gridx = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_END;
		add(historyButton,c);

		c.gridx = 0; c.gridy = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		add(new JLabel("Adhoc Change:"),c);

		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		add(adhocField,c);

		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		add(new JLabel("Date:"),c);

		c.gridx = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = 1;
		add(dateField,c);

		c.gridx = 4;
		c.weightx = 0.0;
		add(levelButton,c);

		c.gridx = 5;
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 1;
		c.weightx = 0.0;
		add(adhocButton,c);

		c.gridx = 0; c.gridy = 2;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		add(new JLabel("Comment: "),c);

		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 5;
		c.weightx = 1.0;
		add(commentsField,c);

		// update fields when character changes
		character.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Creature.PROPERTY_LEVEL)
				|| e.getPropertyName().equals(Creature.PROPERTY_XP)) {
			xpLabel.setText(String.format("%,d / %,d", character.getXP(), character.getRequiredXP()));
			percentage.setText(String.format("%.2f%%",	((float)character.getXP()-XP.getXPRequired(character.getLevel()))/(character.getLevel()*10)));
			levelButton.setEnabled(character.getXP() >= character.getRequiredXP());
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == levelButton) {
			character.setLevel(character.getLevel()+1,commentsField.getText(),(Date)dateField.getValue());
			commentsField.setText(null);

		} else if (e.getSource() == adhocButton) {
			int xp = (Integer)adhocField.getValue();
			character.addXPAdhocChange(xp,commentsField.getText(),(Date)dateField.getValue());
			commentsField.setText(null);

		} else if (e.getSource() == historyButton) {
			//JTextArea area = new JTextArea(character.getXPHistory());
			//JScrollPane scroll = new JScrollPane(area);
			
			XPHistoryPanel panel = new XPHistoryPanel(character);
			JFrame popup = new JFrame(character.getName()+" XP History");
			popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			//popup.getContentPane().add(scroll);
			popup.getContentPane().add(panel);
			popup.pack();
			popup.setLocationRelativeTo(this);
			popup.setVisible(true);
		}
	}
}

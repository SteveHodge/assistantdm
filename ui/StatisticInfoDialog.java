package ui;

import gamesystem.Buff;
import gamesystem.BuffFactory;
import gamesystem.Statistic;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import party.Character;

// TODO should probably convert to factory class
@SuppressWarnings("serial")
public class StatisticInfoDialog extends JDialog {
	static final String[] types = { "", "Alchemical", "Armor", "Circumstance", "Competence", "Deflection",
		"Enhancement", "Inherent", "Insight", "Luck", "Morale", "Natural Armor", "Profane", "Racial",
		"Resistance", "Sacred", "Shield", "Size"
	};

	Character character;
	Statistic statistic;

	JLabel summary;
	JPanel addPanel;
	JButton okButton;

	// intended for use by subclasses to create the dialog
	StatisticInfoDialog(JComponent parent, String title) {
		super(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
	}

	StatisticInfoDialog(JComponent parent, String title, Character chr, final String statName) {
		super(SwingUtilities.getWindowAncestor(parent), title);

		initialize(chr, statName);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(summary, c);

		c.gridy++;
		c.weighty = 0;
		add(addPanel, c);

		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(2, 4, 2, 4);
		add(okButton, c);

		pack();
		setLocationRelativeTo(SwingUtilities.getWindowAncestor(parent));
	}

	void initialize(Character chr, final String statName) {
		character = chr;
		statistic = chr.getStatistic(statName);

		statistic.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				updateSummary();
			}
		});

		summary = new JLabel();
		updateSummary();
		summary.setBorder(BorderFactory.createTitledBorder("Summary"));
		summary.setVerticalAlignment(SwingConstants.TOP);

		okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});

		addPanel = getAdhocPanel(statName);
		addPanel.setBorder(BorderFactory.createTitledBorder("Adhoc Modifier"));
	}

	JPanel getAdhocPanel(final String statName) {
		final JComboBox typeBox = new JComboBox(types);
		typeBox.setSelectedItem("Enhancement");
		typeBox.setEditable(true);

		final JTextField nameField = new JTextField();

		final JFormattedTextField modField = new JFormattedTextField();
		modField.setValue(new Integer(0));
		modField.setColumns(3);

		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BuffFactory bf = new BuffFactory(nameField.getText());
				int mod = (Integer) modField.getValue();
				bf.addEffect(statName, typeBox.getSelectedItem().toString(), mod);
				Buff buff = bf.getBuff();
				buff.applyBuff(character);
				character.buffs.addElement(buff);
			}
		});

		JPanel addPanel = new JPanel();
		addPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		addPanel.add(new JLabel("Source: "), c);

		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 3;
		addPanel.add(nameField, c);

		c.gridy++;
		c.gridx = 0;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridwidth = 2;
		addPanel.add(typeBox, c);

		c.gridx += 2;
		c.weightx = 0.25;
		c.gridwidth = 1;
		addPanel.add(modField, c);

		c.gridx++;
		addPanel.add(addButton, c);
		return addPanel;
	}

	void updateSummary() {
		summary.setText("<html><body>" + statistic.getSummary() + "</body></html>");
		pack();
	}
}

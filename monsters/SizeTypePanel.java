package monsters;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gamesystem.MonsterType;
import gamesystem.SizeCategory;
import gamesystem.core.PropertyListener;
import monsters.EncounterDialog.MonsterData;

// TODO need to be able to set the dex limit for armor
// TODO add the ability to choose armor and shield items
@SuppressWarnings("serial")
class SizeTypePanel extends DetailPanel {
	private Monster monster;

	JComboBox<SizeCategory> sizeCombo;
	JComboBox<MonsterType> typeCombo;
	JTextField subtypeField;

	public SizeTypePanel() {
		setLayout(new GridBagLayout());

		sizeCombo = new JComboBox<>(SizeCategory.values());
		sizeCombo.addActionListener(e -> {
			monster.size.setBaseSize((SizeCategory) sizeCombo.getSelectedItem());
		});
		typeCombo = new JComboBox<>(MonsterType.values());
		typeCombo.addActionListener(e -> {
			monster.race.setType((MonsterType) typeCombo.getSelectedItem());
		});
		subtypeField = new JTextField(50);

		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> {
			String txt = subtypeField.getText();
			String[] subtypes = txt.split("\\s*,\\s*");
			Set<String> newSubtypes = new HashSet<>();
			for (String s : subtypes) {
				newSubtypes.add(s);
				if (!monster.race.hasSubtype(s))
					monster.race.addSubtype(s);
			}
			Set<String> toRemove = new HashSet<>();
			for (String s : monster.race.subtypes) {
				if (!newSubtypes.contains(s))
					toRemove.add(s);
			}
			for (String s : toRemove) {
				monster.race.removeSubtype(s);
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(1, 3, 1, 3);

		add(new JLabel("Size:"), c);
		add(sizeCombo, c);
		c.gridy++;
		add(new JLabel("Type:"), c);
		add(typeCombo, c);
		c.gridy++;
		add(new JLabel("Subtypes:"), c);
		c.weightx = 1.0f;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(subtypeField, c);
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0f;
		add(apply, c);
		c.gridy++;
		c.gridwidth = 2;
		c.weightx = 1.0f;
		c.weighty = 1.0f;
		c.fill = GridBagConstraints.BOTH;
		add(new JPanel(), c);
	}

	@Override
	void setMonster(Monster m, MonsterData d) {
		if (monster == m) return;

		if (monster != null) {
			monster.size.removePropertyListener(listener);
			monster.race.removePropertyListener(listener);
		}

		monster = m;

		if (monster != null) {
			monster.size.addPropertyListener(listener);
			monster.race.addPropertyListener(listener);
		}

		update();
	}

	private void update() {
		if (monster == null) return;
		sizeCombo.setSelectedItem(monster.size.getBaseSize());
		typeCombo.setSelectedItem(monster.race.getType());
		String subtypes = String.join(", ", monster.race.subtypes);
		if (!subtypes.equals(subtypeField.getText()))
			subtypeField.setText(subtypes);
	}

	final private PropertyListener listener = e -> update();
}
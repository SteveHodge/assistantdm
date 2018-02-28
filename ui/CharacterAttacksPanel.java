package ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.BuffFactory;
import gamesystem.Creature;
import gamesystem.Feat;
import gamesystem.Modifier;
import gamesystem.core.OverridableProperty;
import gamesystem.core.PropertyListener;
import gamesystem.core.Property;
import party.Character;
import party.CharacterAttackForm;
import swing.NullableIntegerFieldFactory;

// TODO make power attack and combat expertise filtered/numeric fields or combos or cycles
@SuppressWarnings("serial")
class CharacterAttacksPanel extends CharacterSubPanel implements PropertyListener<Integer> {
	private JFormattedTextField BAB;
	private JLabel babLabel = new JLabel();
	private JLabel strLabel = new JLabel();
	private JLabel dexLabel = new JLabel();
	private JLabel meleeLabel = new JLabel();
	private JLabel rangedLabel = new JLabel();
	private JLabel powerAttackLabel = new JLabel("Power Attack: ");
	private JLabel combatExpertiseLabel = new JLabel("Combat Expertise: ");
	private JTextField powerAttack = new JTextField(4);
	private JTextField combatExpertise = new JTextField(4);
	private JCheckBox fightingDefensively = new JCheckBox("Fighting Defensively");
	private JCheckBox totalDefense = new JCheckBox("Total Defense");
	private Attacks attacks;
	private AttackFormPanel attackPanel;
	private JList<CharacterAttackForm> weaponList;
	private AttackFormListModel attackFormsModel = new AttackFormListModel();

	CharacterAttacksPanel(Character chr) {
		super(chr);
		attacks = chr.getAttacksStatistic();
		summary = getSummary();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(getBaseValuesPanel());
		add(getAttackOptionsPanel());
		add(getWeaponPanel());

		updateToolTip();
		// update labels when character changes
		attacks.addPropertyListener(this);

		addMouseListener(rightClickListener);

		powerAttack.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updatePowerAttack();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updatePowerAttack();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updatePowerAttack();
			}
		});

		combatExpertise.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateCombatExpertise();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateCombatExpertise();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateCombatExpertise();
			}

		});

		fightingDefensively.addActionListener(e -> {
			attacks.setFightingDefensively(fightingDefensively.isSelected());
			if (fightingDefensively.isSelected()) {
				totalDefense.setSelected(false);
				attacks.setTotalDefense(false);
			}
		});

		totalDefense.addActionListener(e -> {
			attacks.setTotalDefense(totalDefense.isSelected());
			if (totalDefense.isSelected()) {
				fightingDefensively.setSelected(false);
				attacks.setFightingDefensively(false);
				combatExpertise.setText("0");
				attacks.setCombatExpertise(0);
				powerAttack.setText("0");
				attacks.setPowerAttack(0);
			}
		});
	}

	private MouseListener rightClickListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (!SwingUtilities.isRightMouseButton(e)) return;
			AttacksInfoDialog dialog = new AttacksInfoDialog();
			dialog.setVisible(true);
		}
	};

	private class AttacksInfoDialog extends StatisticInfoDialog {
		private JLabel rangedSummary;
		private JLabel damageSummary;

		AttacksInfoDialog() {
			super(CharacterAttacksPanel.this, "Attacks");

			rangedSummary = new JLabel();
			rangedSummary.setBorder(BorderFactory.createTitledBorder("Ranged"));
			rangedSummary.setVerticalAlignment(SwingConstants.TOP);
			damageSummary = new JLabel();
			damageSummary.setBorder(BorderFactory.createTitledBorder("Damage"));
			damageSummary.setVerticalAlignment(SwingConstants.TOP);

			initialize(CharacterAttacksPanel.this.character, Creature.STATISTIC_ATTACKS);			// will also call updateSummary

			summary.setBorder(BorderFactory.createTitledBorder("Melee"));

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 0.5;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			add(summary, c);

			c.gridx++;
			add(rangedSummary, c);

			c.gridx++;
			add(damageSummary, c);

			c.gridy++;
			c.gridx = 0;
			c.weighty = 0;
			c.gridwidth = 3;
			c.weightx = 1;
			add(addPanel, c);

			c.gridy++;
			c.fill = GridBagConstraints.NONE;
			c.insets = new Insets(2, 4, 2, 4);
			add(okButton, c);

			pack();
			setLocationRelativeTo(SwingUtilities.getWindowAncestor(CharacterAttacksPanel.this));
		}

		@Override
		void updateSummary() {
			summary.setText("<html><body>" + attacks.getSummary() + "</body></html>");
			rangedSummary.setText("<html><body>" + attacks.getRangedSummary() + "</body></html>");
			damageSummary.setText("<html><body>" + attacks.getDamageStatistic().getSummary() + "</body></html>");
			pack();
		}

		@Override
		JPanel getAdhocPanel(final String statName) {
			final JComboBox<Modifier.StandardType> typeBox = new JComboBox<>(Modifier.StandardType.values());
			typeBox.setSelectedItem("Enhancement");
			typeBox.setEditable(true);

			final JTextField nameField = new JTextField();

			final JFormattedTextField modField = new JFormattedTextField();
			modField.setValue(new Integer(0));
			modField.setColumns(3);

			final JCheckBox attackMod = new JCheckBox("attack");
			attackMod.setSelected(true);
			final JCheckBox damageMod = new JCheckBox("damage");
			damageMod.setSelected(true);

			JButton addButton = new JButton("Add");
			addButton.addActionListener(e -> {
				BuffFactory bf = new BuffFactory(nameField.getText());
				int mod = (Integer) modField.getValue();
				if (attackMod.isSelected()) bf.addEffect(Creature.STATISTIC_ATTACKS, typeBox.getSelectedItem().toString(), mod);
				if (damageMod.isSelected()) bf.addEffect(Creature.STATISTIC_DAMAGE, typeBox.getSelectedItem().toString(), mod);
				creature.addBuff(bf.getBuff());
			});

			JPanel addPanel = new JPanel();
			addPanel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 1;
			c.weightx = 0;
			c.weighty = 0;
			c.fill = GridBagConstraints.NONE;
			addPanel.add(new JLabel("Applies to: "), c);

			c.gridx++;
			addPanel.add(attackMod, c);

			c.gridx++;
			addPanel.add(damageMod, c);

			c.gridx++;
			addPanel.add(addButton, c);

			c.gridx = 0;
			c.gridy = 0;
			addPanel.add(new JLabel("Source: "), c);

			c.gridx++;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			addPanel.add(nameField, c);

			c.gridx++;
			c.weightx = 0.5;
			addPanel.add(typeBox, c);

			c.gridx++;
			c.weightx = 0.25;
			addPanel.add(modField, c);
			return addPanel;
		}
	}

	private void updatePowerAttack() {
		int value = 0;
		try {
			value = Integer.parseInt(powerAttack.getText());
		} catch (NumberFormatException e) {
			// TODO pop up message?
		}
		if (value < 0) value = 0;
		if (value > character.getBAB().getValue()) value = character.getBAB().getValue();
		if (value > 0) {
			totalDefense.setSelected(false);
			attacks.setTotalDefense(false);
		}
		attacks.setPowerAttack(value);
	}

	private void updateCombatExpertise() {
		int value = 0;
		try {
			value = Integer.parseInt(combatExpertise.getText());
		} catch (NumberFormatException e) {
			// TODO pop up message?
		}
		if (value < 0) value = 0;
		if (value > 5) value = 5;
		if (value > 0) {
			totalDefense.setSelected(false);
			attacks.setTotalDefense(false);
		}
		attacks.setCombatExpertise(value);
	}

	private JPanel getBaseValuesPanel() {
		JPanel top = new JPanel();
		top.setLayout(new GridBagLayout());

		BAB = NullableIntegerFieldFactory.createNullableIntegerField();
		BAB.setColumns(3);
		OverridableProperty<Integer> babProp = character.getBAB();
		if (babProp.hasOverride()) BAB.setValue(babProp.getValue());
		BAB.addPropertyChangeListener((e) -> {
			if (BAB.getValue() == null || "".equals(BAB.getText())) {
				character.clearBABOverride();
			} else {
				int total = (Integer) BAB.getValue();
				if (total == babProp.getRegularValue()) {
					character.clearBABOverride();
					BAB.setText("");
				} else {
					character.setBABOverride(total);
				}
			}
		});

		updateLabels();

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridy = 0;
		c.gridheight = 2;
		top.add(new JLabel("BAB:"),c);

		top.add(babLabel, c);
		top.add(new JLabel("override:"), c);
		top.add(BAB,c);

		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_END;
		top.add(new JLabel("Str Mod: "),c);

		top.add(strLabel,c);
		top.add(new JLabel("Melee Attack: "),c);
		top.add(meleeLabel,c);

		c.gridx = 4;
		c.gridy = 1;
		top.add(new JLabel("Dex Mod: "),c);

		c.gridx = GridBagConstraints.RELATIVE;
		top.add(dexLabel,c);
		top.add(new JLabel("Ranged Attack: "),c);
		top.add(rangedLabel,c);

		return top;
	}

	private JPanel getAttackOptionsPanel() {
		JPanel panel = new JPanel();

		panel.add(fightingDefensively);
		fightingDefensively.setSelected(attacks.isFightingDefensively());

		panel.add(totalDefense);
		totalDefense.setSelected(attacks.isTotalDefense());

		panel.add(powerAttackLabel);
		panel.add(powerAttack);
		powerAttack.setText(""+attacks.getPowerAttack());

		panel.add(combatExpertiseLabel);
		panel.add(combatExpertise);
		combatExpertise.setText(""+attacks.getCombatExpertise());

		updateOptions();
		return panel;
	}

	private void updateOptions() {
		if (character.hasFeat(Feat.FEAT_POWER_ATTACK)) {
			powerAttackLabel.setVisible(true);
			powerAttack.setVisible(true);
		} else {
			powerAttackLabel.setVisible(false);
			powerAttack.setVisible(false);
		}

		if (character.hasFeat(Feat.FEAT_COMBAT_EXPERTISE)) {
			combatExpertiseLabel.setVisible(true);
			combatExpertise.setVisible(true);
		} else {
			combatExpertiseLabel.setVisible(false);
			combatExpertise.setVisible(false);
		}
	}

	private JPanel getWeaponPanel() {
		JPanel bottom = new JPanel();
		bottom.setLayout(new GridBagLayout());
		bottom.setBorder(BorderFactory.createTitledBorder("Weapons"));

		JButton newButton = new JButton("New");
		newButton.addActionListener(e -> {
			CharacterAttackForm a = character.addAttackForm(attacks.addAttackForm("new weapon"));
			attackFormsModel.addElement(a);
			weaponList.setSelectedValue(a, true);
		});

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(e -> {
			CharacterAttackForm a = weaponList.getSelectedValue();
			if (a != null) {
				attackFormsModel.removeElement(a);
				character.removeAttackForm(a);
				attackPanel.setAttackForm(weaponList.getSelectedValue());
			}
		});

		JButton upButton = new JButton("/\\");
		upButton.addActionListener(e -> {
			int i = weaponList.getSelectedIndex();
			if (i >= 1) {
				attackFormsModel.move(i, i - 1);
				weaponList.setSelectedIndex(i-1);
			}
		});

		JButton downButton = new JButton("\\/");
		downButton.addActionListener(e -> {
			int i = weaponList.getSelectedIndex();
			if (i != -1 && i < attackFormsModel.getSize() - 1) {
				attackFormsModel.move(i, i + 1);
				weaponList.setSelectedIndex(i+1);
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(newButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(upButton);
		buttonPanel.add(downButton);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 0; c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTH;
		bottom.add(buttonPanel, c);

		weaponList = new JList<>(attackFormsModel);
		weaponList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		weaponList.setVisibleRowCount(6);
		weaponList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				attackPanel.setAttackForm(weaponList.getSelectedValue());
			}
		});
		JScrollPane scroller = new JScrollPane(weaponList);
		//scroller.setPreferredSize(preferredSize);

		c.gridx = 0; c.gridy = 1;
		c.weightx = 0.5; c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.LINE_START;
		bottom.add(scroller, c);


		attackPanel = new AttackFormPanel();

		c.gridx = 1; c.gridy = 0;
		c.gridheight = 2;
		c.weightx = 0.5; c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		bottom.add(attackPanel, c);

		return bottom;
	}

	private void updateLabels() {
		babLabel.setText(Integer.toString(character.getBAB().getRegularValue()));
		strLabel.setText("" + character.getAbilityStatistic(AbilityScore.Type.STRENGTH).getModifierValue());
		String melee = attacks.getAttacksDescription(attacks.getValue())+(attacks.hasConditionalModifier()?"*":"");
		if (attacks.isTotalDefense()) {
			melee = "<html><body><s>"+melee+"</s></body></html>";
		}
		meleeLabel.setText(melee);
		dexLabel.setText("" + character.getAbilityStatistic(AbilityScore.Type.DEXTERITY).getModifierValue());
		String ranged = attacks.getAttacksDescription(attacks.getRangedValue())+(attacks.hasConditionalModifier()?"*":"");
		if (attacks.isTotalDefense()) {
			ranged = "<html><body><s>"+ranged+"</s></body></html>";
		}
		rangedLabel.setText(ranged);
	}

	private void updateToolTip() {
		StringBuilder text = new StringBuilder();
		text.append("<html><body>");
		text.append(attacks.getSummary());
		text.append("</body></html>");
		meleeLabel.setToolTipText(text.toString());

		text = new StringBuilder();
		text.append("<html><body>");
		text.append(attacks.getRangedSummary());
		text.append("</body></html>");
		rangedLabel.setToolTipText(text.toString());
	}

	private String getSummary() {
		StringBuilder s = new StringBuilder();
		s.append("Melee ");
		s.append(attacks.getAttacksDescription(attacks.getValue()));
		s.append("   Ranged ");
		s.append(attacks.getAttacksDescription(attacks.getRangedValue()));
		return s.toString();
	}

	// we rely on the attack to tell us about feat and ability changes indirectly
	// TODO maybe better to directly listen?
	@Override
	public void propertyChanged(Property<Integer> source, Integer old) {
		updateOptions();
		updateLabels();
		updateToolTip();
		updateSummaries(getSummary());
	}

	// ------------ Attack forms / weapons list related ------------
	protected class AttackFormListModel extends AbstractListModel<CharacterAttackForm> implements PropertyChangeListener {
		public AttackFormListModel() {
			for (CharacterAttackForm a : character.attackForms) {
				a.addPropertyChangeListener(this);
			}
		}

		public CharacterAttackForm get(int i) {
			return character.attackForms.get(i);
		}

		@Override
		public CharacterAttackForm getElementAt(int i) {
			return get(i);
		}

		@Override
		public int getSize() {
			return character.attackForms.size();
		}

		public void addElement(final CharacterAttackForm a) {
			a.addPropertyChangeListener(this);
			fireIntervalAdded(this, character.attackForms.size() - 1, character.attackForms.size() - 1);
		}

		public void removeElement(CharacterAttackForm a) {
			int i = character.attackForms.indexOf(a);
			if (i > -1) {
				a.removePropertyChangeListener(this);
				fireIntervalRemoved(this, i, i);
			}
		}

		public void move(int fromIndex, int toIndex) {
			if (fromIndex < 0 || fromIndex > attackFormsModel.getSize() - 1
					|| toIndex < 0 || toIndex > attackFormsModel.getSize() - 1) {
				throw new IndexOutOfBoundsException();	// TODO message
			}

			CharacterAttackForm a = character.attackForms.remove(fromIndex);
			fireIntervalRemoved(this, fromIndex, fromIndex);
			character.attackForms.add(toIndex, a);
			fireIntervalAdded(this, toIndex, toIndex);
		}

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			int i = character.attackForms.indexOf(e.getSource());
			if (i > -1) fireContentsChanged(AttackFormListModel.this, i, i);
			else throw new IllegalArgumentException("Unrecognised source for change event: " + e.getSource());
		}
	}
}

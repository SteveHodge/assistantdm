package ui;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gamesystem.BuffFactory;
import gamesystem.Creature;
import gamesystem.ItemDefinition;
import gamesystem.Modifier.StandardType;
import gamesystem.SizeCategory;
import gamesystem.dice.CombinedDice;
import party.CharacterAttackForm;
import party.CharacterAttackForm.WeaponFocus;
import party.CharacterAttackForm.WeaponSpecialization;

// TODO only attack and damage labels are updated on changes from the attack, other changes will be ignored (needs fixing if something other than this panel makes changes to the attack)

@SuppressWarnings("serial")
class AttackFormPanel extends JPanel implements PropertyChangeListener {
	//Attacks attacks;
	private CharacterAttackForm attack;

	private JTextField nameField = new JTextField(20);
	private JTextField attackBonusField = new JTextField(20);
	private JTextField damageField = new JTextField(20);
	private JTextField criticalField = new JTextField(20);
	private JTextField rangeField = new JTextField(20);
	private JTextField weightField = new JTextField(20);
	private JTextField typeField = new JTextField(20);
	private JComboBox<SizeCategory> sizeCombo = new JComboBox<>(SizeCategory.values());
	private JTextField propertiesField = new JTextField(20);
	private JTextField ammunitionField = new JTextField(20);
	private JComboBox<CharacterAttackForm.Kind> kindCombo;
	private JComboBox<CharacterAttackForm.Usage> usageCombo;
	private JCheckBox proficientCheck = new JCheckBox();
	private JComboBox<WeaponFocus> focusOverride;
	private JComboBox<WeaponSpecialization> specializationOverride;
	private JLabel itemLabel = new JLabel();
	private JButton itemButton = new JButton("...");

	private JLabel totalAttackLabel = new JLabel();
	private JLabel totalDamageLabel = new JLabel();

	private boolean updateAttack = true;	// set to false when we don't want changes to the text fields to be applied to the attack (such as when changing the attack)

	AttackFormPanel() {
		this(null);
	}

	private AttackFormPanel(CharacterAttackForm atk) {
		super(new GridBagLayout());

		kindCombo = new JComboBox<>(CharacterAttackForm.Kind.values());
		usageCombo = new JComboBox<>(CharacterAttackForm.Usage.values());
		focusOverride = new JComboBox<>(WeaponFocus.values());
		specializationOverride = new JComboBox<>(WeaponSpecialization.values());
		itemButton.setMargin(new Insets(2, 4, 2, 2));
		itemButton.addActionListener(e -> openWeaponChooser());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1,2,1,2);
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = 0;
		c.weightx = 0.5;
		add(new JLabel("Name:"), c);
		add(new JLabel("Base Item:"), c);
		add(new JLabel("Attack Bonus:"),c);
		add(new JLabel("Damage:"),c);
		add(new JLabel("Critical:"),c);
		add(new JLabel("Kind:"),c);
		add(new JLabel("Usage:"),c);
		add(new JLabel("Damage Type:"),c);
		add(new JLabel("Size:"),c);
		add(new JLabel("Range (ft):"),c);
		add(new JLabel("Weight (lbs):"),c);
		add(new JLabel("Properties:"),c);
		add(new JLabel("Ammunition:"),c);
		add(new JLabel("Proficient:"),c);
		add(new JLabel("Weapon Focus:"), c);
		add(new JLabel("Specialization:"), c);

		c.gridx = 1; c.gridy = 0;
		c.weightx = 1.0;
		c.gridwidth = 2;
		add(nameField,c);
		c.gridy++;	// add item field later
		c.gridy++;	// add attackBonus field later
		c.gridy++;	// add damage field later
		c.gridy++; add(criticalField,c);
		c.gridy++; add(kindCombo,c);
		c.gridy++; add(usageCombo,c);
		c.gridy++; add(typeField,c);
		c.gridy++; add(sizeCombo,c);
		c.gridy++; add(rangeField,c);
		c.gridy++; add(weightField,c);
		c.gridy++; add(propertiesField,c);
		c.gridy++; add(ammunitionField,c);
		c.gridy++;
		add(proficientCheck, c);
		c.gridy++;
		add(focusOverride, c);
		c.gridy++;
		add(specializationOverride, c);

		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = 1;
		add(itemLabel, c);
		c.gridy++;
		add(attackBonusField, c);
		c.gridy++;
		add(damageField, c);

		c.gridy = 1;
		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		add(itemButton, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy++;
		add(totalAttackLabel, c);
		c.gridy++;
		add(totalDamageLabel, c);

		setAttackForm(atk);

		addMouseListener(rightClickListener);
		totalAttackLabel.addMouseListener(rightClickListener);
		totalDamageLabel.addMouseListener(rightClickListener);

		// the damage field uses an input verifier and applies changes on enter or losing focus
		damageField.setInputVerifier(damageVerifier);
		damageField.addActionListener(e -> damageVerifier.shouldYieldFocus(damageField));

		// the other fields do dynamic updating
		nameField.getDocument().addDocumentListener(docListener);
		attackBonusField.getDocument().addDocumentListener(docListener);
		criticalField.getDocument().addDocumentListener(docListener);
		rangeField.getDocument().addDocumentListener(docListener);
		weightField.getDocument().addDocumentListener(docListener);
		typeField.getDocument().addDocumentListener(docListener);
		propertiesField.getDocument().addDocumentListener(docListener);
		ammunitionField.getDocument().addDocumentListener(docListener);

		proficientCheck.addActionListener(e -> {
			attack.setProficient(proficientCheck.isSelected());
		});

		kindCombo.addActionListener(e -> {
			if (attack != null) {
				attack.setKind((CharacterAttackForm.Kind) kindCombo.getSelectedItem());
			}
		});
		sizeCombo.addActionListener(e -> {
			if (attack != null) {
				attack.attack.setSize((SizeCategory) sizeCombo.getSelectedItem());
			}
		});
		usageCombo.addActionListener(e -> {
			if (attack != null) {
				attack.setUsage((CharacterAttackForm.Usage) usageCombo.getSelectedItem());
			}
		});

		focusOverride.addActionListener(e -> {
			if (attack != null) {
				attack.setFocus((WeaponFocus) focusOverride.getSelectedItem());
			}
		});

		specializationOverride.addActionListener(e -> {
			if (attack != null) {
				attack.setSpecialization((WeaponSpecialization) specializationOverride.getSelectedItem());
			}
		});
	}

	void openWeaponChooser() {
		List<ItemDefinition> weapons = new ArrayList<>(ItemDefinition.getAttacks());
		weapons.sort((a, b) -> {
			if (!(a instanceof ItemDefinition || !(b instanceof ItemDefinition))) return 0;
			return a.getName().compareToIgnoreCase(b.getName());
		});
		Object[] options = new Object[weapons.size() + 1];
		options[0] = new String("none");
		for (int i = 1; i < options.length; i++) {
			options[i] = weapons.get(i - 1);
		}
		Object s = JOptionPane.showInputDialog(this, "Select armor:", "Select armor", JOptionPane.QUESTION_MESSAGE, null, options, null);
		if (s != null) {
			if (s instanceof ItemDefinition) {
				attack.item = (ItemDefinition) s;
				ItemDefinition.Attack atk = attack.item.getAttack(0);
				itemLabel.setText(attack.item.getName());
				nameField.setText(attack.item.getName());
				typeField.setText(atk.getDamageType());
				damageField.setText(atk.getDamage());
				attack.setBaseDamage(atk.getDamage());
				criticalField.setText(atk.getCritical());
				rangeField.setText(atk.getRange());
				weightField.setText(attack.item.getWeight());
				kindCombo.setSelectedItem(CharacterAttackForm.Kind.getKind(atk.getWeaponType()));
				proficientCheck.setSelected(true);
				focusOverride.setSelectedItem(WeaponFocus.DEFAULT);
				specializationOverride.setSelectedItem(WeaponFocus.DEFAULT);
			} else {
				attack.item = null;
				itemLabel.setText("");
				nameField.setText("");
				typeField.setText("");
				damageField.setText("");
				attack.setBaseDamage("");
				criticalField.setText("");
				rangeField.setText("");
				weightField.setText("");
				kindCombo.setSelectedIndex(-1);
				proficientCheck.setSelected(true);
				focusOverride.setSelectedItem(WeaponFocus.DEFAULT);
				specializationOverride.setSelectedItem(WeaponFocus.DEFAULT);
			}
		}
		update();
	}

	private MouseListener rightClickListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (!SwingUtilities.isRightMouseButton(e)) return;
			AttackFormInfoDialog dialog = new AttackFormInfoDialog();
			dialog.setVisible(true);
		}
	};

	private InputVerifier damageVerifier = new InputVerifier() {
		@Override
		public boolean shouldYieldFocus(JComponent c) {
			if (c != damageField) return true;

			if (verify(c)) {
				attack.setBaseDamage(damageField.getText());
				damageField.setText(attack.getBaseDamage());
				return true;
			} else {
				String message = "Invalid damage value.\nPlease try again.";
				JOptionPane.showMessageDialog(null,message,"Invalid Value",JOptionPane.WARNING_MESSAGE);
				return false;
			}
		}

		@Override
		public boolean verify(JComponent c) {
			if (c != damageField) return true;

			try {
				CombinedDice.parse(damageField.getText());
			} catch (Exception e) {
				return false;
			}
			return true;
		}
	};

	private DocumentListener docListener = new DocumentListener() {
		@Override
		public void changedUpdate(DocumentEvent e) {
			updateField(e);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateField(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateField(e);
		}

		protected void updateField(DocumentEvent e) {
			if (attack == null) return;	// shouldn't happen
			if (!updateAttack) return;	// updates disabled

			// TODO ugly. find a better way
			if (e.getDocument() == nameField.getDocument()) {
				attack.setName(nameField.getText());
			} else if (e.getDocument() == attackBonusField.getDocument()) {
				String txt = attackBonusField.getText();
				try {
					int val = Integer.parseInt(txt);
					attack.setAttackEnhancement(val);
					String s = attack.getAttacksDescription();
					if (attack.isTotalDefense()) s = "<html><body><s>"+s+"</s></body></html>";
					totalAttackLabel.setText(s);
				} catch (NumberFormatException ex) {
					if (txt.toLowerCase().equals("mw") || txt.toLowerCase().equals("masterwork")) {
						attack.setMasterwork(true);
					}
				}
			} else if (e.getDocument() == criticalField.getDocument()) {
				attack.critical = criticalField.getText();
			} else if (e.getDocument() == rangeField.getDocument()) {
				try {
					int val = Integer.parseInt(rangeField.getText());
					attack.range = val;
				} catch (NumberFormatException ex) {
					// TODO do what?
				}
			} else if (e.getDocument() == weightField.getDocument()) {
				try {
					int val = Integer.parseInt(weightField.getText());
					attack.weight = val;
				} catch (NumberFormatException ex) {
					// TODO do what?
				}
			} else if (e.getDocument() == typeField.getDocument()) {
				attack.damage_type = typeField.getText();
			} else if (e.getDocument() == propertiesField.getDocument()) {
				attack.properties = propertiesField.getText();
			} else if (e.getDocument() == ammunitionField.getDocument()) {
				attack.ammunition = ammunitionField.getText();
			}
		}
	};

	private void clearAttackForm() {
		if (attack != null) {
			attack.removePropertyChangeListener(this);
		}
		attack = null;

		nameField.setEditable(false);
		attackBonusField.setEditable(false);
		damageField.setEditable(false);
		criticalField.setEditable(false);
		rangeField.setEditable(false);
		weightField.setEditable(false);
		typeField.setEditable(false);
		propertiesField.setEditable(false);
		ammunitionField.setEditable(false);
		sizeCombo.setEnabled(false);
		kindCombo.setEnabled(false);
		usageCombo.setEnabled(false);
		itemButton.setEnabled(false);
		focusOverride.setEnabled(false);
		specializationOverride.setEnabled(false);

		nameField.setText("");
		itemLabel.setText("");
		attackBonusField.setText("");
		totalAttackLabel.setText("");
		damageField.setText("");
		totalDamageLabel.setText("");
		criticalField.setText("");
		rangeField.setText("");
		weightField.setText("");
		typeField.setText("");
		propertiesField.setText("");
		ammunitionField.setText("");
		sizeCombo.setSelectedItem(null);
		kindCombo.setSelectedItem(null);
		usageCombo.setSelectedItem(null);
		focusOverride.setSelectedItem(null);
		specializationOverride.setSelectedItem(null);
	}

	void setAttackForm(CharacterAttackForm attack) {
		if (attack == null) {
			clearAttackForm();
			return;
		}

		if (this.attack == attack) return;	// we do this test rather than at the start because this method is used for initialization with attack = null

		this.attack = attack;
		attack.addPropertyChangeListener(this);

		nameField.setEditable(true);
		attackBonusField.setEditable(true);
		damageField.setEditable(true);
		criticalField.setEditable(true);
		rangeField.setEditable(true);
		weightField.setEditable(true);
		typeField.setEditable(true);
		propertiesField.setEditable(true);
		ammunitionField.setEditable(true);
		sizeCombo.setEnabled(true);
		kindCombo.setEnabled(true);
		usageCombo.setEnabled(true);
		itemButton.setEnabled(true);
		focusOverride.setEnabled(true);
		specializationOverride.setEnabled(true);

		updateAttack = false;	// don't apply these changes to the attack (since they come from the attack)
		nameField.setText(attack.getName());
		if (attack.item != null) {
			itemLabel.setText(attack.item.getName());
		} else {
			itemLabel.setText("");
		}
		if (attack.isMasterwork()) {
			attackBonusField.setText("Masterwork");
		} else {
			attackBonusField.setText("" + attack.getAttackEnhancement());
		}
		damageField.setText(attack.getBaseDamage());
		criticalField.setText(attack.critical);
		rangeField.setText("" + attack.range);
		weightField.setText("" + attack.weight);
		typeField.setText(attack.damage_type);
		propertiesField.setText(attack.properties);
		ammunitionField.setText(attack.ammunition);
		sizeCombo.setSelectedItem(attack.attack.getSize());
		kindCombo.setSelectedItem(attack.getKind());
		usageCombo.setSelectedItem(attack.getUsage());
		proficientCheck.setSelected(attack.getProficient());
		focusOverride.setSelectedItem(attack.getFocus());
		specializationOverride.setSelectedItem(attack.getSpecialization());
		updateAttack = true;
		update();
	}

	private void update() {
		if (attack == null) {
			System.err.println("Error: Update for an AttackFormPanel that has no CharacterAttackForm");
			return;
		}
		// set attacks:
		String s = attack.getAttacksDescription();
		if (attack.isTotalDefense()) s = "<html><body><s>"+s+"</s></body></html>";
		totalAttackLabel.setText(s);

		// set attack tooltip:
		totalAttackLabel.setToolTipText("<html><body>" + attack.getSummary() + "</body></html>");

		// set damage:
		totalDamageLabel.setText(attack.getDamage());

		// set damage tooltip:
		totalDamageLabel.setToolTipText("<html><body>" + attack.getDamageSummary() + "</body></html>");
	}

	// TODO make into subclass of StatisticInfoDialog
	private class AttackFormInfoDialog extends JDialog {
		private JLabel summary;
		private JLabel damageSummary;
		private JButton okButton;
		private JPanel addPanel;

		AttackFormInfoDialog() {
			super(SwingUtilities.getWindowAncestor(AttackFormPanel.this), attack.getName(), Dialog.ModalityType.APPLICATION_MODAL);

			attack.addPropertyChangeListener(e -> updateSummary());

			damageSummary = new JLabel();
			damageSummary.setBorder(BorderFactory.createTitledBorder("Damage"));
			damageSummary.setVerticalAlignment(SwingConstants.TOP);

			summary = new JLabel();
			updateSummary();
			summary.setBorder(BorderFactory.createTitledBorder("Attack"));
			summary.setVerticalAlignment(SwingConstants.TOP);

			okButton = new JButton("Ok");
			okButton.addActionListener(e -> setVisible(false));

			addPanel = getAdhocPanel();
			addPanel.setBorder(BorderFactory.createTitledBorder("Adhoc Modifier"));

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 0.5;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			add(summary, c);

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
			setLocationRelativeTo(SwingUtilities.getWindowAncestor(AttackFormPanel.this));
		}

		JPanel getAdhocPanel() {
			final JComboBox<StandardType> typeBox = new JComboBox<>(StandardType.values());
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
				if (attackMod.isSelected()) bf.addEffect(Creature.STATISTIC_ATTACKS + "[id=" + attack.id + "]", typeBox.getSelectedItem().toString(), mod);
				if (damageMod.isSelected()) bf.addEffect(Creature.STATISTIC_DAMAGE + "[id=" + attack.id + "]", typeBox.getSelectedItem().toString(), mod);
				attack.addBuff(bf.getBuff());
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

			c.gridy++;
			c.gridx = 0;
			addPanel.add(attackMod, c);

			c.gridx++;
			addPanel.add(damageMod, c);

			return addPanel;
		}

		void updateSummary() {
			summary.setText("<html><body>" + attack.getSummary() + "</body></html>");
			damageSummary.setText("<html><body>" + attack.getDamageSummary() + "</body></html>");
			pack();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		update();
	}
}

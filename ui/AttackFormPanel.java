package ui;

import gamesystem.Attacks;
import gamesystem.Attacks.AttackForm;
import gamesystem.SizeCategory;
import gamesystem.dice.CombinedDice;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JButton;
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

// TODO only attack and damage labels are updated on changes from the attack, other changes will be ignored (needs fixing if something other than this panel makes changes to the attack)

@SuppressWarnings("serial")
class AttackFormPanel extends JPanel implements PropertyChangeListener {
	//Attacks attacks;
	private AttackForm attack;

	private JTextField nameField = new JTextField(20);
	private JTextField attackBonusField = new JTextField(20);
	private JTextField damageField = new JTextField(20);
	private JTextField criticalField = new JTextField(20);
	private JTextField rangeField = new JTextField(20);
	private JTextField weightField = new JTextField(20);
	private JTextField typeField = new JTextField(20);
	private JComboBox sizeCombo = new JComboBox(SizeCategory.values());
	private JTextField propertiesField = new JTextField(20);
	private JTextField ammunitionField = new JTextField(20);
	private JComboBox kindCombo;
	private JComboBox usageCombo;

	private JLabel totalAttackLabel = new JLabel();
	private JLabel totalDamageLabel = new JLabel();

	AttackFormPanel() {
		this(null);
	}

	private AttackFormPanel(AttackForm atk) {
		super(new GridBagLayout());

		kindCombo = new JComboBox(Attacks.Kind.values());
		usageCombo = new JComboBox(Attacks.Usage.values());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1,2,1,2);
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = 0; c.gridy = 0;
		c.weightx = 0.5;
		add(new JLabel("Name:"), c);
		c.gridy++; add(new JLabel("Attack Bonus:"),c);
		c.gridy++; add(new JLabel("Damage:"),c);
		c.gridy++; add(new JLabel("Critical:"),c);
		c.gridy++; add(new JLabel("Kind:"),c);
		c.gridy++; add(new JLabel("Usage:"),c);
		c.gridy++; add(new JLabel("Damage Type:"),c);
		c.gridy++; add(new JLabel("Size:"),c);
		c.gridy++; add(new JLabel("Range (ft):"),c);
		c.gridy++; add(new JLabel("Weight (lbs):"),c);
		c.gridy++; add(new JLabel("Properties:"),c);
		c.gridy++; add(new JLabel("Ammunition:"),c);

		c.gridx = 1; c.gridy = 0;
		c.weightx = 1.0;
		c.gridwidth = 2;
		add(nameField,c);
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

		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = 1;
		add(attackBonusField,c);
		c.gridy++; add(damageField,c);

		c.gridy = 1;
		c.gridx = 2;
		add(totalAttackLabel,c);
		c.gridy++; add(totalDamageLabel,c);


		setAttackForm(atk);

		addMouseListener(rightClickListener);
		totalAttackLabel.addMouseListener(rightClickListener);
		totalDamageLabel.addMouseListener(rightClickListener);

		// the damage field uses an input verifier and applies changes on enter or losing focus
		damageField.setInputVerifier(damageVerifier);
		damageField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				damageVerifier.shouldYieldFocus(damageField);
			}
		});

		// the other field do dynamic updating
		nameField.getDocument().addDocumentListener(docListener);
		attackBonusField.getDocument().addDocumentListener(docListener);
		criticalField.getDocument().addDocumentListener(docListener);
		rangeField.getDocument().addDocumentListener(docListener);
		weightField.getDocument().addDocumentListener(docListener);
		typeField.getDocument().addDocumentListener(docListener);
		propertiesField.getDocument().addDocumentListener(docListener);
		ammunitionField.getDocument().addDocumentListener(docListener);

		kindCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (attack != null) {
					attack.setKind((Attacks.Kind)kindCombo.getSelectedItem());
				}
			}
		});
		sizeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (attack != null) {
					attack.size = (SizeCategory) sizeCombo.getSelectedItem();
				}
			}
		});
		usageCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (attack != null) {
					attack.setUsage((Attacks.Usage)usageCombo.getSelectedItem());
				}
			}
		});
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

			// TODO ugly. find a better way
			if (e.getDocument() == nameField.getDocument()) {
				// TODO this should notify the model as well
				attack.setName(nameField.getText());
			} else if (e.getDocument() == attackBonusField.getDocument()) {
				try {
					int val = Integer.parseInt(attackBonusField.getText());
					attack.setAttackEnhancement(val);
					String s = attack.getAttacksDescription();
					if (attack.isTotalDefense()) s = "<html><body><s>"+s+"</s></body></html>";
					totalAttackLabel.setText(s);
				} catch (NumberFormatException ex) {
					// TODO do what?
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

		nameField.setText("");
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
	}

	void setAttackForm(AttackForm attack) {
		if (attack == null) {
			clearAttackForm();

		} else {
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

			nameField.setText(attack.getName());
			attackBonusField.setText(""+attack.getAttackEnhancement());
			damageField.setText(attack.getBaseDamage());
			criticalField.setText(attack.critical);
			rangeField.setText(""+attack.range);
			weightField.setText(""+attack.weight);
			typeField.setText(attack.damage_type);
			propertiesField.setText(attack.properties);
			ammunitionField.setText(attack.ammunition);
			sizeCombo.setSelectedItem(attack.size);
			kindCombo.setSelectedItem(attack.getKind());
			usageCombo.setSelectedItem(attack.getUsage());
			update();
		}
	}

	private void update() {
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

			attack.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					updateSummary();
				}
			});

			damageSummary = new JLabel();
			damageSummary.setBorder(BorderFactory.createTitledBorder("Damage"));
			damageSummary.setVerticalAlignment(SwingConstants.TOP);

			summary = new JLabel();
			updateSummary();
			summary.setBorder(BorderFactory.createTitledBorder("Attack"));
			summary.setVerticalAlignment(SwingConstants.TOP);

			okButton = new JButton("Ok");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});

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
			final JComboBox typeBox = new JComboBox(StatisticInfoDialog.types);
			typeBox.setSelectedItem("Enhancement");
			typeBox.setEditable(true);

			final JTextField nameField = new JTextField();

			final JFormattedTextField modField = new JFormattedTextField();
			modField.setValue(new Integer(0));
			modField.setColumns(3);

			JButton addButton = new JButton("Add");
			addButton.setEnabled(false);
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO implement - need to be able to get to the character (ultimately should appear in the list of the possessor, but remain linked to the item)
//					BuffFactory bf = new BuffFactory(nameField.getText());
//					int mod = (Integer) modField.getValue();
//					bf.addEffect(statName, typeBox.getSelectedItem().toString(), mod);
//					Buff buff = bf.getBuff();
//					buff.applyBuff(character);
//					character.buffs.addElement(buff);
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

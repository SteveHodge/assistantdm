package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.Document;

import gamesystem.AC;
import gamesystem.ItemDefinition;
import gamesystem.Modifier;
import gamesystem.Statistic;
import gamesystem.core.Property;
import gamesystem.core.PropertyListener;
import party.Character;
import swing.SpinnerCellEditor;

// TODO flag conditional modifiers for touch and flatfooted
// TODO integer fields should be validated or formatted
// TODO armor and shield bonus should show tooltip in table and/or show current enhancement bonus
@SuppressWarnings("serial")
public class CharacterACPanel extends CharacterSubPanel {
	protected TableModel acModel;
	protected JLabel totalLabel;
	protected JLabel touchLabel;
	protected JLabel flatLabel;

	JTextField nameField = new JTextField(20);
	JTextField typeField = new JTextField(20);
	JTextField bonusField = new JTextField(20);
	JTextField enhancementField = new JTextField(20);
	JTextField maxDexField = new JTextField(20);
	JTextField checkPenaltyField = new JTextField(20);
	JTextField spellFailureField = new JTextField(20);
	JTextField speedField = new JTextField(20);
	JTextField weightField = new JTextField(20);
	JTextField propertiesField = new JTextField(20);

	JTextField shieldNameField = new JTextField(20);
	JTextField shieldBonusField = new JTextField(20);
	JTextField shieldEnhancementField = new JTextField(20);
	JTextField shieldCheckPenaltyField = new JTextField(20);
	JTextField shieldSpellFailureField = new JTextField(20);
	JTextField shieldWeightField = new JTextField(20);
	JTextField shieldPropertiesField = new JTextField(20);

	JCheckBox proficientCheck = new JCheckBox();
	JCheckBox shieldProficientCheck = new JCheckBox();

	ItemDefinition armorItem;
	ItemDefinition shieldItem;

	JLabel armorItemLabel = new JLabel();
	JLabel shieldItemLabel = new JLabel();

	AC ac;
	AC.Armor armor;
	AC.Shield shield;

	boolean editing = false;	// flag used to prevent updates while a field's document is changing

	PropertyListener<Integer> armorListener = new PropertyListener<Integer>() {
		// NOTE: we call this to initially populate the fields with null evt
		@Override
		public void propertyChanged(Property<Integer> source, Integer oldValue) {
			if (editing) return;
			nameField.setText(armor.description);
			typeField.setText(armor.type);
			bonusField.setText(""+armor.getBonus());
			enhancementField.setText(""+armor.getEnhancement());
			if (armor.getMaxDex() < Integer.MAX_VALUE) {
				maxDexField.setText(""+armor.getMaxDex());
			} else {
				maxDexField.setText("");
			}
			checkPenaltyField.setText(""+armor.getACP());
			spellFailureField.setText(""+armor.spellFailure);
			speedField.setText(""+armor.speed);
			weightField.setText(""+armor.weight);
			propertiesField.setText(armor.properties);
			proficientCheck.setSelected(armor.proficient);
			armorItem = armor.item;
			armorItemLabel.setText(armor.item != null ? armor.item.getName() : "");
		}
	};

	PropertyListener<Integer> shieldListener = new PropertyListener<Integer>() {
		// NOTE: we call this to initially populate the fields with null evt
		@Override
		public void propertyChanged(Property<Integer> source, Integer oldValue) {
			if (editing) return;
			shieldNameField.setText(shield.description);
			shieldBonusField.setText(""+shield.getBonus());
			shieldEnhancementField.setText(""+shield.getEnhancement());
			shieldCheckPenaltyField.setText(""+shield.getACP());
			shieldSpellFailureField.setText(""+shield.spellFailure);
			shieldWeightField.setText(""+shield.weight);
			shieldPropertiesField.setText(shield.properties);
			shieldProficientCheck.setSelected(shield.proficient);
			shieldItem = shield.item;
			shieldItemLabel.setText(shield.item != null ? shield.item.getName() : "");
		}
	};

	public CharacterACPanel(Character c) {
		super(c);

		ac = character.getACStatistic();
		armor = ac.getArmor();
		shield = ac.getShield();

		summary = getSummary();
		acModel = new ACTableModel();

		JTable acTable = new JTable(acModel);
		acTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		acTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		acTable.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		acTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
			Color old = null;
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (column == 1 && !acModel.isCellEditable(row, column)) {
					if (old == null) old = cell.getBackground();
					cell.setBackground(Color.LIGHT_GRAY);
				} else {
					if (old != null) cell.setBackground(old);
				}
				return cell;
			}
		});
		JScrollPane acScrollpane = new JScrollPane(acTable);
		acScrollpane.setPreferredSize(new Dimension(450,200));

		totalLabel = new JLabel("Total AC: "+ac.getValue()+(ac.hasConditionalModifier()?"*":""));
		touchLabel = new JLabel("Touch AC: "+ac.getTouchAC().getValue()+(ac.getTouchAC().hasConditionalModifier()?"*":""));
		flatLabel = new JLabel("Flat-footed AC: "+ac.getFlatFootedAC().getValue()+(ac.getFlatFootedAC().hasConditionalModifier()?"*":""));
		updateToolTips();

		setLayout(new GridBagLayout());
		GridBagConstraints a = new GridBagConstraints();

		a.insets = new Insets(2, 3, 2, 3);

		a.weightx = 0.0; a.weighty = 0.0;
		a.gridx = 0; a.gridy = 0; a.gridheight = 1;
		a.fill = GridBagConstraints.NONE;
		add(totalLabel,a);
		a.gridy = 1; add(touchLabel,a);
		a.gridy = 2; add(flatLabel,a);
		a.fill = GridBagConstraints.BOTH;
		a.weightx = 1.0; a.weighty = 1.0;
		a.gridy = 3; add(acScrollpane,a);

		a.gridx = 1; a.gridy = 0; a.gridheight = 4;
		add(getArmorPanel(),a);

		character.addPropertyListener("ac", (source, old) -> {
			totalLabel.setText("Total AC: " + ac.getValue() + (ac.hasConditionalModifier() ? "*" : ""));
			touchLabel.setText("Touch AC: " + ac.getTouchAC().getValue() + (ac.getTouchAC().hasConditionalModifier() ? "*" : ""));
			flatLabel.setText("Flat-footed AC: " + ac.getFlatFootedAC().getValue() + (ac.getFlatFootedAC().hasConditionalModifier() ? "*" : ""));
			updateToolTips();
			updateSummaries(getSummary());
		});
		armor.addPropertyListener(armorListener);
		shield.addPropertyListener(shieldListener);
		armorListener.propertyChanged(null, null);		// update the values of the fields - won't work if we ever use the event object
		shieldListener.propertyChanged(null, null);		// update the values of the fields - won't work if we ever use the event object
	}

	protected JPanel getArmorPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Armor and shield"));

		nameField.getDocument().addDocumentListener(docListener);
		typeField.getDocument().addDocumentListener(docListener);
		bonusField.getDocument().addDocumentListener(docListener);
		enhancementField.getDocument().addDocumentListener(docListener);
		maxDexField.getDocument().addDocumentListener(docListener);
		checkPenaltyField.getDocument().addDocumentListener(docListener);
		spellFailureField.getDocument().addDocumentListener(docListener);
		speedField.getDocument().addDocumentListener(docListener);
		weightField.getDocument().addDocumentListener(docListener);
		propertiesField.getDocument().addDocumentListener(docListener);
		proficientCheck.addActionListener(e -> {
			armor.proficient = proficientCheck.isSelected();
		});

		shieldNameField.getDocument().addDocumentListener(docListener);
		shieldBonusField.getDocument().addDocumentListener(docListener);
		shieldEnhancementField.getDocument().addDocumentListener(docListener);
		shieldCheckPenaltyField.getDocument().addDocumentListener(docListener);
		shieldSpellFailureField.getDocument().addDocumentListener(docListener);
		shieldWeightField.getDocument().addDocumentListener(docListener);
		shieldPropertiesField.getDocument().addDocumentListener(docListener);
		shieldProficientCheck.addActionListener(e -> {
			shield.proficient = shieldProficientCheck.isSelected();
		});

		JButton armorItemButton = new JButton("...");
		armorItemButton.setMargin(new Insets(2, 4, 2, 2));
		armorItemButton.addActionListener(e -> openArmorChooser());
		JButton shieldItemButton = new JButton("...");
		shieldItemButton.setMargin(new Insets(2, 4, 2, 2));
		shieldItemButton.addActionListener(e -> openShieldChooser());
//		shieldItemLabel.setBorder(BorderFactory.createLineBorder(Color.RED));
//		armorItemLabel.setBorder(BorderFactory.createLineBorder(Color.RED));

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1,2,1,2);
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = 0;
		c.weightx = 0.5;
		c.gridy = 1;
		panel.add(new JLabel("Description:"), c);
		c.gridy = GridBagConstraints.RELATIVE;
		panel.add(new JLabel("Base Item:"), c);
		panel.add(new JLabel("Type:"), c);
		panel.add(new JLabel("Bonus:"), c);
		panel.add(new JLabel("Enhancement:"), c);
		panel.add(new JLabel("Max Dex:"), c);
		panel.add(new JLabel("ACP:"), c);
		panel.add(new JLabel("Spell Failure:"), c);
		panel.add(new JLabel("Speed:"), c);
		panel.add(new JLabel("Weight (lbs):"), c);
		panel.add(new JLabel("Properties:"), c);
		panel.add(new JLabel("Proficient:"), c);

		c.gridx = 1;
		c.weightx = 1.0;
		c.gridwidth = 2;
		panel.add(new JLabel("Armor", SwingConstants.CENTER),c);
		panel.add(nameField, c);
		c.gridwidth = 1;
		panel.add(armorItemLabel, c);
		c.gridwidth = 2;
		panel.add(typeField, c);
		panel.add(bonusField, c);
		panel.add(enhancementField, c);
		panel.add(maxDexField, c);
		panel.add(checkPenaltyField, c);
		panel.add(spellFailureField, c);
		panel.add(speedField, c);
		panel.add(weightField, c);
		panel.add(propertiesField, c);
		panel.add(proficientCheck, c);

		c.gridx = 2;
		c.gridy = 2;
		c.weightx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		panel.add(armorItemButton, c);

		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 1.0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Shield", SwingConstants.CENTER), c);
		c.gridy++; panel.add(shieldNameField,c);
		c.gridwidth = 1;
		c.gridy++;
		panel.add(shieldItemLabel, c);
		c.gridwidth = 2;
		c.gridy++;
		c.gridy++; panel.add(shieldBonusField,c);
		c.gridy++; panel.add(shieldEnhancementField,c);
		c.gridy++;
		c.gridy++; panel.add(shieldCheckPenaltyField,c);
		c.gridy++; panel.add(shieldSpellFailureField,c);
		c.gridy++;
		c.gridy++; panel.add(shieldWeightField,c);
		c.gridy++; panel.add(shieldPropertiesField,c);
		c.gridy++;
		panel.add(shieldProficientCheck, c);

		c.gridx = 4;
		c.gridy = 2;
		c.weightx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		panel.add(shieldItemButton, c);

		return panel;
	}

	void openArmorChooser() {
		List<ItemDefinition> armors = new ArrayList<>(ItemDefinition.getArmor());
		armors.sort((a, b) -> {
			if (!(a instanceof ItemDefinition || !(b instanceof ItemDefinition))) return 0;
			return a.getName().compareToIgnoreCase(b.getName());
		});
		Object[] options = new Object[armors.size() + 1];
		options[0] = new String("none");
		for (int i = 1; i < options.length; i++) {
			options[i] = armors.get(i - 1);
		}
		Object s = JOptionPane.showInputDialog(this, "Select armor:", "Select armor", JOptionPane.QUESTION_MESSAGE, null, options, null);
		if (s != null) {
			if (s instanceof ItemDefinition) {
				armor.item = (ItemDefinition) s;
				armorItemLabel.setText(armor.item.getName());
				nameField.setText(armor.item.getName());
				typeField.setText(armor.item.getArmorType());
				bonusField.setText(armor.item.getArmorBonus());
				maxDexField.setText(armor.item.getArmorMaxDex());
				checkPenaltyField.setText(armor.item.getArmorACP());
				spellFailureField.setText(armor.item.getSpellFailure());
				speedField.setText(armor.item.getArmorSpeed());
				weightField.setText(armor.item.getWeight());
				proficientCheck.setSelected(true);
			} else {
				armor.item = null;
				armorItemLabel.setText("");
				nameField.setText("");
				typeField.setText("");
				bonusField.setText("");
				maxDexField.setText("");
				checkPenaltyField.setText("");
				spellFailureField.setText("");
				speedField.setText("");
				weightField.setText("");
				proficientCheck.setSelected(true);
			}
		}
	}

	void openShieldChooser() {
		List<ItemDefinition> shields = new ArrayList<>(ItemDefinition.getShields());
		shields.sort((a,b) -> {
			if (!(a instanceof ItemDefinition || !(b instanceof ItemDefinition))) return 0;
			return a.getName().compareToIgnoreCase(b.getName());
		});
		Object[] options = new Object[shields.size() + 1];
		options[0] = new String("none");
		for (int i = 1; i < options.length; i++) {
			options[i] = shields.get(i - 1);
		}
		Object s = JOptionPane.showInputDialog(this, "Select a shield:", "Select a shield", JOptionPane.QUESTION_MESSAGE, null, options, null);
		if (s != null) {
			if (s instanceof ItemDefinition) {
				shield.item = (ItemDefinition) s;
				shieldItemLabel.setText(shield.item.getName());
				shieldNameField.setText(shield.item.getName());
				shieldBonusField.setText(shield.item.getShieldBonus());
				shieldCheckPenaltyField.setText(shield.item.getShieldACP());
				shieldSpellFailureField.setText(shield.item.getShieldSpellFailure());
				shieldWeightField.setText(shield.item.getWeight());
				shieldProficientCheck.setSelected(true);
			} else {
				shield.item = null;
				shieldItemLabel.setText("");
				shieldNameField.setText("");
				shieldBonusField.setText("");
				shieldCheckPenaltyField.setText("");
				shieldSpellFailureField.setText("");
				shieldWeightField.setText("");
				shieldProficientCheck.setSelected(true);
			}
		}
	}

	DocumentListener docListener = new DocumentListener() {
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

		protected int getIntValue(String text) {
			return getIntValue(text, 0);
		}

		protected int getIntValue(String text, int def) {
			int v = def;
			try {
				v = Integer.parseInt(text);
			} catch(NumberFormatException ex) {
			}
			return v;
		}

		protected void updateField(DocumentEvent e) {
			if (armor == null || shield == null) return;	// shouldn't happen
			Document d = e.getDocument();

			editing = true;
			// TODO ugly. find a better way
			if (d == nameField.getDocument()) {
				armor.description = nameField.getText();
			} else if (d == typeField.getDocument()) {
				armor.type = typeField.getText();
			} else if (d == bonusField.getDocument()) {
				armor.setBonus(getIntValue(bonusField.getText()));
			} else if (d == enhancementField.getDocument()) {
				armor.setEnhancement(getIntValue(enhancementField.getText()));
			} else if (d == maxDexField.getDocument()) {
				armor.setMaxDex(getIntValue(maxDexField.getText()));
			} else if (d == checkPenaltyField.getDocument()) {
				armor.setACP(getIntValue(checkPenaltyField.getText()));
			} else if (d == spellFailureField.getDocument()) {
				armor.spellFailure = getIntValue(spellFailureField.getText());
			} else if (d == speedField.getDocument()) {
				armor.speed = getIntValue(speedField.getText());
			} else if (d == weightField.getDocument()) {
				armor.weight = getIntValue(weightField.getText());
			} else if (d == propertiesField.getDocument()) {
				armor.properties = propertiesField.getText();

			} else if (d == shieldNameField.getDocument()) {
				shield.description = shieldNameField.getText();
			} else if (d == shieldBonusField.getDocument()) {
				shield.setBonus(getIntValue(shieldBonusField.getText()));
			} else if (d == shieldEnhancementField.getDocument()) {
				shield.setEnhancement(getIntValue(shieldEnhancementField.getText()));
			} else if (d == shieldCheckPenaltyField.getDocument()) {
				shield.setACP(getIntValue(shieldCheckPenaltyField.getText()));
			} else if (d == shieldSpellFailureField.getDocument()) {
				shield.spellFailure = getIntValue(shieldSpellFailureField.getText());
			} else if (d == shieldWeightField.getDocument()) {
				shield.weight = getIntValue(shieldWeightField.getText());
			} else if (d == shieldPropertiesField.getDocument()) {
				shield.properties = shieldPropertiesField.getText();
			}

			editing = false;
		}
	};

	protected String getSummary() {
		StringBuilder s = new StringBuilder();
		s.append("AC ").append(character.getACStatistic().getValue());
		s.append("   Touch ").append(character.getACStatistic().getTouchAC().getValue());
		s.append("   Flat-footed ").append(character.getACStatistic().getFlatFootedAC().getValue());
		return s.toString();
	}

	protected void updateToolTips() {
		Map<Modifier, Boolean> mods = ac.getModifiers();
		StringBuilder text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getACStatistic().getValue()).append(" total");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		totalLabel.setToolTipText(text.toString());

		mods = ac.getTouchAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getACStatistic().getTouchAC().getValue()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		touchLabel.setToolTipText(text.toString());

		mods = ac.getFlatFootedAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getACStatistic().getFlatFootedAC().getValue()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		flatLabel.setToolTipText(text.toString());
	}

	protected class ACTableModel extends AbstractTableModel {
		// need a row for each custom modifier plus a row for each other modifier on the AC
		Modifier[] modifiers;
		boolean[] active;

		public ACTableModel() {
			character.addPropertyListener("ac", (source, old) -> updateModifiers());
			updateModifiers();
		}

		protected void updateModifiers() {
			Map<Modifier, Boolean> mods = ac.getModifiers();
			ArrayList<Modifier> list = new ArrayList<>();
			for (Modifier m : mods.keySet()) {
				if (m.getSource() == null || !m.getSource().equals("user set")) {
					list.add(m);
				}
			}
			modifiers = list.toArray(new Modifier[list.size()]);
			Arrays.sort(modifiers, (a, b) -> {
				// TODO should compare source and condition as well
				String aStr = a.getType() == null ? "" : a.getType();
				String bStr = b.getType() == null ? "" : b.getType();
				return aStr.compareTo(bStr);
			});
			active = new boolean[modifiers.length];
			for (int i=0; i<modifiers.length; i++) {
				active[i] = mods.get(modifiers[i]);
			}

			//System.out.println("Have "+modifiers.length+" modifiers");

			fireTableDataChanged();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) return false;
			//if (rowIndex == Character.ACComponentType.DEX.ordinal()) return false;
			if (rowIndex >= Character.ACComponentType.values().length) return false;
			return true;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (!isCellEditable(rowIndex, columnIndex)) return;
			if (value == null) value = new Integer(0);
			character.setACComponent(Character.ACComponentType.values()[rowIndex], (Integer)value);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Component";
			return "Value";
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return Character.ACComponentType.values().length + modifiers.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < Character.ACComponentType.values().length) {
				if (column == 0) return Character.ACComponentType.values()[row].toString();
				return character.getACComponent(Character.ACComponentType.values()[row]);
			} else {
				row -= Character.ACComponentType.values().length;
				if (column == 0) {
					String s = modifiers[row].getType();
					if (s == null) s = modifiers[row].getModifier() >= 0 ? "Bonus" : "Penalty";
					if (modifiers[row].getSource() != null) s += " (from "+modifiers[row].getSource()+")";
					if (modifiers[row].getCondition() != null) s += " ("+modifiers[row].getCondition()+")";
					if (!active[row]) s += " (inactive)";
					return s;
				}
				return modifiers[row].getModifier();
			}
		}
	}
}

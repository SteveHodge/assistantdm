package ui;

import gamesystem.AC;
import gamesystem.Creature;
import gamesystem.Modifier;
import gamesystem.Statistic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.Document;

import party.Character;
import swing.SpinnerCellEditor;

// TODO flag conditional modifiers for touch and flatfooted
// TODO integer fields should be validated or formatted
// TODO armor and shield bonus should show tooltip in table and/or show current enhancement bonus
@SuppressWarnings("serial")
public class CharacterACPanel extends CharacterSubPanel implements PropertyChangeListener {
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

	AC ac;
	AC.Armor armor;
	AC.Shield shield;

	boolean editing = false;	// flag used to prevent updates while a field's document is changing

	PropertyChangeListener armorListener = new PropertyChangeListener() {
		// NOTE: we call this to initially populate the fields with null evt
		public void propertyChange(PropertyChangeEvent evt) {
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
		}
	};

	PropertyChangeListener shieldListener = new PropertyChangeListener() {
		// NOTE: we call this to initially populate the fields with null evt
		public void propertyChange(PropertyChangeEvent evt) {
			if (editing) return;
			shieldNameField.setText(shield.description);
			shieldBonusField.setText(""+shield.getBonus());
			shieldEnhancementField.setText(""+shield.getEnhancement());
			shieldCheckPenaltyField.setText(""+shield.getACP());
			shieldSpellFailureField.setText(""+shield.spellFailure);
			shieldWeightField.setText(""+shield.weight);
			shieldPropertiesField.setText(shield.properties);
		}
	};

	public CharacterACPanel(Character c) {
		super(c);

		ac = (AC)character.getStatistic(Creature.STATISTIC_AC);
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

		character.addPropertyChangeListener(this);
		armor.addPropertyChangeListener(armorListener);
		shield.addPropertyChangeListener(shieldListener);
		armorListener.propertyChange(null);		// update the values of the fields - won't work if we ever use the event object
		shieldListener.propertyChange(null);		// update the values of the fields - won't work if we ever use the event object
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

		shieldNameField.getDocument().addDocumentListener(docListener);
		shieldBonusField.getDocument().addDocumentListener(docListener);
		shieldEnhancementField.getDocument().addDocumentListener(docListener);
		shieldCheckPenaltyField.getDocument().addDocumentListener(docListener);
		shieldSpellFailureField.getDocument().addDocumentListener(docListener);
		shieldWeightField.getDocument().addDocumentListener(docListener);
		shieldPropertiesField.getDocument().addDocumentListener(docListener);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1,2,1,2);
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = 0; c.gridy = 0;
		c.weightx = 0.5;
		c.gridy++; panel.add(new JLabel("Description:"),c);
		c.gridy++; panel.add(new JLabel("Type:"),c);
		c.gridy++; panel.add(new JLabel("Bonus:"),c);
		c.gridy++; panel.add(new JLabel("Enhancement:"),c);
		c.gridy++; panel.add(new JLabel("Max Dex:"),c);
		c.gridy++; panel.add(new JLabel("ACP:"),c);
		c.gridy++; panel.add(new JLabel("Spell Failure:"),c);
		c.gridy++; panel.add(new JLabel("Speed:"),c);
		c.gridy++; panel.add(new JLabel("Weight (lbs):"),c);
		c.gridy++; panel.add(new JLabel("Properties:"),c);

		c.gridx = 1; c.gridy = 0;
		c.weightx = 1.0;
		panel.add(new JLabel("Armor", JLabel.CENTER),c);
		c.gridy++; panel.add(nameField,c);
		c.gridy++; panel.add(typeField,c);
		c.gridy++; panel.add(bonusField,c);
		c.gridy++; panel.add(enhancementField,c);
		c.gridy++; panel.add(maxDexField,c);
		c.gridy++; panel.add(checkPenaltyField,c);
		c.gridy++; panel.add(spellFailureField,c);
		c.gridy++; panel.add(speedField,c);
		c.gridy++; panel.add(weightField,c);
		c.gridy++; panel.add(propertiesField,c);

		c.gridx = 2; c.gridy = 0;
		panel.add(new JLabel("Shield", JLabel.CENTER),c);
		c.gridy++; panel.add(shieldNameField,c);
		c.gridy++;
		c.gridy++; panel.add(shieldBonusField,c);
		c.gridy++; panel.add(shieldEnhancementField,c);
		c.gridy++; 
		c.gridy++; panel.add(shieldCheckPenaltyField,c);
		c.gridy++; panel.add(shieldSpellFailureField,c);
		c.gridy++; 
		c.gridy++; panel.add(shieldWeightField,c);
		c.gridy++; panel.add(shieldPropertiesField,c);

		return panel;
	}
	
	DocumentListener docListener = new DocumentListener() {
		public void changedUpdate(DocumentEvent e) {
			updateField(e);
		}

		public void insertUpdate(DocumentEvent e) {
			updateField(e);
		}

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

	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(Creature.PROPERTY_AC)) {
			totalLabel.setText("Total AC: "+ac.getValue()+(ac.hasConditionalModifier()?"*":""));
			touchLabel.setText("Touch AC: "+ac.getTouchAC().getValue()+(ac.getTouchAC().hasConditionalModifier()?"*":""));
			flatLabel.setText("Flat-footed AC: "+ac.getFlatFootedAC().getValue()+(ac.getFlatFootedAC().hasConditionalModifier()?"*":""));
			updateToolTips();
			updateSummaries(getSummary());
		}
	}

	protected String getSummary() {
		StringBuilder s = new StringBuilder();
		s.append("AC ").append(character.getAC());
		s.append("   Touch ").append(character.getTouchAC());
		s.append("   Flat-footed ").append(character.getFlatFootedAC());
		return s.toString();
	}

	protected void updateToolTips() {
		Map<Modifier, Boolean> mods = ac.getModifiers();
		StringBuilder text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getAC()).append(" total");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		totalLabel.setToolTipText(text.toString());

		mods = ac.getTouchAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getTouchAC()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		touchLabel.setToolTipText(text.toString());

		mods = ac.getFlatFootedAC().getModifiers();
		text = new StringBuilder();
		text.append("<html><body>10 base<br/>");
		text.append(Statistic.getModifiersHTML(mods));
		text.append(character.getFlatFootedAC()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		flatLabel.setToolTipText(text.toString());
	}

	protected class ACTableModel extends AbstractTableModel implements PropertyChangeListener {
		// need a row for each custom modifier plus a row for each other modifier on the AC
		Modifier[] modifiers;
		boolean[] active;

		public ACTableModel() {
			character.addPropertyChangeListener(Creature.PROPERTY_AC, this);
			updateModifiers();
		}

		protected void updateModifiers() {
			Map<Modifier, Boolean> mods = ac.getModifiers();
			ArrayList<Modifier> list = new ArrayList<Modifier>();
			for (Modifier m : mods.keySet()) {
				if (m.getSource() == null || !m.getSource().equals("user set")) {
					list.add(m);
				}
			}
			modifiers = list.toArray(new Modifier[list.size()]);
			Arrays.sort(modifiers, new Comparator<Modifier>() {
				public int compare(Modifier a, Modifier b) {
					// TODO should compare source and condition as well
					String aStr = a.getType() == null ? "" : a.getType();
					String bStr = b.getType() == null ? "" : b.getType();
					return aStr.compareTo(bStr);
				}
			});
			active = new boolean[modifiers.length];
			for (int i=0; i<modifiers.length; i++) {
				active[i] = mods.get(modifiers[i]);
			}

			//System.out.println("Have "+modifiers.length+" modifiers");

			fireTableDataChanged();
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) return false;
			//if (rowIndex == Character.ACComponentType.DEX.ordinal()) return false;
			if (rowIndex >= Character.ACComponentType.values().length) return false;
			return true;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (!isCellEditable(rowIndex, columnIndex)) return;
			if (value == null) value = new Integer(0);
			character.setACComponent(Character.ACComponentType.values()[rowIndex], (Integer)value);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}

		public String getColumnName(int column) {
			if (column == 0) return "Component";
			return "Value";
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return Character.ACComponentType.values().length + modifiers.length;
		}

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

		public void propertyChange(PropertyChangeEvent evt) {
			//if (evt.getPropertyName().equals(Character.PROPERTY_AC)) {	// assumed
			updateModifiers();
			//}
		}
	}
}

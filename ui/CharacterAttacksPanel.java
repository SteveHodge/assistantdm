package ui;

import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.Feat;
import gamesystem.Modifier;
import gamesystem.Statistic;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import party.Character;
import party.Creature;

// TODO make power attack and combat expertise filtered/numeric fields or combos or cycles 
@SuppressWarnings("serial")
public class CharacterAttacksPanel extends CharacterSubPanel implements PropertyChangeListener {
	protected BoundIntegerField BAB;
	protected JLabel strLabel = new JLabel();
	protected JLabel dexLabel = new JLabel();
	protected JLabel meleeLabel = new JLabel();
	protected JLabel rangedLabel = new JLabel();
	protected JTextField powerAttack = new JTextField(10);
	protected JTextField combatExpertise = new JTextField(10);
	protected JCheckBox fightingDefensively = new JCheckBox("Fighting Defensively");
	protected JCheckBox totalDefense = new JCheckBox("Total Defense");
	protected Attacks attacks;
	protected AttackFormPanel attackPanel;
	protected JList weaponList;

	public CharacterAttacksPanel(Character chr) {
		super(chr);
		attacks = (Attacks)chr.getStatistic(Creature.STATISTIC_ATTACKS); 
		summary = getSummary();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(getBaseValuesPanel());
		add(getAttackOptionsPanel());
		add(getWeaponPanel());

		updateToolTip();
		// update labels when character changes
		character.addPropertyChangeListener(this);

		powerAttack.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) {
				updatePowerAttack();
			}

			public void insertUpdate(DocumentEvent arg0) {
				updatePowerAttack();
			}

			public void removeUpdate(DocumentEvent arg0) {
				updatePowerAttack();
			}
		});

		combatExpertise.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateCombatExpertise();
			}

			public void insertUpdate(DocumentEvent e) {
				updateCombatExpertise();
			}

			public void removeUpdate(DocumentEvent e) {
				updateCombatExpertise();
			}
			
		});

		fightingDefensively.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				attacks.setFightingDefensively(fightingDefensively.isSelected());
				if (fightingDefensively.isSelected()) {
					totalDefense.setSelected(false);
					attacks.setTotalDefense(false);
				}
			}
		});

		totalDefense.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				attacks.setTotalDefense(totalDefense.isSelected());
				if (totalDefense.isSelected()) {
					fightingDefensively.setSelected(false);
					attacks.setFightingDefensively(false);
					combatExpertise.setText("0");
					attacks.setCombatExpertise(0);
					powerAttack.setText("0");
					attacks.setPowerAttack(0);
				}
			}
		});
	}

	protected void updatePowerAttack() {
		int value = 0;
		try {
			value = Integer.parseInt(powerAttack.getText());
		} catch (NumberFormatException e) {
			// TODO pop up message?
		}
		if (value < 0) value = 0;
		if (value > attacks.getBAB()) value = attacks.getBAB();
		if (value > 0) {
			totalDefense.setSelected(false);
			attacks.setTotalDefense(false);
		}
		attacks.setPowerAttack(value);
	}

	protected void updateCombatExpertise() {
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

	protected JPanel getBaseValuesPanel() {
		JPanel top = new JPanel();
		top.setLayout(new GridBagLayout());

		BAB = new BoundIntegerField(character, Creature.PROPERTY_BAB, 3);
		updateLabels();

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = 0; c.gridy = 0;
		c.gridheight = 2;
		top.add(new JLabel("BAB:"),c);

		c.gridx = 1;
		top.add(BAB,c);

		c.gridx = 2; c.gridheight = 1; c.anchor = GridBagConstraints.LINE_END;
		top.add(new JLabel("Str Mod: "),c);

		c.gridx = 3;
		top.add(strLabel,c);

		c.gridx = 4;
		top.add(new JLabel("Melee Attack: "),c);

		c.gridx = 5;
		top.add(meleeLabel,c);

		c.gridx = 2; c.gridy = 1;
		top.add(new JLabel("Dex Mod: "),c);

		c.gridx = 3;
		top.add(dexLabel,c);

		c.gridx = 4;
		top.add(new JLabel("Ranged Attack: "),c);

		c.gridx = 5;
		top.add(rangedLabel,c);

		return top;
	}

	protected JPanel getAttackOptionsPanel() {
		JPanel panel = new JPanel();

		panel.add(fightingDefensively);
		fightingDefensively.setSelected(attacks.isFightingDefensively());

		panel.add(totalDefense);
		totalDefense.setSelected(attacks.isTotalDefense());

		if (character.hasFeat(Feat.FEAT_POWER_ATTACK)) {
			panel.add(new JLabel("Power Attack: "));
			panel.add(powerAttack);
			powerAttack.setText(""+attacks.getPowerAttack());
		}

		if (character.hasFeat(Feat.FEAT_COMBAT_EXPERTISE)) {
			panel.add(new JLabel("Combat Expertise: "));
			panel.add(combatExpertise);
			combatExpertise.setText(""+attacks.getCombatExpertise());
		}

		return panel;
	}
	protected JPanel getWeaponPanel() {
		JPanel bottom = new JPanel();
		bottom.setLayout(new GridBagLayout());
		bottom.setBorder(BorderFactory.createTitledBorder("Weapons"));

		JButton newButton = new JButton("New");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Attacks.AttackForm a = attacks.addAttackForm();
				weaponList.setSelectedValue(a, true);
			}
		});

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Attacks.AttackForm a = (Attacks.AttackForm)weaponList.getSelectedValue();
				if (a != null) {
					attacks.removeAttackForm(a);
					attackPanel.setAttackForm((Attacks.AttackForm)weaponList.getSelectedValue());
				}
			}
		});

		JButton upButton = new JButton("/\\");
		upButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = weaponList.getSelectedIndex();
				if (i >= 1) {
					attacks.moveAttackForm(i, i-1);
					weaponList.setSelectedIndex(i-1);
				}
			}
		});

		JButton downButton = new JButton("\\/");
		downButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = weaponList.getSelectedIndex();
				if (i != -1 && i < attacks.getAttackFormsCount()-1) {
					attacks.moveAttackForm(i, i+1);
					weaponList.setSelectedIndex(i+1);
				}
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

		weaponList = new JList(attacks.getAttackFormsListModel());
		weaponList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		weaponList.setVisibleRowCount(6);
		weaponList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					attackPanel.setAttackForm((Attacks.AttackForm)weaponList.getSelectedValue());
				}
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

	protected void updateLabels() {
		strLabel.setText(""+character.getAbilityModifier(AbilityScore.Type.STRENGTH));
		String melee = attacks.getValue()+(attacks.hasConditionalModifier()?"*":"");
		if (attacks.isTotalDefense()) {
			melee = "<html><body><s>"+melee+"</s></body></html>";
		}
		meleeLabel.setText(melee);
		dexLabel.setText(""+character.getAbilityModifier(AbilityScore.Type.DEXTERITY));
		String ranged = attacks.getRangedValue()+(attacks.hasConditionalModifier()?"*":"");
		if (attacks.isTotalDefense()) {
			ranged = "<html><body><s>"+ranged+"</s></body></html>";
		}
		rangedLabel.setText(ranged);
	}

	protected void updateToolTip() {
		StringBuilder text = new StringBuilder();
		text.append("<html><body>");
		text.append(attacks.getBAB()).append(" base attack bonus<br/>");
		Map<Modifier, Boolean> mods = attacks.getModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(attacks.getValue()).append(" total");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		meleeLabel.setToolTipText(text.toString());

		text = new StringBuilder();
		text.append("<html><body>");
		text.append(attacks.getBAB()).append(" base attack bonus<br/>");
		mods = attacks.getRangedModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(attacks.getRangedValue()).append(" total");
		conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/><br/>").append(conds);
		text.append("</body></html>");
		rangedLabel.setToolTipText(text.toString());
	}

	protected String getSummary() {
		StringBuilder s = new StringBuilder();
		s.append("Melee ");
		s.append(attacks.getAttacksDescription(attacks.getValue()));
		s.append("   Ranged ");
		s.append(attacks.getAttacksDescription(attacks.getRangedValue()));
		return s.toString();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+AbilityScore.Type.STRENGTH.toString())
				|| e.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+AbilityScore.Type.DEXTERITY.toString())
				|| e.getPropertyName().equals(Creature.PROPERTY_BAB)
				) {
			updateLabels();
			updateToolTip();
			updateSummaries(getSummary());
		}
	}

}

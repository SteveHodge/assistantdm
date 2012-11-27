package ui;

import gamesystem.HPs;
import gamesystem.Modifier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import party.Character;
import party.Creature;

// TODO probably don't want both damage and healing to be applied when user hits the apply button. either clear the other value when one value is changed or clear both fields after apply 

@SuppressWarnings("serial")
public class CharacterHitPointPanel extends CharacterSubPanel implements PropertyChangeListener {
	HPs hps;
	JFormattedTextField currHP;
	JFormattedTextField dmgField = new JFormattedTextField(0);
	JFormattedTextField healField = new JFormattedTextField(0);
	JCheckBox nonLethal = new JCheckBox("Non-lethal");

	public CharacterHitPointPanel(Character chr) {
		super(chr);
		hps = (HPs)chr.getStatistic(Creature.STATISTIC_HPS);

		setLayout(new GridBagLayout());

		summary = ""+character.getHPs()+" / "+character.getMaximumHitPoints();

		currHP = new JFormattedTextField();
		currHP.setValue(new Integer(character.getHPs()));
		currHP.setColumns(3);
		currHP.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("value")) {
					int total = (Integer)currHP.getValue();
					if (total != character.getHPs()) {
						character.setWounds(character.getMaximumHitPoints()-character.getNonLethal()-total);
					}
				}
			}
		});

		TempHPModel tempHPModel = new TempHPModel();
		JTable tempTable = new JTable(tempHPModel);

		JButton apply = new JButton("Apply");
		apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int dmg = (Integer)dmgField.getValue();
				if (nonLethal.isSelected()) {
					hps.applyNonLethal(dmg);
				} else {
					hps.applyDamage(dmg);
				}

				int heal = (Integer)healField.getValue();
				hps.applyHealing(heal);
			}
		});
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1,2,1,2);
		c.gridx = 0;
		c.gridwidth = 2; c.gridheight = 1;
		c.weightx = 0.0; c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0; add(new JLabel("Current Hitpoints:"),c);
		c.gridy++; add(new JLabel("Maximum Hitpoints:"), c);
		c.gridy++; add(new JLabel("Wounds:"), c);
		c.gridy++; add(new JLabel("Non-lethal:"), c);
		c.gridwidth = 3;
		c.gridy++; add(new JSeparator(SwingConstants.HORIZONTAL), c);
		c.gridwidth = 1;
		c.gridy++; add(new JLabel("Damage:"), c);
		c.gridy++; add(new JLabel("Healing:"), c);

		c.gridx = 1;
		c.gridy = 5; add(dmgField,c);
		c.gridy++; add(healField,c);

		c.gridx = 2;
		c.gridy = 0; add(currHP,c);
		c.gridy++; add(new BoundIntegerField(character, Creature.PROPERTY_MAXHPS, 3), c);
		c.gridy++; add(new BoundIntegerField(character, Creature.PROPERTY_WOUNDS, 3), c);
		c.gridy++; add(new BoundIntegerField(character, Creature.PROPERTY_NONLETHAL, 3), c);
		c.gridy = 5; add(nonLethal,c);
		c.gridy++; add(apply,c);

		c.gridx = 3; c.gridy = 0;
		c.weightx = 1.0; c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 7;
		JScrollPane scroller = new JScrollPane(tempTable);
		scroller.setPreferredSize(new Dimension(50,50));
		scroller.setBorder(BorderFactory.createTitledBorder("Temporary Hitpoints"));
		add(scroller, c);

		// update fields when character changes
		character.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Creature.PROPERTY_MAXHPS)
				|| e.getPropertyName().equals(Creature.PROPERTY_WOUNDS)
				|| e.getPropertyName().equals(Creature.PROPERTY_NONLETHAL)
				|| e.getPropertyName().equals(Creature.PROPERTY_HPS)) {
			currHP.setValue(new Integer(character.getHPs()));
			updateSummaries(""+character.getHPs()+" / "+character.getMaximumHitPoints());
		}
	}

	// this implementation uses a read-only list of modifiers supplied by the HPs statistic. it doesn't do any
	// work to figure out what has changed when the HPs sends an event, it just rebuilds the whole model.
	// TODO consider tracking changes more closely
	// TODO temporary hitpoints should probably be editable
	protected class TempHPModel extends AbstractTableModel {
		List<Modifier> tempHPs;

		public TempHPModel() {
			hps.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent arg0) {
					updateModel();
				}
			});
			updateModel();
		}

		protected void updateModel() {
			tempHPs =  hps.getTemporaryHPsModifiers();
			fireTableDataChanged();
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return tempHPs.size();
		}

		public Class<?> getColumnClass(int col) {
			if (col == 0) return String.class;
			if (col == 1) return Integer.class;
			return super.getColumnClass(col);
		}

		public String getColumnName(int col) {
			if (col == 0) return "Source";
			if (col == 1) return "Remaining";
			return super.getColumnName(col);
		}

		public Object getValueAt(int row, int col) {
			Modifier m = tempHPs.get(row);
			if (col == 0) return m.getSource();
			if (col == 1) return m.getModifier();
			return null;
		}
	}
}

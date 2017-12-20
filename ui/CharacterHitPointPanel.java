package ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import gamesystem.Creature;
import gamesystem.HPs;
import gamesystem.Modifier;
import party.Character;

// TODO probably don't want both damage and healing to be applied when user hits the apply button. either clear the other value when one value is changed or clear both fields after apply

@SuppressWarnings("serial")
class CharacterHitPointPanel extends CharacterSubPanel {
	private HPs hps;
	private JFormattedTextField currHP;
	private JFormattedTextField dmgField = new JFormattedTextField(0);
	private JFormattedTextField healField = new JFormattedTextField(0);
	private JCheckBox nonLethal = new JCheckBox("Non-lethal");

	CharacterHitPointPanel(Character chr) {
		super(chr);
		hps = chr.getHPStatistic();

		setLayout(new GridBagLayout());

		summary = "" + hps.getHPs() + " / " + hps.getMaxHPStat().getValue();

		currHP = new JFormattedTextField();
		currHP.setValue(new Integer(hps.getHPs()));
		currHP.setColumns(3);
		currHP.addPropertyChangeListener("value", evt -> {
			if (evt.getPropertyName().equals("value")) {
				int total = (Integer)currHP.getValue();
				if (total != hps.getHPs()) {
					hps.setWounds(hps.getMaxHPStat().getValue() - hps.getNonLethal() - total);
				}
			}
		});

		TempHPModel tempHPModel = new TempHPModel();
		JTable tempTable = new JTable(tempHPModel);

		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> {
			int dmg = (Integer)dmgField.getValue();
			if (nonLethal.isSelected()) {
				hps.applyNonLethal(dmg);
			} else {
				hps.applyDamage(dmg);
			}
			dmgField.setValue(0);

			int heal = (Integer)healField.getValue();
			hps.applyHealing(heal);
			healField.setValue(0);
		});

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1,2,1,2);
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0; c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0; add(new JLabel("Current Hitpoints:"),c);
		c.gridy++; add(new JLabel("Maximum Hitpoints:"), c);
		c.gridy++; add(new JLabel("Wounds:"), c);
		c.gridy++; add(new JLabel("Non-lethal:"), c);

		c.gridx = 1;
		c.gridy = 0; add(currHP,c);
		c.gridy++;
		// FIXME replace with BoundIntegerField once Max HPs are a statistic
		add(new JFormattedTextField() {
			{
				addPropertyChangeListener("value", evt -> {
					if (evt.getPropertyName().equals("value")) {
						Integer val = (Integer) getValue();
						if (val != null && !val.equals(hps.getMaxHPStat().getValue())) {
							hps.getMaxHPStat().setMaximumHitPoints(val);
						}
					}
				});
				hps.addPropertyListener((source, old) -> {
					//it's ok to do this even if this change event is due to an update from this control
					//because setValue will not fire a change event if the property isn't actually changing
					setValue(hps.getMaxHPStat().getValue());
				});
				setColumns(3);
				setValue(hps.getMaxHPStat().getValue());
			}
		}, c);
		c.gridy++;
		// TODO replace with BoundIntegerField once wounds are a statistic
		add(new JFormattedTextField() {
			{
				addPropertyChangeListener("value", evt -> {
					if (evt.getPropertyName().equals("value")) {
						Integer val = (Integer) getValue();
						if (val != null && !val.equals(hps.getWounds())) {
							hps.setWounds(val);
						}
					}
				});
				hps.addPropertyListener((source, old) -> {
					//it's ok to do this even if this change event is due to an update from this control
					//because setValue will not fire a change event if the property isn't actually changing
					setValue(hps.getWounds());
				});
				setColumns(3);
				setValue(hps.getWounds());
			}
		}, c);
		c.gridy++;
		add(new JFormattedTextField() {
			{
				addPropertyChangeListener("value", evt -> {
					if (evt.getPropertyName().equals("value")) {
						Integer val = (Integer) getValue();
						if (val != null && !val.equals(hps.getNonLethal())) {
							hps.setNonLethal(val);
						}
					}
				});
				hps.addPropertyListener((source, old) -> {
					//it's ok to do this even if this change event is due to an update from this control
					//because setValue will not fire a change event if the property isn't actually changing
					setValue(hps.getNonLethal());
				});
				setColumns(3);
				setValue(hps.getNonLethal());
			}
		}, c);

		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 1.0; c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 4;
		JScrollPane scroller = new JScrollPane(tempTable);
		scroller.setPreferredSize(new Dimension(50,50));
		scroller.setBorder(BorderFactory.createTitledBorder("Temporary Hitpoints"));
		add(scroller, c);

		scroller.addMouseListener(rightClickListener);
		addMouseListener(rightClickListener);
		tempTable.addMouseListener(rightClickListener);

		// update fields when character changes
		character.addPropertyListener("hit_points", (source, old) -> {
			currHP.setValue(new Integer(hps.getHPs()));
			updateSummaries("" + hps.getHPs() + " / " + hps.getMaxHPStat().getValue());
		});
	}

	private MouseListener rightClickListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (!SwingUtilities.isRightMouseButton(e)) return;
			StatisticInfoDialog dialog = new StatisticInfoDialog(CharacterHitPointPanel.this, "Hitpoints", character, Creature.STATISTIC_HPS);
			dialog.setVisible(true);
		}
	};

	// this implementation uses a read-only list of modifiers supplied by the HPs statistic. it doesn't do any
	// work to figure out what has changed when the HPs sends an event, it just rebuilds the whole model.
	// TODO consider tracking changes more closely
	// TODO temporary hitpoints should probably be editable
	private class TempHPModel extends AbstractTableModel {
		List<Modifier> tempHPs;

		private TempHPModel() {
			hps.addPropertyListener((source, oldValue) -> updateModel());
			updateModel();
		}

		private void updateModel() {
			tempHPs =  hps.getTemporaryHPsModifiers();
			fireTableDataChanged();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return tempHPs.size();
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) return String.class;
			if (col == 1) return Integer.class;
			return super.getColumnClass(col);
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) return "Source";
			if (col == 1) return "Remaining";
			return super.getColumnName(col);
		}

		@Override
		public Object getValueAt(int row, int col) {
			Modifier m = tempHPs.get(row);
			if (col == 0) return m.getSource();
			if (col == 1) return m.getModifier();
			return null;
		}
	}
}

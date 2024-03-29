package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

// TODO allow switching characters?

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import gamesystem.AbilityScore;
import gamesystem.Creature;
import gamesystem.HPs;
import gamesystem.Sanity;
import gamesystem.core.Property;
import gamesystem.core.PropertyEvent;
import gamesystem.core.PropertyListener;
import party.Character;
import swing.JTableWithToolTips;
import swing.SpinnerCellEditor;
import swing.TableModelWithToolTips;

// TODO should probably listen on the character for relevant changes

@SuppressWarnings("serial")
public class CharacterDamagePanel extends JPanel {
	HPPanel hpPanel;

	public CharacterDamagePanel(Character chr) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		hpPanel = new HPPanel(chr);
		add(hpPanel);

		SanityPanel sanityPanel = new SanityPanel(chr.getSanity(), chr.getAbilityStatistic(AbilityScore.Type.WISDOM));
		add(sanityPanel);

		hpPanel.updateSummary();
		sanityPanel.updateSummary();
	}

	public static void openDialog(JComponent parent, String title, Character chr) {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title + " - " + chr.getName());
		CharacterDamagePanel panel = new CharacterDamagePanel(chr);
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(parent));
		dialog.setVisible(true);
		panel.hpPanel.dmgField.grabFocus();
	}

	static class SanityPanel extends JPanel {
		Sanity sanity;
		AbilityScore wisdom;
		JFormattedTextField dmgField;
		JFormattedTextField healField;
		JLabel maxLabel = new JLabel();
		JLabel startingLabel = new JLabel();
		JLabel currentLabel = new JLabel();
		JLabel recentLabel = new JLabel();
		JLabel messageLabel = new JLabel();

		public SanityPanel(Sanity s, AbilityScore wis) {
			sanity = s;
			wisdom = wis;
			setBorder(BorderFactory.createTitledBorder("Sanity"));

			setLayout(new GridBagLayout());

			dmgField = new JFormattedTextField(0);
			dmgField.setColumns(3);
			dmgField.addPropertyChangeListener(e -> updateSummary());
			healField = new JFormattedTextField(0);
			healField.addPropertyChangeListener(e -> updateSummary());
			healField.setColumns(3);

			JButton apply = new JButton("Apply");
			apply.addActionListener(e -> {
				int dmg = (Integer) dmgField.getValue();
				if (dmg > 0) sanity.applyDamage(dmg);
				dmgField.setValue(0);

				int heal = (Integer) healField.getValue();
				if (heal > 0) sanity.applyHealing(heal);
				healField.setValue(0);

				updateSummary();
			});

			JButton healAll = new JButton("Heal All");
			healAll.addActionListener(e -> {
				int healing = sanity.getStartingSanityProperty().getValue() - sanity.getValue();
				if (healing > 0) sanity.applyHealing(healing);
				dmgField.setValue(0);
				healField.setValue(0);
				updateSummary();
			});

			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(1, 2, 1, 2);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.0;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = GridBagConstraints.RELATIVE;
			c.gridy = 0;
			add(new JLabel("Max Sanity:"), c);
			add(maxLabel, c);
			add(new JLabel("Starting Sanity:"), c);
			add(startingLabel, c);

			c.gridy = 1;
			add(new JLabel("Current Sanity:"), c);
			add(currentLabel, c);
			add(new JLabel("Lost Recently:"), c);
			add(recentLabel, c);

			c.gridy = 2;
			add(new JLabel("Damage:"), c);
			add(dmgField, c);
			c.gridwidth = 2;
			add(apply, c);

			c.gridy = 3;
			c.gridwidth = 1;
			add(new JLabel("Healing:"), c);
			add(healField, c);
			c.gridwidth = 2;
			add(healAll, c);

			c.gridy = 4;
			c.gridwidth = 4;
			add(messageLabel, c);
		}

		void updateSummary() {
			int dmg = (Integer) dmgField.getValue();
			int starting = sanity.getStartingSanityProperty().getValue();
			int current = sanity.getValue() - dmg + (Integer) healField.getValue();
			if (current > starting) current = starting;
			int session = sanity.getSessionStartingSanity() - current;

			maxLabel.setText(Integer.toString(sanity.getMaximumSanityProperty().getValue()));
			startingLabel.setText(Integer.toString(starting));
			currentLabel.setText(Integer.toString(current));
			recentLabel.setText(Integer.toString(session));

			if (current <= -10) {
				messageLabel.setText("Permanent insanity triggered");
			} else if (session > sanity.getMaximumSanityProperty().getValue() / 5) {
				messageLabel.setText("Indefinite insanity triggered");
			} else if (dmg >= wisdom.getValue() / 2) {
				messageLabel.setText("Pass sanity check or temporary insanity");
			} else {
				messageLabel.setText(" ");
			}
		}
	}

	public static class HPPanel extends JPanel {
		HPs hps;
		JFormattedTextField dmgField;
		JFormattedTextField healField;
		JCheckBox nonLethal = new JCheckBox("Non-lethal");
		JLabel summary;
		JLabel drLabel;

		public HPPanel(Creature creature) {
			hps = creature.getHPStatistic();
			hps.addPropertyListener(e -> updateSummary());

			setBorder(BorderFactory.createTitledBorder("Hit Points"));

			setLayout(new GridBagLayout());

			summary = new JLabel();
			dmgField = new JFormattedTextField(0) {
				@Override
				protected void processFocusEvent(FocusEvent e) {
					super.processFocusEvent(e);
					if (e.isTemporary())
						return;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							selectAll();
						}
					});
				}
			};
			dmgField.setColumns(3);
			dmgField.addPropertyChangeListener(e -> updateSummary());
			healField = new JFormattedTextField(0);
			healField.addPropertyChangeListener(e -> updateSummary());
			healField.setColumns(3);
			nonLethal.addChangeListener(e -> updateSummary());

			// TODO we track the value of the property if it has one when the panel is created but we don't handle changes from value to no-value or vice versa properly
			Property<?> drProp = creature.getProperty("extra." + Character.PROPERTY_DAMAGE_REDUCTION);
			if (drProp != null && drProp.getValue() != null && drProp.getValue().toString().length() > 0) {
				drLabel = new JLabel("DR: " + drProp.getValue());
				drProp.addPropertyListener(e -> {
					drLabel.setText("DR:" + e.source.getValue());
				});
			} else {
				drLabel = null;
			}

			JButton apply = new JButton("Apply");
			apply.addActionListener(e -> {
				int dmg = (Integer) dmgField.getValue();
				if (nonLethal.isSelected()) {
					hps.applyNonLethal(dmg);
				} else {
					hps.applyDamage(dmg);
				}
				dmgField.setValue(0);

				int heal = (Integer) healField.getValue();
				hps.applyHealing(heal);
				healField.setValue(0);
			});

			JButton healAll = new JButton("Heal All");
			healAll.addActionListener(e -> {
				hps.applyHealing(Math.max(hps.getWounds(), hps.getNonLethal()));
				dmgField.setValue(0);
				healField.setValue(0);
				updateSummary();
			});

			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(1, 2, 1, 2);
			c.gridx = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.0;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridy = 0;
			add(new JLabel("New Hit Points:"), c);
			c.gridy++;
			if (drLabel != null) {
				add(drLabel, c);
				c.gridy++;
			}
			add(new JLabel("Damage:"), c);
			c.gridy++;
			add(new JLabel("Healing:"), c);

			c.gridx = 1;
			c.gridy = 0;
			add(summary, c);
			if (drLabel != null)
				c.gridy++;
			c.gridy++;
			add(dmgField, c);
			c.gridy++;
			add(healField, c);

			c.gridx = 2;
			c.gridy = 0;
			if (drLabel != null)
				c.gridy++;
			add(apply, c);
			c.gridy++;
			add(nonLethal, c);
			c.gridy++;
			add(healAll, c);
		}

		void updateSummary() {
			int current = hps.getHPs();
			int newDmg = 0;
			int newNL = hps.getNonLethal();
			if (!nonLethal.isSelected()) {
				newDmg = (Integer) dmgField.getValue();
				current -= newDmg;
			} else {
				newNL += (Integer) dmgField.getValue();
			}
			newNL -= (Integer) healField.getValue();
			if (newNL < 0) newNL = 0;
			current += Math.min((Integer) healField.getValue(), Math.max(0, hps.getWounds() - (hps.getTemporaryHPs() - newDmg)));	// this should account for the effects of healing including temporary hitpoints and damage done simultaneously

			StringBuilder text = new StringBuilder();
			text.append("<html><body>").append(current);
			if (newNL > 0) text.append(" (").append(newNL).append(" NL)");
			text.append(" / ").append(hps.getMaxHPStat().getValue()).append("</body></html>");
			summary.setText(text.toString());
			revalidate();
		}
	}

	public static class AbilityDamagePanel extends JPanel {
		private AbilityTableModel abilityModel;
		private Creature creature;

		public AbilityDamagePanel(Creature c) {
			creature = c;

			abilityModel = new AbilityTableModel();

			final JTable abilityTable = new JTableWithToolTips(abilityModel);
			abilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			abilityTable.getColumnModel().getColumn(0).setPreferredWidth(80);
			abilityTable.getColumnModel().getColumn(1).setPreferredWidth(20);
			abilityTable.getColumnModel().getColumn(2).setPreferredWidth(20);
			abilityTable.getColumnModel().getColumn(3).setPreferredWidth(20);
			abilityTable.setDefaultEditor(Integer.class, new SpinnerCellEditor() {
				@Override
				public Component getTableCellEditorComponent(JTable table,
						Object value, boolean isSelected, int row, int column) {
					if (value == null) {
						value = abilityModel.getAbility(row).getValue();
					}
					return super.getTableCellEditorComponent(table, value, isSelected, row, column);
				}
			});
			abilityTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (!SwingUtilities.isRightMouseButton(e)) return;
					int row = abilityTable.rowAtPoint(e.getPoint());
					String title = abilityModel.getAbilityName(row);
					String statName = abilityModel.getStatistic(row);
					StatisticInfoDialog dialog = new StatisticInfoDialog(AbilityDamagePanel.this, title, creature, statName);
					dialog.setVisible(true);
				}
			});
			JScrollPane abilityScrollpane = new JScrollPane(abilityTable);

			setLayout(new BorderLayout());
			add(abilityScrollpane);

			setPreferredSize(new Dimension(300, 75));
			setBorder(BorderFactory.createTitledBorder("Ability Damage/Drain"));
		}

		private class AbilityTableModel extends AbstractTableModel implements TableModelWithToolTips {
			public AbilityTableModel() {
				creature.addPropertyListener("ability_scores", new PropertyListener() {
					@Override
					public void propertyChanged(PropertyEvent e) {
						// this is a bit hackish as there is currently no good way to find the ability score or type from the drain and damage properties
						for (int i = 0; i < 6; i++) {
							AbilityScore a = getAbility(i);
							if (a != null && (a == e.source || a.getDamage() == e.source || a.getDrain() == e.source)) {
								fireTableRowsUpdated(i, i);
							}
						}
					}
				});
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				if (getAbility(rowIndex) == null) return false;
				return columnIndex >= 1 && columnIndex <= 2;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0) return String.class;
				return Integer.class;
			}

			@Override
			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				if (columnIndex < 1 || columnIndex > 2) return;
				if (value == null) value = new Integer(0);
				AbilityScore a = getAbility(rowIndex);
				if (columnIndex == 1) {
					if ((Integer) value < 0) value = new Integer(0);
					a.getDrain().setValue((Integer) value);
				} else if (columnIndex == 2) {
					if ((Integer) value < 0) value = new Integer(0);
					a.getDamage().setValue((Integer) value);
				}
			}

			@Override
			public String getColumnName(int column) {
				if (column == 0) return "Ability";
				if (column == 1) return "Drain";
				if (column == 2) return "Damage";
				if (column == 3) return "Current";
				return super.getColumnName(column);
			}

			@Override
			public int getColumnCount() {
				return 4;
			}

			@Override
			public int getRowCount() {
				return 6;
			}

			private String getAbilityName(int row) {
				return AbilityScore.Type.values()[row].toString();
			}

			private String getStatistic(int row) {
				return Creature.STATISTIC_ABILITY[row];
			}

			public AbilityScore getAbility(int row) {
				return creature.getAbilityStatistic(AbilityScore.Type.values()[row]);
			}

			@Override
			public Object getValueAt(int row, int column) {
				if (column == 0) return getAbilityName(row);
				AbilityScore a = getAbility(row);
				if (a == null) return null;
				if (column == 1) return a.getDrain().getValue();
				if (column == 2) return a.getDamage().getValue();
				if (column == 3) {
					return a.getValue() + ((a.hasConditionalModifier() && a.getOverride() == 0) ? "*" : "");
				}
				return null;
			}

			@Override
			public String getToolTipAt(int row, int col) {
				StringBuilder text = new StringBuilder();
				AbilityScore a = getAbility(row);
				if (a == null)
					text.append("<html><body>No ").append(AbilityScore.Type.values()[row].toString()).append("</body></html>");
				else
					text.append("<html><body>").append(a.getSummary()).append("</body></html>");
				return text.toString();
			}
		}
	}
}

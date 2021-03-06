package ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import gamesystem.AbilityScore;
import gamesystem.Creature;
import gamesystem.SkillType;
import gamesystem.Skills;
import gamesystem.Skills.Skill;
import gamesystem.Statistic;
import party.Character;
import swing.JTableWithToolTips;
import swing.SpinnerCellEditor;
import swing.TableModelWithToolTips;

// TODO should allow adding of new skills and setting the ability
// TODO need to add named skills (profession, craft, etc)
// TODO should have column with other modifiers

@SuppressWarnings("serial")
class CharacterSkillsPanel extends JPanel {
	private Character character;

	CharacterSkillsPanel(Character chr) {
		character = chr;

		final SkillsTableModel model = new SkillsTableModel();
		final JTable table = new JTableWithToolTips(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane skillsScrollpane = new JScrollPane(table);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!SwingUtilities.isRightMouseButton(e)) return;
				int row = table.rowAtPoint(e.getPoint());
				String title = model.getSkillName(row);
				String statName = Creature.STATISTIC_SKILLS + "." + title;
				SkillsInfoDialog dialog = new SkillsInfoDialog(CharacterSkillsPanel.this, title, character, statName);
				dialog.setVisible(true);
			}
		});

		JButton addButton = new JButton("+");
		addButton.addActionListener(e -> addSkill());

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.WEST;
		add(addButton, c);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 0;
		add(skillsScrollpane, c);
	}

	void addSkill() {
		List<SkillType> available = new ArrayList<>();
		Set<SkillType> existing = character.getSkills();
		for (SkillType t : SkillType.skills.values()) {
			if (!existing.contains(t)) available.add(t);
		}
		Collections.sort(available);
		SkillType s = (SkillType) JOptionPane.showInputDialog(this, "Selected skill to add:", "Add Skill", JOptionPane.PLAIN_MESSAGE, null, available.toArray(), null);
		character.setSkillRanks(s, 1);
	}

	private class SkillsInfoDialog extends StatisticInfoDialog {
		SkillsInfoDialog(JComponent parent, String title, Character chr, final String statName) {
			super(parent, title);

			initialize(chr, statName);

			JPanel skillsAdhocPanel = getAdhocPanel(Creature.STATISTIC_SKILLS);
			skillsAdhocPanel.setBorder(BorderFactory.createTitledBorder("Adhoc Modifier for all skills"));

			Statistic allSkills = chr.getStatistic(Creature.STATISTIC_SKILLS);
			allSkills.addPropertyListener(e -> updateSummary());

			addPanel.setBorder(BorderFactory.createTitledBorder("Adhoc Modifier for " + title));

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			add(summary, c);

			c.gridy++;
			c.weighty = 0;
			add(addPanel, c);

			c.gridy++;
			add(skillsAdhocPanel, c);

			c.gridy++;
			c.fill = GridBagConstraints.NONE;
			c.insets = new Insets(2, 4, 2, 4);
			add(okButton, c);

			pack();
			setLocationRelativeTo(SwingUtilities.getWindowAncestor(parent));
		}

		@Override
		void updateSummary() {
			Skills s = (Skills) creature.getStatistic(Creature.STATISTIC_SKILLS);
			summary.setText("<html><body>" + s.getSummary(((Skill) statistic).getSkillType()) + "</body></html>");
			pack();
		}
	}

	private class SkillsTableModel extends AbstractTableModel implements TableModelWithToolTips {
		private SkillType[] skills;

		SkillsTableModel() {
			skills = new SkillType[character.getSkills().size()];
			character.getSkills().toArray(this.skills);
			character.getSkillsStatistic().addPropertyListener(e -> {
				if (e.source instanceof Skill) {
					Skill skill = (Skill) e.source;
					boolean found = false;
					for (int i = 0; i < skills.length; i++) {
						if (skills[i].equals(skill.getSkillType())) {
							fireTableRowsUpdated(i, i);
							found = true;
						}
					}
					if (!found) {
						SkillType[] newList = Arrays.copyOf(skills, skills.length + 1);
						newList[skills.length] = skill.getSkillType();
						skills = newList;
						Arrays.sort(skills);
						this.fireTableDataChanged();
					}
				}
			});
			Arrays.sort(skills);
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public int getRowCount() {
			return skills.length;
		}

		String getSkillName(int row) {
			return skills[row].getName();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) return skills[row];
			if (col == 1) {
				AbilityScore.Type ability = skills[row].getAbility();
				if (ability == null) return null;
				return ability.toString();
			}
			if (col == 2) return character.getSkillRanks(skills[row]);
			if (col == 3) {
				AbilityScore.Type ability = skills[row].getAbility();
				if (ability == null) return null;
				return character.getAbilityStatistic(ability).getModifierValue();
			}
			if (col == 4) {
				return character.skills.getModifiersTotal(skills[row], skills[row].getAbility().toString());
			}
			if (col == 5) {
				Skills s = (Skills)character.getStatistic(Creature.STATISTIC_SKILLS);
				return character.getSkillTotal(skills[row]) + (s.hasConditionalModifier(skills[row]) ? "*" : "");	// XXX should we show the * if there is a conditional modifier on all skills?
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0 || col == 1) return String.class;
			if (col == 2) return Float.class;
			return Integer.class;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) return "Skill";
			if (col == 1) return "Ability";
			if (col == 2) return "Ranks";
			if (col == 3) return "Modifier";
			if (col == 4) return "Misc";
			if (col == 5) return "Total";
			return super.getColumnName(col);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			if (col == 2) return true;
			return false;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col == 2 && value instanceof Float) {
				double v = Math.floor(((Float) value * 2)) / 2;	// round to nearest half a rank
				character.setSkillRanks(skills[row], (float) v);
				return;
			}
			super.setValueAt(value, row, col);
		}

		@Override
		public String getToolTipAt(int row, int col) {
			Skills s = (Skills)character.getStatistic(Creature.STATISTIC_SKILLS);
			StringBuilder text = new StringBuilder();
			text.append("<html><body>");
			text.append(s.getSummary(skills[row]));
			text.append("</body></html>");
			return text.toString();
		}
	}
}

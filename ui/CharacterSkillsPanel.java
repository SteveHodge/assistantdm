package ui;

import gamesystem.AbilityScore;
import gamesystem.SkillType;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Creature;
import party.Character;

import swing.SpinnerCellEditor;

// TODO should allow adding of new skills and setting the ability

@SuppressWarnings("serial")
public class CharacterSkillsPanel extends JPanel {
	protected Character character;

	public CharacterSkillsPanel(Character c) {
		character = c;

		TableModel model = new SkillsTableModel();
		JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane skillsScrollpane = new JScrollPane(table);

		setLayout(new BorderLayout());
		add(skillsScrollpane);
		setBorder(new TitledBorder("Skills"));
	}

	protected class SkillsTableModel extends AbstractTableModel implements PropertyChangeListener {
		SkillType[] skills;

		public SkillsTableModel() {
			skills = new SkillType[character.getSkills().size()];
			character.getSkills().toArray(this.skills);
			character.addPropertyChangeListener(this);
			Arrays.sort(skills);
		}

		public int getColumnCount() {
			return 6;
		}

		public int getRowCount() {
			return skills.length;
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) return skills[row];
			if (col == 1) {
				int ability = skills[row].getAbility();
				if (ability == -1) return null;
				return AbilityScore.getAbilityName(ability);
			}
			if (col == 2) return character.getSkillRanks(skills[row]);
			if (col == 3) {
				int ability = skills[row].getAbility();
				if (ability == -1) return null;
				return character.getAbilityModifier(ability);
			}
			if (col == 4) return character.getSkillMisc(skills[row]);
			if (col == 5) return character.getSkillTotal(skills[row]);
			return null;
		}

		public Class<?> getColumnClass(int col) {
			if (col == 0 || col == 1) return String.class;
			if (col == 2) return Float.class;
			return Integer.class;
		}

		public String getColumnName(int col) {
			if (col == 0) return "Skill";
			if (col == 1) return "Ability";
			if (col == 2) return "Ranks";
			if (col == 3) return "Modifier";
			if (col == 4) return "Misc";
			if (col == 5) return "Total";
			return super.getColumnName(col);
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 2 || col == 4) return true;
			return false;
		}

		public void setValueAt(Object arg0, int row, int col) {
			if (col == 2 && arg0 instanceof Float) {
				character.setSkillRanks(skills[row], ((Float)arg0).intValue());
				return;
			} else if (col ==4 && arg0 instanceof Integer) {
				character.setSkillMisc(skills[row], ((Integer)arg0).intValue());
				return;
			}
			super.setValueAt(arg0, row, col);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().startsWith(Creature.PROPERTY_SKILL_PREFIX)) {
				String skill = evt.getPropertyName().substring(Creature.PROPERTY_SKILL_PREFIX.length());
				// XXX this search is inefficient - we know that skills is sorted so could use a binary search
				boolean found = false;
				for (int i = 0; i < skills.length; i++) {
					if (skills[i].getName().equals(skill)) {
						fireTableRowsUpdated(i, i);
						found = true;
					}
				}
				if (!found) {
					SkillType[] newList = Arrays.copyOf(skills, skills.length+1);
					newList[skills.length] = SkillType.getSkill(skill);
					skills = newList;
					Arrays.sort(skills);
					this.fireTableDataChanged();
				}
			}
		}
	}
}

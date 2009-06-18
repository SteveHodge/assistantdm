package ui;

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
import party.Skill;

import swing.SpinnerCellEditor;

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
		String[] skills;

		public SkillsTableModel() {
			skills = new String[character.getSkillNames().size()];
			character.getSkillNames().toArray(this.skills);
			character.addPropertyChangeListener(this);
			Arrays.sort(skills);
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return skills.length;
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) return skills[row];
			if (col == 1) {
				int ability = Skill.getAbilityForSkill(skills[row]);
				if (ability == -1) return null;
				return Creature.getAbilityName(ability);
			}
			if (col == 2) return character.getSkillRanks(skills[row]);
			if (col == 3) {
				int ability = Skill.getAbilityForSkill(skills[row]);
				if (ability == -1) return null;
				return character.getAbilityModifier(ability);
			}
			if (col == 4) return character.getSkill(skills[row]);
			return null;
		}

		public Class<?> getColumnClass(int col) {
			if (col == 0 || col == 1) return String.class;
			return Integer.class;
		}

		public String getColumnName(int col) {
			if (col == 0) return "Skill";
			if (col == 1) return "Ability";
			if (col == 2) return "Ranks";
			if (col == 3) return "Modifier";
			if (col == 4) return "Total";
			return super.getColumnName(col);
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 2) return true;
			return false;
		}

		public void setValueAt(Object arg0, int row, int col) {
			if (col == 2 && arg0 instanceof Integer) {
				character.setSkill(skills[row], ((Integer)arg0).intValue());
				return;
			}
			super.setValueAt(arg0, row, col);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().startsWith(Creature.PROPERTY_SKILL_PREFIX)) {
				String skill = evt.getPropertyName().substring(5);
				// TODO this search is inefficient
				for (int i = 0; i < skills.length; i++) {
					if (skills[i].equals(skill)) {
						this.fireTableRowsUpdated(i, i);
					}
				}
			}
		}
	}
}

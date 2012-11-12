package ui;

import gamesystem.AbilityScore;
import gamesystem.Modifier;
import gamesystem.SkillType;
import gamesystem.Skills;
import gamesystem.Statistic;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import party.Creature;
import party.Character;

import swing.JTableWithToolTips;
import swing.SpinnerCellEditor;
import swing.TableModelWithToolTips;

// TODO should allow adding of new skills and setting the ability
// TODO need to add named skills (profession, craft, etc)
// TODO should have column with other modifiers

@SuppressWarnings("serial")
public class CharacterSkillsPanel extends JPanel {
	protected Character character;

	public CharacterSkillsPanel(Character c) {
		character = c;

		TableModel model = new SkillsTableModel();
		JTable table = new JTableWithToolTips(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		JScrollPane skillsScrollpane = new JScrollPane(table);

		setLayout(new BorderLayout());
		add(skillsScrollpane);
	}

	protected class SkillsTableModel extends AbstractTableModel implements PropertyChangeListener, TableModelWithToolTips {
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
				AbilityScore.Type ability = skills[row].getAbility();
				if (ability == null) return null;
				return ability.toString();
			}
			if (col == 2) return character.getSkillRanks(skills[row]);
			if (col == 3) {
				AbilityScore.Type ability = skills[row].getAbility();
				if (ability == null) return null;
				return character.getAbilityModifier(ability);
			}
			if (col == 4) return character.getSkillMisc(skills[row]);
			if (col == 5) {
				Skills s = (Skills)character.getStatistic(Creature.STATISTIC_SKILLS);
				return character.getSkillTotal(skills[row])+(s.hasConditionalModifier()?"*":"");
			}
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

		public String getToolTipAt(int row, int col) {
			Skills s = (Skills)character.getStatistic(Creature.STATISTIC_SKILLS);
			StringBuilder text = new StringBuilder();
			text.append("<html><body>");
			text.append(s.getRanks(skills[row])).append(" base<br/>");
			Map<Modifier, Boolean> mods = s.getModifiers(skills[row]);
			text.append(Statistic.getModifiersHTML(mods));
			text.append(s.getValue(skills[row])).append(" total ").append(skills[row].getName()).append("<br/>");
			String conds = Statistic.getModifiersHTML(mods, true);
			if (conds.length() > 0) text.append("<br/>").append(conds);
			text.append("</body></html>");
			return text.toString();
		}
	}
}

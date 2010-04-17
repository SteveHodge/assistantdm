package ui;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import party.Character;
import party.Creature;
import party.Party;
import party.PartyListener;
import party.Skill;
import swing.SpinnerCellEditor;

//TODO better layout
//TODO reset is a bit ugly - implement party changes and skill additions in a better way

// note that setting skills in the table modifies to skill to the newly specified total by changing the number
// of ranks. Setting the skill to 0 sets the ranks to 0.

@SuppressWarnings("serial")
public class RollsPanel extends JPanel implements PartyListener, PropertyChangeListener {
	static final int LAST_SAVE_ROW = 2;
	static final int FIRST_SKILL_ROW = 4;

	Party party;
	RollsTableModel model;

	public RollsPanel(Party p) {
		party = p;
		party.addPartyListener(this);
		for (Character c : party) {
			c.addPropertyChangeListener(this);
		}
		reset();
	}

	protected void reset() {
		removeAll();

		// build list of all skills. this should be maintained if skills are added later
		Set<Skill> skills = Skill.getUntrainedSkills();
		for (Character c : party) {
			skills.addAll(c.getSkills());
		}

		model = new RollsTableModel(skills);
		final JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		for (int i=LAST_SAVE_ROW+1; i<FIRST_SKILL_ROW; i++) {
			table.setRowHeight(i,5);
		}
		table.setRowHeight(model.getLastSkillRowIndex()+1,5);
		table.setSelectionModel(model.getSelectionModel());
		table.setDefaultEditor(Integer.class, new SpinnerCellEditor());

		JScrollPane scrollpane = new JScrollPane(table);

		JButton rollButton = new JButton("Roll");
		rollButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selection = table.getSelectedRows();
				for (int i = 0; i < selection.length; i++) {
					model.roll(table.convertRowIndexToModel(selection[i]));
				}
			}
		});
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.clear();
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(rollButton);
		buttonPanel.add(clearButton);

		setLayout(new BorderLayout());
		add(scrollpane);
		add(buttonPanel,"South");
	}

	public void characterAdded(Character c) {
		c.addPropertyChangeListener(this);
		reset();
	}

	public void characterRemoved(Character c) {
		c.removePropertyChangeListener(this);
		reset();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().startsWith(Creature.PROPERTY_SKILL_PREFIX)) {
			String skill = e.getPropertyName().substring(Creature.PROPERTY_SKILL_PREFIX.length());
			Skill s = Skill.getSkill(skill);
			if (!model.skillChange(s)) reset();
		} else if (e.getPropertyName().startsWith(Creature.PROPERTY_SAVE_PREFIX)) {
			String save = e.getPropertyName().substring(Creature.PROPERTY_SAVE_PREFIX.length());
			for (int i = 0; i < 3; i++) {
				if (save.equals(Creature.getSavingThrowName(i))) {
					model.saveChange(i);
				}
			}
		}
	}

	class RollsTableModel extends AbstractTableModel {
		Skill[] skills;
		String currentRollName = "";
		int[] currentRoll = new int[party.size()];
		int currentRollRow = -1;

		public RollsTableModel(Set<Skill> skills) {
			this.skills = new Skill[skills.size()];
			skills.toArray(this.skills);
			Arrays.sort(this.skills);
		}

		public boolean skillChange(Skill s) {
			boolean found = false;
			for (int i = FIRST_SKILL_ROW; i < getLastSkillRowIndex(); i++) {
				if (skills[i-FIRST_SKILL_ROW] == s) {
					found = true;
					fireTableRowsUpdated(i, i);
				}
			}
			return found;
		}

		public void saveChange(int s) {
			fireTableRowsUpdated(s, s);
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			return Integer.class;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) return false;
			if (rowIndex >= 0 && rowIndex <= LAST_SAVE_ROW) return true;
			if (rowIndex >= FIRST_SKILL_ROW && rowIndex <= getLastSkillRowIndex()) return true;
			if (rowIndex == getLastSkillRowIndex()+2 && currentRollRow != -1) return true;	// "rolled" row
			return false;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (rowIndex >= 0 && rowIndex <= LAST_SAVE_ROW) {
				if (value == null) value = new Integer(0);
				party.get(columnIndex-1).setSavingThrow(rowIndex, (Integer)value);
			} else if (rowIndex >= FIRST_SKILL_ROW && rowIndex <= getLastSkillRowIndex()) {
				if (value == null) party.get(columnIndex-1).setSkillRanks(skills[rowIndex-FIRST_SKILL_ROW], 0);
				else party.get(columnIndex-1).setSkillTotal(skills[rowIndex-FIRST_SKILL_ROW], (Integer)value);
			} else if (rowIndex == getLastSkillRowIndex()+2 && currentRollRow != -1) {
				currentRoll[columnIndex-1] = (Integer)value;
			} else {
				return;
			}
			// fire changes: the edited cell and the total rows
			fireTableCellUpdated(rowIndex, columnIndex);
			// +2 is the row after the gap row, i.e. the roll row, +3 is the total row
			fireTableRowsUpdated(getLastSkillRowIndex()+2, getLastSkillRowIndex()+3); 
		}

		public void roll(int rowIndex) {
			if (rowIndex < 0 
					|| (rowIndex > LAST_SAVE_ROW && rowIndex < FIRST_SKILL_ROW)
					|| (rowIndex > getLastSkillRowIndex())
				) {
				clear();
				return;
			}

			currentRollRow = rowIndex;
			currentRollName = getValueAt(rowIndex,0).toString();
			Skill s = skills[rowIndex-FIRST_SKILL_ROW];
			Random r = new Random();
			for (int i=0; i<currentRoll.length; i++) {
				if (s.isTrainedOnly() && party.get(i).getSkillRanks(s) == 0) {
					currentRoll[i] = 0;
				} else {
					currentRoll[i] = r.nextInt(20)+1;
				}
			}
			// +2 is the row after the gap row, i.e. the roll row, +3 is the total row
			fireTableRowsUpdated(getLastSkillRowIndex()+2, getLastSkillRowIndex()+3); 
		}

		public void clear() {
			currentRollRow = -1;
			currentRollName = "";
			for (int i=0; i<currentRoll.length; i++) {
				currentRoll[i] = 0;
			}
			// +2 is the row after the gap row, i.e. the roll row, +3 is the total row
			fireTableRowsUpdated(getLastSkillRowIndex()+2, getLastSkillRowIndex()+3); 
		}

		public int getColumnCount() {
			return party.size()+1;
		}

		public int getRowCount() {
			// next row has +4 because:
			// +1 for the last skill row
			// +1 for the blank row
			// +2 for the roll and total rows
			return getLastSkillRowIndex()+4;
		}

		protected String getRowName(int rowIndex) {
			if (rowIndex <= LAST_SAVE_ROW) {
				return Creature.getSavingThrowName(rowIndex);
			}
			if (rowIndex >= FIRST_SKILL_ROW && rowIndex <= getLastSkillRowIndex()) {
				return skills[rowIndex-FIRST_SKILL_ROW].getName();
			}
			if (rowIndex == getLastSkillRowIndex()+2) return "Rolled "+currentRollName;
			if (rowIndex == getLastSkillRowIndex()+3) return "Total "+currentRollName;
			return null;
		}

		protected int getLastSkillRowIndex() {
			return skills.length+FIRST_SKILL_ROW-1;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) return getRowName(rowIndex);

			if (rowIndex <= LAST_SAVE_ROW) {
				return party.get(columnIndex-1).getSavingThrow(rowIndex);
			}
			if (rowIndex > LAST_SAVE_ROW && rowIndex < FIRST_SKILL_ROW) return null;
			if (rowIndex >= FIRST_SKILL_ROW && rowIndex <= getLastSkillRowIndex()) {
				Skill s = skills[rowIndex-FIRST_SKILL_ROW];
				if (s.isTrainedOnly() && party.get(columnIndex-1).getSkillRanks(s) == 0) {
					// trained skill with no ranks
					return null;
				} else {
					return party.get(columnIndex-1).getSkillTotal(s);
				}
			}
			if (currentRollRow != -1) {
				if (rowIndex == getLastSkillRowIndex()+2) {
					if (currentRoll[columnIndex-1] == 0) return null;
					return currentRoll[columnIndex-1];
				}
				if (rowIndex == getLastSkillRowIndex()+3) {
					if (currentRoll[columnIndex-1] == 0) return null;
					Integer stat = (Integer)getValueAt(currentRollRow,columnIndex);
					if (stat == null) return currentRoll[columnIndex-1];
					return currentRoll[columnIndex-1] + stat;
				}
			}
			return null;
		}

		public String getColumnName(int column) {
			if (column == 0) return "";
			return party.get(column-1).getName();
		}

		// returns a ListSelectionModel that allow single selection of skills and saves only 
		public ListSelectionModel getSelectionModel() {
			return new DefaultListSelectionModel() {
				{
					super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				}

				public void addSelectionInterval(int index0, int index1) {
					setSelectionInterval(index0, index1);
				}

				// TODO this is probably in terms of the model when it should be in terms of the view
				// shouldn't matter as we shouldn't ever reorder rows
				public void setSelectionInterval(int index0, int index1) {
					if ((index1 > LAST_SAVE_ROW && index1 < FIRST_SKILL_ROW)
							|| (index1 > getLastSkillRowIndex())
						) {
						super.clearSelection();
					} else {
						super.setSelectionInterval(index0, index1);
					}
				}

				// overridden to do nothing: we always support only SINGLE_SELECTION mode
				public void setSelectionMode(int selectionMode) {
				}
				
			};
		}
	}
}

package ui;

import gamesystem.CharacterClass;
import gamesystem.Level;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import party.Character;

@SuppressWarnings("serial")
public class CharacterClassesPanel extends CharacterSubPanel {
	Level level;

	public CharacterClassesPanel(Character c) {
		super(c);
		level = (Level) character.getStatistic(Character.STATISTIC_LEVEL);
		summary = getSummary();

		ClassListTableModel model = new ClassListTableModel();
		JTable table = new JTable(model);
		JScrollPane scroller = new JScrollPane(table);
//		table.setFillsViewportHeight(true);
		//setLayout(new BorderLayout());

		TableColumn col = table.getColumnModel().getColumn(0);
		col.setPreferredWidth(60);
		col = table.getColumnModel().getColumn(1);
		col.setPreferredWidth(140);
		JComboBox<CharacterClass> classCombo = new JComboBox<>(CharacterClass.getCoreClasses());
		col.setCellEditor(new DefaultCellEditor(classCombo));

		int visibleRows = 7;
		if (model.getRowCount() > 5) visibleRows = 12;

		scroller.setPreferredSize(new Dimension(table.getPreferredSize().width, table.getRowHeight() * visibleRows));

		add(scroller);
	}

	private String getSummary() {
		// process class levels
		Map<String, Integer> classes = new HashMap<>();
		int defined = 0;
		for (int i = 1; i <= level.getLevel(); i++) {
			CharacterClass cls = level.getClass(i);
			if (cls != null) {
				defined++;
				if (classes.containsKey(cls.toString())) {
					classes.put(cls.toString(), classes.get(cls.toString()) + 1);
				} else {
					classes.put(cls.toString(), 1);
				}
			}
		}
		String[] ordered = classes.keySet().toArray(new String[0]);
		String classStr = "";
		if (ordered.length > 0) {
			Arrays.sort(ordered, (k1, k2) -> classes.get(k2) - classes.get(k1));

			for (int i = 0; i < ordered.length; i++) {
				ordered[i] += " " + classes.get(ordered[i]);
			}

			classStr = String.join("/", ordered);

			if (defined < level.getLevel()) {
				classStr += "/? " + (level.getLevel() - defined);
			}
		} else {
			classStr = "? " + Integer.toString(level.getLevel());
		}
		return classStr;
	}

	private class ClassListTableModel extends AbstractTableModel {
		int currentLevel;

		public ClassListTableModel() {
			currentLevel = level.getLevel();

			level.addPropertyChangeListener((e) -> {
				int old = currentLevel;
				currentLevel = character.getLevel();
				if (currentLevel > old) {
					fireTableRowsInserted(old - 1, currentLevel - 1);
				} else if (currentLevel < old) {
					fireTableRowsDeleted(currentLevel - 1, old - 1);
				}
				updateSummaries(getSummary());
			});
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) return Integer.class;
			if (col == 1) return CharacterClass.class;
			return super.getColumnClass(col);
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) return "Level";
			if (col == 1) return "Class";
			return super.getColumnName(col);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			if (col == 1) return true;
			return false;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col != 1 || row < 0 || row > currentLevel - 1) return;	// TODO throw exception?
			level.setClass(row + 1, (CharacterClass) value);
			updateSummaries(getSummary());
		}

		@Override
		public int getColumnCount() {
			return 2;	// level, class
		}

		@Override
		public int getRowCount() {
			return currentLevel;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) return row + 1;
			if (col == 1) return level.getClass(row + 1);
			return null;
		}

	}
}

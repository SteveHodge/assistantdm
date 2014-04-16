package monsters;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

// TODO move listeners to inner classes

@SuppressWarnings("serial")
public class MonstersPanel extends JPanel implements MouseListener {
	JTable table;
	MonstersTableModel monsters;
	TableRowSorter<MonstersTableModel> sorter;
	Map<JComponent, RowFilter<MonstersTableModel, Integer>> filters = new HashMap<>();
	Map<JComponent, Integer> filterCols = new HashMap<>();
	URL baseURL;

	public MonstersPanel() {
		File f = new File("html/monsters/");
		try {
			baseURL = f.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		monsters = new MonstersTableModel();
		// TODO remove hardcoded files here - probably best to use one global master or just scan for xml files
		monsters.parseXML(new File("html/monsters/monster_manual.xml"));
		monsters.parseXML(new File("html/monsters/monster_manual_ii.xml"));
		monsters.parseXML(new File("html/monsters/monster_manual_iii.xml"));
		monsters.parseXML(new File("html/monsters/ptolus.xml"));
		//filterModel = new FilterTableModel<>(monsters);
		table = new JTable(monsters);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(this);
		sorter = new TableRowSorter<>(monsters);
		table.setRowSorter(sorter);

		JScrollPane scrollpane = new JScrollPane(table);

		setLayout(new BorderLayout());
		add(scrollpane,BorderLayout.CENTER);
		add(createFilterPanel(),BorderLayout.NORTH);
	}

	private void newFilter(JTextField field) {
		RowFilter<MonstersTableModel, Integer> rf = null;
		int col = filterCols.get(field);
		try {
			rf = RowFilter.regexFilter("(?i)" + field.getText(), col);	// slight hack to force case insensitive matching
		} catch (java.util.regex.PatternSyntaxException e) {
			// if the expression doesn't parse then we ignore it
		}
		filters.put(field, rf);
		HashSet<RowFilter<MonstersTableModel, Integer>> set = new HashSet<>(filters.values());
		if (set.contains(null)) set.remove(null);
		sorter.setRowFilter(RowFilter.andFilter(set));
	}

	private void newFilter(JComboBox<String> combo) {
		if (combo.getSelectedItem().equals("")) {
			filters.remove(combo);
		} else {
			RowFilter<MonstersTableModel, Integer> rf = null;
			int col = filterCols.get(combo);
			try {
				rf = RowFilter.regexFilter("^"+combo.getSelectedItem().toString()+"$", col);	// XXX slight hack to force exact matching - could implement a more efficient filter
			} catch (java.util.regex.PatternSyntaxException e) {
				// if the expression doesn't parse then we ignore it
			}
			filters.put(combo, rf);
		}
		HashSet<RowFilter<MonstersTableModel, Integer>> set = new HashSet<>(filters.values());
		if (set.contains(null)) set.remove(null);
		sorter.setRowFilter(RowFilter.andFilter(set));

	}

	// TODO fix the use of Column.X.ordinal(): the fields should take the enum value not it's ordinal
	private JPanel createFilterPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;

		JTextField nameField = createFilterTextField(MonstersTableModel.Column.NAME.ordinal());
		panel.add(new JLabel("Name:"),c);
		c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(nameField,c);

//		JTextField sizeField = createFilterTextField(MonstersTableModel.COLUMN_SIZE);
		JComboBox<String> sizeField = createFilterCombo(MonstersTableModel.Column.SIZE.ordinal());
		c.gridx = 2; c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel("Size:"),c);
		c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(sizeField,c);

		JTextField typeField = createFilterTextField(MonstersTableModel.Column.TYPE.ordinal());
		c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel("Type:"),c);
		c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(typeField,c);

		JTextField environmentField = createFilterTextField(MonstersTableModel.Column.ENVIRONMENT.ordinal());
//		JComboBox environmentField = createFilterCombo(MonstersTableModel.COLUMN_ENVIRONMENT);
		c.gridx = 2; c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel("Environment:"),c);
		c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(environmentField,c);

//		JTextField crField = createFilterTextField(MonstersTableModel.COLUMN_CR);
		JComboBox<String> crField = createFilterCombo(MonstersTableModel.Column.CR.ordinal());
		c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel("CR:"),c);
		c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(crField,c);

//		JTextField sourceField = createFilterTextField(MonstersTableModel.COLUMN_SOURCE);
		JComboBox<String> sourceField = createFilterCombo(MonstersTableModel.Column.SOURCE.ordinal());
		c.gridx = 2; c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel("Source:"),c);
		c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(sourceField,c);

		return panel;
	}

	private JComboBox<String> createFilterCombo(int col) {
		HashSet<String> optionSet = new HashSet<>();
		for (int row = 0; row < monsters.getRowCount(); row++) {
			optionSet.add(monsters.getValueAt(row, col).toString());
		}
		optionSet.add(new String());
		Vector<String> options = new Vector<>(optionSet);
		Collections.sort(options);
		final JComboBox<String> combo = new JComboBox<>(options);
		filterCols.put(combo,col);
		combo.addActionListener(e -> newFilter(combo));
		return combo;
	}

	private JTextField createFilterTextField(int col) {
		final JTextField field = new JTextField(30);
		filterCols.put(field,col);
		// add an ActionListener so we can filter when enter is pressed
		field.addActionListener(e -> newFilter(field));
		// add a DocumentListener so we can filter on every keypress
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				newFilter(field);
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				newFilter(field);
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				newFilter(field);
			}
		});
		return field;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			MonsterEntry me = monsters.getMonsterEntry(table.convertRowIndexToModel(table.getSelectedRow()));
			// see if we can find a statistics block (based on name matching)
			URL url = me.getURL(baseURL);
			if (url != null) {
				System.out.println("URL: " + url);
				System.out.println("Selected: " + me.name);

				List<StatisticsBlock> blocks = StatisticsBlock.parseURL(url);
				StatisticsBlock selected = null;
				int matched = 0;
				for (StatisticsBlock block : blocks) {
					// tries to match the selected MonsterEntry name to a StatisticsBlock name, in one of these ways:
					// 1. MonsterEntry name == StatisticsBlock name
					// 2. MonsterEntry name == StatisticsBlock name up to first comma
					// 3. MonsterEntry name up to first parentheses == StatisticsBlock name
					// 4. MonsterEntry name up to first parentheses == StatisticsBlock name up to first comma
					String blockName = block.getName();
					if (blockName.equals(me.name)
							|| (blockName.contains(",") && blockName.substring(0, blockName.indexOf(',')).equals(me.name))) {
						selected = block;
						matched++;
					} else if (me.name.contains("(")) {
						String entryName = me.name.substring(0, me.name.indexOf('(')).trim();
						if (blockName.equals(entryName)
								|| (blockName.contains(",") && blockName.substring(0, blockName.indexOf(',')).equals(entryName))) {
							selected = block;
							matched++;
						}
					}
				}

				if (matched == 1) {
					Window parentFrame = SwingUtilities.windowForComponent(this);
					EncounterDialog.createOrExtendEncounter(parentFrame, selected);
				} else {
					JFrame frame = new MonsterFrame(me, url);
					frame.setVisible(true);
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
}



package monsters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableRowSorter;

// TODO move listeners to inner classes

@SuppressWarnings("serial")
public class MonstersPanel extends JPanel implements MouseListener, HyperlinkListener {
	JTable table;
	MonstersTableModel monsters;
	TableRowSorter<MonstersTableModel> sorter;
	Map<JComponent,RowFilter<MonstersTableModel,Integer>> filters = new HashMap<JComponent,RowFilter<MonstersTableModel,Integer>>();
	Map<JComponent,Integer>filterCols = new HashMap<JComponent,Integer>();
	URL baseURL;

	public MonstersPanel() {
		File f = new File("html/monsters/");
		try {
			baseURL = f.toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		monsters = new MonstersTableModel();
		// TODO remove hardcoded files here - probably best to use one global master or just scan for xml files
		monsters.parseXML(new File("html/monsters/monster_manual.xml"));
		monsters.parseXML(new File("html/monsters/monster_manual_ii.xml"));
		monsters.parseXML(new File("html/monsters/monster_manual_iii.xml"));
		monsters.parseXML(new File("html/monsters/ptolus.xml"));
		//filterModel = new FilterTableModel<MonsterEntry>(monsters);
		table = new JTable(monsters);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(this);
		sorter = new TableRowSorter<MonstersTableModel>(monsters);
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
		HashSet<RowFilter<MonstersTableModel, Integer>> set = new HashSet<RowFilter<MonstersTableModel, Integer>>(filters.values());
		if (set.contains(null)) set.remove(null);
		sorter.setRowFilter(RowFilter.andFilter(set));
	}

	private void newFilter(JComboBox combo) {
		if (combo.getSelectedItem().toString().equals("")) {
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
		HashSet<RowFilter<MonstersTableModel, Integer>> set = new HashSet<RowFilter<MonstersTableModel, Integer>>(filters.values());
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
		JComboBox sizeField = createFilterCombo(MonstersTableModel.Column.SIZE.ordinal());
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
		JComboBox crField = createFilterCombo(MonstersTableModel.Column.CR.ordinal());
		c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel("CR:"),c);
		c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(crField,c);

//		JTextField sourceField = createFilterTextField(MonstersTableModel.COLUMN_SOURCE);
		JComboBox sourceField = createFilterCombo(MonstersTableModel.Column.SOURCE.ordinal());
		c.gridx = 2; c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel("Source:"),c);
		c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(sourceField,c);

		return panel;
	}

	private JComboBox createFilterCombo(int col) {
		HashSet<String> optionSet = new HashSet<String>();
		for (int row = 0; row < monsters.getRowCount(); row++) {
			optionSet.add(monsters.getValueAt(row, col).toString());
		}
		optionSet.add(new String());
		Vector<String> options = new Vector<String>(optionSet);
		Collections.sort(options);
		final JComboBox combo = new JComboBox(options);
		filterCols.put(combo,col);
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newFilter(combo);
			}
		});
		return combo;
	}

	private JTextField createFilterTextField(int col) {
		final JTextField field = new JTextField(30);
		filterCols.put(field,col);
		// add an ActionListener so we can filter when enter is pressed
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newFilter(field);
			}
		});
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
			try {
				URL url;
				try {
					url = new URL(me.url);
				} catch (MalformedURLException e1) {
					// try relative URL
					url = new URL(baseURL, me.url);
				}
				//System.out.println("URL: "+url);
				JFrame frame = createMonsterFrame(me, url);
				frame.setVisible(true);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private class AddMonsterButton extends JButton {
		StatisticsBlock block;

		public AddMonsterButton(StatisticsBlock b) {
			super("Add " + b.getName());
			block = b;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Window parentFrame = SwingUtilities.windowForComponent(MonstersPanel.this);
					new AddMonsterDialog(parentFrame, block);
				}
			});
		}
	}

	private JFrame createMonsterFrame(MonsterEntry me, URL url) {
		String label = "Size: "+me.size + ", Type: "+me.type+", Environment: "+me.environment+", CR: "+me.cr;
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(0,5));
		File f;
		try {
			URL u = new URL(url.getProtocol(),url.getHost(),url.getPort(),url.getFile());
			f = new File(u.toURI());
			List<StatisticsBlock> blocks = StatisticsBlock.parseFile(f);
			for (StatisticsBlock block : blocks) {
				JButton button = new AddMonsterButton(block);
				buttons.add(button);
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(new JLabel(label),BorderLayout.NORTH);
		topPanel.add(buttons);

		JFrame frame = new JFrame(me.name);
		JEditorPane p = createWebPanel(url);
		JScrollPane sp = new JScrollPane(p);
		sp.setSize(new Dimension(800,600));
		sp.setPreferredSize(new Dimension(800,600));
		frame.add(topPanel,BorderLayout.NORTH);
		frame.add(sp);
		frame.setSize(new Dimension(800,600));
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	private JEditorPane createWebPanel(URL url) {
		JEditorPane p = new JEditorPane();
		p.setEditable(false);
		p.addHyperlinkListener(this);
		try {
			p.setPage(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		try{
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				if (e.getURL() != null) {
					JFrame frame = new JFrame(e.getURL().toString());
					JScrollPane sp;
					if (e.getURL().getFile().endsWith(".jpg")) {
						JLabel pic = new JLabel(new ImageIcon(e.getURL()));
						sp = new JScrollPane(pic);
					} else {
						JEditorPane p = createWebPanel(e.getURL());
						sp = new JScrollPane(p);
						sp.setPreferredSize(new Dimension(800,600));
					}
					frame.add(sp);
					frame.pack();
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setVisible(true);
				} else {
					System.out.println("No URL, string was: "+e.getDescription());
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
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



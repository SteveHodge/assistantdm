package monsters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableRowSorter;

@SuppressWarnings("serial")
public class MonstersPanel extends JPanel implements MouseListener, HyperlinkListener {
	JTable table;
	MonstersTableModel monsters;
	TableRowSorter<MonstersTableModel> sorter;
	Map<JTextField,RowFilter<MonstersTableModel,Integer>> filters = new HashMap<JTextField,RowFilter<MonstersTableModel,Integer>>();
	Map<JTextField,Integer>filterCols = new HashMap<JTextField,Integer>();
	URL baseURL;

	public MonstersPanel() {
		File f = new File("html/monsters/");
		try {
			baseURL = f.toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MonstersTableModel monsters = MonstersTableModel.parseXML(new File("html/monsters/monster_manual.xml"));
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

	protected void newFilter(JTextField field) {
	    RowFilter<MonstersTableModel, Integer> rf = null;
	    int col = filterCols.get(field);
	    try {
	        rf = RowFilter.regexFilter(field.getText(), col);
	    } catch (java.util.regex.PatternSyntaxException e) {
	    	// if the expression doesn't parse then we ignore it
	    }
	    filters.put(field, rf);
	    HashSet<RowFilter<MonstersTableModel, Integer>> set = new HashSet<RowFilter<MonstersTableModel, Integer>>(filters.values());
	    if (set.contains(null)) set.remove(null);
	    sorter.setRowFilter(RowFilter.andFilter(set));
	}

	protected JPanel createFilterPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3,4));

		JTextField nameField = createFilterTextField(0);
		panel.add(new JLabel("Name:"));
		panel.add(nameField);

		JTextField sizeField = createFilterTextField(1);
		panel.add(new JLabel("Size:"));
		panel.add(sizeField);

		JTextField typeField = createFilterTextField(2);
		panel.add(new JLabel("Type:"));
		panel.add(typeField);

		JTextField environmentField = createFilterTextField(3);
		panel.add(new JLabel("Environment:"));
		panel.add(environmentField);

		JTextField crField = createFilterTextField(4);
		panel.add(new JLabel("CR:"));
		panel.add(crField);

		return panel;
	}

	protected JTextField createFilterTextField(int col) {
		final JTextField field = new JTextField(30);
		filterCols.put(field,col);
		// add an ActionListener so we can filter when enter is pressed
		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newFilter(field);
			}
		});
		// add a DocumentListener so we can filter on every keypress
		field.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) {
				newFilter(field);
			}

			public void insertUpdate(DocumentEvent arg0) {
				newFilter(field);
			}

			public void removeUpdate(DocumentEvent arg0) {
				newFilter(field);
			}
		});
		return field;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			MonsterEntry me = monsters.getMonsterEntry(table.getSelectedRow());
			try {
				URL url;
				try {
					url = new URL(me.url);
				} catch (MalformedURLException e1) {
					// try relative URL
					url = new URL(baseURL, me.url); 
				}
				//System.out.println("URL: "+url);
				JFrame frame = new JFrame(me.name);
				JEditorPane p = createWebPanel(url);
				JScrollPane sp = new JScrollPane(p);
				sp.setSize(new Dimension(800,600));
				sp.setPreferredSize(new Dimension(800,600));
				String label = "Size: "+me.size + ", Type: "+me.type+", Environment: "+me.environment+", CR: "+me.cr;
				frame.add(new JLabel(label),BorderLayout.NORTH);
				frame.add(sp);
				frame.setSize(new Dimension(800,600));
				frame.pack();
				frame.setVisible(true);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public JEditorPane createWebPanel(URL url) {
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

	public void hyperlinkUpdate(HyperlinkEvent e) {
		try{
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				JFrame frame = new JFrame(e.getURL().toString());
				JEditorPane p = createWebPanel(e.getURL());
				JScrollPane sp = new JScrollPane(p);
				sp.setSize(new Dimension(800,600));
				sp.setPreferredSize(new Dimension(800,600));
				frame.add(sp);
				frame.setSize(new Dimension(800,600));
				frame.pack();
				frame.setVisible(true);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}



package monsters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class MonstersPanel extends JPanel implements MouseListener {
	JEditorPane tp;
	JTable table;
	FilterTableModel<MonsterEntry> filterModel;
	NameFilter nameFilter;
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
		filterModel = new FilterTableModel<MonsterEntry>(monsters);
		table = new JTable(filterModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(this);
		nameFilter = new NameFilter();
		filterModel.addFilter(nameFilter);
		final JTextField nameField = new JTextField(30);
		// add an ActionListener so we can filter when enter is pressed
		nameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nameFilter.setString(nameField.getText());
			}
		});
		// add a DocumentListener so we can filter on every keypress
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) {
				nameFilter.setString(nameField.getText());
			}

			public void insertUpdate(DocumentEvent arg0) {
				nameFilter.setString(nameField.getText());
			}

			public void removeUpdate(DocumentEvent arg0) {
				nameFilter.setString(nameField.getText());
			}
		});

		JScrollPane scrollpane = new JScrollPane(table);

		setLayout(new BorderLayout());
		add(scrollpane,BorderLayout.CENTER);
		add(nameField,BorderLayout.NORTH);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			MonsterEntry me = filterModel.getRowObject(table.getSelectedRow());
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
				MonsterPanel p = new MonsterPanel(url);
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

	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	protected static class NameFilter extends AbstractFilter<MonsterEntry> {
		protected String str = "";

		public void setString(String s) {
			str = s.toLowerCase();
			notifyModels();
		}

		public boolean matches(MonsterEntry object) {
			return object.name.toLowerCase().contains(str);
		}
	}
}



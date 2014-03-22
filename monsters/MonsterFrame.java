package monsters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

@SuppressWarnings("serial")
public class MonsterFrame extends JFrame {
	public MonsterFrame(StatisticsBlock block) {
		super(block.getName());
		// TODO proper info row
		try {
			createFrame("", block.getURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public MonsterFrame(MonsterEntry me, URL url) {
		super(me.name);
		createFrame("Size: " + me.size + ", Type: " + me.type + ", Environment: " + me.environment + ", CR: " + me.cr, url);
	}

	private void createFrame(String info, URL url) {
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(0, 5));
		List<StatisticsBlock> blocks = StatisticsBlock.parseURL(url);
		for (StatisticsBlock block : blocks) {
			JButton button = new AddMonsterButton(block);
			buttons.add(button);
		}

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(new JLabel(info), BorderLayout.NORTH);
		topPanel.add(buttons);

		JEditorPane p = createWebPanel(url);
		JScrollPane sp = new JScrollPane(p);
		sp.setSize(new Dimension(800, 600));
		sp.setPreferredSize(new Dimension(800, 600));

		add(topPanel, BorderLayout.NORTH);
		add(sp);
		setSize(new Dimension(800, 600));
		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private JEditorPane createWebPanel(URL url) {
		JEditorPane p = new JEditorPane();
		p.setEditable(false);
		p.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				try {
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
								sp.setPreferredSize(new Dimension(800, 600));
							}
							frame.add(sp);
							frame.pack();
							frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							frame.setVisible(true);
						} else {
							System.out.println("No URL, string was: " + e.getDescription());
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		try {
			p.setPage(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}

	private class AddMonsterButton extends JButton {
		StatisticsBlock block;

		public AddMonsterButton(StatisticsBlock b) {
			super("Add " + b.getName());
			block = b;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Window parentFrame = SwingUtilities.windowForComponent(MonsterFrame.this);
					EncounterDialog.createOrExtendEncounter(parentFrame, block);
				}
			});
		}
	}
}

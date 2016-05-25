package monsters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
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
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
		String html = null;
		try {
			if (url.getFile().endsWith(".xml")) {
				File xmlFile = new File(url.toURI());
				File stylesheet = new File(xmlFile.getParent() + "/Monster.xsl");
				Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(stylesheet));
				StringWriter output = new StringWriter();
				transformer.transform(new StreamSource(xmlFile), new StreamResult(output));
				html = output.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(new JLabel(info), BorderLayout.NORTH);

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(0, 5));
		List<StatisticsBlock> blocks = StatisticsBlock.parseURL(url);
		for (StatisticsBlock block : blocks) {
			JButton button = new AddMonsterButton(block);
			buttons.add(button);
		}
		topPanel.add(buttons);
		JEditorPane p = createWebPanel(url, html);

		JScrollPane sp = new JScrollPane(p);
		sp.setSize(new Dimension(800, 600));
		sp.setPreferredSize(new Dimension(800, 600));

		add(topPanel, BorderLayout.NORTH);
		add(sp);
		setSize(new Dimension(800, 600));
		pack();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private JEditorPane createWebPanel() {
		JEditorPane p = new JEditorPane();
		p.setEditable(false);
		p.addHyperlinkListener(e -> {
			try {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (e.getURL() != null) {
						JFrame frame = new JFrame(e.getURL().toString());
						JScrollPane sp;
						if (e.getURL().getFile().endsWith(".jpg")) {
							JLabel pic = new JLabel(new ImageIcon(e.getURL()));
							sp = new JScrollPane(pic);
						} else {
							JEditorPane pane = createWebPanel(e.getURL());
							sp = new JScrollPane(pane);
							sp.setPreferredSize(new Dimension(800, 600));
						}
						frame.add(sp);
						frame.pack();
						frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
						frame.setVisible(true);
					} else {
						System.out.println("No URL, string was: " + e.getDescription());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		return p;
	}

	private JEditorPane createWebPanel(URL url, String html) {
		if (html == null) return createWebPanel(url);

		JEditorPane p = createWebPanel();
		p.setContentType("text/html; charset=utf-8");
		p.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
		if (p.getDocument() instanceof HTMLDocument) {
			((HTMLDocument) p.getDocument()).setBase(url);
		}
		p.setText(html);
		return p;
	}

	private JEditorPane createWebPanel(URL url) {
		JEditorPane p = createWebPanel();
		try {
			p.setPage(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}

	private class AddMonsterButton extends JButton {
		StatisticsBlock block;

		public AddMonsterButton(StatisticsBlock b) {
			super("Add " + b.getName());
			block = b;
			addActionListener(e -> {
				Window parentFrame = SwingUtilities.windowForComponent(MonsterFrame.this);
				EncounterDialog.createOrExtendEncounter(parentFrame, block);
			});
		}
	}
}

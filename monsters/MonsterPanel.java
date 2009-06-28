package monsters;

import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

@SuppressWarnings("serial")
public class MonsterPanel extends JEditorPane implements HyperlinkListener {
	public MonsterPanel(URL url) {
		setEditable(false);
		addHyperlinkListener(this);
		try {
			setPage(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		try{
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				setPage(e.getURL());
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}

package swing;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;


public class ReorderableListEntry extends JPanel {
	String text = "";

	public ReorderableListEntry() {
	}

	public ReorderableListEntry(String text) {
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		this.text = text;
		add(new JLabel(text));
	}
}

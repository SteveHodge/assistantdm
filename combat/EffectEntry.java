package combat;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.w3c.dom.Element;

//Class no longer used
// TODO deletes should be handled by passing an event on to listeners

@SuppressWarnings("serial")
public class EffectEntry extends JPanel implements ActionListener {
	protected JButton delete;
	protected JLabel durationLabel;
	protected JLabel effectLabel;
	protected JLabel sourceLabel;
	protected JLabel initiativeLabel;

	String effect;
	String source;
	int initiative;
	int duration;
	EffectListModel model;

	public EffectEntry(EffectListModel model, String effect, String source, int init, int duration) {
		this.effect = effect;
		this.source = source;
		this.initiative = init;
		this.duration = duration;
		this.model = model;

		setLayout(new GridBagLayout());

		delete = new JButton("X");
		delete.setMargin(new Insets(2, 4, 2, 3));
		delete.setFocusPainted(false);
		delete.addActionListener(this);

		effectLabel = new JLabel(effect);
		durationLabel = new JLabel(getDurationString());
		sourceLabel = new JLabel(source);
		initiativeLabel = new JLabel("("+initiative+")");

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 3, 2, 3);
		c.anchor = GridBagConstraints.LINE_START;
		add(delete, c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		add(effectLabel, c);
		c.weightx = 0.5;
		add(sourceLabel, c);
		add(initiativeLabel, c);
		c.anchor = GridBagConstraints.LINE_END;
		add(durationLabel, c);
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		model.removeEntry(this);
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int d) {
		duration = d;
		durationLabel.setText(getDurationString());
	}

	protected String getDurationString() {
		if (duration >= 900) {
			return ""+(duration / 600)+" Hours";
		} else if (duration >= 20) {
			return ""+(duration / 10)+" Minutes";
		}
		return ""+duration+" Rounds";
	}

	public static EffectEntry parseDOM(EffectListModel model, Element el) {
		if (!el.getNodeName().equals("EffectEntry")) return null;
		EffectEntry c = new EffectEntry(model, el.getAttribute("effect"), el.getAttribute("source"),
				Integer.parseInt(el.getAttribute("initiative")),
				Integer.parseInt(el.getAttribute("duration"))
				);
		return c;
	}

	public String getXML(String indent, String nextIndent) {
		StringBuilder b = new StringBuilder();
		b.append(indent).append("<EffectEntry effect=\"").append(effect);
		b.append("\" source=\"").append(source);
		b.append("\" initiative=\"").append(initiative);
		b.append("\" duration=\"").append(duration);
		b.append("\"/>").append(System.getProperty("line.separator"));
		return b.toString();
	}
}

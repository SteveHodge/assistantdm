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

@SuppressWarnings("serial")
public class EffectEntry extends JPanel implements ActionListener {
	protected JButton delete;
	protected JLabel durationLabel;

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

		JLabel e = new JLabel(effect);
		durationLabel = new JLabel(getDurationString());
		JLabel s = new JLabel(source);
		JLabel in = new JLabel("("+initiative+")");

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		add(delete, c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		add(e, c);
		c.weightx = 0.5;
		add(s, c);
		add(in, c);
		c.anchor = GridBagConstraints.LINE_END;
		add(durationLabel, c);
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

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
}

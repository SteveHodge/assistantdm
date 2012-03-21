package combat;

import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

//TODO figure out how to position the dialog properly
@SuppressWarnings("serial")
public class SelectTimeDialog extends JDialog implements ActionListener {
	boolean returnOk = false;
	JFormattedTextField rounds;
	JFormattedTextField minutes;
	JFormattedTextField hours;
	JFormattedTextField days;

	public SelectTimeDialog(Window owner) {
		super(owner, "Select time", Dialog.ModalityType.APPLICATION_MODAL);

		rounds = new JFormattedTextField();
		rounds.setValue(new Integer(0));
		rounds.setColumns(new Integer(5));
		minutes = new JFormattedTextField();
		minutes.setValue(new Integer(0));
		minutes.setColumns(new Integer(5));
		hours = new JFormattedTextField();
		hours.setValue(new Integer(0));
		hours.setColumns(new Integer(5));
		days = new JFormattedTextField();
		days.setValue(new Integer(0));
		days.setColumns(new Integer(5));

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4,2));
		panel.add(new JLabel("Rounds:"));
		panel.add(rounds);
		panel.add(new JLabel("Minutes:"));
		panel.add(minutes);
		panel.add(new JLabel("Hours:"));
		panel.add(hours);
		panel.add(new JLabel("Days:"));
		panel.add(days);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JPanel buttons = new JPanel();
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttons.add(okButton);
		buttons.add(cancelButton);

		add(panel);
		add(buttons,"South");
		pack();
	}

	public boolean isCancelled() {
		return !returnOk;
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("Ok")) {
			returnOk = true;
		}

		setVisible(false);
	}

	public int getValue() {
		int r = ((Integer)rounds.getValue()).intValue();
		int m = ((Integer)minutes.getValue()).intValue();
		int h = ((Integer)hours.getValue()).intValue();
		int d = ((Integer)days.getValue()).intValue();
		return ((d*24+h)*60+m)*10+r;
	}

	public void setValue(int value) {
		rounds.setValue(new Integer(value % 10));
		minutes.setValue(new Integer(value/10 % 60));
		hours.setValue(new Integer(value/(60*10) % 24));
		days.setValue(new Integer(value/(24*60*10)));
	}
}

package digital_table.controller;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import digital_table.server.TableDisplay;

// TODO probably best to fold this into DisplayConfig

@SuppressWarnings("serial")
class MonitorConfigFrame extends JFrame {
	private JComboBox[] screenCombos = new JComboBox[6];
	private TableDisplay display;
	boolean openScreens = false;
	int[] screenNums;

	public MonitorConfigFrame(TableDisplay disp) {
		super("Select screens");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		display = disp;

		JButton button = new JButton("Identify");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// reset the combo boxes
					DisplayConfig.getScreens(display);
					for (int i = 0; i < screenCombos.length; i++) {
						screenCombos[i].removeAllItems();
						screenCombos[i].addItem("unassigned");
						for (int j = 0; j < DisplayConfig.screens.size(); j++) {
							screenCombos[i].addItem("" + (j+1));
						}
					}

					display.setScreenIDsVisible(true);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		JButton openButton = new JButton("Open screens");
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				screenNums = new int[screenCombos.length];
				for (int i = 0; i < screenCombos.length; i++) {
					screenNums[i] = screenCombos[i].getSelectedIndex()-1;
				}
				openScreens = true;
				dispose();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(button);
		buttonPanel.add(openButton);
		add(buttonPanel, BorderLayout.NORTH);

		JPanel screensPanel = new JPanel();
		screensPanel.setLayout(new GridLayout(3, 2));

		// make the combo boxes
		DisplayConfig.getScreens(display);
		Object[] options = new Object[DisplayConfig.screens.size()+1];
		options[0] = "unassigned";
		for (int i = 1; i <= DisplayConfig.screens.size(); i++) {
			options[i] = ""+i;
		}
		for (int i = 0; i < screenCombos.length; i++) {
			screenCombos[i] = new JComboBox(options);
			screensPanel.add(screenCombos[i]);
		}
		add(screensPanel, BorderLayout.CENTER);

		pack();
		setVisible(true);
	}
}

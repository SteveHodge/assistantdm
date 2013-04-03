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


@SuppressWarnings("serial")
public class MonitorConfigFrame extends JFrame {
	JComboBox[] screenCombos = new JComboBox[6];
	TableDisplay display;

	public MonitorConfigFrame(TableDisplay disp) {
		super("Select screens");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		display = disp;

		JButton button = new JButton("Identify");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// reset the combo boxes
					Object[] screens = display.getScreenList();
					for (int i = 0; i < screenCombos.length; i++) {
						screenCombos[i].removeAllItems();
						for (Object item : screens) {
							screenCombos[i].addItem(item);
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
			public void actionPerformed(ActionEvent arg0) {
				try {
					int[] screenNums = new int[screenCombos.length];
					for (int i = 0; i < screenCombos.length; i++) {
						screenNums[i] = screenCombos[i].getSelectedIndex()-1;
					}
					display.showScreens(screenNums);
					new ControllerFrame(display);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(button);
		buttonPanel.add(openButton);
		add(buttonPanel, BorderLayout.NORTH);

		JPanel screensPanel = new JPanel();
		screensPanel.setLayout(new GridLayout(3, 2));
		try {
			Object[] screens = display.getScreenList();
			for (int i = 0; i < screenCombos.length; i++) {
				screenCombos[i] = new JComboBox(screens);
				screensPanel.add(screenCombos[i]);
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
		add(screensPanel, BorderLayout.CENTER);

		pack();
		setVisible(true);
	}
}

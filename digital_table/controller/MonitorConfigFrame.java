package digital_table.controller;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import digital_table.server.TableDisplay;

// TODO probably best to fold this into DisplayConfig

@SuppressWarnings("serial")
class MonitorConfigFrame extends JFrame {
	private final static int NUM_SCREENS = 6;

	private List<JComboBox<String>> screenCombos = new ArrayList<>();
	private TableDisplay display;
	boolean openScreens = false;
	int[] screenNums;

	public MonitorConfigFrame(TableDisplay disp) {
		super("Select screens");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		display = disp;

		JButton button = new JButton("Identify");
		button.addActionListener(e -> {
			try {
				// reset the combo boxes
				DisplayConfig.getScreens(display);
				for (int i = 0; i < screenCombos.size(); i++) {
					JComboBox<String> combo = screenCombos.get(i);
					combo.removeAllItems();
					combo.addItem("unassigned");
					for (int j = 0; j < DisplayConfig.screens.size(); j++) {
						combo.addItem("" + (j + 1));
					}
				}

				display.setScreenIDsVisible(true);
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
		});
		JButton openButton = new JButton("Open screens");
		openButton.addActionListener(e -> {
			screenNums = new int[screenCombos.size()];
			for (int i = 0; i < screenCombos.size(); i++) {
				screenNums[i] = screenCombos.get(i).getSelectedIndex() - 1;
			}
			openScreens = true;
			dispose();
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(button);
		buttonPanel.add(openButton);
		add(buttonPanel, BorderLayout.NORTH);

		JPanel screensPanel = new JPanel();
		screensPanel.setLayout(new GridLayout(3, 2));

		// make the combo boxes
		DisplayConfig.getScreens(display);
		String[] options = new String[DisplayConfig.screens.size() + 1];
		options[0] = "unassigned";
		for (int i = 1; i <= DisplayConfig.screens.size(); i++) {
			options[i] = ""+i;
		}
		for (int i = 0; i < NUM_SCREENS; i++) {
			JComboBox<String> combo = new JComboBox<>(options);
			screenCombos.add(combo);
			screensPanel.add(combo);
		}
		add(screensPanel, BorderLayout.CENTER);

		pack();
		setVisible(true);
	}
}

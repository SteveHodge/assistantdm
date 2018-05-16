package ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import gamesystem.ItemDefinition;

@SuppressWarnings("serial")
public class SelectItemDialog extends JDialog {
	private boolean okSelected = false;
	private ItemDefinition item = null;

	public SelectItemDialog(JComponent parent) {
		super(SwingUtilities.windowForComponent(parent), "Select Item", Dialog.ModalityType.DOCUMENT_MODAL);

		ItemDefinition[] items = ItemDefinition.getItems().toArray(new ItemDefinition[0]);
		Arrays.sort(items, (a, b) -> {
			return a.getName().compareTo(b.getName());
		});
		JList<ItemDefinition> itemList = new JList<>(items);
		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemList.setVisibleRowCount(15);
		JScrollPane scroller = new JScrollPane(itemList);

//		setLayout(new GridBagLayout());
//
//		GridBagConstraints c = new GridBagConstraints();
//		c.insets = new Insets(1, 2, 1, 2);
//		c.gridwidth = 1;
//		c.gridheight = 1;
//		c.weightx = 1.0;
//		c.weighty = 1.0;
//		c.fill = GridBagConstraints.BOTH;
//		c.gridx = GridBagConstraints.RELATIVE;
//		c.gridy = 0;
//		add(scroller, c);

		add(scroller, BorderLayout.CENTER);

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(e -> {
			item = itemList.getSelectedValue();
			dispose();
			okSelected = true;
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());

		JPanel buttons = new JPanel();
		buttons.add(okButton);
		buttons.add(cancelButton);

		add(buttons, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(SwingUtilities.getWindowAncestor(parent));
		setVisible(true);
	}

	public ItemDefinition getItem() {
		if (okSelected)
			return item;
		return null;
	}
}
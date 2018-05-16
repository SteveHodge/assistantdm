package ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gamesystem.ItemDefinition;

@SuppressWarnings("serial")
public class SelectItemDialog extends JDialog {
	private boolean okSelected = false;
	private ItemDefinition item = null;
	final private SelectItemListModel model = new SelectItemListModel();

	public SelectItemDialog(JComponent parent) {
		super(SwingUtilities.windowForComponent(parent), "Select Item", Dialog.ModalityType.DOCUMENT_MODAL);

		JList<ItemDefinition> itemList = new JList<>(model);
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

		JTextField nameField = new JTextField(30);
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				model.setNameFilter(nameField.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				model.setNameFilter(nameField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				model.setNameFilter(nameField.getText());
			}
		});
		JPanel panel = new JPanel();
		panel.add(new JLabel("Name: "));
		panel.add(nameField);
		add(panel, BorderLayout.NORTH);

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

	class SelectItemListModel extends AbstractListModel<ItemDefinition> {
		ItemDefinition[] items;
		List<ItemDefinition> filteredItems = new ArrayList<>();

		SelectItemListModel() {
			items = ItemDefinition.getItems().toArray(new ItemDefinition[0]);
			Arrays.sort(items, (a, b) -> {
				return a.getName().compareTo(b.getName());
			});
			filteredItems.addAll(Arrays.asList(items));
		}

		@Override
		public ItemDefinition getElementAt(int index) {
			return filteredItems.get(index);
		}

		@Override
		public int getSize() {
			return filteredItems.size();
		}

		public void setNameFilter(String filter) {
			int oldSize = filteredItems.size();
			filteredItems.clear();
			if (filter == null) {
				filteredItems.addAll(Arrays.asList(items));
			} else {
				filter = filter.toLowerCase();
				for (int i = 0; i < items.length; i++) {
					if (items[i].getName().toLowerCase().contains(filter)) {
						filteredItems.add(items[i]);
					}
				}
			}
			int size = filteredItems.size();
			if (size < oldSize) {
				fireIntervalRemoved(this, size, oldSize - 1);
			} else if (size > oldSize) {
				fireIntervalAdded(this, oldSize, size - 1);
			}
			fireContentsChanged(this, 0, size - 1);
		}
	}
}
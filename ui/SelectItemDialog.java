package ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
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
import swing.CheckedComboBox;

@SuppressWarnings("serial")
public class SelectItemDialog extends JDialog {
	private boolean okSelected = false;
	private ItemDefinition item = null;
	final private SelectItemListModel model = new SelectItemListModel();
	JTextField nameField;
	CheckedComboBox<String> categoryCombo;
	DefaultComboBoxModel<String> categoryModel;
	ListSelectionModel categorySelectModel;

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

		nameField = new JTextField(30);
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFilter();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFilter();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFilter();
			}
		});

		String[] categories = ItemDefinition.stream().map(ItemDefinition::getCategory).filter(s -> s != null).distinct().sorted().toArray(String[]::new);
		System.out.println("Categories = " + String.join(", ", categories));
		categoryModel = new DefaultComboBoxModel<String>(categories);
		categoryCombo = new CheckedComboBox<String>(categoryModel);
		categoryCombo.setPreferredSize(new Dimension(200, 20));
		categorySelectModel = categoryCombo.getSelectionModel();
		categoryCombo.selectAll();
		categorySelectModel.addListSelectionListener(e -> updateFilter());

		JPanel panel = new JPanel();
		panel.add(new JLabel("Name: "));
		panel.add(nameField);
		panel.add(new JLabel("Category: "));
		panel.add(categoryCombo);
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

	private void updateFilter() {
		final Predicate<ItemDefinition> nameFilter;
		final Predicate<ItemDefinition> catFilter;

		String name = nameField.getText();
		if (name != null && name.length() > 0) {
			nameFilter = i -> i.getName().toLowerCase().contains(name);
		} else {
			nameFilter = null;
		}

		if (!categoryCombo.allSelected()) {
			catFilter = i -> {
				int idx = categoryModel.getIndexOf(i.getCategory());
				return idx > 0 && categorySelectModel.isSelectedIndex(idx);
			};
		} else {
			catFilter = null;
		}

		Predicate<ItemDefinition> f = i -> {
			if (nameFilter != null && !nameFilter.test(i)) return false;
			if (catFilter != null && !catFilter.test(i)) return false;
			return true;
		};
		model.filter(f);
	}

	class SelectItemListModel extends AbstractListModel<ItemDefinition> {
		List<ItemDefinition> filteredItems = new ArrayList<>();

		SelectItemListModel() {
			filter(null);
		}

		@Override
		public ItemDefinition getElementAt(int index) {
			return filteredItems.get(index);
		}

		@Override
		public int getSize() {
			return filteredItems.size();
		}

		public void filter(Predicate<ItemDefinition> filter) {
			int oldSize = filteredItems.size();
			Stream<ItemDefinition> stream = ItemDefinition.stream();
			if (filter != null)
				stream = stream.filter(filter);
			filteredItems = stream.sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList());
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
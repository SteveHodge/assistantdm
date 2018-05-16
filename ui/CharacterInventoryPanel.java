package ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Random;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import gamesystem.ItemDefinition;
import party.Character;
import party.Inventory;

@SuppressWarnings("serial")
public class CharacterInventoryPanel extends CharacterSubPanel {
	public CharacterInventoryPanel(Character chr) {
		super(chr);

		InventoryListModel model = new InventoryListModel();
		JList<ItemDefinition> itemList = new JList<>(model);

		setLayout(new GridBagLayout());

		JButton newButton = new JButton("New");
		newButton.addActionListener(e -> {
			SelectItemDialog dialog = new SelectItemDialog(this);
			ItemDefinition item = dialog.getItem();
			if (item != null)
				character.inventory.add(item);
		});

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(e -> {
			int index = itemList.getSelectedIndex();
			if (index != -1)
				character.inventory.remove(index);
		});

		JButton upButton = new JButton("/\\");
		upButton.addActionListener(e -> {
			int i = itemList.getSelectedIndex();
			if (i >= 1) {
				ItemDefinition item = character.inventory.remove(i);
				character.inventory.add(--i, item);
				itemList.setSelectedIndex(i);
			}
		});

		JButton downButton = new JButton("\\/");
		downButton.addActionListener(e -> {
			int i = itemList.getSelectedIndex();
			if (i != -1 && i < model.getSize() - 1) {
				ItemDefinition item = character.inventory.remove(i);
				character.inventory.add(++i, item);
				itemList.setSelectedIndex(i);
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(newButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(upButton);
		buttonPanel.add(downButton);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTH;
		add(buttonPanel, c);

		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemList.setVisibleRowCount(12);
//		itemList.addListSelectionListener(e -> {
//			if (!e.getValueIsAdjusting()) {
//				attackPanel.setAttackForm(weaponList.getSelectedValue());
//			}
//		});
		JScrollPane scroller = new JScrollPane(itemList);
		//scroller.setPreferredSize(preferredSize);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.5;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.LINE_START;
		add(scroller, c);
	}

	private class InventoryListModel extends AbstractListModel<ItemDefinition> {
		Inventory inventory;

		InventoryListModel() {
			inventory = character.inventory;
			inventory.addPropertyListener(e -> {
				// TODO more appropriate events
				fireContentsChanged(this, 0, getSize() - 1);
			});
			ItemDefinition[] items = ItemDefinition.getItemsForSlot(null).toArray(new ItemDefinition[0]);
			Random rand = new Random();
			ItemDefinition item = items[rand.nextInt(items.length)];
			inventory.add(item);
			item = items[rand.nextInt(items.length)];
			inventory.add(item);
			item = items[rand.nextInt(items.length)];
			inventory.add(item);
		}

		@Override
		public ItemDefinition getElementAt(int index) {
			return inventory.get(index);
		}

		@Override
		public int getSize() {
			return inventory.size();
		}
	}
}


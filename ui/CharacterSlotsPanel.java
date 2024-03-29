package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import gamesystem.ItemDefinition;
import gamesystem.ItemDefinition.SlotType;
import party.Character;
import party.InventorySlots.Slot;
import swing.JTableWithToolTips;
import swing.TableModelWithToolTips;

@SuppressWarnings("serial")
public class CharacterSlotsPanel extends CharacterSubPanel {
	private SlotsTableModel slotsModel;

	public CharacterSlotsPanel(Character c) {
		super(c);

		slotsModel = new SlotsTableModel();

		final JTable slotsTable = new JTableWithToolTips(slotsModel);
		slotsTable.setFillsViewportHeight(true);
		slotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		slotsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		slotsTable.setDefaultEditor(SlotType.class, new ItemEditor());
		slotsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
//				if (!SwingUtilities.isRightMouseButton(e)) return;
//				int row = abilityTable.rowAtPoint(e.getPoint());
//				String title = slotsModel.getAbilityName(row);
//				String statName = slotsModel.getStatistic(row);
//				StatisticInfoDialog dialog = new StatisticInfoDialog(CharacterSlotsPanel.this, title, character, statName);
//				dialog.setVisible(true);
			}
		});
		slotsTable.setPreferredScrollableViewportSize(new Dimension(slotsTable.getPreferredSize().width, slotsTable.getRowHeight() * slotsModel.getRowCount() + 1));
		JScrollPane slotsScrollpane = new JScrollPane(slotsTable);

		setLayout(new BorderLayout());
		add(slotsScrollpane);
	}

	private class ItemEditor extends AbstractCellEditor implements TableCellEditor {
		final SlotType[] slots = { SlotType.RING, SlotType.RING, SlotType.HANDS, SlotType.ARMS, SlotType.HEAD, SlotType.FACE, SlotType.SHOULDERS, SlotType.NECK, SlotType.BODY, SlotType.TORSO, SlotType.WAIST, SlotType.FEET };

		JComboBox<ItemDefinition> combo;

		@Override
		public Object getCellEditorValue() {
			if (combo != null) return combo.getSelectedItem();
			return null;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
			List<ItemDefinition> options = new ArrayList<>(ItemDefinition.getItemsForSlot(slots[row]));
			options.sort((a, b) -> {
				if (!(a instanceof ItemDefinition || !(b instanceof ItemDefinition))) return 0;
				return a.getName().compareToIgnoreCase(b.getName());
			});
			options.add(0, null);
			combo = new JComboBox<>(options.toArray(new ItemDefinition[0]));
			if (options.contains(value)) {
				combo.setSelectedItem(value);
			}
			return combo;
		}

	}

	private class SlotsTableModel extends AbstractTableModel implements TableModelWithToolTips {
		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return Slot.values().length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return Slot.values()[row].toString();
			} else if (col == 1) {
				return character.getSlotItem(Slot.values()[row]);
			} else if (col == 2) {
				return character.getSlotItem(Slot.values()[row]) != null && character.isSlotItemEquipped(Slot.values()[row]);
			}
			return null;
		}


		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == 1) {
				if (value != null && !(value instanceof ItemDefinition)) return;
				character.setSlotItem(Slot.values()[row], (ItemDefinition) value);
			} else if (column == 2) {
				if (!(value instanceof Boolean)) return;
				character.setSlotItemEquipped(Slot.values()[row], ((Boolean) value).booleanValue());
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 1)
				return true;
			if (columnIndex == 2)
				return character.getSlotItem(Slot.values()[rowIndex]) != null;
			return false;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0)
				return String.class;
			else if (columnIndex == 1)
				return SlotType.class;
			else
				return Boolean.class;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0)
				return "Slot";
			else if (col == 1)
				return "Item";
			else if (col == 2)
				return "Equipped";
			else
				return super.getColumnName(col);
		}

		@Override
		public String getToolTipAt(int row, int col) {
			return Slot.values()[row].toString();
		}

	}

}


package party;

import java.util.HashMap;
import java.util.Map;

import gamesystem.Buff;
import gamesystem.BuffFactory;
import gamesystem.ItemDefinition;
import gamesystem.ItemDefinition.SlotType;
import gamesystem.core.AbstractProperty;
import gamesystem.core.PropertyEvent;

public class InventorySlots extends AbstractProperty<InventorySlots> {
	public enum Slot {
		RIGHT_RING("Ring Slot (RH)", SlotType.RING), LEFT_RING("Ring Slot (LH", SlotType.RING), HANDS("Hand Slot", SlotType.HANDS), ARMS("Arm Slot", SlotType.ARMS), HEAD("Head Slot",
				SlotType.HEAD), FACE("Face Slot", SlotType.FACE), SHOULDERS("Shoulder Slot", SlotType.SHOULDERS), NECK("Neck Slot",
						SlotType.NECK), BODY("Body Slot", SlotType.BODY), TORSO("Torso Slot", SlotType.TORSO), WAIST("Waist Slot", SlotType.WAIST), FEET("Feet Slot", SlotType.FEET);

		private Slot(String d, SlotType t) {
			description = d;
			this.type = t;
		}

		@Override
		public String toString() {
			return description;
		}

		public SlotType getSlotType() {
			return type;
		}

		// XXX brute force implementation - could keep a map
		public static Slot getSlot(String d) {
			for (Slot s : values()) {
				if (s.name().compareToIgnoreCase(d) == 0) return s;
			}
			return null;	// TODO probably better to throw an exception
		}

		private final String description;
		private final SlotType type;
	}

	Character character;
	Map<Slot, ItemDefinition> items = new HashMap<>();	// the item in each slot
	Map<Slot, Buff> buffs = new HashMap<>();	// the buff (if any) applied by the item in each slot
	Map<Slot, Boolean> equipped = new HashMap<>();

	public InventorySlots(Character c) {
		super("inventory_slots", c);
		character = c;
	}

	@Override
	public InventorySlots getValue() {
		return this;
	}

	public ItemDefinition getItem(Slot slot) {
		return items.get(slot);
	}

	public void setItem(Slot slot, ItemDefinition item) {
		ItemDefinition old = items.get(slot);
		if (old != null && old.equals(item) || old == null && item == null) return;
		Buff buff = buffs.get(slot);
		if (buff != null) {
			character.removeBuff(buff);
			buffs.remove(slot);
		}
		if (item == null) {
			items.remove(slot);
		} else {
			items.put(slot, item);
			equipped.put(slot, true);
			BuffFactory factory = item.getBuffFactory();
			if (factory != null) {
				buff = factory.getBuff();
				buffs.put(slot, buff);
				character.addBuff(buff);
			}
		}
		PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
		e.set("slot", slot);
		fireEvent(e);
	}

	public boolean isEquipped(Slot slot) {
		if (equipped.containsKey(slot))
			return equipped.get(slot);
		return false;
	}

	public void setEquipped(Slot slot, boolean equipped) {
		this.equipped.put(slot, equipped);
		Buff b = buffs.get(slot);
		if (b != null)
			b.setDisabled(!equipped);
		PropertyEvent e = createEvent(PropertyEvent.VALUE_CHANGED);
		e.set("slot", slot);
		fireEvent(e);
	}
}

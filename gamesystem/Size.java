package gamesystem;

import gamesystem.core.AbstractOverridableProperty;
import gamesystem.core.OverridableProperty;
import gamesystem.core.OverridablePropertyEvent;
import gamesystem.core.OverridablePropertyEvent.EventType;
import gamesystem.core.PropertyCollection;
import gamesystem.core.StatisticEvent;

// TODO need to store certain size related characteristics of creatures: whether they are tall or long (for reach), and whether they count as quadrupeds for carrying capacity
public class Size extends Statistic {
	private SizeCategory category = SizeCategory.MEDIUM;
	private int spaceValue = SizeCategory.MEDIUM.getSpace();
	private int reachValue = SizeCategory.MEDIUM.getReachTall();
	private final Modifier grappleMod = new SizeModifier() {
		@Override
		public int getModifier() {
			return category.getGrappleModifier();
		}
	};
	private final Modifier sizeMod = new SizeModifier() {
		@Override
		public int getModifier() {
			return category.getSizeModifier();
		}
	};

	private AbstractOverridableProperty<Integer> space;
	private AbstractOverridableProperty<Integer> reach;

	public Size(PropertyCollection parent) {
		super("size", "Size", parent);

		space = new AbstractOverridableProperty<Integer>("size.space", parent) {
			@Override
			public Integer getRegularValue() {
				if (getModifiersTotal() == 0) return spaceValue;
				if (spaceValue == category.getSpace()) return getSize().getSpace();	// standard space so use standard for changed size
				// TODO adjust space based on change in size
				return spaceValue;
			}
		};

		reach = new AbstractOverridableProperty<Integer>("size.reach", parent) {
			@Override
			public Integer getRegularValue() {
				if (getModifiersTotal() == 0) return reachValue;
				if (reachValue == category.getReachTall()) return getSize().getReachTall();	// standard reach
				if (reachValue == category.getReachLong()) return getSize().getReachLong();	// standard reach
				// TODO adjust reach based on change in size
				return reachValue;
			}
		};
	}

	public Modifier getSizeModifier() {
		return sizeMod;
	}

	public Modifier getGrappleSizeModifier() {
		return grappleMod;
	}

	// TODO should probably replace the modifier with an ImmutableModifier of enhancement type
	@Override
	public void addModifier(Modifier m) {
		m.addPropertyChangeListener(listener);
		modifiers.add(m);
		fireEvent(new StatisticEvent(this, StatisticEvent.EventType.MODIFIER_ADDED));
	}

	// TODO should probably replace the modifier with an ImmutableModifier of enhancement type
	@Override
	public void removeModifier(Modifier m) {
		modifiers.remove(m);
		m.removePropertyChangeListener(listener);
		fireEvent(new StatisticEvent(this, StatisticEvent.EventType.MODIFIER_REMOVED));
	}

	public SizeCategory getBaseSize() {
		return category;
	}

	public SizeCategory getSize() {
		if (getModifiersTotal() == 0) return category;
		return category.resize(getModifiersTotal());
	}

	public void setBaseSize(SizeCategory size) {
		if (size == null) throw new NullPointerException("Size cannot be set to null");
		category = size;
		fireEvent(new StatisticEvent(this, StatisticEvent.EventType.TOTAL_CHANGED));
	}

	public OverridableProperty<Integer> getSpace() {
		return space;
	}

	public OverridableProperty<Integer> getReach() {
		return reach;
	}

	public void setBaseSpace(int s) {
		if (s < 0) throw new IllegalArgumentException("Space cannot be negative: " + s);
		int old = space.getValue();
		spaceValue = s;
		parent.fireEvent(new OverridablePropertyEvent<>(space, EventType.REGULAR_VALUE_CHANGED, old));
	}

	public void setBaseReach(int r) {
		if (r < 0) throw new IllegalArgumentException("Reach cannot be negative: " + r);
		int old = reach.getValue();
		reachValue = r;
		parent.fireEvent(new OverridablePropertyEvent<>(reach, EventType.REGULAR_VALUE_CHANGED, old));
	}

	protected abstract class SizeModifier extends AbstractModifier {
		public SizeModifier() {
			Size.this.addPropertyListener(e -> pcs.firePropertyChange("value", null, getModifier()));
		}

		@Override
		public String getType() {
			return name;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			if (getModifier() >= 0) s.append("+");
			s.append(getModifier()).append(" size modifier");
			return s.toString();
		}
	};

}

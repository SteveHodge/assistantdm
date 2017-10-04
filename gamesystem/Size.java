package gamesystem;

import gamesystem.core.PropertyCollection;

// TODO need to store certain size related characteristics of creatures: whether they are tall or long (for reach), and whether they count as quadrupeds for carrying capacity
public class Size extends Statistic {
	private SizeCategory category = SizeCategory.MEDIUM;
	private int space = SizeCategory.MEDIUM.getSpace();
	private int reach = SizeCategory.MEDIUM.getReachTall();
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

	public Size(PropertyCollection parent) {
		super("size", "Size", parent);
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
		fireEvent();
	}

	// TODO should probably replace the modifier with an ImmutableModifier of enhancement type
	@Override
	public void removeModifier(Modifier m) {
		modifiers.remove(m);
		m.removePropertyChangeListener(listener);
		fireEvent();
	}

	public int getSpace() {
		if (getModifiersTotal() == 0) return space;
		if (space == category.getSpace()) return getSize().getSpace();	// standard space so use standard for changed size
		// TODO adjust space based on change in size
		return space;
	}

	public int getReach() {
		if (getModifiersTotal() == 0) return reach;
		if (reach == category.getReachTall()) return getSize().getReachTall();	// standard reach
		if (reach == category.getReachLong()) return getSize().getReachLong();	// standard reach
		// TODO adjust reach based on change in size
		return reach;
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
		fireEvent();
	}

	public void setBaseSpace(int s) {
		if (s < 0) throw new IllegalArgumentException("Space cannot be negative: " + s);
		space = s;
		fireEvent();
	}

	public void setBaseReach(int r) {
		if (r < 0) throw new IllegalArgumentException("Reach cannot be negative: " + r);
		reach = r;
		fireEvent();
	}

	protected abstract class SizeModifier extends AbstractModifier {
		public SizeModifier() {
			Size.this.addPropertyListener((source, old) -> pcs.firePropertyChange("value", null, getModifier()));
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

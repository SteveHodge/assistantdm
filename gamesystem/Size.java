package gamesystem;

// TODO need to store certain size related characteristics of creatures: whether they are tall or long (for reach), and whether they count as quadrupeds for carrying capacity
// FIXME should make the size modifier and the grapple modifier real modifiers
public class Size extends Statistic {
	private SizeCategory category = SizeCategory.MEDIUM;
	private int space = SizeCategory.MEDIUM.getSpace();
	private int reach = SizeCategory.MEDIUM.getReachTall();

	public Size() {
		super("Size");
	}

	// TODO should probably replace the modifier with an ImmutableModifier of enhancement type
	@Override
	public void addModifier(Modifier m) {
		m.addPropertyChangeListener(listener);
		modifiers.add(m);
		SizeCategory newValue = getSize();
		pcs.firePropertyChange("value", null, newValue);
	}

	// TODO should probably replace the modifier with an ImmutableModifier of enhancement type
	@Override
	public void removeModifier(Modifier m) {
		modifiers.remove(m);
		m.removePropertyChangeListener(listener);
		SizeCategory newValue = getSize();
		pcs.firePropertyChange("value", null, newValue);
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
		pcs.firePropertyChange("value", null, category);
	}

	public void setBaseSpace(int s) {
		if (s < 0) throw new IllegalArgumentException("Space cannot be negative: " + s);
		space = s;
		pcs.firePropertyChange("space", null, space);
	}

	public void setBaseReach(int r) {
		if (r < 0) throw new IllegalArgumentException("Reach cannot be negative: " + r);
		reach = r;
		pcs.firePropertyChange("reach", null, reach);
	}
}

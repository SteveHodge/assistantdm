package gamesystem;


public class LimitModifier extends AbstractModifier {
	int limit = Integer.MAX_VALUE;
	Modifier modifier;

	public LimitModifier(Modifier m) {
		modifier = m;
		modifier.addPropertyChangeListener(e -> pcs.firePropertyChange("value", null, getModifier()));
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int l) {
		if (l == limit) return;
		limit = l;
		pcs.firePropertyChange("value", null, getModifier());	// we always fire even if the actual modifier didn't change incase someone is interested in the limit
	}

	@Override
	public int getModifier() {
		if (modifier.getModifier() < limit) return modifier.getModifier();
		return limit;
	}

	@Override
	public String getType() {
		return modifier.getType();
	}

	@Override
	public String getSource() {
		return modifier.getSource();
	}

	@Override
	public String getCondition() {
		return modifier.getCondition();
	}

	// TODO is it right to forward this?
	@Override
	public int getID() {
		return modifier.getID();
	}
}
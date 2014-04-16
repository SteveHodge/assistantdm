package gamesystem;


public class DoubleModifier extends AbstractModifier {
	final Modifier modifier;

	public DoubleModifier(Modifier m) {
		modifier = m;
		modifier.addPropertyChangeListener(e -> {
			Object oldValue = e.getOldValue();
			Object doubleOld = oldValue;
			if (oldValue != null && oldValue instanceof Integer) doubleOld = 2*(Integer)oldValue;

			Object newValue = e.getNewValue();
			Object doubleNew = newValue;
			if (newValue != null && newValue instanceof Integer) doubleNew = 2*(Integer)newValue;

			pcs.firePropertyChange(e.getPropertyName(), doubleOld, doubleNew);
		});
	}

	@Override
	public int getModifier() {
		return 2*modifier.getModifier();
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
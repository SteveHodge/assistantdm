package gamesystem;

/*
 * ImmutableModifier - a single modifier that cannot be changed after creation, except to enable or disable it.
 */
public class ImmutableModifier extends AbstractModifier {
	public int modifier;
	public String type;
	public String source;
	public String condition;
	public int id;

	public ImmutableModifier(int mod) {
		this(mod, null, null, null);
	}

	public ImmutableModifier(int mod, String type) {
		this(mod, type, null, null);
	}

	public ImmutableModifier(int mod, String type, String source) {
		this(mod,type,source,null);
	}

	public ImmutableModifier(int mod, String type, String source, String condition) {
		this.modifier = mod;
		if (type != null && type.length() > 0) this.type = type;
		this.source = source;
		this.condition = condition;
	}

	@Override
	public int getModifier() {
		return modifier;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public void setSource(String s) {
		source = s;
	}

	@Override
	public String getCondition() {
		return condition;
	}

	@Override
	public int getID() {
		return id;
	}

	// returns true if all modifier, source, type, and condition match. disabled state is not considered
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Modifier)) return false;
		Modifier m = (Modifier) obj;
		String ours = "" + getType() + "|" + getSource() + "|" + getCondition() + "|" + getModifier();
		String other = "" + m.getType() + "|" + m.getSource() + "|" + m.getCondition() + "|" + m.getModifier();
		return ours.equals(other);
	}

	@Override
	public int hashCode() {
		String ours = "" + getType() + "|" + getSource() + "|" + getCondition() + "|" + getModifier();
		return ours.hashCode();
	}
}
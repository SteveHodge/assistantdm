package gamesystem.core;

public interface SettableBaseValueProperty<T> extends OverridableProperty<T> {
	void setBaseValue(T val);
}

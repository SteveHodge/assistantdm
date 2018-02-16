package gamesystem.core;

public interface SettableRegularValueProperty<T> extends OverridableProperty<T> {
	void setRegularValue(T val);
}

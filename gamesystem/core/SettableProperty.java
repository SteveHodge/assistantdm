package gamesystem.core;

public interface SettableProperty<T> extends Property<T> {
	void setValue(T val);
}

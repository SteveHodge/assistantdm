package util;

public interface ModuleListener<M extends Module> {
	// fired when a module of the type M is registered. will fire as soon as the
	// listener is added if a module of type M is already registered.
	void moduleRegistered(M module);

	// fired immediately before a module of type M is removed from the registry.
	void moduleRemoved(M module);
}

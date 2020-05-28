package util;

/* Modules should implement a subinterface of this interface. The subinterface is used
 * as a key in ModuleRegistry; only one module of each type of interface is permitted.
 *
 * Modules should register themselves with the registry, but note that any listener
 * will be immediately notified so the module should be ready for client accesses as soon as
 * ModuleRegistry.register() is called. Modules that can be subclassed need particular
 * care - the subclass should registry itself, the superclass should not (otherwise a
 * partially initialised module will be registred with the wrong class).
 */

public interface Module {
	void moduleExit();
}

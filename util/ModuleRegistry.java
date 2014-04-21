package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Modules are independent parts of the system. There is some limited interaction between
 * modules, the ModuleRegistry facilitates this interaction by providing a central registry
 * for module discovery and module startup/shutdown notification.
 *
 * Modules in the system:
 * * EncounterModule - the combat panel (and eventually general encounters)
 * * CameraModule - access to images from the camera
 * * DigitalTableModule - access to the digital table controller (perhaps split off remote display)
 */

public class ModuleRegistry {
	private static Map<Class<? extends Module>, Module> modules = new HashMap<>();
	private static Map<Module, Class<? extends Module>> moduleClasses = new HashMap<>();
	private static Map<Class<? extends Module>, List<ModuleListener<? extends Module>>> listeners = new HashMap<>();

	public static <M extends Module> void register(Class<M> c, M module) {
//		System.out.println("Registered " + c + ": " + module.hashCode());
		if (modules.containsKey(c)) {
			remove(modules.get(c));
		}
		modules.put(c, module);
		moduleClasses.put(module, c);
		fireModuleRegisterEvent(module);
	}

	public static void remove(Module m) {
		if (moduleClasses.containsKey(m)) {
//			System.out.println("Removed " + m);
			fireModuleRemovedEvent(m);
			modules.remove(moduleClasses.get(m));
			moduleClasses.remove(m);
		}
	}

	public static <M extends Module> M getModule(Class<M> c) {
		return c.cast(modules.get(c));
	}

	public static void exit() {
		System.out.println("ModuleRegistry.exit()");
		for (Module m : modules.values()) {
			fireModuleRemovedEvent(m);
			m.moduleExit();
		}
	}

	@SuppressWarnings("unchecked")
	private static <M extends Module> void fireModuleRegisterEvent(M module) {
		Class<? extends Module> c = moduleClasses.get(module);
		List<ModuleListener<? extends Module>> modListeners = listeners.get(c);
//		System.out.println("Registered: Module class: " + c + ", listeners: " + modListeners);
		if (modListeners != null) {
			for (ModuleListener<? extends Module> l : modListeners) {
				((ModuleListener<M>) l).moduleRegistered(module);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <M extends Module> void fireModuleRemovedEvent(M module) {
		Class<? extends Module> c = moduleClasses.get(module);
		List<ModuleListener<? extends Module>> modListeners = listeners.get(c);
//		System.out.println("Removed: Module class: " + c + ", listeners: " + modListeners);
		if (modListeners != null) {
			for (ModuleListener<? extends Module> l : modListeners) {
				((ModuleListener<M>) l).moduleRemoved(module);
			}
		}
	}

	// this will immediately fire a moduleRegistered event if a module of class c is already registered
	public static <M extends Module> void addModuleListener(Class<M> c, ModuleListener<M> l) {
		List<ModuleListener<?>> modListeners = listeners.get(c);
		if (modListeners == null) {
			modListeners = new ArrayList<>();
			listeners.put(c, modListeners);
		}
		modListeners.add(l);
		Module m = modules.get(c);
		if (m != null) l.moduleRegistered(c.cast(m));
	}

	public static <M extends Module> void removeModuleListener(Class<M> c, ModuleListener<M> l) {
		List<ModuleListener<?>> modListeners = listeners.get(c);
		if (modListeners != null) {
			listeners.remove(l);
		}
	}
}

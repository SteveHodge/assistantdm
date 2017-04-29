package digital_table.controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.function.Consumer;

import digital_table.elements.MapElement;
import digital_table.server.MemoryLog;
import digital_table.server.TableDisplay;
import javafx.application.Platform;

/**
 * RemoteConnection provides the local interface to the remote display. It insulates clients
 * from any exceptions thrown during communication and in the future will provided threaded
 * communication and automatic retries for failed messages.
 *
 * @author Steve
 *
 */

public class RemoteConnection {
	private TableDisplay remote;

	private RemoteConnection(TableDisplay remote) {
		this.remote = remote;
	}

	static void attemptConnection(String server, Consumer<RemoteConnection> consumer) {
		TableDisplay remote = null;
		try {
			String name = "TableDisplay";
			Registry registry = LocateRegistry.getRegistry(server);
			remote = (TableDisplay) registry.lookup(name);
		} catch (Exception e) {
			System.err.println("TableDisplay exception:" + e.getMessage());
			//e.printStackTrace();
		}

		if (remote != null) {
			RemoteConnection rc = new RemoteConnection(remote);
			Platform.setImplicitExit(false);
			final MonitorConfigFrame f = new MonitorConfigFrame(remote);
			f.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					if (f.openScreens) rc.openScreens(f, consumer);
				}
			});
		}
	}


	private void openScreens(MonitorConfigFrame f, Consumer<RemoteConnection> consumer) {
		try {
			for (int i = 0; i < f.screenNums.length; i++) {
				if (f.screenNums[i] >= 0) {
					DisplayConfig.Screen s = DisplayConfig.screens.get(f.screenNums[i]);
					s.location = DisplayConfig.defaultLocations[i];
					s.open = true;
				}
			}
			remote.showScreens(f.screenNums, DisplayConfig.defaultLocations);
			consumer.accept(this);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void requestExit() {
		try {
			remote.requestExit();
		} catch (RemoteException e) {
			System.err.println("remote.requestExit() failed: " + e);
		}
	}

	public void setOffset(int offx, int offy) {
		try {
			remote.setOffset(offx, offy);
		} catch (RemoteException e) {
			System.err.printf("remote.setOffset(%d, %d) failed: %s\n", offx, offy, e);
		}
	}

	public void addElement(MapElement element) {
		try {
			remote.addElement(element);
		} catch (RemoteException e) {
			System.err.printf("remote.addElement(%s) failed: %s\n", element, e);
		}
	}

	public void addElement(MapElement element, MapElement parent) {
		try {
			remote.addElement(element, parent.getID());
		} catch (RemoteException e) {
			System.err.printf("remote.addElement(%s, %s) failed: %s\n", element, parent, e);
		}
	}

	public void removeElement(int id) {
		try {
			remote.removeElement(id);
		} catch (RemoteException e) {
			System.err.printf("remote.removeElement(%s) failed: %s\n", id, e);
		}
	}

	public void changeParent(MapElement element, MapElement parent) {
		try {
			remote.changeParent(element.getID(), parent == null ? -1 : parent.getID());
		} catch (RemoteException e) {
			System.err.printf("remote.changeParent(%s, %s) failed: %s\n", element, parent, e);
		}
	}

	public void promoteElement(MapElement element) {
		try {
			remote.promoteElement(element.getID());
		} catch (RemoteException e) {
			System.err.printf("remote.promoteElement(%s) failed: %s\n", element, e);
		}
	}

	public void demoteElement(MapElement element) {
		try {
			remote.demoteElement(element.getID());
		} catch (RemoteException e) {
			System.err.printf("remote.demoteElement(%s) failed: %s\n", element, e);
		}
	}

	public void reorganiseBefore(MapElement el1, MapElement el2) {
		try {
			remote.reorganiseBefore(el1.getID(), el2.getID());
		} catch (RemoteException e) {
			System.err.printf("remote.moveBefore(%s, %s) failed: %s\n", el1, el2, e);
		}
	}

	public void setElementProperty(MapElement element, String property, Object value) {
		try {
			remote.setElementProperty(element.getID(), property, value);
		} catch (RemoteException e) {
			System.err.printf("remote.setElementProperty(%s, %s, %s) failed: %s\n", element, property, (value == null ? "<null>" : value.toString()), e);
		}
	}

	public boolean hasMedia(URI uri) {
		try {
			return remote.hasMedia(uri);
		} catch (RemoteException e) {
			System.err.printf("hasMedia(%s) failed: %s\n", uri.toString(), e);
		}
		return false;
	}

	public void addMedia(URI uri, byte[] bytes) {
		try {
			remote.addMedia(uri, bytes);
		} catch (RemoteException e) {
			System.err.printf("addMedia(%s, bytes) failed: %s\n", uri.toString(), e);
		}
	}

	public MemoryLog getMemoryUsage() {
		try {
			return remote.getMemoryUsage();
		} catch (RemoteException e) {
			System.err.printf("getMemoryUsage() failed: %s\n", e);
		}
		return null;
	}
}

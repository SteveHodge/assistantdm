package digital_table.controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import digital_table.elements.MapElement;
import digital_table.server.MeasurementLog;
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

@SuppressWarnings("restriction")
public class RemoteConnection {
	interface RemoteOperation {
		void execute() throws RemoteException;
	}

	class Operation {
		String description;
		RemoteOperation operation;

		Operation(String desc, RemoteOperation op) {
			description = desc;
			operation = op;
		}

		void execute() {
			try {
				operation.execute();
			} catch (RemoteException e) {
				System.err.printf("remote.%s failed: %s\n", description, e);
			}
		}

		@Override
		public
		String toString() {
			return description;
		}
	}

	private TableDisplay remote;
	private final BlockingQueue<Operation> queue;

	private RemoteConnection(TableDisplay remote) {
		this.remote = remote;
		queue = new LinkedBlockingQueue<>();
		new Thread(new Sender()).start();
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

	// addElement needs to be synchronous as it can't be serialised after being added to the display (also we don't want to send property changes that were supposed to be local)
	public void addElement(MapElement element) {
		try {
			remote.addElement(element);
		} catch (RemoteException e) {
			System.err.printf("remote.addElement(%s) failed: %s\n", element, e);
		}
	}

	// addElement needs to be synchronous as it can't be serialised after being added to the display (also we don't want to send property changes that were supposed to be local)
	public void addElement(MapElement element, MapElement parent) {
		try {
			remote.addElement(element, parent.getID());
		} catch (RemoteException e) {
			System.err.printf("remote.addElement(%s, %s) failed: %s\n", element, parent, e);
		}
	}

	public void removeElement(int id) {
		queue.add(new Operation(String.format("removeElement(%s)", id), () -> {
			remote.removeElement(id);
		}));
	}

	public void changeParent(MapElement element, MapElement parent) {
		queue.add(new Operation(String.format("changeParent(%s, %s)", element, parent), () -> {
			remote.changeParent(element.getID(), parent == null ? -1 : parent.getID());
		}));
	}

	public void promoteElement(MapElement element) {
		queue.add(new Operation(String.format("promoteElement(%s)", element), () -> {
			remote.promoteElement(element.getID());
		}));
	}

	public void demoteElement(MapElement element) {
		queue.add(new Operation(String.format("demoteElement(%s)", element), () -> {
			remote.demoteElement(element.getID());
		}));
	}

	public void reorganiseBefore(MapElement el1, MapElement el2) {
		queue.add(new Operation(String.format("moveBefore(%s, %s)", el1, el2), () -> {
			remote.reorganiseBefore(el1.getID(), el2.getID());
		}));
	}

	public void setElementProperty(MapElement element, String property, Object value) {
		queue.add(new Operation(String.format("setElementProperty(%s, %s, %s)", element, property, (value == null ? "<null>" : value.toString())), () -> {
			remote.setElementProperty(element.getID(), property, value);
		}));
	}

	public boolean hasMedia(URI uri) {
		try {
			return remote.hasMedia(uri);
		} catch (RemoteException e) {
			System.err.printf("remote.hasMedia(%s) failed: %s\n", uri.toString(), e);
		}
		return false;
	}

	public void addMedia(URI uri, byte[] bytes) {
		try {
			remote.addMedia(uri, bytes);
		} catch (RemoteException e) {
			System.err.printf("remote.addMedia(%s, bytes) failed: %s\n", uri.toString(), e);
		}
	}

	public MeasurementLog getMemoryUsage() {
		try {
			return remote.getMemoryUsage();
		} catch (RemoteException e) {
			System.err.printf("remote.getMemoryUsage() failed: %s\n", e);
		}
		return null;
	}

	public MeasurementLog getPaintTiming() {
		try {
			return remote.getPaintTiming();
		} catch (RemoteException e) {
			System.err.printf("remote.getPaintTiming() failed: %s\n", e);
		}
		return null;
	}

	class Sender implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					Operation op = queue.take();
//					if (queue.size() > 1)
//						System.out.println("Queue backlog = " + queue.size());
					op.execute();
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
}

package digital_table.controller;

import java.net.URI;
import java.rmi.RemoteException;

import digital_table.elements.MapElement;
import digital_table.server.MediaManager;
import digital_table.server.TableDisplay;

/**
 * DisplayManager provides the interface between elements and the various displays. It insulates the elements from
 * needing to know the number or details of the displays.
 * 
 * Currently supports 3 displays: the LOCAL display intended for the DM, the REMOTE display for the digital table,
 * and the OVERLAY display that shows tokens overlaid on the camera image on the website.
 * 
 * @author Steve
 * 
 */

class DisplayManager {
	enum Mode {
		ALL,		// all displays
		LOCAL,		// only the local (DM's) display
		REMOTE,		// the remote display and the webpage camera image overlay
		OVERLAY		// only the webpage camera image overlay
	}

	private TableDisplay remote;
	private MiniMapPanel local;
	private TokenOverlay overlay;

	DisplayManager(TableDisplay remote, MiniMapPanel local, TokenOverlay overlay) {
		this.remote = remote;
		this.local = local;
		this.overlay = overlay;
	}

	void requestExit() {
		try {
			remote.requestExit();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

// --- not currently used as we only proxy for the remote display after the screens have been configured
//	Rectangle[] getScreenBounds() {
//		try {
//			return remote.getScreenBounds();
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	void setScreenIDsVisible(boolean visible) {
//		try {
//			remote.setScreenIDsVisible(visible);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//	}
//
//	void showScreens(int[] screenNums, Point[] offsets) {
//		try {
//			remote.showScreens(screenNums, offsets);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//	}

	void addElement(MapElement element, MapElement parent) {
		addElement(element, parent, element);
	}

	void addElement(MapElement element, MapElement parent, MapElement localEl) {
		try {
			if (parent == null) {
				remote.addElement(element);
			} else {
				remote.addElement(element, parent.getID());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (overlay != null) overlay.addElement(localEl, parent);
		local.addElement(localEl, parent);	// local must be last to avoid issues with serialisation
	}

	void removeElement(MapElement element) {
		local.removeElement(element.getID());
		if (overlay != null) overlay.removeElement(element.getID());
		try {
			remote.removeElement(element.getID());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	void changeParent(MapElement element, MapElement parent) {
		local.changeParent(element, parent);
		if (overlay != null) overlay.changeParent(element.getID(), parent == null ? -1 : parent.getID());
		try {
			remote.changeParent(element.getID(), parent == null ? -1 : parent.getID());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	void promoteElement(MapElement element) {
		local.promoteElement(element);
		if (overlay != null) overlay.promoteElement(element);
		try {
			remote.promoteElement(element.getID());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	void demoteElement(MapElement element) {
		local.demoteElement(element);
		if (overlay != null) overlay.demoteElement(element);
		try {
			remote.demoteElement(element.getID());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	void setProperty(MapElement element, String property, Object value) {
		setProperty(element, property, value, Mode.ALL);
	}

	void setProperty(MapElement element, String property, Object value, Mode mode) {
		if (mode != Mode.LOCAL) {
			if (mode != Mode.OVERLAY) {
				try {
					remote.setElementProperty(element.getID(), property, value);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			if (overlay != null) overlay.setProperty(element.getID(), property, value);
		}
		if (mode != Mode.REMOTE && mode != Mode.OVERLAY) element.setProperty(property, value);
	}

	void setMedia(MapElement element, String property, URI uri) {
		byte[] bytes = MediaManager.INSTANCE.getFile(uri);

		try {
			if (!remote.hasMedia(uri)) {
				remote.addMedia(uri, bytes);
			}
			remote.setElementProperty(element.getID(), property, uri);

		} catch (RemoteException e) {
			e.printStackTrace();
		}

		element.setProperty(property, uri);
	}
}

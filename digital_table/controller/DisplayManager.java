package digital_table.controller;

import java.awt.geom.Point2D;
import java.net.URI;

import digital_table.elements.MapElement;
import digital_table.server.CoordinateConverter;
import digital_table.server.MapCanvas;
import digital_table.server.MediaManager;

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

class DisplayManager implements CoordinateConverter {
	enum Mode {
		ALL,		// all displays
		LOCAL,		// only the local (DM's) display
		REMOTE,		// the remote display and the webpage camera image overlay
		OVERLAY		// only the webpage camera image overlay
	}

	private RemoteConnection remote;
	private MiniMapCanvas local;
	private TokenOverlay overlay;

	private int xoffset = 0;	// cached values of any offsets applied to the remote display
	private int yoffset = 0;

	void setLocal(MiniMapCanvas local) {
		this.local = local;
	}

	void setRemote(RemoteConnection remote) {
		this.remote = remote;
	}

	void setOverlay(TokenOverlay overlay) {
		this.overlay = overlay;
	}

	void requestExit() {
		if (remote != null) remote.requestExit();
	}

	void setRemoteOffset(int offx, int offy) {
		if (remote != null) remote.setOffset(offx, offy);
		if (overlay != null) overlay.setOffset(offx, offy);
		xoffset = offx;
		yoffset = offy;
	}

	void addElement(MapElement element, MapElement parent) {
		addElement(element, parent, element);
	}

	void addElement(MapElement element, MapElement parent, MapElement localEl) {
		if (remote != null) {
			if (parent == null) {
				remote.addElement(element);
			} else {
				remote.addElement(element, parent);
			}
		}
		if (overlay != null) overlay.addElement(localEl, parent);
		if (local != null) local.addElement(localEl, parent);	// local must be last to avoid issues with serialisation
	}

	void removeElement(MapElement element) {
		if (local != null) local.removeElement(element.getID());
		if (overlay != null) overlay.removeElement(element.getID());
		if (remote != null) remote.removeElement(element.getID());
	}

	void changeParent(MapElement element, MapElement parent) {
		if (local != null) local.changeParent(element, parent);
		if (overlay != null) overlay.changeParent(element, parent);
		if (remote != null) remote.changeParent(element, parent);
	}

	void promoteElement(MapElement element) {
		if (local != null) local.promoteElement(element);
		if (overlay != null) overlay.promoteElement(element);
		if (remote != null) remote.promoteElement(element);
	}

	void demoteElement(MapElement element) {
		if (local != null) local.demoteElement(element);
		if (overlay != null) overlay.demoteElement(element);
		if (remote != null) remote.demoteElement(element);
	}

	void setProperty(MapElement element, String property, Object value) {
		setProperty(element, property, value, Mode.ALL);
	}

	void setProperty(MapElement element, String property, Object value, Mode mode) {
		if (mode != Mode.LOCAL) {
			if (mode != Mode.OVERLAY) {
				if (remote != null) remote.setElementProperty(element, property, value);
			}
			if (overlay != null) overlay.setProperty(element, property, value);
		}
		if (mode != Mode.REMOTE && mode != Mode.OVERLAY) element.setProperty(property, value);
	}

	void setMedia(MapElement element, String property, URI uri) {
		if (remote != null) {
			if (uri == null) {
				remote.setElementProperty(element, property, null);
			} else {
				byte[] bytes = MediaManager.INSTANCE.getFile(uri);

				if (!remote.hasMedia(uri)) {
					remote.addMedia(uri, bytes);
				}
				remote.setElementProperty(element, property, uri);
			}
		}

		element.setProperty(property, uri);
	}

	// CoordinateConverter methods
	// these convert coordinates in the remote display's coordinate system to the remote grid coordinate system
	// the conversion is performed locally using cached information on the required coordinate systems

	@Override
	public Point2D convertDisplayCoordsToGrid(int width, int height) {
		double col = (double) width * MapCanvas.RESOLUTION_DENOMINATOR / MapCanvas.RESOLUTION_NUMERATOR;
		double row = (double) height * MapCanvas.RESOLUTION_DENOMINATOR / MapCanvas.RESOLUTION_NUMERATOR;
		return new Point2D.Double(col, row);
	}

	@Override
	public Point2D convertDisplayCoordsToCanvas(int x, int y) {
		double col = (double) x * MapCanvas.RESOLUTION_DENOMINATOR / MapCanvas.RESOLUTION_NUMERATOR;
		double row = (double) y * MapCanvas.RESOLUTION_DENOMINATOR / MapCanvas.RESOLUTION_NUMERATOR;
		return new Point2D.Double(col + xoffset, row + yoffset);
	}

	@Override
	public int getXOffset() {
		return xoffset;
	}

	@Override
	public int getYOffset() {
		return yoffset;
	}
}

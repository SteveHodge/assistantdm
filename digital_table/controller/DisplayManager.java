package digital_table.controller;

import java.awt.geom.Point2D;
import java.net.URI;

import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Layer;
import digital_table.server.CoordinateConverter;
import digital_table.server.MapCanvas;
import digital_table.server.MeasurementLog;
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

// XXX currently RemoteImageDisplay will force TokenOverlay repaints during it's own repaint. Therefore all changes
// to the RemoteImageDisplay need to be done after changes to the TokenOverlay

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
	private RemoteImageDisplay image;

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

	void setRemoteImageDisplay(RemoteImageDisplay imgDisp) {
		image = imgDisp;
	}

	void requestExit() {
		if (remote != null) remote.requestExit();
	}

	void setRemoteOffset(int offx, int offy) {
		if (remote != null) remote.setOffset(offx, offy);
		if (overlay != null) overlay.setOffset(offx, offy);
		if (image != null) image.setOffset(offx, offy);
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
		if (image != null) image.addElement(localEl, parent);
		if (local != null) local.addElement(localEl, parent);	// local must be last to avoid issues with serialisation
	}

	void removeElement(MapElement element) {
		if (local != null) local.removeElement(element.getID());
		if (overlay != null) overlay.removeElement(element.getID());
		if (image != null) image.removeElement(element.getID());
		if (remote != null) remote.removeElement(element.getID());
	}

	void changeParent(MapElement element, MapElement parent) {
		if (local != null) local.changeParent(element, parent);
		if (overlay != null) overlay.changeParent(element, parent);
		if (image != null) image.changeParent(element, parent);
		if (remote != null) remote.changeParent(element, parent);
	}

	void promoteElement(MapElement element) {
		if (local != null) local.promoteElement(element);
		if (overlay != null) overlay.promoteElement(element);
		if (image != null) image.promoteElement(element);
		if (remote != null) remote.promoteElement(element);
	}

	void demoteElement(MapElement element) {
		if (local != null) local.demoteElement(element);
		if (overlay != null) overlay.demoteElement(element);
		if (image != null) image.demoteElement(element);
		if (remote != null) remote.demoteElement(element);
	}

	void setProperty(MapElement element, String property, Object value) {
		setProperty(element, property, value, Mode.ALL);
	}

	void setProperty(MapElement element, String property, Object value, Mode mode) {
		if (mode != Mode.LOCAL) {
			if (overlay != null) overlay.setProperty(element, property, value);
			if (mode != Mode.OVERLAY) {
				if (remote != null) remote.setElementProperty(element, property, value);
				if (image != null) image.setProperty(element, property, value);
			}
		}
		if (mode != Mode.REMOTE && mode != Mode.OVERLAY) {
			if (property.equals(MapElement.PROPERTY_LAYER)) {
				local.changeLayer(element, (Layer) value);
			} else {
				element.setProperty(property, value);
			}
		}
	}

	// this doesn't bother setting the property on the overlay element
	void setMedia(MapElement element, String property, URI uri) {
		if (remote != null) {
			if (uri == null) {
				remote.setElementProperty(element, property, null);
			} else {
				byte[] bytes = MediaManager.INSTANCE.getFileContents(uri);

				if (!remote.hasMedia(uri)) {
					remote.addMedia(uri, bytes);
				}
				remote.setElementProperty(element, property, uri);
			}
		}

		if (image != null) image.setProperty(element, property, uri);
		element.setProperty(property, uri);
	}

	public MeasurementLog getRemoteMemoryUsage() {
		if (remote == null)
			return null;
		return remote.getMemoryUsage();
	}

	public MeasurementLog getRemotePaintTiming() {
		if (remote == null)
			return null;
		return remote.getPaintTiming();
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

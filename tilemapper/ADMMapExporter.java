package tilemapper;

import java.io.File;
import java.net.URI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tilemapper.MapPanel.PlacedTile;

public class ADMMapExporter implements MapProcessor {
	protected Document doc;
	protected Element mapEl;
	protected URI baseURI;

	public ADMMapExporter(Document d, File baseDir) {
		doc = d;
		baseURI = baseDir.toURI();
	}

	@Override
	public void ProcessMap(MapPanel map) {
		if (doc == null) return;

		mapEl = doc.createElement("Group");
		mapEl.setAttribute("label", "tilemap");
		mapEl.setAttribute("location", "0.0,0.0");
		mapEl.setAttribute("remote_visibility", "HIDDEN");
		Element elementsEl = doc.createElement("Elements");
		elementsEl.appendChild(mapEl);
		Element encounterEl = doc.createElement("Encounter");
		encounterEl.appendChild(elementsEl);
		doc.appendChild(encounterEl);
	}

	@Override
	public void ProcessTile(PlacedTile tile) {
		Element tileEl = doc.createElement("Image");
		URI tileURI = tile.tile.file.toURI();
		tileURI = baseURI.relativize(tileURI);
		tileEl.setAttribute("uri", tileURI.toString());
		tileEl.setAttribute("location", "" + tile.x + "," + tile.y);
		tileEl.setAttribute("rotations", "" + tile.orientation);
		tileEl.setAttribute("height", "" + tile.getHeight());
		tileEl.setAttribute("width", "" + tile.getWidth());
		tileEl.setAttribute("label", (tile.getIndex() + 1) + " - " + tile.tile.file.getName());
		tileEl.setAttribute("mirrored", tile.tile.mirrored ? "true" : "false");

		tileEl.setAttribute("alpha", "1.0");
		tileEl.setAttribute("aspect_locked", "true");
		tileEl.setAttribute("background", "#FFFFFF");
		tileEl.setAttribute("remote_visible", "HIDDEN");
		tileEl.setAttribute("show_background", "false");
		tileEl.setAttribute("show_border", "false");
		tileEl.setAttribute("visible", "VISIBLE");

		mapEl.appendChild(tileEl);
	}
}

package tilemapper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tilemapper.MapPanel.PlacedTile;

public class XMLMapExporter implements MapProcessor {
	protected Document doc;
	protected Element mapEl;

	public XMLMapExporter(Document d) {
		doc = d;
	}

	@Override
	public void ProcessMap(MapPanel map) {
		if (doc == null) return;

		mapEl = doc.createElement("Map");
		doc.appendChild(mapEl);
	}

	@Override
	public void ProcessTile(PlacedTile tile) {
		Element tileEl = doc.createElement("Tile");
		tileEl.setAttribute("file", tile.tile.file.getName());
		tileEl.setAttribute("x", "" + tile.x);
		tileEl.setAttribute("y", "" + tile.y);
		tileEl.setAttribute("orientation", "" + tile.orientation);
		if (tile.tile.mirrored) tileEl.setAttribute("mirrored", "true");
		mapEl.appendChild(tileEl);
	}
}

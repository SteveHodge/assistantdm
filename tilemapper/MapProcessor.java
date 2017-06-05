package tilemapper;

import tilemapper.MapPanel.PlacedTile;

public interface MapProcessor {
	void ProcessMap(MapPanel map);

	void ProcessTile(PlacedTile tile);
}

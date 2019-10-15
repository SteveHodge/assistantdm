package tilemapper;

import java.beans.PropertyChangeListener;
import java.util.List;

public interface TilesFilter {
	List<Tile> getTiles();

	void addPropertyChangeListener(PropertyChangeListener l);
}

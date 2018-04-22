package gamesystem.core;

import java.util.EventListener;

public interface PropertyListener extends EventListener {
	public void propertyChanged(PropertyEvent event);
}

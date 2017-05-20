package tilemapper;

import java.awt.Component;


public interface DragSource {
	public Component getDragComponent();
	public void dragFinished(boolean cancelled);
}

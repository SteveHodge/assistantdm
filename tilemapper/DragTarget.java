package tilemapper;

import java.awt.Component;
import java.awt.Point;


public interface DragTarget {
	public Component dragEntered(Component source);
	public void dragExited(Component source);
	public void dragCompleted(Point p);
}

package digital_table.server;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.rmi.Remote;
import java.rmi.RemoteException;

import digital_table.elements.MapElement;


public interface TableDisplay extends Remote {
	public void requestExit() throws RemoteException;
	public Rectangle[] getScreenBounds() throws RemoteException;
	public void setScreenIDsVisible(boolean visible) throws RemoteException;
	public void showScreens(int[] screenNums, Point[] offsets) throws RemoteException;

	public void addElement(MapElement element) throws RemoteException;
	public void removeElement(int id) throws RemoteException;
	public void reorderElement(int id, int index) throws RemoteException;
	public void setElementProperty(int id, String property, Object value) throws RemoteException;
	public void setElementVisible(int id, boolean visible) throws RemoteException;
	public void setElementLocation(int id, Object target, Point2D point) throws RemoteException;
	public void elementClicked(int id, Point2D mouse, MouseEvent evt, boolean dragging) throws RemoteException;
}

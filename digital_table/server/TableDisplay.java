package digital_table.server;

import java.awt.Point;
import java.awt.Rectangle;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;

import digital_table.elements.MapElement;

public interface TableDisplay extends Remote {
	public void requestExit() throws RemoteException;

	public Rectangle[] getScreenBounds() throws RemoteException;

	public void setScreenIDsVisible(boolean visible) throws RemoteException;

	public void showScreens(int[] screenNums, Point[] offsets) throws RemoteException;

	public void addElement(MapElement element) throws RemoteException;

	public void addElement(MapElement element, int parent) throws RemoteException;

	public void removeElement(int id) throws RemoteException;

	public void changeParent(int id, int parent) throws RemoteException;

	public void promoteElement(int id) throws RemoteException;

	public void demoteElement(int id) throws RemoteException;

	public void setElementProperty(int id, String property, Object value) throws RemoteException;

	public boolean hasMedia(URI uri) throws RemoteException;

	public void addMedia(URI uri, byte[] bytes) throws RemoteException;
}

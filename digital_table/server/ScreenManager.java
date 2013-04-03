package digital_table.server;

import javax.swing.JComponent;

public interface ScreenManager {
	public void addComponent(int elementID, JComponent component, int screen);
	public void removeComponent(int elementID, JComponent component);
}

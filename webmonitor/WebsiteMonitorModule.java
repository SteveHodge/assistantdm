package webmonitor;

import util.Module;

public interface WebsiteMonitorModule extends Module {
	public void addMessageListener(WebsiteMessageListener l);

	public void removeMessageListener(WebsiteMessageListener l);

	public void requestInitiative();
}

package webmonitor;

import java.util.EventListener;

import party.Character;

public interface WebsiteMessageListener extends EventListener {
	void addMessage(Character c, String message);
}

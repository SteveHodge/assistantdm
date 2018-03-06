package util;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;

import org.json.JSONObject;

import digital_table.controller.DigitalTableController;
import party.Character;
import party.Party;

public class WebsiteMonitor implements Module {
	Party party;
	DigitalTableController dtt;
	SseEventSource eventSource;

	public static String URL = "http://armitage/assistantdm/updates/dm";

	public WebsiteMonitor(Party party, DigitalTableController controller) {
		this.party = party;
		dtt = controller;
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);

		eventSource = SseEventSource.target(target).build();
		eventSource.register(e -> {
			String data = e.readData();

			if (data != null && data.length() > 0 && !data.equals("x")) {
				System.out.println("got message: '" + data + "'");

				JSONObject update = new JSONObject(data);

				String name = update.getString("name");
				Character character = party.get(name);
				if (character == null) {
					System.out.println("Unknown character: " + name);
				} else if (update.has("moveto")) {
					String newLoc = update.getString("moveto");
					System.out.println("Moving " + character.getName() + " to " + newLoc);
					dtt.moveToken(character, newLoc);
				} else {
					System.out.println("got message: '" + data + "'");
				}
			}
		});

		eventSource.open();
		ModuleRegistry.register(WebsiteMonitor.class, this);
	}

	@Override
	public void moduleExit() {
		if (eventSource != null) eventSource.close();
	}
}

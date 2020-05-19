package webmonitor;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;

import org.json.JSONObject;

import combat.CombatPanel;
import digital_table.controller.DigitalTableController;
import party.Character;
import party.Party;
import util.Module;
import util.ModuleRegistry;

public class WebsiteMonitor implements Module {
	Party party;
	DigitalTableController dtt;
	SseEventSource eventSource;
	MonitorFrame frame;
	SubscriberModel subModel;
	CombatPanel combat;
	Map<Character, JDialog> dialogs = new HashMap<>();

	public static String URL = "http://armitage/assistantdm/updates/dm";

	public WebsiteMonitor(Party party, DigitalTableController controller, CombatPanel combat) {
		this.party = party;
		dtt = controller;
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);

		subModel = new SubscriberModel();

		eventSource = SseEventSource.target(target).build();
		eventSource.register(e -> {
			String data = e.readData();

			if (data != null && data.length() > 0 && !data.equals("x")) {
//				System.out.println("got message: '" + data + "'");

				JSONObject update = new JSONObject(data);

				String name = update.getString("name");
				Character character = party.get(name);
				String type = update.getString("type");
				if (character == null) {
					System.out.println("Unknown character: " + name);

				} else if (type.equals("move") && update.has("moveto")) {
					String newLoc = update.getString("moveto");
					System.out.println("Moving " + character.getName() + " to " + newLoc);
					dtt.moveToken(character, newLoc);

				} else if (type.equals("roll")) {
					String text = name + " rolled " + update.getString("dice-type") + " for " + update.getString("title") + " " + update.getString("suffix") + ": "
							+ update.getJSONArray("rolls").join(" + ");
					int mod = update.getInt("mod");
					if (mod != 0) {
						text += (mod > 0 ? " + " : " ") + mod;
					}
					text += " = " + update.getInt("total");
					System.out.println(text);
					frame.addMessage(text);

					if (update.getString("title").equals("initiative")) {
						JDialog dialog = dialogs.get(character);
						if (dialog != null) {
							dialog.setVisible(false);
							dialog.dispose();
						}

						JOptionPane pane = new JOptionPane(text + "\nApply the roll?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
						dialog = pane.createDialog(frame, name + " rolled for initiative");
						dialog.setModal(false);
						dialog.addComponentListener(new ComponentListener() {
							@Override
							public void componentHidden(ComponentEvent e) {
								if (pane.getValue() != null && pane.getValue() instanceof Integer) {
									int option = (int) pane.getValue();
									if (option == JOptionPane.OK_OPTION) {
										System.out.println("applying");
										int roll = update.getInt("total") - mod;
										combat.getInitiativeListModel().setRoll(character, roll);
									} else {
									}
								}
							}

							@Override
							public void componentMoved(ComponentEvent e) {
							}

							@Override
							public void componentResized(ComponentEvent e) {
							}

							@Override
							public void componentShown(ComponentEvent e) {
							}

						});
						dialogs.put(character, dialog);
						dialog.setVisible(true);
					}

				} else if (type.equals("subscribe") || type.equals("unsubscribe")) {
					subModel.handleMessage(update);

				} else {
					System.out.println("got message: '" + data + "'");
				}
			}
		});

		eventSource.open();
		ModuleRegistry.register(WebsiteMonitor.class, this);

		frame = new MonitorFrame(subModel);
		frame.setVisible(true);
	}

	@Override
	public void moduleExit() {
		if (eventSource != null) eventSource.close();
	}
}

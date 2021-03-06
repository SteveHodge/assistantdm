package webmonitor;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;

import org.json.JSONArray;
import org.json.JSONObject;

import combat.EncounterModule;
import digital_table.controller.DigitalTableModule;
import party.Character;
import party.Party;
import util.Module;
import util.ModuleRegistry;
import util.Updater;
import webmonitor.MonitorFrame.RequestType;
import webmonitor.SubscriberModel.Subscriber;

public class WebsiteMonitor implements WebsiteMonitorModule {
	Party party;
	SseEventSource eventSource;
	MonitorFrame frame;
	SubscriberModel subModel;
	Map<Character, JDialog> dialogs = new HashMap<>();
	EventListenerList listenerList = new EventListenerList();

	public static String URL = "http://armitage/assistantdm/updates/dm";
	public static String SEND_URL = "http://armitage/assistantdm/updates/input";

	public WebsiteMonitor(Party party) {
		this.party = party;

		ModuleRegistry.register(WebsiteMonitorModule.class, this);

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL);

		subModel = new SubscriberModel();

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
				} else {
					handleMessage(character, update, data);
				}
			}
		});

		eventSource.open();

		frame = new MonitorFrame(subModel, this);
		frame.setVisible(true);
	}

	@Override
	public void addMessageListener(WebsiteMessageListener l) {
		listenerList.add(WebsiteMessageListener.class, l);
	}

	@Override
	public void removeMessageListener(WebsiteMessageListener l) {
		listenerList.remove(WebsiteMessageListener.class, l);
	}

	static class RollRequest {
		Character character;
		String type;
		String title;
		String token;
		String dice;

		RollRequest(Character c, String rollType, String title, String dice) {
			character = c;
			type = rollType;
			this.dice = dice;
			this.title = title;
			token = "" + hashCode();
		}

		String getJSON() {
			return "{\n\t\"type\": \"rollreq\",\n"
					+ "\t\"name\": \"" + character.getName() + "\",\n"
					+ "\t\"roll-type\": \"" + type + "\",\n"
					+ "\t\"title\": \"" + title + "\",\n"
					+ "\t\"dice-spec\": \"" + dice + "\",\n"
					+ "\t\"req-token\": \"" + token + "\"\n"
					+ "}";
		}
	}

	Map<String, RollRequest> requests = new HashMap<>();

	@Override
	public void requestInitiative() {
		Set<Character> characters = new HashSet<>();
		for (Subscriber s : subModel.subscribers) {
			Character c = party.get(s.character);
			characters.add(c);
		}

		for (Character c : characters) {
			int mod = c.getInitiativeStatistic().getValue();
			String dice = "1d20" + (mod > 0 ? "+" : "") + mod;
			RollRequest req = new RollRequest(c, "initiative", "initiative", dice);
			requests.put(req.token, req);
			System.out.println("Request Initiative from " + c.getName() + " (" + req.dice + ")");

			Updater.updateURL(SEND_URL, "application/json", req.getJSON().getBytes());
		}
	}

	public void requestRoll(Set<Subscriber> subscribers, RequestType type) {
		Set<Character> characters = new HashSet<>();
		for (Subscriber s : subscribers) {
			Character c = party.get(s.character);
			characters.add(c);
		}

		for (Character c : characters) {
			RollRequest req = new RollRequest(c, type.getRollType(), type.getTitle(), type.getRoll(c));
			requests.put(req.token, req);
			System.out.println("Request " + type.toString() + " from " + c.getName() + " (" + req.dice + ")");
			System.out.println(req.getJSON());

			Updater.updateURL(SEND_URL, "application/json", req.getJSON().getBytes());
		}
	}

	protected void fireMessageEvent(Character c, String message) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == WebsiteMessageListener.class) {
				((WebsiteMessageListener) listeners[i + 1]).addMessage(c, message);
			}
		}
	}

	void handleMessage(Character character, JSONObject update, String data) {
		String type = update.getString("type");
		if (type.equals("move") && update.has("moveto")) {
			String newLoc = update.getString("moveto");
			System.out.println("Moving " + character.getName() + " to " + newLoc);
			moduleDo(DigitalTableModule.class, dtt -> dtt.moveToken(character, newLoc));

		} else if (type.equals("roll")) {
			List<String> rolls = new ArrayList<>();
			int constant = 0;
			JSONArray parts = update.getJSONArray("parts");
			for (int i = 0; i < parts.length(); i++) {
				JSONObject part = parts.getJSONObject(i);
				if (part.has("constant")) {
					constant += part.getInt("constant");
				}
				if (part.has("dice")) {
					JSONArray rollsArray = part.getJSONArray("rolls");
					for (int j = 0; j < rollsArray.length(); j++) {
						int roll = rollsArray.getInt(j);
						rolls.add(Integer.toString(roll));
					}
				}
			}

			String text = character.getName() + " rolled " + update.getString("dice-spec");
			if (update.has("custom-spec") && update.getString("custom-spec").length() > 0) {
				text += " + " + update.getString("custom-spec");
			}
			text += " for " + update.getString("title") + " " + update.getString("suffix");
			if (rolls.size() > 0) {
				text += ": " + String.join(" + ", rolls) + (constant < 0 ? "" : " + ") + constant;
			}
			text += " = " + update.getInt("total");
			System.out.println(text);
			frame.addMessage(text);
			fireMessageEvent(character, text);

			if (update.getString("title").equals("initiative")) {
				JDialog dialog = dialogs.get(character);
				if (dialog != null) {
					dialog.setVisible(false);
					dialog.dispose();
				}

				JOptionPane pane = new JOptionPane(text + "\nApply the roll?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
				dialog = pane.createDialog(frame, character.getName() + " rolled for initiative");
				dialog.setModal(false);
				final int mod = constant;
				dialog.addComponentListener(new ComponentListener() {
					@Override
					public void componentHidden(ComponentEvent e) {
						if (pane.getValue() != null && pane.getValue() instanceof Integer) {
							int option = (int) pane.getValue();
							if (option == JOptionPane.OK_OPTION) {
								System.out.println("applying");
								int roll = update.getInt("total") - mod;
								moduleDo(EncounterModule.class, enc -> enc.setRoll(character, roll));
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

	static <M extends Module> void moduleDo(Class<M> c, Consumer<M> op) {
		M module = ModuleRegistry.getModule(c);
		if (module != null) {
			op.accept(module);
		}
	}

	@Override
	public void moduleExit() {
		if (eventSource != null) eventSource.close();
	}
}

package webmonitor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.json.JSONObject;

public class SubscriberModel extends AbstractTableModel {
	static class Subscriber {
		String token;
		String player;
		String character;
		String ip;
		String userAgent;
	}

	List<Subscriber> subscribers = new ArrayList<>();

	private static final long serialVersionUID = 1L;

	public void handleMessage(JSONObject msg) {
		String type = msg.getString("type");
		if (type.equals("subscribe")) {
			// TODO should check for existing matching subscription
			Subscriber s = new Subscriber();
			s.token = msg.getString("token");
			s.player = msg.getString("player");
			s.character = msg.getString("name");
			s.ip = msg.getString("ip");
			s.userAgent = msg.getString("user-agent");
			subscribers.add(s);
			fireTableRowsInserted(subscribers.size() - 1, subscribers.size() - 1);

		} else if (type.equals("unsubscribe")) {
			String token = msg.getString("token");
			for (int i = 0; i < subscribers.size(); i++) {
				Subscriber s = subscribers.get(i);
				if (token.equals(s.token)) {
					subscribers.remove(i);
					fireTableRowsDeleted(i, i);
					break;
				}
			}
		}
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		return subscribers.size();
	}

	public Subscriber getSubscriber(int row) {
		return subscribers.get(row);
	}

	@Override
	public Object getValueAt(int row, int column) {
		Subscriber s = subscribers.get(row);
		switch (column) {
		case 0:
			return s.player;
		case 1:
			return s.character;
		case 2:
			return s.token;
		case 3:
			return s.ip;
		case 4:
			return s.userAgent;
		}
		return null;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return "Player";
		case 1:
			return "Character";
		case 2:
			return "Token";
		case 3:
			return "IP Address";
		case 4:
			return "User Agent";
		}
		return null;
	}
}

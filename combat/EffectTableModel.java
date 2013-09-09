package combat;

import gamesystem.Buff;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.Character;

// TODO check args, particular index/row args
@SuppressWarnings("serial")
public class EffectTableModel extends AbstractTableModel {
	public final static int EFFECT_COLUMN = 0;
	public final static int SOURCE_COLUMN = 1;
	public final static int INITIATIVE_COLUMN = 2;
	public final static int DURATION_COLUMN = 3;

	private class Effect {
		private String effect;
		private String source;
		private int initiative;
		private int duration;
		private int buff_id;

		private Effect(String effect, String source, int initiative, int duration) {
			this(effect, source, initiative, duration, 0);
		}

		private Effect(String effect, String source, int initiative, int duration, int buff_id) {
			this.effect = effect;
			this.source = source;
			this.initiative = initiative;
			this.duration = duration;
			this.buff_id = buff_id;
		}

//		private String getDurationString() {
//			if (duration >= 900) {
//				return ""+(duration / 600)+" Hours";
//			} else if (duration >= 20) {
//				return ""+(duration / 10)+" Minutes";
//			}
//			return ""+duration+" Rounds";
//		}

		public Element getElement(Document doc) {
			Element e = doc.createElement("EffectEntry");
			e.setAttribute("effect", effect);
			e.setAttribute("source", source);
			e.setAttribute("initiative", "" + initiative);
			e.setAttribute("duration", "" + duration);
			if (buff_id > 0) e.setAttribute("buff_id", "" + buff_id);
			return e;
		}
	}

	private List<Effect> list = new ArrayList<Effect>();

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0 || columnIndex == 1) {
			return String.class;
		} else if (columnIndex == 2 || columnIndex == 3) {
			return Integer.class;
		}
		return super.getColumnClass(columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case EFFECT_COLUMN:
			return "Effect";
		case SOURCE_COLUMN:
			return "Source";
		case INITIATIVE_COLUMN:
			return "Initiative";
		case DURATION_COLUMN:
			return "Duration";
		}
		return super.getColumnName(column);
	}

	@Override
	public Object getValueAt(int row, int col) {
		Effect e = list.get(row);
		switch (col) {
		case EFFECT_COLUMN:
			return e.effect;
		case SOURCE_COLUMN:
			return e.source;
		case INITIATIVE_COLUMN:
			return e.initiative;
		case DURATION_COLUMN:
			return e.duration;
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Effect e = list.get(rowIndex);
		switch (columnIndex) {
		case EFFECT_COLUMN:
			e.effect = aValue.toString();
			return;
		case SOURCE_COLUMN:
			e.source = aValue.toString();
			return;
		case INITIATIVE_COLUMN:
			e.initiative = ((Integer)aValue).intValue();
			return;
		case DURATION_COLUMN:
			e.duration = ((Integer)aValue).intValue();
			return;
		}
		super.setValueAt(aValue, rowIndex, columnIndex);
	}

	int addDuration(int row, int delta) {
		Effect e = list.get(row);
		e.duration += delta;
		fireTableCellUpdated(row, DURATION_COLUMN);
		return e.duration;
	}

	void removeEntry(int index) {
		list.remove(index);
		fireTableRowsDeleted(index, index);
	}

	void addEntry(String effect, String source, int initiative, int duration, Buff buff) {
		Effect e;
		if (buff != null) {
			e = new Effect(effect, source, initiative, duration, buff.id);
		} else {
			e = new Effect(effect, source, initiative, duration);
		}
		addEntry(e);
		this.fireTableRowsInserted(list.size()-1, list.size()-1);
	}

	private void addEntry(Effect e) {
		list.add(e);
	}

	int getBuffID(int row) {
		Effect e = list.get(row);
		return e.buff_id;
	}

//	private Effect getEntry(int index) {
//		return list.get(index);
//	}

	void expireEffect(int index, Component parent, InitiativeListModel potentials) {
		removeEffect(index, "Confirm buff expiry", getValueAt(index, EffectTableModel.EFFECT_COLUMN)
				+ " has expired. Remove", parent, potentials);
	}

	void removeEffect(int index, Component parent, InitiativeListModel potentials) {
		removeEffect(index, "Confirm delete", "Remove " + getValueAt(index, EffectTableModel.EFFECT_COLUMN), parent, potentials);
	}

	private void removeEffect(int index, String title, String text, Component parent, InitiativeListModel potentials) {
		int buffId = getBuffID(index);
		if (buffId > 0) {
			// there is a buff attached to the effect so confirm it should be removed
			List<Character> targets = new ArrayList<Character>();
			Buff buff = null;
			for (int i = 0; i < potentials.getSize(); i++) {
				Object t = potentials.getElementAt(i);
				if (t instanceof CharacterCombatEntry) {
					Character c = ((CharacterCombatEntry) t).getCharacter();
					ListModel list = c.getBuffListModel();
					for (int j = 0; j < list.getSize(); j++) {
						Buff b = (Buff) list.getElementAt(j);
						if (b.id == buffId) {
							buff = b;
							targets.add(c);
						}
					}
				}
			}
			String targetList;
			if (targets.size() == 0) {
				// no targets, just remove it
				removeEntry(index);
			} else {
				if (targets.size() == 1) {
					targetList = targets.get(0).getName();
				} else {
					StringBuilder s = new StringBuilder();
					for (Character c : targets) {
						if (s.length() > 0) s.append(",\n");
						s.append(c.getName());
					}
					targetList = "\n" + s;
				}
				int option = JOptionPane.showConfirmDialog(parent, text + " from " + targetList + "?", title, JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					for (Character c : targets) {
						// remove by id because if the buff was loaded from xml then the particular
						// character's copy of the buff might not be the same object as 'buff', though
						// it will have the same id.
						c.removeBuff(buff.id);
					}
					removeEntry(index);
				}
			}

		} else {
			// no buff, just remove it
			removeEntry(index);
		}
	}

	void clear() {
		int size = list.size();
		list.clear();
		fireTableRowsDeleted(0, size-1);
	}

	void parseDOM(Element el) {
		if (!el.getNodeName().equals("EffectList")) return;
		list.clear();
		int oldSize = list.size();

		NodeList nodes = el.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element)nodes.item(i);
			String tag = e.getTagName();
			if (tag.equals("EffectEntry")) {
				Effect c;
				if (e.hasAttribute("buff_id")) {
					c = new Effect(e.getAttribute("effect"),
							e.getAttribute("source"),
							Integer.parseInt(e.getAttribute("initiative")),
							Integer.parseInt(e.getAttribute("duration")),
							Integer.parseInt(e.getAttribute("buff_id"))
							);
				} else {
					c = new Effect(e.getAttribute("effect"),
							e.getAttribute("source"),
							Integer.parseInt(e.getAttribute("initiative")),
							Integer.parseInt(e.getAttribute("duration"))
							);
				}
				addEntry(c);
			}
		}
		if (list.size() != oldSize) {
			fireTableRowsInserted(oldSize,list.size()-1);
		}
	}

	Element getElement(Document doc) {
		Element el = doc.createElement("EffectList");
		for (Effect e : list) {
			el.appendChild(e.getElement(doc));
		}
		return el;
	}
}

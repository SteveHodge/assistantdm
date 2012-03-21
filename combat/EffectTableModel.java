package combat;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO check args, particular index/row args
@SuppressWarnings("serial")
public class EffectTableModel extends AbstractTableModel {
	public final static int EFFECT_COLUMN = 0;
	public final static int SOURCE_COLUMN = 1;
	public final static int INITIATIVE_COLUMN = 2;
	public final static int DURATION_COLUMN = 3;

	public class Effect {
		String effect;
		String source;
		int initiative;
		int duration;

		public Effect (String effect, String source, int initiative, int duration) {
			this.effect = effect;
			this.source = source;
			this.initiative = initiative;
			this.duration = duration;
		}

		protected String getDurationString() {
			if (duration >= 900) {
				return ""+(duration / 600)+" Hours";
			} else if (duration >= 20) {
				return ""+(duration / 10)+" Minutes";
			}
			return ""+duration+" Rounds";
		}

		public String getXML(String indent, String nextIndent) {
			StringBuilder b = new StringBuilder();
			b.append(indent).append("<EffectEntry effect=\"").append(effect);
			b.append("\" source=\"").append(source);
			b.append("\" initiative=\"").append(initiative);
			b.append("\" duration=\"").append(duration);
			b.append("\"/>").append(System.getProperty("line.separator"));
			return b.toString();
		}
	}

	List<Effect> list = new ArrayList<Effect>();

	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
		return list.size();
	}

	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0 || columnIndex == 1) {
			return String.class;
		} else if (columnIndex == 2 || columnIndex == 3) {
			return Integer.class;
		}
		return super.getColumnClass(columnIndex);
	}

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

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		System.out.println("setValueAt: row = "+rowIndex+", col = "+columnIndex+", to "+aValue);
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

	public int addDuration(int row, int delta) {
		Effect e = list.get(row);
		e.duration += delta;
		fireTableCellUpdated(row, DURATION_COLUMN);
		return e.duration;
	}

	public void removeEntry(int index) {
		list.remove(index);
		fireTableRowsDeleted(index, index);
	}

	public void addEntry(String effect, String source, int initiative, int duration) {
		Effect e = new Effect(effect, source, initiative, duration);
		addEntry(e);
		this.fireTableRowsInserted(list.size()-1, list.size()-1);
	}

	public void addEntry(Effect e) {
		list.add(e);
	}

	public Effect getEntry(int index) {
		return list.get(index);
	}

	public void clear() {
		int size = list.size();
		list.clear();
		fireTableRowsDeleted(0, size-1);
	}

	public void parseDOM(Element el) {
		if (!el.getNodeName().equals("EffectList")) return;
		list.clear();
		int oldSize = list.size();

		NodeList nodes = el.getChildNodes();
		if (nodes != null) {
			for (int i=0; i<nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
				Element e = (Element)nodes.item(i);
				String tag = e.getTagName();
				if (tag.equals("EffectEntry")) {
					Effect c = new Effect(e.getAttribute("effect"),
							e.getAttribute("source"),
							Integer.parseInt(e.getAttribute("initiative")),
							Integer.parseInt(e.getAttribute("duration"))
							);
					addEntry(c);
				}
			}
		}
		if (list.size() != oldSize) {
			fireTableRowsInserted(oldSize,list.size()-1);
		}
	}

	public String getXML(String indent, String nextIndent) {
		StringBuilder b = new StringBuilder();
		String nl = System.getProperty("line.separator");
		b.append(indent).append("<EffectList>").append(nl);
		for(Effect e : list) {
			b.append(e.getXML(indent+nextIndent,nextIndent));
		}
		b.append(indent).append("</EffectList>").append(nl);
		return b.toString();
	}
}

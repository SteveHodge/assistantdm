package monsters;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import xml.XMLUtils;

@SuppressWarnings("serial")
public class MonstersTableModel extends DefaultTableModel {
	private List<MonsterEntry> monsters;

	public enum Column {
		NAME("Name"),
		SIZE("Size"),
		TYPE("Type"),
		ENVIRONMENT("Environment"),
		CR("CR"),
		SOURCE("Source");

		@Override
		public String toString() {
			return name;
		}

		private Column(String n) {
			name = n;
		}

		private String name;
	}

	MonstersTableModel() {
		monsters = new ArrayList<MonsterEntry>();
	}

	MonsterEntry getMonsterEntry(int index) {
		return monsters.get(index);
	}

	@Override
	public int getColumnCount() {
		return Column.values().length;
	}

	@Override
	public String getColumnName(int column) {
		if (column >= 0 && column < getColumnCount()) {
			return Column.values()[column].toString();
		}
		return super.getColumnName(column);
	}

	@Override
	public int getRowCount() {
		if (monsters != null) return monsters.size();
		return 0;
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column >= 0 && column < getColumnCount()) {
			Column col = Column.values()[column];
			MonsterEntry e = monsters.get(row);
			switch (col) {
			case NAME:
				return e.name;
			case SIZE:
				return e.size;
			case TYPE:
				return e.type;
			case ENVIRONMENT:
				return e.environment;
			case CR:
				return e.cr;
			case SOURCE:
				return e.source;
			}
		}

		return super.getValueAt(row, column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	void parseXML(File xmlFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			InputStream is = getClass().getClassLoader().getResourceAsStream("monsters.xsd");
			factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
			Document dom = factory.newDocumentBuilder().parse(xmlFile);
			//printNode(dom,"");

			Element node = XMLUtils.findNode(dom,"MonsterList");
			if (node != null) {
				String src = node.getAttribute("source");
				NodeList children = node.getChildNodes();
				for (int i=0; i<children.getLength(); i++) {
					if (children.item(i).getNodeName().equals("Monster")) {
						MonsterEntry me = parseDOM((Element)children.item(i));
						if (me != null) {
							me.source = src;
							monsters.add(me);
						}
					}
				}
			}

			Collections.sort(monsters,new Comparator<MonsterEntry>() {
				@Override
				public int compare(MonsterEntry arg0, MonsterEntry arg1) {
					return arg0.name.compareTo(arg1.name);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static MonsterEntry parseDOM(Element node) {
		if (!node.getNodeName().equals("Monster")) return null;
		MonsterEntry me = new MonsterEntry();
		me.name = node.getAttribute("name");
		me.url = node.getAttribute("url");
		me.size = node.getAttribute("size");
		me.type = node.getAttribute("type");
		me.environment = node.getAttribute("environment");
		me.cr = node.getAttribute("cr");
		return me;
	}
}

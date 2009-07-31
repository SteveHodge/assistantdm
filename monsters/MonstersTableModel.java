package monsters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xml.XMLUtils;

@SuppressWarnings("serial")
public class MonstersTableModel extends DefaultTableModel {
	protected List<MonsterEntry> monsters;

	public final static int COLUMN_NAME = 0;
	public final static int COLUMN_SIZE = 1;
	public final static int COLUMN_TYPE = 2;
	public final static int COLUMN_ENVIRONMENT = 3;
	public final static int COLUMN_CR = 4;
	public final static int COLUMN_SOURCE = 5;
	public final static int TOTAL_COLUMNS = 6;

	public MonstersTableModel() {
		monsters = new ArrayList<MonsterEntry>();
	}

	public MonsterEntry getMonsterEntry(int index) {
		return monsters.get(index);
	}

	public int getColumnCount() {
		return TOTAL_COLUMNS;
	}

	public String getColumnName(int column) {
		if (column == COLUMN_NAME) return "Name";
		if (column == COLUMN_SIZE) return "Size";
		if (column == COLUMN_TYPE) return "Type";
		if (column == COLUMN_ENVIRONMENT) return "Environment";
		if (column == COLUMN_CR) return "CR";
		if (column == COLUMN_SOURCE) return "Source";
		return super.getColumnName(column);
	}

	public int getRowCount() {
		if (monsters != null) return monsters.size();
		return 0;
	}

	public Object getValueAt(int row, int column) {
		MonsterEntry e = monsters.get(row);
		if (column == COLUMN_NAME) return e.name;
		if (column == COLUMN_SIZE) return e.size;
		if (column == COLUMN_TYPE) return e.type;
		if (column == COLUMN_ENVIRONMENT) return e.environment;
		if (column == COLUMN_CR) return e.cr;
		if (column == COLUMN_SOURCE) return e.source;
		return super.getValueAt(row, column);
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public void parseXML(File xmlFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", new File("monsters.xsd"));

			Document dom = factory.newDocumentBuilder().parse(xmlFile);
			//printNode(dom,"");

			Node node = XMLUtils.findNode(dom,"MonsterList");
			if (node != null) {
				String src = XMLUtils.getAttribute(node, "source");
				NodeList children = node.getChildNodes();
				if (children != null) {
					for (int i=0; i<children.getLength(); i++) {
						if (children.item(i).getNodeName().equals("Monster")) {
							MonsterEntry me = parseDOM(children.item(i));
							if (me != null) {
								me.source = src;
								monsters.add(me);
							}
						}
					}
				}
			}

			Collections.sort(monsters,new Comparator<MonsterEntry>() {
				public int compare(MonsterEntry arg0, MonsterEntry arg1) {
					return arg0.name.compareTo(arg1.name);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static MonsterEntry parseDOM(Node node) {
		if (!node.getNodeName().equals("Monster")) return null;
		MonsterEntry me = new MonsterEntry();
		me.name = XMLUtils.getAttribute(node, "name");
		me.url = XMLUtils.getAttribute(node, "url");
		me.size = XMLUtils.getAttribute(node, "size");
		me.type = XMLUtils.getAttribute(node, "type");
		me.environment = XMLUtils.getAttribute(node, "environment");
		me.cr = XMLUtils.getAttribute(node, "cr");
		return me;
	}
}

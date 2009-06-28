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
public class MonstersTableModel extends DefaultTableModel implements FilterableTableModel<MonsterEntry> {
	protected List<MonsterEntry> monsters;

	public MonstersTableModel() {
		monsters = new ArrayList<MonsterEntry>();
	}

	public MonsterEntry getRowObject(int index) {
		return monsters.get(index);
	}

	public int getColumnCount() {
		return 5;
	}

	public String getColumnName(int column) {
		if (column == 0) return "Name";
		if (column == 1) return "Size";
		if (column == 2) return "Type";
		if (column == 3) return "Environment";
		if (column == 4) return "CR";
		return super.getColumnName(column);
	}

	public int getRowCount() {
		if (monsters != null) return monsters.size();
		return 0;
	}

	public Object getValueAt(int row, int column) {
		MonsterEntry e = monsters.get(row);
		if (column == 0) return e.name;
		if (column == 1) return e.size;
		if (column == 2) return e.type;
		if (column == 3) return e.environment;
		if (column == 4) return e.cr;
		return super.getValueAt(row, column);
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public static MonstersTableModel parseXML(File xmlFile) {
		MonstersTableModel m = new MonstersTableModel();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

			Document dom = factory.newDocumentBuilder().parse(xmlFile);
			//printNode(dom,"");

			Node node = XMLUtils.findNode(dom,"MonsterList");
			if (node != null) {
				NodeList children = node.getChildNodes();
				if (children != null) {
					for (int i=0; i<children.getLength(); i++) {
						if (children.item(i).getNodeName().equals("Monster")) {
							MonsterEntry me = parseDOM(children.item(i));
							if (me != null) m.monsters.add(me);
						}
					}
				}
			}

			Collections.sort(m.monsters,new Comparator<MonsterEntry>() {
				public int compare(MonsterEntry arg0, MonsterEntry arg1) {
					return arg0.name.compareTo(arg1.name);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m;
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

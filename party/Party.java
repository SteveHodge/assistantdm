package party;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xml.XML;
import xml.XMLUtils;

public class Party implements Iterable<Character>, XML {
	List<Character> characters;

	public Party() {
		characters = new ArrayList<Character>();
	}

	public void add(Character c) {
		characters.add(c);
	}

	public Character get(int i) {
		return characters.get(i);
	}

	public int size() {
		return characters.size();
	}

	public Iterator<Character> iterator() {
		return characters.iterator();
	}

	public static Party parseXML(File xmlFile) {
		Party p = new Party();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", new File("party.xsd"));

			Document dom = factory.newDocumentBuilder().parse(xmlFile);
			//printNode(dom,"");

			Node node = XMLUtils.findNode(dom,"Party");
			if (node != null) {
				NodeList children = node.getChildNodes();
				if (children != null) {
					for (int i=0; i<children.getLength(); i++) {
						if (children.item(i).getNodeName().equals("Character")) {
							Character c = Character.parseDOM(children.item(i));
							if (c != null) p.add(c);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}

	public String getXML() {
		return getXML("", "    ");
	}

	public String getXML(String indent, String nextIndent) {
		StringBuilder b = new StringBuilder();
		String nl = System.getProperty("line.separator");
		b.append(indent);
		b.append("<Party xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"party.xsd\">");
		b.append(nl);
		for (Character c : characters) {
			b.append(c.getXML(indent+nextIndent,nextIndent));
		}
		b.append(indent).append("</Party>").append(nl);
		return b.toString();
	}
}

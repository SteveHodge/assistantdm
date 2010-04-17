package party;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xml.XML;
import xml.XMLUtils;

public class Party implements Iterable<Character>, XML {
	protected List<Character> characters;
	protected List<PartyListener> listeners = new ArrayList<PartyListener>();

	public Party() {
		characters = new ArrayList<Character>();
	}

	public void addPartyListener(PartyListener l) {
		listeners.add(l);
	}

	public void removePartyListener(PartyListener l) {
		listeners.remove(l);
	}

	public void add(Character c) {
		if (characters.contains(c)) return;
		characters.add(c);
		for (PartyListener l : listeners) l.characterAdded(c);
	}

	public boolean remove(Character c) {
		if (characters.remove(c)) {
			for (PartyListener l : listeners) l.characterRemoved(c);
			return true;
		}
		return false;
	}

	public boolean contains(Character c) {
		return characters.contains(c);
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

	// TODO move to CharacterLibrary (except Party block)?
	// If the file has a <Party> block then the characters are all added to the CharacterLibrary and the
	// new Party is set up to contain only those characters specified in the <Party> block.
	// If the file does not contain a <Party> block then all characters in the file are added to the Party,
	// but they are not added to CharacterLibrary (this case is used for updates)
	public static Party parseXML(File xmlFile) {
		Party p = new Party();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", new File("party.xsd"));

			Document dom = factory.newDocumentBuilder().parse(xmlFile);
			//XMLUtils.printNode(dom, "");

			Map<String,Character> charMap = new HashMap<String,Character>();
			Node node = XMLUtils.findNode(dom,"Characters");
			if (node != null) {
				NodeList children = node.getChildNodes();
				if (children != null) {
					for (int i=0; i<children.getLength(); i++) {
						if (children.item(i).getNodeName().equals("Character")) {
							Character c = Character.parseDOM((Element)children.item(i));
							if (c != null) {
								charMap.put(c.getName(),c);
							}
						}
					}
				}
			}

			node = XMLUtils.findNode(node, "Party");
			if (node != null) {
				NodeList children = node.getChildNodes();
				if (children != null) {
					for (int i=0; i<children.getLength(); i++) {
						if (children.item(i).getNodeName().equals("Member")) {
							Element m = (Element)children.item(i);
							Character c = charMap.get(m.getAttribute("name"));
							if (c != null) {
								p.add(c);
							}
						}
					}
				}
				// Add characters to CharacterLibrary:
				for (Character c: charMap.values()) {
					CharacterLibrary.add(c);
				}

			} else {
				System.out.println("Party not found");
				// Add all characters to the party but not the library:
				for (Character c: charMap.values()) {
					p.add(c);
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

	// TODO move to CharacterLibrary (except Party block)?
	public String getXML(String indent, String nextIndent) {
		StringBuilder b = new StringBuilder();
		String nl = System.getProperty("line.separator");
		b.append(indent);
		b.append("<Characters xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"party.xsd\">");
		b.append(nl);
		for (Character c : CharacterLibrary.characters) {
			b.append(c.getXML(indent+nextIndent,nextIndent));
		}
		b.append(indent+nextIndent+"<Party>"+nl);
		for (Character c : characters) {
			b.append(indent+nextIndent+nextIndent+"<Member name=\""+c.getName()+"\"/>"+nl);
		}
		b.append(indent+nextIndent+"</Party>"+nl);
		b.append(indent).append("</Characters>").append(nl);
		return b.toString();
	}
}

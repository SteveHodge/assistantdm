package party;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.Module;
import util.ModuleRegistry;
import util.XMLUtils;

public class Party implements Iterable<Character>, Module {
	protected List<Character> characters;
	protected List<PartyListener> listeners = new ArrayList<>();

	public Party() {
		ModuleRegistry.register(Party.class, this);
		characters = new ArrayList<>();
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

	public Character get(String name) {
		for (Character c : characters) {
			if (c.getName().equals(name)) return c;
		}
		return null;
	}

	public int size() {
		return characters.size();
	}

	@Override
	public Iterator<Character> iterator() {
		return characters.iterator();
	}

	public static Document parseXML(File xmlFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			InputStream is = (new Party()).getClass().getClassLoader().getResourceAsStream("party.xsd");
			factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
			Document dom = factory.newDocumentBuilder().parse(xmlFile);
			//XMLUtils.printNode(dom, "");
			return dom;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Party parseDOM(Document dom) {
		return parseDOM(dom, false);
	}

	/**
	 * Reads the supplied Document are builds a party of characters from it. All
	 * characters in the file are added to the <code>CharacterLibrary</code> unless
	 * <code>update</code> is true. If the file has a &lt;Party&gt; block then the
	 * new <code>Party</code> is set up to conatin just those characters, otherwise
	 * it is set up with all the characters from the file.
	 *
	 * @param xmlFile
	 *            the File to parse
	 * @param update
	 *            if true then the incomming characters are not added to the CharacterLibrary
	 * @return the new Party
	 */
	public static Party parseDOM(Document dom, boolean update) {
		Party p = new Party();

		Map<String, Character> charMap = new HashMap<>();
		Node node = XMLUtils.findNode(dom,"Characters");
		if (node != null) {
			NodeList children = node.getChildNodes();
			for (int i=0; i<children.getLength(); i++) {
				if (children.item(i).getNodeName().equals("Character")) {
					Character c = Character.parseDOM((Element)children.item(i));
					if (c != null) {
						if (!update) CharacterLibrary.add(c);
						charMap.put(c.getName(),c);
					}
				}
			}
		}

		node = XMLUtils.findNode(node, "Party");
		if (node != null) {
			NodeList children = node.getChildNodes();
			for (int i=0; i<children.getLength(); i++) {
				if (children.item(i).getNodeName().equals("Member")) {
					Element m = (Element)children.item(i);
					Character c = charMap.get(m.getAttribute("name"));
					if (c != null) {
						if ("true".equals(m.getAttribute("autosave")) || "1".equals(m.getAttribute("autosave"))) {
							c.setAutoSave(true);
						}
						p.add(c);
					}
				}
			}

		} else {
			System.out.println("Party not found");
			for (Character c: charMap.values()) {
				p.add(c);
			}
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
		//b.append("<Characters xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"party.xsd\">");
		b.append("<Characters>");
		b.append(nl);

		Character[] allCharacters = new Character[CharacterLibrary.characters.size()];
		allCharacters = CharacterLibrary.characters.toArray(allCharacters);
		Arrays.sort(allCharacters, (a, bb) -> a.getName().compareTo(bb.getName()));
//		for (Character c : allCharacters) {
//			b.append(c.getXML(indent+nextIndent,nextIndent));
//		}
		b.append(indent+nextIndent+"<Party>"+nl);
		for (Character c : characters) {
			b.append(indent + nextIndent + nextIndent + "<Member name=\"" + c.getName() + "\"");
			if (c.isAutoSaving()) b.append(" autosave=\"true\"");
			b.append("/>" + nl);
		}
		b.append(indent+nextIndent+"</Party>"+nl);
		b.append(indent).append("</Characters>").append(nl);
		return b.toString();
	}

	public Element getElement(Document doc) {
		Element e = doc.createElement("Characters");
		Character[] allCharacters = new Character[CharacterLibrary.characters.size()];
		allCharacters = CharacterLibrary.characters.toArray(allCharacters);
		Arrays.sort(allCharacters, (a, b) -> a.getName().compareTo(b.getName()));
		for (Character c : allCharacters) {
			XMLOutputCharacterProcessor processor = new XMLOutputCharacterProcessor(doc);
			c.executeProcess(processor);
			e.appendChild(processor.getElement());
		}
		Element p = doc.createElement("Party");
		for (Character c : characters) {
			Element m = doc.createElement("Member");
			m.setAttribute("name", c.getName());
			if (c.isAutoSaving()) m.setAttribute("autosave", "true");
			p.appendChild(m);
		}
		e.appendChild(p);
		return e;
	}

	@Override
	public void moduleExit() {
	}
}

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

// XXX not sure if Party or CharacterCollection or both should be the module

public class Party implements Iterable<Character>, Module {
	protected List<Character> characters;
	protected List<PartyListener> listeners = new ArrayList<>();
	protected Map<PartyXMLPlugin, String> plugins = new HashMap<>();

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

	public void addXMLPlugin(PartyXMLPlugin plugin, String tagName) {
		plugins.put(plugin, tagName);
	}

	public void removeXMLPlugin(PartyXMLPlugin plugin) {
		plugins.remove(plugin);
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

	/**
	 * The <code>Party</code> is emptied and then characters from the supplied Document
	 * are added to it. All characters in the file are added to the <code>CharacterLibrary</code>.
	 * If the file has a &lt;Party&gt; block then the <code>Party</code> is set up to contain
	 * just those characters, otherwise it is set up with all the characters from the file.
	 * Any registered XML plug-ins are called to handle the elements they are registerd for.
	 *
	 * @param xmlFile
	 *            the File to parse
	 */
	public void parseDOM(Document dom) {
		Map<String, Character> charMap = new HashMap<>();
		boolean hasParty = false;
		Map<String, PartyXMLPlugin> pluginMap = new HashMap<>();

		// build a map of tags to the plugins that handle them. could maintain this all the time but parsing is rare so just build it when we need it
		for (PartyXMLPlugin p : plugins.keySet()) {
			pluginMap.put(plugins.get(p), p);
		}

		while (characters.size() > 0) {
			Character c = characters.get(0);
			remove(c);
		}

		Node root = XMLUtils.findNode(dom, "Characters");	// root node
		if (root != null) {
			NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
				Element child = (Element) children.item(i);
				String name = child.getNodeName();
				if (name.equals("Character")) {
					Character c = Character.parseDOM(child);
					if (c != null) {
						CharacterLibrary.add(c);
						charMap.put(c.getName(),c);
					}
				} else if (name.equals("Party")) {
					hasParty = true;

					NodeList partyChildren = child.getChildNodes();
					for (int j = 0; j < partyChildren.getLength(); j++) {
						if (partyChildren.item(j).getNodeName().equals("Member")) {
							Element m = (Element) partyChildren.item(j);
							Character c = charMap.get(m.getAttribute("name"));
							if (c != null) {
								if ("true".equals(m.getAttribute("autosave")) || "1".equals(m.getAttribute("autosave"))) {
									c.setAutoSave(true);
								}
								add(c);
							}
						}
					}

				} else if (pluginMap.containsKey(name)) {
					// we have a plugin to manage this element
					PartyXMLPlugin plugin = pluginMap.get(name);
					plugin.parseElement(child);

				} else {
					System.err.println("Unknown element found while parsing Party: " + name);
				}
			}
		}

		if (!hasParty) {
			System.out.println("Party not found");
			for (Character c: charMap.values()) {
				add(c);
			}
		}
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

		for (PartyXMLPlugin plugin : plugins.keySet()) {
			Element el = plugin.getElement(doc);
			if (el != null) e.appendChild(el);
		}

		return e;
	}

	@Override
	public void moduleExit() {
	}
}

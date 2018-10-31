package monsters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import monsters.StatisticsBlock.Field;
import util.LocalEntityResolver;
import util.XMLUtils;

public class MonsterLibrary {
	final public static MonsterLibrary instance = new MonsterLibrary();
	public List<MonsterEntry> monsters = new ArrayList<>();
	private List<ListDataListener> listeners = new ArrayList<>();
	private Document document;
	private Map<String, Element> customElements = new HashMap<>();
	private File libraryFile = new File("CustomMonsters.xml");

	private MonsterLibrary() {
	}

	public void addListener(ListDataListener l) {
		listeners.add(l);
	}

	public void removeListener(ListDataListener l) {
		listeners.remove(l);
	}

	private void fireContentsChanged() {
		ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, monsters.size());
		for (ListDataListener l : listeners) {
			l.contentsChanged(e);
		}
	}

	public void loadLibraryFile() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			int count = 0;
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new LocalEntityResolver());
			document = builder.parse(libraryFile);

			NodeList monsterEls = document.getElementsByTagName("monster");
			for (int i = 0; i < monsterEls.getLength(); i++) {
				Element el = (Element) monsterEls.item(i);
				List<StatisticsBlock> statsBlocks = StatisticsBlock.parseMonsterElement(el);

				for (StatisticsBlock m : statsBlocks) {
					MonsterEntry me = new MonsterEntry();
					me.name = m.getName();
//					me.url = node.getAttribute("url");
					me.size = m.getSize().toString();
					me.type = m.getType().toString();
					me.environment = m.get(Field.ENVIRONMENT);
					me.cr = m.get(Field.CR);
					me.source = "Custom";
					monsters.add(me);
					customElements.put(m.getName(), el);
					count++;
//					System.out.println("Loaded custom monster " + me.name);
				}
			}

			System.out.println("Loaded " + count + " monsters from " + libraryFile);
			Collections.sort(monsters, (a, b) -> a.name.compareTo(b.name));	// TODO monster library shouldn't be sorted
			fireContentsChanged();

		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void parseXML(File xmlFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			InputStream is = instance.getClass().getClassLoader().getResourceAsStream("monsters.xsd");
			factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
			Document dom = factory.newDocumentBuilder().parse(xmlFile);
			//printNode(dom,"");

			Element node = XMLUtils.findNode(dom, "MonsterList");
			if (node != null) {
				String src = node.getAttribute("source");
				NodeList children = node.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					if (children.item(i).getNodeName().equals("Monster")) {
						MonsterEntry me = parseDOM((Element) children.item(i));
						if (me != null) {
							me.source = src;
							monsters.add(me);
						}
					}
				}
			}

			Collections.sort(monsters, (a, b) -> a.name.compareTo(b.name));	// TODO monster library shouldn't be sorted
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Element getMonsterElement(String name) {
		return customElements.get(name);
	}

	public void addMonster(Monster m) {
		StatsBlockCreatureView view = StatsBlockCreatureView.getView(m);
		try {
			if (document == null) {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				document.appendChild(document.createElement("monsters"));
			}
			Element e = view.getXMLElement(document);
			document.getDocumentElement().appendChild(e);
			customElements.put(m.getName(), e);
			XMLUtils.writeDOMWithBackup(document, libraryFile);
		} catch (ParserConfigurationException x) {
			x.printStackTrace();
		}

		MonsterEntry me = new MonsterEntry();
		me.name = m.getName();
//		me.url = node.getAttribute("url");
		me.size = m.getSizeStatistic().getSize().toString();
		me.type = m.race.toString();
		me.environment = m.getPropertyValue("field." + Field.ENVIRONMENT.name()).toString();
		me.cr = m.getPropertyValue("field." + Field.CR.name()).toString();
		me.source = "Custom";
		monsters.add(me);
		Collections.sort(monsters, (a, b) -> a.name.compareTo(b.name));	// TODO monster library shouldn't be sorted
		fireContentsChanged();
	}

	private MonsterEntry parseDOM(Element node) {
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

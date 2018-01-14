package party;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface PartyXMLPlugin {
	public Element getElement(Document doc);

	public void parseElement(Element element);
}

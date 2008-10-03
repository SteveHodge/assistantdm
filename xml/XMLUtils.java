package xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final public class XMLUtils {
	private XMLUtils() {};

	public static Node findNode(Node parent, String name) {
		NodeList nodes = parent.getChildNodes();
		if (nodes != null) {
			for (int i=0; i<nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals(name)) {
					return node;
				}
			}
		}
		return null;
	}

	public static void printNode(Node node, String indent)  {
		switch (node.getNodeType()) {
			case Node.DOCUMENT_NODE:
				System.out.println("<xml version=\"1.0\">\n");
				// recurse on each child
				NodeList nodes = node.getChildNodes();
				if (nodes != null) {
					for (int i=0; i<nodes.getLength(); i++) {
						printNode(nodes.item(i), "");
					}
				}
				break;
				
			case Node.ELEMENT_NODE:
				String name = node.getNodeName();
				System.out.print(indent + "<" + name);
				NamedNodeMap attributes = node.getAttributes();
				for (int i=0; i<attributes.getLength(); i++) {
					Node current = attributes.item(i);
					System.out.print( " " + current.getNodeName() + "=\"" + current.getNodeValue() + "\"");
				}
				System.out.print(">");
				
				// recurse on each child
				NodeList children = node.getChildNodes();
				if (children != null) {
					for (int i=0; i<children.getLength(); i++) {
						printNode(children.item(i), indent + "  ");
					}
				}
				
				System.out.print("</" + name + ">");
				break;

			case Node.TEXT_NODE:
				System.out.print(node.getNodeValue());
				break;
		}
	}

	public static String getAttribute(Node node, String name) {
		return getAttribute(node.getAttributes(), name, null);
	}

	public static String getAttribute(NamedNodeMap attributes, String name) {
		return getAttribute(attributes, name, null);
	}

	// elementName is used only in exception messages - it is safe to pass empty string or null
	public static String getAttribute( NamedNodeMap attributes, String name, String elementName ) {
		Node attribute = attributes.getNamedItem(name);
		if( attribute == null ) System.err.println(name+" attribute is required in element "+elementName);
		return attribute.getNodeValue();
	}
}

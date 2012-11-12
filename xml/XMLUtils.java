package xml;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final public class XMLUtils {
	private XMLUtils() {};

	public static Element findNode(Node parent, String name) {
		NodeList nodes = parent.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(name)) {
				return (Element)node;
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
				for (int i=0; i<nodes.getLength(); i++) {
					printNode(nodes.item(i), "");
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
				for (int i=0; i<children.getLength(); i++) {
					printNode(children.item(i), indent + "  ");
				}
				
				System.out.print("</" + name + ">");
				break;

			case Node.TEXT_NODE:
				System.out.print(node.getNodeValue());
				break;
		}
	}
}

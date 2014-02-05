package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final public class XMLUtils {
	private XMLUtils() {};

	public static void writeDOM(Document doc, File f) {
		OutputStreamWriter writer = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			writer = new OutputStreamWriter(fos, "UTF-8");
			writeDOM(doc, writer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static void writeDOM(Document doc, Writer outputStream) {
		try {
			doc.setXmlStandalone(true);
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			trans.transform(new DOMSource(doc), new StreamResult(outputStream));

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

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

	public static void printNode(Node node, String indent) {
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

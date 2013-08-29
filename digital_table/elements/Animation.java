package digital_table.elements;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
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

// TODO this class is used to send the contents of the animation xml file to the remote display. but maybe it would be better just to send the filename. that would allow different effect configurations 

public class Animation implements Serializable {
	private static final long serialVersionUID = 1L;

	private Element animationNode = null;
	private transient AnimationFrame[] frames = null;

	public Animation(File f) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		InputStream is = getClass().getClassLoader().getResourceAsStream("animation.xsd");
		try {
			factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
			Document dom = factory.newDocumentBuilder().parse(f);

			NodeList nodes = dom.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals("Animation")) {
					animationNode = (Element) node;
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	// TODO move this to PNGSequenceIM. parsing could be done here so that a list of frame filenames is available
	AnimationFrame[] getFrames(ImageManager mgr, File defaultDir) {
		if (frames != null) return frames;

		if (animationNode == null) return null;

		int framerate = 20;
		if (animationNode.hasAttribute("framerate")) {
			framerate = Integer.parseInt(animationNode.getAttribute("framerate"));
		}
		int delay = 1000 / framerate;
		List<AnimationFrame> frameList = new ArrayList<AnimationFrame>();

		NodeList nodes = animationNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("Frame")) {
				Element frameEl = (Element) nodes.item(i);
				try {
					// TODO more sophisticated filename parsing - detect if it has path or not
					File imgFile = new File(defaultDir, frameEl.getAttribute("filename"));
					System.out.println("Adding frame " + imgFile);
					BufferedImage image = ImageIO.read(imgFile);
					image = mgr.resizeImage(image);
					AnimationFrame frame = new AnimationFrame(image, delay);
					frameList.add(frame);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		frames = frameList.toArray(new AnimationFrame[frameList.size()]);
		return frames;
	}
}
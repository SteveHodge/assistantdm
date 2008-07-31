import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xml.XMLUtils;

import magicgenerator.Item;

public class ShoppingPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	List<Shop> shops = new ArrayList<Shop>();
	JLabel dayLabel;
	int day = 0;
	JTabbedPane tabbedPane;

	@SuppressWarnings("unchecked")
	public ShoppingPanel(ObjectInputStream in) throws IOException, ClassNotFoundException {
		this((List<Shop>)in.readObject());
	}

	public ShoppingPanel(List<Shop> ss) {
		initializeUI();
		shops = ss;
		for (Shop s : shops) {
			if (s.day > day) day = s.day;
			String tabName = "shop";
			String tabTip = "";
			if (s.procName.equals("Myraeth's Shop")) {
				tabName = "Myraeth's";
				tabTip = "Myraeth's Oddities";
			} else if (s.procName.equals("Rastor's Shop")) {
				tabName = "Rastor's";
				tabTip = "Rastor's Weapons";
			} else if (s.procName.equals("Bull and Bear Armory")) {
				tabName = "Bull and Bear";
				tabTip = "Bull and Bear Armory";
			}
			JPanel panel = new ShopPanel(s);
			tabbedPane.addTab(tabName, null, panel, tabTip);
		}
		// TODO could bring any lagging shops up to maxDay
		dayLabel.setText("Day = "+day);
	}

	public ShoppingPanel() {
		initializeUI();

		JPanel panel;
		Shop myraeth = new Shop("myraeth.txt","Myraeth's Shop",5,3,10,7,20,10);
		myraeth.createInitialInventory();
		shops.add(myraeth);
		panel = new ShopPanel(myraeth);
		tabbedPane.addTab("Myraeth's", null, panel, "Myraeth's Oddities");

		Shop rastor = new Shop("rastor.txt","Rastor's Shop",0,0,5,1,10,5);
		rastor.createInitialInventory();
		rastor.setSellChance(Item.CLASS_MEDIUM, 33);
		shops.add(rastor);
		panel = new ShopPanel(rastor);
		tabbedPane.addTab("Rastor's", null, panel, "Rastor's Weapons");

		Shop bullandbear = new Shop("bullandbear.txt","Bull and Bear Armory",0,0,5,1,10,5);
		bullandbear.createInitialInventory();
		bullandbear.setSellChance(Item.CLASS_MEDIUM, 33);
		shops.add(bullandbear);
		panel = new ShopPanel(bullandbear);
		tabbedPane.addTab("Bull and Bear", null, panel, "Bull and Bear Armory");
	}

	private void initializeUI() {
		setLayout(new BorderLayout());

		JPanel header = new JPanel();
		dayLabel = new JLabel("Day = 0");
		header.add(dayLabel);
		JButton nextDayButton = new JButton("Next Day");
		nextDayButton.addActionListener(this);
		header.add(nextDayButton);
		add(header,BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();
	
		add(tabbedPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		day++;
		for (Shop s : shops) s.nextDay();
		dayLabel.setText("Day = "+day);
	}

	public void saveShops(ObjectOutputStream out) throws IOException {
		out.writeObject(shops);
	}

	static public void writeShopsXML(List<Shop> shops, String filename) {
        FileWriter outputStream = null;

		try {
			outputStream = new FileWriter(filename);
			outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			outputStream.write(System.getProperty("line.separator"));
			outputStream.write(System.getProperty("line.separator"));
			outputStream.write("<Shops xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"shops.xsd\">");
			outputStream.write(System.getProperty("line.separator"));
			for (Shop s : shops) {
				outputStream.write(s.getXML("    ", "    "));
			}
			outputStream.write("</Shops>");
			outputStream.write(System.getProperty("line.separator"));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static List<Shop> parseShopsXML(String filename) {
		List<Shop> shops = new ArrayList<Shop>();
		File xmlFile = new File(filename);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

			Document dom = factory.newDocumentBuilder().parse(xmlFile);

			Node node = XMLUtils.findNode(dom,"Shops");
			if (node != null) {
				NodeList children = node.getChildNodes();
				if (children != null) {
					for (int i=0; i<children.getLength(); i++) {
						if (children.item(i).getNodeName().equals("Shop")) {
							Shop s = Shop.parseDOM(children.item(i));
							if (s != null) shops.add(s);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return shops;
	}
}

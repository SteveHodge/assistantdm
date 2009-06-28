package magicitems;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilderFactory;

import magicgenerator.Item;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xml.XMLUtils;

public class ShoppingPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	List<Shop> shops = new ArrayList<Shop>();
	JLabel dayLabel;
	int day = 0;
	JTabbedPane tabbedPane;
	JButton nextDayButton;
	JButton listItemsButton;
	JCheckBox alertCheckBox;

	protected PrintStream log = null;

	@SuppressWarnings("unchecked")
	public ShoppingPanel(ObjectInputStream in) throws IOException, ClassNotFoundException {
		this((List<Shop>)in.readObject());
	}

	public ShoppingPanel(List<Shop> ss) {
		setupLog("shops.log");
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
		dayLabel.setText(PtolusCalendar.getShortDescription(day));
	}

	public ShoppingPanel() {
		setupLog("shops.log");
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

	protected void setupLog(String logname) {
		try {
			FileOutputStream os = new FileOutputStream(logname, true);
			log = new PrintStream(os);
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("Could not open log file for shops: "+e.getMessage());
		}
	}

	private void initializeUI() {
		setLayout(new BorderLayout());

		JPanel header = new JPanel();
		dayLabel = new JLabel("(no date)");
		header.add(dayLabel);
		nextDayButton = new JButton("Next Day");
		nextDayButton.addActionListener(this);
		header.add(nextDayButton);

		listItemsButton = new JButton("List All Items");
		listItemsButton.addActionListener(this);
		header.add(listItemsButton);

		alertCheckBox = new JCheckBox("Changes Alert");
		alertCheckBox.setSelected(true);
		header.add(alertCheckBox);
		add(header,BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();
	
		add(tabbedPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == nextDayButton) {
			day++;
			List<Item>changes = new ArrayList<Item>();
			for (Shop s : shops) {
				changes.addAll(s.nextDay());
			}
			dayLabel.setText(PtolusCalendar.getShortDescription(day));
			log.println("---- "+PtolusCalendar.getShortDescription(day)+" ----");
			String items = getItemListString(changes,new CostFirstItemFormatter() {
				public String toString(Item i) {
					if (i.getField("sold") != null) {
						return "Sold: "+super.toString(i);
					} else if (i.getField("added") != null) {
						return "Added: "+super.toString(i);
					}
					return super.toString(i);
				}
			});
			log.println(items);
			if (alertCheckBox.isSelected()) {
				popupMessage(items);
			}

		} else if (e.getSource() == listItemsButton) {
			List<Item>items = new ArrayList<Item>();
			for (Shop s : shops) {
				items.addAll(s.inventory);
			}
			String itemStr = getItemListString(items);
			log.println("---- All Current Items ----");
			log.println(itemStr);
			log.println("---------------------------");
			popupMessage(itemStr);
		}
	}

	// sorts by cost
	protected String getItemListString(List<Item>items) {
		return getItemListString(items,new CostFirstItemFormatter());
	}

	// sorts by cost
	protected String getItemListString(List<Item>items, ItemFormatter formatter) {
		// sort the items
		Collections.sort(items, new Comparator<Item>() {
			public int compare(Item o1, Item o2) {
				int c1 = o1.getCost();
				int c2 = o2.getCost();
				if (c1 != c2) return c1 - c2;
				String i1 = o1.getValue("item").toString();
				String i2 = o2.getValue("item").toString();
				return i1.compareTo(i2);
			}
		}); 

		// list the items
		StringBuffer output = new StringBuffer();
		for (Item i : items) {
			output.append(formatter.toString(i));
			output.append("\n");
		}

		return output.toString();
	}

	// sorts by cost
	protected void popupMessage(String message) {
		JTextArea area = new JTextArea(message);
		JScrollPane scroll = new JScrollPane(area);
		
		JFrame popup = new JFrame("All Items");
		popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		popup.getContentPane().add(scroll);
		popup.pack();
		popup.setVisible(true);
	}

	public void saveShops(ObjectOutputStream out) throws IOException {
		out.writeObject(shops);
	}

	public void writeShopsXML(String filename) {
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

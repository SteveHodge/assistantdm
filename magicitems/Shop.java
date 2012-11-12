package magicitems;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractListModel;

import magicgenerator.Field;
import magicgenerator.Generator;
import magicgenerator.Item;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * Average inventory can be calculated as follows. Given:
 * average number of new items per day (avgNew)
 * and sale chance per item (saleChance)
 * then average inventory (avgInv) = avgNew / saleChance
 * or saleChance = avgNew/avgInv
 *
 * Simplest option is to give avgInv chances at saleChance per day.
 *
 * For Myraeth:
 * Aiming for average of 3 major, 7 medium, 10 minor items
 * Typical chances of sale (per day) are: 0.05, 0.10, 0.20
 * Therefore need to add on average 0.15 major, 0.7 medium, 2 minor per day
 */
@SuppressWarnings("serial")
public class Shop extends AbstractListModel implements ItemTarget {
	List<Item> inventory;

	int day = 0;
	int[] newChance = new int[3];
	int[] sellChance = new int[3];
	int[] number = new int[3];

	transient Random random;
	transient Generator generator;
	String scriptName;
	String procName;

	public Shop(String scriptName, String procName,
			int majorChance, int majorNumber,
			int mediumChance, int mediumNumber,
			int minorChance, int minorNumber)
	{
		random = new Random();
		generator = new Generator(scriptName);
		this.procName = procName;
		this.scriptName = scriptName;

		newChance[Item.CLASS_MINOR] = minorChance;
		newChance[Item.CLASS_MEDIUM] = mediumChance;
		newChance[Item.CLASS_MAJOR] = majorChance;
		sellChance[Item.CLASS_MINOR] = minorChance;
		sellChance[Item.CLASS_MEDIUM] = mediumChance;
		sellChance[Item.CLASS_MAJOR] = majorChance;
		number[Item.CLASS_MINOR] = minorNumber;
		number[Item.CLASS_MEDIUM] = mediumNumber;
		number[Item.CLASS_MAJOR] = majorNumber;

		inventory = new ArrayList<Item>();
	}

	public void createInitialInventory() {
		// setup initial inventory:
		for (int i=0; i<3; i++) {
			for (int j=0; j<number[i]; j++) {
				Item item = generator.generate(i, procName);
				inventory.add(item);
			}
		}
	}

	public void setSellChance(int cat, int chance) {
		sellChance[cat] = chance;
	}

	public List<Item> nextDay() {
		List<Item> changes = new ArrayList<Item>();
		day++;

		// check for sold items
		Iterator<Item> ii = inventory.iterator();
		while (ii.hasNext()) {
			Item item = ii.next();
			if (random.nextInt(100) < sellChance[item.getCategory()]) {
				ii.remove();
				System.out.println("Sold ("+getCategory(item)+") "+item);
				item.setValue("sold", day);
				changes.add(item);
			}
		}

		for (int i=0; i<3; i++) {
			// check for new items
			for (int j=0; j<number[i]; j++) {
				if (random.nextInt(100) < newChance[i]) {
					Item item = generator.generate(i, procName);
					inventory.add(item);
					System.out.println("Added ("+getCategory(item)+") "+item);
					item.setValue("added", day);
					changes.add(item);
				}
			}
		}

		fireContentsChanged(this, 0, inventory.size());
		return changes;
	}

	static void printItems(List<Item> inventory) {
		for (Item item : inventory) {
			System.out.print("("+getCategory(item)+") "+item+" ("+item.getValue("cost")+")");
			Field f = item.getField("qualities");
			if (f != null) {
				String qualities = item.getValue(f).toString();
				if (qualities.length() > 0) {
					System.out.print(" - "+qualities);
				}
			}
			System.out.println();
		}
	}
	
	static String getCategory(Item i) {
		switch (i.getCategory()) {
		case Item.CLASS_MINOR:
			return "Minor";
		case Item.CLASS_MEDIUM:
			return "Medium";
		case Item.CLASS_MAJOR:
			return "Major";
		}
		return "unknown";
	}

	public Object getElementAt(int index) {
		return inventory.get(index);
	}

	public Item deleteItemAt(int i) {
		Item item = inventory.remove(i);
		fireIntervalRemoved(this, i, i);
		return item;
	}

	public void recreateItemAt(int i) {
		Item item = inventory.remove(i);
		Item newItem = generator.generate(item.getCategory(), procName);
		inventory.add(i, newItem);
		fireContentsChanged(this, i, i);
	}

	public void createItem(int power) {
		addItem(generator.generate(power, procName));
	}

	public synchronized void addItem(Item i) {
		inventory.add(i);
		fireIntervalAdded(this, inventory.size(), inventory.size());
	}

	public int getSize() {
		return inventory.size();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		random = new Random();
		generator = new Generator(scriptName);
	}

	public String getXML() {
		return getXML("", "    ");
	}

	public String getXML(String indent, String nextIndent) {
		StringBuilder s = new StringBuilder();
		s.append(indent).append("<Shop scriptName=\"").append(scriptName).append("\" procName=\"");
		s.append(procName).append("\" day=\"").append(day).append("\">\n");

		s.append(indent).append(nextIndent).append("<Minor newChance=\"");
		s.append(newChance[Item.CLASS_MINOR]).append("\" sellChance=\"");
		s.append(sellChance[Item.CLASS_MINOR]).append("\" number=\"");
		s.append(number[Item.CLASS_MINOR]).append("\"/>\n");

		s.append(indent).append(nextIndent).append("<Medium newChance=\"");
		s.append(newChance[Item.CLASS_MEDIUM]).append("\" sellChance=\"");
		s.append(sellChance[Item.CLASS_MEDIUM]).append("\" number=\"");
		s.append(number[Item.CLASS_MEDIUM]).append("\"/>\n");

		s.append(indent).append(nextIndent).append("<Major newChance=\"");
		s.append(newChance[Item.CLASS_MAJOR]).append("\" sellChance=\"");
		s.append(sellChance[Item.CLASS_MAJOR]).append("\" number=\"");
		s.append(number[Item.CLASS_MAJOR]).append("\"/>\n");

		s.append(indent).append(nextIndent).append("<Items>\n");
		for (Item i : inventory) {
			s.append(i.getXML(indent + nextIndent + nextIndent, nextIndent));
		}
		s.append(indent).append(nextIndent).append("</Items>\n");
		s.append(indent).append("</Shop>\n");
		return s.toString();
	}

	public static Shop parseDOM(Element node) {
		if (!node.getNodeName().equals("Shop")) return null;
		String script = node.getAttribute("scriptName");
		String proc = node.getAttribute("procName");
		int day = Integer.parseInt(node.getAttribute("day"));
		int majorChance = 0, majorNumber = 0, mediumChance = 0;
		int mediumNumber = 0, minorChance = 0, minorNumber = 0;
		int majorSell = -99, mediumSell = -99, minorSell = -99;

		Shop s = null;

		NodeList nodes = node.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element e = (Element)nodes.item(i);
			String tag = e.getTagName();

			if (tag.equals("Minor")) {
				minorChance = Integer.parseInt(e.getAttribute("newChance"));
				minorNumber = Integer.parseInt(e.getAttribute("number"));
				minorSell = Integer.parseInt(e.getAttribute("sellChance"));

			} else if (tag.equals("Medium")) {
				mediumChance = Integer.parseInt(e.getAttribute("newChance"));
				mediumNumber = Integer.parseInt(e.getAttribute("number"));
				mediumSell = Integer.parseInt(e.getAttribute("sellChance"));

			} else if (tag.equals("Major")) {
				majorChance = Integer.parseInt(e.getAttribute("newChance"));
				majorNumber = Integer.parseInt(e.getAttribute("number"));
				majorSell = Integer.parseInt(e.getAttribute("sellChance"));

			} else if (tag.equals("Items")) {
				s = new Shop(script,proc, majorChance, majorNumber, mediumChance, mediumNumber,
						minorChance, minorNumber);
				s.setSellChance(Item.CLASS_MINOR, minorSell);
				s.setSellChance(Item.CLASS_MEDIUM, mediumSell);
				s.setSellChance(Item.CLASS_MAJOR, majorSell);
				s.day = day;

				NodeList items = e.getChildNodes();
				for (int j=0; j<items.getLength(); j++) {
					if (!items.item(j).getNodeName().equals("Item")) continue;
					Element ie = (Element)items.item(j);
					Item item = Item.parseItemDOM(ie);
					if (item != null) {
						s.addItem(item);
					}
				}
			}
		}
		return s;
	}
}

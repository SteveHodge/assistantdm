import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractListModel;

import magicgenerator.Field;
import magicgenerator.Generator;
import magicgenerator.Item;

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
public class Shop extends AbstractListModel {
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

	public void nextDay() {
		day++;

		// check for sold items
		Iterator<Item> ii = inventory.iterator();
		while (ii.hasNext()) {
			Item item = ii.next();
			if (random.nextInt(100) < sellChance[item.getCategory()]) {
				ii.remove();
				System.out.println("Sold ("+getCategory(item)+") "+item);
			}
		}

		for (int i=0; i<3; i++) {
			// check for new items
			for (int j=0; j<number[i]; j++) {
				if (random.nextInt(100) < newChance[i]) {
					Item item = generator.generate(i, procName);
					inventory.add(item);
					System.out.println("Added ("+getCategory(item)+") "+item);
				}
			}
		}

		fireContentsChanged(this, 0, inventory.size());
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

	public int getSize() {
		return inventory.size();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		random = new Random();
		generator = new Generator(scriptName);
	}
}

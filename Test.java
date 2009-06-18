import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import magicgenerator.Field;
import magicgenerator.Generator;
import magicgenerator.Item;

/*
 * Average inventory can be calculated as follows. Given:
 * average number of new items per day (avgNew)
 * and sale chance per item (saleChance)
 * then average inventory = avgNew / saleChance
 *
 * For Myraeth:
 * Aiming for average of 3 major, 7 medium, 10 minor items
 * Typical chances of sale (per day) are: 0.05, 0.10, 0.20
 * Therefore need to add on average 0.15 major, 0.7 medium, 2 minor per day
 */
public class Test {
	public static void main(String[] args) {
		int[] chance = {20,10,5};
		int[] number = {10,7,3};

		Random r = new Random();
		Generator magic = new Generator("myraeth.txt");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		List<Item> inventory = new ArrayList<Item>();
		// setup initial inventory:
		for (int i=0; i<3; i++) {
			for (int j=0; j<number[i]; j++) {
				Item item = magic.generate(i, "Myraeth's Shop");
				inventory.add(item);
			}
		}

		System.out.println("Initial Inventory:");
		printItems(inventory);

		for (int day=0; day < 100; day++) {
			// check for sold items
			Iterator<Item> ii = inventory.iterator();
			while (ii.hasNext()) {
				Item item = ii.next();
				if (r.nextInt(100) < chance[item.getCategory()]) {
					ii.remove();
					System.out.println("Sold ("+getCategory(item)+") "+item);
				}
			}

			for (int i=0; i<3; i++) {
				// check for new items
				for (int j=0; j<number[i]; j++) {
					if (r.nextInt(100) < chance[i]) {
						Item item = magic.generate(i, "Myraeth's Shop");
						inventory.add(item);
						System.out.println("Added ("+getCategory(item)+") "+item);
					}
				}
			}

			System.out.println("\nDay "+day+":");
			printItems(inventory);

			System.out.print("Quit? ");
			System.out.flush();
			String quit;
			try {
				quit = in.readLine();
				if (quit.equalsIgnoreCase("q")
						|| quit.equalsIgnoreCase("quit")) {
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
}

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
		int DAYS=1000;

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

		int[][] data = new int[3][DAYS];

		System.out.println("Initial Inventory:");
		//printItems(inventory);
		updateData(inventory, data, 0);
		///printSummary(inventory);

		for (int day=0; day < DAYS; day++) {
			// check for sold items
			Iterator<Item> ii = inventory.iterator();
			while (ii.hasNext()) {
				Item item = ii.next();
				if (r.nextInt(100) < chance[item.getCategory()]) {
					ii.remove();
					//System.out.println("Sold ("+getCategory(item)+") "+item);
				}
			}

			for (int i=0; i<3; i++) {
				// check for new items
				for (int j=0; j<number[i]; j++) {
					if (r.nextInt(100) < chance[i]) {
						Item item = magic.generate(i, "Myraeth's Shop");
						inventory.add(item);
						//System.out.println("Added ("+getCategory(item)+") "+item);
					}
				}
			}

			//System.out.println("\nDay "+day+":");
			//printSummary(inventory);
			updateData(inventory, data, day);
//			printItems(inventory);
//
//			System.out.print("Quit? ");
//			System.out.flush();
//			String quit;
//			try {
//				quit = in.readLine();
//				if (quit.equalsIgnoreCase("q")
//						|| quit.equalsIgnoreCase("quit")) {
//					break;
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}

		double means[] = new double[3];
		for (int i=0; i<DAYS; i++) {
			means[0] += data[0][i];
			means[1] += data[1][i];
			means[2] += data[2][i];
		}
		means[0] = means[0]/DAYS;
		means[1] = means[1]/DAYS;
		means[2] = means[2]/DAYS;

		double sd[] = new double[3];
		for (int i=0; i<DAYS; i++) {
			sd[0] += (data[0][i] - means[0]) * (data[0][i] - means[0]);
			sd[1] += (data[1][i] - means[1]) * (data[1][i] - means[1]);
			sd[2] += (data[2][i] - means[2]) * (data[2][i] - means[2]);
		}
		sd[0] = Math.sqrt(sd[0]/DAYS);
		sd[1] = Math.sqrt(sd[1]/DAYS);
		sd[2] = Math.sqrt(sd[2]/DAYS);

		System.out.println(String.format("Minor: Mean = %5.2f, sd = %5.2f", means[0], sd[0]));
		System.out.println(String.format("Medium: Mean = %5.2f, sd = %5.2f", means[1], sd[1]));
		System.out.println(String.format("Major: Mean = %5.2f, sd = %5.2f", means[2], sd[2]));
	}

	static void updateData(List<Item> inventory, int[][] data, int i) {
		for (Item item : inventory) {
			data[item.getCategory()][i]++;
		}
	}

	static void printSummary(List<Item> inventory) {
		int [] counts = new int[3];
		for (Item item : inventory) {
			counts[item.getCategory()]++;
		}
		System.out.println(String.format("Minor: %02d, Medium: %02d, Major: %02d",
				counts[Item.CLASS_MINOR],
				counts[Item.CLASS_MEDIUM],
				counts[Item.CLASS_MAJOR]));
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

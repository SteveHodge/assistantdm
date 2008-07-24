import magicgenerator.Field;
import magicgenerator.Generator;
import magicgenerator.Item;
import magicgenerator.Roller;


public class TreasureGenerator {
	public static void main(String[] args) {
		Generator magic = new Generator("random_item_dmg.txt");
		int i = 0;
		//for (int i=0; i<200000; i++) {
		while(i < 10) {
			i++;
			//System.out.println();
			Item item = magic.generate(Roller.rand.nextInt(3), "Random Magic Item");
			System.out.print(""+i+": "+item+" ("+item.getValue("cost")+")");
			Field f = item.getField("qualities");
			if (f != null) {
				String qualities = item.getValue(f).toString();
				if (qualities.length() > 0) {
					System.out.print(" - "+qualities);
				}
			}
			System.out.println();

			//Item item = magic.generate(1, "Weapons");
			//System.out.println(""+item+" cost = "+item.getValue("cost")+", effective modifier = "+item.getValue("effective_modifier"));
		}
	}
}

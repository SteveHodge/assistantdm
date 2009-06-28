package magicitems;
import magicgenerator.Field;
import magicgenerator.Item;


public class DefaultItemFormatter implements ItemFormatter {
	public String toString(Item i) {
		return getItemDescription(i);
	}

	public static String getItemDescription(Item i) {
		String str = i.toString()+"\nValue: "+i.getCost();
		Field f = i.getField("qualities");
		if (f != null) {
			String quals = i.getValue(f).toString();
			if (!quals.equals("")) str += "\nQualities: "+ quals;
		}
		return str;
	}
}

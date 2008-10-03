import magicgenerator.Field;
import magicgenerator.Item;

/**
 * 
 */

class CostFirstItemFormatter implements ItemFormatter {
	public String toString(Item i) {
		StringBuffer output = new StringBuffer();
		output.append(i.getCost()).append(" - ").append(i.getValue("item"));

		Field f = i.getField("qualities");
		if (f != null) {
			String quals = i.getValue(f).toString();
			if (!quals.equals("")) output.append(" (").append(quals).append(")");
		}
		return output.toString();
	}
}
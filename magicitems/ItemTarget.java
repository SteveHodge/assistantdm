package magicitems;
import magicgenerator.Item;

// interface implemented by classes that can accept an Item
public interface ItemTarget {
	public void addItem(Item i);
}

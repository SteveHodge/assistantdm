package party;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
 * This static collection holds all the individual characters and monsters 
 */
public class CreatureLibrary implements Iterable<Creature> {
	private Set<Creature> creatures = new HashSet<Creature>();
	
	public void add(Creature c) {
		creatures.add(c);
	}
	
	public void remove(Creature c) {
		creatures.remove(c);
	}

	public Iterator<Creature> iterator() {
		return creatures.iterator();
	}
}

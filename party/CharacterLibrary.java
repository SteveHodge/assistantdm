package party;

import java.util.HashSet;
import java.util.Set;

public class CharacterLibrary {
	public static Set<Character> characters = new HashSet<Character>();

	public static void add(Character c) {
		characters.add(c);
	}
}

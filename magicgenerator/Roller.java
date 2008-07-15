package magicgenerator;
import java.util.Random;

public class Roller {
	public static java.util.Random rand = new Random();

	public static int roll() {
		return rand.nextInt(100)+1;
	}
}

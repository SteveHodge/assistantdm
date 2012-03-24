package combat;

import java.io.FileWriter;
import java.io.IOException;

public class InitiativeFileUpdater {
	static final String DESTINATION = "m:\\webcam\\ftp\\images\\initiative.txt";

	String lastOutput = "";

	public void writeFile(int round, String text) {
		String output = "round="+round+"\n"+text;
		if (!output.equals(lastOutput)) {
			//System.out.println(output);
			lastOutput = output;
			try {
				FileWriter file = new FileWriter(DESTINATION);
				file.write(output);
				file.close();
			} catch (IOException e1) {
				System.out.println("Exception writing initiative file: "+e1);
			}
		}
	}
}

package maptool;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/* TODO:
 * Open function should handle .map files
 * Functionality to trim masks
 * Add POI groups
 * BUG: masks don't respect zoom settings
 */
public class MapTool {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			MapToolFrame inst = new MapToolFrame(new File("D:/DnDBooks/_Campaigns/Ptolus Madness Rising/Maps/Ptolus/Small.png"), null);
			inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			inst.setLocationRelativeTo(null);
			inst.setVisible(true);
		});
	}

}

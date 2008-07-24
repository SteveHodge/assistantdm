import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import magicgenerator.Item;

public class ShoppingPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	List<Shop> shops = new ArrayList<Shop>();
	JLabel dayLabel;
	int day = 0;
	JTabbedPane tabbedPane;

	@SuppressWarnings("unchecked")
	public ShoppingPanel(ObjectInputStream in) throws IOException, ClassNotFoundException {
		initializeUI();
		shops = (List<Shop>)in.readObject();
		for (Shop s : shops) {
			String tabName = "shop";
			String tabTip = "";
			if (s.procName.equals("Myraeth's Shop")) {
				tabName = "Myraeth's";
				tabTip = "Myraeth's Oddities";
			} else if (s.procName.equals("Rastor's Shop")) {
				tabName = "Rastor's";
				tabTip = "Rastor's Weapons";
			} else if (s.procName.equals("Bull and Bear Armory")) {
				tabName = "Bull and Bear";
				tabTip = "Bull and Bear Armory";
			}
			JPanel panel = new ShopPanel(s);
			tabbedPane.addTab(tabName, null, panel, tabTip);
		}
	}

	public ShoppingPanel() {
		initializeUI();

		JPanel panel;
		Shop myraeth = new Shop("myraeth.txt","Myraeth's Shop",5,3,10,7,20,10);
		shops.add(myraeth);
		panel = new ShopPanel(myraeth);
		tabbedPane.addTab("Myraeth's", null, panel, "Myraeth's Oddities");

		Shop rastor = new Shop("rastor.txt","Rastor's Shop",0,0,5,1,10,5);
		rastor.setSellChance(Item.CLASS_MEDIUM, 33);
		shops.add(rastor);
		panel = new ShopPanel(rastor);
		tabbedPane.addTab("Rastor's", null, panel, "Rastor's Weapons");

		Shop bullandbear = new Shop("bullandbear.txt","Bull and Bear Armory",0,0,5,1,10,5);
		bullandbear.setSellChance(Item.CLASS_MEDIUM, 33);
		shops.add(bullandbear);
		panel = new ShopPanel(bullandbear);
		tabbedPane.addTab("Bull and Bear", null, panel, "Bull and Bear Armory");
	}

	private void initializeUI() {
		setLayout(new BorderLayout());

		JPanel header = new JPanel();
		dayLabel = new JLabel("Day = 0");
		header.add(dayLabel);
		JButton nextDayButton = new JButton("Next Day");
		nextDayButton.addActionListener(this);
		header.add(nextDayButton);
		add(header,BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();
	
		add(tabbedPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		day++;
		for (Shop s : shops) s.nextDay();
		dayLabel.setText("Day = "+day);
	}

	public void saveShops(ObjectOutputStream out) throws IOException {
		out.writeObject(shops);
	}
}

package led_control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gamesystem.HPs;
import gamesystem.core.PropertyListener;
import gamesystem.core.SimpleProperty;
import party.Character;
import party.Party;
import util.XMLUtils;

// This panel provides the ui to configure the leds for either per-character hitpoint status display or a global colour override.

// TODO this panel won't get updated if the party membership changes - needs to handle that
// TODO custom effects

@SuppressWarnings("serial")
public class LEDControllerPanel extends JPanel {
	LEDController controller;
	Party party;

	int brightness = 32;	// global brightness

	SolidRGBPattern overridePattern = new SolidRGBPattern(0, 0, 0);
	List<Region> overrideRegion = Collections.singletonList(new Region(255, true, 0, 1000, overridePattern));	// TODO should get the number of leds from the controller
	private JCheckBox overrideCheckBox;
	JColorChooser colorChooser;
	List<Color> recentColours = new ArrayList<>();

	List<Region> regions = new ArrayList<>();	// character regions
	Map<Character, Region> regionMap = new HashMap<>();

	public LEDControllerPanel(LEDController con, Party party) {
		controller = con;
		this.party = party;

		final PlayerRegionTableModel regionsModel = new PlayerRegionTableModel();
		final JTable regionsTable = new JTable(regionsModel);
		regionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		regionsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		JScrollPane regionsScrollpane = new JScrollPane(regionsTable);
		regionsScrollpane.setBorder(BorderFactory.createTitledBorder("Characters"));

		JPanel overridePanel = createOverridePanel();

		JSlider brightnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, brightness);
		brightnessSlider.setMajorTickSpacing(32);
		brightnessSlider.setMinorTickSpacing(8);
		brightnessSlider.setPaintTicks(true);
		Dimension d = brightnessSlider.getPreferredSize();
		d.width = 400;
		brightnessSlider.setMinimumSize(d);
		brightnessSlider.setPreferredSize(d);
		brightnessSlider.addChangeListener(e -> {
			if (!brightnessSlider.getValueIsAdjusting()) {
				brightness = brightnessSlider.getValue();
			}
		});

		JPanel brightnessPanel = new JPanel();
		brightnessPanel.add(new JLabel("Brightness: "));
		brightnessPanel.add(brightnessSlider);

		GridBagConstraints c = new GridBagConstraints();
		setLayout(new GridBagLayout());

		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.weightx = 1.0d;
		c.weighty = 0.0d;
		add(brightnessPanel, c);

		c.gridy++;
		c.gridwidth = 1;
		c.weighty = 1.0d;
		c.weightx = 1.0d;
		c.fill = GridBagConstraints.BOTH;
		add(regionsScrollpane, c);

		c.gridx++;
		c.weightx = 0.0d;
		c.fill = GridBagConstraints.VERTICAL;
		add(overridePanel, c);

		sendConfig();
	}

	void sendConfig() {
		if (overrideCheckBox.isSelected()) {
			controller.sendConfig(brightness, overrideRegion);
		} else {
			controller.sendConfig(brightness, regions);
		}
	}

	JPanel createOverridePanel() {
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Override"));

		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(new GridBagLayout());

		overrideCheckBox = new JCheckBox("Enabled");
		overrideCheckBox.addActionListener(e -> {
			sendConfig();
		});
		overrideCheckBox.setSelected(true);

		JPanel colorPanel = new JPanel() {
		};		// if we use a JPanel as the preview component then JColorChooser will disable the preview. So we use a subclass.
		colorPanel.setOpaque(true);
		colorPanel.setMinimumSize(new Dimension(50, 20));
		colorPanel.setPreferredSize(new Dimension(200, 50));

		colorChooser = new JColorChooser(Color.BLACK);
		colorChooser.setPreviewPanel(colorPanel);

		colorChooser.getSelectionModel().addChangeListener(e -> {
			colorPanel.setBackground(colorChooser.getColor());
			overridePattern.setColor(colorChooser.getColor());
		});

		JPanel recentsPanel = new RecentColoursPanel();

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> {
			Color newColour = colorChooser.getColor();
			for (int i = 0; i < recentColours.size(); i++) {
				if (recentColours.get(i).equals(newColour)) {
					recentColours.remove(i);
					break;
				}
			}
			recentColours.add(0, newColour);
			while (recentColours.size() > 20) {
				recentColours.remove(20);
			}
			recentsPanel.repaint();
			if (overrideCheckBox.isSelected()) sendConfig();
		});

		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0d;
		c.weighty = 0.0d;
		p.add(overrideCheckBox, c);

		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		p.add(colorChooser, c);

		c.gridy++;
		p.add(recentsPanel, c);

		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		p.add(applyButton, c);

		return p;
	}

	public Element getElement(Document doc) {
		Element e = doc.createElement("LEDControl");
		e.setAttribute("brightness", Integer.toString(brightness));

		Element e2 = doc.createElement("Regions");
		for (Character c : regionMap.keySet()) {
			Region r = regionMap.get(c);
			if (r != null) {
				Element m = doc.createElement("Region");
				m.setAttribute("name", c.getName());
				m.setAttribute("id", Integer.toString(r.id));
				m.setAttribute("start", Integer.toString(r.start));
				m.setAttribute("count", Integer.toString(r.count));
				m.setAttribute("enabled", Boolean.toString(r.enabled));
				e.appendChild(m);
			}
		}
		e.appendChild(e2);

		e2 = doc.createElement("RecentColours");
		for (Color c : recentColours) {
			Element cEl = doc.createElement("Colour");
			cEl.setAttribute("red", Integer.toString(c.getRed()));
			cEl.setAttribute("green", Integer.toString(c.getGreen()));
			cEl.setAttribute("blue", Integer.toString(c.getBlue()));
			e2.appendChild(cEl);
		}
		e.appendChild(e2);
		return e;
	}

	public void parseDOM(Party p, Document dom) {
		boolean gotConfig = false;

		Element node = XMLUtils.findNode(dom.getDocumentElement(), "LEDControl");
		if (node == null) return;
		if (node.hasAttribute("brightness")) {
			brightness = Integer.parseInt(node.getAttribute("brightness"));
			if (brightness < 0) brightness = 0;
			if (brightness > 255) brightness = 255;
		}

		Element regionsEl = XMLUtils.findNode(node, "Regions");
		if (regionsEl != null) {
			NodeList children = regionsEl.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeName().equals("Region")) {
					Element m = (Element) children.item(i);
					Character c = p.get(m.getAttribute("name"));
					if (c != null) {
						Region r = addCharacter(c);
						r.id = Integer.parseInt(m.getAttribute("id"));
						r.start = Integer.parseInt(m.getAttribute("start"));
						r.count = Integer.parseInt(m.getAttribute("count"));
						r.enabled = Boolean.parseBoolean(m.getAttribute("enabled"));
						updateCharacter(c);
					}
				}
			}
			gotConfig = true;
		}

		Element recent = XMLUtils.findNode(node, "RecentColours");
		if (recent != null) {
			NodeList children = recent.getChildNodes();
			for (int i = children.getLength() - 1; i >= 0; i--) {
				if (children.item(i).getNodeName().equals("Colour")) {
					Element colourEl = (Element) children.item(i);
					int red = Integer.parseInt(colourEl.getAttribute("red"));
					int green = Integer.parseInt(colourEl.getAttribute("green"));
					int blue = Integer.parseInt(colourEl.getAttribute("blue"));
					Color c = new Color(red, green, blue);
					recentColours.add(0, c);
				}
			}
			gotConfig = true;
		}

		if (gotConfig) sendConfig();

		if (recentColours.size() > 0) {
			colorChooser.setColor(recentColours.get(0));
		}
	}

	Region addCharacter(Character c) {
		Pattern p = new SolidRGBPattern(0, 0, 0);
		Region r = new Region(regions.size(), true, 0, 0, p);
		regions.add(r);
		regionMap.put(c, r);
		c.addPropertyListener(c.getHPStatistic(), new PropertyListener<Integer>() {
			@Override
			public void propertyChanged(SimpleProperty<Integer> source, Integer oldValue) {
				updateCharacter(c, false);
			}
		});
		return r;
	}

	void updateCharacter(Character c, boolean force) {
		boolean changed = updateCharacter(c);
		if (changed || force) {
			sendConfig();	// TODO maybe make this an updater type thing where it only sends every X seconds
		}
	}

	boolean updateCharacter(Character c) {
		HPs hps = c.getHPStatistic();
		Color color = LEDControllerPanel.getColor(hps);
		Region r = regionMap.get(c);
		SolidRGBPattern p = (SolidRGBPattern) r.pattern;
		boolean changed = (Integer) p.red != color.getRed() || (Integer) p.blue != color.getBlue() || (Integer) p.green != color.getGreen();
//		System.out.println(hps.getShortSummary() + " -> " + color + " changed? " + changed);
		p.red = color.getRed();
		p.blue = color.getBlue();
		p.green = color.getGreen();
		return changed;
	}

	static Color getColor(HPs hps) {
		// color for MaxHPs is green, full brightness
		// color for 0 is red, full brightness
		// color for -10 is black
		int curr = hps.getHPs();
		if (curr >= 0) {
			int value = hps.getMaxHPStat().getValue() > 0 ? curr * 510 / hps.getMaxHPStat().getValue() : 1;
			if (value > 510) value = 510;

			int red;
			int green;
			if (value <= 255) {
				red = 255;
				//green = (int) Math.round(Math.sqrt(value) * 16);
				green = value;
			} else {
				green = 255;
				//value = value - 255;
				//red = Math.round(256 - (value * value / 255));
				red = 510 - value;
			}
			return new Color(red, green, 0);
		} else {
			curr = curr + 10;
			if (curr < 0) curr = 0;
			int red = Math.round(255 * curr / 10);
			return new Color(red, 0, 0);
		}
	}

	class RecentColoursPanel extends JPanel {
		class RecentColourPatch extends JPanel {
			int index;

			RecentColourPatch(int i) {
				index = i;
				setMinimumSize(new Dimension(30, 30));
				setPreferredSize(new Dimension(30, 30));
				setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			}

			@Override
			protected void paintComponent(Graphics g) {
				if (index < recentColours.size()) {
					Color c = recentColours.get(index);
					if (c != null) {
						setOpaque(true);
						setBackground(c);
					} else {
						setOpaque(false);
					}
				} else {
					setOpaque(false);
				}
				super.paintComponent(g);
			}

		}

		RecentColoursPanel() {
			setBorder(BorderFactory.createTitledBorder("Recent Colours"));
			setLayout(new GridLayout(2, 10));
			for (int i = 0; i < 20; i++) {
				RecentColourPatch p = new RecentColourPatch(i);
				p.addMouseListener(new MouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (p.index < recentColours.size()) {
							Color c = recentColours.get(p.index);
							if (c != null) {
								colorChooser.setColor(c);
							}
						}
					}

					@Override
					public void mouseEntered(MouseEvent e) {
					}

					@Override
					public void mouseExited(MouseEvent e) {
					}

					@Override
					public void mousePressed(MouseEvent e) {
					}

					@Override
					public void mouseReleased(MouseEvent e) {
					}
				});
				add(p);
			}

		}
	}

	class PlayerRegionTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return party.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Character chr = party.get(row);
			Region r = regionMap.get(chr);
			if (col > 0 && r == null) return null;
			switch (col) {
			case 0:
				return chr.getName();
			case 1:
				return r.id;
			case 2:
				return r.start;
			case 3:
				return r.count;
			case 4:
				return r.enabled;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 4) return Boolean.class;
			if (col == 0) return String.class;
			return Integer.class;
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Player";
			case 1:
				return "ID";
			case 2:
				return "Start";
			case 3:
				return "Count";
			case 4:
				return "Enabled";
			}
			return super.getColumnName(col);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex != 0) return true;
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int row, int col) {
			Character chr = party.get(row);
			Region r = regionMap.get(chr);
			if (r == null) {
				r = addCharacter(chr);
			}
			switch (col) {
			case 1:
				r.id = (Integer) aValue;
				break;
			case 2:
				r.start = (Integer) aValue;
				break;
			case 3:
				r.count = (Integer) aValue;
				break;
			case 4:
				r.enabled = (Boolean) aValue;
				break;
			}
			fireTableRowsUpdated(row, row);
			updateCharacter(chr, true);
		}
	}

}

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
import java.util.List;

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

import party.Character;
import party.Party;
import util.XMLUtils;

// TODO this panel won't get updated if the party or the led controller change independently
// TODO need to handle adding/removing characters
// TODO custom effects
// TODO add "apply" button and recent colours (which will be saved)

@SuppressWarnings("serial")
public class LEDControllerPanel extends JPanel {
	HitPointLEDController controller;
	Party party;
	SolidRGBPattern overridePattern = new SolidRGBPattern(0, 0, 0);
	List<Region> overrideRegion = Collections.singletonList(new Region(255, true, 0, 1000, overridePattern));	// TODO should get the number of leds from the controller
	private JCheckBox overrideCheckBox;
	JColorChooser colorChooser;
	List<Color> recentColours = new ArrayList<>();

	public LEDControllerPanel(Party party) {
		controller = new HitPointLEDController();
		this.party = party;

		final PlayerRegionTableModel regionsModel = new PlayerRegionTableModel();
		final JTable regionsTable = new JTable(regionsModel);
		regionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		regionsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		JScrollPane regionsScrollpane = new JScrollPane(regionsTable);
		regionsScrollpane.setBorder(BorderFactory.createTitledBorder("Characters"));

		JPanel overridePanel = createOverridePanel();

		JSlider brightnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, controller.getBrightness());
		brightnessSlider.setMajorTickSpacing(32);
		brightnessSlider.setMinorTickSpacing(8);
		brightnessSlider.setPaintTicks(true);
		Dimension d = brightnessSlider.getPreferredSize();
		d.width = 400;
		brightnessSlider.setMinimumSize(d);
		brightnessSlider.setPreferredSize(d);
		brightnessSlider.addChangeListener(e -> {
			if (!brightnessSlider.getValueIsAdjusting()) {
				controller.setBrightness(brightnessSlider.getValue());
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
			controller.sendConfig(overrideRegion);
		} else {
			controller.sendConfig();
		}
	}

	JPanel createOverridePanel() {
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Override"));

		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(new GridBagLayout());

		overrideCheckBox = new JCheckBox("Enabled");
		overrideCheckBox.addActionListener(e -> {
			controller.enabled = !overrideCheckBox.isSelected();
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
		e.setAttribute("brightness", Integer.toString(controller.getBrightness()));
		Element e2 = controller.getElement(doc);
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
		Element node = XMLUtils.findNode(dom.getDocumentElement(), "LEDControl");
		if (node == null) return;
		if (node.hasAttribute("brightness")) {
			controller.setBrightness(Integer.parseInt(node.getAttribute("brightness")));
		}
		controller.parseDOM(p, node);

		Element recent = XMLUtils.findNode(node, "RecentColours");
		if (recent != null) {
			NodeList children = recent.getChildNodes();
			for (int i = children.getLength() - 1; i >= 0; i--) {
				if (children.item(i).getNodeName().equals("Colour")) {
					Element e = (Element) children.item(i);
					int red = Integer.parseInt(e.getAttribute("red"));
					int green = Integer.parseInt(e.getAttribute("green"));
					int blue = Integer.parseInt(e.getAttribute("blue"));
					Color c = new Color(red, green, blue);
					recentColours.add(0, c);
				}
			}
			sendConfig();
		}
		if (recentColours.size() > 0) {
			colorChooser.setColor(recentColours.get(0));
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
			Region r = controller.regionMap.get(chr);
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
			Region r = controller.regionMap.get(chr);
			if (r == null) {
				r = controller.addCharacter(chr);
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
			controller.updateCharacter(chr, true);
		}
	}

}

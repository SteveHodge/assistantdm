package digital_table.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Layer;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.POIGroup;
import digital_table.elements.PointOfInterest;

@SuppressWarnings("serial")
public class POIOptionsPanel extends OptionsPanel<POIGroup> {
	POIsModel poisModel;
	JTable poisTable;
	private JComboBox<Layer> layerCombo;
	private JSlider alphaSlider;
	private JComboBox<String> rotationsCombo;
	private JPanel colorPanel;
	private JPanel bgColorPanel;
	private JCheckBox bgCheck;
	private JTextField fontSizeField;
	private JTextField textField;
	private JCheckBox visibleCheck;

	POIOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new POIGroup();
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		display.addElement(element, parent);
		element.addPropertyChangeListener(listener);

		poisModel = new POIsModel();

		layerCombo = new JComboBox<>(Layer.values());
		layerCombo.setSelectedItem(poisModel.getLayer());
		layerCombo.addActionListener(e -> {
			poisModel.setLayer((Layer) layerCombo.getSelectedItem());
		});

		alphaSlider = createSliderControl(POIGroup.PROPERTY_ALPHA);
		colorPanel = createColorControl(POIGroup.PROPERTY_COLOR);
		bgColorPanel = createColorControl(POIGroup.PROPERTY_BACKGROUND_COLOR);
		rotationsCombo = createRotationControl(POIGroup.PROPERTY_ROTATIONS, Mode.ALL);
		visibleCheck = createVisibilityControl();
		bgCheck = createCheckBox(POIGroup.PROPERTY_SOLID_BACKGROUND, Mode.ALL, "solid background?");
		fontSizeField = createDoubleControl(POIGroup.PROPERTY_FONT_SIZE);
		textField = createStringControl(POIGroup.PROPERTY_LABEL);

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(new JLabel("Layer:"), c);
		c.gridy++; add(new JLabel("Label:"), c);
		c.gridy++; add(new JLabel("Font size (in grid cells):"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Background:"), c);
		c.gridy++;
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.gridx = 2;
		c.gridy = 0;
		add(visibleCheck, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(layerCombo, c);
		c.gridwidth = 2;
		c.gridy++; add(textField, c);
		c.gridy++; add(fontSizeField, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(bgColorPanel, c);
		c.gridy++; add(bgCheck, c);
		c.gridy++; add(alphaSlider, c);
		//@formatter:on

		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridy = GridBagConstraints.RELATIVE;

		JButton addButton = new JButton("Add POI");
		addButton.addActionListener(e -> {
			POI poi = new POI();
			poi.text = "text";
			poi.id = "id";
			poi.visible = false;
			poisModel.add(poi);
		});

		JButton delButton = new JButton("Delete");
		delButton.addActionListener(e -> {
			poisModel.delete(poisTable.getSelectedRow());
		});

		JButton upButton = new JButton("/\\");
		upButton.addActionListener(e -> {
			poisModel.promote(poisTable.getSelectedRow());
		});

		JButton downButton = new JButton("\\/");
		downButton.addActionListener(e -> {
			poisModel.demote(poisTable.getSelectedRow());
		});

		JPanel panel = new JPanel();
		panel.add(addButton);
		panel.add(delButton);
		panel.add(upButton);
		panel.add(downButton);

		add(panel, c);

		add(new JLabel("Points of Interest:"), c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;

		poisTable = new JTable(poisModel);
		poisTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		poisTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		poisTable.getColumnModel().getColumn(2).setPreferredWidth(160);
		poisTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(poisTable);
		scrollPane.setPreferredSize(new Dimension(200, 200));
		poisTable.setFillsViewportHeight(true);
		add(scrollPane, c);
	}

	private class POI {
		PointOfInterest element;
		Boolean visible;
		String id;
		String text;
		double relX;
		double relY;
	}

	private class POIsModel extends AbstractTableModel {
		private List<POI> pois = new ArrayList<>();
		Layer defaultLayer = Layer.MAP_FOREGROUND;

		void add(POI poi) {
			poi.element = new PointOfInterest();
			poi.element.setProperty(PointOfInterest.PROPERTY_TEXT, poi.text);
			poi.element.setProperty(PointOfInterest.PROPERTY_ID, poi.id);
			poi.element.setProperty(MapElement.PROPERTY_VISIBLE, poi.visible ? Visibility.VISIBLE : Visibility.HIDDEN);
			poi.element.setProperty(PointOfInterest.PROPERTY_REL_X, poi.relX);
			poi.element.setProperty(PointOfInterest.PROPERTY_REL_Y, poi.relY);
			display.addElement(poi.element, element);
			pois.add(poi);
			fireTableRowsInserted(pois.size() - 1, pois.size() - 1);
		}

		POI getPOI(int row) {
			if (row < 0 || row >= pois.size()) return null;
			return pois.get(row);
		}

		void delete(int row) {
			if (row >= 0) {
				POI poi = pois.remove(row);
				display.removeElement(poi.element);
				fireTableRowsDeleted(row, row);
			}
		}

		void promote(int row) {
			if (row > 0) {
				pois.add(row - 1, pois.remove(row));
				fireTableRowsUpdated(row - 1, row);
				poisTable.setRowSelectionInterval(row - 1, row - 1);
			}
		}

		void demote(int row) {
			if (row < pois.size() - 1) {
				pois.add(row + 1, pois.remove(row));
				fireTableRowsUpdated(row, row + 1);
				poisTable.setRowSelectionInterval(row + 1, row + 1);
			}
		}

		Layer getLayer() {
			if (pois.size() == 0) return defaultLayer;
			return (Layer) pois.get(0).element.getProperty(MapElement.PROPERTY_LAYER);
		}

		void setLayer(Layer layer) {
			defaultLayer = layer;
			for (POI p : pois) {
				display.setProperty(p.element, MapElement.PROPERTY_LAYER, layer);
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			POI poi = pois.get(row);
			if (col == 0) {
				poi.visible = (Boolean) value;
				display.setProperty(poi.element, MapElement.PROPERTY_VISIBLE, poi.visible ? Visibility.VISIBLE : Visibility.HIDDEN);
			}
			if (col == 1) {
				poi.id = (String) value;
				display.setProperty(poi.element, PointOfInterest.PROPERTY_ID, poi.id);
			}
			if (col == 2) {
				poi.text = (String) value;
				display.setProperty(poi.element, PointOfInterest.PROPERTY_TEXT, poi.text);
			}
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) return Boolean.class;
			return String.class;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) return "Visible?";
			if (col == 1) return "ID";
			if (col == 2) return "Text";
			return super.getColumnName(col);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return pois.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) return pois.get(row).visible;
			if (col == 1) return pois.get(row).id;
			if (col == 2) return pois.get(row).text;
			return null;
		}
	};

	@Override
	MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	private MapElementMouseListener mouseListener = new MapElementMouseListener() {
		@Override
		public MapElement getCoordElement() {
			return element;
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;
			POI poi = poisModel.getPOI(poisTable.getSelectedRow());
			if (poi != null) {
				display.setProperty(poi.element, PointOfInterest.PROPERTY_LOCATION, gridloc);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {
		}

		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {
		}
	};

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_LAYER)) {
				layerCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(POIGroup.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int) (100 * (Float) e.getNewValue()));

			} else if (e.getPropertyName().equals(POIGroup.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer) e.getNewValue());

			} else if (e.getPropertyName().equals(POIGroup.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(POIGroup.PROPERTY_BACKGROUND_COLOR)) {
				bgColorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(POIGroup.PROPERTY_SOLID_BACKGROUND)) {
				bgCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(POIGroup.PROPERTY_FONT_SIZE)) {
				fontSizeField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(POIGroup.PROPERTY_LABEL)) {
				textField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapElement.PROPERTY_DRAGGING)) {
				// ignore

			} else {
				System.out.println(toString() + ": Unknown property: " + e.getPropertyName());
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "POIGroup";
	final static String POI_TAG = "POI";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		if (e.hasAttribute(Group.PROPERTY_LOCATION))
			e.removeAttribute(Group.PROPERTY_LOCATION);	// remove location attribute as it's not actually valid for POIGroup

		for (int i = 0; i < poisModel.getRowCount(); i++) {
			POI poi = poisModel.pois.get(i);
			Element p = doc.createElement(POI_TAG);
			for (String prop : poi.element.getProperties()) {
				if (!MapElement.PROPERTY_DRAGGING.equals(prop))
					setAttribute(p, prop, poi.element.getProperty(prop));
			}
//			p.setAttribute(MapElement.PROPERTY_VISIBLE, poi.visible ? Visibility.VISIBLE.toString() : Visibility.HIDDEN.toString());
			e.appendChild(p);
		}

		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(POIGroup.PROPERTY_LABEL, e, Mode.ALL);
		parseColorAttribute(POIGroup.PROPERTY_COLOR, e, Mode.ALL);
		parseColorAttribute(POIGroup.PROPERTY_BACKGROUND_COLOR, e, Mode.ALL);
		parseFloatAttribute(POIGroup.PROPERTY_ALPHA, e, Mode.ALL);
		parseIntegerAttribute(POIGroup.PROPERTY_ROTATIONS, e, Mode.ALL);
		parseDoubleAttribute(POIGroup.PROPERTY_FONT_SIZE, e, Mode.ALL);
		parseBooleanAttribute(POIGroup.PROPERTY_SOLID_BACKGROUND, e, Mode.ALL);

		Layer layer = Layer.MAP_FOREGROUND;
		boolean layerFound = false;

		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element m = (Element) nodes.item(i);
			if (m.getTagName().equals(POI_TAG)) {
				POI poi = new POI();
				poi.id = m.getAttribute(PointOfInterest.PROPERTY_ID);
				poi.text = m.getAttribute(PointOfInterest.PROPERTY_TEXT);
				poi.visible = !m.getAttribute(MapElement.PROPERTY_VISIBLE).equals(Visibility.HIDDEN.toString());
				String attr = m.getAttribute(PointOfInterest.PROPERTY_REL_X);
				if (attr.length() > 0) {
					try {
						poi.relX = Double.parseDouble(attr);
						poi.relY = Double.parseDouble(m.getAttribute(PointOfInterest.PROPERTY_REL_Y));
					} catch (NumberFormatException ex) {
						ex.printStackTrace();
					}
				}

				if (!layerFound) {
					attr = m.getAttribute(MapElement.PROPERTY_LAYER);
					if (attr.length() > 0) {
						try {
							layer = Enum.valueOf(Layer.class, attr);
							layerFound = true;
						} catch (IllegalArgumentException ex) {
							ex.printStackTrace();
						}
					}
				}
				poisModel.add(poi);
			}
		}

		String attr = e.getAttribute(MapElement.PROPERTY_VISIBLE);
		if (attr.length() > 0) {
			try {
				Visibility v = Visibility.valueOf(attr);
				if (v == Visibility.VISIBLE) {
					display.setProperty(element, MapElement.PROPERTY_VISIBLE, v, Mode.ALL);
					visibleCheck.setSelected(true);
				} else {
					display.setProperty(element, MapElement.PROPERTY_VISIBLE, Visibility.HIDDEN, Mode.REMOTE);
					display.setProperty(element, MapElement.PROPERTY_VISIBLE, Visibility.FADED, Mode.LOCAL);
					visibleCheck.setSelected(false);
				}
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			}
		}

		layerCombo.setSelectedItem(layer);
	}
}

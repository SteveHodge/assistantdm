package digital_table.controller;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.MapImage;
import digital_table.elements.Mask;
import digital_table.server.MediaManager;

@SuppressWarnings("serial")
public class MaskOptionsPanel extends OptionsPanel<Mask> {
	MasksModel masksModel;
	JTable maskTable;

	MaskOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new Mask();
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.FADED);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;

		JButton addButton = new JButton("Add Mask");
		addButton.addActionListener(e -> {
			URI[] uris = MediaManager.INSTANCE.showMultiFileChooser(MaskOptionsPanel.this);
			if (uris != null) {
				for (URI uri : uris) {
					masksModel.add(uri);
				}
			}
		});

		JButton delButton = new JButton("Delete");
		delButton.addActionListener(e -> {
			masksModel.delete(maskTable.getSelectedRow());
		});

		JButton upButton = new JButton("/\\");
		upButton.addActionListener(e -> {
			masksModel.promote(maskTable.getSelectedRow());
		});

		JButton downButton = new JButton("\\/");
		downButton.addActionListener(e -> {
			masksModel.demote(maskTable.getSelectedRow());
		});

		JPanel panel = new JPanel();
		panel.add(addButton);
		panel.add(delButton);
		panel.add(upButton);
		panel.add(downButton);

		add(panel, c);

		add(new JLabel("Masks:"), c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;

		masksModel = new MasksModel();
		maskTable = new JTable(masksModel);
		maskTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		maskTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		maskTable.getColumnModel().getColumn(2).setPreferredWidth(5);
		maskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(maskTable);
		scrollPane.setPreferredSize(new Dimension(200, 400));
		maskTable.setFillsViewportHeight(true);
		add(scrollPane, c);
	}

	private class MasksModel extends AbstractTableModel {
		// TODO time to change to a single list of mask objects
		private List<URI> masks = new ArrayList<>();
		private List<Boolean> visible = new ArrayList<>();
		private List<String> names = new ArrayList<>();
		private List<Boolean> isImage = new ArrayList<>();

		private void add(URI uri) {
			display.setMedia(element, Mask.PROPERTY_ADD_MASK, uri);
			masks.add(uri);
			visible.add(true);
			isImage.add(false);
			String path = uri.getPath();
			if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
			if (path.contains(".")) path = path.substring(0, path.lastIndexOf('.'));
			names.add(path);
			fireTableRowsInserted(masks.size() - 1, masks.size() - 1);
		}

		void delete(int row) {
			if (row >= 0) {
				display.setProperty(element, Mask.PROPERTY_REMOVE_MASK, row);
				masks.remove(row);
				visible.remove(row);
				names.remove(row);
				isImage.remove(row);
				fireTableRowsDeleted(row, row);
			}
		}

		void promote(int row) {
			if (row > 0) {
				display.setProperty(element, Mask.PROPERTY_PROMOTE_MASK, row);
				masks.add(row - 1, masks.remove(row));
				visible.add(row - 1, visible.remove(row));
				names.add(row - 1, names.remove(row));
				isImage.add(row - 1, isImage.remove(row));
				fireTableRowsUpdated(row - 1, row);
				maskTable.setRowSelectionInterval(row - 1, row - 1);
			}
		}

		void demote(int row) {
			if (row < masks.size() - 1) {
				display.setProperty(element, Mask.PROPERTY_DEMOTE_MASK, row);
				masks.add(row + 1, masks.remove(row));
				visible.add(row + 1, visible.remove(row));
				names.add(row + 1, names.remove(row));
				isImage.add(row + 1, isImage.remove(row));
				fireTableRowsUpdated(row, row + 1);
				maskTable.setRowSelectionInterval(row + 1, row + 1);
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col == 0) {
				visible.set(row, (Boolean) value);
				display.setProperty(element, visible.get(row) ? Mask.PROPERTY_SHOW_MASK : Mask.PROPERTY_HIDE_MASK, row);
			}
			if (col == 1) names.set(row, (String) value);
			if (col == 2) {
				isImage.set(row, (Boolean) value);
				display.setProperty(element, isImage.get(row) ? Mask.PROPERTY_SET_TYPE_IMAGE : Mask.PROPERTY_SET_TYPE_MASK, row);
			}
		}

		public void setName(URI uri, String name) {
			// search from last to first because currently this is only called on newly added masks
			for (int i = masks.size() - 1; i >= 0; i--) {
				if (masks.get(i).equals(uri)) {
					names.set(i, name);
					fireTableRowsUpdated(i, i);
				}
			}
		}

		public void setVisible(URI uri, boolean b) {
			// search from last to first because currently this is only called on newly added masks
			for (int i = masks.size() - 1; i >= 0; i--) {
				if (masks.get(i).equals(uri)) {
					visible.set(i, b);
					display.setProperty(element, visible.get(i) ? Mask.PROPERTY_SHOW_MASK : Mask.PROPERTY_HIDE_MASK, i);
					fireTableRowsUpdated(i, i);
				}
			}
		}

		public void setIsImage(URI uri, boolean b) {
			for (int i = masks.size() - 1; i >= 0; i--) {
				if (masks.get(i).equals(uri)) {
					isImage.set(i, b);
					display.setProperty(element, isImage.get(i) ? Mask.PROPERTY_SET_TYPE_IMAGE : Mask.PROPERTY_SET_TYPE_MASK, i);
					fireTableRowsUpdated(i, i);
				}
			}
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) return Boolean.class;
			if (col == 2) return Boolean.class;
			return String.class;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) return "Visible?";
			if (col == 1) return "Mask Name";
			if (col == 2) return "Image?";
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
			return masks.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) return visible.get(row);
			if (col == 1) return names.get(row);
			if (col == 2) return isImage.get(row);
			return null;
		}
	};

	@Override
	MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	private MapElementMouseListener mouseListener = new MapElementMouseListener() {
		private boolean dragging = false;
		private int button;
		private boolean dragClear;	// when dragging if true then we clear cells, otherwise we reset cells

		@Override
		public MapElement getCoordElement() {
			return element.parent;
		}

		private void setCleared(Point p, boolean mask) {
			element.setCleared(p, mask);
			if (mask) {
				display.setProperty(element, MapImage.PROPERTY_CLEARCELL, p, Mode.REMOTE);
			} else {
				display.setProperty(element, MapImage.PROPERTY_UNCLEARCELL, p, Mode.REMOTE);
			}
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {
			button = e.getButton();
			dragClear = element.isCleared(gridCell(gridloc));
		}

		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {
			if (dragging) {
				setCleared(gridCell(gridloc), !dragClear);	// TODO might not be necessary - not sure if a mouseDragged event is generated or not for location of the release
				dragging = false;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;
			setCleared(gridCell(gridloc), !element.isCleared(gridCell(gridloc)));
		}

		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {
			if (dragging) {
				setCleared(gridCell(gridloc), !dragClear);
			} else if (button == MouseEvent.BUTTON1) {
				dragging = true;
			}
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "MaskSet";
	final static String MASK_TAG = "Mask";
	final static String MASK_NAME_ATTRIBUTE = "name";
	final static String FILE_ATTRIBUTE_NAME = "uri";
	final static String TYPE_ATTRIBUTE_NAME = "type";
	final static String IMAGE_TYPE = "IMAGE";
	final static String MASK_TYPE = "MASK";
	private final static String CLEARED_CELL_LIST_ATTRIBUTE = "cleared_cells";

//	private final static String FILE_ATTRIBUTE_NAME = "uri";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
//		setAllAttributes(e);

		for (int i = 0; i < masksModel.getRowCount(); i++) {
			URI uri = masksModel.masks.get(i);
			String name = masksModel.names.get(i);
			boolean visible = masksModel.visible.get(i);
			boolean isImage = masksModel.isImage.get(i);

			Element m = doc.createElement(MASK_TAG);
			m.setAttribute(FILE_ATTRIBUTE_NAME, uri.toASCIIString());
			m.setAttribute(MapElement.PROPERTY_VISIBLE, visible ? Visibility.VISIBLE.toString() : Visibility.HIDDEN.toString());
			m.setAttribute(TYPE_ATTRIBUTE_NAME, isImage ? IMAGE_TYPE : MASK_TYPE);
			m.setAttribute(MASK_NAME_ATTRIBUTE, name);
			e.appendChild(m);
		}

		setCellListAttribute(e, CLEARED_CELL_LIST_ATTRIBUTE, element.getCells());
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			Element m = (Element) nodes.item(i);
			if (m.getTagName().equals(MASK_TAG)) {
				try {
					URI uri = new URI(m.getAttribute(FILE_ATTRIBUTE_NAME));
					masksModel.add(uri);
					masksModel.setName(uri, m.getAttribute(MASK_NAME_ATTRIBUTE));
					if (m.getAttribute(MapElement.PROPERTY_VISIBLE).equals(Visibility.HIDDEN.toString())) {
						masksModel.setVisible(uri, false);
					}
					if (m.hasAttribute(TYPE_ATTRIBUTE_NAME)) {
						masksModel.setIsImage(uri, m.getAttribute(TYPE_ATTRIBUTE_NAME).equals(IMAGE_TYPE));
					}
				} catch (URISyntaxException ex) {
					ex.printStackTrace();
				}
			}
		}

		parseCellList(MapImage.PROPERTY_CLEARCELL, e, CLEARED_CELL_LIST_ATTRIBUTE, Mode.ALL);
	}

}

package digital_table.controller;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.MapImage;
import digital_table.elements.MaskedImage;
import digital_table.server.MediaManager;

@SuppressWarnings("serial")
public class MaskedImageOptionsPanel extends ImageOptionsPanel {
	MasksModel masksModel;

	MaskedImageOptionsPanel(URI uri, MapElement parent, DisplayManager r) {
		super(uri, parent, r);

		display.setProperty(element, MaskedImage.PROPERTY_MASK_VISIBILITY, Visibility.FADED, Mode.LOCAL);
	}

	@Override
	MapImage createElement(String label) {
		return new MaskedImage(label);
	}

	@Override
	void addExtraControls() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;

		JButton addButton = new JButton("Add Mask");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				URI uri = MediaManager.INSTANCE.showFileChooser(MaskedImageOptionsPanel.this);
				if (uri != null) {
					masksModel.add(uri);
				}
			}
		});

//		JButton stopButton = new JButton("Stop");
//		stopButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				display.setProperty(element, LineTemplate.PROPERTY_IMAGE_STOP, null);
//			}
//		});

		JPanel panel = new JPanel();
		panel.add(addButton);
//		panel.add(stopButton);

		add(panel, c);

		c.gridx = 0;
		add(new JLabel("Masks:"), c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridwidth = 2;

		masksModel = new MasksModel();
		JTable maskTable = new JTable(masksModel);
		maskTable.getColumnModel().getColumn(0).setPreferredWidth(2);
		maskTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		JScrollPane scrollPane = new JScrollPane(maskTable);
		scrollPane.setPreferredSize(new Dimension(10, 10));
		maskTable.setFillsViewportHeight(true);
		add(scrollPane, c);
	}

	private class MasksModel extends AbstractTableModel {
		private List<URI> masks = new ArrayList<URI>();
		private List<Boolean> visible = new ArrayList<Boolean>();
		private List<String> names = new ArrayList<String>();

		private void add(URI uri) {
			display.setProperty(element, MaskedImage.PROPERTY_ADD_MASK, uri);
			masks.add(uri);
			visible.add(true);
			String path = uri.getPath();
			if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
			if (path.contains(".")) path = path.substring(0, path.lastIndexOf('.'));
			names.add(path);
			fireTableRowsInserted(masks.size() - 1, masks.size() - 1);
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col == 0) {
				visible.set(row, (Boolean) value);
				display.setProperty(element, visible.get(row) ? MaskedImage.PROPERTY_SHOW_MASK : MaskedImage.PROPERTY_HIDE_MASK, row);
			}
			if (col == 1) names.set(row, (String) value);
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
					display.setProperty(element, visible.get(i) ? MaskedImage.PROPERTY_SHOW_MASK : MaskedImage.PROPERTY_HIDE_MASK, i);
					fireTableRowsUpdated(i, i);
				}
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
			if (col == 1) return "Mask Name";
			return super.getColumnName(col);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return masks.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) return visible.get(row);
			if (col == 1) return names.get(row);
			return null;
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "MaskedImage";
	final static String MASK_TAG = "Mask";
	final static String MASK_NAME_ATTRIBUTE = "Name";

	@Override
	String getTag() {
		return XML_TAG;
	}

	@Override
	Element getElement(Document doc) {
		Element e = super.getElement(doc);

		for (int i = 0; i < masksModel.getRowCount(); i++) {
			URI uri = masksModel.masks.get(i);
			String name = masksModel.names.get(i);
			boolean visible = masksModel.visible.get(i);

			Element m = doc.createElement(MASK_TAG);
			m.setAttribute(FILE_ATTRIBUTE_NAME, uri.toASCIIString());
			m.setAttribute(MapElement.PROPERTY_VISIBLE, visible ? Visibility.VISIBLE.toString() : Visibility.HIDDEN.toString());
			m.setAttribute(MASK_NAME_ATTRIBUTE, name);
			e.appendChild(m);
		}

		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		super.parseDOM(e, parent);

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
				} catch (URISyntaxException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}

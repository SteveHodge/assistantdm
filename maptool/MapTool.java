package maptool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.XMLUtils;

@SuppressWarnings("serial")
public class MapTool extends JFrame {
	double scale = 1.0d;
	JLabel zoomLabel;
	JLabel sizeLabel;
	JLabel gridLabel;
	JLabel pixelLabel;
	JLabel memLabel;
	ScalableImagePanel imagePane;
	BufferedImage image;
	JLayeredPane canvas;
	File lastDir = null;
	JFileChooser chooser = new JFileChooser();
	JToggleButton grid1;
	JToggleButton grid2;
	Point grid1loc, grid2loc;
	JFormattedTextField cols;
	JFormattedTextField rows;
	ButtonGroup gridGroup;
	GridPanel grid;
	URI imageURI;
	JTable maskTable;
	MasksModel masksModel;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			MapTool inst = new MapTool();
			inst.setLocationRelativeTo(null);
			inst.setVisible(true);
		});
	}

	MapTool() {
		super("MapTool");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		lastDir = new File("D:/Programming/git/assistantdm/media/Maps/Chapter 5/The Halls of Wrath/Background.png");
		imageURI = lastDir.toURI();
		image = openImage(lastDir);

		imagePane = new ScalableImagePanel(image);
		imagePane.addMouseListener(mouseListener);

		grid = new GridPanel(imagePane);

		canvas = new JLayeredPane();
		canvas.setLayout(new MapLayoutManager(imagePane));
		canvas.add(imagePane, JLayeredPane.DEFAULT_LAYER);
		canvas.add(grid, new Integer(100));

		JScrollPane scroller = new JScrollPane(canvas);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		setLayout(new BorderLayout());

		add(scroller, BorderLayout.CENTER);
		add(makeControls(), BorderLayout.LINE_END);
		setZoom(1.0d);

		setSize(1200, 800);
		setExtendedState(MAXIMIZED_BOTH);

	}

	MouseListener mouseListener = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (!grid1.isSelected() && !grid2.isSelected()) return;
			Point p = new Point(e.getX(), e.getY());
			if (grid1.isSelected()) {
				grid1loc = p;
				grid.setRef1(p.x, p.y);
				grid2.setSelected(true);
			} else if (grid2.isSelected()) {
				grid2loc = p;
				grid.setRef2(p.x, p.y);
				grid1.setSelected(true);
			}
			updateGrid();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	};

	private void setZoom(double zoom) {
		scale = zoom;
		imagePane.scale(scale);
		zoomLabel.setText(String.format("Zoom: %.1f%%", 100 * scale));
	}

	private BufferedImage openImage(File f) {
		byte[] bytes = new byte[0];
		BufferedImage image = null;

		try (FileInputStream in = new FileInputStream(f);) {
			bytes = new byte[(int) f.length()];
			in.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			ImageInputStream iis = ImageIO.createImageInputStream(stream);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				reader.setInput(iis);

				image = reader.read(reader.getMinIndex());

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (image == null) {
			System.out.println("No image");
		}

		return image;
	}

	private void openFile() {
		File f = null;

		if (lastDir != null) chooser.setCurrentDirectory(lastDir);
		chooser.setMultiSelectionEnabled(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
		} else {
			System.out.println("Cancelled");
			return;
		}

		if (f == null) {
			System.out.println("No file selected");
			return;
		}

		masksModel.deleteAll();

		lastDir = f;
		System.out.println(f);
		image = openImage(f);
		imageURI = f.toURI();
		imagePane.setImage(image);
		setZoom(1.0d);
		sizeLabel.setText(String.format("Original size: %dx%d\n", image.getWidth(), image.getHeight()));
	}

	private void saveFile() {
		File f = null;

		if (lastDir != null) chooser.setCurrentDirectory(lastDir);
		chooser.setMultiSelectionEnabled(false);
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
		} else {
			System.out.println("Cancelled");
			return;
		}

		if (f == null) {
			System.out.println("No file selected");
			return;
		}

		lastDir = f;
		if (!f.getName().endsWith(".map")) {
			f = new File(f.toString() + ".map");
		}
		System.out.println(f);

		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("Map");
			if (image != null) {
				root.setAttribute("uri", imageURI.toString());
				if (grid.getGridCellWidth() != 0) {
					root.setAttribute("width", Double.toString(grid.getGridWidth()));
					root.setAttribute("height", Double.toString(grid.getGridHeight()));
					double xoff = grid.getXOffset();
					double yoff = grid.getYOffset();
					root.setAttribute("location", String.format("%f,  %f", xoff, yoff));
				}
			}

//		    <Image
//			alpha="1.0"
//			aspect_locked="true"
//			background="#FFFFFF"
//			label="Background.png"
//			remote_visible="VISIBLE"
//			rotations="3"
//			show_background="false"
//			show_border="false"
//			visible="VISIBLE"

			doc.appendChild(root);

//			Element el = doc.createElement("Elements");
//			List<MapElement> selected = elements.getSelectedValuesList();
//			for (MapElement element : selected) {
//				if (!(element instanceof Grid)) {
//					addElement(doc, el, element);
//				}
//			}
//			root.appendChild(el);

			XMLUtils.writeDOM(doc, f);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void updateGrid() {
		double w = grid.getGridWidth();
		double h = grid.getGridHeight();
		if (w != 0 && h != 0) {
			gridLabel.setText(String.format("%.2f x %.2f", w, h));
		}
		double pixels = w * h * 25400 * 25400 / (294 * 294);	// based on 0.294 mm dot pitch monitor
		pixelLabel.setText(String.format("%.2f MP", (pixels / 1000000f)));
		// TODO need to get size of each mask and include that
		long mem = ((long) pixels) * 4L;
		String unit = "B";
		if (mem >= 1024 && mem < 1024 * 1024) {
			unit = "kB";
			mem = mem / 1024;
		} else if (mem < 1024 * 1024 * 1024) {
			unit = "MB";
			mem = mem / (1024 * 1024);
		} else {
			unit = "GB";
			mem = mem / (1024 * 1024 * 1024);
		}
		memLabel.setText(mem + " " + unit);
	}

	private JPanel makeControls() {
		JButton openButton = new JButton("Open");
		openButton.addActionListener(e -> openFile());

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> saveFile());

		sizeLabel = new JLabel(String.format("Original size: %dx%d\n", image.getWidth(), image.getHeight()));

		zoomLabel = new JLabel("Zoom: ");
		JButton zoomIn = new JButton("+");
		zoomIn.addActionListener(e -> setZoom(scale * Math.sqrt(2)));
		JButton zoomOut = new JButton("-");
		zoomOut.addActionListener(e -> setZoom(scale / Math.sqrt(2)));
		JButton zoomFit = new JButton("Fit");
		zoomFit.addActionListener(e -> {
			Insets insets = imagePane.getInsets();
			int availWidth = imagePane.getWidth() - insets.right - insets.left;
			int availHeight = imagePane.getHeight() - insets.right - insets.left;
			imagePane.scaleToFit(availWidth, availHeight);
			scale = imagePane.getScale();
			zoomLabel.setText(String.format("Zoom: %.1f%%", 100 * scale));
		});
		JButton zoom100 = new JButton("100%");
		zoom100.addActionListener(e -> setZoom(1.0d));

		grid1 = new JToggleButton("1");
		grid1.setSelected(true);
		grid2 = new JToggleButton("2");
		gridGroup = new ButtonGroup();
		gridGroup.add(grid1);
		gridGroup.add(grid2);
		cols = new JFormattedTextField(10);
		cols.addActionListener(e -> {
			Integer v = (Integer) cols.getValue();
			grid.setRefSeparationColumns(v == null ? 0 : v.intValue());
			updateGrid();
		});
		rows = new JFormattedTextField(10);
		rows.addActionListener(e -> {
			Integer v = (Integer) rows.getValue();
			grid.setRefSeparationRows(v == null ? 0 : v.intValue());
			updateGrid();
		});
		grid.setRefSeparationColumns(10);
		grid.setRefSeparationRows(10);
		gridLabel = new JLabel(" ");
		pixelLabel = new JLabel(" ");
		memLabel = new JLabel(" ");

		JPanel pane = new JPanel(new GridBagLayout());
		pane.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 6));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		pane.add(openButton, c);
		c.gridy++;
		pane.add(saveButton, c);
		c.gridy++;
		addSeparator(pane, c);

		c.gridy++;
		pane.add(sizeLabel, c);
		c.gridy++;
		addSeparator(pane, c);

		c.gridy++;
		pane.add(zoomLabel, c);
		c.gridwidth = 1;
		c.weightx = 0.5d;
		c.gridy++;
		pane.add(zoomIn, c);
		c.gridx++;
		pane.add(zoomOut, c);
		c.gridy++;
		c.gridx = 0;
		pane.add(zoomFit, c);
		c.gridx++;
		pane.add(zoom100, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		addSeparator(pane, c);

		c.gridy++;
		pane.add(new JLabel("Set grid refs:"), c);
		c.gridy++;
		c.gridwidth = 1;
		pane.add(grid1, c);
		c.gridx++;
		pane.add(grid2, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		pane.add(new JLabel("Ref separation:"), c);
		c.gridy++;
		c.gridwidth = 1;
		pane.add(cols, c);
		c.gridx++;
		pane.add(rows, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		pane.add(new JLabel("Map size in squares:"), c);
		c.gridy++;
		pane.add(gridLabel, c);
		c.gridy++;
		pane.add(new JLabel("Pixels:"), c);
		c.gridy++;
		pane.add(pixelLabel, c);
		c.gridy++;
		pane.add(new JLabel("Memory required:"), c);
		c.gridy++;
		pane.add(memLabel, c);
		c.gridy++;
		addSeparator(pane, c);

		c.gridy++;
		pane.add(getMaskPanel(), c);

		c.gridy++;
		c.gridwidth = 2;
		c.weighty = 1.0d;
		c.fill = GridBagConstraints.BOTH;
		JPanel p = new JPanel();
		pane.add(p, c);
		return pane;
	}

	private JPanel getMaskPanel() {
		JButton addMaskButton = new JButton("Add Mask");
		addMaskButton.addActionListener(e -> {
			if (lastDir != null) chooser.setCurrentDirectory(lastDir);
			chooser.setMultiSelectionEnabled(true);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File[] fs = chooser.getSelectedFiles();
				if (fs != null) {
					for (int i = 0; i < fs.length; i++) {
						lastDir = fs[i];
						masksModel.add(fs[i]);
					}
				}
//			} else {
//				System.out.println("Cancelled");
			}
		});

		JButton delMaskButton = new JButton("Delete");
		delMaskButton.addActionListener(e -> {
			masksModel.delete(maskTable.getSelectedRow());
		});

		JButton upMaskButton = new JButton("/\\");
		upMaskButton.addActionListener(e -> {
			masksModel.promote(maskTable.getSelectedRow());
		});

		JButton downMaskButton = new JButton("\\/");
		downMaskButton.addActionListener(e -> {
			masksModel.demote(maskTable.getSelectedRow());
		});

		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();


		masksModel = new MasksModel();
		maskTable = new JTable(masksModel);
		maskTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		maskTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		maskTable.getColumnModel().getColumn(2).setPreferredWidth(5);
		maskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(maskTable);
		scrollPane.setPreferredSize(new Dimension(300, 400));
		maskTable.setFillsViewportHeight(true);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		p.add(addMaskButton, c);
		c.gridx++;
		p.add(delMaskButton, c);
		c.gridx++;
		p.add(upMaskButton, c);
		c.gridx++;
		p.add(downMaskButton, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.weightx = 1.0d;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 4;
		p.add(scrollPane, c);

		return p;
	}

	private void addSeparator(JPanel p, GridBagConstraints c) {
		c.insets = new Insets(10, 3, 10, 3);
		p.add(new JSeparator(), c);
		c.insets = new Insets(0, 0, 0, 0);
	}

	private static class Mask {
		File file;
		boolean visible;
		String name;
		boolean isImage;
		BufferedImage image;
		ScalableImagePanel imagePane;
	}

	private class MasksModel extends AbstractTableModel {
		private List<Mask> masks = new ArrayList<>();

		private void add(File file) {
			Mask m = new Mask();
			m.file = file;
			m.visible = true;
			m.isImage = false;
			m.name = file.getName();
			m.image = openImage(m.file);
			if (m.image != null) {
				m.imagePane = new ScalableImagePanel(m.image);
				m.imagePane.setAlpha(0.50f);
				m.imagePane.setOpaque(false);
				masks.add(m);
				canvas.add(m.imagePane, new Integer(50));
				canvas.setLayer(m.imagePane, 50, masks.size() - 1);
				canvas.revalidate();
				fireTableRowsInserted(masks.size() - 1, masks.size() - 1);
			}
		}

		void deleteAll() {
			for (int i = masks.size() - 1; i >= 0; i--) {
				delete(i);
			}
		}

		void delete(int row) {
			Mask m = masks.remove(row);
			if (m != null) {
				System.out.println("removing");
				canvas.remove(m.imagePane);
				canvas.repaint();
			}
			fireTableRowsDeleted(row, row);
		}

		void promote(int row) {
			if (row > 0) {
				masks.add(row - 1, masks.remove(row));
				// we update the layering for all masks as delete() leaves gaps in the order
				for (int i = 0; i < masks.size(); i++) {
					Mask m = masks.get(i);
					canvas.setLayer(m.imagePane, m.isImage ? 25 : 50, i);
				}
				fireTableRowsUpdated(row - 1, row);
				maskTable.setRowSelectionInterval(row - 1, row - 1);
			}
		}

		void demote(int row) {
			if (row < masks.size() - 1) {
				masks.add(row + 1, masks.remove(row));
				// we update the layering for all masks as delete() leaves gaps in the order
				for (int i = 0; i < masks.size(); i++) {
					Mask m = masks.get(i);
					canvas.setLayer(m.imagePane, m.isImage ? 25 : 50, i);
				}
				fireTableRowsUpdated(row, row + 1);
				maskTable.setRowSelectionInterval(row + 1, row + 1);
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			Mask m = masks.get(row);
			if (m == null) return;
			if (col == 0) {
				m.visible = (Boolean) value;
				m.imagePane.setVisible(m.visible);
			}
			if (col == 1) m.name = (String) value;
			if (col == 2) {
				m.isImage = (Boolean) value;
				m.imagePane.setAlpha(m.isImage ? 1.0f : 0.5f);
				canvas.setLayer(m.imagePane, m.isImage ? 25 : 50, masks.indexOf(m));
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
			if (col == 0) return masks.get(row).visible;
			if (col == 1) return masks.get(row).name;
			if (col == 2) return masks.get(row).isImage;
			return null;
		}
	}
}

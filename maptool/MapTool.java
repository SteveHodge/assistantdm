package maptool;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.JOptionPane;
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

/* TODO:
 * Open function should handle .map files
 * Functionality to trim masks
 * Add POI groups
 * BUG: masks don't respect zoom settings
 */
@SuppressWarnings("serial")
public class MapTool extends JFrame {
	// TODO should probably use MediaManager for file handling
	private File mediaPath = new File("media/");
	private URI mediaURI = mediaPath.getAbsoluteFile().toURI();

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
	JToggleButton locateButton;
	Point grid1loc, grid2loc;
	JFormattedTextField cols;
	JFormattedTextField rows;
	ButtonGroup gridGroup;
	GridPanel grid;
	URI imageURI;
	JTable maskTable;
	MasksModel masksModel;
	POIsModel poisModel;
	JTable poisTable;
	JScrollPane imageScroller;

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

		lastDir = new File("D:/DnDBooks/_Campaigns/Ptolus Madness Rising/Maps/Ptolus/Small.png");
		imageURI = lastDir.toURI();
		image = openImage(lastDir);

		imagePane = new ScalableImagePanel(image);
		imagePane.addMouseListener(mouseListener);

		grid = new GridPanel(imagePane);

		canvas = new JLayeredPane();
		canvas.setLayout(new MapLayoutManager(imagePane));
		canvas.add(imagePane, JLayeredPane.DEFAULT_LAYER);
		canvas.add(grid, new Integer(100));

		imageScroller = new JScrollPane(canvas);
		imageScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		imageScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		setLayout(new BorderLayout());

		add(imageScroller, BorderLayout.CENTER);
		add(makeControls(), BorderLayout.LINE_END);
		setZoom(1.0d);

		setSize(1200, 800);
		setExtendedState(MAXIMIZED_BOTH);
		updateGrid();
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
			if (image == null) return;
			if (!grid1.isSelected() && !grid2.isSelected() && !locateButton.isSelected()) return;
			Point p = new Point(e.getX(), e.getY());
			if (locateButton.isSelected()) {
				int selectedIdx = poisTable.getSelectedRow();
				POI selected = poisModel.getPOI(selectedIdx);
				if (selected == null) return;
				selected.relX = p.getX() / imagePane.displayWidth;
				selected.relY = p.getY() / imagePane.displayHeight;
				grid.setPOI(selected);
				if (++selectedIdx < poisModel.getRowCount())
					poisTable.setRowSelectionInterval(selectedIdx, selectedIdx);	// advance to next poi

			} else {
				if (grid1.isSelected()) {
					grid1loc = p;
					grid.setRef1(p);
					grid2.setSelected(true);
				} else if (grid2.isSelected()) {
					grid2loc = p;
					grid.setRef2(p);
					grid1.setSelected(true);
				}
				updateGrid();
			}
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

	BufferedImage openImage(File f) {
		byte[] bytes = new byte[0];
		BufferedImage image = null;

		try (FileInputStream in = new FileInputStream(f);) {
			bytes = new byte[(int) f.length()];
			in.read(bytes);
		} catch (IOException e) {
			System.err.println(e);
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
			System.err.println(e);
		}

		if (image == null) {
			System.err.println("No image");
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
		if (image == null) return;
		setZoom(1.0d);
		sizeLabel.setText(String.format("Original size: %dx%d\n", image.getWidth(), image.getHeight()));
		updateGrid();
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
				URI uri = mediaURI.relativize(imageURI);
				root.setAttribute("uri", uri.toString());
				if (grid.getGridCellWidth() != 0) {
					root.setAttribute("width", Double.toString(grid.getGridWidth()));
					root.setAttribute("height", Double.toString(grid.getGridHeight()));
					double xoff = grid.getXOffset();
					double yoff = grid.getYOffset();
					root.setAttribute("location", String.format("%f,  %f", xoff, yoff));
				}
			}

			doc.appendChild(root);

			if (masksModel.getRowCount() > 0) {
				Element el = doc.createElement("MaskSet");
				for (int i = 0; i < masksModel.getRowCount(); i++) {
					Mask mask = masksModel.masks.get(i);
					Element m = doc.createElement("Mask");
					m.setAttribute("name", mask.name);
					m.setAttribute("type", mask.isImage ? "IMAGE" : "MASK");
					URI uri = mask.file.toURI();
					uri = mediaURI.relativize(uri);
					m.setAttribute("uri", uri.toString());
					m.setAttribute("visible", mask.visible ? "VISIBLE" : "HIDDEN");
					el.appendChild(m);
				}
				root.appendChild(el);
			}

			if (poisModel.getRowCount() > 0) {
				Element el = doc.createElement("POIGroup");
				el.setAttribute("visible", "FADED");
				for (int i = 0; i < poisModel.getRowCount(); i++) {
					POI poi = poisModel.getPOI(i);
					Element m = doc.createElement("POI");
					m.setAttribute("text", poi.text);
					m.setAttribute("id", poi.id);
					m.setAttribute("rel-x", "" + poi.relX);
					m.setAttribute("rel-y", "" + poi.relY);
					m.setAttribute("visible", "VISIBLE");
					el.appendChild(m);
				}
				root.appendChild(el);
			}

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

		double mem = 0;
		// calculate the scale factor in x and y directions from the original image to the remote image
		// for the map image we could just use the pixel calculation above but masks may be smaller
		double xscale = image.getWidth() / w;		//source image pixels per grid
		double yscale = image.getHeight() / h;
		for (int i = 0; i < canvas.getComponentCount(); i++) {
			Component c = canvas.getComponent(i);
			if (c instanceof ScalableImagePanel) {
				ScalableImagePanel s = (ScalableImagePanel) c;
				int imgW = 0;
				int imgH = 0;
				if (!(s.sourceImage instanceof BufferedImage)) {
					// currently only handle BufferedImages which is all that we shound encounter
					System.err.println("Unexpected Image type");
					return;
				}
				String name = s.toString();
				if (s == imagePane) name = "Background";
				BufferedImage img = (BufferedImage) s.sourceImage;
				for (int j = 0; j < masksModel.getRowCount(); j++) {
					Mask mask = masksModel.masks.get(j);
					if (mask.imagePane == s) {
						name = mask.name;
						if (mask.trimmedHeight > 0 && mask.trimmedWidth > 0) {
							imgW = mask.trimmedWidth;
							imgH = mask.trimmedHeight;
							System.out.println("Trimmed size = " + imgW + " x " + imgH + ", original size = " + img.getWidth() + " x " + img.getHeight());
						}
						break;
					}
				}
				if (imgW == 0 || imgH == 0) {
					imgW = img.getWidth();
					imgH = img.getHeight();
				}
				double remoteWidth = 25400 * imgW / (xscale * 294);	// source image size in grid units * pixels per grid on remote display
				double remoteHeight = 25400 * imgH / (yscale * 294);
				long m = (long) remoteWidth * (long) remoteHeight;
				mem += m * img.getColorModel().getPixelSize() / 8;
				System.out.format("%s: Remote pixels = %d, memory = %.2f MB\n", name, m, (double) m * img.getColorModel().getPixelSize() / (8 * 1024 * 1024));
			}
		}

		System.out.println("   Total image memory = " + formatMem(mem));
		System.out.println("   Runtime free memory (bytes): " + formatMem(Runtime.getRuntime().freeMemory()));
		long maxMemory = Runtime.getRuntime().maxMemory();
		System.out.println("   Maximum memory (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : formatMem(maxMemory)));
		System.out.println("   Runtime total memory (bytes): " + formatMem(Runtime.getRuntime().totalMemory()));
		long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
		System.out.println("   Presumable free memory (bytes): " + formatMem(presumableFreeMemory));

		memLabel.setText(formatMem(mem));
	}

	private String formatMem(double mem) {
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
		return String.format("%.2f %s", mem, unit);
	}

	private JPanel makeControls() {
		JButton openButton = new JButton("Open");
		openButton.addActionListener(e -> openFile());

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> saveFile());

		if (image != null)
			sizeLabel = new JLabel(String.format("Original size: %dx%d\n", image.getWidth(), image.getHeight()));
		else
			sizeLabel = new JLabel("Original size: 0x0\n");

		zoomLabel = new JLabel("Zoom: ");
		JButton zoomIn = new JButton("+");
		zoomIn.addActionListener(e -> setZoom(scale * Math.sqrt(2)));
		JButton zoomOut = new JButton("-");
		zoomOut.addActionListener(e -> setZoom(scale / Math.sqrt(2)));
		JButton zoomFit = new JButton("Fit");
		zoomFit.addActionListener(e -> {
			Dimension avail = imageScroller.getViewport().getExtentSize();
			imagePane.scaleToFit(avail.width, avail.height);
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
		c.weightx = 1.0d;
		addSeparator(pane, c);

		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		pane.add(getMaskPanel(), c);

		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0d;
		addSeparator(pane, c);

		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		pane.add(getPOIPanel(), c);

		return pane;
	}

	private JPanel getMaskPanel() {
		JButton addMaskButton = new JButton("Add Mask");
		addMaskButton.addActionListener(e -> {
			if (lastDir != null) chooser.setCurrentDirectory(lastDir);
			chooser.setMultiSelectionEnabled(true);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File[] fs = chooser.getSelectedFiles();
				if (fs != null && fs.length > 0) {
					for (int i = 0; i < fs.length; i++) {
						lastDir = fs[i];
						masksModel.add(fs[i]);
					}
					updateGrid();
				}
//			} else {
//				System.out.println("Cancelled");
			}
		});

		JButton delMaskButton = new JButton("Delete");
		delMaskButton.addActionListener(e -> {
			masksModel.delete(maskTable.getSelectedRow());
			updateGrid();
		});

		JButton upMaskButton = new JButton("/\\");
		upMaskButton.addActionListener(e -> {
			masksModel.promote(maskTable.getSelectedRow());
		});

		JButton downMaskButton = new JButton("\\/");
		downMaskButton.addActionListener(e -> {
			masksModel.demote(maskTable.getSelectedRow());
		});

		JButton trimButton = new JButton("Test trim masks");
		trimButton.addActionListener(e -> masksModel.trimMasks());

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
		c.weightx = 0.25d;

		p.add(addMaskButton, c);
		c.gridx++;
		p.add(delMaskButton, c);
		c.gridx++;
		p.add(upMaskButton, c);
		c.gridx++;
		p.add(downMaskButton, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 4;
		p.add(trimButton, c);

		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.weightx = 1.0d;
		p.add(scrollPane, c);
		return p;
	}

	private JPanel getPOIPanel() {
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JButton addButton = new JButton("Add POI");
		addButton.addActionListener(e -> {
			POI poi = new POI();
			poi.text = "";
			poi.id = "id";
			poisModel.add(poi);
		});

		JButton addManyButton = new JButton("Add Multiple");
		addManyButton.addActionListener(e -> {
			String startStr = JOptionPane.showInputDialog(p, "Enter first POI's ID:", "1");
			// if startStr is an integer, use that. if startStr ends with an integer then remember the prefix. otherwise just use the startStr as is
			int start = -1;
			try {
				start = Integer.parseInt(startStr);
				startStr = "";
			} catch (Exception x) {
			}
			if (start == -1) {
				Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
				Matcher matcher = lastIntPattern.matcher(startStr);
				if (matcher.find()) {
					start = Integer.parseInt(matcher.group(1));
					startStr = startStr.substring(0, matcher.start(1));
				}
			}
			if (start == -1) {
				String countStr = JOptionPane.showInputDialog(p, "Enter number of POIs to create:", "1");
				try {
					int count = Integer.parseInt(countStr);
					for (int i = 0; i < count; i++) {
						POI poi = new POI();
						poi.text = "";
						poi.id = startStr;
						poisModel.add(poi);
					}
				} catch (Exception x) {
				}
			} else {
				String countStr = JOptionPane.showInputDialog(p, "Enter last ID number or number of POIs to create (with leading '+')", "+1");
				int end = -1;
				try {
					if (countStr.startsWith("+")) {
						end = start + Integer.parseInt(countStr.substring(1));
					} else {
						end = Integer.parseInt(countStr) + 1;
					}
					for (int i = start; i < end; i++) {
						POI poi = new POI();
						poi.text = "";
						poi.id = startStr + i;
						poisModel.add(poi);
					}
				} catch (Exception x) {
				}
			}
		});

		JButton delButton = new JButton("Delete");
		delButton.addActionListener(e -> {
			poisModel.delete(poisTable.getSelectedRow());
		});

		locateButton = new JToggleButton("Locate");
		gridGroup.add(locateButton);

		poisModel = new POIsModel();
		poisTable = new JTable(poisModel);
		poisTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		poisTable.getColumnModel().getColumn(1).setPreferredWidth(160);
		poisTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane poiScrollPane = new JScrollPane(poisTable);
		poiScrollPane.setPreferredSize(new Dimension(200, 200));
		poisTable.setFillsViewportHeight(true);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 0.2d;
		c.fill = GridBagConstraints.HORIZONTAL;

		p.add(addButton, c);
		c.gridx++;
		p.add(addManyButton, c);
		c.gridx++;
		p.add(delButton, c);
		c.gridx++;
		p.add(locateButton, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.weightx = 1.0d;
		p.add(poiScrollPane, c);
		return p;
	}

	private void addSeparator(JPanel p, GridBagConstraints c) {
		c.insets = new Insets(10, 3, 10, 3);
		p.add(new JSeparator(), c);
		c.insets = new Insets(0, 0, 0, 0);
	}

	static class Mask {
		File file;
		boolean visible;
		String name;
		boolean isImage;
		BufferedImage image;
		ScalableImagePanel imagePane;
		int trimmedWidth = -1;
		int trimmedHeight = -1;
		int xOffset = 0;
		int yOffset = 0;
	}


	class MasksModel extends AbstractTableModel {
		private List<Mask> masks = new ArrayList<>();

		void add(File file) {
			Mask m = new MapTool.Mask();
			m.file = file;
			m.visible = true;
			m.isImage = false;
			m.name = file.getName();
			if (m.name.contains(".")) m.name = m.name.substring(0, m.name.lastIndexOf('.'));
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

		void trimMasks() {
			for (Mask m : masks) {
				if (m.imagePane.sourceImage instanceof BufferedImage) {
					BufferedImage img = (BufferedImage) m.imagePane.sourceImage;
					WritableRaster raster = img.getRaster();
					int[] row = null;
					int minX = raster.getWidth() * 4;
					int maxX = -1;
					int minY = raster.getHeight();
					int maxY = -1;
					for (int y = 0; y < raster.getHeight(); y++) {
						row = raster.getPixels(raster.getMinX(), y + raster.getMinY(), raster.getWidth(), 1, row);
						int rowMinX = raster.getWidth() * 4;
						int rowMaxX = -1;
						for (int x = 0; x < row.length; x += 4) {
							int val = row[x] << 24 | row[x + 1] << 16 | row[x + 2] << 8 | row[x + 3];
							if (val != 0xffffff00) {
								if (rowMaxX == -1) rowMinX = x;
								if (x > rowMaxX) rowMaxX = x;
								if (maxY == -1) minY = y;
								if (y > maxY) maxY = y;
							}
						}
						if (rowMinX < minX) minX = rowMinX;
						if (rowMaxX > maxX) maxX = rowMaxX;
					}
					m.trimmedWidth = (maxX - minX) / 4 + 1;
					m.trimmedHeight = maxY - minY + 1;
					m.xOffset = minX / 4;
					m.yOffset = minY;

					BufferedImage dest = new BufferedImage(m.trimmedWidth, m.trimmedHeight, img.getType());
					Graphics g = dest.getGraphics();
					g.drawImage(img, 0, 0, m.trimmedWidth, m.trimmedHeight, m.xOffset, m.yOffset, m.xOffset + m.trimmedWidth, m.yOffset + m.trimmedHeight, null);
					g.dispose();
					m.image = dest;
					m.imagePane.setImage(dest);
				}
			}
			System.gc();
			updateGrid();
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

	static class POI {
		String id;
		String text;
		double relX;
		double relY;
	}

	class POIsModel extends AbstractTableModel {
		private List<POI> pois = new ArrayList<>();

		void add(POI poi) {
			pois.add(poi);
			fireTableRowsInserted(pois.size() - 1, pois.size() - 1);
		}

		POI getPOI(int row) {
			if (row < 0 || row >= pois.size()) return null;
			return pois.get(row);
		}

		void delete(int row) {
			if (row >= 0) {
				pois.remove(row);
				fireTableRowsDeleted(row, row);
			}
		}

		void promote(int row) {
			if (row > 0) {
				pois.add(row - 1, pois.remove(row));
				fireTableRowsUpdated(row - 1, row);
			}
		}

		void demote(int row) {
			if (row < pois.size() - 1) {
				pois.add(row + 1, pois.remove(row));
				fireTableRowsUpdated(row, row + 1);
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			POI poi = pois.get(row);
			if (col == 0) {
				poi.id = (String) value;
				if (poi.relX != 0 || poi.relY != 0) grid.repaint();
			}
			if (col == 1) {
				poi.text = (String) value;
			}
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) return "ID";
			if (col == 1) return "Text";
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
			return pois.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) return pois.get(row).id;
			if (col == 1) return pois.get(row).text;
			return null;
		}
	};
}

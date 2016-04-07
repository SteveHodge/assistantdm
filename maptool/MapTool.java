package maptool;

import java.awt.BorderLayout;
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
import java.util.Iterator;

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
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MapTool extends JFrame {
	double scale = 1.0d;
	JLabel zoomLabel;
	JLabel sizeLabel;
	JLabel gridLabel;
	ScalableImagePanel imagePane;
	BufferedImage image;
	File lastDir = null;
	JFileChooser chooser = new JFileChooser();
	JToggleButton grid1;
	JToggleButton grid2;
	Point grid1loc, grid2loc;
	JFormattedTextField cols;
	JFormattedTextField rows;
	ButtonGroup gridGroup;
	GridPanel grid;

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

		image = openImage(new File("D:/Programming/git/assistantdm/media/Maps/Chapter 5/The Halls of Wrath/Background.png"));

		imagePane = new ScalableImagePanel(image);
		imagePane.addMouseListener(mouseListener);

		grid = new GridPanel();

		JLayeredPane p = new JLayeredPane();
		p.setLayout(new MapLayoutManager(imagePane, grid));
		p.add(imagePane, JLayeredPane.DEFAULT_LAYER);
		p.add(grid, new Integer(100));

		JScrollPane scroller = new JScrollPane(p);
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
		URI uri = null;

		if (lastDir != null) chooser.setCurrentDirectory(lastDir);
		chooser.setMultiSelectionEnabled(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (f != null) {
//				lastDir = f;
				uri = f.toURI();
//				uri = mediaURI.relativize(uri);
			}
		} else {
			System.out.println("Cancelled");
			return;
		}

//		uri = mediaURI.resolve(uri.normalize());
		System.out.println(uri);
		File f = new File(uri);
		image = openImage(f);
		imagePane.setImage(image);
		setZoom(1.0d);
		sizeLabel.setText(String.format("Original size: %dx%d\n", image.getWidth(), image.getHeight()));
	}

	private void updateGrid() {
		double w = grid.getGridCellWidth();
		double h = grid.getGridCellHeight();
		if (w != 0 && h != 0) {
			System.out.println("Grid size = " + w + "x" + h);
			double width = imagePane.displayWidth / w;
			double height = imagePane.displayHeight / h;
			gridLabel.setText(String.format("%.2f x %.2f", width, height));
		}
	}

	private JPanel makeControls() {
		JButton openButton = new JButton("Open");
		openButton.addActionListener(e -> openFile());

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
		gridLabel = new JLabel();

		JPanel pane = new JPanel(new GridBagLayout());
		pane.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 6));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		pane.add(openButton, c);
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
		c.gridwidth = 2;
		c.weighty = 1.0d;
		c.fill = GridBagConstraints.BOTH;
		JPanel p = new JPanel();
		pane.add(p, c);
		return pane;
	}

	private void addSeparator(JPanel p, GridBagConstraints c) {
		c.insets = new Insets(10, 3, 10, 3);
		p.add(new JSeparator(), c);
		c.insets = new Insets(0, 0, 0, 0);
	}
}

package tilemapper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * TODO
 * UI for determining tiles that fit. Need to be able to select a variable sized area (perhaps by dragging a template tile)
 * Random map
 *
 * Mini-map
 * Scaling UI
 * Allow map panel to be used as palette as well (selected tile populates top palette, tiles can be used as drag source)
 * Keep recently used tiles in top palette
 * Set management
 * Image caching
 * Enforce correct placement / show incorrect fit while dragging and while warning dialog is up
 * Auto rotate tiles to fit
 */
@SuppressWarnings("serial")
public class TileMapper extends JPanel implements ActionListener, KeyListener {
	static TileMapper tileMapper;

	private JMenuBar menuBar;
	private JToolBar toolBar;

	MapPanel mapPanel;
	TilePalette tilePanel;
	File file = null;
	JFrame frame;
	JSplitPane splitPane;
	ComponentDragger dragger;

	public TileMapper() {
		super(new BorderLayout());

		File tileDir = new File("media/Tiles");
		System.out.println(tileDir.getAbsolutePath());
		TileManager.scanDirectory(tileDir);
		//TileManager.readXMLConfig("tiles.xml");

		frame = new JFrame("TileMapper");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Action newAction = new MapperAction("New", new ImageIcon("tilemapper/Icons/New.png"), "New map", KeyEvent.VK_N) {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapPanel.reset();
			}
		};

		Action openAction = new MapperAction("Open...", new ImageIcon("tilemapper/Icons/Open.png"), "Open map file", KeyEvent.VK_O) {
			@Override
			public void actionPerformed(ActionEvent e) {
				open();
			}
		};

		Action saveAction = new MapperAction("Save", new ImageIcon("tilemapper/Icons/Save.png"), "Save map to file", KeyEvent.VK_S) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (file != null) save(file);
				else saveAs();
			}
		};

		Action saveAsAction = new MapperAction("Save As...", new ImageIcon("tilemapper/Icons/SaveAs.png"), "Save map to new file") {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		};

		Action saveImageAction = new MapperAction("Save Image...", new ImageIcon("tilemapper/Icons/SaveImage.png"), "Save image to file", KeyEvent.VK_I) {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		};

		Action listTilesAction = new MapperAction("List Tiles", new ImageIcon("tilemapper/Icons/ListTiles.png"), "List tiles in map", KeyEvent.VK_L) {

			@Override
			public void actionPerformed(ActionEvent e) {
				mapPanel.listTiles();
			}
		};

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(new JMenuItem(newAction));
		fileMenu.add(new JMenuItem(openAction));
		fileMenu.add(new JMenuItem(saveAction));
		fileMenu.add(new JMenuItem(saveAsAction));
		fileMenu.add(new JMenuItem(saveImageAction));
		fileMenu.add(new JMenuItem(listTilesAction));
		fileMenu.add(new JMenuItem(new AbstractAction("Exit") {@Override
			public void actionPerformed(ActionEvent arg0) {System.exit(0);}}));

		JMenu setMenu = new JMenu("Sets");
		ArrayList<String> sets = new ArrayList<String>(TileManager.getSets());
		Collections.sort(sets);
		for (String s : sets) {
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(s,true);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
					TileManager.setSetVisible(item.getText(), item.getState());
					tilePanel.tilesChanged();
				}
			});
			setMenu.add(item);
		}

		JMenu styleMenu = new JMenu("Styles");
		ArrayList<String> styles = new ArrayList<String>(TileManager.getStyles());
		Collections.sort(styles);
		for (String s : styles) {
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(s,true);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
					TileManager.setStyleVisible(item.getText(), item.getState());
					tilePanel.tilesChanged();
				}
			});
			styleMenu.add(item);
		}

		menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(setMenu);
		menuBar.add(styleMenu);
		frame.setJMenuBar(menuBar);

		toolBar = new JToolBar("Still draggable");
		toolBar.add(new JButton(newAction));
		add(toolBar, BorderLayout.NORTH);

		dragger = new ComponentDragger(frame) {
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if (dragComponent != null && e.getButton() == MouseEvent.BUTTON3 && dragComponent instanceof DraggableTile) {
					((DraggableTile)dragComponent).rotateTile();
				}
			}
		};

		tilePanel = new TilePalette(dragger);
		JScrollPane tileScroll = new JScrollPane(tilePanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tileScroll.getVerticalScrollBar().setUnitIncrement(40);
		mapPanel = new MapPanel(dragger);
		JScrollPane mapScroll = new JScrollPane(mapPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		mapScroll.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JViewport vp = (JViewport)e.getSource();
				Dimension vpSize = vp.getExtentSize();
				Dimension mpSize = mapPanel.getSize();
				if (mpSize.width > vpSize.width) vpSize.width = mpSize.width;
				if (mpSize.height > vpSize.height) vpSize.height = mpSize.height;
				if (vpSize.width > mpSize.width || vpSize.height > mpSize.height) {
					mapPanel.setSize(vpSize);
				}
			}
		});

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tileScroll, mapScroll);
		splitPane.setDividerLocation(460);
		splitPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		setPreferredSize(new Dimension(1800, 1000));
		add(splitPane, BorderLayout.CENTER);

		setFocusable(true);
		addKeyListener(this);
		requestFocusInWindow();

		setOpaque(true); //content panes must be opaque
		frame.setContentPane(this);

		//Display the window.
		frame.setSize(new Dimension(2000, 1100));
		//frame.pack();
		frame.setVisible(true);
		tilePanel.revalidate();	// this is necessary here to ensure that the palette is correctly sized
	}

	// TODO should ask to save modified map first
	// TODO use fixed location for schema file
	public void open() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Map Files", "map"));
		fc.setCurrentDirectory(new File("d:/programming/workspace/tilemapper"));
		//fc.setCurrentDirectory(file);
		fc.setSelectedFile(file);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		file = fc.getSelectedFile();
		frame.setTitle("Tile Mapper - "+file.getName());
		System.out.println("Opening "+file.getAbsolutePath());

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

			Document dom = factory.newDocumentBuilder().parse(file);

			// we have a valid document
			//printNode(dom, "");
			Node mapNode = null;
			NodeList nodes = dom.getChildNodes();
			for (int i=0; i<nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName() == "Map") mapNode = node;
			}
			if (mapNode == null) return;
			mapPanel.parseDOM((Element)mapNode);

		} catch (ParserConfigurationException e) {
			System.out.println("The underlying parser does not support the requested features.");
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			System.out.println("Error occurred obtaining Document Builder Factory.");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save(File f) {
		FileWriter outputStream = null;

		// check if file exists
		if (f.exists()) {
			String filename = f.getName();
			String backName;
			if (filename.contains(".")) {
				backName = filename.substring(0,filename.lastIndexOf('.'));
				backName += "_backup";
				backName += filename.substring(filename.lastIndexOf('.'));
			} else {
				backName = filename + "_backup";
			}
			File back = new File(f.getParent(),backName);
			System.out.println("Writing backup to: "+back.getAbsolutePath());
			if (back.exists()) back.delete();
			File newF = f;
			f.renameTo(back);
			f = newF;
		}

		try {
			outputStream = new FileWriter(f);
			outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			outputStream.write(System.getProperty("line.separator"));
			outputStream.write(System.getProperty("line.separator"));
			outputStream.write(mapPanel.getXML("", "    "));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void saveAs() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Map Files", "map"));
		fc.setCurrentDirectory(new File("d:/programming/workspace/tilemapper"));
//		fc.setCurrentDirectory(file);
		fc.setSelectedFile(file);
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			frame.setTitle("TileMapper - "+file.getName());
			System.out.println("Saving to "+file.getAbsolutePath());
			save(file);
		}
	}

	public void saveImage() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Jpeg Files", "jpg"));
		fc.setCurrentDirectory(new File("d:/programming/workspace/tilemapper"));
		//fc.setSelectedFile(file);
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			RenderedImage img = mapPanel.getImage();
			File file = fc.getSelectedFile();
			System.out.println("Saving image to "+file.getAbsolutePath());
			try {
				ImageIO.write(img, "jpeg", file);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.err.println("ActionEvent from unknown source: "+e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			mapPanel.deleteSelected();
		}
		/*if (e.getKeyCode() == KeyEvent.VK_F) {
				Tile.Edge[] edges = mapPanel.getSelectedTileEdges();
				if (edges[0] != null || edges[1] != null || edges[2] != null || edges[3] != null) {
					tilePanel.setTopPalette(TileManager.getMatchingTiles(edges));
				} else {
					JOptionPane.showMessageDialog(mapPanel, "All tiles can go there",
							"Warning", JOptionPane.WARNING_MESSAGE);
				}

			}*/
		if (e.getKeyCode() == KeyEvent.VK_M) {
			// TODO fix this
/*			DraggableImage drag = dragger.getDraggingImage();
			if (drag != null && drag.getModel() instanceof DraggableTileModel) {
				DraggableTileModel m = (DraggableTileModel)drag.getModel();
				m.mirrorTile();
			}*/
		}
	};

	@Override
	public void keyTyped(KeyEvent e) {
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				tileMapper = new TileMapper();
			}

		});
	}

	abstract class MapperAction extends AbstractAction {
		public MapperAction(String text, ImageIcon icon, String desc) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
		}

		public MapperAction(String text, ImageIcon icon, String desc, int mnemonic) {
			this(text, icon, desc);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(mnemonic, ActionEvent.CTRL_MASK));
		}
	}
}

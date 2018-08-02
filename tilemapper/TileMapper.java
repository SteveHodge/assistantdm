package tilemapper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * TODO
 * BUG - still problem with dragging tiles on the map. Because the selected tile is a separate component, while the drag is still over the tile's original location it doesn't snap to grid.
 * BUG - refresh bug with tile palette when selecting a set after the palette has previous been cleared - doesn't always happen
 * Disappearing tile bug still happens - confirm
 * Smooth dragging when it should be snapping still happens - confirm
 * Save to layout overlay image in ADM save file
 * Should save/load dialogs all default to common last used dir?
 * Have separate default directories for tilemap, image, ADM files?
 * Performance is bad
 * Allow map panel to be used as palette as well (selected tile populates top palette, tiles can be used as drag source)
 * Keep recently used tiles in top palette?
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

	File defaultDir = new File("C:\\Users\\Steve\\Documents");
	File mediaDir = new File("media");

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

		Action saveADMAction = new MapperAction("Save ADM Map...", new ImageIcon("tilemapper/Icons/SaveAs.png"), "Save map to AssistantDM file") {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveADMMap();
			}
		};

		Action saveLayoutImageAction = new MapperAction("Save Layout Image...", new ImageIcon("tilemapper/Icons/SaveImage.png"), "Save layout image to file", KeyEvent.VK_L) {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveLayoutImage();
			}
		};

		Action listTilesAction = new MapperAction("List Tiles", new ImageIcon("tilemapper/Icons/ListTiles.png"), "List tiles in map", KeyEvent.VK_T) {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapPanel.listTiles();
			}
		};

		Action debugAction = new AbstractAction("Debug") {
			@Override
			public void actionPerformed(ActionEvent e) {
				//mapPanel.debug();
				JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(TileMapper.this), "Styles");
//				dialog.add(TileManager.getStylesPanel());
				dialog.add(TileManager.getSetsPanel());
				dialog.pack();
				dialog.setVisible(true);
			}
		};

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(new JMenuItem(newAction));
		fileMenu.add(new JMenuItem(openAction));
		fileMenu.add(new JMenuItem(saveAction));
		fileMenu.add(new JMenuItem(saveAsAction));
		fileMenu.add(new JMenuItem(saveImageAction));
		fileMenu.add(new JMenuItem(saveADMAction));
		fileMenu.add(new JMenuItem(saveLayoutImageAction));
		fileMenu.add(new JMenuItem(listTilesAction));
		fileMenu.add(new JMenuItem(new AbstractAction("Exit") {@Override
			public void actionPerformed(ActionEvent arg0) {System.exit(0);}}));

		JMenu snapMenu = new JMenu("Snap to Grid");
		snapMenu.setMnemonic(KeyEvent.VK_G);
		JRadioButtonMenuItem snap1 = new JRadioButtonMenuItem("1x1", true);
		snap1.addActionListener(e -> {
			mapPanel.setGridSnap(1);
		});
		JRadioButtonMenuItem snap3 = new JRadioButtonMenuItem("3x3");
		snap3.addActionListener(e -> {
			mapPanel.setGridSnap(3);
		});
		JRadioButtonMenuItem snap6 = new JRadioButtonMenuItem("6x6");
		snap6.addActionListener(e -> {
			mapPanel.setGridSnap(6);
		});
		ButtonGroup snapGroup = new ButtonGroup();
		snapGroup.add(snap1);
		snapGroup.add(snap3);
		snapGroup.add(snap6);
		snapMenu.add(snap1);
		snapMenu.add(snap3);
		snapMenu.add(snap6);

		JMenu gridMenu = new JMenu("Zoom");
		snapMenu.setMnemonic(KeyEvent.VK_Z);
		JRadioButtonMenuItem zoom1 = new JRadioButtonMenuItem("16x16", true);
		zoom1.addActionListener(e -> {
			mapPanel.setGridSize(16);
		});
		JRadioButtonMenuItem zoom2 = new JRadioButtonMenuItem("33x33");
		zoom2.addActionListener(e -> {
			mapPanel.setGridSize(33);
		});
		ButtonGroup zoomGroup = new ButtonGroup();
		zoomGroup.add(zoom1);
		zoomGroup.add(zoom2);
		gridMenu.add(zoom1);
		gridMenu.add(zoom2);

		menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(snapMenu);
		menuBar.add(gridMenu);
		frame.setJMenuBar(menuBar);

		toolBar = new JToolBar("Still draggable");
		toolBar.add(new JButton(newAction));
		toolBar.add(new JButton(openAction));
		toolBar.add(new JButton(saveAction));
		toolBar.add(new JButton(saveImageAction));
		toolBar.add(new JButton(debugAction));
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

		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		JPanel p = TileManager.getStylesPanel();
		filterPanel.add(p, c);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0f;
		c.weightx = 1.0f;
		p = TileManager.getSetsPanel();
		JScrollPane setsScroll = new JScrollPane(p, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		filterPanel.add(setsScroll, c);
		add(filterPanel, BorderLayout.WEST);

		tilePanel = new TilePalette(dragger);
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

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tilePanel, mapScroll);
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

	File getSaveFile(String fileTypeDesc, String defaultExt) {
		return getFile(fileTypeDesc, defaultExt, true, null);
	}

	File getSaveFile(String fileTypeDesc, String defaultExt, JComponent extraComp) {
		return getFile(fileTypeDesc, defaultExt, true, extraComp);
	}

	File getOpenFile(String fileTypeDesc, String defaultExt) {
		return getFile(fileTypeDesc, defaultExt, false, null);
	}

	File getFile(String fileTypeDesc, String defaultExt, boolean save, JComponent extraComp) {
		JFileChooser fc = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter(fileTypeDesc, defaultExt);
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(defaultDir);
		fc.setAccessory(extraComp);
//		fc.setCurrentDirectory(file);
		if (defaultExt.equals("tilemap")) {
			fc.setSelectedFile(file);
		}
		int returnVal;
		if (save) {
			returnVal = fc.showSaveDialog(this);
		} else {
			returnVal = fc.showOpenDialog(this);
		}
		if (returnVal != JFileChooser.APPROVE_OPTION) return null;
		File f = fc.getSelectedFile();
		if (fc.getFileFilter() == filter && !f.getName().contains(".")) {
			// add default extension
			String n = f.getName() + "." + defaultExt;
			f = new File(f.getParent(), n);
		}
		defaultDir = f.getParentFile();
		return f;
	}

	// TODO should ask to save modified map first
	// TODO use fixed location for schema file
	public void open() {
		File f = getOpenFile("Map Files", "tilemap");
		if (f == null) return;
		file = f;
		frame.setTitle("Tile Mapper - "+file.getName());
		System.out.println("Opening "+file.getAbsolutePath());

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("tilemapper/Map.xsd");
			factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));

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

		FileOutputStream outputStream = null;
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//			b.append("<Map xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"map.xsd\">");
//			ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/assistantdm/static/CharacterSheetTemplate.xsl\"");
//			doc.appendChild(pi);
			XMLMapExporter processor = new XMLMapExporter(doc);
			mapPanel.executeProcess(processor);
			doc.setXmlStandalone(true);

			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			outputStream = new FileOutputStream(f);
			trans.transform(new DOMSource(doc), new StreamResult(outputStream));
		} catch (Exception ex) {
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
		File f = getSaveFile("Map Files", "tilemap");
		if (f != null) {
			file = f;
			frame.setTitle("TileMapper - "+file.getName());
			System.out.println("Saving to "+file.getAbsolutePath());
			save(file);
		}
	}

	public void saveImage() {
		String[] dpiOptions = { "33 dpi", "86 dpi (DTT)", "200 dpi" };
		double[] dpis = { 33.0d, 25400.0d / 294.0d, 200.0d };	// 2nd element is the resolution of a 0.294 mm dot pitch monitor

		JComboBox<String> dpiCombo = new JComboBox<>(dpiOptions);
		System.out.println("Combo preferred size = " + dpiCombo.getPreferredSize());
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		panel.add(new JLabel("Output DPI:"), c);
		panel.add(dpiCombo, c);

		File f = getSaveFile("Jpeg Files", "jpg", panel);
		if (f == null) return;

		RenderedImage img = mapPanel.getImage(dpis[dpiCombo.getSelectedIndex()]);
		if (img != null) {
			System.out.println("Saving image to " + f.getAbsolutePath());
			try {
				ImageIO.write(img, "jpeg", f);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void saveLayoutImage() {
		File f = getSaveFile("Jpeg Files", "jpg");
		if (f != null) {
			RenderedImage img = mapPanel.getLayoutImage();
			System.out.println("Saving image to "+file.getAbsolutePath());
			try {
				ImageIO.write(img, "png", f);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void saveADMMap() {
		File file = getSaveFile("ADM Map Files", "xml");
		if (file == null) return;
		System.out.println("Saving ADM map file to " + file.getAbsolutePath());
		FileOutputStream outputStream = null;
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			ADMMapExporter processor = new ADMMapExporter(doc, mediaDir);
			mapPanel.executeProcess(processor);
			doc.setXmlStandalone(true);

			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			outputStream = new FileOutputStream(file);
			trans.transform(new DOMSource(doc), new StreamResult(outputStream));
		} catch (Exception ex) {
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

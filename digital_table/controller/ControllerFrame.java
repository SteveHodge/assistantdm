package digital_table.controller;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import camera.CameraModule;
import camera.CameraModuleListener;
import combat.EncounterModule;
import combat.MonsterCombatEntry;
import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Calibrate;
import digital_table.elements.Grid;
import digital_table.elements.Group;
import digital_table.elements.Label;
import digital_table.elements.MapElement;
import digital_table.elements.MapImage;
import digital_table.elements.SpreadTemplate;
import digital_table.elements.Token;
import digital_table.server.MediaManager;
import digital_table.server.MemoryLog;
import gamesystem.Creature;
import javafx.application.Platform;
import monsters.Monster;
import monsters.XMLMonsterParser;
import monsters.XMLOutputMonsterProcessor;
import util.ModuleRegistry;
import util.XMLUtils;

//TODO JavaFX platform stuff should only be called if necessary (once Browser is added)

@SuppressWarnings("serial")
public class ControllerFrame extends JFrame {
	private static ControllerFrame instance = null;	// TODO remove

	private DisplayManager display;
	private MiniMapCanvas miniMapCanvas = new MiniMapCanvas();
	private TokenOverlay overlay = null;
	private RemoteImageDisplay remoteImg = null;
	private CameraModule camera;	// TODO remove
	private JPanel elementPanel = new JPanel();
	private Map<MapElement, OptionsPanel<?>> optionPanels = new HashMap<>();
	private JComboBox<AddElementAction<?>> availableCombo;
	private JTree elementTree;

	private GridOptionsPanel gridPanel;
	private GridCoordinatesOptionsPanel gridCoordsPanel;

	public ControllerFrame() {
		super("DigitalTable Controller");

		instance = this;

		this.camera = ModuleRegistry.getModule(CameraModule.class);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(windowListener);

		overlay = new TokenOverlay();
		if (camera != null) {
			camera.addCameraModuleListener(new CameraModuleListener() {
				@Override
				public void imageCaptured(BufferedImage image, long size, Exception ex, String updateMsg) {
					overlay.updateOverlay(image.getWidth(), image.getHeight());
				}

				@Override
				public void cameraConnected(String name) {
				}

				@Override
				public void cameraDisconnected() {
				}

				@Override
				public void cameraError(int error) {
				}

				@Override
				public void homographyChanged(Exception ex) {
				}
			});

			// setup remote image generation since camera doesn't work
			// TODO should have a option for this
			remoteImg = new RemoteImageDisplay(overlay);

		} else {
			remoteImg = new RemoteImageDisplay(overlay);
//			JFrame overlayFrame = new JFrame("Token Overlay");
//			JPanel overlayPanel = remoteImg.getPanel();
//			overlayPanel.setPreferredSize(new Dimension(20 * remoteImg.rows, 20 * remoteImg.columns));
//			JPanel buttons = new JPanel();
//			JButton update = new JButton("Update server");
//			update.addActionListener((e) -> overlay.updateOverlay(20 * overlay.rows, 20 * overlay.columns));
//			update.addActionListener((e) -> remoteImg.updateOverlay(20 * remoteImg.rows, 20 * remoteImg.columns));
//			buttons.add(update);
//			overlayFrame.add(buttons, BorderLayout.NORTH);
//			overlayFrame.add(overlayPanel);
//			overlayFrame.pack();
//			overlayFrame.setVisible(true);
		}
		display = new DisplayManager();
		display.setLocal(miniMapCanvas);
		display.setOverlay(overlay);
		if (remoteImg != null) {
			display.setRemoteImageDisplay(remoteImg);
		}
		miniMapCanvas.setRemote(display);

		miniMapCanvas.getPanel().addMouseMotionListener(miniMapMouseListener);
		miniMapCanvas.getPanel().addMouseListener(miniMapMouseListener);
		add(miniMapCanvas.getPanel());

		AddElementAction<?>[] availableElements = {
				tokenAction,
				imageElementAction,
				cameraImageAction,
				templateAction,
				lineAction,
				shapeableAction,
				drawingAction,
				personalEmanationAction,
				labelAction,
				darknessAction,
				lightSourceAction,
				wallsAction,
				initiativeAction,
				browserAction,
				groupAction,
				screensAction,
				callibrateAction
		};
		availableCombo = new JComboBox<>(availableElements);

		elementTree = new JTree(miniMapCanvas.getTreeModel());
		elementTree.setRootVisible(false);
		elementTree.setVisibleRowCount(10);
		elementTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		elementTree.addTreeSelectionListener(new TreeSelectionListener() {
			MapElement previous = null;
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (previous != null) {
					previous.setSelected(false);
				}
				MapElement element = getSelectedElement();
				if (element != null) {
					element.setSelected(true);
					previous = element;
					JPanel options = optionPanels.get(element);
					if (options != null) {
						elementPanel.removeAll();
						elementPanel.add(options);
						options.revalidate();
						options.repaint();
						//System.out.println(""+element);
					}
				}
			}
		});

		JButton addButton = new JButton("Add");
		addButton.addActionListener(e -> {
			AddElementAction<?> action = (AddElementAction<?>) availableCombo.getSelectedItem();
			if (action != null) {
				OptionsPanel<?> options = action.addElement(null);
				elementTree.setSelectionPath(miniMapCanvas.getTreePath(options.getElement()));
			}
		});

		JButton addChildButton = new JButton("Add Child");
		addChildButton.addActionListener(e -> {
			AddElementAction<?> action = (AddElementAction<?>) availableCombo.getSelectedItem();
			MapElement parent = getSelectedElement();
			if (action != null && parent != null && parent instanceof Group) {
				OptionsPanel<?> options = action.addElement(parent);
				elementTree.setSelectionPath(miniMapCanvas.getTreePath(options.getElement()));
			}
		});

		JButton groupsButton = new JButton("Groups...");
		groupsButton.addActionListener(e -> new GroupsDialog());

		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> {
			MapElement element = getSelectedElement();
			if (element != null && element != gridPanel.getElement() && element != gridCoordsPanel.getElement()) {
				elementPanel.removeAll();
				elementPanel.revalidate();
				elementPanel.repaint();
				display.removeElement(element);
				optionPanels.remove(element);
			}
		});

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> reset());

		JButton upButton = new JButton("Up");
		upButton.addActionListener(e -> {
			MapElement element = getSelectedElement();
			if (element != null) {
				display.promoteElement(element);
				elementTree.setSelectionPath(miniMapCanvas.getTreePath(element));
			}
		});

		JButton downButton = new JButton("Down");
		downButton.addActionListener(e -> {
			MapElement element = getSelectedElement();
			if (element != null) {
				display.demoteElement(element);
				elementTree.setSelectionPath(miniMapCanvas.getTreePath(element));
			}
		});

		JButton debugButton = new JButton("Debug");
		debugButton.addActionListener(e -> {
			System.out.print("Local memory usage: ");
			printMemoryUsage(miniMapCanvas.getMemoryUsage());
			System.out.print("Remote memory usage: ");
			printMemoryUsage(display.getRemoteMemoryUsage());
		});

		JButton loadButton = new JButton("Load...");
		loadButton.addActionListener(loadListener);

		JButton saveButton = new JButton("Save...");
		saveButton.addActionListener(e -> new SaveDialog());

		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(e -> quit());

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		rightPanel.add(availableCombo, c);
		rightPanel.add(addButton, c);
		rightPanel.add(addChildButton, c);
		rightPanel.add(groupsButton, c);

		c.gridy++;
		rightPanel.add(upButton, c);
		rightPanel.add(downButton, c);
		rightPanel.add(removeButton, c);
		rightPanel.add(resetButton, c);

		c.gridy++;
		c.gridx = 1;
		rightPanel.add(debugButton, c);
		c.gridx = 2;
		rightPanel.add(loadButton, c);
		c.gridx = 3;
		rightPanel.add(saveButton, c);
		//		buttonPanel.add(quitButton);

		c.gridy = GridBagConstraints.RELATIVE;
		c.gridx = 0;
		c.gridwidth = 4;
		rightPanel.add(new JScrollPane(elementTree), c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		rightPanel.add(elementPanel, c);
		elementPanel.setLayout(new BorderLayout());

		add(rightPanel, BorderLayout.EAST);

		pack();

		gridPanel = new GridOptionsPanel(display, miniMapCanvas);
		optionPanels.put(gridPanel.getElement(), gridPanel);
		gridCoordsPanel = new GridCoordinatesOptionsPanel(display);
		optionPanels.put(gridCoordsPanel.getElement(), gridCoordsPanel);

		loadDisplayFile(new File("display.xml"));
		setVisible(true);
		if (remoteImg != null) {
			remoteImg.setOutputEnabled(true);
		}
	}

	private static void printMemoryUsage(MemoryLog log) {
		printMemoryUsage("", log);
	}

	private static void printMemoryUsage(String prefix, MemoryLog log) {
		System.out.printf("%s%s (%08x) %s\n", prefix, log.name, log.id, formatMemoryValue(log.total));
		if (log.components != null && log.components.length > 0) {
			for (int i = 0; i < log.components.length; i++) {
				if (log.components[i] != null) {
					printMemoryUsage(prefix + "  ", log.components[i]);
				} else {
					System.out.println(prefix + "** null component " + i);
				}
			}
		}
	}

	private static String formatMemoryValue(long m) {
		double mem = m / 1024.0;
		String units = "kB";
		if (mem >= 1024) {
			mem /= 1024;
			units = "MB";
		}
		if (mem >= 1024) {
			mem /= 1024;
			units = "GB";
		}
		return String.format("%.2f %s", mem, units);
	}

	private void resetAll() {
		// TODO should traverse the element tree instead of scanning the optionpanels
		Iterator<OptionsPanel<?>> panels = optionPanels.values().iterator();
		while (panels.hasNext()) {
			OptionsPanel<?> panel = panels.next();
			display.removeElement(panel.getElement());
			panels.remove();
		}

		gridPanel = new GridOptionsPanel(display, miniMapCanvas);
		optionPanels.put(gridPanel.getElement(), gridPanel);
		gridCoordsPanel = new GridCoordinatesOptionsPanel(display);
		optionPanels.put(gridCoordsPanel.getElement(), gridCoordsPanel);

		elementPanel.removeAll();
		elementPanel.add(gridPanel);
		gridPanel.revalidate();
		gridPanel.repaint();
		elementTree.setSelectionPath(miniMapCanvas.getTreePath(gridPanel.getElement()));
	}

	private void reset() {
		// TODO should traverse the element tree instead of scanning the optionpanels
		Iterator<OptionsPanel<?>> panels = optionPanels.values().iterator();
		while (panels.hasNext()) {
			OptionsPanel<?> panel = panels.next();
			if (panel != gridPanel && panel != gridCoordsPanel
					&& !(panel instanceof BoundsOptionsPanel)
					&& !(panel instanceof CalibrateOptionsPanel)
					&& !(panel instanceof InitiativeOptionsPanel)
					&& !(panel instanceof CameraOptionsPanel)
					&& !(panel instanceof GroupOptionsPanel)) {
				// will also want to skip tokens that are bound to characters and descendents of such tokens
				display.removeElement(panel.getElement());
				panels.remove();
			}
		}
		// TODO should remove any empty groups
		elementPanel.removeAll();
		elementPanel.add(gridPanel);
		gridPanel.revalidate();
		gridPanel.repaint();
		elementTree.setSelectionPath(miniMapCanvas.getTreePath(gridPanel.getElement()));
	}

	// reset and rebuilds the elements from the supplied XML document
	void reload(Element doc) {
		boolean remoteEnabled = false;
		if (remoteImg != null) {
			remoteEnabled = remoteImg.isOutputEnabled();
			// disable remote image output to avoid unnecessary repaints
			remoteImg.setOutputEnabled(false);
		}
		resetAll();
		parseDOM(doc);
		if (remoteEnabled) {
			remoteImg.setOutputEnabled(true);
		}

	}

	void setRemote(RemoteConnection remote) {
		display.setRemote(remote);

		// we build the dom and then clear all elements and parse the dom
		// TODO would be better if we could simply resend the existing elements
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			doc.appendChild(getElement(doc));
			reload(doc.getDocumentElement());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void loadDisplayFile(File xmlFile) {
		System.out.println("Loading " + xmlFile.getName());
		Document dom = null;
		if (xmlFile.exists()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//InputStream is = p.getClass().getClassLoader().getResourceAsStream("party.xsd");
			//factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
			try {
				dom = factory.newDocumentBuilder().parse(xmlFile);
			} catch (SAXException | IOException | ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		if (dom != null) {
			reload(dom.getDocumentElement());
			System.out.println("Loaded " + xmlFile.getName());
		}
	}

	private MapElement getSelectedElement() {
		// return (MapElement)elementList.getSelectedValue();
		TreePath selected = elementTree.getSelectionPath();
		if (selected != null) {
			return (MapElement) selected.getLastPathComponent();
		}
		return null;
	}

	public void quit() {
		System.out.println("Requesting exit");
		display.requestExit();
		Platform.exit();
		dispose();

		//			ThreadGroup group = Thread.currentThread().getThreadGroup();
		//			Thread[] threads = new Thread[group.activeCount()];
		//			int count = group.enumerate(threads);
		//			System.out.println("ThreadGroup: "+group);
		//			System.out.println("Parent = "+group.getParent());
		//			System.out.println("Active threads = "+count);
		//			for (int i = 0; i < count; i++) {
		//				if (threads[i] == Thread.currentThread()) System.out.print("*");
		//				System.out.println(threads[i].getName()+" ("+threads[i].getId()+")");
		//			}
	}

	public static void addMonster(Creature m, File imageFile) {
		if (instance != null) {
			TokenOptionsPanel panel = instance.tokenAction.addElement(null);
			panel.setCreature(m);
			panel.setImage(imageFile);
			instance.elementTree.setSelectionPath(instance.miniMapCanvas.getTreePath(panel.getElement()));
		}
	}

	public void replaceToken(TokenOptionsPanel options, int x, int y, int width, int height, boolean visible) {
//		int ret = JOptionPane.showConfirmDialog(this, "Replace this token with a corpse image?", "Replace token", JOptionPane.YES_NO_OPTION);
//		if (ret != JOptionPane.YES_OPTION) return;

		URI uri = MediaManager.INSTANCE.showFileChooser(ControllerFrame.this);
		if (uri != null) {
			// XXX maybe should have dedicated group for corpses
			ImageOptionsPanel imagePanel = new ImageOptionsPanel(uri, options.element.parent, display, maskAction);
			MapImage image = imagePanel.getElement();
			display.setProperty(image, Group.PROPERTY_X, (double) x, Mode.ALL);
			display.setProperty(image, Group.PROPERTY_Y, (double) y, Mode.ALL);
			display.setProperty(image, MapImage.PROPERTY_ASPECT_LOCKED, false, Mode.ALL);
			display.setProperty(image, MapImage.PROPERTY_WIDTH, (double) width, Mode.ALL);
			display.setProperty(image, MapImage.PROPERTY_HEIGHT, (double) height, Mode.ALL);
			imagePanel.visibleCheck.setSelected(visible);
			display.setProperty(image, MapImage.PROPERTY_LABEL, options.element.getProperty(Token.PROPERTY_LABEL), Mode.LOCAL);
			image.addPropertyChangeListener(labelListener);
			optionPanels.put(image, imagePanel);

			display.reorganiseBefore(image, options.getElement());

			display.removeElement(options.getElement());
			optionPanels.remove(options.getElement());

			elementTree.setSelectionPath(miniMapCanvas.getTreePath(image));
		}
	}

// this MapElementMouseListener changes the canvas offset when it detects mouse dragging
	private MapElementMouseListener gridMouseListener = new MapElementMouseListener() {
		private boolean dragging = false;
		private int button;
		private Point offset;
		private int startOffsetX, startOffsetY;	// original offsets for remote
		private int localOffsetX, localOffsetY;	// original offsets for local

		private void setOffset(Point p) {
			int x = (offset.x - p.x) / miniMapCanvas.getColumnWidth();
			int y = (offset.y - p.y) / miniMapCanvas.getRowHeight();
			if (button == MouseEvent.BUTTON3)
				gridPanel.setRemoteOffset(startOffsetX + x, startOffsetY + y);
			miniMapCanvas.setOffset(localOffsetX + x, localOffsetY + y);
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {
			button = e.getButton();
			offset = e.getPoint();
			startOffsetX = display.getXOffset();
			startOffsetY = display.getYOffset();
			localOffsetX = miniMapCanvas.getXOffset();
			localOffsetY = miniMapCanvas.getYOffset();
		}

		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {
			if (dragging) {
				setOffset(e.getPoint());
				dragging = false;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {
			if (button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3) {
				dragging = true;
			}
			if (dragging) {
				setOffset(e.getPoint());
			}
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
		}

		@Override
		public MapElement getCoordElement() {
			return null;
		}
	};

	private MouseInputListener miniMapMouseListener = new MouseInputListener() {
		protected MapElementMouseListener getElementMouseListener() {
			MapElement element = getSelectedElement();
			if (element != null) {
				OptionsPanel<?> options = optionPanels.get(element);
				if (options != null) {
					if (options == gridPanel) return gridMouseListener;
					return options.getMouseListener();
				}
			}
			return null;
		}

		// returns the grid location of the click. if the listener has a non-null
		// getCoordElement() then the resulting grid coordinates are relative to the
		// element returned by getCoordElement(). otherwise the returned grid coordinates
		// are relative to the canvas (i.e. adjusted by the canvas offset)
		protected Point2D getLocation(MouseEvent e, MapElementMouseListener listener) {
			Point2D click;
			if (listener != null && listener.getCoordElement() != null) {
				click = miniMapCanvas.convertDisplayCoordsToGrid(e.getX(), e.getY());
				Point2D origin = miniMapCanvas.getElementOrigin(listener.getCoordElement());
				click.setLocation(click.getX() - origin.getX(), click.getY() - origin.getY());
			} else {
				click = miniMapCanvas.convertDisplayCoordsToCanvas(e.getX(), e.getY());
			}
			return click;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			MapElementMouseListener l = getElementMouseListener();
			if (l != null) {
				l.mousePressed(e, getLocation(e, l));
				return;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			MapElementMouseListener l = getElementMouseListener();
			if (l != null) {
				l.mouseReleased(e, getLocation(e, l));
				return;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			MapElementMouseListener l = getElementMouseListener();
			if (l != null) {
				l.mouseDragged(e, getLocation(e, l));
				return;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			MapElementMouseListener l = getElementMouseListener();
			if (l != null) {
				l.mouseClicked(e, getLocation(e, l));
				return;
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	};

	private WindowListener windowListener = new WindowListener() {
		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			System.out.println("ControllerFrame windowClosing");
			quit();
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowClosed(WindowEvent e) {
			System.out.println("ControllerFrame windowClosed");
		}
	};

	private PropertyChangeListener labelListener = e -> {
		if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_LABEL)
				|| (e.getSource() instanceof Label && e.getPropertyName().equals(Label.PROPERTY_TEXT))) {
			MapElement element = (MapElement) e.getSource();
			miniMapCanvas.updateTreeNode(element);
			//elementTree.repaint();
		}
	};

	private class GroupsDialog extends JDialog {
		// TODO apply dialog should only be enabled when selections have been made in both trees
		private JTree elements;
		private JTree groups;

		GroupsDialog() {
			super(ControllerFrame.this, "Rearrange hierarchy", true);

			elements = new JTree(miniMapCanvas.getTreeModel());
			elements.setRootVisible(false);
			elements.setVisibleRowCount(10);
			elements.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

			groups = new JTree(groupsTreeModel);
			groups.setVisibleRowCount(10);
			groups.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			JButton okButton = new JButton("Ok");
			okButton.addActionListener(e -> {
				apply();
				dispose();
			});

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(e -> dispose());

			JButton applyButton = new JButton("Apply");
			applyButton.addActionListener(e -> apply());

			JPanel trees = new JPanel();
			trees.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = 0;
			c.weightx = 1.0d;
			c.fill = GridBagConstraints.HORIZONTAL;
			trees.add(new JLabel("Elements to move"), c);
			trees.add(new JLabel("Group to move to"), c);

			c.gridy = 1;
			c.weighty = 1.0d;
			c.fill = GridBagConstraints.BOTH;
			JScrollPane scroller = new JScrollPane(elements);
			trees.add(scroller, c);
			scroller = new JScrollPane(groups);
			trees.add(scroller, c);

			JPanel buttons = new JPanel();
			buttons.add(okButton);
			buttons.add(cancelButton);
			buttons.add(applyButton);

			add(trees);
			add(buttons, BorderLayout.SOUTH);

			pack();
			setVisible(true);
		}

		void apply() {
			TreePath targetPath = groups.getSelectionPath();
			TreePath[] elementPaths = elements.getSelectionPaths();
			if (targetPath == null || elementPaths.length == 0) return;

			MapElement newParent = null;
			if (targetPath.getLastPathComponent() == addNode) {
				GroupOptionsPanel panel = groupAction.addElement(null);
				newParent = panel.getElement();
			} else if (targetPath.getLastPathComponent() != rootNode){
				newParent = (MapElement)targetPath.getLastPathComponent();
			}

			for (TreePath elementPath : elementPaths) {
				MapElement element = (MapElement) elementPath.getLastPathComponent();
				if (element != newParent) display.changeParent(element, newParent);
			}

			// TODO reset selections - currently unnecessary because the change fires a structure changed event which rebuilds the trees
		}

		private String rootNode = new String("Top level");
		private String addNode = new String("<new group>");

		TreeModel groupsTreeModel = new TreeModel() {
			private EventListenerList listeners = new EventListenerList();
			private TreeModel m = miniMapCanvas.getTreeModel();

			{
				m.addTreeModelListener(new TreeModelListener() {
					// TODO converting all events to structure change events on the whole tree makes changes inefficient
					@Override
					public void treeNodesChanged(TreeModelEvent e) {
						fireTreeStructureChanged();
					}

					@Override
					public void treeNodesInserted(TreeModelEvent e) {
						fireTreeStructureChanged();
					}

					@Override
					public void treeNodesRemoved(TreeModelEvent e) {
						fireTreeStructureChanged();
					}

					@Override
					public void treeStructureChanged(TreeModelEvent e) {
						fireTreeStructureChanged();
					}

					void fireTreeStructureChanged() {
						Object[] list = listeners.getListenerList();
						for (int i = list.length - 2; i >= 0; i -= 2) {
							if (list[i] == TreeModelListener.class) {
								TreePath path = new TreePath(rootNode);
								TreeModelEvent e = new TreeModelEvent(this, path);
								((TreeModelListener) list[i + 1]).treeStructureChanged(e);
							}
						}
					}
				});
			}

			@Override
			public Object getRoot() {
				return rootNode;
			}

			@Override
			public Object getChild(Object parent, int index) {
				if (parent == addNode) return null;
				if (parent == rootNode) {
					if (index == 0) return addNode;
					parent = m.getRoot();
					index--;
				}
				for (int i = 0; i < m.getChildCount(parent); i++) {
					Object e = m.getChild(parent, i);
					if (e instanceof Group && --index < 0) return e;
				}
				return null;
			}

			@Override
			public int getChildCount(Object parent) {
				if (parent == addNode) return 0;
				int children = 0;
				if (parent == rootNode) {
					parent = m.getRoot();
					children++;
				}
				for (int i = 0; i < m.getChildCount(parent); i++) {
					Object e = m.getChild(parent, i);
					if (e instanceof Group) children++;
				}
				return children;
			}

			@Override
			public int getIndexOfChild(Object parent, Object child) {
				if (parent == addNode) return -1;
				int childIndex = 0;
				if (parent == rootNode) {
					if (child == addNode) return 0;
					childIndex++;
					parent = m.getRoot();
				}
				for (int i = 0; i < m.getChildCount(parent); i++) {
					Object e = m.getChild(parent, i);
					if (e instanceof Group) {
						if (e == child) return childIndex;
						childIndex++;
					}
				}
				return -1;
			}

			@Override
			public boolean isLeaf(Object node) {
				return getChildCount(node) == 0;
			}

			@Override
			public void valueForPathChanged(TreePath path, Object newValue) {
			}

			@Override
			public void addTreeModelListener(TreeModelListener e) {
				listeners.add(TreeModelListener.class, e);
			}

			@Override
			public void removeTreeModelListener(TreeModelListener e) {
				listeners.remove(TreeModelListener.class, e);
			}
		};
	}

	private ActionListener loadListener = new ActionListener() {
		// TODO should load effects as well
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
			fc.setCurrentDirectory(new File("."));
			int returnVal = fc.showOpenDialog(ControllerFrame.this);
			if (returnVal != JFileChooser.APPROVE_OPTION) return;

			File file = fc.getSelectedFile();
			System.out.println("Opening encounter " + file.getAbsolutePath());

			Document dom = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//InputStream is = p.getClass().getClassLoader().getResourceAsStream("party.xsd");
			//factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
			try {
				dom = factory.newDocumentBuilder().parse(file);
			} catch (SAXException | IOException | ParserConfigurationException ex) {
				ex.printStackTrace();
			}

			if (dom != null) {
				Map<Integer, Creature> idMap = new HashMap<>();	// map of ids in this file plus characters we've already loaded
				EncounterModule enc = ModuleRegistry.getModule(EncounterModule.class);
				if (enc != null) {
					idMap = enc.getCharacterIDMap();
				}

				Element displayEl = null;
				NodeList nodes = dom.getDocumentElement().getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node.getNodeName().equals("Elements")) {
						displayEl = (Element) node;
					} else if (node.getNodeName().equals("Creatures") && enc != null) {
						NodeList children = node.getChildNodes();
						for (int j = 0; j < children.getLength(); j++) {
							if (children.item(j).getNodeName().equals("Monster")) {
								XMLMonsterParser parser = new XMLMonsterParser();
								Monster m = parser.parseDOM((Element) children.item(j));
								String idStr = ((Element) children.item(j)).getAttribute("id");
								idMap.put(Integer.parseInt(idStr), m);
								MonsterCombatEntry mce = new MonsterCombatEntry(m);
								enc.getInitiativeListModel().addEntry(mce);
							}
						}
					}
				}

				if (displayEl != null) parseDOM(displayEl, idMap);
				else
					System.out.println("No elements found in encounter");
			}
		}
	};

	private class SaveDialog extends JDialog {
		private JList<MapElement> elements;
		private JCheckBox saveCreatures;
		private JCheckBox removeAfter;

		SaveDialog() {
			super(ControllerFrame.this, "Save...", true);

			Vector<MapElement> list = new Vector<>();
			ListModel<MapElement> m = miniMapCanvas.getModel();
			for (int i = 0; i < m.getSize(); i++) {
				MapElement e = m.getElementAt(i);
				if (e.getParent() == null && !(e instanceof Grid)) list.add(e);
			}

			elements = new JList<>(list);
			elements.setVisibleRowCount(10);
			elements.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			saveCreatures = new JCheckBox("Save monsters with tokens");
			saveCreatures.setSelected(true);
			removeAfter = new JCheckBox("Remove saved elements");

			JButton okButton = new JButton("Ok");
			okButton.addActionListener(arg0 -> {
				dispose();
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				chooser.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
				chooser.setSelectedFile(new File("Encounter.xml"));
				if (chooser.showSaveDialog(ControllerFrame.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (f != null) {
						save(f);
					}
				}
			});

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(arg0 -> dispose());

			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.weightx = 1.0d;
			c.fill = GridBagConstraints.HORIZONTAL;
			content.add(new JLabel("Elements to save"), c);

			c.weighty = 1.0d;
			c.fill = GridBagConstraints.BOTH;
			JScrollPane scroller = new JScrollPane(elements);
			content.add(scroller, c);

			c.weighty = 0.0d;
			c.fill = GridBagConstraints.HORIZONTAL;
			content.add(saveCreatures, c);
			content.add(removeAfter, c);

			JPanel buttons = new JPanel();
			buttons.add(okButton);
			buttons.add(cancelButton);

			add(content);
			add(buttons, BorderLayout.SOUTH);

			pack();
			setVisible(true);
		}

		private void save(File f) {
			System.out.println("Save to " + f);

			// TODO if f exists then confirm overwrite or confirm add/replace display config if it's an encounter file

			boolean saved = false;

			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element root = doc.createElement("Encounter");
				doc.appendChild(root);

				Element el = doc.createElement("Elements");
				List<MapElement> selected = elements.getSelectedValuesList();
				for (MapElement element : selected) {
					if (!(element instanceof Grid)) {
						addElement(doc, el, element);
					}
				}
				root.appendChild(el);

				if (saveCreatures.isSelected()) {
					el = null;

					// TODO will need to save any effects in play. should we save the combat entry if any as well?
					for (MapElement element : selected) {
						if (element instanceof Token) {
							Token t = (Token) element;
							TokenOptionsPanel p = (TokenOptionsPanel) optionPanels.get(t);
							if (p != null && p.getCreature() != null) {
								if (el == null) el = doc.createElement("Creatures");
								if (p.getCreature() instanceof Monster) {
									XMLOutputMonsterProcessor processor = new XMLOutputMonsterProcessor(doc);
									p.getCreature().executeProcess(processor);
									el.appendChild(processor.getElement());
								}
							}
						}
					}
				}
				if (el != null) root.appendChild(el);

				XMLUtils.writeDOM(doc, f);
				saved = true;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			if (saved && removeAfter.isSelected()) {
				List<MapElement> selected = elements.getSelectedValuesList();
				for (MapElement element : selected) {
					if (!(element instanceof Grid)) {
						display.removeElement(element);
						optionPanels.remove(element);
					}
				}
				elementPanel.removeAll();
				elementPanel.revalidate();
				elementPanel.repaint();
			}
		}

		private void addElement(Document doc, Element docParent, MapElement mapElement) {
			OptionsPanel<?> p = optionPanels.get(mapElement);
			Element e = p.getElement(doc);
			if (e != null) {
				docParent.appendChild(e);
				addChildren(doc, e, mapElement);
			}
		}
	}

	private abstract class AddElementAction<P extends OptionsPanel<?>> extends AbstractAction implements ElementFactory<P> {
		String name;

		protected AddElementAction(String name) {
			this.name = name;
			putValue(NAME, "Add " + name);
		}

		abstract protected P createOptionsPanel(MapElement parent);

		@Override
		public void actionPerformed(ActionEvent arg0) {
			addElement(null);
		}

		@Override
		public P addElement(MapElement parent) {
			P panel = createOptionsPanel(parent);
			if (panel != null) {
				MapElement element = panel.getElement();
				element.addPropertyChangeListener(labelListener);
				optionPanels.put(element, panel);
			}
			return panel;
		}

		@Override
		public void removeElement(P panel) {
			// TODO if this element is selected then need to clear the option panel (see remove button action)
			display.removeElement(panel.getElement());
			optionPanels.remove(panel.getElement());
		}

		@Override
		public String toString() {
			return name;
		}
	};

	private AddElementAction<ImageOptionsPanel> imageElementAction = new AddElementAction<ImageOptionsPanel>("Image/Map") {
		@Override
		protected ImageOptionsPanel createOptionsPanel(MapElement parent) {
			URI uri = MediaManager.INSTANCE.showFileChooser(ControllerFrame.this);
			if (uri != null) {
				return new ImageOptionsPanel(uri, parent, display, maskAction);
			}
			return null;
		}
	};

	private AddElementAction<MaskOptionsPanel> maskAction = new AddElementAction<MaskOptionsPanel>("Image Mask") {
		@Override
		protected MaskOptionsPanel createOptionsPanel(MapElement parent) {
			return new MaskOptionsPanel(parent, display);
		}
	};

	private AddElementAction<SpreadTemplateOptionsPanel> templateAction = new AddElementAction<SpreadTemplateOptionsPanel>("Template") {
		@Override
		protected SpreadTemplateOptionsPanel createOptionsPanel(MapElement parent) {
			return new SpreadTemplateOptionsPanel(parent, display);
		}
	};

	private AddElementAction<LineTemplateOptionsPanel> lineAction = new AddElementAction<LineTemplateOptionsPanel>("Line") {
		@Override
		protected LineTemplateOptionsPanel createOptionsPanel(MapElement parent) {
			return new LineTemplateOptionsPanel(parent, display);
		}
	};

	private AddElementAction<ShapeableTemplateOptionsPanel> shapeableAction = new AddElementAction<ShapeableTemplateOptionsPanel>("Shapeable") {
		@Override
		protected ShapeableTemplateOptionsPanel createOptionsPanel(MapElement parent) {
			return new ShapeableTemplateOptionsPanel(parent, display);
		}
	};

	private AddElementAction<DrawingOptionsPanel> drawingAction = new AddElementAction<DrawingOptionsPanel>("Drawing") {
		@Override
		protected DrawingOptionsPanel createOptionsPanel(MapElement parent) {
			return new DrawingOptionsPanel(parent, display);
		}
	};

	private AddElementAction<DarknessMaskOptionsPanel> darknessAction = new AddElementAction<DarknessMaskOptionsPanel>("Darkness") {
		@Override
		protected DarknessMaskOptionsPanel createOptionsPanel(MapElement parent) {
			return new DarknessMaskOptionsPanel(parent, display);
		}
	};

	private AddElementAction<WallsOptionsPanel> wallsAction = new AddElementAction<WallsOptionsPanel>("Walls") {
		@Override
		protected WallsOptionsPanel createOptionsPanel(MapElement parent) {
			return new WallsOptionsPanel(parent, display);
		}
	};

	private AddElementAction<BrowserOptionsPanel> browserAction = new AddElementAction<BrowserOptionsPanel>("Browser") {
		@Override
		protected BrowserOptionsPanel createOptionsPanel(MapElement parent) {
			return new BrowserOptionsPanel(parent, display);
		}
	};

	private AddElementAction<InitiativeOptionsPanel> initiativeAction = new AddElementAction<InitiativeOptionsPanel>("Initiative") {
		@Override
		protected InitiativeOptionsPanel createOptionsPanel(MapElement parent) {
			return new InitiativeOptionsPanel(parent, display);
		}
	};

	private AddElementAction<LabelOptionsPanel> labelAction = new AddElementAction<LabelOptionsPanel>("Label") {
		@Override
		protected LabelOptionsPanel createOptionsPanel(MapElement parent) {
			return new LabelOptionsPanel(parent, display);
		}
	};

	private AddElementAction<BoundsOptionsPanel> screensAction = new AddElementAction<BoundsOptionsPanel>("Screens") {
		@Override
		protected BoundsOptionsPanel createOptionsPanel(MapElement parent) {
			return new BoundsOptionsPanel(parent, display);
		}
	};

	private AddElementAction<TokenOptionsPanel> tokenAction = new AddElementAction<TokenOptionsPanel>("Token") {
		@Override
		protected TokenOptionsPanel createOptionsPanel(MapElement parent) {
			return new TokenOptionsPanel(parent, display, labelAction, ControllerFrame.this);
		}
	};

	private AddElementAction<GroupOptionsPanel> groupAction = new AddElementAction<GroupOptionsPanel>("Group") {
		@Override
		protected GroupOptionsPanel createOptionsPanel(MapElement parent) {
			return new GroupOptionsPanel(parent, display);
		}
	};

	private AddElementAction<LightSourceOptionsPanel> lightSourceAction = new AddElementAction<LightSourceOptionsPanel>("Light Source") {
		@Override
		protected LightSourceOptionsPanel createOptionsPanel(MapElement parent) {
			return new LightSourceOptionsPanel(parent, display);
		}
	};

	private AddElementAction<PersonalEmanationOptionsPanel> personalEmanationAction = new AddElementAction<PersonalEmanationOptionsPanel>("Personal Emanation") {
		@Override
		protected PersonalEmanationOptionsPanel createOptionsPanel(MapElement parent) {
			return new PersonalEmanationOptionsPanel(parent, display);
		}
	};

	private AddElementAction<CalibrateOptionsPanel> callibrateAction = new AddElementAction<CalibrateOptionsPanel>("Callibrate") {
		@Override
		protected CalibrateOptionsPanel createOptionsPanel(MapElement parent) {
			return new CalibrateOptionsPanel(parent, display);
		}
	};

	private AddElementAction<CameraOptionsPanel> cameraImageAction = new AddElementAction<CameraOptionsPanel>("Camera Image") {
		@Override
		protected CameraOptionsPanel createOptionsPanel(MapElement parent) {
			return new CameraOptionsPanel(parent, display, camera);
		}
	};

	private void addChildren(Document doc, Element docParent, Object treeParent) {
		TreeModel tree = miniMapCanvas.getTreeModel();
		for (int i = 0; i < tree.getChildCount(treeParent); i++) {
			Object mapElement = tree.getChild(treeParent, i);
			OptionsPanel<?> p = optionPanels.get(mapElement);
			Element e = p.getElement(doc);
			if (e != null) {
				docParent.appendChild(e);
				addChildren(doc, e, mapElement);
			}
		}
	}

	public Node getElement(Document doc) {
		Element root = doc.createElement("Elements");
		TreeModel tree = miniMapCanvas.getTreeModel();
		addChildren(doc, root, tree.getRoot());
		return root;
	}

	public void parseNode(Element e, OptionsPanel<?> parentPanel, Map<Integer, Creature> idMap) {
		String tag = e.getTagName();
		OptionsPanel<?> p = null;
		MapElement parent = parentPanel == null ? null : parentPanel.getElement();

		if (tag.equals(GridOptionsPanel.XML_TAG)) {
			p = gridPanel;
		} else if (tag.equals(GridCoordinatesOptionsPanel.XML_TAG)) {
			p = gridCoordsPanel;
		} else if (tag.equals(SpreadTemplateOptionsPanel.XML_TAG)) {
			p = templateAction.addElement(parent);
		} else if (tag.equals(LineTemplateOptionsPanel.XML_TAG)) {
			p = lineAction.addElement(parent);
		} else if (tag.equals(PersonalEmanationOptionsPanel.XML_TAG)) {
			p = personalEmanationAction.addElement(parent);
		} else if (tag.equals(LabelOptionsPanel.XML_TAG)) {
			p = labelAction.addElement(parent);
		} else if (tag.equals(DarknessMaskOptionsPanel.XML_TAG)) {
			p = darknessAction.addElement(parent);
		} else if (tag.equals(WallsOptionsPanel.XML_TAG)) {
			p = wallsAction.addElement(parent);
		} else if (tag.equals(LightSourceOptionsPanel.XML_TAG)) {
			p = lightSourceAction.addElement(parent);
		} else if (tag.equals(InitiativeOptionsPanel.XML_TAG)) {
			p = initiativeAction.addElement(parent);
		} else if (tag.equals(GroupOptionsPanel.XML_TAG)) {
			p = groupAction.addElement(parent);
		} else if (tag.equals(BoundsOptionsPanel.XML_TAG)) {
			p = screensAction.addElement(parent);
		} else if (tag.equals(CalibrateOptionsPanel.XML_TAG)) {
			p = callibrateAction.addElement(parent);
		} else if (tag.equals(TokenOptionsPanel.XML_TAG)) {
			p = tokenAction.addElement(parent);
			((TokenOptionsPanel) p).setIDMap(idMap);
		} else if (tag.equals(ShapeableTemplateOptionsPanel.XML_TAG)) {
			p = shapeableAction.addElement(parent);
		} else if (tag.equals(DrawingOptionsPanel.XML_TAG)) {
			p = drawingAction.addElement(parent);
		} else if (tag.equals(BrowserOptionsPanel.XML_TAG)) {
			p = browserAction.addElement(parent);
		} else if (tag.equals(MaskOptionsPanel.XML_TAG)) {
			p = maskAction.addElement(parent);
		} else if (tag.equals(ImageOptionsPanel.XML_TAG)) {
			if (e.hasAttribute(ImageOptionsPanel.FILE_ATTRIBUTE_NAME)) {
				URI uri;
				try {
					uri = new URI(e.getAttribute(ImageOptionsPanel.FILE_ATTRIBUTE_NAME));
					p = new ImageOptionsPanel(uri, parent, display, maskAction);
					MapElement element = p.getElement();
					element.addPropertyChangeListener(labelListener);
					optionPanels.put(element, p);
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		} else if (!tag.equals(MaskOptionsPanel.MASK_TAG)) {
			System.err.println("Unrecognised element tag: " + tag);
		}

		if (p != null) p.parseDOM(e, parentPanel);

		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			parseNode((Element) nodes.item(i), p, idMap);
		}
	}

	public void parseDOM(Element el) {
		if (!el.getTagName().equals("Elements")) return;

		boolean remoteEnabled = false;
		if (remoteImg != null) {
			remoteEnabled = remoteImg.isOutputEnabled();
			// disable remote image output to avoid unnecessary repaints
			remoteImg.setOutputEnabled(false);
		}

		Map<Integer, Creature> idMap = new HashMap<>();
		EncounterModule enc = ModuleRegistry.getModule(EncounterModule.class);
		if (enc != null) {
			idMap = enc.getIDMap();
		}
		parseNode(el, null, idMap);
		elementTree.setSelectionPath(miniMapCanvas.getTreePath(gridPanel.getElement()));

		if (remoteEnabled) {
			remoteImg.setOutputEnabled(true);
		}
	}

	public void parseDOM(Element el, Map<Integer, Creature> idMap) {
		if (!el.getTagName().equals("Elements")) return;

		boolean remoteEnabled = false;
		if (remoteImg != null) {
			remoteEnabled = remoteImg.isOutputEnabled();
			// disable remote image output to avoid unnecessary repaints
			remoteImg.setOutputEnabled(false);
		}

		parseNode(el, null, idMap);
		elementTree.setSelectionPath(miniMapCanvas.getTreePath(gridPanel.getElement()));

		if (remoteEnabled) {
			remoteImg.setOutputEnabled(true);
		}
	}

	private static Calibrate calibrateElement;

	public void setCalibrateDisplay(boolean show) {
		if (show && calibrateElement == null) {
			calibrateElement = new Calibrate();
			calibrateElement.setProperty(Calibrate.PROPERTY_VISIBLE, MapElement.Visibility.VISIBLE);
			display.addElement(calibrateElement, null);
		} else if (!show) {
			display.removeElement(calibrateElement);
			calibrateElement = null;
		}
	}
}

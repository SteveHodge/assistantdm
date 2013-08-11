package digital_table.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.Monster;
import camera.CameraPanel;
import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.SpreadTemplate;
import digital_table.server.TableDisplay;

//TODO JavaFX platform stuff should only be called if necessary (once Browser is added)

@SuppressWarnings("serial")
public class ControllerFrame extends JFrame {
	private static ControllerFrame instance = null;	// TODO remove

	private DisplayManager display;
	private MiniMapPanel miniMapPanel = new MiniMapPanel();
	private TokenOverlay overlay = null;
	private CameraPanel camera;
	private JPanel elementPanel = new JPanel();
	private Map<MapElement, OptionsPanel> optionPanels = new HashMap<MapElement, OptionsPanel>();
	private JList availableList;
	//private JList elementList;
	private JTree elementTree;
	//private DefaultListModel elements;
	private GridOptionsPanel gridPanel;

	private File last_file = null;

	public ControllerFrame(TableDisplay remote, CameraPanel camera) {
		super("DigitalTable Controller");

		instance = this;

		this.camera = camera;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(windowListener);

		if (camera != null) {
			overlay = new TokenOverlay();
			camera.setOverlayGenerator(this);
		}
		display = new DisplayManager(remote, miniMapPanel, overlay);

		miniMapPanel.addMouseMotionListener(miniMapMouseListener);
		miniMapPanel.addMouseListener(miniMapMouseListener);
		//elements = (DefaultListModel) miniMapPanel.getModel();
		add(miniMapPanel);

		AddElementAction<?>[] availableElements = {
				tokenAction,
				imageElementAction,
				cameraImageAction,
				templateAction,
				lineAction,
				shapeableAction,
				personalEmanationAction,
				labelAction,
				darknessAction,
				lightSourceAction,
				initiativeAction,
				browserAction,
				groupAction,
				screensAction,
				callibrateAction
		};
		availableList = new JList(availableElements);
		availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//		elementList = new JList(elements);
		//		elementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//		elementList.addListSelectionListener(new ListSelectionListener() {
		//			@Override
		//			public void valueChanged(ListSelectionEvent e) {
		//				if (!e.getValueIsAdjusting()) {
		//					MapElement element = (MapElement) elementList.getSelectedValue();
		//					if (element != null) {
		//						JPanel options = optionPanels.get(element);
		//						if (options != null) {
		//							elementPanel.removeAll();
		//							elementPanel.add(options);
		//							options.revalidate();
		//							options.repaint();
		//							//System.out.println(""+element);
		//						}
		//					}
		//				}
		//			}
		//		});
		elementTree = new JTree(miniMapPanel.getTreeModel());
		elementTree.setRootVisible(false);
		elementTree.setVisibleRowCount(10);
		elementTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		elementTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				MapElement element = getSelectedElement();
				if (element != null) {
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
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AddElementAction<?> action = (AddElementAction<?>) availableList.getSelectedValue();
				if (action != null) action.actionPerformed(arg0);
			}
		});

		JButton addChildButton = new JButton("Add Child");
		addChildButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AddElementAction<?> action = (AddElementAction<?>) availableList.getSelectedValue();
				MapElement parent = getSelectedElement();
				if (action != null && parent != null && parent instanceof Group) {
					action.addElement(parent);
				}
			}
		});

		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				MapElement element = getSelectedElement();
				if (element != null && element != gridPanel.getElement()) {
					elementPanel.removeAll();
					elementPanel.revalidate();
					display.removeElement(element);
					optionPanels.remove(element);
				}
			}
		});

		JButton upButton = new JButton("Up");
		upButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MapElement element = getSelectedElement();
				if (element != null) {
					display.promoteElement(element);
					elementTree.setSelectionPath(miniMapPanel.getTreePath(element));
				}
			}
		});

		JButton downButton = new JButton("Down");
		downButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MapElement element = getSelectedElement();
				if (element != null) {
					display.demoteElement(element);
					elementTree.setSelectionPath(miniMapPanel.getTreePath(element));
				}
			}
		});

		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 4));
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(upButton);
		buttonPanel.add(downButton);
		buttonPanel.add(addChildButton);
		//		buttonPanel.add(quitButton);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		rightPanel.add(Box.createHorizontalStrut(420));
		rightPanel.add(new JScrollPane(availableList));
		rightPanel.add(buttonPanel);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		rightPanel.add(new JScrollPane(elementTree));
		rightPanel.add(elementPanel);
		elementPanel.setLayout(new BorderLayout());
		rightPanel.add(Box.createVerticalGlue());

		add(rightPanel, BorderLayout.EAST);

		pack();

		gridPanel = new GridOptionsPanel(display);
		optionPanels.put(gridPanel.getElement(), gridPanel);

		setVisible(true);
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

	public static void addMonster(Monster m, File imageFile) {
		if (instance != null) {
			TokenOptionsPanel panel = instance.tokenAction.addElement(null);
			panel.setCreature(m);
			panel.setImage(imageFile);
		}
	}

	private MouseInputListener miniMapMouseListener = new MouseInputListener() {
		protected MapElementMouseListener getOptionsPanel() {
			MapElement element = getSelectedElement();
			if (element != null) {
				OptionsPanel options = optionPanels.get(element);
				if (options != null) {
					return options.getMouseListener();
				}
			}
			return null;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			MapElementMouseListener l = getOptionsPanel();
			if (l != null) {
				l.mousePressed(e, miniMapPanel.getGridCoordinates(e.getX(), e.getY()));
				return;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			MapElementMouseListener l = getOptionsPanel();
			if (l != null) {
				l.mouseReleased(e, miniMapPanel.getGridCoordinates(e.getX(), e.getY()));
				return;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			MapElementMouseListener l = getOptionsPanel();
			if (l != null) {
				l.mouseDragged(e, miniMapPanel.getGridCoordinates(e.getX(), e.getY()));
				return;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			MapElementMouseListener l = getOptionsPanel();
			if (l != null) {
				l.mouseClicked(e, miniMapPanel.getGridCoordinates(e.getX(), e.getY()));
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
		}
	};

	private PropertyChangeListener labelListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_LABEL)) {
				MapElement element = (MapElement) e.getSource();
				miniMapPanel.updateTreeNode(element);
				//elementTree.repaint();
			}
		}
	};

	private abstract class AddElementAction<P extends OptionsPanel> extends AbstractAction {
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

		protected P addElement(MapElement parent) {
			P panel = createOptionsPanel(parent);
			if (panel != null) {
				MapElement element = panel.getElement();
				element.addPropertyChangeListener(labelListener);
				optionPanels.put(element, panel);
				elementTree.setSelectionPath(miniMapPanel.getTreePath(element));
				//elementList.setSelectedValue(element, true);
			}
			return panel;
		}

		@Override
		public String toString() {
			return name;
		}
	};

	private AddElementAction<ImageOptionsPanel> imageElementAction = new AddElementAction<ImageOptionsPanel>("Image") {
		JFileChooser chooser = new JFileChooser(last_file);

		@Override
		protected ImageOptionsPanel createOptionsPanel(MapElement parent) {
			if (chooser.showOpenDialog(ControllerFrame.this) == JFileChooser.APPROVE_OPTION) {
				last_file = chooser.getSelectedFile();
				return new ImageOptionsPanel(chooser.getSelectedFile(), parent, display);
			}
			return null;
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

	private AddElementAction<DarknessMaskOptionsPanel> darknessAction = new AddElementAction<DarknessMaskOptionsPanel>("Darkness") {
		@Override
		protected DarknessMaskOptionsPanel createOptionsPanel(MapElement parent) {
			return new DarknessMaskOptionsPanel(parent, display);
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
			return new TokenOptionsPanel(parent, display);
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

	private AddElementAction<CallibrateOptionsPanel> callibrateAction = new AddElementAction<CallibrateOptionsPanel>("Callibrate") {
		@Override
		protected CallibrateOptionsPanel createOptionsPanel(MapElement parent) {
			return new CallibrateOptionsPanel(parent, display);
		}
	};

	private AddElementAction<CameraOptionsPanel> cameraImageAction = new AddElementAction<CameraOptionsPanel>("Camera Image") {
		@Override
		protected CameraOptionsPanel createOptionsPanel(MapElement parent) {
			return new CameraOptionsPanel(parent, display, camera);
		}
	};

	private void addChildren(Document doc, Element docParent, TreeModel tree, Object treeParent) {
		for (int i = 0; i < tree.getChildCount(treeParent); i++) {
			Object mapElement = tree.getChild(treeParent, i);
			OptionsPanel p = this.optionPanels.get(mapElement);
			Element e = p.getElement(doc);
			if (e != null) {
				docParent.appendChild(e);
				addChildren(doc, e, tree, mapElement);
			}
		}
	}

	public Node getElement(Document doc) {
		Element root = doc.createElement("Elements");
		TreeModel tree = miniMapPanel.getTreeModel();
		addChildren(doc, root, tree, tree.getRoot());
		return root;
	}

	public void parseNode(Element e, MapElement parent) {
		String tag = e.getTagName();
		OptionsPanel p = null;

		if (tag.equals(GridOptionsPanel.XML_TAG)) {
			p = gridPanel;
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
		} else if (tag.equals(LightSourceOptionsPanel.XML_TAG)) {
			p = lightSourceAction.addElement(parent);
		} else if (tag.equals(InitiativeOptionsPanel.XML_TAG)) {
			p = initiativeAction.addElement(parent);
		} else if (tag.equals(GroupOptionsPanel.XML_TAG)) {
			p = groupAction.addElement(parent);
		} else if (tag.equals(BoundsOptionsPanel.XML_TAG)) {
			p = screensAction.addElement(parent);
		} else if (tag.equals(CallibrateOptionsPanel.XML_TAG)) {
			p = callibrateAction.addElement(parent);
		} else if (tag.equals(TokenOptionsPanel.XML_TAG)) {
			p = tokenAction.addElement(parent);
		} else if (tag.equals(ShapeableTemplateOptionsPanel.XML_TAG)) {
			p = shapeableAction.addElement(parent);
		} else if (tag.equals(BrowserOptionsPanel.XML_TAG)) {
			p = browserAction.addElement(parent);
		} else if (tag.equals(ImageOptionsPanel.XML_TAG)) {
			if (e.hasAttribute(ImageOptionsPanel.FILE_ATTRIBUTE_NAME)) {
				File file = new File(e.getAttribute(ImageOptionsPanel.FILE_ATTRIBUTE_NAME));
				p = new ImageOptionsPanel(file, parent, display);
				MapElement element = p.getElement();
				element.addPropertyChangeListener(labelListener);
				optionPanels.put(element, p);
				elementTree.setSelectionPath(miniMapPanel.getTreePath(element));
			}
		}

		if (p != null) {
			p.parseDOM(e);
			parent = p.getElement();
		}

		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			parseNode((Element) nodes.item(i), parent);
		}
	}

	public void parseDOM(Element el) {
		if (!el.getTagName().equals("Elements")) return;

		parseNode(el, null);
	}

	public void updateOverlay(int width, int height) {
		overlay.updateOverlay(width, height);
	}
}

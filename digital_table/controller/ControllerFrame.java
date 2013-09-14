package digital_table.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
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
import digital_table.elements.Label;
import digital_table.elements.MapElement;
import digital_table.elements.SpreadTemplate;
import digital_table.server.MediaManager;
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
	private Map<MapElement, OptionsPanel<?>> optionPanels = new HashMap<MapElement, OptionsPanel<?>>();
	private JList availableList;
	private JTree elementTree;
	private GridOptionsPanel gridPanel;

	public ControllerFrame(TableDisplay remote, CameraPanel camera) {
		super("DigitalTable Controller");

		instance = this;

		this.camera = camera;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(windowListener);

		overlay = new TokenOverlay();
		if (camera != null) {
			camera.setOverlayGenerator(this);
		} else {
			JFrame overlayFrame = new JFrame("Token Overlay");
			JPanel overlayPanel = overlay.getPanel();
			overlayPanel.setPreferredSize(new Dimension(20 * overlay.rows, 20 * overlay.columns));
			overlayFrame.add(overlayPanel);
			overlayFrame.pack();
			overlayFrame.setVisible(true);
		}
		display = new DisplayManager(remote, miniMapPanel, overlay);

		miniMapPanel.addMouseMotionListener(miniMapMouseListener);
		miniMapPanel.addMouseListener(miniMapMouseListener);
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

		elementTree = new JTree(miniMapPanel.getTreeModel());
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
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AddElementAction<?> action = (AddElementAction<?>) availableList.getSelectedValue();
				if (action != null) {
					OptionsPanel<?> options = action.addElement(null);
					elementTree.setSelectionPath(miniMapPanel.getTreePath(options.getElement()));
				}
			}
		});

		JButton addChildButton = new JButton("Add Child");
		addChildButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AddElementAction<?> action = (AddElementAction<?>) availableList.getSelectedValue();
				MapElement parent = getSelectedElement();
				if (action != null && parent != null && parent instanceof Group) {
					OptionsPanel<?> options = action.addElement(parent);
					elementTree.setSelectionPath(miniMapPanel.getTreePath(options.getElement()));
				}
			}
		});

		JButton groupsButton = new JButton("Groups...");
		groupsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new GroupsDialog();
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
					elementPanel.repaint();
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
		buttonPanel.add(groupsButton);
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
			instance.elementTree.setSelectionPath(instance.miniMapPanel.getTreePath(panel.getElement()));
		}
	}

	private MouseInputListener miniMapMouseListener = new MouseInputListener() {
		protected MapElementMouseListener getOptionsPanel() {
			MapElement element = getSelectedElement();
			if (element != null) {
				OptionsPanel<?> options = optionPanels.get(element);
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
			if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_LABEL)
					|| (e.getSource() instanceof Label && e.getPropertyName().equals(Label.PROPERTY_TEXT))) {
				MapElement element = (MapElement) e.getSource();
				miniMapPanel.updateTreeNode(element);
				//elementTree.repaint();
			}
		}
	};

	private class GroupsDialog extends JDialog {
		// TODO apply dialog should only be enabled when selections have been made in both trees
		private JTree elements;
		private JTree groups;

		GroupsDialog() {
			super(ControllerFrame.this, "Rearrange hierarchy", true);

			elements = new JTree(miniMapPanel.getTreeModel());
			elements.setRootVisible(false);
			elements.setVisibleRowCount(10);
			elements.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

			groups = new JTree(groupsTreeModel);
			groups.setVisibleRowCount(10);
			groups.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			JButton okButton = new JButton("Ok");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					apply();
					dispose();
				}
			});

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});

			JButton applyButton = new JButton("Apply");
			applyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					apply();
				}
			});

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
			private TreeModel m = miniMapPanel.getTreeModel();

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

	private AddElementAction<ImageOptionsPanel> imageElementAction = new AddElementAction<ImageOptionsPanel>("Image") {
		@Override
		protected ImageOptionsPanel createOptionsPanel(MapElement parent) {
			URI uri = MediaManager.INSTANCE.showFileChooser(ControllerFrame.this);
			if (uri != null) {
				return new ImageOptionsPanel(uri, parent, display);
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
			return new TokenOptionsPanel(parent, display, labelAction);
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
			OptionsPanel<?> p = this.optionPanels.get(mapElement);
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

	public void parseNode(Element e, OptionsPanel<?> parentPanel) {
		String tag = e.getTagName();
		OptionsPanel<?> p = null;
		MapElement parent = parentPanel == null ? null : parentPanel.getElement();

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
				URI uri;
				try {
					uri = new URI(e.getAttribute(ImageOptionsPanel.FILE_ATTRIBUTE_NAME));
					p = new ImageOptionsPanel(uri, parent, display);
					MapElement element = p.getElement();
					element.addPropertyChangeListener(labelListener);
					optionPanels.put(element, p);
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		}

		if (p != null) p.parseDOM(e, parentPanel);

		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			parseNode((Element) nodes.item(i), p);
		}
	}

	public void parseDOM(Element el) {
		if (!el.getTagName().equals("Elements")) return;

		parseNode(el, null);
		elementTree.setSelectionPath(miniMapPanel.getTreePath(gridPanel.getElement()));
	}

	public void updateOverlay(int width, int height) {
		overlay.updateOverlay(width, height);
	}
}

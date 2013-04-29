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
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
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
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import combat.CombatPanel;
import combat.InitiativeListener;

import digital_table.elements.Browser;
import digital_table.elements.BrowserLocal;
import digital_table.elements.BrowserRemote;
import digital_table.elements.DarknessMask;
import digital_table.elements.Grid;
import digital_table.elements.Group;
import digital_table.elements.Initiative;
import digital_table.elements.Label;
import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;
import digital_table.elements.MapImage;
import digital_table.elements.ScreenBounds;
import digital_table.elements.ShapeableTemplate;
import digital_table.elements.SpreadTemplate;
import digital_table.elements.Token;
import digital_table.server.TableDisplay;

/* TODO main priorities:
 * BUG Issue with image scaling before remote visible
 * Improve Tokens element: hps/status, floating label, rotate labels with token
 * Better integration with AssistantDM - auto open controller if server is available, exit just controller when closing controller window, cleanup controller when exiting assitantdm window
 * Implement Creature Size
 * Expand DarknessMask to true visibility element including light sources and low-light
 * Camera integration
 * Alternate button dragging (e.g. resize)
 * Recalibrate - could be done using screen bounds element
 * Auto configure - set defaults according to OS screen layout
 * Load/Save
 * Fix MapImage for that rotation preseves scale
 * Make line and spread templates editable?
 * Swarm Token (editable token with replicated painting)
 * Zoomed view on controller
 * MiniMapPanel should maintain aspect ratio when resizing (at least optionally)
 * Convert MapElements to use location property instead of X and Y properties - will reduce dragging code in OptionsPanel subclasses
 * dice roller element?
 * thrown object scatter?
 */

/* grouping:
 * General grouping e.g. for encounter (images, tokens, perhaps darkness and templates)
 *  - grouped in interface but maintain display order
 *  - don't need to drag group
 *  - visibility: show/hide group, probably don't need alpha
 *  > maybe better to use load/save of selected elements
 * 
 * Token grouping
 *  - grouped in interface and can be painted together
 *  - don't need to drag group
 *  - visibility: show/hide group - useful to have independent setting from individual tokens though that could be confusing
 *  - override alpha, colour, perhaps status settings, image
 *  - if templates can be attached to token then painting order needs to be maintained
 *  - also want to be able to attach light sources
 *  > could be implemented purely in interface except show/hide would have to override
 * 
 * Darkness mask light sources
 *  - grouped in interface, painted as part of darkness mask (no independent painting)
 *  - visibility: not independent of darkness mask
 *  - no override of properties or dragging of group
 *  - note: can be attached to token and dragged with token
 *  > needs to be implemented in darkness mask element
 * 
 * Attached label
 *  - grouped in interface or attached to element's ui but not shown in list. can be painted with attached element
 *  - visibility: parent element should override. could have independent visibility and combined alpha
 *  - label must drag with parent
 *  > could implement as property on elements like token, template
 * 
 * imlpementations:
 * UI only implementation:
 *  - easy to override properties
 *  - drag can be done
 *  - hard to show/hide group without overiding children's visibility setting
 *  - can't support light sources
 * 
 * Full implementation with any element as child:
 *  - hard to determine what properties can be overidden
 *  - might need to restrict types of children for specific parent types
 *  - drag needs to determine if children support it
 */

//TODO JavaFX platform stuff should only be called if necessary (once Browser is added)

@SuppressWarnings("serial")
public class ControllerFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private TableDisplay display;
	private MiniMapPanel miniMapPanel = new MiniMapPanel();
	private JPanel elementPanel = new JPanel();
	private Map<MapElement, OptionsPanel> optionPanels = new HashMap<MapElement, OptionsPanel>();
	private JList availableList;
	//private JList elementList;
	private JTree elementTree;
	//private DefaultListModel elements;
	private Grid grid;

	public ControllerFrame(TableDisplay remote) {
		super("DigitalTable Controller");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(windowListener);

		display = remote;

		miniMapPanel.addMouseMotionListener(miniMapMouseListener);
		miniMapPanel.addMouseListener(miniMapMouseListener);
		//elements = (DefaultListModel) miniMapPanel.getModel();
		add(miniMapPanel);

		AddElementAction<?>[] availableElements = {
				tokenAction,
				imageElementAction,
				templateAction,
				lineAction,
				shapeableAction,
				labelAction,
				darknessAction,
				initiativeAction,
				browserAction,
				groupAction,
				screensAction
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
				if (element != null && element != grid) {
					try {
						elementPanel.removeAll();
						elementPanel.revalidate();
						display.removeElement(element.getID());
						miniMapPanel.removeElement(element.getID());
						optionPanels.remove(element);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		});

		JButton upButton = new JButton("Up");
		upButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MapElement element = getSelectedElement();
				if (element != null) {
					try {
						display.promoteElement(element.getID());
						miniMapPanel.promoteElement(element);
						elementTree.setSelectionPath(miniMapPanel.getTreePath(element));
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JButton downButton = new JButton("Down");
		downButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MapElement element = getSelectedElement();
				if (element != null) {
					try {
						display.demoteElement(element.getID());
						miniMapPanel.demoteElement(element);
						elementTree.setSelectionPath(miniMapPanel.getTreePath(element));
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
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

		try {
			grid = new Grid();
			display.addElement(grid);
			OptionsPanel p = new GridOptionsPanel(grid, display);	// set up the option panel as the remote grid is configured
			grid.setProperty(Grid.PROPERTY_RULER_COLUMN, 0);
			grid.setProperty(Grid.PROPERTY_RULER_ROW, 0);
			miniMapPanel.addElement(grid, null);
			optionPanels.put(grid, p);

		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

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
		try {
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
		} catch (RemoteException e1) {
			e1.printStackTrace();
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

	private abstract class AddElementAction<E extends MapElement> extends AbstractAction {
		String name;

		protected AddElementAction(String name) {
			this.name = name;
			putValue(NAME, "Add " + name);
		}

		abstract protected E createElement(MapElement parent);

		abstract protected OptionsPanel createOptionsPanel(E e);

		@Override
		public void actionPerformed(ActionEvent arg0) {
			addElement(null);
		}

		protected void addElement(MapElement parent) {
			E element = createElement(parent);
			if (element != null) {
				miniMapPanel.addElement(element, parent);
				optionPanels.put(element, createOptionsPanel(element));
				elementTree.setSelectionPath(miniMapPanel.getTreePath(element));
				//elementList.setSelectedValue(element, true);
			}
		}

		protected boolean sendElement(E e, MapElement parent) {
			try {
				if (parent == null) {
					display.addElement(e);
				} else {
					display.addElement(e, parent.getID());
				}
				return true;
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			return false;
		}

		@Override
		public String toString() {
			return name;
		}
	};

	private AddElementAction<MapImage> imageElementAction = new AddElementAction<MapImage>("Image") {
		JFileChooser chooser = new JFileChooser();

		@Override
		protected MapImage createElement(MapElement parent) {
			if (chooser.showOpenDialog(ControllerFrame.this) == JFileChooser.APPROVE_OPTION) {
				try {
					File f = chooser.getSelectedFile();
					byte bytes[] = new byte[(int) f.length()];
					FileInputStream stream = new FileInputStream(f);
					stream.read(bytes);
					MapImage mapImage = new MapImage(bytes, f.getName());
					if (sendElement(mapImage, parent)) {
						mapImage.setProperty(MapElement.PROPERTY_VISIBLE, true);
						mapImage.addPropertyChangeListener(labelListener);
						return mapImage;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				System.out.println("Cancelled");
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(MapImage e) {
			return new ImageOptionsPanel(e, display);
		}
	};

	private AddElementAction<SpreadTemplate> templateAction = new AddElementAction<SpreadTemplate>("Template") {
		@Override
		protected SpreadTemplate createElement(MapElement parent) {
			SpreadTemplate template = new SpreadTemplate(4, 10, 10);
			if (sendElement(template, parent)) {
				template.setProperty(MapElement.PROPERTY_VISIBLE, true);
				template.addPropertyChangeListener(labelListener);
				return template;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(SpreadTemplate e) {
			return new SpreadTemplateOptionsPanel(e, display);
		}
	};

	private AddElementAction<LineTemplate> lineAction = new AddElementAction<LineTemplate>("Line") {
		@Override
		protected LineTemplate createElement(MapElement parent) {
			LineTemplate template = new LineTemplate(18, 14, 21, 7);
			if (sendElement(template, parent)) {
				template.setProperty(MapElement.PROPERTY_VISIBLE, true);
				template.addPropertyChangeListener(labelListener);
				return template;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(LineTemplate e) {
			return new LineTemplateOptionsPanel(e, display);
		}
	};

	private AddElementAction<ShapeableTemplate> shapeableAction = new AddElementAction<ShapeableTemplate>("Shapeable") {
		@Override
		protected ShapeableTemplate createElement(MapElement parent) {
			ShapeableTemplate template = new ShapeableTemplate();
			if (sendElement(template, parent)) {
				template.setProperty(MapElement.PROPERTY_VISIBLE, true);
				template.addPropertyChangeListener(labelListener);
				return template;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(ShapeableTemplate e) {
			return new ShapeableTemplateOptionsPanel(e, display);
		}
	};

	private AddElementAction<DarknessMask> darknessAction = new AddElementAction<DarknessMask>("Darkness") {
		@Override
		protected DarknessMask createElement(MapElement parent) {
			DarknessMask template = new DarknessMask();
			template.setProperty(MapElement.PROPERTY_VISIBLE, true);
			if (sendElement(template, parent)) {
				template.setProperty(DarknessMask.PROPERTY_ALPHA, 0.5f);
				return template;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(DarknessMask e) {
			return new DarknessMaskOptionsPanel(e, display);
		}
	};

	private AddElementAction<Browser> browserAction = new AddElementAction<Browser>("Browser") {
		@Override
		protected Browser createElement(MapElement parent) {
			BrowserRemote remote = new BrowserRemote();
			if (sendElement(remote, parent)) {
				Browser browser = new BrowserLocal(remote);
				browser.addPropertyChangeListener(labelListener);
				return browser;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(Browser e) {
			return new BrowserOptionsPanel(e, display);
		}
	};

	private AddElementAction<Initiative> initiativeAction = new AddElementAction<Initiative>("Initiative") {
		@Override
		protected Initiative createElement(MapElement parent) {
			final Initiative init = new Initiative();
			if (sendElement(init, parent)) {
				init.setProperty(MapElement.PROPERTY_VISIBLE, true);
				CombatPanel.getCombatPanel().addInitiativeListener(new InitiativeListener() {
					@Override
					public void initiativeUpdated(String text) {
						init.setProperty(Label.PROPERTY_TEXT, text);
						try {
							display.setElementProperty(init.getID(), Label.PROPERTY_TEXT, text);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				});
				return init;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(Initiative e) {
			return new InitiativeOptionsPanel(e, display);
		}
	};

	private AddElementAction<Label> labelAction = new AddElementAction<Label>("Label") {
		@Override
		protected Label createElement(MapElement parent) {
			final Label label = new Label();
			if (sendElement(label, parent)) {
				label.setProperty(MapElement.PROPERTY_VISIBLE, true);
				label.addPropertyChangeListener(labelListener);
				return label;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(Label e) {
			return new LabelOptionsPanel(e, display);
		}
	};

	private AddElementAction<ScreenBounds> screensAction = new AddElementAction<ScreenBounds>("Screens") {
		@Override
		protected ScreenBounds createElement(MapElement parent) {
			ScreenBounds bounds = new ScreenBounds();
			if (sendElement(bounds, parent)) {
				bounds.setProperty(MapElement.PROPERTY_VISIBLE, true);
				return bounds;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(ScreenBounds e) {
			return new BoundsOptionsPanel(e, display);
		}
	};

	private AddElementAction<Token> tokenAction = new AddElementAction<Token>("Token") {
		@Override
		protected Token createElement(MapElement parent) {
			Token token = new Token();
			if (sendElement(token, parent)) {
				token.setProperty(MapElement.PROPERTY_VISIBLE, true);
				token.addPropertyChangeListener(labelListener);
				return token;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(Token e) {
			return new TokenOptionsPanel(e, display);
		}
	};

	private AddElementAction<Group> groupAction = new AddElementAction<Group>("Group") {
		@Override
		protected Group createElement(MapElement parent) {
			Group group = new Group();
			if (sendElement(group, parent)) {
				group.setProperty(MapElement.PROPERTY_VISIBLE, true);
				group.addPropertyChangeListener(labelListener);
				return group;
			}
			return null;
		}

		@Override
		protected OptionsPanel createOptionsPanel(Group e) {
			return new GroupOptionsPanel(e, display);
		}
	};
}

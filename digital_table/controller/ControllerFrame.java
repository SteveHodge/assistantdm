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

import party.Monster;
import camera.CameraPanel;
import digital_table.elements.Grid;
import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.SpreadTemplate;
import digital_table.server.TableDisplay;

//TODO JavaFX platform stuff should only be called if necessary (once Browser is added)

@SuppressWarnings("serial")
public class ControllerFrame extends JFrame {
	private static ControllerFrame instance = null;	// TODO remove

	private TableDisplay display;
	private CameraPanel camera;
	private MiniMapPanel miniMapPanel = new MiniMapPanel();
	private JPanel elementPanel = new JPanel();
	private Map<MapElement, OptionsPanel> optionPanels = new HashMap<MapElement, OptionsPanel>();
	private JList availableList;
	//private JList elementList;
	private JTree elementTree;
	//private DefaultListModel elements;
	private Grid grid;

	public ControllerFrame(TableDisplay remote, CameraPanel camera) {
		super("DigitalTable Controller");

		instance = this;

		this.camera = camera;
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
				miniMapPanel.addElement(element, parent);
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
		JFileChooser chooser = new JFileChooser();

		@Override
		protected ImageOptionsPanel createOptionsPanel(MapElement parent) {
			if (chooser.showOpenDialog(ControllerFrame.this) == JFileChooser.APPROVE_OPTION) {
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
}

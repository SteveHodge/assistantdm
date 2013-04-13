package digital_table.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import combat.CombatPanel;
import combat.InitiativeListener;

import digital_table.server.TableDisplay;
import digital_table.elements.*;

/* TODO main priorities:
 * Darkness mask element
 * Alternate button dragging (e.g. resize)
 * Recalibrate - could be done using screen bounds element
 * Auto configure - set defaults according to OS screen layout
 * Load/Save
 * Fix MapImage for that rotation preseves scale
 * Add MapElement method that is called on remove - use for cleanup?
 * Make MapImage editable? other templates?
 * Zoomed view on controller
 * MiniMapPanel should maintain aspect ratio when resizing (at least optionally)
 */

//TODO JavaFX platform stuff should only be called if necessary (once Browser is added)
//TODO dragging/clicking handling should be moved to optionpanel
/* at the moment darknessmask won't reset on drag on the remote because the remote doesn't get the getDragTarget() call.
 * it would be more efficient to only send the resulting changes to the remote rather than all drag mouse events.
 * optionpanel is already used for snap to grid.
 */

public class ControllerFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	TableDisplay display;
	MiniMapPanel miniMapPanel = new MiniMapPanel();
	JPanel elementPanel = new JPanel();
	Map<MapElement,OptionsPanel> optionPanels = new HashMap<MapElement,OptionsPanel>();
	JList availableList; 
	JList elementList;
	DefaultListModel elements;
	Grid grid;

	public ControllerFrame(TableDisplay remote) {
		super("DigitalTable Controller");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(windowListener);

		display = remote;

		miniMapPanel.addMouseMotionListener(miniMapMouseListener);
		miniMapPanel.addMouseListener(miniMapMouseListener);
		elements = (DefaultListModel)miniMapPanel.getModel();
		add(miniMapPanel);

		AddElementAction[] availableElements = {
				imageElementAction,
				templateAction,
				lineAction,
				shapeableAction,
				darknessAction,
				initiativeAction,
				browserAction,
				screensAction
		};
		availableList = new JList(availableElements);
		availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		elementList = new JList(elements);
		elementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		elementList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					MapElement element = (MapElement)elementList.getSelectedValue();
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
			}
		});

		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AddElementAction action = (AddElementAction)availableList.getSelectedValue();
				if (action != null) action.actionPerformed(arg0);
			}
		});
		
		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MapElement element = (MapElement)elementList.getSelectedValue();
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
			public void actionPerformed(ActionEvent e) {
				int index = elementList.getSelectedIndex();
				if (index > 0) {
					MapElement element = (MapElement)elements.remove(index);
					elements.add(index-1, element);
					elementList.setSelectedIndex(index-1);
					try {
						display.reorderElement(element.getID(), index-1);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JButton downButton = new JButton("Down");
		downButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = elementList.getSelectedIndex();
				if (index != -1 && index < elements.size()-1) {
					MapElement element = (MapElement)elements.remove(index);
					elements.add(index+1, element);
					elementList.setSelectedIndex(index+1);
					try {
						display.reorderElement(element.getID(), index+1);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0,4));
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(upButton);
		buttonPanel.add(downButton);
//		buttonPanel.add(quitButton);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		rightPanel.add(new JScrollPane(availableList));
		rightPanel.add(buttonPanel);
		rightPanel.add(Box.createRigidArea(new Dimension(0,5)));
		rightPanel.add(new JScrollPane(elementList));
		rightPanel.add(elementPanel);
		elementPanel.setLayout(new BorderLayout());
		rightPanel.add(Box.createVerticalGlue());
		
		add(rightPanel, BorderLayout.EAST);
		
		pack();

		try {
			grid = new Grid();
			display.addElement(grid);
			OptionsPanel p = new GridOptionsPanel(grid,display);	// set up the option panel as the remote grid is configured 
			grid.setRulerColumn(0);
			grid.setRulerRow(0);
			miniMapPanel.addElement(grid);
			optionPanels.put(grid, p);

		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

		setVisible(true);
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

	// TODO at the moment for PAINT drags we have no we of knowing which button was used. should create an artificial mouse event for the generated clicks (could then dispense with the dragging parameter)
	MouseInputListener miniMapMouseListener = new MouseInputListener() {
		boolean dragging = false;
		Point2D offset = null;	// initial offset of the mouse pointer from the location of the element in grid coordinates
		MapElement element = null;
		Object target;

		public void mousePressed(MouseEvent e) {
			MapElement el = (MapElement)elementList.getSelectedValue();
			if (el == null) return;
			if (el.getDragMode() != MapElement.DragMode.NONE) {
				Point2D mouse = miniMapPanel.getGridCoordinates(e.getX(), e.getY());
				Object t = el.getDragTarget(mouse);
				if (t == null) return;
				element = el;
				target = t;
				if (el.getDragMode() == MapElement.DragMode.MOVE) {
					Point2D targetLoc = element.getLocation(target);
					offset = new Point2D.Double(mouse.getX()-targetLoc.getX(), mouse.getY()-targetLoc.getY());
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (dragging) {
				if (element.getDragMode() == MapElement.DragMode.MOVE) {
					Point2D p = miniMapPanel.getGridCoordinates(e.getX(), e.getY());
					p = new Point2D.Double(p.getX() - offset.getX(), p.getY() - offset.getY());
					if (optionPanels.get(element).snapToGrid()) {
						p.setLocation((int)p.getX(),(int)p.getY());
					}
					element.setLocation(target, p);
					try {
						display.setElementLocation(element.getID(), target, p);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}

				} else if (element.getDragMode() == MapElement.DragMode.PAINT) {
					elementClicked(element, e, true);	// TODO check if this is superfluous. is there always a drag event for the final location?
				}
				dragging = false;
			}
		}

		public void mouseDragged(MouseEvent e) {
			dragging = true;

			if (element.getDragMode() == MapElement.DragMode.PAINT) {
				elementClicked(element, e, true);
				
			} else if (element.getDragMode() == MapElement.DragMode.MOVE) {
				Point2D p = miniMapPanel.getGridCoordinates(e.getX(), e.getY());
				p = new Point2D.Double(p.getX() - offset.getX(), p.getY() - offset.getY());
				if (optionPanels.get(element).snapToGrid()) {
					p.setLocation((int)p.getX(),(int)p.getY());
				}
				element.setLocation(target, p);
			}
		}

		public void mouseClicked(MouseEvent e) {
			MapElement el = (MapElement)elementList.getSelectedValue();
			if (el == null) return;
			elementClicked(el, e, false);
		}

		protected void elementClicked(MapElement el, MouseEvent e, boolean dragging) {
			Point2D mouse = miniMapPanel.getGridCoordinates(e.getX(), e.getY());
			el.elementClicked(mouse, e, dragging);
			try {
				display.elementClicked(el.getID(), mouse, e, dragging);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
		
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {}
	};

	WindowListener windowListener = new WindowListener() {
		public void windowActivated(WindowEvent e) {}
		public void windowClosing(WindowEvent e) {
			quit();			
		}
		public void windowDeactivated(WindowEvent e) {}
		public void windowDeiconified(WindowEvent e) {}
		public void windowIconified(WindowEvent e) {}
		public void windowOpened(WindowEvent e) {}
		public void windowClosed(WindowEvent e) {}
	};

	PropertyChangeListener labelListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_LABEL)) {
				elementList.repaint();
			}
		}
	};

	@SuppressWarnings("serial")
	public abstract class AddElementAction extends AbstractAction {
		String name;

		protected AddElementAction(String name) {
			this.name = name;
			putValue(NAME,"Add "+name);
		}

		public boolean sendElement(MapElement e) {
			try {
				display.addElement(e);
				return true;
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			return false;
		}

		public void addElement(MapElement e, OptionsPanel p) {
			miniMapPanel.addElement(e);
			optionPanels.put(e, p);
			elementList.setSelectedValue(e, true);
		}

		public String toString() {
			return name;
		}
	};

	@SuppressWarnings("serial")
	public AddElementAction imageElementAction = new AddElementAction("Image") {
		JFileChooser chooser = new JFileChooser();

		public void actionPerformed(ActionEvent arg0) {
			if (chooser.showOpenDialog(ControllerFrame.this) == JFileChooser.APPROVE_OPTION) {
				try {
					File f = chooser.getSelectedFile();
					byte bytes[] = new byte[(int)f.length()];
					FileInputStream stream = new FileInputStream(f);
					stream.read(bytes);
					MapImage mapImage = new MapImage(bytes, f.getName());
					if (sendElement(mapImage)) {
						mapImage.setVisible(true);
						mapImage.addPropertyChangeListener(labelListener);
						addElement(mapImage, new ImageOptionsPanel(mapImage, display));
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				System.out.println("Cancelled");
			}
		}
	};

	@SuppressWarnings("serial")
	public AddElementAction templateAction = new AddElementAction("Template") {
		public void actionPerformed(ActionEvent e) {
			SpreadTemplate template = new SpreadTemplate(4, 10, 10);
			if (sendElement(template)) {
				template.setVisible(true);
				template.addPropertyChangeListener(labelListener);
				addElement(template, new SpreadTemplateOptionsPanel(template, display));
			}
		}
	};

	@SuppressWarnings("serial")
	public AddElementAction lineAction = new AddElementAction("Line") {
		public void actionPerformed(ActionEvent e) {
			LineTemplate template = new LineTemplate(18, 14, 21, 7);
			if (sendElement(template)) {
				template.setVisible(true);
				template.addPropertyChangeListener(labelListener);
				addElement(template, new LineTemplateOptionsPanel(template, display));
			}
		}
	};

	@SuppressWarnings("serial")
	public AddElementAction shapeableAction = new AddElementAction("Shapeable") {
		public void actionPerformed(ActionEvent e) {
			ShapeableTemplate template = new ShapeableTemplate();
			if (sendElement(template)) {
				template.setVisible(true);
				template.addPropertyChangeListener(labelListener);
				addElement(template, new ShapeableTemplateOptionsPanel(template, display));
			}
		}
	};

	@SuppressWarnings("serial")
	public AddElementAction darknessAction = new AddElementAction("Darkness") {
		public void actionPerformed(ActionEvent e) {
			DarknessMask template = new DarknessMask();
			template.setVisible(true);
			if (sendElement(template)) {
				template.setAlpha(0.5f);
				addElement(template, new DarknessMaskOptionsPanel(template, display));
			}
		}
	};

	@SuppressWarnings("serial")
	public AddElementAction browserAction = new AddElementAction("Browser") {
		public void actionPerformed(ActionEvent e) {
			BrowserRemote remote = new BrowserRemote();
			if (sendElement(remote)) {
				Browser browser = new BrowserLocal(remote);
				browser.addPropertyChangeListener(labelListener);
				addElement(browser, new BrowserOptionsPanel(browser, display));
			}
		}
	};

	@SuppressWarnings("serial")
	public AddElementAction initiativeAction = new AddElementAction("Initiative") {
		public void actionPerformed(ActionEvent e) {
			final Initiative init = new Initiative();
			if (sendElement(init)) {
				init.setVisible(true);
				CombatPanel.getCombatPanel().addInitiativeListener(new InitiativeListener() {
					public void initiativeUpdated(String text) {
						init.setText(text);
						try {
							display.setElementProperty(init.getID(), Initiative.PROPERTY_TEXT, text);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				});
				addElement(init, new InitiativeOptionsPanel(init, display));
			}
		}
	};

	@SuppressWarnings("serial")
	public AddElementAction screensAction = new AddElementAction("Screens") {
		public void actionPerformed(ActionEvent e) {
			ScreenBounds bounds = new ScreenBounds();
			if (sendElement(bounds)) {
				bounds.setVisible(true);
				addElement(bounds, new BoundsOptionsPanel(bounds, display));
			}
		}
	};
}

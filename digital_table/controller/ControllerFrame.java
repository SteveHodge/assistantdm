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
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import digital_table.elements.Browser;
import digital_table.elements.Grid;
import digital_table.elements.Initiative;
import digital_table.elements.LineTemplate;
import digital_table.elements.MapElement;
import digital_table.elements.MapImage;
import digital_table.elements.SpreadTemplate;

// TODO JavaFX platform stuff should only be called if necessary (once Browser is added)

public class ControllerFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	static final String imageFile = "D:\\Ptolus\\Maps\\DeluxeCityMap\\Ptolus_1px_to_5ft.png";

	TableDisplay display;
	MiniMapPanel miniMapPanel = new MiniMapPanel();
	JPanel elementPanel = new JPanel();
	Map<MapElement,OptionsPanel> optionPanels = new HashMap<MapElement,OptionsPanel>();
	JList elementList;
	DefaultListModel elements;
	Grid grid;

	MouseInputListener dragAndDrop = new MouseInputListener() {
		boolean dragging = false;
		//Point offset = null;
		Point2D offset = null;	// initial offset of the mouse pointer from the location of the element in grid coordinates
		MapElement target = null;

		public void mousePressed(MouseEvent e) {
			MapElement el = (MapElement)elementList.getSelectedValue();
			if (el != null && el.isDraggable()) {
				target = el;
				dragging = true;
				Point2D mouse = miniMapPanel.getGridCoordinates(e.getX(), e.getY());
				offset = new Point2D.Double(mouse.getX()-target.getLocation().getX(), mouse.getY()-target.getLocation().getY());
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (dragging) {
				Point2D p = miniMapPanel.getGridCoordinates(e.getX(), e.getY());
				p = new Point2D.Double(p.getX() - offset.getX(), p.getY() - offset.getY());
				if (optionPanels.get(target).snapToGrid()) {
					p.setLocation((int)p.getX(),(int)p.getY());
				}
				target.setLocation(p);
				try {
					display.setElementPosition(target.getID(), p);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				dragging = false;
			}
		}

		public void mouseDragged(MouseEvent e) {
			if (dragging) {
				Point2D p = miniMapPanel.getGridCoordinates(e.getX(), e.getY());
				p = new Point2D.Double(p.getX() - offset.getX(), p.getY() - offset.getY());
				if (optionPanels.get(target).snapToGrid()) {
					p.setLocation((int)p.getX(),(int)p.getY());
				}
				target.setLocation(p);
			}
		}

		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {}
	};

	public void quit() {
		try {
			display.quit();
			Platform.exit();
			System.exit(0);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}
	
	public ControllerFrame(TableDisplay remote) {
		super("DigitalTable Controller");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}

			public void windowClosed(WindowEvent e) {
				quit();
			}
		});

		display = remote;

		miniMapPanel.addMouseMotionListener(dragAndDrop);
		miniMapPanel.addMouseListener(dragAndDrop);
		elements = (DefaultListModel)miniMapPanel.getModel();
		add(miniMapPanel);

		final PropertyChangeListener labelListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_LABEL)) {
					elementList.repaint();
				}
			}
		};
		
		JButton showImageButton = new JButton("Add Image");
		showImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MapImage mapImage = new MapImage(imageFile);
					display.addElement(mapImage);
					mapImage.setVisible(true);
					mapImage.addPropertyChangeListener(labelListener);
					miniMapPanel.addElement(mapImage);
					optionPanels.put(mapImage, new ImageOptionsPanel(mapImage, display));
					elementList.setSelectedValue(mapImage, true);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		JButton addTemplateButton = new JButton("Add Template");
		addTemplateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					SpreadTemplate template = new SpreadTemplate(4, 10, 10);
					display.addElement(template);
					template.setVisible(true);
					template.addPropertyChangeListener(labelListener);
					miniMapPanel.addElement(template);
					optionPanels.put(template, new SpreadTemplateOptionsPanel(template, display));
					elementList.setSelectedValue(template, true);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		JButton addLineButton = new JButton("Add Line");
		addLineButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					LineTemplate template = new LineTemplate(18, 2, 21, 7);
					display.addElement(template);
					template.setVisible(true);
					template.addPropertyChangeListener(labelListener);
					miniMapPanel.addElement(template);
					optionPanels.put(template, new LineTemplateOptionsPanel(template, display));
					elementList.setSelectedValue(template, true);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});

		JButton addBrowserButton = new JButton("Add Browser");
		addBrowserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Browser browser = new Browser();
					display.addElement(browser);
					browser.setVisible(true);
					browser.addPropertyChangeListener(labelListener);
					miniMapPanel.addElement(browser);
					optionPanels.put(browser, new BrowserOptionsPanel(browser, display));
					elementList.setSelectedValue(browser, true);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JButton addInitiativeButton = new JButton("Add Initiative");
		addInitiativeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					final Initiative init = new Initiative();
					display.addElement(init);
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
					miniMapPanel.addElement(init);
					optionPanels.put(init, new InitiativeOptionsPanel(init, display));
					elementList.setSelectedValue(init, true);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});

		JButton hideImageButton = new JButton("Remove");
		hideImageButton.addActionListener(new ActionListener() {
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
		buttonPanel.setLayout(new GridLayout(0,3));
		buttonPanel.add(hideImageButton);
		buttonPanel.add(showImageButton);
		buttonPanel.add(addTemplateButton);
		buttonPanel.add(addLineButton);
		buttonPanel.add(addBrowserButton);
		buttonPanel.add(addInitiativeButton);
		buttonPanel.add(upButton);
		buttonPanel.add(downButton);
		buttonPanel.add(quitButton);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		rightPanel.add(buttonPanel);
		rightPanel.add(Box.createRigidArea(new Dimension(0,5)));
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
		JScrollPane scroller = new JScrollPane(elementList);
		rightPanel.add(scroller);
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
}

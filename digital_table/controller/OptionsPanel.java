package digital_table.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import digital_table.elements.MapElement;
import digital_table.server.TableDisplay;


@SuppressWarnings("serial")
abstract public class OptionsPanel extends JPanel {
	protected TableDisplay remote;

	protected OptionsPanel(TableDisplay r) {
		remote = r;
	}

	protected enum Mode {BOTH, LOCAL, REMOTE};

	public enum DragMode {
		NONE,	// no dragging
		MOVE,	// dragging will move the element or a subelement
		PAINT	// dragging will trigger a click event on each mouse move
	};
	
	protected JTextField createIntegerControl(final MapElement element, final String property) {
		return createIntegerControl(element, property, Mode.BOTH);
	}

	protected JTextField createIntegerControl(final MapElement element, final String property, final Mode mode) {
		final JTextField field = new JTextField(8);
		field.setText(""+element.getProperty(property));
		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					int newValue = Integer.parseInt(field.getText());
					if (mode != Mode.REMOTE) element.setProperty(property, newValue);
					if (mode != Mode.LOCAL) remote.setElementProperty(element.getID(), property, newValue);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		return field;
	}

	protected JTextField createNullableIntegerControl(final MapElement element, final String property, final Mode mode) {
		final JTextField field = new JTextField(8);
		if (element.getProperty(property) != null) field.setText(""+element.getProperty(property));
		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Integer newValue = null;
					if (field.getText().length() > 0) newValue = Integer.parseInt(field.getText());
					if (mode != Mode.REMOTE) element.setProperty(property, newValue);
					if (mode != Mode.LOCAL) remote.setElementProperty(element.getID(), property, newValue);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		return field;
	}

	
	protected JTextField createDoubleControl(final MapElement element, final String property) {
		return createDoubleControl(element, property, Mode.BOTH);
	}

	protected JTextField createDoubleControl(final MapElement element, final String property, final Mode mode) {
		final JTextField field = new JTextField(8);
		field.setText(""+element.getProperty(property));
		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					double newRadius = Double.parseDouble(field.getText());
					if (mode != Mode.REMOTE) element.setProperty(property, newRadius);
					if (mode != Mode.LOCAL) remote.setElementProperty(element.getID(), property, newRadius);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		return field;
	}

	protected JPanel createColorControl(final MapElement element, final String property) {
		return createColorControl(element, property, Mode.BOTH);
	}

	protected JPanel createColorControl(final MapElement element, final String property, final Mode mode) {
		final JPanel colorPanel = new JPanel();
		colorPanel.setBackground((Color)element.getProperty(property));
		colorPanel.setOpaque(true);
		colorPanel.setMinimumSize(new Dimension(50,20));
		colorPanel.setPreferredSize(new Dimension(50,20));
		colorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		colorPanel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				try {
					Color newColor = JColorChooser.showDialog(OptionsPanel.this, "Choose colour", (Color)element.getProperty(property));
					if (mode != Mode.LOCAL) remote.setElementProperty(element.getID(), property, newColor);
					if (mode != Mode.REMOTE) element.setProperty(property, newColor);
					colorPanel.setBackground(newColor);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
	
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});
		return colorPanel;
	}
	
	protected JSlider createSliderControl(final MapElement element, final String property) {
		return createSliderControl(element, property, Mode.BOTH);
	}

	protected JSlider createSliderControl(final MapElement element, final String property, final Mode mode) {
		JSlider alphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		alphaSlider.setValue((int)(100*(Float)element.getProperty(property)));
		alphaSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				try {
					JSlider slider = (JSlider)e.getSource();
					float alpha = ((int)slider.getValue())/100f;
					if (mode != Mode.REMOTE) element.setProperty(property, alpha);
					if (!slider.getValueIsAdjusting() && mode != Mode.LOCAL) {
						remote.setElementProperty(element.getID(), property, alpha);
					}
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return alphaSlider;
	}

	protected JComboBox createComboControl(final MapElement element, final String property, Object[] values) {
		return createComboControl(element, property, Mode.BOTH, values);
	}

	protected JComboBox createComboControl(final MapElement element, final String property, final Mode mode, Object[] values) {
		final JComboBox typeCombo = new JComboBox(values);
		typeCombo.setSelectedItem(element.getProperty(property));
		typeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JComboBox combo = (JComboBox)e.getSource();
					Object selected = combo.getSelectedItem();
					if (mode != Mode.REMOTE) element.setProperty(property, selected);
					if (mode != Mode.LOCAL) remote.setElementProperty(element.getID(), property, selected);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return typeCombo;
	}

	protected JComboBox createComboControl(final MapElement element, final String property, final Mode mode, ComboBoxModel values) {
		final JComboBox typeCombo = new JComboBox(values);
		typeCombo.setSelectedItem(element.getProperty(property));
		typeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JComboBox combo = (JComboBox)e.getSource();
					Object selected = combo.getSelectedItem();
					if (mode != Mode.REMOTE) element.setProperty(property, selected);
					if (mode != Mode.LOCAL) remote.setElementProperty(element.getID(), property, selected);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return typeCombo;
	}

	protected JTextField createStringControl(final MapElement element, final String property) {
		return createStringControl(element, property);
	}

	protected JTextField createStringControl(final MapElement element, final String property, final Mode mode) {
		final JTextField textField = new JTextField(30);
		textField.setText(""+element.getProperty(property));
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (mode != Mode.REMOTE) element.setProperty(property, textField.getText());
					if (mode != Mode.LOCAL) remote.setElementProperty(element.getID(), property, textField.getText());
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return textField;
	}

	protected JLabel createLabelControl(final MapElement element, final String property) {
		final JLabel label = new JLabel();
		label.setText(""+element.getProperty(property));
		return label;
	}
	
	// TODO use this for visibility - need to implement visibility properties
	protected JCheckBox createCheckBox(final MapElement element, final String property, final Mode mode, String label) {
		JCheckBox check = new JCheckBox(label);
		check.setSelected((Boolean)element.getProperty(property));
		check.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				try {
					JCheckBox check = (JCheckBox)e.getSource();
					if (mode != Mode.REMOTE) element.setProperty(property, check.isSelected());
					if (mode != Mode.LOCAL) remote.setElementProperty(element.getID(), property, check.isSelected());
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return check;
	}

	// TODO convert users to use createCheckBox?
	// this control does not apply the changes to the local MapElement - it's intended for visibility 
	protected JCheckBox createVisibilityControl(final MapElement element) {
		JCheckBox cb = createCheckBox(element, MapElement.PROPERTY_VISIBLE, Mode.REMOTE, "visible?");
		cb.setSelected(false);
		return cb;
	}

	// TODO convert users to use createCheckBox?
	// this control does not apply the changes to the local MapElement - it's intended for visibility 
	protected JCheckBox createVisibilityControl(final MapElement element, String label) {
		JCheckBox cb = createCheckBox(element, MapElement.PROPERTY_VISIBLE, Mode.REMOTE, label);
		cb.setSelected(false);
		return cb;
	}
	
	protected final static String[] options = {"0","90","180","270"};

	protected JComboBox createRotationControl(final MapElement element, final String property, final Mode mode) {
		JComboBox rotationsCombo = new JComboBox(options);
		rotationsCombo.setSelectedIndex((Integer)element.getProperty(property));
		rotationsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JComboBox combo = (JComboBox)e.getSource();
					int index = combo.getSelectedIndex();
					if (mode != Mode.REMOTE) element.setProperty(property, index);
					if (mode != Mode.LOCAL) remote.setElementProperty(element.getID(), property, index);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return rotationsCombo;
	};
	
	public MapElementMouseListener getMouseListener() {
		return null;
	}

	public abstract MapElement getElement();
	
	abstract public class DefaultDragger implements MapElementMouseListener {
		protected boolean dragging = false;
		protected int button;
		protected Point2D offset;
		protected String target;
		
		protected abstract String getDragTarget(Point2D gridLocation);

		protected void setTargetLocation(Point2D p) {
			if (target == null) return;
			try {
				remote.setElementProperty(getElement().getID(), target, p);
				getElement().setProperty(target, p);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		protected Point2D getTargetLocation() {
			return (Point2D)getElement().getProperty(target);
		}

		public void mousePressed(MouseEvent e, Point2D gridloc) {
			button = e.getButton();
			target = getDragTarget(gridloc);
			if (target == null) return;
			Point2D targetLoc = getTargetLocation();
			offset = new Point2D.Double(gridloc.getX()-targetLoc.getX(), gridloc.getY()-targetLoc.getY());
		}
	
		public void mouseReleased(MouseEvent e, Point2D gridloc) {
			if (dragging) {
				Point2D p = new Point2D.Double(gridloc.getX() - offset.getX(), gridloc.getY() - offset.getY());
				setTargetLocation(p);
				dragging = false;
			}
		}
	
		public void mouseDragged(MouseEvent e, Point2D gridloc) {
			if (button == MouseEvent.BUTTON1 && target != null) {
				dragging = true;
			}
			if (dragging) {
				Point2D p = new Point2D.Double(gridloc.getX() - offset.getX(), gridloc.getY() - offset.getY());
				setTargetLocation(p);
			} 
		}

		public void mouseClicked(MouseEvent e, Point2D gridloc) {}
	};
}

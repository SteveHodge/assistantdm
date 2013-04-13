package digital_table.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
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

	abstract public boolean snapToGrid();

	protected enum Mode {BOTH, LOCAL, REMOTE};
	
	protected JTextField createIntegerControl(final MapElement element, final String property) {
		final JTextField field = new JTextField(8);
		field.setText(""+element.getProperty(property));
		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					int newRadius = Integer.parseInt(field.getText());
					element.setProperty(property, newRadius);
					remote.setElementProperty(element.getID(), property, newRadius);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		return field;
	}

	protected JTextField createDoubleControl(final MapElement element, final String property) {
		final JTextField field = new JTextField(8);
		field.setText(""+element.getProperty(property));
		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					double newRadius = Double.parseDouble(field.getText());
					element.setProperty(property, newRadius);
					remote.setElementProperty(element.getID(), property, newRadius);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		return field;
	}

	protected JPanel createColorControl(final MapElement element, final String property) {
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
					remote.setElementProperty(element.getID(), property, newColor);
					element.setProperty(property, newColor);
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
		final JComboBox typeCombo = new JComboBox(values);
		typeCombo.setSelectedItem(element.getProperty(property));
		typeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JComboBox combo = (JComboBox)e.getSource();
					Object selected = combo.getSelectedItem();
					element.setProperty(property, selected);
					remote.setElementProperty(element.getID(), property, selected);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return typeCombo;
	}

	protected JTextField createStringControl(final MapElement element, final String property) {
		final JTextField textField = new JTextField(30);
		textField.setText(""+element.getProperty(property));
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				element.setProperty(property, textField.getText());
				try {
					remote.setElementProperty(element.getID(), property, textField.getText());
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return textField;
	}

	// this control does not send the changes to the remote MapElement - it's intended for labels
	protected JTextField createLocalStringControl(final MapElement element, final String property) {
		final JTextField textField = new JTextField(30);
		textField.setText(""+element.getProperty(property));
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				element.setProperty(property, textField.getText());
			}
		});
		return textField;
	}

	protected JLabel createLabelControl(final MapElement element, final String property) {
		final JLabel label = new JLabel();
		label.setText(""+element.getProperty(property));
		return label;
	}
	
	// this control does not apply the changes to the local MapElement - it's intended for visibility 
	protected JCheckBox createVisibilityControl(final MapElement element) {
		return createVisibilityControl(element, "visible?");
	}

	// this control does not apply the changes to the local MapElement - it's intended for visibility 
	protected JCheckBox createVisibilityControl(final MapElement element, String label) {
		JCheckBox visibleCheck = new JCheckBox(label);
		visibleCheck.setSelected(false);
		visibleCheck.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				try {
					JCheckBox check = (JCheckBox)e.getSource();
					remote.setElementVisible(element.getID(), check.isSelected());
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return visibleCheck;
	}
}

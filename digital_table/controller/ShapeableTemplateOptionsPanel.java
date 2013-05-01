package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import digital_table.elements.ShapeableTemplate;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class ShapeableTemplateOptionsPanel extends OptionsPanel {
	ShapeableTemplate template;
	JPanel colorPanel;
	JTextField labelField;
	JTextField maximumField;
	JSlider alphaSlider;
	JLabel remaining = new JLabel();

	public ShapeableTemplateOptionsPanel(ShapeableTemplate t, TableDisplay r) {
		super(r);
		template = t;
		template.addPropertyChangeListener(listener);

		colorPanel = createColorControl(template, ShapeableTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(template, ShapeableTemplate.PROPERTY_ALPHA);
		maximumField = createIntegerControl(template, ShapeableTemplate.PROPERTY_MAXIMUM);
		labelField = createStringControl(template, ShapeableTemplate.PROPERTY_LABEL, Mode.LOCAL);
		JCheckBox visibleCheck = createVisibilityControl(template);
		updateRemaining();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Maximum:"), c);
		c.gridy++; add(new JLabel("Remaining:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(maximumField, c);
		c.gridy++; add(remaining, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);

	}

	@Override
	public ShapeableTemplate getElement() {
		return template;
	}

	protected void updateRemaining() {
		int max = (Integer)template.getProperty(ShapeableTemplate.PROPERTY_MAXIMUM);
		if (max == 0) {
			remaining.setText("");
		} else {
			remaining.setText("" + (max - template.getPlaced()));
		}
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_MAXIMUM)) {
				maximumField.setText(e.getNewValue().toString());
				updateRemaining();

			} else if (e.getPropertyName().equals(ShapeableTemplate.PROPERTY_PLACED)) {
				updateRemaining();

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	@Override
	public MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	MapElementMouseListener mouseListener = new MapElementMouseListener() {
		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;

			// get nearest grid intersection
			int x = (int)(gridloc.getX() + 0.5d);
			int y = (int)(gridloc.getY() + 0.5d);
			Point p = new Point(x,y);
			if (template.contains(p)) {
				try {
					remote.setElementProperty(template.getID(), ShapeableTemplate.PROPERTY_REMOVECUBE, p);
					template.removeCube(p);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			} else {
				try {
					remote.setElementProperty(template.getID(), ShapeableTemplate.PROPERTY_ADDCUBE, p);
					template.addCube(p);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {}
		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {}
		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {}
	};
}

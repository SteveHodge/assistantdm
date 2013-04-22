package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import digital_table.elements.Initiative;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class InitiativeOptionsPanel extends OptionsPanel {
	Initiative initiative;

	JTextField xField;
	JTextField yField;
	JSlider alphaSlider;
	JComboBox rotationsCombo;
	JPanel colorPanel;
	JPanel bgColorPanel;

	public InitiativeOptionsPanel(Initiative init, TableDisplay r) {
		super(r);
		initiative = init;
		initiative.addPropertyChangeListener(listener);

		xField = createDoubleControl(initiative, Initiative.PROPERTY_X);
		yField = createDoubleControl(initiative, Initiative.PROPERTY_Y);
		alphaSlider = createSliderControl(initiative, Initiative.PROPERTY_ALPHA);
		colorPanel = createColorControl(initiative, Initiative.PROPERTY_COLOR);
		bgColorPanel = createColorControl(initiative, Initiative.PROPERTY_BACKGROUND_COLOR);
		rotationsCombo = createRotationControl(initiative, Initiative.PROPERTY_ROTATIONS, Mode.BOTH);
		JCheckBox visibleCheck = createVisibilityControl(initiative);
				
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1; add(new JLabel("Left edge column:"), c);
		c.gridy++; add(new JLabel("Top edge Row:"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Background:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(bgColorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	public Initiative getElement() {
		return initiative;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Initiative.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(Initiative.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(Initiative.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Initiative.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer)e.getNewValue());

			} else if (e.getPropertyName().equals(Initiative.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());
				
			} else if (e.getPropertyName().equals(Initiative.PROPERTY_BACKGROUND_COLOR)) {
				bgColorPanel.setBackground((Color)e.getNewValue());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	public MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	protected MapElementMouseListener mouseListener = new DefaultDragger() {
		protected String getDragTarget(Point2D gridLocation) {
			return "location";
		}
	
		public void setTargetLocation(Point2D p) {
			try {
				remote.setElementProperty(initiative.getID(), Initiative.PROPERTY_X, p.getX());
				remote.setElementProperty(initiative.getID(), Initiative.PROPERTY_Y, p.getY());
				initiative.setProperty(Initiative.PROPERTY_X, p.getX());
				initiative.setProperty(Initiative.PROPERTY_Y, p.getY());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		protected Point2D getTargetLocation() {
			return new Point2D.Double((Double)initiative.getProperty(Initiative.PROPERTY_X),
					(Double)initiative.getProperty(Initiative.PROPERTY_Y));
		}
	};
}

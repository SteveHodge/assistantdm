package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import digital_table.server.TableDisplay;
import digital_table.elements.LineTemplate;

@SuppressWarnings("serial")
public class LineTemplateOptionsPanel extends OptionsPanel {
	LineTemplate template;
	JTextField xField;
	JTextField yField;
	JTextField rangeField;
	JTextField originXField;
	JTextField originYField;
	JPanel colorPanel;
	JTextField labelField;
	JSlider alphaSlider;
	
	public LineTemplateOptionsPanel(LineTemplate t, TableDisplay r) {
		super(r);
		template = t;
		template.addPropertyChangeListener(listener);

		xField = createIntegerControl(template, LineTemplate.PROPERTY_X);
		yField = createIntegerControl(template, LineTemplate.PROPERTY_Y);
		originXField = createIntegerControl(template, LineTemplate.PROPERTY_ORIGIN_X);
		originYField = createIntegerControl(template, LineTemplate.PROPERTY_ORIGIN_Y);
		rangeField = createIntegerControl(template, LineTemplate.PROPERTY_RANGE);
		colorPanel = createColorControl(template, LineTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(template, LineTemplate.PROPERTY_ALPHA);
		labelField = createLocalStringControl(template, LineTemplate.PROPERTY_LABEL);
		JCheckBox visibleCheck = createVisibilityControl(template);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy = 1; add(new JLabel("Origin X:"), c);
		c.gridy = 2; add(new JLabel("Origin Y:"), c);
		c.gridy = 3; add(new JLabel("Target X:"), c);
		c.gridy = 4; add(new JLabel("Target Y:"), c);
		c.gridy = 5; add(new JLabel("Range:"), c);
		c.gridy = 6; add(new JLabel("Colour:"), c);
		c.gridy = 7; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy = 1; add(originXField, c);
		c.gridy = 2; add(originYField, c);
		c.gridy = 3; add(xField, c);
		c.gridy = 4; add(yField, c);
		c.gridy = 5; add(rangeField, c);
		c.gridy = 6; add(colorPanel, c);
		c.gridy = 7; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(LineTemplate.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_ORIGIN_X)) {
				originXField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_ORIGIN_Y)) {
				originYField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LineTemplate.PROPERTY_RANGE)) {
				rangeField.setText(e.getNewValue().toString());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	public DragMode getDragMode() {
		return DragMode.MOVE;
	}

	public Object getDragTarget(Point2D gridLocation) {
		if (gridLocation.distance(template.getOriginX(), template.getOriginY()) < 2.0d
				&& gridLocation.distance(template.getOriginX(), template.getOriginY()) < gridLocation.distance(template.getX(), template.getY())) {
			return "ORIGIN";
		} else {
			return "TARGET";
		}
	}

	public Point2D getLocation(Object target) {
		if (target.equals("ORIGIN")) {
			return new Point(template.getOriginX(),template.getOriginY());
		} else if (target.equals("TARGET")) {
			return new Point(template.getX(),template.getY());
		}
		return null;
	}

	public void setLocation(Object target, Point2D p) {
		if (target.equals("ORIGIN")) {
			try {
				remote.setElementProperty(template.getID(), LineTemplate.PROPERTY_ORIGIN_X, (int)p.getX());
				remote.setElementProperty(template.getID(), LineTemplate.PROPERTY_ORIGIN_Y, (int)p.getY());
				template.setOriginX((int)p.getX());
				template.setOriginY((int)p.getY());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else if (target.equals("TARGET")) {
			try {
				remote.setElementProperty(template.getID(), LineTemplate.PROPERTY_X, (int)p.getX());
				remote.setElementProperty(template.getID(), LineTemplate.PROPERTY_Y, (int)p.getY());
				template.setX((int)p.getX());
				template.setY((int)p.getY());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}

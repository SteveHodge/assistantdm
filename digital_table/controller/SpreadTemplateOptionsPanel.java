package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import digital_table.elements.SpreadTemplate;
import digital_table.server.TableDisplay;


@SuppressWarnings("serial")
public class SpreadTemplateOptionsPanel extends OptionsPanel {
	SpreadTemplate template;
	JTextField radiusField;
	JTextField xField;
	JTextField yField;
	JComboBox directionCombo;
	JComboBox typeCombo;
	JPanel colorPanel;
	JTextField labelField;
	JSlider alphaSlider;
	
	public SpreadTemplateOptionsPanel(SpreadTemplate t, TableDisplay r) {
		super(r);
		template = t;
		template.addPropertyChangeListener(listener);

		radiusField = createIntegerControl(template, SpreadTemplate.PROPERTY_RADIUS);
		xField = createIntegerControl(template, SpreadTemplate.PROPERTY_X);
		yField = createIntegerControl(template, SpreadTemplate.PROPERTY_Y);
		colorPanel = createColorControl(template, SpreadTemplate.PROPERTY_COLOR);
		alphaSlider = createSliderControl(template, SpreadTemplate.PROPERTY_ALPHA);

		typeCombo = createComboControl(template, SpreadTemplate.PROPERTY_TYPE, SpreadTemplate.Type.values());
		typeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				directionCombo.setEnabled(typeCombo.getSelectedItem() == SpreadTemplate.Type.QUADRANT);
			}
		});
		
		directionCombo = createComboControl(template, SpreadTemplate.PROPERTY_DIRECTION, SpreadTemplate.Direction.values());
		directionCombo.setEnabled(template.getProperty(SpreadTemplate.PROPERTY_TYPE) == SpreadTemplate.Type.QUADRANT);

		labelField = createStringControl(template, SpreadTemplate.PROPERTY_LABEL, Mode.LOCAL);
		JCheckBox visibleCheck = createVisibilityControl(template);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy = 1; add(new JLabel("Radius:"), c);
		c.gridy = 2; add(new JLabel("Column:"), c);
		c.gridy = 3; add(new JLabel("Row:"), c);
		c.gridy = 4; add(new JLabel("Colour:"), c);
		c.gridy = 5; add(new JLabel("Transparency:"), c);
		c.gridy = 6; add(new JLabel("Type:"), c);
		c.gridy = 7; add(new JLabel("Direction:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy = 1; add(radiusField, c);
		c.gridy = 2; add(xField, c);
		c.gridy = 3; add(yField, c);
		c.gridy = 4; add(colorPanel, c);
		c.gridy = 5; add(alphaSlider, c);
		c.gridy = 6; add(typeCombo, c);
		c.gridy = 7; add(directionCombo, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	public SpreadTemplate getElement() {
		return template;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));
				
			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());
				
			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_DIRECTION)) {
				directionCombo.setSelectedItem(e.getNewValue());
				
			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_RADIUS)) {
				radiusField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_TYPE)) {
				typeCombo.setSelectedItem(e.getNewValue());
				
			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(SpreadTemplate.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());
				
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
				remote.setElementProperty(template.getID(), SpreadTemplate.PROPERTY_X, (int)p.getX());
				remote.setElementProperty(template.getID(), SpreadTemplate.PROPERTY_Y, (int)p.getY());
				template.setProperty(SpreadTemplate.PROPERTY_X, (int)p.getX());
				template.setProperty(SpreadTemplate.PROPERTY_Y, (int)p.getY());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		protected Point2D getTargetLocation() {
			return new Point((Integer)template.getProperty(SpreadTemplate.PROPERTY_X),
					(Integer)template.getProperty(SpreadTemplate.PROPERTY_Y));
		}
	};
}

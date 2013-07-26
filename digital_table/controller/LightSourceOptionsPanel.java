package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import digital_table.elements.LightSource;
import digital_table.elements.MapElement;
import digital_table.server.TableDisplay;


@SuppressWarnings("serial")
public class LightSourceOptionsPanel extends OptionsPanel {
	LightSource light;
	JTextField radiusField;
	JTextField xField;
	JTextField yField;
	JTextField labelField;
	JComboBox typeCombo;

	public LightSourceOptionsPanel(MapElement parent, TableDisplay r) {
		super(r);
		light = new LightSource(4, 0, 0);
		sendElement(light, parent);
		light.setProperty(MapElement.PROPERTY_VISIBLE, true);
		light.addPropertyChangeListener(listener);

		radiusField = createIntegerControl(light, LightSource.PROPERTY_RADIUS);
		xField = createIntegerControl(light, LightSource.PROPERTY_X);
		yField = createIntegerControl(light, LightSource.PROPERTY_Y);
		typeCombo = createComboControl(light, LightSource.PROPERTY_TYPE, LightSource.Type.values());
		labelField = createStringControl(light, LightSource.PROPERTY_LABEL, Mode.LOCAL);
		JCheckBox visibleCheck = createVisibilityControl(light);

		// @formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Type:"), c);
		c.gridy++; add(new JLabel("Radius:"), c);
		c.gridy++; add(new JLabel("Column:"), c);
		c.gridy++; add(new JLabel("Row:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(typeCombo, c);
		c.gridy++; add(radiusField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
		add(new JPanel(), c);
		// @formatter:on
	}

	@Override
	public LightSource getElement() {
		return light;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(LightSource.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_RADIUS)) {
				radiusField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(LightSource.PROPERTY_TYPE)) {
				typeCombo.setSelectedItem(e.getNewValue());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	@Override
	public MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	MapElementMouseListener mouseListener = new DefaultDragger() {
		@Override
		String getDragTarget(Point2D gridLocation) {
			return "location";
		}

		@Override
		void setTargetLocation(Point2D p) {
			setRemote(light.getID(), LightSource.PROPERTY_X, (int) p.getX());
			setRemote(light.getID(), LightSource.PROPERTY_Y, (int) p.getY());
			light.setProperty(LightSource.PROPERTY_X, (int) p.getX());
			light.setProperty(LightSource.PROPERTY_Y, (int) p.getY());
		}

		@Override
		Point2D getTargetLocation() {
			return new Point((Integer) light.getProperty(LightSource.PROPERTY_X),
					(Integer) light.getProperty(LightSource.PROPERTY_Y));
		}
	};
}

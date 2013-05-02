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

import digital_table.elements.PersonalEmanation;
import digital_table.server.TableDisplay;


@SuppressWarnings("serial")
public class PersonalEmanationOptionsPanel extends OptionsPanel {
	PersonalEmanation template;
	JTextField radiusField;
	JTextField xField;
	JTextField yField;
	JPanel colorPanel;
	JTextField spaceField;
	JTextField labelField;
	JSlider alphaSlider;

	public PersonalEmanationOptionsPanel(PersonalEmanation t, TableDisplay r) {
		super(r);
		template = t;
		template.addPropertyChangeListener(listener);

		radiusField = createIntegerControl(template, PersonalEmanation.PROPERTY_RADIUS);
		xField = createIntegerControl(template, PersonalEmanation.PROPERTY_X);
		yField = createIntegerControl(template, PersonalEmanation.PROPERTY_Y);
		colorPanel = createColorControl(template, PersonalEmanation.PROPERTY_COLOR);
		alphaSlider = createSliderControl(template, PersonalEmanation.PROPERTY_ALPHA);
		spaceField = createIntegerControl(template, PersonalEmanation.PROPERTY_SPACE);
		labelField = createStringControl(template, PersonalEmanation.PROPERTY_LABEL, Mode.LOCAL);
		JCheckBox visibleCheck = createVisibilityControl(template);

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Radius:"), c);
		c.gridy++; add(new JLabel("Column:"), c);
		c.gridy++; add(new JLabel("Row:"), c);
		c.gridy++; add(new JLabel("Space:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(radiusField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(spaceField, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
		add(new JPanel(), c);
		//@formatter:on
	}

	@Override
	public PersonalEmanation getElement() {
		return template;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_RADIUS)) {
				radiusField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_SPACE)) {
				spaceField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(PersonalEmanation.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

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
			try {
				remote.setElementProperty(template.getID(), PersonalEmanation.PROPERTY_X, (int) p.getX());
				remote.setElementProperty(template.getID(), PersonalEmanation.PROPERTY_Y, (int) p.getY());
				template.setProperty(PersonalEmanation.PROPERTY_X, (int) p.getX());
				template.setProperty(PersonalEmanation.PROPERTY_Y, (int) p.getY());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		Point2D getTargetLocation() {
			return new Point((Integer) template.getProperty(PersonalEmanation.PROPERTY_X),
					(Integer) template.getProperty(PersonalEmanation.PROPERTY_Y));
		}
	};
}

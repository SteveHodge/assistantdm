package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import digital_table.server.TableDisplay;
import digital_table.elements.MapImage;

@SuppressWarnings("serial")
public class ImageOptionsPanel extends OptionsPanel {
	MapImage image;
	//JTextField filenameField;
	JTextField xField;
	JTextField yField;
	JTextField widthField;
	JTextField heightField;
	JSlider alphaSlider;
	JTextField labelField;
	JComboBox rotationsCombo;
	JCheckBox snapCheck;

	public ImageOptionsPanel(MapImage img, TableDisplay r) {
		super(r);
		image = img;
		image.addPropertyChangeListener(listener);

		xField = createDoubleControl(image, MapImage.PROPERTY_X);
		yField = createDoubleControl(image, MapImage.PROPERTY_Y);
		widthField = createDoubleControl(image, MapImage.PROPERTY_WIDTH);
		heightField = createDoubleControl(image, MapImage.PROPERTY_HEIGHT);
		alphaSlider = createSliderControl(image, MapImage.PROPERTY_ALPHA);
		
		String[] options = {"0","90","180","270"};
		rotationsCombo = new JComboBox(options);
		rotationsCombo.setSelectedIndex(image.getRotations());
		rotationsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JComboBox combo = (JComboBox)e.getSource();
					int index = combo.getSelectedIndex();
					image.setRotations(index);
					remote.setElementProperty(image.getID(), MapImage.PROPERTY_ROTATIONS, index);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});

		labelField = createLocalStringControl(image, MapImage.PROPERTY_LABEL);
		JCheckBox visibleCheck = createVisibilityControl(image);
		
		snapCheck = new JCheckBox("snap to grid?");
		snapCheck.setSelected(true);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy = 1; add(new JLabel("Left edge column:"), c);
		c.gridy = 2; add(new JLabel("Top edge Row:"), c);
		c.gridy = 3; add(new JLabel("Width:"), c);
		c.gridy = 4; add(new JLabel("Height:"), c);
		c.gridy = 5; add(new JLabel("Rotation:"), c);
		c.gridy = 6; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy = 1; add(xField, c);
		c.gridy = 2; add(yField, c);
		c.gridy = 3; add(widthField, c);
		c.gridy = 4; add(heightField, c);
		c.gridy = 5; add(rotationsCombo, c);
		c.gridy = 6; add(alphaSlider, c);
		c.gridy = 7; add(snapCheck, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapImage.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(MapImage.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_WIDTH)) {
				widthField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(MapImage.PROPERTY_HEIGHT)) {
				heightField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer)e.getNewValue());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	public DragMode getDragMode() {
		return DragMode.MOVE;
	}

	public Object getDragTarget(Point2D gridLocation) {
		return "IMAGE";
	}

	public Point2D getLocation(Object target) {
		if (target.equals("IMAGE")) {
			return new Point2D.Double(image.getX(), image.getY());
		}
		return null;
	}

	public void setLocation(Object target, Point2D p) {
		double x = p.getX();
		double y = p.getY();
		if (snapCheck.isSelected()) {
			x = Math.floor(x);
			y = Math.floor(y);
		}
		if (target.equals("IMAGE")) {
			try {
				remote.setElementProperty(image.getID(), MapImage.PROPERTY_X, x);
				remote.setElementProperty(image.getID(), MapImage.PROPERTY_Y, y);
				image.setX(x);
				image.setY(y);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}

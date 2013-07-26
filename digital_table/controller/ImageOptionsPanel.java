package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import digital_table.elements.MapElement;
import digital_table.elements.MapImage;
import digital_table.server.TableDisplay;

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

	public ImageOptionsPanel(File f, MapElement parent, TableDisplay r) {
		super(r);
		byte[] bytes = new byte[(int) f.length()];
		try {
			FileInputStream stream;
			stream = new FileInputStream(f);
			stream.read(bytes);
		} catch (FileNotFoundException e) {
			// TODO handle exceptions correctly
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		image = new MapImage(bytes, f.getName());
		sendElement(image, parent);
		image.setProperty(MapElement.PROPERTY_VISIBLE, true);
		image.addPropertyChangeListener(listener);

		xField = createDoubleControl(image, MapImage.PROPERTY_X);
		yField = createDoubleControl(image, MapImage.PROPERTY_Y);
		widthField = createDoubleControl(image, MapImage.PROPERTY_WIDTH);
		heightField = createDoubleControl(image, MapImage.PROPERTY_HEIGHT);
		alphaSlider = createSliderControl(image, MapImage.PROPERTY_ALPHA);
		rotationsCombo = createRotationControl(image, MapImage.PROPERTY_ROTATIONS, Mode.BOTH);
		labelField = createStringControl(image, MapImage.PROPERTY_LABEL, Mode.LOCAL);
		JCheckBox visibleCheck = createVisibilityControl(image);

		snapCheck = new JCheckBox("snap to grid?");
		snapCheck.setSelected(true);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Left edge column:"), c);
		c.gridy++; add(new JLabel("Top edge Row:"), c);
		c.gridy++; add(new JLabel("Width:"), c);
		c.gridy++; add(new JLabel("Height:"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(widthField, c);
		c.gridy++; add(heightField, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(alphaSlider, c);
		c.gridy++; add(snapCheck, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	@Override
	public MapImage getElement() {
		return image;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
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
			double x = p.getX();
			double y = p.getY();
			if (snapCheck.isSelected()) {
				x = Math.floor(x);
				y = Math.floor(y);
			}
			setRemote(image.getID(), MapImage.PROPERTY_X, x);
			setRemote(image.getID(), MapImage.PROPERTY_Y, y);
			image.setProperty(MapImage.PROPERTY_X, x);
			image.setProperty(MapImage.PROPERTY_Y, y);
		}

		@Override
		Point2D getTargetLocation() {
			return new Point2D.Double((Double)image.getProperty(MapImage.PROPERTY_X),
					(Double)image.getProperty(MapImage.PROPERTY_Y));
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;

			Point p = new Point((int)gridloc.getX(), (int)gridloc.getY());
			boolean clear = !image.isCleared(p);
			image.setCleared(p, clear);
			if (clear) {
				setRemote(image.getID(), MapImage.PROPERTY_CLEARCELL, p);
			} else {
				setRemote(image.getID(), MapImage.PROPERTY_UNCLEARCELL, p);
			}
		}
	};
}

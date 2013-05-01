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

import digital_table.elements.Label;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class LabelOptionsPanel extends OptionsPanel {
	private Label label;

	private JTextField xField;
	private JTextField yField;
	private JSlider alphaSlider;
	private JComboBox rotationsCombo;
	private JPanel colorPanel;
	private JPanel bgColorPanel;
	private JCheckBox bgCheck;
	private JTextField fontSizeField;
	private JTextField textField;

	public LabelOptionsPanel(Label l, TableDisplay r) {
		super(r);
		label = l;
		label.addPropertyChangeListener(listener);

		xField = createDoubleControl(label, Label.PROPERTY_X);
		yField = createDoubleControl(label, Label.PROPERTY_Y);
		alphaSlider = createSliderControl(label, Label.PROPERTY_ALPHA);
		colorPanel = createColorControl(label, Label.PROPERTY_COLOR);
		bgColorPanel = createColorControl(label, Label.PROPERTY_BACKGROUND_COLOR);
		rotationsCombo = createRotationControl(label, Label.PROPERTY_ROTATIONS, Mode.BOTH);
		JCheckBox visibleCheck = createVisibilityControl(label);
		bgCheck = createCheckBox(label, Label.PROPERTY_SOLID_BACKGROUND, Mode.BOTH, "show background?");
		fontSizeField = createDoubleControl(label, Label.PROPERTY_FONT_SIZE);
		textField = createStringControl(label, Label.PROPERTY_TEXT);

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1; add(new JLabel("Label:"), c);
		c.gridy++; add(new JLabel("Left edge column:"), c);
		c.gridy++; add(new JLabel("Top edge Row:"), c);
		c.gridy++; add(new JLabel("Font size (in grid cells):"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Background:"), c);
		c.gridy++;
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(textField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(fontSizeField, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(bgColorPanel, c);
		c.gridy++; add(bgCheck, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		add(new JPanel(), c);
		//@formatter:on
	}

	@Override
	public Label getElement() {
		return label;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Label.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int) (100 * (Float) e.getNewValue()));

			} else if (e.getPropertyName().equals(Label.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Label.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Label.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer) e.getNewValue());

			} else if (e.getPropertyName().equals(Label.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(Label.PROPERTY_BACKGROUND_COLOR)) {
				bgColorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(Label.PROPERTY_SOLID_BACKGROUND)) {
				bgCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(Label.PROPERTY_FONT_SIZE)) {
				fontSizeField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Label.PROPERTY_TEXT)) {
				textField.setText(e.getNewValue().toString());

			} else {
				System.out.println(toString() + ": Unknown property: " + e.getPropertyName());
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
				remote.setElementProperty(label.getID(), Label.PROPERTY_X, p.getX());
				remote.setElementProperty(label.getID(), Label.PROPERTY_Y, p.getY());
				label.setProperty(Label.PROPERTY_X, p.getX());
				label.setProperty(Label.PROPERTY_Y, p.getY());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		Point2D getTargetLocation() {
			return new Point2D.Double((Double) label.getProperty(Label.PROPERTY_X),
					(Double) label.getProperty(Label.PROPERTY_Y));
		}
	};
}

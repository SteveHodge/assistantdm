package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import digital_table.elements.Group;
import digital_table.elements.Token;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class GroupOptionsPanel extends OptionsPanel {
	private Group group;

	//	private JTextField xField;
	//	private JTextField yField;
	//	private JSlider alphaSlider;
	//	private JComboBox rotationsCombo;
	//	private JPanel colorPanel;
	private JTextField labelField;

	public GroupOptionsPanel(Group g, TableDisplay r) {
		super(r);
		group = g;
		group.addPropertyChangeListener(listener);

		//		xField = createDoubleControl(group, Label.PROPERTY_X);
		//		yField = createDoubleControl(group, Label.PROPERTY_Y);
		//		alphaSlider = createSliderControl(group, Label.PROPERTY_ALPHA);
		//		colorPanel = createColorControl(group, Label.PROPERTY_COLOR);
		//		bgColorPanel = createColorControl(group, Label.PROPERTY_BACKGROUND_COLOR);
		//		rotationsCombo = createRotationControl(group, Label.PROPERTY_ROTATIONS, Mode.BOTH);
		JCheckBox visibleCheck = createVisibilityControl(group);
		labelField = createStringControl(group, Token.PROPERTY_LABEL);

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		add(visibleCheck, c);
//		c.gridy = 1; add(new JLabel("Label:"), c);
//		c.gridy++; add(new JLabel("Left edge column:"), c);
//		c.gridy++; add(new JLabel("Top edge Row:"), c);
//		c.gridy++; add(new JLabel("Rotation:"), c);
//		c.gridy++; add(new JLabel("Colour:"), c);
//		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
//		c.gridy++; add(xField, c);
//		c.gridy++; add(yField, c);
//		c.gridy++; add(rotationsCombo, c);
//		c.gridy++; add(colorPanel, c);
//		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		add(new JPanel(), c);
		//@formatter:on
	}

	@Override
	public Group getElement() {
		return group;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			//			if (e.getPropertyName().equals(Label.PROPERTY_ALPHA)) {
			//				alphaSlider.setValue((int) (100 * (Float) e.getNewValue()));
			//
			//			} else if (e.getPropertyName().equals(Label.PROPERTY_X)) {
			//				xField.setText(e.getNewValue().toString());
			//
			//			} else if (e.getPropertyName().equals(Label.PROPERTY_Y)) {
			//				yField.setText(e.getNewValue().toString());
			//
			//			} else if (e.getPropertyName().equals(Label.PROPERTY_ROTATIONS)) {
			//				rotationsCombo.setSelectedIndex((Integer) e.getNewValue());
			//
			//			} else if (e.getPropertyName().equals(Label.PROPERTY_COLOR)) {
			//				colorPanel.setBackground((Color) e.getNewValue());
			//			} else 
			if (e.getPropertyName().equals(Group.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else {
				System.out.println(toString() + ": Unknown property: " + e.getPropertyName());
			}
		}
	};
	/*
		@Override
		public MapElementMouseListener getMouseListener() {
			return mouseListener;
		}

		protected MapElementMouseListener mouseListener = new DefaultDragger() {
			@Override
			protected String getDragTarget(Point2D gridLocation) {
				return "location";
			}

			@Override
			public void setTargetLocation(Point2D p) {
				try {
					remote.setElementProperty(group.getID(), Label.PROPERTY_X, p.getX());
					remote.setElementProperty(group.getID(), Label.PROPERTY_Y, p.getY());
					group.setProperty(Label.PROPERTY_X, p.getX());
					group.setProperty(Label.PROPERTY_Y, p.getY());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			@Override
			protected Point2D getTargetLocation() {
				return new Point2D.Double((Double) group.getProperty(Label.PROPERTY_X),
						(Double) group.getProperty(Label.PROPERTY_Y));
			}
		};*/
}

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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import digital_table.elements.MapImage;
import digital_table.elements.Token;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class TokenOptionsPanel extends OptionsPanel {
	Token token;
	JTextField xField;
	JTextField yField;
	JComboBox rotationsCombo;
	JPanel colorPanel;
	JTextField labelField;
	JSlider alphaSlider;
	JComboBox sizeCombo;
	
	public TokenOptionsPanel(Token t, TableDisplay r) {
		super(r);
		token = t;
		token.addPropertyChangeListener(listener);

		File f = new File("D:/Programming/Workspace/AssistantDM/html/monsters/images/MM35_gallery/MM35_PG203.jpg");
		byte bytes[] = new byte[(int)f.length()];
		try {
			FileInputStream stream = new FileInputStream(f);
			stream.read(bytes);
			remote.setElementProperty(token.getID(), Token.PROPERTY_IMAGE, bytes);
			token.setImage(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		xField = createIntegerControl(token, Token.PROPERTY_X);
		yField = createIntegerControl(token, Token.PROPERTY_Y);
		colorPanel = createColorControl(token, Token.PROPERTY_COLOR);
		alphaSlider = createSliderControl(token, Token.PROPERTY_ALPHA);
		sizeCombo = createComboControl(token, Token.PROPERTY_SIZE, Token.Size.values());

		String[] options = {"0","90","180","270"};
		rotationsCombo = new JComboBox(options);
		rotationsCombo.setSelectedIndex(token.getRotations());
		rotationsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JComboBox combo = (JComboBox)e.getSource();
					int index = combo.getSelectedIndex();
					token.setRotations(index);
					remote.setElementProperty(token.getID(), Token.PROPERTY_ROTATIONS, index);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});

		labelField = this.createLocalStringControl(token, Token.PROPERTY_LABEL);
		JCheckBox visibleCheck = createVisibilityControl(token);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Column:"), c);
		c.gridy++; add(new JLabel("Row:"), c);
		c.gridy++; add(new JLabel("Size:"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(sizeCombo, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 8; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	public Token getElement() {
		return token;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Token.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));
				
			} else if (e.getPropertyName().equals(Token.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());
				
			} else if (e.getPropertyName().equals(Token.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(Token.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(Token.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(Token.PROPERTY_SIZE)) {
				sizeCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(MapImage.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer)e.getNewValue());

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
				remote.setElementProperty(token.getID(), Token.PROPERTY_X, (int)p.getX());
				remote.setElementProperty(token.getID(), Token.PROPERTY_Y, (int)p.getY());
				token.setX((int)p.getX());
				token.setY((int)p.getY());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		protected Point2D getTargetLocation() {
			return new Point(token.getX(),token.getY());
		}
	};
}

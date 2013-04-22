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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

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
	JCheckBox reachWeapon;
	JCheckBox remoteReach;
	JCheckBox localReach;
	
	static File imageFile = null;	// last selected image - used to keep the current directory
	
	public TokenOptionsPanel(Token t, TableDisplay r) {
		super(r);
		token = t;
		token.addPropertyChangeListener(listener);

		xField = createIntegerControl(token, Token.PROPERTY_X);
		yField = createIntegerControl(token, Token.PROPERTY_Y);
		colorPanel = createColorControl(token, Token.PROPERTY_COLOR);
		alphaSlider = createSliderControl(token, Token.PROPERTY_ALPHA);
		sizeCombo = createComboControl(token, Token.PROPERTY_SIZE, Token.Size.values());
		rotationsCombo = createRotationControl(token, Token.PROPERTY_ROTATIONS, Mode.BOTH);
		labelField = createStringControl(token, Token.PROPERTY_LABEL, Mode.LOCAL);
		JCheckBox visibleCheck = createVisibilityControl(token);
		localReach = createCheckBox(token, Token.PROPERTY_SHOWREACH, Mode.LOCAL, "local");
		remoteReach = createCheckBox(token, Token.PROPERTY_SHOWREACH, Mode.REMOTE, "remote");
		reachWeapon = createCheckBox(token, Token.PROPERTY_REACHWEAPON, Mode.BOTH, "Reach weapon?");
		
		JButton imageButton = new JButton("Set Image");
		imageButton.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();

			public void actionPerformed(ActionEvent arg0) {
				if (imageFile != null) chooser.setCurrentDirectory(imageFile);
				if (chooser.showOpenDialog(TokenOptionsPanel.this) == JFileChooser.APPROVE_OPTION) {
					imageFile = chooser.getSelectedFile();
					byte bytes[] = new byte[(int)imageFile.length()];
					try {
						FileInputStream stream = new FileInputStream(imageFile);
						stream.read(bytes);
						remote.setElementProperty(token.getID(), Token.PROPERTY_IMAGE, bytes);
						token.setImage(imageFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Cancelled");
				}
			}
		});
		
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
		c.gridy++; add(new JLabel("Show reach:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(xField, c);
		c.gridy++; add(yField, c);
		c.gridy++; add(sizeCombo, c);
		c.gridy++; add(rotationsCombo, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);
		JPanel p = new JPanel();
		p.add(localReach);
		p.add(remoteReach);
		p.add(reachWeapon);
		c.gridy++; add(p, c);
		c.gridy++; add(imageButton, c); 

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
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

			} else if (e.getPropertyName().equals(Token.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer)e.getNewValue());

			} else if (e.getPropertyName().equals(Token.PROPERTY_SHOWREACH)) {
				localReach.setSelected((Boolean)e.getNewValue());

			} else if (e.getPropertyName().equals(Token.PROPERTY_REACHWEAPON)) {
				reachWeapon.setSelected((Boolean)e.getNewValue());

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
				token.setProperty(Token.PROPERTY_X, (int)p.getX());
				token.setProperty(Token.PROPERTY_Y, (int)p.getY());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		protected Point2D getTargetLocation() {
			return new Point((Integer)token.getProperty(Token.PROPERTY_X),(Integer)token.getProperty(Token.PROPERTY_Y));
		}
	};
}

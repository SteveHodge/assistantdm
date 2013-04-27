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

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import party.Creature;

import combat.CombatPanel;
import combat.InitiativeListModel;

import digital_table.elements.Token;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class TokenOptionsPanel extends OptionsPanel {
	private Token token;
	private JTextField xField;
	private JTextField yField;
	private JComboBox rotationsCombo;
	private JPanel colorPanel;
	private JTextField labelField;
	private JTextField remoteLabelField;
	private JSlider alphaSlider;
	private JComboBox sizeCombo;
	private JCheckBox reachWeapon;
	private JCheckBox remoteReach;
	private JCheckBox localReach;
	private JComboBox creatureCombo;
	private JTextField maxHPsField;
	private JTextField currentHPsField;
	private JComboBox statusCombo;
	private JComboBox statusDisplayCombo;

	private static File imageFile = new File(".");	// last selected image - used to keep the current directory

	private class CreatureListModel implements ComboBoxModel {
		InitiativeListModel list;
		Creature selected = null;
		EventListenerList listeners = new EventListenerList();

		CreatureListModel(InitiativeListModel m) {
			list = m;
			list.addListDataListener(new ListDataListener() {
				@Override
				public void contentsChanged(ListDataEvent e) {
					fireListDataEvent(new ListDataEvent(this, e.getType(), e.getIndex0(), e.getIndex1()));
				}

				@Override
				public void intervalAdded(ListDataEvent e) {
					fireListDataEvent(new ListDataEvent(this, e.getType(), e.getIndex0(), e.getIndex1()));
				}

				@Override
				public void intervalRemoved(ListDataEvent e) {
					fireListDataEvent(new ListDataEvent(this, e.getType(), e.getIndex0(), e.getIndex1()));
				}

			});
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(ListDataListener.class, l);
		}

		@Override
		public Creature getElementAt(int index) {
			return list.getElementAt(index).getSource();
		}

		@Override
		public int getSize() {
			return list.getSize();
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(ListDataListener.class, l);
		}

		@Override
		public Creature getSelectedItem() {
			return selected;
		}

		@Override
		public void setSelectedItem(Object sel) {
			if (sel == selected) return;
			if (selected != null) {
				selected.removePropertyChangeListener(propListener);
			}
			selected = (Creature) sel;
			if (selected == null) {
				maxHPsField.setText("");
				currentHPsField.setText("");
				labelField.setText("");
				token.setProperty(Token.PROPERTY_LABEL, "");
				try {
					remote.setElementProperty(token.getID(), Token.PROPERTY_LABEL, "");
					remoteLabelField.setText("");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				selected.addPropertyChangeListener(propListener);
				updateHPs();
				labelField.setText(selected.getName());
				token.setProperty(Token.PROPERTY_LABEL, selected.getName());
				try {
					remote.setElementProperty(token.getID(), Token.PROPERTY_LABEL, selected.getName());
					remoteLabelField.setText(selected.getName());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			fireListDataEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
		}

		private PropertyChangeListener propListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(Creature.PROPERTY_MAXHPS)) {
					updateHPs();
				} else if (evt.getPropertyName().equals(Creature.PROPERTY_WOUNDS)
						|| evt.getPropertyName().equals(Creature.PROPERTY_NONLETHAL)) {
					updateHPs();
				}
			}
		};

		private void updateHPs() {
			int max = selected.getMaximumHitPoints();
			maxHPsField.setText("" + max);
			currentHPsField.setText("" + (max - selected.getWounds() - selected.getNonLethal()));
		}

		private void fireListDataEvent(ListDataEvent e) {
			Object[] l = listeners.getListenerList();
			for (int i = l.length - 2; i >= 0; i -= 2) {
				if (l[i] == ListDataListener.class) {
					switch (e.getType()) {
					case ListDataEvent.CONTENTS_CHANGED:
						((ListDataListener) l[i + 1]).contentsChanged(e);
						break;
					case ListDataEvent.INTERVAL_ADDED:
						((ListDataListener) l[i + 1]).intervalAdded(e);
						break;
					case ListDataEvent.INTERVAL_REMOVED:
						((ListDataListener) l[i + 1]).intervalRemoved(e);
						break;
					}
				}
			}
		}
	}

	TokenOptionsPanel(Token t, TableDisplay r) {
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
		remoteLabelField = createStringControl(token, Token.PROPERTY_LABEL, Mode.REMOTE);
		JCheckBox visibleCheck = createVisibilityControl(token);
		localReach = createCheckBox(token, Token.PROPERTY_SHOWREACH, Mode.LOCAL, "local");
		remoteReach = createCheckBox(token, Token.PROPERTY_SHOWREACH, Mode.REMOTE, "remote");
		reachWeapon = createCheckBox(token, Token.PROPERTY_REACHWEAPON, Mode.BOTH, "Reach weapon?");
		maxHPsField = createNullableIntegerControl(token, Token.PROPERTY_MAX_HPS, Mode.BOTH);
		currentHPsField = createNullableIntegerControl(token, Token.PROPERTY_CURRENT_HPS, Mode.BOTH);
		if (CombatPanel.getCombatPanel() != null) {
			ComboBoxModel m = new CreatureListModel(CombatPanel.getCombatPanel().getInitiativeListModel());
			creatureCombo = new JComboBox(m);
		}
		statusCombo = createComboControl(token, Token.PROPERTY_STATUS_TYPE, Token.StatusType.values());
		statusDisplayCombo = createComboControl(token, Token.PROPERTY_STATUS_DISPLAY, Token.StatusDisplay.values());

		JButton imageButton = new JButton("Set Image");
		imageButton.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (imageFile != null) chooser.setCurrentDirectory(imageFile);
				if (chooser.showOpenDialog(TokenOptionsPanel.this) == JFileChooser.APPROVE_OPTION) {
					imageFile = chooser.getSelectedFile();
					byte bytes[] = new byte[(int) imageFile.length()];
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

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(new JLabel("Remote Label:"), c);
		if (CombatPanel.getCombatPanel() != null) {
			c.gridy++; add(new JLabel("For:"), c);
		}
		c.gridy++; add(new JLabel("Status Display:"), c);
		c.gridy++; add(new JLabel("Status Level:"), c);
		c.gridy++; add(new JLabel("Max HPs:"), c);
		c.gridy++; add(new JLabel("Current HPs:"), c);
		c.gridy++; add(new JLabel("Column:"), c);
		c.gridy++; add(new JLabel("Row:"), c);
		c.gridy++; add(new JLabel("Size:"), c);
		c.gridy++; add(new JLabel("Rotation:"), c);
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);
		c.gridy++; add(new JLabel("Show reach:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(labelField, c);
		c.gridy++; add(remoteLabelField, c);
		if (CombatPanel.getCombatPanel() != null) {
			c.gridy++; add(creatureCombo, c);
		}
		c.gridy++; add(statusDisplayCombo, c);
		c.gridy++; add(statusCombo, c);
		c.gridy++; add(maxHPsField, c);
		c.gridy++; add(currentHPsField, c);
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
		c.gridy++;
		add(p, c);
		c.gridy++;
		add(imageButton, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		add(new JPanel(), c);
		//@formatter:on
	}

	@Override
	public Token getElement() {
		return token;
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Token.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int) (100 * (Float) e.getNewValue()));

			} else if (e.getPropertyName().equals(Token.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color) e.getNewValue());

			} else if (e.getPropertyName().equals(Token.PROPERTY_LABEL)) {
				labelField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Token.PROPERTY_X)) {
				xField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Token.PROPERTY_Y)) {
				yField.setText(e.getNewValue().toString());

			} else if (e.getPropertyName().equals(Token.PROPERTY_SIZE)) {
				sizeCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(Token.PROPERTY_ROTATIONS)) {
				rotationsCombo.setSelectedIndex((Integer) e.getNewValue());

			} else if (e.getPropertyName().equals(Token.PROPERTY_SHOWREACH)) {
				localReach.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(Token.PROPERTY_REACHWEAPON)) {
				reachWeapon.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(Token.PROPERTY_CURRENT_HPS)) {
				String text = "";
				if (e.getNewValue() != null) text = e.getNewValue().toString();
				currentHPsField.setText(text);

			} else if (e.getPropertyName().equals(Token.PROPERTY_MAX_HPS)) {
				String text = "";
				if (e.getNewValue() != null) text = e.getNewValue().toString();
				maxHPsField.setText(text);

			} else if (e.getPropertyName().equals(Token.PROPERTY_STATUS_TYPE)) {
				statusCombo.setSelectedItem(e.getNewValue());

			} else if (e.getPropertyName().equals(Token.PROPERTY_STATUS_DISPLAY)) {
				statusDisplayCombo.setSelectedItem(e.getNewValue());

			} else {
				System.out.println(toString() + ": Unknown property: " + e.getPropertyName());
			}
		}
	};

	@Override
	public MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	private MapElementMouseListener mouseListener = new DefaultDragger() {
		@Override
		protected String getDragTarget(Point2D gridLocation) {
			return "location";
		}

		@Override
		public void setTargetLocation(Point2D p) {
			try {
				remote.setElementProperty(token.getID(), Token.PROPERTY_X, (int) p.getX());
				remote.setElementProperty(token.getID(), Token.PROPERTY_Y, (int) p.getY());
				token.setProperty(Token.PROPERTY_X, (int) p.getX());
				token.setProperty(Token.PROPERTY_Y, (int) p.getY());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected Point2D getTargetLocation() {
			return new Point((Integer) token.getProperty(Token.PROPERTY_X), (Integer) token.getProperty(Token.PROPERTY_Y));
		}
	};
}

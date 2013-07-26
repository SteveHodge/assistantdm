package digital_table.controller;

import gamesystem.SizeCategory;

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

import digital_table.elements.MapElement;
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
	private JTextField spaceField;
	private JTextField reachField;
	private JCheckBox reachWeapon;
	private JCheckBox remoteReach;
	private JCheckBox localReach;
	private JComboBox creatureCombo;
	private JTextField maxHPsField;
	private JTextField currentHPsField;
	private JComboBox statusCombo;
	private JComboBox statusDisplayCombo;
	private Creature creature;

	// TODO shouldn't be public
	public static File imageFile = new File(".");	// last selected image - used to keep the current directory

	TokenOptionsPanel(MapElement parent, TableDisplay r) {
		super(r);
		token = new Token();
		sendElement(token, parent);
		token.setProperty(MapElement.PROPERTY_VISIBLE, true);
		token.addPropertyChangeListener(listener);

		xField = createIntegerControl(token, Token.PROPERTY_X);
		yField = createIntegerControl(token, Token.PROPERTY_Y);
		colorPanel = createColorControl(token, Token.PROPERTY_COLOR);
		alphaSlider = createSliderControl(token, Token.PROPERTY_ALPHA);
		reachField = createIntegerControl(token, Token.PROPERTY_REACH);
		rotationsCombo = createRotationControl(token, Token.PROPERTY_ROTATIONS, Mode.BOTH);
		labelField = createStringControl(token, Token.PROPERTY_LABEL, Mode.LOCAL);
		remoteLabelField = createStringControl(token, Token.PROPERTY_LABEL, Mode.REMOTE);
		JCheckBox visibleCheck = createVisibilityControl(token);
		localReach = createCheckBox(token, Token.PROPERTY_SHOWREACH, Mode.LOCAL, "local");
		remoteReach = createCheckBox(token, Token.PROPERTY_SHOWREACH, Mode.REMOTE, "remote");
		reachWeapon = createCheckBox(token, Token.PROPERTY_REACHWEAPON, Mode.BOTH, "Reach weapon?");
		maxHPsField = createNullableIntegerControl(token, Token.PROPERTY_MAX_HPS, Mode.BOTH);
		currentHPsField = createNullableIntegerControl(token, Token.PROPERTY_CURRENT_HPS, Mode.BOTH);
		statusCombo = createComboControl(token, Token.PROPERTY_STATUS_TYPE, Token.StatusType.values());
		statusDisplayCombo = createComboControl(token, Token.PROPERTY_STATUS_DISPLAY, Token.StatusDisplay.values());

		sizeCombo = new JComboBox(CreatureSize.values());
		sizeCombo.setSelectedItem(CreatureSize.MEDIUM);
		sizeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				CreatureSize selected = (CreatureSize) combo.getSelectedItem();
				setRemote(token.getID(), Token.PROPERTY_SPACE, selected.getSpace());
				setRemote(token.getID(), Token.PROPERTY_REACH, selected.getReach());
				token.setProperty(Token.PROPERTY_SPACE, selected.getSpace());
				token.setProperty(Token.PROPERTY_REACH, selected.getReach());
			}
		});

		spaceField = new JTextField(8);
		int space = (Integer) token.getProperty(Token.PROPERTY_SPACE);
		spaceField.setText("" + ((float) space) / 2);
		spaceField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int newSpace = (int) (Double.parseDouble(spaceField.getText()) * 2);
				setRemote(token.getID(), Token.PROPERTY_SPACE, newSpace);
				token.setProperty(Token.PROPERTY_SPACE, newSpace);
			}
		});

		if (CombatPanel.getCombatPanel() != null) {
			ComboBoxModel m = new CreatureListModel(CombatPanel.getCombatPanel().getInitiativeListModel());
			creatureCombo = new JComboBox(m);
		}

		JButton imageButton = new JButton("Set Image");
		imageButton.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (imageFile != null) chooser.setCurrentDirectory(imageFile);
				if (chooser.showOpenDialog(TokenOptionsPanel.this) == JFileChooser.APPROVE_OPTION) {
					setImage(chooser.getSelectedFile());
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
		c.gridy++; add(new JLabel("Space:"), c);
		c.gridy++; add(new JLabel("Reach:"), c);
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
		c.gridy++; add(spaceField, c);
		c.gridy++; add(reachField, c);
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

	void setImage(File f) {
		imageFile = f;
		byte bytes[];
		if (f == null) {
			bytes = null;
		} else {
			bytes = new byte[(int) imageFile.length()];
			try {
				FileInputStream stream = new FileInputStream(imageFile);
				stream.read(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		setRemote(token.getID(), Token.PROPERTY_IMAGE, bytes);
		token.setImage(imageFile);
	}

	void setCreature(Creature c) {
		if (creature == c) return;
		if (creature != null) {
			creature.removePropertyChangeListener(propListener);
		}
		creature = c;
		if (creature == null) {
			setRemote(token.getID(), Token.PROPERTY_MAX_HPS, 0);
			setRemote(token.getID(), Token.PROPERTY_CURRENT_HPS, 0);
			setRemote(token.getID(), Token.PROPERTY_LABEL, "");
			token.setProperty(Token.PROPERTY_MAX_HPS, 0);
			token.setProperty(Token.PROPERTY_CURRENT_HPS, 0);
			token.setProperty(Token.PROPERTY_LABEL, "");
			maxHPsField.setText("");
			currentHPsField.setText("");
			labelField.setText("");
			remoteLabelField.setText("");
		} else {
			creature.addPropertyChangeListener(propListener);
			updateHPs();

			spaceField.setText("" + ((float) creature.getSpace()) / 2);
			reachField.setText("" + creature.getReach());
			sizeCombo.setSelectedItem(CreatureSize.getSize(creature.getSize(), creature.getReach()));
			setRemote(token.getID(), Token.PROPERTY_SPACE, creature.getSpace());
			setRemote(token.getID(), Token.PROPERTY_REACH, creature.getReach());
			token.setProperty(Token.PROPERTY_SPACE, creature.getSpace());
			token.setProperty(Token.PROPERTY_REACH, creature.getReach());

			labelField.setText(creature.getName());
			token.setProperty(Token.PROPERTY_LABEL, creature.getName());
			setRemote(token.getID(), Token.PROPERTY_LABEL, creature.getName());
			remoteLabelField.setText(creature.getName());
		}

	}

	private PropertyChangeListener propListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(Creature.PROPERTY_MAXHPS)) {
				updateHPs();
			} else if (evt.getPropertyName().equals(Creature.PROPERTY_WOUNDS)
					|| evt.getPropertyName().equals(Creature.PROPERTY_NONLETHAL)) {
				updateHPs();
			} else {
				if (evt.getPropertyName().equals(Creature.PROPERTY_SIZE)) {
					sizeCombo.setSelectedItem(CreatureSize.getSize(creature.getSize(), creature.getReach()));
				}
				if (evt.getPropertyName().equals(Creature.PROPERTY_SPACE) || evt.getPropertyName().equals(Creature.PROPERTY_SIZE)) {
					spaceField.setText("" + ((float) creature.getSpace()) / 2);
					setRemote(token.getID(), Token.PROPERTY_SPACE, creature.getSpace());
					token.setProperty(Token.PROPERTY_SPACE, creature.getSpace());
				} else if (evt.getPropertyName().equals(Creature.PROPERTY_REACH) || evt.getPropertyName().equals(Creature.PROPERTY_SIZE)) {
					reachField.setText("" + creature.getReach());
					setRemote(token.getID(), Token.PROPERTY_REACH, creature.getReach());
					token.setProperty(Token.PROPERTY_REACH, creature.getReach());
				}
			}
		}
	};

	private void updateHPs() {
		if (creature == null) return;
		int max = creature.getMaximumHitPoints();
		int curr = max - creature.getWounds() - creature.getNonLethal();
		setRemote(token.getID(), Token.PROPERTY_MAX_HPS, max);
		setRemote(token.getID(), Token.PROPERTY_CURRENT_HPS, curr);
		token.setProperty(Token.PROPERTY_MAX_HPS, max);
		token.setProperty(Token.PROPERTY_CURRENT_HPS, curr);
		maxHPsField.setText("" + max);
		currentHPsField.setText("" + curr);
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

			} else if (e.getPropertyName().equals(Token.PROPERTY_LOCATION)) {
				Point2D p = (Point2D) e.getNewValue();
				xField.setText("" + (int) p.getX());
				yField.setText("" + (int) p.getY());

			} else if (e.getPropertyName().equals(Token.PROPERTY_SPACE)) {
				int space = (Integer) e.getNewValue();
				spaceField.setText("" + ((float) space) / 2);

			} else if (e.getPropertyName().equals(Token.PROPERTY_REACH)) {
				reachField.setText(e.getNewValue().toString());

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

	MapElementMouseListener mouseListener = new DefaultDragger() {
		@Override
		String getDragTarget(Point2D gridLocation) {
			return "location";
		}

		@Override
		void setTargetLocation(Point2D p) {
			setRemote(token.getID(), Token.PROPERTY_X, (int) p.getX());
			setRemote(token.getID(), Token.PROPERTY_Y, (int) p.getY());
			token.setProperty(Token.PROPERTY_X, (int) p.getX());
			token.setProperty(Token.PROPERTY_Y, (int) p.getY());
		}

		@Override
		Point2D getTargetLocation() {
			return new Point((Integer) token.getProperty(Token.PROPERTY_X), (Integer) token.getProperty(Token.PROPERTY_Y));
		}
	};

	private enum CreatureSize {
		FINE(SizeCategory.FINE, false),
		DIMINUTIVE(SizeCategory.DIMINUTIVE, false),
		TINY(SizeCategory.TINY, false),
		SMALL(SizeCategory.SMALL, false),
		MEDIUM(SizeCategory.MEDIUM, false),
		LARGE_LONG(SizeCategory.LARGE, true),
		LARGE_TALL(SizeCategory.LARGE, false),
		HUGE_LONG(SizeCategory.HUGE, true),
		HUGE_TALL(SizeCategory.HUGE, false),
		GARGANTUAN_LONG(SizeCategory.GARGANTUAN, true),
		GARGANTUAN_TALL(SizeCategory.GARGANTUAN, false),
		COLOSSAL_LONG(SizeCategory.COLOSSAL, true),
		COLOSSAL_TALL(SizeCategory.COLOSSAL, false);

		public int getSpace() {
			return size.getSpace();
		}

		public int getReach() {
			return isLong ? size.getReachLong() : size.getReachTall();
		}

		public static CreatureSize getSize(SizeCategory cat, int reach) {
			boolean l = reach > cat.getReachLong() ? false : true;
			CreatureSize best = null;
			for (CreatureSize s : values()) {
				if (s.size == cat) {
					if (best == null || s.isLong == l) {
						best = s;
					}
				}
			}
			return best;
		}

		@Override
		public String toString() {
			String d = size.toString();
			if (size.getReachLong() != size.getReachTall()) {
				d += isLong ? " (tall)" : " (long)";
			}
			return d;
		}

		private CreatureSize(SizeCategory s, boolean l) {
			size = s;
			isLong = l;
		}

		private SizeCategory size;
		private boolean isLong = false;
	}

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
			setCreature((Creature) sel);
			fireListDataEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
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
}

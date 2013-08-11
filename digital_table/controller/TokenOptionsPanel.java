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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import party.Creature;

import combat.CombatPanel;
import combat.InitiativeListModel;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.Token;

@SuppressWarnings("serial")
public class TokenOptionsPanel extends OptionsPanel {
	private Token token;
	private JTextField xField;
	private JTextField yField;
	private JComboBox rotationsCombo;
	private JPanel colorPanel;
	private JTextField labelField;
	private JTextField remoteLabelField;
	private JTextField webLabelField;
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
	private JCheckBox visibleCheck;
	private Creature creature;
	private File imageFile = null;

	// TODO shouldn't be public - default directories should be moved to a global config class
	public static File lastDir = new File(".");	// last selected image - used to keep the current directory

	TokenOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		token = new Token();
		display.addElement(token, parent);
		token.setProperty(MapElement.PROPERTY_VISIBLE, true);
		token.addPropertyChangeListener(listener);

		xField = createIntegerControl(token, Token.PROPERTY_X);
		yField = createIntegerControl(token, Token.PROPERTY_Y);
		colorPanel = createColorControl(token, Token.PROPERTY_COLOR);
		alphaSlider = createSliderControl(token, Token.PROPERTY_ALPHA);
		reachField = createIntegerControl(token, Token.PROPERTY_REACH);
		rotationsCombo = createRotationControl(token, Token.PROPERTY_ROTATIONS, Mode.ALL);
		labelField = createStringControl(token, Token.PROPERTY_LABEL, Mode.LOCAL);
		remoteLabelField = createStringControl(token, Token.PROPERTY_LABEL, Mode.REMOTE);
		visibleCheck = createVisibilityControl(token);
		localReach = createCheckBox(token, Token.PROPERTY_SHOWREACH, Mode.LOCAL, "local");
		remoteReach = createCheckBox(token, Token.PROPERTY_SHOWREACH, Mode.REMOTE, "remote");
		reachWeapon = createCheckBox(token, Token.PROPERTY_REACHWEAPON, Mode.ALL, "Reach weapon?");
		maxHPsField = createNullableIntegerControl(token, Token.PROPERTY_MAX_HPS, Mode.ALL);
		currentHPsField = createNullableIntegerControl(token, Token.PROPERTY_CURRENT_HPS, Mode.ALL);
		statusCombo = createComboControl(token, Token.PROPERTY_STATUS_TYPE, Token.StatusType.values());
		statusDisplayCombo = createComboControl(token, Token.PROPERTY_STATUS_DISPLAY, Token.StatusDisplay.values());

		webLabelField = new JTextField(30);
		webLabelField.setText("");
		webLabelField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				display.setProperty(token, TokenOverlay.PROPERTY_WEB_LABEL, webLabelField.getText(), Mode.OVERLAY);
			}
		});

		sizeCombo = new JComboBox(CreatureSize.values());
		sizeCombo.setSelectedItem(CreatureSize.MEDIUM);
		sizeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				CreatureSize selected = (CreatureSize) combo.getSelectedItem();
				display.setProperty(token, Token.PROPERTY_SPACE, selected.getSpace());
				display.setProperty(token, Token.PROPERTY_REACH, selected.getReach());
			}
		});

		spaceField = new JTextField(8);
		int space = (Integer) token.getProperty(Token.PROPERTY_SPACE);
		spaceField.setText("" + ((float) space) / 2);
		spaceField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int newSpace = (int) (Double.parseDouble(spaceField.getText()) * 2);
				display.setProperty(token, Token.PROPERTY_SPACE, newSpace);
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
				if (lastDir != null) chooser.setCurrentDirectory(lastDir);
				if (chooser.showOpenDialog(TokenOptionsPanel.this) == JFileChooser.APPROVE_OPTION) {
					setImage(chooser.getSelectedFile());
				} else {
					System.out.println("Cancelled");
				}
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		add(visibleCheck, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JLabel("Remote Label:"), c);
		add(new JLabel("Web Label:"), c);
		if (CombatPanel.getCombatPanel() != null) {
			add(new JLabel("For:"), c);
		}
		add(new JLabel("Status Display:"), c);
		add(new JLabel("Status Level:"), c);
		add(new JLabel("Max HPs:"), c);
		add(new JLabel("Current HPs:"), c);
		add(new JLabel("Column:"), c);
		add(new JLabel("Row:"), c);
		add(new JLabel("Size:"), c);
		add(new JLabel("Space:"), c);
		add(new JLabel("Reach:"), c);
		add(new JLabel("Rotation:"), c);
		add(new JLabel("Colour:"), c);
		add(new JLabel("Transparency:"), c);
		add(new JLabel("Show reach:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0;
		add(labelField, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(remoteLabelField, c);
		add(webLabelField, c);
		if (CombatPanel.getCombatPanel() != null) {
			add(creatureCombo, c);
		}
		add(statusDisplayCombo, c);
		add(statusCombo, c);
		add(maxHPsField, c);
		add(currentHPsField, c);
		add(xField, c);
		add(yField, c);
		add(sizeCombo, c);
		add(spaceField, c);
		add(reachField, c);
		add(rotationsCombo, c);
		add(colorPanel, c);
		add(alphaSlider, c);
		JPanel p = new JPanel();
		p.add(localReach);
		p.add(remoteReach);
		p.add(reachWeapon);
		add(p, c);
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
		lastDir = f;
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
		display.setProperty(token, Token.PROPERTY_IMAGE, bytes, Mode.REMOTE);
		token.setImage(imageFile);
	}

	void setCreature(Creature c) {
		if (creature == c) return;
		if (creature != null) {
			creature.removePropertyChangeListener(propListener);
		}
		creature = c;
		if (creature == null) {
			display.setProperty(token, Token.PROPERTY_MAX_HPS, 0);
			display.setProperty(token, Token.PROPERTY_CURRENT_HPS, 0);
			display.setProperty(token, Token.PROPERTY_LABEL, "");
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
			display.setProperty(token, Token.PROPERTY_SPACE, creature.getSpace());
			display.setProperty(token, Token.PROPERTY_REACH, creature.getReach());

			labelField.setText(creature.getName());
			display.setProperty(token, Token.PROPERTY_LABEL, creature.getName());
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
					display.setProperty(token, Token.PROPERTY_SPACE, creature.getSpace());
				} else if (evt.getPropertyName().equals(Creature.PROPERTY_REACH) || evt.getPropertyName().equals(Creature.PROPERTY_SIZE)) {
					reachField.setText("" + creature.getReach());
					display.setProperty(token, Token.PROPERTY_REACH, creature.getReach());
				}
			}
		}
	};

	private void updateHPs() {
		if (creature == null) return;
		int max = creature.getMaximumHitPoints();
		int curr = max - creature.getWounds() - creature.getNonLethal();
		display.setProperty(token, Token.PROPERTY_MAX_HPS, max);
		display.setProperty(token, Token.PROPERTY_CURRENT_HPS, curr);
		maxHPsField.setText("" + max);
		currentHPsField.setText("" + curr);
	}

	@Override
	Token getElement() {
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

	private MapElementMouseListener mouseListener = new DefaultDragger() {
		@Override
		String getDragTarget(Point2D gridLocation) {
			return "location";
		}

		@Override
		void setTargetLocation(Point2D p) {
			display.setProperty(token, Token.PROPERTY_X, (int) p.getX());
			display.setProperty(token, Token.PROPERTY_Y, (int) p.getY());
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

	// ---- XML serialisation methods ----
	final static String XML_TAG = "Token";
	private final static String FILE_ATTRIBUTE_NAME = "image_path";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected());
		setAttribute(e, REMOTE_PREFIX + Token.PROPERTY_LABEL, remoteLabelField.getText());
		if (webLabelField.getText().length() > 0) setAttribute(e, TokenOverlay.PROPERTY_WEB_LABEL, webLabelField.getText());
		setAttribute(e, REMOTE_PREFIX + Token.PROPERTY_SHOWREACH, remoteReach.isSelected());
		if (imageFile != null) setAttribute(e, FILE_ATTRIBUTE_NAME, imageFile.getPath());
		Point2D location = (Point2D) token.getProperty(Group.PROPERTY_LOCATION);
		e.setAttribute(Group.PROPERTY_LOCATION, location.getX() + "," + location.getY());	// maybe should output X and Y separately
		return e;
	}

	@Override
	void parseDOM(Element e) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseStringAttribute(Token.PROPERTY_LABEL, e, Mode.LOCAL);
		parseStringAttribute(Token.PROPERTY_LABEL, e, remoteLabelField);
		parseColorAttribute(Token.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(Token.PROPERTY_ALPHA, e, Mode.ALL);
		parseDoubleAttribute(Token.PROPERTY_X, e, Mode.ALL);
		parseDoubleAttribute(Token.PROPERTY_Y, e, Mode.ALL);
		parseIntegerAttribute(Token.PROPERTY_REACH, e, Mode.ALL);
		parseIntegerAttribute(Token.PROPERTY_SPACE, e, Mode.ALL);
		parseIntegerAttribute(Token.PROPERTY_ROTATIONS, e, Mode.ALL);
		parseBooleanAttribute(Token.PROPERTY_SHOWREACH, e, Mode.LOCAL);
		parseBooleanAttribute(Token.PROPERTY_SHOWREACH, e, remoteReach);
		parseBooleanAttribute(Token.PROPERTY_REACHWEAPON, e, Mode.ALL);
		parseIntegerAttribute(Token.PROPERTY_MAX_HPS, e, Mode.ALL);
		parseIntegerAttribute(Token.PROPERTY_CURRENT_HPS, e, Mode.ALL);
		parseEnumAttribute(Token.PROPERTY_STATUS_TYPE, Token.StatusType.class, e, Mode.ALL);
		parseEnumAttribute(Token.PROPERTY_STATUS_DISPLAY, Token.StatusDisplay.class, e, Mode.ALL);
		//parseEnumAttribute("size", CreatureSize.class, e, Mode.BOTH);
		//sizeCombo.setSelectedItem(CreatureSize.getSize(creature.getSize(), creature.getReach()));

		if (e.hasAttribute(FILE_ATTRIBUTE_NAME)) {
			setImage(new File(e.getAttribute(FILE_ATTRIBUTE_NAME)));
		}

		if (e.hasAttribute(Group.PROPERTY_LOCATION)) {
//			try {
			String coords[] = e.getAttribute(Group.PROPERTY_LOCATION).split(",");
			Double x = Double.parseDouble(coords[0]);
			Double y = Double.parseDouble(coords[1]);
			Point2D value = new Point2D.Double(x, y);
			display.setProperty(token, Group.PROPERTY_LOCATION, value);
//			} catch (NumberFormatException e) {
//			}
		}

		if (e.hasAttribute(TokenOverlay.PROPERTY_WEB_LABEL)) {
			String value = e.getAttribute(TokenOverlay.PROPERTY_WEB_LABEL);
			display.setProperty(getElement(), TokenOverlay.PROPERTY_WEB_LABEL, value, Mode.OVERLAY);
			webLabelField.setText(value);
		} else {
			webLabelField.setText("");
		}

		parseBooleanAttribute(MapElement.PROPERTY_VISIBLE, e, visibleCheck);
	}
}

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
import java.util.Map;

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

import combat.CombatEntry;
import combat.EncounterModule;
import combat.InitiativeListModel;
import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.Group;
import digital_table.elements.Label;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.Token;
import gamesystem.Creature;
import gamesystem.HPs;
import gamesystem.Size;
import gamesystem.SizeCategory;
import gamesystem.core.PropertyListener;
import gamesystem.core.SimpleProperty;
import util.ModuleRegistry;

@SuppressWarnings("serial")
public class TokenOptionsPanel extends OptionsPanel<Token> {
	private JTextField xField;
	private JTextField yField;
	private JComboBox<String> rotationsCombo;
	private JPanel colorPanel;
	private JTextField labelField;
	private JTextField remoteLabelField;
	private JTextField webLabelField;
	private JSlider alphaSlider;
	private JComboBox<CreatureSize> sizeCombo;
	private JTextField spaceField;
	private JTextField reachField;
	private JCheckBox reachWeapon;
	private JCheckBox remoteReach;
	private JCheckBox localReach;
	private JComboBox<Creature> creatureCombo;
	private JTextField maxHPsField;
	private JTextField currentHPsField;
	private JComboBox<Token.StatusType> statusCombo;
	private JComboBox<Token.StatusDisplay> statusDisplayCombo;
	private JCheckBox visibleCheck;
	private JCheckBox floatingLabelCheck;
	LabelOptionsPanel floatingLabel = null;	// accessed by LabelOptionsPanel
	private Creature creature = null;
	private File imageFile = null;
	private JButton deadButton = null;

	// TODO shouldn't be public - default directories should be moved to a global config class
	public static File lastDir = new File(".");	// last selected image - used to keep the current directory

	TokenOptionsPanel(MapElement parent, DisplayManager r, final ElementFactory<LabelOptionsPanel> labelFactory, final ControllerFrame frame) {
		super(r);
		element = new Token();
		if (parent == null) {
			element.setProperty(Group.PROPERTY_X, r.getXOffset());
			element.setProperty(Group.PROPERTY_Y, r.getYOffset());
		}
		display.addElement(element, parent);
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		element.addPropertyChangeListener(listener);

		visibleCheck = createVisibilityControl();
		alphaSlider = createSliderControl(Token.PROPERTY_ALPHA);
		xField = createIntegerControl(Group.PROPERTY_X);
		yField = createIntegerControl(Group.PROPERTY_Y);
		colorPanel = createColorControl(Token.PROPERTY_COLOR);
		reachField = createIntegerControl(Token.PROPERTY_REACH);
		labelField = createStringControl(Token.PROPERTY_LABEL, Mode.LOCAL);
		localReach = createCheckBox(Token.PROPERTY_SHOWREACH, Mode.LOCAL, "local");
		remoteReach = createCheckBox(Token.PROPERTY_SHOWREACH, Mode.REMOTE, "remote");
		reachWeapon = createCheckBox(Token.PROPERTY_REACHWEAPON, Mode.ALL, "Reach weapon?");
		maxHPsField = createHPControl(Token.PROPERTY_MAX_HPS);
		currentHPsField = createHPControl(Token.PROPERTY_CURRENT_HPS);
		statusCombo = createStatusControl(Token.PROPERTY_STATUS_TYPE, Token.StatusType.values());
		statusDisplayCombo = createStatusControl(Token.PROPERTY_STATUS_DISPLAY, Token.StatusDisplay.values());

		webLabelField = new JTextField(30);
		webLabelField.setText("");
		webLabelField.addActionListener(e -> display.setProperty(element, TokenOverlay.PROPERTY_WEB_LABEL, webLabelField.getText(), Mode.OVERLAY));

		sizeCombo = new JComboBox<>(CreatureSize.values());
		sizeCombo.setSelectedItem(CreatureSize.MEDIUM);
		sizeCombo.addActionListener(e -> {
			CreatureSize selected = sizeCombo.getItemAt(sizeCombo.getSelectedIndex());
			display.setProperty(element, Token.PROPERTY_SPACE, selected.getSpace());
			display.setProperty(element, Token.PROPERTY_REACH, selected.getReach());
		});

		spaceField = new JTextField(8);
		int space = (Integer) element.getProperty(Token.PROPERTY_SPACE);
		spaceField.setText("" + ((float) space) / 2);
		spaceField.addActionListener(e -> {
			int newSpace = (int) (Double.parseDouble(spaceField.getText()) * 2);
			display.setProperty(element, Token.PROPERTY_SPACE, newSpace);
		});

		EncounterModule enc = ModuleRegistry.getModule(EncounterModule.class);
		if (enc != null) {
			ComboBoxModel<Creature> m = new CreatureListModel(enc.getInitiativeListModel());
			creatureCombo = new JComboBox<>(m);
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

		deadButton = new JButton("Replace with corpse");
		deadButton.addActionListener(e -> {
			int x = ((Double) element.getProperty(Group.PROPERTY_X)).intValue();
			int y = ((Double) element.getProperty(Group.PROPERTY_Y)).intValue();
			int cells = ((Integer) element.getProperty(Token.PROPERTY_SPACE)) / 10;
			if (cells < 1) cells = 1;
			frame.replaceToken(TokenOptionsPanel.this, x, y, cells, cells, visibleCheck.isSelected());
		});

		floatingLabelCheck = new JCheckBox("Floating labels");
		floatingLabelCheck.addItemListener(e -> {
			JCheckBox check = (JCheckBox) e.getSource();
			if (check.isSelected()) {
				if (floatingLabel == null) {
					floatingLabel = labelFactory.addElement(element);
					floatingLabel.setFloating();
				}
				// configure the floating label
				Label l = floatingLabel.getElement();
				display.setProperty(l, Label.PROPERTY_TEXT, remoteLabelField.getText(), Mode.ALL);
				// default position is right under the token
				// TODO this should account for rotation
				double tokenSpace = Double.parseDouble(spaceField.getText()) / 5.0d;
				display.setProperty(l, Label.PROPERTY_Y, tokenSpace, Mode.ALL);
				display.setProperty(l, Label.PROPERTY_ROTATIONS, rotationsCombo.getSelectedIndex(), Mode.ALL);
				display.setProperty(l, MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE, Mode.REMOTE);
				// remove the label from the remote token
				display.setProperty(element, Token.PROPERTY_LABEL, "", Mode.REMOTE);
				updateFloatingStatus();
			} else if (floatingLabel != null) {
				// remove the floating label
				labelFactory.removeElement(floatingLabel);
				floatingLabel = null;
				// replace the existing label
				display.setProperty(element, Token.PROPERTY_LABEL, remoteLabelField.getText(), Mode.REMOTE);
			}
		});

		remoteLabelField = new JTextField(30);
		remoteLabelField.setText("" + element.getProperty(Token.PROPERTY_LABEL));
		remoteLabelField.addActionListener(e -> {
			if (floatingLabel == null) {
				display.setProperty(element, Token.PROPERTY_LABEL, remoteLabelField.getText(), Mode.REMOTE);
			} else {
				display.setProperty(floatingLabel.getElement(), Label.PROPERTY_TEXT, remoteLabelField.getText(), Mode.ALL);
			}
		});
		rotationsCombo = new JComboBox<>(options);
		rotationsCombo.setSelectedIndex((Integer) element.getProperty(Token.PROPERTY_ROTATIONS));
		rotationsCombo.addActionListener(e -> {
			int index = rotationsCombo.getSelectedIndex();
			display.setProperty(element, Token.PROPERTY_ROTATIONS, index, Mode.ALL);
			// TODO should reposition the floating label too
			if (floatingLabel != null) display.setProperty(floatingLabel.getElement(), Token.PROPERTY_ROTATIONS, index, Mode.ALL);
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		add(visibleCheck, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JLabel("Remote Label:"), c);
		add(new JLabel("Web Label:"), c);
		if (enc != null) {
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
		c.gridwidth = 2;
		add(labelField, c);
		c.gridy = GridBagConstraints.RELATIVE;
		add(remoteLabelField, c);
		c.gridwidth = 1;
		add(webLabelField, c);
		c.gridwidth = 2;
		if (enc != null) {
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
		p = new JPanel();
		p.add(imageButton);
		if (deadButton != null) p.add(deadButton);
		add(p, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridx = 0;
		c.gridwidth = 3;
		add(new JPanel(), c);

		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		add(floatingLabelCheck, c);
	}

	private JTextField createHPControl(final String property) {
		final JTextField field = new JTextField(8);
		if (element.getProperty(property) != null) field.setText("" + element.getProperty(property));
		field.addActionListener(e -> {
			Integer newValue = null;
			if (field.getText().length() > 0) newValue = Integer.parseInt(field.getText());
			display.setProperty(element, property, newValue, Mode.ALL);
			updateFloatingStatus();
		});
		return field;
	}

	private <T> JComboBox<T> createStatusControl(final String property, T[] values) {
		final JComboBox<T> typeCombo = new JComboBox<>(values);
		typeCombo.setSelectedItem(element.getProperty(property));
		typeCombo.addActionListener(e -> {
			Object selected = typeCombo.getSelectedItem();
			display.setProperty(element, property, selected, Mode.ALL);
			updateFloatingStatus();
		});
		return typeCombo;
	}

	void setImage(File f) {
		imageFile = f;
		lastDir = f;
		byte bytes[];
		if (f == null) {
			bytes = null;
		} else {
			bytes = new byte[(int) imageFile.length()];
			try (FileInputStream stream = new FileInputStream(imageFile);) {
				stream.read(bytes);
			} catch (IOException e) {
				System.err.println("Could not set image for token: " + e.getLocalizedMessage());
			}
		}
		display.setProperty(element, Token.PROPERTY_IMAGE, bytes, Mode.REMOTE);
		element.setImage(imageFile);
	}

	Creature getCreature() {
		return creature;
	}

	void setCreature(Creature c) {
		if (creature == c) return;
		if (creature != null) {
			creature.removePropertyListener("hps", hpListener);
			creature.removePropertyListener("size", sizeListener);
		}
		creature = c;
		if (creature == null) {
			display.setProperty(element, Token.PROPERTY_MAX_HPS, 0);
			display.setProperty(element, Token.PROPERTY_CURRENT_HPS, 0);
			display.setProperty(element, Token.PROPERTY_LABEL, "");
			maxHPsField.setText("");
			currentHPsField.setText("");
			labelField.setText("");
			remoteLabelField.setText("");
		} else {
			creature.addPropertyListener("hps", hpListener);
			creature.addPropertyListener("size", sizeListener);
			updateHPs();

			Size size = creature.getSizeStatistic();
			spaceField.setText("" + ((float) size.getSpace().getValue()) / 2);
			reachField.setText("" + size.getReach().getValue());
			sizeCombo.setSelectedItem(CreatureSize.getSize(size.getSize(), size.getReach().getValue()));
			display.setProperty(element, Token.PROPERTY_SPACE, size.getSpace().getValue());
			display.setProperty(element, Token.PROPERTY_REACH, size.getReach().getValue());

			labelField.setText(creature.getName());
			remoteLabelField.setText(creature.getName());
			display.setProperty(element, Token.PROPERTY_LABEL, creature.getName(), Mode.ALL);

//			labelField.setText(creature.getName());
//			display.setProperty(element, Token.PROPERTY_LABEL, creature.getName(), Mode.LOCAL);
//			// remove any trailing number from the remote name (probably not what we want)
//			String name = creature.getName().replaceAll("\\s\\d+$", "");
//			remoteLabelField.setText(name);
//			display.setProperty(element, Token.PROPERTY_LABEL, name, Mode.REMOTE);
		}

	}

	private PropertyListener<Integer> hpListener = new PropertyListener<Integer>() {
		@Override
		public void propertyChanged(SimpleProperty<Integer> source, Integer oldValue) {
			updateHPs();
		}
	};

	private PropertyListener<Integer> sizeListener = new PropertyListener<Integer>() {
		@Override
		public void propertyChanged(SimpleProperty<Integer> source, Integer oldValue) {
			Size size = creature.getSizeStatistic();
			sizeCombo.setSelectedItem(CreatureSize.getSize(size.getSize(), size.getReach().getValue()));
			spaceField.setText("" + ((float) size.getSpace().getValue()) / 2);
			display.setProperty(element, Token.PROPERTY_SPACE, size.getSpace().getValue());
			reachField.setText("" + size.getReach().getValue());
			display.setProperty(element, Token.PROPERTY_REACH, size.getReach().getValue());
		}
	};

	private void updateFloatingStatus() {
		if (floatingLabel == null || statusDisplayCombo.getSelectedItem() != Token.StatusDisplay.LABEL) return;
		String s = remoteLabelField.getText();
		if (s.length() > 0) s += " ";
		s += element.getStatusDescription();
		display.setProperty(floatingLabel.getElement(), Label.PROPERTY_TEXT, s, Mode.ALL);
	}

	// TODO needs to account for temporary hitpoints
	private void updateHPs() {
		if (creature == null) return;
		HPs hps = creature.getHPStatistic();
		int max = hps.getMaxHPStat().getValue();
		int curr = max - hps.getWounds() - hps.getNonLethal();
		display.setProperty(element, Token.PROPERTY_MAX_HPS, max);
		display.setProperty(element, Token.PROPERTY_CURRENT_HPS, curr);
		maxHPsField.setText("" + max);
		currentHPsField.setText("" + curr);

		updateFloatingStatus();
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(Token.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int) ((Float) e.getNewValue() * 100));

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
			display.setProperty(element, Group.PROPERTY_X, (int) Math.round(p.getX()));
			display.setProperty(element, Group.PROPERTY_Y, (int) Math.round(p.getY()));
		}

		@Override
		Point2D getTargetLocation() {
			int x = (int) Math.round((Double) element.getProperty(Group.PROPERTY_X));
			int y = (int) Math.round((Double) element.getProperty(Group.PROPERTY_Y));
			return new Point(x, y);
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

	private class CreatureListModel implements ComboBoxModel<Creature> {
		InitiativeListModel list;
//		Creature selected = null;
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
			CombatEntry entry = list.getElementAt(index);
			if (entry.isBlank()) return null;
			return entry.getSource();
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
			//return selected;
			return creature;
		}

		@Override
		public void setSelectedItem(Object sel) {
//			if ((selected != null && !selected.equals(sel)) ||
//					selected == null && sel != null) {
//				selected = (Creature) sel;
			if ((creature != null && !creature.equals(sel)) ||
					creature == null && sel != null) {
				setCreature((Creature) sel);
				fireListDataEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
			}
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
	private final static String CREATURE_ID = "creature_id";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		setAttribute(e, REMOTE_PREFIX + Token.PROPERTY_LABEL, remoteLabelField.getText());
		setAttribute(e, REMOTE_PREFIX + Token.PROPERTY_SHOWREACH, remoteReach.isSelected());
		if (webLabelField.getText().length() > 0) setAttribute(e, TokenOverlay.PROPERTY_WEB_LABEL, webLabelField.getText());
		if (imageFile != null) setAttribute(e, FILE_ATTRIBUTE_NAME, imageFile.getPath());
		EncounterModule enc = ModuleRegistry.getModule(EncounterModule.class);
		if (enc != null) {
			Creature c = (Creature) creatureCombo.getSelectedItem();
			if (c != null) e.setAttribute(CREATURE_ID, Integer.toString(c.getID()));
		}
		Point2D location = (Point2D) element.getProperty(Group.PROPERTY_LOCATION);
		e.setAttribute(Group.PROPERTY_LOCATION, location.getX() + "," + location.getY());	// maybe should output X and Y separately
		return e;
	}

	// idMap is used to find the creature for a given id. id's are really only valid within a file as it is loaded so this should be
	// set each time the DOM is parsed
	private Map<Integer, Creature> idMap;

	void setIDMap(Map<Integer, Creature> map) {
		idMap = map;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		EncounterModule enc = ModuleRegistry.getModule(EncounterModule.class);
		if (enc != null) {
			String idStr = e.getAttribute(CREATURE_ID);
			if (idStr.length() > 0) {
//				Creature c = Creature.getCreature(Integer.parseInt(idStr));
				Creature c = idMap.get(Integer.parseInt(idStr));
				setCreature(c);
			}
		}
		parseStringAttribute(Token.PROPERTY_LABEL, e, Mode.LOCAL);
		parseStringAttribute(Token.PROPERTY_LABEL, e, remoteLabelField);
		parseColorAttribute(Token.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(Token.PROPERTY_ALPHA, e, Mode.ALL);
		parseDoubleAttribute(Group.PROPERTY_X, e, Mode.ALL);
		parseDoubleAttribute(Group.PROPERTY_Y, e, Mode.ALL);
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
			display.setProperty(element, Group.PROPERTY_LOCATION, value);
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

		parseVisibility(e, visibleCheck);
	}
}

package combat;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gamesystem.Creature;
import monsters.Monster;
import monsters.StatsBlockCreatureView;
import ui.BoundIntegerField;

//TODO tooltips get lost on reload - the connection to the stat block is not stored in the xml file
//TODO finish tooltip implementation
//TODO ff/touch AC doesn't update for dex changes - it's setting an override value (the controls here should probably apply a misc. modifier)

@SuppressWarnings("serial")
public class MonsterCombatEntry extends CombatEntry {
	// creates a new MonsterCombatEntry backed by a new Monster
	MonsterCombatEntry() {
		creature = new Monster("");
		createEntry();
		setToolTipText(getToolTipText(null));
	}

	public MonsterCombatEntry(Creature m) {
		creature = m;
		createEntry();
		setToolTipText(getToolTipText(null));
		initBlank();
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		if (creature instanceof Monster) {
			StatsBlockCreatureView statsBlockView = StatsBlockCreatureView.getView((Monster) creature);
			return statsBlockView.getHTML();
		}
		return "";
	}

	public Monster getMonster() {
		if (creature instanceof Monster) return (Monster) creature;
		return null;
	}

	void createEntry() {
		acComp = new BoundIntegerField(creature, Creature.PROPERTY_AC, 4);
		touchACComp = new BoundIntegerField(creature, Monster.PROPERTY_AC_TOUCH, 4);
		flatFootedACComp = new BoundIntegerField(creature, Monster.PROPERTY_AC_FLATFOOTED, 4);

		modifierComp = new BoundIntegerField(creature, Creature.PROPERTY_INITIATIVE, 3);

		creature.addPropertyChangeListener(this);
		createPanel();
	}

	@Override
	void createButtons() {
		apply = new JButton("Apply");
		apply.addActionListener(e -> {
			int delta = ((Integer) dmgField.getValue());
			applyDamage(delta, nonLethal.isSelected());
		});

		healAll = new JButton("Heal All");
		healAll.addActionListener(e -> healAll());
	}

	@Override
	JComponent createNameSection() {
		JPanel section = new JPanel();
		section.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		delete = new JButton("X");
		delete.setMargin(new Insets(0, 4, 0, 3));
		delete.setFocusPainted(false);
		delete.setEnabled(false);
		delete.addActionListener(e -> {
			// fire a delete action to all listeners
			Object[] listeners = listenerList.getListenerList();
			// Process the listeners last to first, notifying those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ChangeListener.class) {
					// Lazily create the event:
					if (actionEvent == null)
						actionEvent = new ActionEvent(MonsterCombatEntry.this, ActionEvent.ACTION_PERFORMED, "delete", e.getWhen(), e.getModifiers());
					((ActionListener) listeners[i + 1]).actionPerformed(actionEvent);
				}
			}
		});
		section.add(delete, c);
		nameField = new JTextField(20);
		nameField.setText(creature.getName());
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				initBlank();
				creature.setName(nameField.getText());
				fireChange();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				initBlank();
				creature.setName(nameField.getText());
				fireChange();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				initBlank();
				creature.setName(nameField.getText());
				fireChange();
			}

		});

		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		section.add(nameField, c);

		return section;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals("value")) {
			if (initBlank()) fireChange();
		}
	}

	public static MonsterCombatEntry parseDOM(Element el) {
		if (!el.getNodeName().equals("MonsterEntry")) return null;
		String name = el.getAttribute("name");
		MonsterCombatEntry c = new MonsterCombatEntry(new Monster(name));
		c.nameField.setText(name);
		c.rollField.setValue(Integer.parseInt(el.getAttribute("roll")));
		c.tiebreakField.setValue(Integer.parseInt(el.getAttribute("tieBreak")));
		c.creature.setInitiativeModifier(Integer.parseInt(el.getAttribute("initMod")));
		c.creature.setMaximumHitPoints(Integer.parseInt(el.getAttribute("maxHPs")));
		c.creature.setWounds(Integer.parseInt(el.getAttribute("wounds")));
		c.creature.setNonLethal(Integer.parseInt(el.getAttribute("nonLethal")));
		c.creature.setAC(Integer.parseInt(el.getAttribute("fullAC")));
		c.creature.setTouchAC(Integer.parseInt(el.getAttribute("touchAC")));
		c.creature.setFlatFootedAC(Integer.parseInt(el.getAttribute("flatFootedAC")));
		String idStr = el.getAttribute("creatureID");
		if (idStr.length() > 0) c.creature.setID(Integer.parseInt(idStr));
		return c;
	}

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement("MonsterEntry");
		e.setAttribute("creatureID", Integer.toString(creature.getID()));
		e.setAttribute("name", creature.getName());
		e.setAttribute("roll", Integer.toString(getRoll()));
		e.setAttribute("tieBreak", Integer.toString(getTieBreak()));
		e.setAttribute("initMod", Integer.toString(creature.getInitiativeModifier()));
		e.setAttribute("maxHPs", Integer.toString(creature.getMaximumHitPoints()));
		e.setAttribute("wounds", Integer.toString(creature.getWounds()));
		e.setAttribute("nonLethal", Integer.toString(creature.getNonLethal()));
		e.setAttribute("fullAC", Integer.toString(creature.getAC()));
		e.setAttribute("touchAC", Integer.toString(creature.getTouchAC()));
		e.setAttribute("flatFootedAC", Integer.toString(creature.getFlatFootedAC()));
		return e;
	}
}

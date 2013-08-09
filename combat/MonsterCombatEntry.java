package combat;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import party.Creature;
import party.Monster;
import ui.BoundIntegerField;

//TODO tooltips get lost on reload - the connection to the stat block is not stored in the xml file

@SuppressWarnings("serial")
class MonsterCombatEntry extends CombatEntry {
	// creates a new MonsterCombatEntry backed by a new Monster
	MonsterCombatEntry() {
		creature = new Monster();
		createEntry();
	}

	MonsterCombatEntry(Monster m) {
		creature = m;
		createEntry();
		if (m.getStatsBlock() != null) setToolTipText(m.getStatsBlock().getHTML());
		initBlank();
	}

	void createEntry() {
		acComp = new BoundIntegerField(creature, Monster.PROPERTY_AC, 4);
		touchACComp = new BoundIntegerField(creature, Monster.PROPERTY_AC_TOUCH, 4);
		flatFootedACComp = new BoundIntegerField(creature, Monster.PROPERTY_AC_FLATFOOTED, 4);

		modifierComp = new BoundIntegerField(creature, Creature.PROPERTY_INITIATIVE, 3);

		creature.addPropertyChangeListener(this);
		createPanel();
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
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// fire a delete action to all listeners
				// Guaranteed to return a non-null array
				Object[] listeners = listenerList.getListenerList();
				// Process the listeners last to first, notifying
				// those that are interested in this event
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == ChangeListener.class) {
						// Lazily create the event:
						if (actionEvent == null)
							actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "delete", e.getWhen(), e.getModifiers());
						((ActionListener) listeners[i + 1]).actionPerformed(actionEvent);
					}
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

	static MonsterCombatEntry parseDOM(Element el) {
		if (!el.getNodeName().equals("MonsterEntry")) return null;
		MonsterCombatEntry c = new MonsterCombatEntry(new Monster());
		c.nameField.setText(el.getAttribute("name"));
		c.rollField.setValue(Integer.parseInt(el.getAttribute("roll")));
		c.tiebreakField.setValue(Integer.parseInt(el.getAttribute("tieBreak")));
		c.creature.setInitiativeModifier(Integer.parseInt(el.getAttribute("initMod")));
		c.creature.setMaximumHitPoints(Integer.parseInt(el.getAttribute("maxHPs")));
		c.creature.setWounds(Integer.parseInt(el.getAttribute("wounds")));
		c.creature.setNonLethal(Integer.parseInt(el.getAttribute("nonLethal")));
		c.creature.setAC(Integer.parseInt(el.getAttribute("fullAC")));
		c.creature.setTouchAC(Integer.parseInt(el.getAttribute("touchAC")));
		c.creature.setFlatFootedAC(Integer.parseInt(el.getAttribute("flatFootedAC")));
		return c;
	}

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement("MonsterEntry");
		e.setAttribute("name", creature.getName());
		e.setAttribute("roll", "" + getRoll());
		e.setAttribute("tieBreak", "" + getTieBreak());
		e.setAttribute("initMod", "" + creature.getInitiativeModifier());
		e.setAttribute("maxHPs", "" + creature.getMaximumHitPoints());
		e.setAttribute("wounds", "" + creature.getWounds());
		e.setAttribute("nonLethal", "" + creature.getNonLethal());
		e.setAttribute("fullAC", "" + creature.getAC());
		e.setAttribute("touchAC", "" + creature.getTouchAC());
		e.setAttribute("flatFootedAC", "" + creature.getFlatFootedAC());
		return e;
	}
}

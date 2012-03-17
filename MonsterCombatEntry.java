import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.w3c.dom.Element;

import party.Character;
import party.Creature;
import party.Monster;
import party.Party;
import ui.BoundIntegerField;

@SuppressWarnings("serial")
public class MonsterCombatEntry extends CombatEntry {
	public MonsterCombatEntry(Creature creature) {
		this.creature = creature;

		acComp = new BoundIntegerField(creature, Monster.PROPERTY_AC, 4);
		touchACComp = new BoundIntegerField(creature, Monster.PROPERTY_AC_TOUCH, 4);
		flatFootedACComp = new BoundIntegerField(creature, Monster.PROPERTY_AC_FLATFOOTED, 4);

		modifierComp = new BoundIntegerField(creature, Creature.PROPERTY_INITIATIVE, 3);

		creature.addPropertyChangeListener(this);
		createPanel();
	}

	protected JComponent createNameSection() {
		JPanel section = new JPanel();
		section.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		delete = new JButton("X");
		delete.setMargin(new Insets(0, 4, 0, 3));
		delete.setFocusPainted(false);
		delete.setEnabled(false);
		delete.addActionListener(this);
		section.add(delete, c);
		nameField = new JTextField(20);
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				initBlank();
				creature.setName(nameField.getText());
				fireChange();
			}

			public void insertUpdate(DocumentEvent e) {
				initBlank();
				creature.setName(nameField.getText());
				fireChange();
			}

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

	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals("value")) {
			if (initBlank()) fireChange();
		}
	}

	public static MonsterCombatEntry parseDOM(Element el) {
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

	public String getXML(String indent, String nextIndent) {
		StringBuilder b = new StringBuilder();
		b.append(indent).append("<MonsterEntry name=\"").append(creature.getName());
		b.append("\" roll=\"").append(getRoll());
		b.append("\" tieBreak=\"").append(getTieBreak());
		b.append("\" initMod=\"").append(creature.getInitiativeModifier());
		b.append("\" maxHPs=\"").append(creature.getMaximumHitPoints());
		b.append("\" wounds=\"").append(creature.getWounds());
		b.append("\" nonLethal=\"").append(creature.getNonLethal());
		b.append("\" fullAC=\"").append(creature.getAC());
		b.append("\" touchAC=\"").append(creature.getTouchAC());
		b.append("\" flatFootedAC=\"").append(creature.getFlatFootedAC());
		b.append("\"/>").append(System.getProperty("line.separator"));
		return b.toString();
	}
}

package combat;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
import ui.CharacterDamagePanel;
import ui.CharacterDamagePanel.AbilityDamagePanel;
import ui.CharacterDamagePanel.HPPanel;
import ui.PropertyFields;

//TODO tooltips get lost on reload - the connection to the stat block is not stored in the xml file
//TODO finish tooltip implementation
//TODO ff/touch AC doesn't update for dex changes - it's setting an override value (the controls here should probably apply a misc. modifier)

@SuppressWarnings("serial")
public class MonsterCombatEntry extends CombatEntry {
	// creates a new MonsterCombatEntry backed by a new Monster
	MonsterCombatEntry() {
		super(new Monster(""));
		createEntry();
		setToolTipText(getToolTipText(null));
	}

	public MonsterCombatEntry(Creature m) {
		super(m);
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
		acComp = PropertyFields.createOverrideIntegerField(creature.getACStatistic(), 4);
		touchACComp = PropertyFields.createOverrideIntegerField(creature.getACStatistic().getTouchAC(), 4);
		flatFootedACComp = PropertyFields.createOverrideIntegerField(creature.getACStatistic().getFlatFootedAC(), 4);

		modifierComp = PropertyFields.createBaseValueField(creature.getInitiativeStatistic(), 3);
		createPanel();
	}

	@Override
	void createButtons() {
		apply = new JButton("Apply");
		apply.addActionListener(e -> {
			int dmg = ((Integer) dmgField.getValue());
			if (dmg > 0) {
				if (nonLethal.isSelected()) {
					hps.applyNonLethal(dmg);
				} else {
					hps.applyDamage(dmg);
				}
			} else if (dmg < 0) {
				hps.applyHealing(-dmg);
			}
		});

		healAll = new JButton("Heal All");
		healAll.addActionListener(e -> {
			hps.applyHealing(Math.max(hps.getWounds(), hps.getNonLethal()));
		});
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
		nameField.setMinimumSize(nameField.getPreferredSize());
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
	void updateDetails(JPanel panel, boolean selected) {
		if (!selected) {
			// TODO need to remove listeners
			panel.removeAll();
			return;
		}
		JLabel name = new JLabel(getCreatureName());
		Font f = name.getFont();
		name.setFont(f.deriveFont(Font.BOLD, f.getSize2D() * 1.5f));

		HPPanel dmg = new CharacterDamagePanel.HPPanel(creature);
		AbilityDamagePanel abs = new CharacterDamagePanel.AbilityDamagePanel(creature);

		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		panel.add(name, c);

		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.gridy = 1;
		panel.add(dmg, c);

		c.gridx = 1;
		c.fill = GridBagConstraints.VERTICAL;
		c.weighty = 1.0;
		panel.add(abs, c);

		panel.revalidate();
		panel.repaint();
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
		Monster m = new Monster(name);
		m.getInitiativeStatistic().setValue(Integer.parseInt(el.getAttribute("initMod")));
		m.getHPStatistic().getMaxHPStat().addOverride(Integer.parseInt(el.getAttribute("maxHPs")));
		m.getHPStatistic().getWoundsProperty().setValue(Integer.parseInt(el.getAttribute("wounds")));
		m.getHPStatistic().getNonLethalProperty().setValue((Integer.parseInt(el.getAttribute("nonLethal"))));
		// FIXME fix this somehow - overrides?
//		c.creature.setAC(Integer.parseInt(el.getAttribute("fullAC")));
//		c.creature.setTouchAC(Integer.parseInt(el.getAttribute("touchAC")));
//		c.creature.setFlatFootedAC(Integer.parseInt(el.getAttribute("flatFootedAC")));
		MonsterCombatEntry c = new MonsterCombatEntry(m);
		c.nameField.setText(name);
		c.rollField.setValue(Integer.parseInt(el.getAttribute("roll")));
		c.tiebreakField.setValue(Integer.parseInt(el.getAttribute("tieBreak")));
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
		e.setAttribute("initMod", Integer.toString(creature.getInitiativeStatistic().getValue()));
		e.setAttribute("maxHPs", Integer.toString(creature.getHPStatistic().getMaxHPStat().getValue()));
		e.setAttribute("wounds", Integer.toString(creature.getHPStatistic().getWounds()));
		e.setAttribute("nonLethal", Integer.toString(creature.getHPStatistic().getNonLethal()));
		e.setAttribute("fullAC", Integer.toString(creature.getACStatistic().getValue()));
		e.setAttribute("touchAC", Integer.toString(creature.getACStatistic().getTouchAC().getValue()));
		e.setAttribute("flatFootedAC", Integer.toString(creature.getACStatistic().getFlatFootedAC().getValue()));
		return e;
	}
}

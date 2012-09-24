package ui;

import gamesystem.Buff;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import party.Character;

@SuppressWarnings("serial")
public class CharacterBuffsPanel extends JPanel implements ItemListener {
	Map<JCheckBox,Buff> buffs = new HashMap<JCheckBox,Buff>();
	Character character;

	public CharacterBuffsPanel(Character c) {
		character = c;
		
		setBorder(new TitledBorder("Buffs / Penalties"));
		setLayout(new GridLayout(0,2));
		for (Buff b : Buff.buffs) {
			JCheckBox cb = new JCheckBox(b.name);
			cb.addItemListener(this);
			cb.setToolTipText(b.getDescription());
			buffs.put(cb,b);
			add(cb);
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Buff b = buffs.get(e.getSource());
		if (b != null) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				b.applyBuff(character);
			} else {
				b.removeBuff(character);
			}
		}
	}
}

package ui;

import gamesystem.Buff;
import gamesystem.BuffFactory;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import party.Character;

@SuppressWarnings("serial")
public class CharacterBuffsPanel extends JPanel implements ItemListener {
	Character character;

	protected class BuffCheckBox extends JCheckBox {
		BuffFactory buffFactory;
		Buff buff;

		BuffCheckBox(BuffFactory bf) {
			super(bf.name);
			buffFactory = bf;
			setToolTipText(bf.getDescription());
		}
	}

	public CharacterBuffsPanel(Character c) {
		character = c;
		
		setBorder(new TitledBorder("Buffs / Penalties"));
		setLayout(new GridLayout(0,2));
		for (BuffFactory bf : BuffFactory.buffs) {
			JCheckBox cb = new BuffCheckBox(bf);
			cb.addItemListener(this);
			add(cb);
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (!(e.getSource() instanceof BuffCheckBox)) return;
		final BuffCheckBox cb = (BuffCheckBox)e.getSource();
		if (e.getStateChange() == ItemEvent.SELECTED) {
			// need to invoke this code later since it involves a modal dialog
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (cb.buffFactory.requiresCasterLevel()) {
						String s = (String)JOptionPane.showInputDialog(null, "Enter caster level:", "Enter caster level...", JOptionPane.QUESTION_MESSAGE);
						int cl = 0;
						if (s != null) {
							try {
								cl = Integer.parseInt(s);
							} catch(NumberFormatException ex) {
							}
						}
						// TODO what to do if the input is invalid
						cb.buff= cb.buffFactory.getBuff(cl);
						System.out.println("buff created");
					} else {
						cb.buff = cb.buffFactory.getBuff();
					}
					cb.buff.applyBuff(character);
				}
			});
		} else {
			cb.buff.removeBuff(character);
			cb.buff = null;
		}
	}
}

package ui;

import gamesystem.Buff;
import gamesystem.BuffFactory;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import party.Character;
import swing.JListWithToolTips;

@SuppressWarnings("serial")
public class CharacterBuffsPanel extends CharacterSubPanel {
	public CharacterBuffsPanel(Character c) {
		super(c);
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		final BuffUI ui = new BuffUI();
		JListWithToolTips<BuffFactory> buffs = ui.getBuffList();
		buffs.setVisibleRowCount(20);
		add(buffs);

		final JListWithToolTips<Buff> applied = new JListWithToolTips<>(character.getBuffListModel());
		applied.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		applied.setVisibleRowCount(8);

		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> character.addBuff(ui.getBuff()));

		JButton remove = new JButton("Remove");
		remove.addActionListener(e -> {
			for (Buff b : applied.getSelectedValuesList()) {
				character.removeBuff(b);
			}
		});

		JScrollPane scroller = new JScrollPane(buffs);
		//scroller.setPreferredSize(preferredSize);
		add(scroller);

		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
		right.add(ui.getOptionsPanel());

		JPanel buttons = new JPanel();
		buttons.add(apply);
		buttons.add(remove);
		right.add(buttons);

		scroller = new JScrollPane(applied);
		scroller.setBorder(new TitledBorder("Currently Applied:"));
		right.add(scroller);
		add(right);
	}
}

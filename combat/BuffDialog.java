package combat;

import gamesystem.Buff;
import gamesystem.BuffFactory;
import gamesystem.Creature;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import swing.JListWithToolTips;
import ui.BuffUI;

@SuppressWarnings("serial")
class BuffDialog extends JDialog {
	private JComponent owner;
	private BuffUI ui;

	private boolean okSelected = false;
	private Map<JCheckBox, Creature> targets = new HashMap<>();

	BuffDialog(JComponent own, InitiativeListModel ilm) {
		super(SwingUtilities.windowForComponent(own), "Choose buff", Dialog.ModalityType.APPLICATION_MODAL);
		owner = own;

		ui = new BuffUI();

		JListWithToolTips<BuffFactory> buffs = ui.getBuffList();
		buffs.addListSelectionListener(e -> pack());

		JScrollPane scroller = new JScrollPane(buffs);
		//scroller.setPreferredSize(preferredSize);
		add(scroller, BorderLayout.WEST);

		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
		right.add(ui.getOptionsPanel());

		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		right.add(separator);

		for (int i = 0; i < ilm.getSize(); i++) {
			CombatEntry e = ilm.getElementAt(i);
			if (!e.blank) {
				JCheckBox cb = new JCheckBox(e.creature.getName());
				right.add(cb);
				targets.put(cb, e.creature);
			}
		}

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(e -> {
			dispose();
			okSelected = true;
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());

		JPanel buttons = new JPanel();
		buttons.add(okButton);
		buttons.add(cancelButton);
		right.add(buttons);

		add(right);
		buffs.setSelectedIndex(0);
		pack();
		setVisible(true);
		setLocationRelativeTo(SwingUtilities.getWindowAncestor(owner));
	}

	Buff getBuff() {
		if (okSelected) return ui.getBuff();
		return null;
	}

	Set<Creature> getTargets() {
		HashSet<Creature> targetted = new HashSet<>();
		if (okSelected) {
			for (JCheckBox cb : targets.keySet()) {
				if (cb.isSelected()) targetted.add(targets.get(cb));
			}
		}
		return targetted;
	}
}

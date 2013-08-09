package combat;

import gamesystem.Buff;
import gamesystem.Buff.Effect;
import gamesystem.Buff.ModifierEffect;
import gamesystem.BuffFactory;
import gamesystem.Modifier;
import gamesystem.dice.Dice;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import party.Character;
import swing.JListWithToolTips;
import ui.CharacterBuffsPanel.BuffListModel;

@SuppressWarnings("serial")
class BuffDialog extends JDialog {
	private JComponent owner;
	private JPanel buffOptionsPanel;
	private boolean okSelected = false;
	private Map<JCheckBox, Character> targets = new HashMap<JCheckBox, Character>();

	// controls used in the options panel
	private Buff buff;
	private SpinnerNumberModel clModel;
	private JSpinner clSpinner;
	private JCheckBox empCheckBox;
	private JCheckBox maxCheckBox;
	private JPanel clPanel;

	// controls used in the descriptions of the effects of the buff
	private JLabel[] effectLabels;

	BuffDialog(JComponent own, InitiativeListModel ilm) {
		super(SwingUtilities.windowForComponent(own), "Choose buff", Dialog.ModalityType.APPLICATION_MODAL);
		owner = own;

		initializeOptionsControls();

		setLayout(new GridLayout(0, 2));

		BuffFactory[] availableBuffs = Arrays.copyOf(BuffFactory.buffs, BuffFactory.buffs.length);
		Arrays.sort(availableBuffs, new Comparator<BuffFactory>() {
			@Override
			public int compare(BuffFactory a, BuffFactory b) {
				return a.name.compareTo(b.name);
			}
		});
		BuffListModel bfModel = new BuffListModel(availableBuffs);
		final JListWithToolTips buffs = new JListWithToolTips(bfModel);
		buffs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buffs.setVisibleRowCount(20);
		buffs.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				buff = ((BuffFactory) buffs.getSelectedValue()).getBuff();
				updateBuffOptions();
			}
		});

		JScrollPane scroller = new JScrollPane(buffs);
		//scroller.setPreferredSize(preferredSize);
		add(scroller);

		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
		buffOptionsPanel = new JPanel();
		buffOptionsPanel.setLayout(new BoxLayout(buffOptionsPanel, BoxLayout.PAGE_AXIS));
		right.add(buffOptionsPanel);

		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		right.add(separator);

		for (int i = 0; i < ilm.getSize(); i++) {
			Object e = ilm.getElementAt(i);
			if (e instanceof CharacterCombatEntry) {
				Character c = ((CharacterCombatEntry) e).getCharacter();
				JCheckBox cb = new JCheckBox(c.getName());
				right.add(cb);
				targets.put(cb, c);
			}
		}

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				okSelected = true;
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		JPanel buttons = new JPanel();
		buttons.add(okButton);
		buttons.add(cancelButton);
		right.add(buttons);

		add(right);
		pack();
		buffs.setSelectedIndex(0);
		setVisible(true);
		setLocationRelativeTo(SwingUtilities.getWindowAncestor(owner));
	}

	Buff getBuff() {
		if (okSelected) return buff;
		return null;
	}

	Set<Character> getTargets() {
		HashSet<Character> targetted = new HashSet<Character>();
		if (okSelected) {
			for (JCheckBox cb : targets.keySet()) {
				if (cb.isSelected()) targetted.add(targets.get(cb));
			}
		}
		return targetted;
	}

	private void initializeOptionsControls() {
		clModel = new SpinnerNumberModel(1, 1, 20, 1);
		clModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				buff.setCasterLevel(clModel.getNumber().intValue());
				updateEffects(buff);
			}
		});
		clSpinner = new JSpinner(clModel);

		empCheckBox = new JCheckBox("Empowered");
		empCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				buff.empowered = empCheckBox.isSelected();
				updateEffects(buff);
			}
		});

		maxCheckBox = new JCheckBox("Maximized");
		maxCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				buff.maximized = maxCheckBox.isSelected();
				updateBuffOptions();
			}
		});

		clPanel = new JPanel();
		clPanel.add(new JLabel("Caster Level: "));
		clPanel.add(clSpinner);
		clPanel.add(empCheckBox);
		clPanel.add(maxCheckBox);
	}

	private void updateBuffOptions() {
		buffOptionsPanel.removeAll();

		if (buff.requiresCasterLevel()) {
			buffOptionsPanel.add(clPanel);
		}

		JPanel effectsPanel = new JPanel();
		effectsPanel.setLayout(new GridBagLayout());
		effectLabels = new JLabel[buff.effects.size()];
		SpinnerNumberModel[] diceModels = new SpinnerNumberModel[buff.effects.size()];
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(2, 5, 2, 5);
		int i = 0;
		for (Effect e : buff.effects) {
			JLabel l = new JLabel(e.toString());
			c.gridy = i;
			c.gridx = 0;
			effectsPanel.add(l, c);
			c.fill = GridBagConstraints.NONE;

			final Dice d = e.getDice();
			if (d != null && (!buff.maximized || buff.empowered)) {
				// if the buff is maximized but not empowered then we don't need to roll

				int roll = d.roll();
				buff.setRoll(d, roll);
				diceModels[i] = new SpinnerNumberModel(roll, d.getMinimum(), d.getMaximum(), 1);
				diceModels[i].addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						buff.setRoll(d, ((SpinnerNumberModel) e.getSource()).getNumber().intValue());
						updateEffects(buff);
					}
				});
				JSpinner s = new JSpinner(diceModels[i]);
				c.gridx = 1;
				effectsPanel.add(new JLabel("Roll: "), c);
				c.gridx = 2;
				effectsPanel.add(s, c);
			}
			if (e.requiresCasterLevel()) {
				effectLabels[i] = new JLabel();
				c.gridx = 3;
				effectsPanel.add(effectLabels[i], c);
			}
			i++;
		}
		buffOptionsPanel.add(effectsPanel);

		updateEffects(buff);

		pack();
	}

	private void updateEffects(Buff buff) {
		int i = 0;
		for (Effect e : buff.effects) {
			if (e instanceof ModifierEffect) {
				if (e.requiresCasterLevel()) {
					StringBuilder s = new StringBuilder("(");
					Modifier m = ((ModifierEffect) e).getModifier(buff);
					if (m.getModifier() >= 0) s.append("+");
					s.append(m.getModifier()).append(")");
					effectLabels[i].setText(s.toString());
				}
				i++;
			}
		}
	}
}

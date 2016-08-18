package ui;

import gamesystem.Buff;
import gamesystem.BuffFactory;
import gamesystem.Modifier;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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

		JButton custom = new JButton("Custom");
		custom.addActionListener(e -> {
			CustomBuffDialog dialog = new CustomBuffDialog(this, c);
			if (dialog.okSelected()) {
				Buff b = dialog.getBuff();
				if (b != null) character.addBuff(b);
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
		buttons.add(custom);
		right.add(buttons);

		scroller = new JScrollPane(applied);
		scroller.setBorder(new TitledBorder("Currently Applied:"));
		right.add(scroller);
		add(right);
	}

	static class TargetComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {
		String[][] targets;
		Object selected;

		TargetComboBoxModel(Character c) {
			targets = c.getValidTargets();
		}

		@Override
		public String getElementAt(int i) {
			return targets[i][0];
		}

		@Override
		public int getSize() {
			return targets.length;
		}

		@Override
		public Object getSelectedItem() {
			return selected;
		}

		@Override
		public void setSelectedItem(Object s) {
			selected = s;
		}

		public String getSelectedTarget() {
			for (String[] t : targets) {
				if (t[0] == selected) return t[1];
			}
			return null;
		}
	}

	static class ModifierDef {
		int modifier;
		String type;
		String target;
		String targetName;

		@Override
		public String toString() {
			String s = "";
			if (type != null) s += " " + type;
			if (modifier >= 0) {
				s = "+" + modifier + s + " bonus";
			} else {
				s = modifier + s + " penalty";
			}
			s += " to " + targetName;
			return s;
		}
	}

	static class CustomBuffDialog extends JDialog {
		private JComponent owner;

		final JTextField sourceField = new JTextField();
		final DefaultListModel<ModifierDef> modsModel = new DefaultListModel<ModifierDef>();
		private boolean okSelected = false;

		CustomBuffDialog(JComponent own, Character chr) {
			super(SwingUtilities.windowForComponent(own), "Choose buff", Dialog.ModalityType.DOCUMENT_MODAL);
			owner = own;

			final JFormattedTextField modField = new JFormattedTextField();
			modField.setValue(new Integer(0));
			modField.setColumns(3);

			final JComboBox<Modifier.StandardType> typeBox = new JComboBox<>(Modifier.StandardType.values());
			typeBox.setSelectedItem(Modifier.StandardType.ENHANCEMENT);
			typeBox.setEditable(true);

			final TargetComboBoxModel targetModel = new TargetComboBoxModel(chr);
			final JComboBox<String> targetStat = new JComboBox<>(targetModel);

			final JList<ModifierDef> modsList = new JList<>(modsModel);
			JScrollPane scroller = new JScrollPane(modsList);
			//scroller.setPreferredSize(preferredSize);

			final JButton addButton = new JButton("Add");
			addButton.addActionListener(e -> {
				ModifierDef m = new ModifierDef();
				m.modifier = (Integer) modField.getValue();
				m.type = typeBox.getSelectedItem().toString();
				m.targetName = targetStat.getSelectedItem().toString();
				m.target = targetModel.getSelectedTarget();
				modsModel.addElement(m);
			});

			final JButton delButton = new JButton("Delete");
			delButton.addActionListener(e -> {
				int[] selected = modsList.getSelectedIndices();
				for (int i = selected.length - 1; i >= 0; i--) {
					modsModel.removeElementAt(selected[i]);
				}
			});

			final JButton okButton = new JButton("Ok");
			okButton.addActionListener(e -> {
				dispose();
				okSelected = true;
			});

			final JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(e -> dispose());

			JPanel buttons = new JPanel();
			buttons.add(okButton);
			buttons.add(cancelButton);

			add(buttons, BorderLayout.SOUTH);

//			JPanel right = new JPanel();
//			right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
//			JPanel optPanel = ui.getOptionsPanel();
//			optPanel.setAlignmentX(0.0f);
//			right.add(optPanel);

			final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);

			JPanel pane = new JPanel();
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(2, 2, 2, 2);

			c.gridx = 0;
			c.gridy = 0;
			pane.add(new JLabel("Source: "), c);

			c.gridx = 1;
			c.gridwidth = 4;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			pane.add(sourceField, c);

			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0.0;
			pane.add(modField, c);

			c.gridx = 1;
			pane.add(new JLabel("(Type:) "), c);

			c.gridx = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.5;
			pane.add(typeBox, c);

			c.gridx = 3;
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0.0;
			pane.add(new JLabel(" to "), c);

			c.gridx = 4;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.5;
			pane.add(targetStat, c);

			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 5;
			c.fill = GridBagConstraints.NONE;
			c.weightx = 1.0;
			buttons = new JPanel();
			buttons.add(addButton);
			buttons.add(delButton);
			pane.add(buttons, c);

			c.gridy = 3;
			c.gridwidth = 5;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.CENTER;
			c.weighty = 1.0;
			pane.add(scroller, c);

			c.gridy = 4;
			pane.add(separator, c);

			add(pane);

			pack();
			setLocationRelativeTo(SwingUtilities.getWindowAncestor(owner));
			setVisible(true);
		}

		boolean okSelected() {
			return okSelected;
		}

		Buff getBuff() {
			if (!okSelected || modsModel.getSize() == 0) return null;
			BuffFactory bf = new BuffFactory(sourceField.getText());
			for (int i = 0; i < modsModel.getSize(); i++) {
				ModifierDef d = modsModel.get(i);
				bf.addEffect(d.target, d.type, d.modifier);
			}
			return bf.getBuff();
		}
	}
}

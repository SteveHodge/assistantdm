package ui;

import java.awt.GridLayout;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import gamesystem.Buff;
import gamesystem.Feat;
import gamesystem.Feat.FeatDefinition;
import party.Character;
import swing.JListWithToolTips;
import swing.ListModelWithToolTips;

@SuppressWarnings("serial")
public class CharacterFeatsPanel extends CharacterSubPanel {
	public CharacterFeatsPanel(Character c) {
		super(c);
		setLayout(new GridLayout(0,2));

		FeatDefinition[] availableFeats = Feat.feats.toArray(new FeatDefinition[0]);
		Arrays.sort(availableFeats, (a, b) -> a.name.compareTo(b.name));
		FeatListModel bfModel = new FeatListModel(availableFeats);
		final JListWithToolTips<FeatDefinition> feats = new JListWithToolTips<>(bfModel);
		feats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		feats.setVisibleRowCount(20);

		final JListWithToolTips<Feat> chosen = new JListWithToolTips<>(character.feats);
		chosen.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		chosen.setVisibleRowCount(8);

		JButton apply = new JButton("Take Feat");
		apply.addActionListener(e -> SwingUtilities.invokeLater(() -> {
			// need to invoke this code later since it involves a modal dialog
			FeatDefinition def = feats.getSelectedValue();
			Feat feat;
			if (def.hasTarget()) {
				String target = JOptionPane.showInputDialog("Feat " + def.name + ":\nPlease specify type of " + def.target);
				feat = def.getFeat(target);
			} else {
				feat = def.getFeat();
			}
			feat.apply(character);
			character.feats.addElement(feat);
		}));

		JButton remove = new JButton("Untake Feat");
		remove.addActionListener(e -> {
			for (Feat f : chosen.getSelectedValuesList()) {
				f.remove(character);
				character.feats.removeElement(f);
			}
		});

		JScrollPane scroller = new JScrollPane(feats);
		//scroller.setPreferredSize(preferredSize);
		add(scroller);

		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));

		JPanel buttons = new JPanel();
		buttons.add(apply);
		buttons.add(remove);
		right.add(buttons);

		scroller = new JScrollPane(chosen);
		scroller.setBorder(new TitledBorder("Chosen Feats:"));
		right.add(scroller);
		add(right);
	}

	public static class FeatListModel extends DefaultListModel<FeatDefinition> implements ListModelWithToolTips<FeatDefinition> {
		public FeatListModel() {
			super();
		}

		public FeatListModel(FeatDefinition[] buffs) {
			super();
			for (FeatDefinition bf : buffs) {
				addElement(bf);
			}
		}

		@Override
		public String getToolTipAt(int index) {
			if (index < 0) return null;
			Object o = get(index);
			if (o instanceof FeatDefinition) {
				return ((FeatDefinition)o).getDescription();
			} else if (o instanceof Buff) {
				return ((Buff)o).getDescription();
			}
			return null;
		}
	}
}

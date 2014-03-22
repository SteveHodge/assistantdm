package ui;

import gamesystem.Buff;
import gamesystem.Feat;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import party.Character;
import swing.JListWithToolTips;
import swing.ListModelWithToolTips;

@SuppressWarnings("serial")
public class CharacterFeatsPanel extends CharacterSubPanel {
	public CharacterFeatsPanel(Character c) {
		super(c);
		setLayout(new GridLayout(0,2));

		Feat[] availableFeats = Arrays.copyOf(Feat.FEATS, Feat.FEATS.length);
		Arrays.sort(availableFeats, new Comparator<Feat>() {
			@Override
			public int compare(Feat a, Feat b) {
				return a.name.compareTo(b.name);
			}
		});
		FeatListModel bfModel = new FeatListModel(availableFeats);
		final JListWithToolTips<Feat> feats = new JListWithToolTips<>(bfModel);
		feats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		feats.setVisibleRowCount(20);

		final JListWithToolTips<Buff> chosen = new JListWithToolTips<>(character.feats);
		chosen.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		chosen.setVisibleRowCount(8);

		JButton apply = new JButton("Take Feat");
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// need to invoke this code later since it involves a modal dialog
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Feat bf = feats.getSelectedValue();
						Buff buff = applyFeat(bf);
						character.feats.addElement(buff);
					}
				});
			}
		});

		JButton remove = new JButton("Untake Feat");
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (Buff b : chosen.getSelectedValuesList()) {
					b.removeBuff(character);
					character.feats.removeElement(b);
				}
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

	protected Buff applyFeat(Feat bf) {
		Buff buff = bf.getBuff();
		buff.applyBuff(character);
		return buff;
	}

	public static class FeatListModel extends DefaultListModel<Feat> implements ListModelWithToolTips<Feat> {
		public FeatListModel() {
			super();
		}

		public FeatListModel(Feat[] buffs) {
			super();
			for (Feat bf : buffs) {
				addElement(bf);
			}
		}

		@Override
		public String getToolTipAt(int index) {
			if (index < 0) return null;
			Object o = get(index);
			if (o instanceof Feat) {
				return ((Feat)o).getDescription();
			} else if (o instanceof Buff) {
				return ((Buff)o).getDescription();
			}
			return null;
		}
	}
}

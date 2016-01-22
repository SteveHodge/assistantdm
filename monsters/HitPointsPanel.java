package monsters;

import gamesystem.Creature;
import gamesystem.HPs;
import gamesystem.dice.DiceList;
import gamesystem.dice.HDDice;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


// TODO hitdice should show con bonus as read-only and auto-adjust with change to con
// FIXME probably wrong with new implementation

@SuppressWarnings("serial")
class HitPointsPanel extends DetailPanel {
	private Monster creature;
	private HPs hps;				// cached copy from creature
	private DiceList<HDDice> hitdice;		// cached copy from creature

	private JTextField hitDiceField;
	private Color defaultBG;
	private JFormattedTextField hitPointsField;	// TODO split into current and max. also should make this a spinner
	private JLabel hpRangeLabel;
	private JLabel statsLabel;
	private JRadioButton averageHPsButton;
	private JRadioButton rollHPsButton;
	private JRadioButton minHalfHPsButton;
	private HPGraphPanel graph;

	HitPointsPanel() {
		setLayout(new GridBagLayout());

		hitDiceField = new JTextField(20);
		defaultBG = hitDiceField.getBackground();
		hitDiceField.addActionListener(evt -> {
			String s = hitDiceField.getText();
			try {
				List<HDDice> test = HDDice.parseList(s);
				if (creature != null) {
					hitdice = DiceList.fromList(test);
					creature.hitDice.setHitDice(test);	// will fire property change that will trigger update
				}
				hitDiceField.setBackground(defaultBG);
			} catch(Exception e) {
				System.err.println(e);
				hitDiceField.setBackground(Color.RED.brighter());
			}
		});

		hitPointsField = new JFormattedTextField();
		hitPointsField.setColumns(5);
		hitPointsField.addPropertyChangeListener("value", evt -> {
			if (evt.getPropertyName().equals("value")) {
				//TODO some type checking should be done
				if (creature != null) {
					int value = (Integer) hitPointsField.getValue();
					if (value < hitdice.getMinimum()) value = hitdice.getMinimum();
					if (value > hitdice.getMaximum()) value = hitdice.getMaximum();
					creature.setProperty(Creature.PROPERTY_MAXHPS, value);
				}
			}
		});
		hpRangeLabel = new JLabel();
		graph = new HPGraphPanel();
		statsLabel = new JLabel();

		averageHPsButton = new JRadioButton("Fixed average ()");
		averageHPsButton.setSelected(true);
		rollHPsButton = new JRadioButton("Straight roll");
		minHalfHPsButton = new JRadioButton("Roll, min 1/2 per die");
		ButtonGroup hpsGroup = new ButtonGroup();
		hpsGroup.add(averageHPsButton);
		hpsGroup.add(rollHPsButton);
		hpsGroup.add(minHalfHPsButton);

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> setHPs());
		// TODO apply to all monsters button
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(applyButton);

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		add(new JLabel("Hit Dice: "), c);
		c.gridwidth = 2;
		add(hitDiceField, c);

		c.gridy++;
		c.gridwidth = 1;
		add(new JLabel("Hit Points: "), c);
		add(hitPointsField, c);
		add(hpRangeLabel, c);

		c.gridy++;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0d;
		c.weighty = 1.0d;
		add(graph, c);

		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0d;
		c.weighty = 0d;
		add(statsLabel, c);

		// hp generation options:
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.gridwidth = 3;
		add(new JLabel("Set hit points to..."), c);

		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(averageHPsButton, c);
		add(rollHPsButton, c);
		add(minHalfHPsButton, c);

		c.gridwidth = 3;
		add(buttonPanel, c);
	}

	private class HPGraphPanel extends JPanel {
		HPGraphPanel() {
			setMinimumSize(new Dimension(200, 100));
			setPreferredSize(new Dimension(200, 100));
			addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					int len = hitdice.getMaximum() - hitdice.getMinimum() + 1;
					int hps = e.getX() * len / getSize().width + hitdice.getMinimum();
					creature.setMaximumHitPoints(hps);
				}
			});
		}

		void update() {
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Dimension d = getSize();
			g.clearRect(0, 0, d.width, d.height);

			if (creature == null) return;

			int first = hitdice.getNumber();
			int length = hitdice.getMaximum() - hitdice.getModifier() + 1;
			int possTotals = length - first;

			BigInteger max = hitdice.getCombinations(first);
			for (int i = first; i < length; i++) {
				if (hitdice.getCombinations(i) != null && hitdice.getCombinations(i).compareTo(max) > 0) max = hitdice.getCombinations(i);
			}
//			max = max.subtract(BigInteger.ONE);

			// stepwise
			Polygon p = new Polygon();
			int prevX = 0;
			p.addPoint(prevX, d.height - 1);
			for (int i = first; i < length; i++) {
				int x = ((i - first + 1) * (d.width - 1)) / possTotals;
				//int y = d.height - 1 - (int) ((d.height - 1) * (probs[i] - 1) / max);
				int y;
				if (max.equals(BigInteger.ONE)) {
					// no result has more than one combination - we'll draw 1 combination results as a bar 3/4 of the height of the graph
					y = d.height - 1 - hitdice.getCombinations(i).multiply(BigInteger.valueOf(3 * (d.height - 1))).divide(BigInteger.valueOf(4)).intValue();
				} else {
					y = d.height - 1 - hitdice.getCombinations(i).multiply(BigInteger.valueOf(d.height - 1)).divide(max).intValue();
				}
				p.addPoint(prevX, y);
				p.addPoint(x, y);
				prevX = x;
			}
			p.addPoint(prevX, d.height - 1);

			g.setColor(new Color(255, 150, 150));
			g.fillPolygon(p);
			g.setColor(Color.RED);
			g.drawPolygon(p);

			int hps = creature.getMaximumHitPoints() - hitdice.getModifier();
			int minx = ((hps - first) * d.width) / possTotals;
			int maxx = ((hps - first + 1) * d.width) / possTotals;
			g.setColor(new Color(0f, 0f, 1f, 0.5f));
			g.fillRect(minx, 0, maxx - minx + 1, d.height);
		}
	};

	@Override
	void setMonster(Monster m) {
		if (creature == m) return;

		if (creature != null) {
			hps.removePropertyChangeListener(hpListener);
		}

		creature = m;

		if (creature != null) {
			hps = creature.getHPStatistic();
			hps.addPropertyChangeListener(hpListener);
			hitdice = DiceList.fromList(creature.getHitDice().getValue());
		} else {
			hps = null;
		}

		update();
	}

	private PropertyChangeListener hpListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			update();
		}
	};

	private void update() {
		if (creature != null) {
			int maxHPs = hps.getMaximumHitPoints();
			hitDiceField.setText(hitdice.toString());
			hitPointsField.setValue(new Integer(maxHPs));
			hpRangeLabel.setText("(" + hitdice.getMinimum() + " - " + hitdice.getMaximum() + ")");
			double stdDev = hitdice.getStdDeviation();
			double devs = Math.abs(maxHPs - hitdice.getMeanRoll()) / stdDev;
			statsLabel.setText(String.format("%dth percentile, %.2f standard deviations (std dev = %.2f)", getPercentile(maxHPs, hitdice), devs, stdDev));
			averageHPsButton.setText("Fixed average (" + hitdice.getMeanRoll() + ")");
		} else {
			hitDiceField.setText("");
			hitPointsField.setValue(new Integer(0));
			hpRangeLabel.setText("");
			statsLabel.setText("");
			averageHPsButton.setText("Fixed average");
		}
		graph.update();
	}

	// calculates percentile as the % of possible rolls that are less than hps. half of rolls equals to hps are counted
	// as less.
	private static int getPercentile(int hps, DiceList<HDDice> hd) {
		BigInteger total = BigInteger.ZERO;
		BigInteger larger = BigInteger.ZERO;
		BigInteger equal = BigInteger.ZERO;
		hps -= hd.getModifier();	// remove constant modifier so hps represents index

		int length = hd.getMaximum() - hd.getModifier();
		for (int i = 0; i < length; i++) {
			if (hd.getCombinations(i) != null) {
				total = total.add(hd.getCombinations(i));
				if (i > hps) larger = larger.add(hd.getCombinations(i));
				if (i == hps) equal = hd.getCombinations(i);
			}
		}

		BigInteger pct = (total.subtract(larger).subtract(equal.divide(BigInteger.valueOf(2))))
				.multiply(BigInteger.valueOf(100)).divide(total);
		//return (total - larger - equal / 2) * 100 / total;
		return pct.intValue();
	}

	private void setHPs() {
		int newVal = 0;
		if (averageHPsButton.isSelected()) {
			newVal = (int) hitdice.getMeanRoll();
		} else if (rollHPsButton.isSelected()) {
			newVal = hitdice.roll();
		} else if (minHalfHPsButton.isSelected()) {
			newVal = hitdice.rollMinHalf();
		}
		hps.setMaximumHitPoints(newVal);
	}
}

package monsters;

import gamesystem.HPs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import party.Creature;
import party.DetailedMonster;

@SuppressWarnings("serial")
class HitPointsPanel extends DetailPanel {
	private DetailedMonster creature;
	private HPs hps;

	private JTextField hitDiceField;
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
		hitDiceField.setEditable(false);	// TODO make this editable
		hitPointsField = new JFormattedTextField();
		hitPointsField.setColumns(5);
		hitPointsField.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("value")) {
					//TODO some type checking should be done
					if (creature != null) {
						int value = (Integer) hitPointsField.getValue();
						HitDice hitdice = creature.getHitDice();
						if (value < hitdice.getMinimumRoll()) value = hitdice.getMinimumRoll();
						if (value > hitdice.getMaximumRoll()) value = hitdice.getMaximumRoll();
						creature.setProperty(Creature.PROPERTY_MAXHPS, value);
					}
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
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setHPs();
			}
		});
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
		}

		void update() {
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Dimension d = getSize();
			g.clearRect(0, 0, d.width, d.height);

			if (creature == null) return;
			HitDice hd = creature.getHitDice();
			long[] probs = hd.getProbabilities();

			int first = hd.getNumber();

			long max = probs[first];
			for (int i = first; i < probs.length; i++) {
				if (probs[i] > max) max = probs[i];
			}
			max--;

			// stepwise
			Polygon p = new Polygon();
			int prevX = -1;
			for (int i = first; i < probs.length; i++) {
				int x = ((i - first + 1) * d.width) / (probs.length - first);
				int y = d.height - 1 - (int) ((d.height - 1) * (probs[i] - 1) / max);
				p.addPoint(prevX, y);
				p.addPoint(x, y);
				prevX = x;
			}

			g.setColor(new Color(255, 150, 150));
			g.fillPolygon(p);
			g.setColor(Color.RED);
			g.drawPolygon(p);

			int hps = creature.getMaximumHitPoints() - hd.getModifier();
			int minx = ((hps - first) * d.width) / (probs.length - first);
			int maxx = ((hps - first + 1) * d.width) / (probs.length - first);
			g.setColor(new Color(0f, 0f, 1f, 0.5f));
			g.fillRect(minx, 0, maxx - minx + 1, d.height - 1);
		}
	};

	@Override
	void setMonster(DetailedMonster m) {
		if (creature == m) return;

		if (creature != null) {
			hps.removePropertyChangeListener(listener);
		}

		creature = m;

		if (creature != null) {
			hps = (HPs) creature.getStatistic(Creature.STATISTIC_HPS);
			hps.addPropertyChangeListener(listener);
		} else {
			hps = null;
		}

		update();
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			update();
		}
	};

	private void update() {
		if (creature != null) {
			HitDice hitdice = creature.getHitDice();
			int maxHPs = hps.getMaximumHitPoints();
			hitDiceField.setText(hitdice.toString());
			hitPointsField.setValue(new Integer(maxHPs));
			hpRangeLabel.setText("(" + hitdice.getMinimumRoll() + " - " + hitdice.getMaximumRoll() + ")");
			double stdDev = hitdice.getStdDeviation();
			double devs = Math.abs(maxHPs - hitdice.getMeanRoll()) / stdDev;
			statsLabel.setText(String.format("%dth percentile, %.2f standard deviations (std dev = %.2f)", getPercentile(maxHPs, hitdice), devs, stdDev));
			averageHPsButton.setText("Fixed average (" + (int) hitdice.getMeanRoll() + ")");
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
	private static int getPercentile(int hps, HitDice hd) {
		long[] probs = hd.getProbabilities();

		BigInteger total = BigInteger.ZERO;
		BigInteger larger = BigInteger.ZERO;
		BigInteger equal = BigInteger.ZERO;
		hps -= hd.getModifier();	// remove constant modifier so hps represents index

		for (int i = 0; i < probs.length; i++) {
			total = total.add(BigInteger.valueOf(probs[i]));
			if (i > hps) larger = larger.add(BigInteger.valueOf(probs[i]));
			if (i == hps) equal = BigInteger.valueOf(probs[i]);
		}

		BigInteger pct = (total.subtract(larger).subtract(equal.divide(BigInteger.valueOf(2))))
				.multiply(BigInteger.valueOf(100)).divide(total);
		//return (total - larger - equal / 2) * 100 / total;
		return pct.intValue();
	}

	private void setHPs() {
		HitDice hitdice = creature.getHitDice();
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

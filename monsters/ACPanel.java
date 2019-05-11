package monsters;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.ImmutableModifier;
import gamesystem.Modifier;
import gamesystem.core.PropertyListener;
import monsters.EncounterDialog.MonsterData;

// TODO need to be able to set the dex limit for armor
// TODO add the ability to choose armor and shield items
@SuppressWarnings("serial")
class ACPanel extends DetailPanel {
	private Monster monster;

	JLabel totalAC = new JLabel();
	JLabel touchAC = new JLabel();
	JLabel ffAC = new JLabel();

	Map<String, JComponent> components = new HashMap<>();
	Map<String, Modifier> modifiers = new HashMap<>();
	String[] order = {
			Modifier.StandardType.SIZE.toString(),
			AbilityScore.Type.DEXTERITY.toString(),
			Modifier.StandardType.NATURAL_ARMOR.toString(),
			Modifier.StandardType.ARMOR.toString(),
			Modifier.StandardType.SHIELD.toString(),
			Modifier.StandardType.DEFLECTION.toString(),
			Modifier.StandardType.DODGE.toString(),
			Modifier.StandardType.INSIGHT.toString(),
			"Misc"
	};

	public ACPanel() {
		setLayout(new GridBagLayout());

		components.put(Modifier.StandardType.SIZE.toString(), new JLabel());
		components.put(AbilityScore.Type.DEXTERITY.toString(), new JLabel());
		components.put(Modifier.StandardType.NATURAL_ARMOR.toString(), new JSpinner());
		components.put(Modifier.StandardType.ARMOR.toString(), new JSpinner());
		components.put(Modifier.StandardType.SHIELD.toString(), new JSpinner());
		components.put(Modifier.StandardType.DEFLECTION.toString(), new JSpinner());
		components.put(Modifier.StandardType.DODGE.toString(), new JSpinner());
		components.put(Modifier.StandardType.INSIGHT.toString(), new JSpinner());
		components.put("Misc", new JSpinner());

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(1, 3, 1, 3);

		add(new JLabel("Total AC:"), c);
		add(totalAC, c);
		c.gridy++;
		add(new JLabel("Touch AC:"), c);
		add(touchAC, c);
		c.gridy++;
		add(new JLabel("Flatfooted AC:"), c);
		add(ffAC, c);
		c.gridy++;
		JPanel space = new JPanel();
		space.setPreferredSize(new Dimension(20, 20));
		add(space, c);
		c.gridy++;
		add(new JLabel("Modifier"), c);
		add(new JLabel("Value"), c);

		for (String label : order) {
			c.gridy++;
			add(new JLabel(label), c);
			JComponent comp = components.get(label);
			if (comp instanceof JSpinner) {
				JSpinner s = (JSpinner) comp;
				SpinnerNumberModel m = new SpinnerNumberModel(0, -40, 40, 1);
				s.setModel(m);
				s.addChangeListener(new SpinnerListener(label));
			}
			add(comp, c);
		}
	}

	@Override
	void setMonster(Monster m, MonsterData d) {
		if (monster == m) return;

		if (monster != null) {
			monster.getACStatistic().removePropertyListener(listener);
		}

		monster = m;

		if (monster != null) {
			monster.getACStatistic().addPropertyListener(listener);
		}

		update();
	}

	private void update() {
		if (monster == null) return;
		AC ac = monster.getACStatistic();
		totalAC.setText(Integer.toString(ac.getValue()));
		touchAC.setText(Integer.toString(ac.getTouchAC().getValue()));
		ffAC.setText(Integer.toString(ac.getFlatFootedAC().getValue()));

		Map<String, Integer> totals = new HashMap<>();
		totals.put(Modifier.StandardType.ARMOR.toString(), ac.getArmor().getValue());
		totals.put(Modifier.StandardType.SHIELD.toString(), ac.getShield().getValue());

		Map<Modifier, Boolean> mods = ac.getModifiers();
		for (Modifier mod : mods.keySet()) {
			if (!mods.get(mod)) continue;
			String type = mod.getType();
//			System.err.println("'" + type + "': " + mod.getModifier());
			if (type.equals(Modifier.StandardType.ARMOR.toString())) {
			} else if (type.equals(Modifier.StandardType.SHIELD.toString())) {
			} else if (type.equals(Modifier.StandardType.SIZE.toString())
					|| type.equals(AbilityScore.Type.DEXTERITY.toString())) {
				JLabel l = (JLabel) components.get(type);
				l.setText(mod.getModifier() >= 0 ? "+" + mod.getModifier() : Integer.toString(mod.getModifier()));
			} else {
				String t = type;
				if (!components.containsKey(type)) {
					// FIXME totaling unknown mod types as "misc" doesn't work because SpinnerListener.stateChanged() only checks to see if the actual misc mod matches. i.e. if we have mod type A then we set misc to the same value but then
					// SpinnerListener.stateChanged() will set a actual misc modifier to that value which results in update() being called which will then calculate the correct value of the misc modifier as A+misc (=A+A). this causes infinite recursion
					System.err.println("ACPanel: Unknown modifier type " + type);
					t = "Misc";
				}
				int total = 0;
				if (totals.containsKey(t)) total = totals.get(t);
				totals.put(t, total + mod.getModifier());
			}
		}

//		for (String type : totals.keySet()) {
//			System.err.println(type + ": " + totals.get(type));
//		}

		for (String t : components.keySet()) {
			JComponent c = components.get(t);
			if (c instanceof JSpinner) {
				if (totals.containsKey(t)) {
//					System.err.println("ACPanel update spinner for " + t + " to " + totals.get(t));
					((JSpinner) c).setValue(totals.get(t));
				} else {
					((JSpinner) c).setValue(0);
				}
			}
		}
	}

	final private PropertyListener listener = e -> update();

	class SpinnerListener implements ChangeListener {
		String modName;

		SpinnerListener(String modName) {
			this.modName = modName;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			JSpinner spinner = (JSpinner) e.getSource();
			int newVal = (Integer)spinner.getValue();
//			System.err.println("Spinner for " + modName + " changed to " + newVal);
			AC ac = monster.getACStatistic();

			if (modName.equals(Modifier.StandardType.ARMOR.toString())) {
				ac.getArmor().setBonus(newVal);

			} else if (modName.equals(Modifier.StandardType.SHIELD.toString())) {
				ac.getShield().setBonus(newVal);

			} else if (modName.equals(Modifier.StandardType.NATURAL_ARMOR.toString())) {
				monster.race.setNaturalArmor(ac, newVal);

			} else {
				Map<Modifier, Boolean> mods = ac.getModifiers();
				boolean found = false;
				for (Modifier mod : mods.keySet()) {
					if (modName.equals(mod.getType())) {
						if (mod.getModifier() == newVal && !found) {
							found = true;
						} else {
//							System.err.println("Removing " + mod + ", found = " + found);
							ac.removeModifier(mod);
						}
					}
				}
				if (!found) {
					Modifier m = new ImmutableModifier(newVal, modName, "user");
					ac.addModifier(m);
				}
			}
		}
	}
}
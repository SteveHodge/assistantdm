package combat;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import party.Creature;
import ui.Status;

@SuppressWarnings("serial")
abstract public class CombatEntry extends JPanel implements PropertyChangeListener {
	JFormattedTextField rollField;
	JComponent modifierComp;
	JFormattedTextField tiebreakField;
	private JFormattedTextField maxHPsField;
	private JFormattedTextField dmgField;
	JLabel total;
	JCheckBox onlyDM;
	JTextField nameField;
	JButton delete;
	private JButton apply, healAll;
	boolean blank = true;
	private JLabel currentHPs;
	JLabel acLabel, touchACLabel, flatFootedACLabel;
	JComponent acComp, touchACComp, flatFootedACComp;
	private JCheckBox nonLethal;
	private JPanel statusPanel = new JPanel();

	Creature creature;

	EventListenerList listenerList = new EventListenerList();
	private ChangeEvent changeEvent = null;
	ActionEvent actionEvent = null;

	public Creature getSource() {
		return creature;
	}

	void addActionListener(ActionListener l) {
		listenerList.add(ActionListener.class, l);
	}

	void removeActionListener(ActionListener l) {
		listenerList.remove(ActionListener.class, l);
	}

	void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	void applyDamage(int delta, boolean nonLethal) {
		// apply current damage
		if (nonLethal) {
			creature.setNonLethal(creature.getNonLethal() + delta);
		} else {
			creature.setWounds(creature.getWounds() + delta);
		}
	}

	void healAll() {
		// remove all damage
		creature.setWounds(0);
		creature.setNonLethal(0);
	}

	// Note: marks the entry as having been changed if it was blank, and if it has the roll is 0, randomly rolls
	// return true if it changed from blank to not-blank
	boolean initBlank() {
		if (blank) {
			blank = false;
			if (rollField.getValue().equals(0)) {
				Random r = new Random();
				rollField.setValue(r.nextInt(20)+1);
			}
			if (delete != null) {
				delete.setEnabled(true);
			}
		}
		return !blank;
	}

	void fireChange() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ChangeListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
			}
		}
	}

	void createPanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0; c.gridy = 0;
		c.gridheight = 3;
		c.fill = GridBagConstraints.VERTICAL;
		statusPanel.setMinimumSize(new Dimension(15,15));
		statusPanel.setPreferredSize(new Dimension(15,15));
		statusPanel.setMaximumSize(new Dimension(15,100));
		updateStatus();
		add(statusPanel,c);

		c.insets = new Insets(2, 3, 2, 3);
		c.gridx = 1; c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 8; c.gridheight = 1;
		add(createInitiativeSection(),c);

		c.fill = GridBagConstraints.NONE;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 0.0;
		add(new JLabel("HP: "), c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		String hps = ""+(creature.getMaximumHitPoints()-creature.getWounds()-creature.getNonLethal());
		if (creature.getNonLethal() != 0) hps += " ("+creature.getNonLethal()+")";
		currentHPs = new JLabel(hps);
		add(currentHPs, c);
		c.weightx = 0.0;
		add(new JLabel("Max: "), c);
		c.weightx = 1.0;
		maxHPsField = new JFormattedTextField();
		maxHPsField.setValue(new Integer(creature.getMaximumHitPoints()));
		maxHPsField.setColumns(4);
		maxHPsField.addPropertyChangeListener("value", this);
		add(maxHPsField, c);
		c.weightx = 0.0;
		add(new JLabel("Dmg:"), c);
		c.weightx = 1.0;
		dmgField = new JFormattedTextField();
		dmgField.setValue(new Integer(0));
		dmgField.setColumns(4);
		add(dmgField, c);
		c.weightx = 0.0;
		nonLethal = new JCheckBox("NL");
		add(nonLethal, c);
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		apply = new JButton("Apply");
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int delta = ((Integer) dmgField.getValue());
				applyDamage(delta, nonLethal.isSelected());
			}
		});
		add(apply, c);
		c.fill = GridBagConstraints.NONE;

		// AC components should have been setup by subclasses
		acLabel = new JLabel("AC: ");
		touchACLabel = new JLabel("Touch: ");
		flatFootedACLabel = new JLabel("Flat Footed:");
		c.gridx = 1; c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		add(acLabel, c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		add(acComp, c);
		c.weightx = 0.0;
		add(touchACLabel, c);
		c.weightx = 1.0;
		add(touchACComp, c);
		c.weightx = 0.0;
		add(flatFootedACLabel, c);
		c.weightx = 1.0;
		c.gridwidth = 2;
		add(flatFootedACComp, c);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		healAll = new JButton("Heal All");
		healAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				healAll();
			}
		});
		add(healAll, c);

		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	JComponent createInitiativeSection() {
		onlyDM = new JCheckBox();
		onlyDM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireChange();
			}
		});

		rollField = new JFormattedTextField();
		rollField.setValue(new Integer(0));
		rollField.setColumns(3);
		rollField.addPropertyChangeListener("value", this);

		tiebreakField = new JFormattedTextField();
		tiebreakField.setValue(new Integer(0));
		tiebreakField.setColumns(3);
		tiebreakField.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				fireChange();
			}
		});

		total = new JLabel("= XXX");
		Dimension size = total.getPreferredSize();
		total.setPreferredSize(size);
		total.setText("= 0");

		JPanel section = new JPanel();
		section.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(0, 3, 0, 3);
		section.add(onlyDM, c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		section.add(createNameSection(),c);
		c.weightx = 0.0;
		section.add(rollField, c);
		section.add(new JLabel("+"), c);
		section.add(modifierComp, c);
		section.add(total, c);
		section.add(tiebreakField, c);
		return section;
	}

	abstract JComponent createNameSection();

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == creature) {
			// update the relevant fields
			if (evt.getPropertyName().equals(Creature.PROPERTY_MAXHPS)) {
				maxHPsField.setValue(new Integer(creature.getMaximumHitPoints()));
				updateHPs();
			} else if (evt.getPropertyName().equals(Creature.PROPERTY_WOUNDS)
					|| evt.getPropertyName().equals(Creature.PROPERTY_NONLETHAL)) {
				updateHPs();
			} else if (evt.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
				total.setText("= "+getTotal());
				fireChange();
			}

		} else if (evt.getPropertyName().equals("value")) {
			if (evt.getSource() == maxHPsField) {
				creature.setMaximumHitPoints((Integer)maxHPsField.getValue());
				if (initBlank()) fireChange();
			} else if (evt.getSource() == rollField
					|| evt.getSource() == modifierComp) {
				// one of the initiative fields has changed
				total.setText("= "+getTotal());
				fireChange();
			}
		}
	}


	private void updateHPs() {
		String hps = ""+(creature.getMaximumHitPoints()-creature.getWounds()-creature.getNonLethal());
		if (creature.getNonLethal() != 0) hps += " ("+creature.getNonLethal()+")";
		currentHPs.setText(hps);
		updateStatus();
	}

	private void updateStatus() {
		Status status = Status.getStatus(creature.getMaximumHitPoints(), creature.getMaximumHitPoints()-creature.getWounds()-creature.getNonLethal());
		//System.out.println("Status = "+Status.descriptions[status]);
		statusPanel.setBackground(status.getColor());
		statusPanel.setToolTipText(status.toString());
	}

	int getTotal() {
		int tot = (Integer)rollField.getValue();
		tot += creature.getInitiativeModifier();
		return tot;
	}

	int getModifier() {
		return creature.getInitiativeModifier();
	}

	int getTieBreak() {
		return (Integer)tiebreakField.getValue();
	}

	int getRoll() {
		return (Integer)rollField.getValue();
	}

	void setRoll(int roll) {
		rollField.setValue(roll);
	}

	void setTieBreak(int roll) {
		tiebreakField.setValue(roll);
	}

	void adjustRoll(int delta) {
		rollField.setValue(getRoll()+delta);
	}

	String getCreatureName() {
		return creature.getName();
	}

	boolean isDMOnly() {
		return !onlyDM.isSelected();
	}

	@Override
	public String toString() {
		return "CombatEntry (name="+getCreatureName()+", roll="+rollField.getValue()
				+", modifier="+getModifier()+", tiebreak="+tiebreakField.getValue()+")";
	}

	boolean isBlank() {
		return blank;
	}

	// compares InitiativeEntrys for initiative order, i.e. highest total first, ties
	// broken by modifier and then tiebreak
	static int compareInitiatives(int total1, int mod1, int tie1, int total2, int mod2, int tie2) {
		if (total1 != total2) return total2 - total1;
		// totals the same, next check is modifiers
		if (mod1 != mod2) return mod2 - mod1;
		// totals and modifiers are the same, next check is tie break
		return tie2 - tie1;
	}

	// compares InitiativeEntrys for initiative order, i.e. highest total first, ties
	// broken by modifier and then tiebreak
	static int compareInitiatives(CombatEntry ie1, CombatEntry ie2) {
		if (ie2.isBlank()) {
			if (ie1.isBlank()) return 0;
			else return -1;
		}
		if (ie1.isBlank()) return 1;
		return compareInitiatives(ie1.getTotal(), ie1.getModifier(), ie1.getTieBreak(),
				ie2.getTotal(), ie2.getModifier(), ie2.getTieBreak());
	}

	abstract Element getElement(Document doc);
}

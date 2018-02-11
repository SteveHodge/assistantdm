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

import gamesystem.Creature;
import gamesystem.HPs;
import gamesystem.core.Property;
import gamesystem.core.PropertyListener;
import ui.Status;

@SuppressWarnings("serial")
abstract public class CombatEntry extends JPanel implements PropertyChangeListener, PropertyListener<Integer> {
	JFormattedTextField rollField;
	JComponent modifierComp;
	JFormattedTextField tiebreakField;
	private JFormattedTextField maxHPsField;
	JFormattedTextField dmgField;
	JLabel total;
	JCheckBox onlyDM;
	JTextField nameField;
	JButton delete;
	JButton apply, healAll;
	boolean blank = true;
	JLabel currentHPs;
	JLabel acLabel, touchACLabel, flatFootedACLabel;
	JComponent acComp, touchACComp, flatFootedACComp;
	JCheckBox nonLethal;
	private JPanel statusPanel = new JPanel();

	Creature creature;
	HPs hps;

	EventListenerList listenerList = new EventListenerList();
	private ChangeEvent changeEvent = null;
	ActionEvent actionEvent = null;

	CombatEntry(Creature c) {
		creature = c;
		hps = c.getHPStatistic();
		c.addPropertyListener(hps, this);
	}

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
			hps.getNonLethalProperty().setValue(hps.getNonLethal() + delta);
		} else {
			hps.getWoundsProperty().setValue(hps.getWounds() + delta);
		}
	}

	void healAll() {
		// remove all damage
		hps.getWoundsProperty().setValue(0);
		hps.getNonLethalProperty().setValue(0);
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

		createButtons();

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

		layoutHPComponents(c);

		layoutACComponents(c);

		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	void layoutHPComponents(GridBagConstraints c) {
		c.fill = GridBagConstraints.NONE;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 0.0;
		add(new JLabel("HP: "), c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		currentHPs = new JLabel(creature.getHPStatistic().getShortSummary());
		add(currentHPs, c);
		c.weightx = 0.0;
		add(new JLabel("Max: "), c);
		c.weightx = 1.0;
		maxHPsField = new JFormattedTextField();
		maxHPsField.setValue(new Integer(hps.getMaxHPStat().getValue()));
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
		if (apply != null) add(apply, c);
		c.fill = GridBagConstraints.NONE;
	}

	void layoutACComponents(GridBagConstraints c) {
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
		if (healAll != null) add(healAll, c);
	}

	abstract void createButtons();

	JComponent createInitiativeSection() {
		onlyDM = new JCheckBox();
		onlyDM.addActionListener(e -> fireChange());

		rollField = new JFormattedTextField();
		rollField.setValue(new Integer(0));
		rollField.setColumns(3);
		rollField.addPropertyChangeListener("value", this);

		tiebreakField = new JFormattedTextField();
		tiebreakField.setValue(new Integer(0));
		tiebreakField.setColumns(3);
		tiebreakField.addPropertyChangeListener("value", evt -> fireChange());

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
		if (evt.getPropertyName().equals("value")) {
			if (evt.getSource() == maxHPsField) {
				hps.getMaxHPStat().setMaximumHitPoints((Integer) maxHPsField.getValue());
				if (initBlank()) fireChange();
			} else if (evt.getSource() == rollField
					|| evt.getSource() == modifierComp) {
				// one of the initiative fields has changed
				total.setText("= "+getTotal());
				fireChange();
			}
		}
	}

	@Override
	public void propertyChanged(Property<Integer> source, Integer oldValue) {
		// update the relevant fields
		if (source == hps) {
			updateHPs();
		} else if (source == hps.getMaxHPStat()) {
			if (maxHPsField != null) maxHPsField.setValue(hps.getMaxHPStat().getValue());
			updateHPs();
		} else if (source == creature.getInitiativeStatistic()) {
			total.setText("= "+getTotal());
			fireChange();
		}
	}

	private void updateHPs() {
		currentHPs.setText(hps.getShortSummary());
		updateStatus();
	}

	// FIXME think the status should be based on hps.getHPs() rather than calculating current hps below
	private void updateStatus() {
		Status status = Status.getStatus(hps.getMaxHPStat().getValue(), hps.getMaxHPStat().getValue() - hps.getWounds() - hps.getNonLethal());
		//System.out.println("Status = "+Status.descriptions[status]);
		statusPanel.setBackground(status.getColor());
		statusPanel.setToolTipText(status.toString());
	}

	int getTotal() {
		int tot = (Integer)rollField.getValue();
		tot += creature.getInitiativeStatistic().getValue();
		return tot;
	}

	int getModifier() {
		return creature.getInitiativeStatistic().getValue();
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

	public boolean isBlank() {
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

	public abstract Element getElement(Document doc);
}

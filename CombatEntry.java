
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
import javax.swing.JToolTip;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;

import party.Creature;
import party.Character;

// TODO altering initiative doesn't update character on party tab

@SuppressWarnings("serial")
public class CombatEntry extends JPanel implements PropertyChangeListener, ActionListener {
	protected JFormattedTextField rollField;
	protected JFormattedTextField modifierField;
	protected JFormattedTextField tiebreakField;
	protected JFormattedTextField maxHPsField;
	protected JFormattedTextField dmgField;
	protected JLabel total;
	protected JCheckBox onlyDM;
	protected JTextField nameField;
	protected JButton delete, apply, healAll;
	protected boolean blank = true;
	protected JLabel currentHPs;
	protected JComponent acComp, touchACComp, flatFootedACComp;
	protected JCheckBox nonLethal;

	protected Creature creature;
	protected boolean editable;

	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent changeEvent = null;
	protected ActionEvent actionEvent = null;

	public CombatEntry(Creature creature) {
		this(creature,true);
	}

	public CombatEntry(Creature creature, boolean editable) {
		this.creature = creature;
		this.editable = editable;
		if (!editable) blank = false;
		creature.addPropertyChangeListener(this);
		createPanel();
	}

	public Creature getSource() {
		return creature;
	}

	public void addActionListener(ActionListener l) {
		listenerList.add(ActionListener.class, l);
	}

	public void removeActionListener(ActionListener l) {
		listenerList.remove(ActionListener.class, l);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Apply")) {
			// apply current damage
			int delta = ((Integer)dmgField.getValue());
			if (nonLethal.isSelected()) {
				creature.setNonLethal(creature.getNonLethal()+delta);
			} else {
				creature.setWounds(creature.getWounds()+delta);
			}
			return;

		} else if (e.getActionCommand().equals("Heal All")) {
			// remove all damage
			creature.setWounds(0);
			creature.setNonLethal(0);
			return;
		}

		// must be a delete button - fire a delete action to all listeners
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ChangeListener.class) {
				// Lazily create the event:
				if (actionEvent == null)
					actionEvent = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"delete",e.getWhen(),e.getModifiers());
				((ActionListener)listeners[i+1]).actionPerformed(actionEvent);
			}
		}
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	// Note: marks the entry as having been changed if it was blank, and if it has the roll is 0, randomly rolls
	protected void fireChange() {
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

	public JToolTip createToolTip() {
		JToolTip tip = new JToolTip() {
			public String getTipText() {
				if (creature instanceof Character) {
					Character c = (Character)creature;
					// get the AC components
					String components = "<html>Base AC: 10<br>";
					for (int i = 0; i < Creature.AC_MAX_INDEX; i++) {
						int v = c.getACComponent(i);
						if (v != 0) {
							components += Creature.getACComponentName(i) + ": " + v + "<br>";
						}
					}
					return components+"</html>";
				}
				return null;
			}
		};
        tip.setComponent(this);
        return tip;
	}

	protected void createPanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.insets = new Insets(2, 3, 2, 3);
		c.gridx = 0; c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 8;
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
		apply.addActionListener(this);
		add(apply, c);
		c.fill = GridBagConstraints.NONE;

		if (editable) {
			JFormattedTextField field = new JFormattedTextField();
			field.setValue(new Integer(0));
			field.setColumns(4);
			acComp = field;
			field = new JFormattedTextField();
			field.setValue(new Integer(0));
			field.setColumns(4);
			touchACComp = field;
			field = new JFormattedTextField();
			field.setValue(new Integer(0));
			field.setColumns(4);
			flatFootedACComp = field;
		} else {
			acComp = new JLabel(""+creature.getAC());
			touchACComp = new JLabel(""+creature.getTouchAC());
			flatFootedACComp = new JLabel(""+creature.getFlatFootedAC());
		}
		c.gridx = 0; c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		add(new JLabel("AC: "), c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		add(acComp, c);
		c.weightx = 0.0;
		add(new JLabel("Touch: "), c);
		c.weightx = 1.0;
		add(touchACComp, c);
		c.weightx = 0.0;
		add(new JLabel("Flat Footed:"), c);
		c.weightx = 1.0;
		c.gridwidth = 2;
		add(flatFootedACComp, c);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		healAll = new JButton("Heal All");
		healAll.addActionListener(this);
		add(healAll, c);

		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		if (creature instanceof Character) {
			setToolTipText("has tooltip"); // the text is irrelevant as we override the JToolTip (see above). this just forces the tip to appear
		}
	}

	protected JComponent createInitiativeSection() {
		onlyDM = new JCheckBox();
		onlyDM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireChange();
			}
		});

		rollField = new JFormattedTextField();
		rollField.setValue(new Integer(0));
		rollField.setColumns(3);
		rollField.addPropertyChangeListener("value", this);

		modifierField = new JFormattedTextField();
		modifierField.setValue(new Integer(0));
		modifierField.setColumns(3);
		modifierField.addPropertyChangeListener("value", this);

		tiebreakField = new JFormattedTextField();
		tiebreakField.setValue(new Integer(0));
		tiebreakField.setColumns(3);
		tiebreakField.addPropertyChangeListener("value", new PropertyChangeListener() {
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
		JComponent name;
		if (editable) {
			name = createEditableNameSection();
		} else {
			onlyDM.setSelected(true); // we assume an non-editable source is a character that should be visible
			int mod = creature.getInitiativeModifier();
			modifierField.setValue(mod);
			total.setText("= "+mod);
			name = new JLabel(creature.getName());
		}
		section.add(name,c);
		c.weightx = 0.0;
		section.add(rollField, c);
		section.add(new JLabel("+"), c);
		section.add(modifierField, c);
		section.add(total, c);
		section.add(tiebreakField, c);
		return section;
	}

	protected JComponent createEditableNameSection() {
		JPanel section = new JPanel();
		section.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		delete = new JButton("X");
		delete.setMargin(new Insets(0, 4, 0, 3));
		delete.setFocusPainted(false);
		delete.setEnabled(false);
		delete.addActionListener(this);
		section.add(delete, c);
		nameField = new JTextField(20);
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				fireChange();
			}

			public void insertUpdate(DocumentEvent e) {
				fireChange();
			}

			public void removeUpdate(DocumentEvent e) {
				fireChange();
			}
			
		});

		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		section.add(nameField, c);

		return section;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == creature) {
			// update the relevant fields
			if (evt.getPropertyName().equals(Creature.PROPERTY_MAXHPS)) {
				maxHPsField.setValue(new Integer(creature.getMaximumHitPoints()));
				updateHPs();
			} else if (evt.getPropertyName().equals(Creature.PROPERTY_AC)) {
				if (editable) {
					((JFormattedTextField)acComp).setValue(creature.getAC());
					((JFormattedTextField)touchACComp).setValue(creature.getTouchAC());
					((JFormattedTextField)flatFootedACComp).setValue(creature.getFlatFootedAC());
				} else {
					((JLabel)acComp).setText(""+creature.getAC());
					((JLabel)touchACComp).setText(""+creature.getTouchAC());
					((JLabel)flatFootedACComp).setText(""+creature.getFlatFootedAC());
				}
			} else if (evt.getPropertyName().equals(Creature.PROPERTY_WOUNDS)
					|| evt.getPropertyName().equals(Creature.PROPERTY_NONLETHAL)) {
				updateHPs();
			} else if (evt.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
				int mod = creature.getInitiativeModifier();
				modifierField.setValue(mod);
				total.setText("= "+mod);
			}

		} else if (evt.getPropertyName().equals("value")) {
			if (evt.getSource() == maxHPsField) {
				creature.setMaximumHitPoints((Integer)maxHPsField.getValue());
			} else {
				// one of the initiative fields has changed
				total.setText("= "+getTotal());
				fireChange();
			}
		}
	}

	protected void updateHPs() {
		String hps = ""+(creature.getMaximumHitPoints()-creature.getWounds()-creature.getNonLethal());
		if (creature.getNonLethal() != 0) hps += " ("+creature.getNonLethal()+")";
		currentHPs.setText(hps);
	}

	public int getTotal() {
		int tot = (Integer)rollField.getValue();
		tot += (Integer)modifierField.getValue();
		return tot;
	}

	public int getModifier() {
		return (Integer)modifierField.getValue();
	}

	public int getTieBreak() {
		return (Integer)tiebreakField.getValue();
	}

	public int getRoll() {
		return (Integer)rollField.getValue();
	}

	public void setRoll(int roll) {
		rollField.setValue(roll);
	}

	public void adjustRoll(int delta) {
		rollField.setValue(getRoll()+delta);
	}

	public String getCreatureName() {
		return creature.getName();
	}

	public boolean isDMOnly() {
		return !onlyDM.isSelected();
	}

	public String toString() {
		return "CombatEntry (name="+getCreatureName()+", roll="+rollField.getValue()
			+", modifier="+getModifier()+", tiebreak="+tiebreakField.getValue()+")";
	}

	public boolean isBlank() {
		return blank;
	}

	// compares InitiativeEntrys for initiative order, i.e. highest total first, ties
	// broken by modifier and then tiebreak
	protected static int compareInitiatives(int total1, int mod1, int tie1, int total2, int mod2, int tie2) {
		if (total1 != total2) return total2 - total1;
		// totals the same, next check is modifiers
		if (mod1 != mod2) return mod2 - mod1;
		// totals and modifiers are the same, next check is tie break
		return tie2 - tie1;
	}

	// compares InitiativeEntrys for initiative order, i.e. highest total first, ties
	// broken by modifier and then tiebreak
	public static int compareInitiatives(CombatEntry ie1, CombatEntry ie2) {
		if (ie2.isBlank()) {
			if (ie1.isBlank()) return 0;
			else return -1;
		}
		if (ie1.isBlank()) return 1;
		return compareInitiatives(ie1.getTotal(), ie1.getModifier(), ie1.getTieBreak(),
				ie2.getTotal(), ie2.getModifier(), ie2.getTieBreak());
	}
}
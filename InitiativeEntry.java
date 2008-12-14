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
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;

import swing.ReorderableListEntry;

// TODO cleanup architecture. protected constructor is ugly, createPanel is ugly, addNameSection is ugly
@SuppressWarnings("serial")
public class InitiativeEntry extends ReorderableListEntry implements PropertyChangeListener, ActionListener {
	protected JFormattedTextField rollField;
	protected JFormattedTextField modifierField;
	protected JFormattedTextField tiebreakField;
	protected JLabel total;
	protected JCheckBox onlyDM;
	protected JTextField nameField;
	protected JButton delete;
	protected boolean blank = true;

	EventListenerList listenerList = new EventListenerList();
	ChangeEvent changeEvent = null;
	ActionEvent actionEvent = null;

	protected InitiativeEntry(boolean initUI) {
		if (initUI) createPanel();
	}

	public InitiativeEntry() {
		this(true);
	}

	public void addActionListener(ActionListener l) {
		listenerList.add(ActionListener.class, l);
	}

	public void removeActionListener(ActionListener l) {
		listenerList.remove(ActionListener.class, l);
	}

	public void actionPerformed(ActionEvent e) {
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

	protected void createPanel() {
		setLayout(new GridBagLayout());

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

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		add(onlyDM, c);
		c.gridx = GridBagConstraints.RELATIVE;
		addNameSection(c);
		c.weightx = 0.0;
		add(rollField, c);
		add(new JLabel("+"), c);
		add(modifierField, c);
		add(total, c);
		add(tiebreakField, c);
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	protected void addNameSection(GridBagConstraints c) {
		delete = new JButton("X");
		delete.setMargin(new Insets(2, 4, 2, 3));
		delete.setFocusPainted(false);
		delete.setEnabled(false);
		delete.addActionListener(this);
		add(delete, c);
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
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		add(nameField, c);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		total.setText("= "+getTotal());
		fireChange();
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

	public String getName() {
		return nameField.getText();
	}

	public boolean isDMOnly() {
		return !onlyDM.isSelected();
	}

	public String toString() {
		return "InitiativeEntry (name="+getName()+", roll="+rollField.getValue()
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
	public static int compareInitiatives(InitiativeEntry ie1, InitiativeEntry ie2) {
		if (ie2.isBlank()) {
			if (ie1.isBlank()) return 0;
			else return -1;
		}
		if (ie1.isBlank()) return 1;
		return compareInitiatives(ie1.getTotal(), ie1.getModifier(), ie1.getTieBreak(),
				ie2.getTotal(), ie2.getModifier(), ie2.getTieBreak());
	}
}
